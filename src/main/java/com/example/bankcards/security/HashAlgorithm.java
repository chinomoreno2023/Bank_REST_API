package com.example.bankcards.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HashAlgorithm {
    SHA_256("SHA-256");

    private final String algorithm;
}
