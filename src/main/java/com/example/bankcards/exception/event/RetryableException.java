package com.example.bankcards.exception.event;

public class RetryableException extends RuntimeException{
    public RetryableException(String message,Throwable cause) {
        super(cause);
    }

    public RetryableException(String message) {
        super(message);
    }
}
