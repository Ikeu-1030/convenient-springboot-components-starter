package com.ikeu.components.security.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password encryption utilities: BCrypt hashing and AES symmetric encryption.
 *
 * <h3>BCrypt</h3>
 * Uses {@code BCryptPasswordEncoder} (strength 10). The salt is embedded in the hash —
 * each call to {@code encode()} produces a different hash for the same input.
 * <pre>{@code
 * String hash = PasswordUtils.encode("myPassword");
 * boolean match = PasswordUtils.matches("myPassword", hash);  // true
 * }</pre>
 *
 * <h3>AES (CBC mode, PKCS5 padding)</h3>
 * For encrypting sensitive data like phone numbers or ID cards. A random IV
 * (16 bytes) is prepended to the ciphertext, then the whole is Base64-encoded.
 * <pre>{@code
 * String cipher = PasswordUtils.aesEncrypt("13800138000", "my16byteAESKey!!");
 * String plain = PasswordUtils.aesDecrypt(cipher, "my16byteAESKey!!");
 * }</pre>
 * <p>
 * <b>Key length:</b> keys shorter than 16 bytes are right-padded with '0';
 * keys longer than 16 bytes are truncated. Provide exactly 16 bytes for best results.
 *
 * @author ikeu
 * @since 1.0.0
 */
@Slf4j
public final class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    private static final String AES_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    private PasswordUtils() {
    }

    /**
     * Encode a raw password using BCrypt.
     */
    public static String encode(String raw) {
        return ENCODER.encode(raw);
    }

    /**
     * Verify a raw password against a BCrypt hash.
     */
    public static boolean matches(String raw, String encoded) {
        if (raw == null || encoded == null) {
            return false;
        }
        return ENCODER.matches(raw, encoded);
    }

    /**
     * AES encrypt plain text with the given key. Returns Base64-encoded cipher text with IV prepended.
     */
    public static String encryptAes(String plainText, String secretKey) {
        if (plainText == null || secretKey == null) {
            return null;
        }
        try {
            byte[] keyBytes = getKeyBytes(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("AES encryption failed", e);
            throw new RuntimeException("AES encryption failed", e);
        }
    }

    /**
     * AES decrypt Base64-encoded cipher text (with IV prepended).
     */
    public static String decryptAes(String cipherText, String secretKey) {
        if (cipherText == null || secretKey == null) {
            return null;
        }
        try {
            byte[] keyBytes = getKeyBytes(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] combined = Base64.getDecoder().decode(cipherText);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES decryption failed", e);
            throw new RuntimeException("AES decryption failed", e);
        }
    }

    private static byte[] getKeyBytes(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 16) {
            byte[] padded = new byte[16];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        } else if (keyBytes.length > 16 && keyBytes.length < 24) {
            // chop to 16 bytes for AES-128
            byte[] chopped = new byte[16];
            System.arraycopy(keyBytes, 0, chopped, 0, 16);
            keyBytes = chopped;
        } else if (keyBytes.length > 24 && keyBytes.length < 32) {
            byte[] chopped = new byte[24];
            System.arraycopy(keyBytes, 0, chopped, 0, 24);
            keyBytes = chopped;
        } else if (keyBytes.length > 32) {
            byte[] chopped = new byte[32];
            System.arraycopy(keyBytes, 0, chopped, 0, 32);
            keyBytes = chopped;
        }
        return keyBytes;
    }
}
