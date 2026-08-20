package com.autodeploy.gateway.filter;

import com.autodeploy.common.security.JwtClaims;
import com.autodeploy.common.security.JwtTokenProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Global JWT authentication filter for the Spring Cloud Gateway.
 *
 * <p>For every request:</p>
 * <ol>
 *   <li>Checks if the path is in the public whitelist (bypass JWT check)</li>
 *   <li>Extracts the {@code Authorization: Bearer <token>} header</li>
 *   <li>Validates the token using {@link JwtTokenProvider}</li>
 *   <li>Forwards the user's identity to downstream services as headers:
 *       {@code X-User-Id}, {@code X-Username}, {@code X-User-Email}</li>
 *   <li>Returns 401 if the token is missing, expired, or invalid</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Value("${app.security.public-paths:#{T(java.util.List).of('/oauth2/**','/login/oauth2/**','/api/auth/refresh','/actuator/**','/webjars/**','/v3/api-docs/**')}}")
    private List<String> publicPaths;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // ─── Headers forwarded to downstream services ────────────────────────────
    public static final String HEADER_USER_ID    = "X-User-Id";
    public static final String HEADER_USERNAME   = "X-Username";
    public static final String HEADER_USER_EMAIL = "X-User-Email";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip auth for whitelisted paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeUnauthorizedResponse(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            JwtClaims claims = jwtTokenProvider.validateAndExtract(token);

            if (!claims.isAccessToken()) {
                return writeUnauthorizedResponse(exchange, "Refresh tokens cannot be used for API access");
            }

            // Mutate request to add user identity headers for downstream services
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(HEADER_USER_ID, claims.userId())
                    .header(HEADER_USERNAME, claims.username())
                    .header(HEADER_USER_EMAIL, claims.email() != null ? claims.email() : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception ex) {
            log.debug("JWT validation failed for path {}: {}", path, ex.getMessage());
            return writeUnauthorizedResponse(exchange, ex.getMessage());
        }
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private Mono<Void> writeUnauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "success", false,
                "error", "UNAUTHORIZED",
                "message", message,
                "path", exchange.getRequest().getPath().value(),
                "timestamp", Instant.now().toString()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;  // Run before all other filters
    }
}
