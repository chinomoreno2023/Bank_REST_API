package com.example.bankcards.repository.auth;

import com.example.bankcards.entity.auth.RefreshToken;

public interface RefreshTokenHashProjection {
    String getTokenHash();
    String getTokenSalt();
    RefreshToken getRefreshToken();
}
