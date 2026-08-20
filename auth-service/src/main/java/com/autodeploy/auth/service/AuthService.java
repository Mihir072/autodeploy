package com.autodeploy.auth.service;

import com.autodeploy.auth.dto.AuthResponse;
import com.autodeploy.auth.dto.UserDto;
import com.autodeploy.auth.entity.RefreshToken;
import com.autodeploy.auth.entity.User;
import com.autodeploy.auth.repository.RefreshTokenRepository;
import com.autodeploy.auth.repository.UserRepository;
import com.autodeploy.common.config.JwtProperties;
import com.autodeploy.common.exception.UnauthorizedException;
import com.autodeploy.common.security.JwtTokenProvider;
import com.autodeploy.common.util.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Core authentication service.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>User upsert on OAuth2 login</li>
 *   <li>Encrypting and persisting GitHub access tokens</li>
 *   <li>Refresh token lifecycle (create → verify → rotate → revoke)</li>
 *   <li>Logout (single-session or all-session revocation)</li>
 *   <li>Scheduled cleanup of expired tokens</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AesEncryptionUtil aesEncryptionUtil;

    // ─── User Management ──────────────────────────────────────────────────────

    /**
     * Creates a new user or updates the profile of an existing one identified by
     * their GitHub ID. Called on every successful OAuth2 login.
     */
    public User findOrCreateUser(Long githubId, String username, String email, String avatarUrl) {
        return userRepository.findByGithubId(githubId)
                .map(existingUser -> {
                    // Sync GitHub profile fields that may have changed
                    existingUser.setUsername(username);
                    if (email != null) existingUser.setEmail(email);
                    if (avatarUrl != null) existingUser.setAvatarUrl(avatarUrl);
                    log.debug("Updated existing user: {} (githubId={})", username, githubId);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    User newUser = new User(githubId, username, email, avatarUrl);
                    log.info("Created new user: {} (githubId={})", username, githubId);
                    return userRepository.save(newUser);
                });
    }

    /**
     * Encrypts and stores the user's GitHub OAuth2 access token.
     * Called after the success handler retrieves the token from {@code OAuth2AuthorizedClient}.
     */
    public void updateGithubAccessToken(UUID userId, String rawToken) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setGithubAccessTokenEncrypted(aesEncryptionUtil.encrypt(rawToken));
            userRepository.save(user);
        });
    }

    /**
     * Decrypts and returns the stored GitHub access token for the given user.
     * Used by project-service (via Feign) when making GitHub API calls.
     *
     * @throws UnauthorizedException if no token is stored
     */
    @Transactional(readOnly = true)
    public String getDecryptedGithubToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (user.getGithubAccessTokenEncrypted() == null) {
            throw new UnauthorizedException("No GitHub access token stored — please re-authenticate");
        }
        return aesEncryptionUtil.decrypt(user.getGithubAccessTokenEncrypted());
    }

    // ─── Refresh Token Lifecycle ──────────────────────────────────────────────

    /**
     * Persists a new refresh token (stored as its SHA-256 hash).
     *
     * @param user     the owning user
     * @param rawToken the raw JWT refresh token (returned to client, never stored raw)
     */
    public void saveRefreshToken(User user, String rawToken) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hashToken(rawToken));
        token.setExpiresAt(Instant.now().plusMillis(jwtProperties.getRefreshExpiration()));
        refreshTokenRepository.save(token);
        log.debug("Saved refresh token for user: {}", user.getId());
    }

    /**
     * Validates a refresh token, rotates it, and returns a new JWT pair.
     *
     * <p>Rotation ensures that stolen refresh tokens are detected — once used,
     * the old token is immediately revoked. Presenting a revoked token is a
     * potential theft indicator.</p>
     *
     * @param rawRefreshToken the raw token from the client
     * @return a new {@link AuthResponse} with fresh access + refresh tokens
     * @throws UnauthorizedException if the token is invalid, revoked, or expired
     */
    public AuthResponse refreshTokens(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid or revoked refresh token"));

        if (stored.isExpired()) {
            // Revoke even if the caller didn't, to clean up
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new UnauthorizedException("Refresh token has expired — please log in again");
        }

        User user = stored.getUser();

        // Rotate: revoke the consumed token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // Issue a new pair
        String newAccessToken  = jwtTokenProvider.generateAccessToken(
                user.getId().toString(), user.getUsername(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());
        saveRefreshToken(user, newRefreshToken);

        log.debug("Rotated refresh token for user: {}", user.getId());
        return AuthResponse.of(newAccessToken, newRefreshToken,
                               jwtProperties.getExpiration(), UserDto.from(user));
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    /**
     * Revokes one or all refresh tokens for the user.
     *
     * @param userId         the authenticated user's ID
     * @param rawRefreshToken if provided, only this session is revoked; otherwise all sessions
     */
    public void logout(UUID userId, String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            String tokenHash = hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                    .ifPresent(rt -> {
                        rt.setRevoked(true);
                        refreshTokenRepository.save(rt);
                        log.info("Single-session logout for user: {}", userId);
                    });
        } else {
            int count = refreshTokenRepository.revokeAllByUserId(userId);
            log.info("Full logout ({} sessions revoked) for user: {}", count, userId);
        }
    }

    // ─── Scheduled Maintenance ────────────────────────────────────────────────

    /**
     * Purges expired refresh tokens from the database.
     * Runs every day at 03:00 UTC to keep the table small.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(Instant.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired refresh tokens", deleted);
        }
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    /**
     * Produces a SHA-256 hex digest of the raw token.
     * Using a 64-char hex string that fits the DB column (length=64).
     */
    String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available on this JVM", e);
        }
    }
}
