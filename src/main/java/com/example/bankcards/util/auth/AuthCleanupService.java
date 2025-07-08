package com.example.bankcards.util.auth;

import com.example.bankcards.entity.auth.BlacklistedAccessToken;
import com.example.bankcards.entity.auth.BlacklistedSession;
import com.example.bankcards.entity.auth.RefreshToken;
import com.example.bankcards.repository.auth.BlacklistedAccessTokenRepository;
import com.example.bankcards.repository.auth.BlacklistedSessionRepository;
import com.example.bankcards.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthCleanupService {
    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedSessionRepository blacklistedSessionRepository;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional
    public void removeExpiredAccessTokens() {
        List<BlacklistedAccessToken> tokens
                = blacklistedAccessTokenRepository.findExpiredTokensForUpdate(Instant.now());

        if (!tokens.isEmpty()) {
            blacklistedAccessTokenRepository.deleteAllInBatch(tokens);
        }
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional
    public void removeExpiredRefreshTokens() {
        Instant threshold = Instant.now().minus(Duration.ofMinutes(60));
        List<RefreshToken> expiredTokens = refreshTokenRepository.findAllExpiredForUpdate(threshold);

        if (!expiredTokens.isEmpty()) {
            refreshTokenRepository.deleteAllInBatch(expiredTokens);
        }
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional
    public void removeExpiredBlacklistedSessions() {
        List<BlacklistedSession> expiredSessions
                = blacklistedSessionRepository.findExpiredSessionsForUpdate(Instant.now());

        if (!expiredSessions.isEmpty()) {
            blacklistedSessionRepository.deleteAllInBatch(expiredSessions);
        }
    }
}
