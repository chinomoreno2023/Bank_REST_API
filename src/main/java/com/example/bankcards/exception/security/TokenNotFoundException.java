package com.example.bankcards.exception.security;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(String message) {
        super(message);
    }
     public TokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
