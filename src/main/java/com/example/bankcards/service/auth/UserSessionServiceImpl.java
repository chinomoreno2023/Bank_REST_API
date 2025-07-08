package com.example.bankcards.service.auth;

import com.example.bankcards.dto.auth.SessionInfoDto;
import com.example.bankcards.entity.auth.RefreshToken;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.repository.auth.RefreshTokenRepository;
import com.example.bankcards.security.auth.JwtService;
import com.example.bankcards.util.auth.ContextExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import static com.example.bankcards.util.auth.HttpUtils.clearCookie;
import static com.example.bankcards.util.auth.HttpUtils.extractJwtAccessToken;

@Component
@RequiredArgsConstructor
public class UserSessionServiceImpl implements UserSessionService {
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistService tokenBlacklistService;
    private final ContextExtractor contextExtractor;
    private final TransactionTemplate transactionTemplate;
    private static final String REFRESH_TOKEN_HEADER = "refresh-token";
    private static final String SESSION_ID_HEADER = "X-Refresh-Session";

    @Transactional
    @Override
    public void logout(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        User user = contextExtractor.getUserFromContext();
        RefreshToken token = jwtService.validateRefreshToken(refreshToken, user);
        String accessToken = extractJwtAccessToken(request);

        tokenBlacklistService.blacklistToken(accessToken, jwtService.getAccessTokenExpiry(accessToken));
        refreshTokenRepository.delete(token);
        clearCookie(REFRESH_TOKEN_HEADER, response);
    }

    @Override
    @Transactional
    public void logoutFromAllDevices(HttpServletResponse response) {
        User user = contextExtractor.getUserFromContext();
        List<RefreshToken> tokens = refreshTokenRepository.findAllForUpdateByUser(user);

        if (!tokens.isEmpty()) {
            for (RefreshToken token : tokens) {
                tokenBlacklistService.blacklistSession(token.getSessionId(), token.getExpiryDate());
            }
            refreshTokenRepository.deleteAll(tokens);
        }
        clearCookie(REFRESH_TOKEN_HEADER, response);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionInfoDto> getActiveSessions() {
        User user = contextExtractor.getUserFromContext();

        return refreshTokenRepository.findByUser(user).stream()
                .filter(token ->
                        !tokenBlacklistService.isSessionBlacklisted(token.getSessionId()))
                .map(SessionInfoDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void logoutBySessionId(String sessionId, HttpServletRequest request, HttpServletResponse response) {
        User user = contextExtractor.getUserFromContext();

        RefreshToken token = refreshTokenRepository
                .findByUserAndSessionId(user, sessionId)
                .orElseThrow(() -> new IllegalStateException("Session not found"));

        String accessToken = extractJwtAccessToken(request);

        tokenBlacklistService.blacklistToken(accessToken, jwtService.getAccessTokenExpiry(accessToken));
        tokenBlacklistService.blacklistSession(sessionId, token.getExpiryDate());
        refreshTokenRepository.delete(token);
        clearCookie(REFRESH_TOKEN_HEADER, response);
    }

    @Transactional
    @Override
    public void logoutCurrentSession(HttpServletRequest request, HttpServletResponse response) {
        String sessionId = request.getHeader(SESSION_ID_HEADER);

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Missing session ID header");
        }
        transactionTemplate.execute(status -> {
            logoutBySessionId(sessionId, request, response);
            return null;
        });
    }
}
