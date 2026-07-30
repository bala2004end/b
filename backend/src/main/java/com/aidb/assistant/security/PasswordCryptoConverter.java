package com.aidb.assistant.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES/GCM/NoPadding converter for database connection passwords.
 * Uses a random 12-byte IV prepended to each ciphertext so that identical
 * plaintexts always produce different ciphertexts — ECB mode vulnerability eliminated.
 *
 * Format stored in DB: Base64(iv[12] + ciphertext + authTag[16])
 */
@Component
@Converter
public class PasswordCryptoConverter implements AttributeConverter<String, String> {

    private static final Logger log = LoggerFactory.getLogger(PasswordCryptoConverter.class);

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;      // 96-bit IV (NIST recommended)
    private static final int GCM_TAG_LENGTH = 128;    // 128-bit authentication tag

    private final SecretKey secretKey;

    public PasswordCryptoConverter(@Value("${aidb.encryption.key}") String rawKey) {
        // Key must be exactly 32 bytes (256-bit AES)
        byte[] keyBytes = Arrays.copyOf(rawKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), 32);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return null;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // Prepend IV to ciphertext for storage
            byte[] ivPlusCiphertext = new byte[GCM_IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, ivPlusCiphertext, 0, GCM_IV_LENGTH);
            System.arraycopy(ciphertext, 0, ivPlusCiphertext, GCM_IV_LENGTH, ciphertext.length);

            return Base64.getEncoder().encodeToString(ivPlusCiphertext);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt database password", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            byte[] ivPlusCiphertext = Base64.getDecoder().decode(dbData);

            if (ivPlusCiphertext.length < GCM_IV_LENGTH) {
                // Legacy unencrypted data — return as-is and log a warning
                log.warn("Encountered a DB password that appears to be unencrypted (length < IV). Re-encrypt on next save.");
                return dbData;
            }

            byte[] iv = Arrays.copyOfRange(ivPlusCiphertext, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(ivPlusCiphertext, GCM_IV_LENGTH, ivPlusCiphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(ciphertext), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to decrypt database password — data may be corrupted or encrypted with a different key");
            throw new RuntimeException("Failed to decrypt database password", e);
        }
    }
}
