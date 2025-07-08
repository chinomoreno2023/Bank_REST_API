package com.example.bankcards.exception.event;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import static com.example.bankcards.util.exception.ExceptionResponseManager.buildResponseForClient;
import static com.example.bankcards.util.exception.ExceptionResponseManager.logError;

@RestControllerAdvice
public class EventExceptionHandler {

    @ExceptionHandler(EventSendException.class)
    public ResponseEntity<Map<String, Object>> handleKafkaSendException(EventSendException e) {
        logError(e, HttpStatus.INTERNAL_SERVER_ERROR, "Kafka Send Error");

        return buildResponseForClient(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Kafka Send Error"
        );
    }
}
