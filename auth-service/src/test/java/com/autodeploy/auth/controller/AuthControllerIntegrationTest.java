package com.autodeploy.auth.controller;

import com.autodeploy.auth.entity.User;
import com.autodeploy.auth.repository.RefreshTokenRepository;
import com.autodeploy.auth.repository.UserRepository;
import com.autodeploy.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController}.
 *
 * <p>Uses a real PostgreSQL container (Testcontainers) with Flyway migrations applied.
 * GitHub OAuth2 is not tested here (requires real credentials) — only the REST endpoints
 * that operate on already-authenticated users.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("auth_db_test")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(12345L, "octocat", "octo@example.com", "https://github.com/images/octocat.png");
        testUser = userRepository.save(testUser);
    }

    // ─── GET /auth/me ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /auth/me returns 200 with user profile when X-User-Id header is valid")
    void getMe_validUserId_returnsUserProfile() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header("X-User-Id", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("octocat"))
                .andExpect(jsonPath("$.data.email").value("octo@example.com"))
                .andExpect(jsonPath("$.data.id").value(testUser.getId().toString()))
                // Sensitive fields must not be exposed
                .andExpect(jsonPath("$.data.githubAccessTokenEncrypted").doesNotExist());
    }

    @Test
    @DisplayName("GET /auth/me returns 404 when user does not exist")
    void getMe_nonExistentUser_returns404() throws Exception {
        mockMvc.perform(get("/auth/me")
                        .header("X-User-Id", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /auth/me returns 400 when X-User-Id is missing")
    void getMe_missingHeader_returns400() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().is4xxClientError());
    }

    // ─── POST /auth/refresh ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/refresh returns new token pair for a valid refresh token")
    void refresh_validToken_returnsNewPair() throws Exception {
        // Issue a real refresh token and store it
        String rawRefreshToken = authService
                .refreshTokens(issueInitialRefreshToken())
                .refreshToken(); // After first rotation — get a fresh token

        // We need a direct refresh token, not a rotated one
        // Issue fresh via helper
        String freshToken = issueInitialRefreshToken();

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", freshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").isNumber())
                .andExpect(jsonPath("$.data.user.username").value("octocat"));
    }

    @Test
    @DisplayName("POST /auth/refresh returns 401 for an invalid token")
    void refresh_invalidToken_returns401() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", "bogus-token"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("POST /auth/refresh returns 400 when refreshToken field is blank")
    void refresh_blankToken_returns400() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    // ─── POST /auth/logout ────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/logout revokes the specific token and returns 200")
    void logout_withToken_revokesSession() throws Exception {
        String refreshToken = issueInitialRefreshToken();

        mockMvc.perform(post("/auth/logout")
                        .header("X-User-Id", testUser.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));

        // Attempting to use the revoked token should fail
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/logout without body revokes all sessions")
    void logout_withoutBody_revokesAllSessions() throws Exception {
        // Issue 2 sessions
        String token1 = issueInitialRefreshToken();
        String token2 = issueInitialRefreshToken();

        mockMvc.perform(post("/auth/logout")
                        .header("X-User-Id", testUser.getId().toString()))
                .andExpect(status().isOk());

        // Both tokens should now be invalid
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", token1))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", token2))))
                .andExpect(status().isUnauthorized());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Creates a real refresh token in the DB for {@code testUser}. */
    private String issueInitialRefreshToken() {
        // Use AuthService's internal token hasher to generate a deterministic-looking raw token
        String rawToken = "test-refresh-token-" + System.nanoTime();
        authService.saveRefreshToken(testUser, rawToken);
        return rawToken;
    }
}
