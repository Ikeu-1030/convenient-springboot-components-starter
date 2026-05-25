package com.ikeu.components.security.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void bcryptEncode_producesDifferentFromRaw() {
        String hash = PasswordUtils.encode("myPassword");
        assertNotNull(hash);
        assertNotEquals("myPassword", hash);
        assertTrue(hash.startsWith("$2a$"));
    }

    @Test
    void bcryptMatches_correctPassword() {
        String hash = PasswordUtils.encode("secret123");
        assertTrue(PasswordUtils.matches("secret123", hash));
    }

    @Test
    void bcryptMatches_wrongPassword() {
        String hash = PasswordUtils.encode("secret123");
        assertFalse(PasswordUtils.matches("wrong", hash));
    }

    @Test
    void bcryptMatches_nullInputs() {
        assertFalse(PasswordUtils.matches(null, "hash"));
        assertFalse(PasswordUtils.matches("raw", null));
        assertFalse(PasswordUtils.matches(null, null));
    }

    @Test
    void aesEncryptDecrypt_roundTrip() {
        String key = "my16byteAESKey!!";
        String plain = "13800138000";
        String encrypted = PasswordUtils.encryptAes(plain, key);
        assertNotNull(encrypted);
        assertNotEquals(plain, encrypted);
        String decrypted = PasswordUtils.decryptAes(encrypted, key);
        assertEquals(plain, decrypted);
    }

    @Test
    void aesEncrypt_nullInputs() {
        assertNull(PasswordUtils.encryptAes(null, "key"));
        assertNull(PasswordUtils.encryptAes("text", null));
    }

    @Test
    void aesDecrypt_nullInputs() {
        assertNull(PasswordUtils.decryptAes(null, "key"));
        assertNull(PasswordUtils.decryptAes("text", null));
    }

    @Test
    void aesEncrypt_differentKeysDifferentResults() {
        String plain = "13800138000";
        String encrypted1 = PasswordUtils.encryptAes(plain, "keyOne16bytes!!!");
        String encrypted2 = PasswordUtils.encryptAes(plain, "keyTwo16bytes!!!");
        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void aesEncrypt_shortKey() {
        // Key shorter than 16 bytes should be padded
        String encrypted = PasswordUtils.encryptAes("hello", "short");
        assertNotNull(encrypted);
        String decrypted = PasswordUtils.decryptAes(encrypted, "short");
        assertEquals("hello", decrypted);
    }
}