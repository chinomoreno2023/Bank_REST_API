package com.example.bankcards.dto.auth;

import com.example.bankcards.entity.auth.RefreshToken;
import java.time.Instant;

public record SessionInfoDto(
        String sessionId,
        String userAgent,
        String ipAddress,
        Instant createdAt
) {
    public static SessionInfoDto fromEntity(RefreshToken token) {
        return new SessionInfoDto(
                token.getSessionId(),
                token.getUserAgent(),
                token.getIpAddress(),
                token.getCreatedAt()
        );
    }
}

