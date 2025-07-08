package com.example.bankcards.exception.security;

public class KeyStoreLoadException extends RuntimeException {
  public KeyStoreLoadException(String message, Throwable cause) {
    super(message, cause);
  }

  public KeyStoreLoadException(String message) {
    super(message);
  }
}