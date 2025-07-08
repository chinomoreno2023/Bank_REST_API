package com.example.bankcards.exception.event;

public class EventSendException extends RuntimeException {
    public EventSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventSendException(String message) {
        super(message);
    }
}