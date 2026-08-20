package com.autodeploy.auth.security;

import com.autodeploy.auth.entity.User;
import com.autodeploy.auth.service.AuthService;
import com.autodeploy.common.config.JwtProperties;
import com.autodeploy.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Called by Spring Security after a successful GitHub OAuth2 authorization code exchange.
 *
 * <p>Steps performed:</p>
 * <ol>
 *   <li>Retrieve the GitHub access token from the {@code OAuth2AuthorizedClient}</li>
 *   <li>Encrypt and persist the token in the user record</li>
 *   <li>Issue a platform access JWT + refresh token pair</li>
 *   <li>Persist the refresh token hash in the DB</li>
 *   <li>Redirect the browser to the frontend callback URL with the tokens as query params</li>
 * </ol>
 *
 * <p>The frontend should extract the tokens from the URL, store them securely
 * (e.g., HttpOnly cookie or secure storage), and discard the URL params.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend-redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, jakarta.servlet.ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        User user = principal.getUser();

        // 1. Retrieve and encrypt the GitHub access token
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName());
            if (client != null && client.getAccessToken() != null) {
                String rawGithubToken = client.getAccessToken().getTokenValue();
                authService.updateGithubAccessToken(user.getId(), rawGithubToken);
            }
        } catch (Exception ex) {
            // Non-fatal — user can still log in; GitHub API calls may fail until next login
            log.warn("Failed to store GitHub access token for user {}: {}", user.getId(), ex.getMessage());
        }

        // 2. Issue platform JWT pair
        String accessToken  = jwtTokenProvider.generateAccessToken(
                user.getId().toString(), user.getUsername(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId().toString());

        // 3. Persist refresh token (stored as SHA-256 hash)
        authService.saveRefreshToken(user, refreshToken);

        log.info("OAuth2 login successful for user: {} (id={})", user.getUsername(), user.getId());

        // 4. Clear authentication + redirect to frontend with token pair
        clearAuthenticationAttributes(request);

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl + "/auth/callback")
                .queryParam("access_token",  URLEncoder.encode(accessToken,  StandardCharsets.UTF_8))
                .queryParam("refresh_token", URLEncoder.encode(refreshToken, StandardCharsets.UTF_8))
                .queryParam("expires_in",    jwtProperties.getExpiration() / 1000)
                .build(true)   // already encoded
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
