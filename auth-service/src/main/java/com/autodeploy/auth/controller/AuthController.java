package com.autodeploy.auth.controller;

import com.autodeploy.auth.dto.AuthResponse;
import com.autodeploy.auth.dto.LogoutRequest;
import com.autodeploy.auth.dto.RefreshTokenRequest;
import com.autodeploy.auth.dto.UserDto;
import com.autodeploy.auth.service.AuthService;
import com.autodeploy.auth.service.UserService;
import com.autodeploy.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for authentication operations.
 *
 * <p>All endpoints except {@code /auth/refresh} are accessed via the API gateway,
 * which validates the JWT and injects the {@code X-User-Id} header.
 * This controller trusts that header as the authenticated user identity.</p>
 *
 * <p>Note: The OAuth2 login flow itself ({@code GET /oauth2/authorization/github})
 * is handled automatically by Spring Security — no controller method needed.</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "GitHub OAuth2 login, token management, and user profile")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Returns the profile of the currently authenticated user.
     *
     * <p>The {@code X-User-Id} header is injected by the API Gateway's JWT filter
     * after validating the access token.</p>
     *
     * <pre>GET /auth/me  →  200 { "data": { "id", "username", "email", "avatarUrl" } }</pre>
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
               description = "Returns the authenticated user's profile. Requires X-User-Id header (injected by API Gateway).")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId) {

        UserDto user = userService.findById(UUID.fromString(userId));
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Issues a new access token + refresh token pair using a valid refresh token.
     * The submitted refresh token is immediately revoked (rotation).
     *
     * <pre>POST /auth/refresh  →  200 { "data": { "accessToken", "refreshToken", ... } }</pre>
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token",
               description = "Validates the refresh token, rotates it, and returns a new JWT pair.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "New token pair issued"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid, revoked, or expired refresh token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse authResponse = authService.refreshTokens(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    /**
     * Revokes refresh token(s) and terminates the user's session.
     *
     * <p>If {@code refreshToken} is provided in the request body, only that session
     * is terminated. If omitted, all active sessions are revoked ("logout everywhere").</p>
     *
     * <pre>POST /auth/logout  →  200 { "success": true, "message": "Logged out successfully" }</pre>
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout",
               description = "Revokes the current session's refresh token. Omit body to revoke all sessions.")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) LogoutRequest request) {

        String refreshToken = (request != null) ? request.refreshToken() : null;
        authService.logout(UUID.fromString(userId), refreshToken);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
