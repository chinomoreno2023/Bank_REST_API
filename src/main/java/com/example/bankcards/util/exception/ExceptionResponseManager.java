package com.example.bankcards.util.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.Map;
import static net.logstash.logback.argument.StructuredArguments.keyValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
public class ExceptionResponseManager {
    private ExceptionResponseManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void logError(Throwable e, HttpStatus status, String message) {
        log.error(message,
                keyValue("timestamp", LocalDateTime.now()),
                keyValue("status", status.value()),
                keyValue("error_type", e.getClass().getSimpleName()),
                keyValue("message", e.getMessage()),
                e
        );
    }

    public static void logWarning(Throwable e, HttpStatus status, String message) {
        log.warn(message,
                keyValue("timestamp", LocalDateTime.now()),
                keyValue("status", status.value()),
                keyValue("error_type", e.getClass().getSimpleName()),
                keyValue("message", e.getMessage()),
                e
        );
    }

    public static ResponseEntity<Map<String, Object>> buildResponseForClient(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .contentType(APPLICATION_JSON)
                .body(buildBody(status, message));
    }

    public static Map<String, Object> buildBody(HttpStatus status, String message) {
        return Map.of(
                "status", status.value(),
                "error", message
        );
    }
}
