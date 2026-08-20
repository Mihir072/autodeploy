package com.autodeploy.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT configuration properties.
 * Bind via {@code @EnableConfigurationProperties(JwtProperties.class)} in each service,
 * or rely on the CommonAutoConfiguration if you're using the auto-config module.
 *
 * <pre>
 * jwt:
 *   secret: ${JWT_SECRET}
 *   expiration: 86400000        # 24h in ms
 *   refresh-expiration: 604800000  # 7d in ms
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Base64-encoded HMAC-SHA secret key (minimum 512 bits / 64 bytes for HS512). */
    private String secret;

    /** Access token TTL in milliseconds (default 24 hours). */
    private long expiration = 86_400_000L;

    /** Refresh token TTL in milliseconds (default 7 days). */
    private long refreshExpiration = 604_800_000L;
}
