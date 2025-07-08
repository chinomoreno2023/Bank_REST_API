package com.example.bankcards.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SaltByteLength {
    SALT_16(16),
    SALT_32(32),
    SALT_64(64);

    private final int length;
}
