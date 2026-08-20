package com.autodeploy.auth.service;

import com.autodeploy.auth.dto.AuthResponse;
import com.autodeploy.auth.entity.RefreshToken;
import com.autodeploy.auth.entity.User;
import com.autodeploy.auth.repository.RefreshTokenRepository;
import com.autodeploy.auth.repository.UserRepository;
import com.autodeploy.common.config.JwtProperties;
import com.autodeploy.common.exception.UnauthorizedException;
import com.autodeploy.common.security.JwtTokenProvider;
import com.autodeploy.common.util.AesEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 * All dependencies are mocked — no Spring context or database required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock AesEncryptionUtil aesEncryptionUtil;

    @InjectMocks AuthService authService;

    // JwtProperties is not a mock — use a real instance with test values
    JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("dGVzdC1zZWNyZXQtdGhhdC1pcy1sb25nLWVub3VnaC1mb3ItaG1hYy1zaGE1MTItc2lnbmluZy10ZXN0LW9ubHk=");
        jwtProperties.setExpiration(3_600_000L);          // 1 hour
        jwtProperties.setRefreshExpiration(86_400_000L);  // 24 hours

        // Inject jwtProperties manually since @InjectMocks won't find it (not a mock)
        var field = findField(AuthService.class, "jwtProperties");
        field.setAccessible(true);
        try { field.set(authService, jwtProperties); }
        catch (IllegalAccessException e) { throw new RuntimeException(e); }
    }

    // ─── findOrCreateUser ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("findOrCreateUser")
    class FindOrCreateUserTests {

        @Test
        @DisplayName("creates new user when GitHub ID not found")
        void createsNewUser_whenNotExists() {
            when(userRepository.findByGithubId(12345L)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = authService.findOrCreateUser(12345L, "octocat", "octo@example.com", "http://avatar.url");

            assertThat(result.getGithubId()).isEqualTo(12345L);
            assertThat(result.getUsername()).isEqualTo("octocat");
            assertThat(result.getEmail()).isEqualTo("octo@example.com");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("updates profile of existing user")
        void updatesExistingUser_whenFound() {
            User existing = testUser();
            existing.setUsername("old-name");
            when(userRepository.findByGithubId(existing.getGithubId())).thenReturn(Optional.of(existing));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            User result = authService.findOrCreateUser(
                existing.getGithubId(), "new-name", "new@email.com", "http://new.avatar");

            assertThat(result.getUsername()).isEqualTo("new-name");
            assertThat(result.getEmail()).isEqualTo("new@email.com");
            verify(userRepository).save(existing);
        }
    }

    // ─── refreshTokens ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("refreshTokens")
    class RefreshTokensTests {

        @Test
        @DisplayName("returns new token pair for a valid refresh token")
        void returnsNewPair_whenTokenIsValid() {
            String rawToken = "valid-refresh-token";
            String tokenHash = authService.hashToken(rawToken);
            User user = testUser();

            RefreshToken stored = validRefreshToken(user, tokenHash);
            when(refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash))
                    .thenReturn(Optional.of(stored));
            when(jwtTokenProvider.generateAccessToken(any(), any(), any()))
                    .thenReturn("new-access-token");
            when(jwtTokenProvider.generateRefreshToken(any()))
                    .thenReturn("new-refresh-token");
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse result = authService.refreshTokens(rawToken);

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(result.tokenType()).isEqualTo("Bearer");
            assertThat(stored.isRevoked()).isTrue();                // Old token must be revoked
            verify(refreshTokenRepository, times(2)).save(any());  // revoke old + save new
        }

        @Test
        @DisplayName("throws UnauthorizedException for an invalid token hash")
        void throwsUnauthorized_whenTokenNotFound() {
            when(refreshTokenRepository.findByTokenHashAndRevokedFalse(any()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshTokens("unknown-token"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("Invalid or revoked");
        }

        @Test
        @DisplayName("throws UnauthorizedException and revokes when token is expired")
        void throwsAndRevokes_whenTokenExpired() {
            String rawToken = "expired-refresh-token";
            String tokenHash = authService.hashToken(rawToken);
            User user = testUser();

            RefreshToken expired = validRefreshToken(user, tokenHash);
            expired.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
            when(refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash))
                    .thenReturn(Optional.of(expired));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            assertThatThrownBy(() -> authService.refreshTokens(rawToken))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("expired");

            assertThat(expired.isRevoked()).isTrue();  // Should be revoked even on failure
            verify(refreshTokenRepository).save(expired);
        }
    }

    // ─── logout ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("logout")
    class LogoutTests {

        @Test
        @DisplayName("revokes specific token when refresh token is provided")
        void revokesSpecificToken_whenRefreshTokenProvided() {
            String rawToken = "my-refresh-token";
            String tokenHash = authService.hashToken(rawToken);
            User user = testUser();

            RefreshToken stored = validRefreshToken(user, tokenHash);
            when(refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash))
                    .thenReturn(Optional.of(stored));
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            authService.logout(user.getId(), rawToken);

            assertThat(stored.isRevoked()).isTrue();
            verify(refreshTokenRepository, never()).revokeAllByUserId(any());
        }

        @Test
        @DisplayName("revokes all tokens when no refresh token is provided")
        void revokesAllTokens_whenNoRefreshTokenProvided() {
            UUID userId = UUID.randomUUID();
            when(refreshTokenRepository.revokeAllByUserId(userId)).thenReturn(3);

            authService.logout(userId, null);

            verify(refreshTokenRepository).revokeAllByUserId(userId);
            verify(refreshTokenRepository, never()).findByTokenHashAndRevokedFalse(any());
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private User testUser() {
        User user = new User(99L, "octocat", "octo@example.com", "https://avatar.url");
        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, UUID.randomUUID());
        } catch (Exception e) { throw new RuntimeException(e); }
        return user;
    }

    private RefreshToken validRefreshToken(User user, String tokenHash) {
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(tokenHash);
        rt.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        rt.setRevoked(false);
        return rt;
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        try { return clazz.getDeclaredField(fieldName); }
        catch (NoSuchFieldException e) { throw new RuntimeException(e); }
    }
}
