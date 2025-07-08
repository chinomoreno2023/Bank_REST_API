package com.example.bankcards.exception.validation;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import static com.example.bankcards.util.exception.ExceptionResponseManager.*;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(found -> found.getField() + ": " + found.getDefaultMessage())
                .findFirst()
                .orElse("Validation Error");

        logWarning(e, HttpStatus.BAD_REQUEST, message);

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(found -> found.getPropertyPath() + ": " + found.getMessage())
                .findFirst()
                .orElse("Constraint Violation");

        logWarning(e, HttpStatus.BAD_REQUEST, message);

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        logWarning(e, HttpStatus.BAD_REQUEST, "Invalid request format. Please check your data and try again");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                "Invalid request format. Please check your data and try again"
        );
    }

    @ExceptionHandler(InvalidPageRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPageRequestException(InvalidPageRequestException e) {
        logWarning(e, HttpStatus.BAD_REQUEST, "Invalid page request");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException e) {
        logWarning(e, HttpStatus.CONFLICT, "Username already exists");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }

    @ExceptionHandler(CardAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleCardAlreadyExistsException(CardAlreadyExistsException e) {
        logWarning(e, HttpStatus.CONFLICT, "Card already exists");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }
}
