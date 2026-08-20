package com.autodeploy.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import static com.autodeploy.gateway.filter.JwtAuthenticationFilter.HEADER_USER_ID;

/**
 * API Gateway configuration.
 * Provides the {@code KeyResolver} bean used by Spring Cloud Gateway's
 * {@code RequestRateLimiter} filter to identify rate-limit buckets per user.
 */
@Configuration
public class GatewayConfig {

    /**
     * Rate-limit key resolver: use the {@code X-User-Id} header injected by
     * the {@link com.autodeploy.gateway.filter.JwtAuthenticationFilter}.
     * Falls back to the remote IP for unauthenticated (public) routes.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst(HEADER_USER_ID);
            if (userId != null && !userId.isBlank()) {
                return Mono.just(userId);
            }
            // Fallback: use remote address for public paths
            String remoteAddr = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(remoteAddr);
        };
    }
}
