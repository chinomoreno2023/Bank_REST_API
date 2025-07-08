package com.example.bankcards.exception.validation;

public class InvalidPageRequestException extends RuntimeException {
    public InvalidPageRequestException(String message) {
        super(message);
    }
    public InvalidPageRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
