package com.example.bankcards.service.auth;

import com.example.bankcards.entity.auth.BlacklistedAccessToken;
import com.example.bankcards.entity.auth.BlacklistedSession;
import com.example.bankcards.repository.auth.BlacklistedAccessTokenRepository;
import com.example.bankcards.repository.auth.BlacklistedSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@RequiredArgsConstructor
@Service
public class BlacklistServiceImpl implements BlacklistService {
    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;
    private final BlacklistedSessionRepository blacklistedSessionRepository;

    @Override
    @Transactional
    public void blacklistToken(String accessToken, Instant expiry) {
        blacklistedAccessTokenRepository.save(
                BlacklistedAccessToken.builder()
                        .token(accessToken)
                        .expiryDate(expiry)
                        .build()
        );
    }

    @Override
    @Transactional
    public boolean isTokenBlacklisted(String token) {
        return blacklistedAccessTokenRepository.findByToken(token)
                .map(tokenObj -> tokenObj.getExpiryDate().isAfter(Instant.now()))
                .orElse(false);

    }

    @Override
    @Transactional
    public void blacklistSession(String sessionId, Instant expiry) {
        blacklistedSessionRepository.save(
                BlacklistedSession.builder()
                        .sessionId(sessionId)
                        .expiryDate(expiry)
                        .build()
        );
    }

    @Override
    @Transactional
    public boolean isSessionBlacklisted(String sessionId) {
        return blacklistedSessionRepository.existsBySessionIdAndExpiryDateAfter(
                sessionId, Instant.now()
        );
    }
}
