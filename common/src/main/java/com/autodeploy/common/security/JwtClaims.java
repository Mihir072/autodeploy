package com.autodeploy.common.security;

/**
 * Immutable record holding the claims extracted from a validated JWT.
 * Used across services whenever a token needs to be inspected.
 *
 * @param userId   The platform user UUID (JWT "sub" claim)
 * @param username The GitHub username
 * @param email    The user's email
 * @param tokenType  Either "access" or "refresh"
 */
public record JwtClaims(
        String userId,
        String username,
        String email,
        String tokenType
) {
    public boolean isAccessToken() {
        return "access".equals(tokenType);
    }

    public boolean isRefreshToken() {
        return "refresh".equals(tokenType);
    }
}
