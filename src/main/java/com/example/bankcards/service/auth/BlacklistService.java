package com.example.bankcards.service.auth;

import java.time.Instant;

public interface BlacklistService {
    void blacklistToken(String accessToken, Instant expiry);
    boolean isTokenBlacklisted(String token);
    void blacklistSession(String sessionId, Instant expiry);
    boolean isSessionBlacklisted(String sessionId);
}
