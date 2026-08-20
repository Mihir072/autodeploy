package com.autodeploy.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /auth/refresh}.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token must not be blank")
        String refreshToken
) {}
