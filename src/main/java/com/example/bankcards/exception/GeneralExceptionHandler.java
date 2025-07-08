package com.example.bankcards.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import static com.example.bankcards.util.exception.ExceptionResponseManager.*;
import static com.example.bankcards.util.exception.ExceptionResponseManager.buildResponseForClient;

@RestControllerAdvice
@Slf4j
public class GeneralExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UsernameNotFoundException e) {
        logWarning(e, HttpStatus.NOT_FOUND, "User Not Found");

        return buildResponseForClient(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException e) {
        logError(e, HttpStatus.BAD_REQUEST, "Invalid State");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        logError(e, HttpStatus.BAD_REQUEST, "Invalid Argument");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
        logWarning(e, HttpStatus.NOT_FOUND, "Not Found");

        return buildResponseForClient(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        logError(e, HttpStatus.CONFLICT, "Data Integrity Violation");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                "Data Integrity Violation"
        );
    }
}
