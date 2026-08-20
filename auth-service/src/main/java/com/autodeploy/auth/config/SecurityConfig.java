package com.autodeploy.auth.config;

import com.autodeploy.auth.security.CustomOAuth2UserService;
import com.autodeploy.auth.security.OAuth2AuthenticationFailureHandler;
import com.autodeploy.auth.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for auth-service.
 *
 * <p>Design decisions:</p>
 * <ul>
 *   <li><strong>Sessions</strong>: required during the OAuth2 flow (Spring Security uses the
 *       session to preserve the CSRF/state parameter during the GitHub redirect round-trip).
 *       After the success handler redirects with JWT tokens, the session is discarded —
 *       the frontend uses JWTs for all subsequent requests.</li>
 *   <li><strong>Authorization</strong>: all paths are permitted at the service level because
 *       the API Gateway validates JWTs and injects {@code X-User-Id} headers. Downstream
 *       services trust that header. Direct access to auth-service (bypassing the gateway)
 *       is blocked by network policy in production.</li>
 *   <li><strong>CSRF</strong>: disabled for the REST endpoints (stateless, JWT-based).
 *       The OAuth2 state parameter serves as CSRF protection for the OAuth2 flow itself.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // Use sessions only for the OAuth2 state parameter — not for API auth
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // OAuth2 authorization endpoints — must be open
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // All other endpoints are accessed via the API gateway which
                        // validates JWT and injects X-User-Id — we trust that header
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
