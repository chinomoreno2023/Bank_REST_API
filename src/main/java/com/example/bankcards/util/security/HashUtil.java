package com.example.bankcards.util.security;

import com.example.bankcards.security.HashAlgorithm;
import com.example.bankcards.security.SaltByteLength;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class HashUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    private HashUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String generateSalt(SaltByteLength length) {
        int lengthBytes = length.getLength();
        if (lengthBytes < 8 || lengthBytes > 64 || lengthBytes % 2 != 0) {
            throw new IllegalArgumentException("Salt length must be even, between 8 and 64 bytes");
        }
        byte[] salt = new byte[lengthBytes];
        secureRandom.nextBytes(salt);

        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hashWithSecret(String data, String secret, HashAlgorithm algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getAlgorithm());
            String combined = secret + data;
            byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash data", e);
        }
    }
}
