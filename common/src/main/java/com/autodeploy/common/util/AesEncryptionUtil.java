package com.autodeploy.common.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256-GCM encryption utility for storing GitHub access tokens and
 * user-supplied environment variables at rest.
 *
 * <p>The encryption key must be a Base64-encoded 32-byte (256-bit) key.
 * Generate one with: {@code openssl rand -base64 32}</p>
 *
 * <p>Output format: {@code Base64(IV[12 bytes] || ciphertext+tag)}</p>
 *
 * <p>This bean is registered automatically via {@link com.autodeploy.common.config.CommonAutoConfiguration}.
 * Configure the key via {@code encryption.key=${ENCRYPTION_KEY}} in application.yml.</p>
 */
public class AesEncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    private final SecretKeySpec secretKeySpec;

    public AesEncryptionUtil(String base64EncodedKey) {
        byte[] keyBytes = Base64.getDecoder().decode(base64EncodedKey);
        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "AES key must be 32 bytes (256 bits). Got: " + keyBytes.length + " bytes.");
        }
        this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypts the given plaintext.
     *
     * @param plaintext the string to encrypt
     * @return Base64-encoded {@code IV || ciphertext+GCM-tag}
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV so we can extract it during decryption
            byte[] combined = new byte[GCM_IV_LENGTH_BYTES + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH_BYTES);
            System.arraycopy(ciphertext, 0, combined, GCM_IV_LENGTH_BYTES, ciphertext.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a value previously encrypted by {@link #encrypt(String)}.
     *
     * @param encryptedBase64 the Base64-encoded {@code IV || ciphertext+GCM-tag}
     * @return the original plaintext
     */
    public String decrypt(String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH_BYTES);

            byte[] ciphertext = new byte[combined.length - GCM_IV_LENGTH_BYTES];
            System.arraycopy(combined, GCM_IV_LENGTH_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
