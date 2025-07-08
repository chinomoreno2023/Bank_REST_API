package com.example.bankcards.exception.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import static com.example.bankcards.util.exception.ExceptionResponseManager.*;

@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e) {
        logWarning(e, HttpStatus.UNAUTHORIZED, "Bad Credentials");

        return buildResponseForClient(
                HttpStatus.UNAUTHORIZED,
                e.getMessage()
        );
    }

    @ExceptionHandler(EncryptionException.class)
    public ResponseEntity<Map<String, Object>> handleCardEncryptionError(EncryptionException e) {
        logError(e, HttpStatus.INTERNAL_SERVER_ERROR, "Encryption Failed");

        return buildResponseForClient(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
        );
    }

    @ExceptionHandler(DecryptionException.class)
    public ResponseEntity<Map<String, Object>> handleCardDecryptionError(DecryptionException e) {
        logError(e, HttpStatus.INTERNAL_SERVER_ERROR, "Decryption Failed");

        return buildResponseForClient(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
        );
    }

    @ExceptionHandler(KeyStoreLoadException.class)
    public ResponseEntity<Map<String, Object>> handleKeyStoreError(KeyStoreLoadException e) {
        logError(e, HttpStatus.INTERNAL_SERVER_ERROR, "KeyStore Error");

        return buildResponseForClient(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException e) {
        logWarning(e, HttpStatus.FORBIDDEN, "Access Denied");

        return buildResponseForClient(
                HttpStatus.FORBIDDEN,
                e.getMessage()
        );
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenError(TokenNotFoundException e) {
        logWarning(e, HttpStatus.BAD_REQUEST, "Access Token Error");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenError(RefreshTokenException e) {
        logWarning(e, HttpStatus.BAD_REQUEST, "Refresh Token Error");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }


    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestCookieException(MissingRequestCookieException e) {
        logWarning(e, HttpStatus.BAD_REQUEST, "Refresh token is required");

        return buildResponseForClient(
                HttpStatus.BAD_REQUEST,
                "Refresh token is required"
        );
    }
}
