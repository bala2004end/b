package com.aidb.assistant.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for PasswordCryptoConverter verifying AES/GCM encryption behavior.
 */
class PasswordCryptoConverterTest {

    private PasswordCryptoConverter converter;

    @BeforeEach
    void setUp() {
        // 32+ character key — will be padded/truncated to 32 bytes internally
        converter = new PasswordCryptoConverter("test-secret-key-32-bytes-padding!");
    }

    @Test
    @DisplayName("Encryption and decryption roundtrip restores original password")
    void encryptAndDecrypt_roundtrip() {
        String original = "mySecurePassword123!";

        String encrypted = converter.convertToDatabaseColumn(original);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    @DisplayName("Same password encrypted twice produces different ciphertexts (GCM IV uniqueness)")
    void encrypt_twiceSameInput_produceDifferentCiphertext() {
        String password = "samePasswordEveryTime";

        String encrypted1 = converter.convertToDatabaseColumn(password);
        String encrypted2 = converter.convertToDatabaseColumn(password);

        // IV randomness ensures ciphertexts differ — this is the key AES/ECB vulnerability fix
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("Null input returns null (no exception)")
    void encrypt_nullInput_returnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("Blank string returns null")
    void encrypt_blankString_returnsNull() {
        assertThat(converter.convertToDatabaseColumn("")).isNull();
        assertThat(converter.convertToEntityAttribute("")).isNull();
    }

    @Test
    @DisplayName("Special characters and unicode in password are preserved")
    void encryptAndDecrypt_specialChars_preserved() {
        String special = "p@$$w0rd! 中文 ñoño ™ €100";

        String encrypted = converter.convertToDatabaseColumn(special);
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertThat(decrypted).isEqualTo(special);
    }

    @Test
    @DisplayName("Encrypted output is Base64-encoded string")
    void encrypted_isBase64() {
        String encrypted = converter.convertToDatabaseColumn("anypassword");

        // Base64 characters: A-Z, a-z, 0-9, +, /, =
        assertThat(encrypted).matches("^[A-Za-z0-9+/=]+$");
    }
}
