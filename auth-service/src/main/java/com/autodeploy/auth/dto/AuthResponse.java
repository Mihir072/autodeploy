package com.autodeploy.auth.dto;

/**
 * Response returned after successful authentication (OAuth2 callback or token refresh).
 *
 * <pre>
 * {
 *   "accessToken":  "eyJhbGci...",
 *   "refreshToken": "eyJhbGci...",
 *   "tokenType":    "Bearer",
 *   "expiresIn":    86400,
 *   "user": { ... }
 * }
 * </pre>
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        /** Access token TTL in seconds. */
        long expiresIn,
        UserDto user
) {
    public static AuthResponse of(String accessToken, String refreshToken,
                                   long expiresInMs, UserDto user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInMs / 1000, user);
    }
}
