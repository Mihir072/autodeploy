package com.autodeploy.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Encryption configuration properties bound from {@code encryption.key}.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {

    /** Base64-encoded 32-byte AES-256 key. Generate with: {@code openssl rand -base64 32} */
    private String key;
}
