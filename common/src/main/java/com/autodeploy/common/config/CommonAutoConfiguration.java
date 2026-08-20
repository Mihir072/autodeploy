package com.autodeploy.common.config;

import com.autodeploy.common.security.JwtTokenProvider;
import com.autodeploy.common.util.AesEncryptionUtil;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for the common library.
 * Automatically registers {@link JwtTokenProvider} and {@link AesEncryptionUtil}
 * in any Spring Boot service that has this library on the classpath.
 *
 * <p>Registered via {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}</p>
 */
@AutoConfiguration
@EnableConfigurationProperties(JwtProperties.class)
public class CommonAutoConfiguration {

    /**
     * Registers the {@link JwtTokenProvider} bean.
     * Requires {@code jwt.secret} to be configured.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "jwt", name = "secret")
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    /**
     * Registers the {@link AesEncryptionUtil} bean.
     * Requires {@code encryption.key} to be configured.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "encryption", name = "key")
    public AesEncryptionUtil aesEncryptionUtil(EncryptionProperties encryptionProperties) {
        return new AesEncryptionUtil(encryptionProperties.getKey());
    }
}
