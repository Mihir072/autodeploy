package com.autodeploy.auth.dto;

/**
 * Optional request body for {@code POST /auth/logout}.
 *
 * <p>If {@code refreshToken} is provided, only that specific session is revoked.
 * If omitted, <strong>all</strong> active sessions for the user are revoked.</p>
 */
public record LogoutRequest(
        String refreshToken
) {}
