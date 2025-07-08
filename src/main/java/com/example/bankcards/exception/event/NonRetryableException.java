package com.example.bankcards.exception.event;

public class NonRetryableException extends RuntimeException{
    public NonRetryableException(String message, Throwable cause) {
        super(cause);
    }

    public NonRetryableException(String message) {
        super(message);
    }
}