package com.autodeploy.common.security;

import com.autodeploy.common.config.JwtProperties;
import com.autodeploy.common.exception.UnauthorizedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Provides JWT creation, signing (HMAC-SHA512), validation, and claims extraction.
 *
 * <p>This bean is registered automatically via {@link CommonAutoConfiguration}.
 * The JWT secret must be a Base64-encoded key of at least 512 bits (64 bytes).</p>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtProperties properties;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ─── Token Generation ────────────────────────────────────────────────────

    /**
     * Creates a signed access token with user identity claims.
     *
     * @param userId   platform user UUID
     * @param username GitHub username
     * @param email    user email
     */
    public String generateAccessToken(String userId, String username, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getExpiration());

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("email", email)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Creates a signed refresh token (minimal claims — just the user ID).
     *
     * @param userId platform user UUID
     */
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.getRefreshExpiration());

        return Jwts.builder()
                .subject(userId)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // ─── Token Validation & Parsing ─────────────────────────────────────────

    /**
     * Validates the token signature and expiry, then extracts claims.
     *
     * @param token the raw JWT string (without "Bearer " prefix)
     * @return parsed {@link JwtClaims}
     * @throws UnauthorizedException if the token is invalid or expired
     */
    public JwtClaims validateAndExtract(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new JwtClaims(
                    claims.getSubject(),
                    claims.get("username", String.class),
                    claims.get("email", String.class),
                    claims.get("type", String.class)
            );
        } catch (ExpiredJwtException ex) {
            log.debug("JWT token expired: {}", ex.getMessage());
            throw new UnauthorizedException("Token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            throw new UnauthorizedException("Invalid token");
        }
    }

    /**
     * Returns {@code true} if the token can be validated without throwing.
     */
    public boolean isValid(String token) {
        try {
            validateAndExtract(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Extracts the subject (userId) without full validation — use only when
     * you already know the token is expired and need to identify the user for refresh.
     */
    public String extractSubjectIgnoreExpiry(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (ExpiredJwtException ex) {
            return ex.getClaims().getSubject();
        }
    }
}
