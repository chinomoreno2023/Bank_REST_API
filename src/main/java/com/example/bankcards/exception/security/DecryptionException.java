package com.example.bankcards.exception.security;

public class DecryptionException extends RuntimeException {
    public DecryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DecryptionException(String message) {
        super(message);
    }
}