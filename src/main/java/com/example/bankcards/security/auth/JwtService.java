package com.example.bankcards.security.auth;

import com.example.bankcards.config.security.EncryptionProperties;
import com.example.bankcards.dto.auth.TokenAuthResponse;
import com.example.bankcards.entity.auth.RefreshToken;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.security.RefreshTokenException;
import com.example.bankcards.repository.auth.RefreshTokenHashProjection;
import com.example.bankcards.repository.auth.RefreshTokenRepository;
import com.example.bankcards.security.KeyStoreKeyProvider;
import com.example.bankcards.service.auth.BlacklistService;
import com.example.bankcards.util.auth.ContextExtractor;
import com.example.bankcards.util.auth.HttpUtils;
import com.example.bankcards.util.security.SymmetricEncryptor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import static com.example.bankcards.security.HashAlgorithm.*;
import static com.example.bankcards.util.auth.HttpUtils.getClientIp;
import static com.example.bankcards.security.SaltByteLength.SALT_16;
import static com.example.bankcards.util.security.HashUtil.generateSalt;
import static com.example.bankcards.util.security.HashUtil.hashWithSecret;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final KeyStoreKeyProvider keyStoreKeyProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistService tokenBlacklistService;
    private final EncryptionProperties encryptionProperties;
    private final SymmetricEncryptor symmetricEncryptor;
    private final ContextExtractor contextExtractor;

    public String createAccessToken(User user, String sessionId) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole().name())
                .claim("sessionId", sessionId)
                .setExpiration(Date.from(Instant.now().plus(
                        encryptionProperties.getAccessTokenExpiration(), ChronoUnit.MINUTES))
                )
                .signWith(keyStoreKeyProvider.loadKey(
                        encryptionProperties.getJwtKeyAlias()), SignatureAlgorithm.HS256)
                .compact();
    }

    @Transactional
    public String createAndSaveRefreshToken(User user, HttpServletRequest request) {
        String rawToken = UUID.randomUUID().toString();
        String salt = generateSalt(SALT_16);
        String tokenHash = hashWithSecret(rawToken, salt, SHA_256);
        String encryptedToken = symmetricEncryptor.encrypt(
                rawToken, encryptionProperties.getRefreshKeyAlias()
        );

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(encryptedToken)
                .expiryDate(Instant.now().plus(
                        encryptionProperties.getRefreshTokenExpiration(), ChronoUnit.DAYS)
                )
                .tokenHash(tokenHash)
                .tokenSalt(salt)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .createdAt(Instant.now())
                .sessionId(UUID.randomUUID().toString())
                .build();

        refreshTokenRepository.save(token);
        return rawToken;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String rawToken, User user) {
        List<RefreshTokenHashProjection> tokens
                = refreshTokenRepository.findHashesWithTokenByUser(user.getId());

        return tokens.stream()
                .filter(p -> {
                    String expectedHash = hashWithSecret(rawToken, p.getTokenSalt(), SHA_256);
                    return expectedHash.equals(p.getTokenHash());
                })
                .map(RefreshTokenHashProjection::getRefreshToken)
                .filter(token -> token.getExpiryDate().isAfter(Instant.now()))
                .findFirst()
                .orElseThrow(() -> new RefreshTokenException("Invalid or expired refresh token"));
    }

    @Transactional
    public TokenAuthResponse refresh(String refreshToken,
                                     HttpServletRequest request,
                                     HttpServletResponse response) {
        User user = contextExtractor.getUserFromContext();
        RefreshToken token = validateRefreshToken(refreshToken, user);
        String accessToken = HttpUtils.extractJwtAccessToken(request);

        tokenBlacklistService.blacklistToken(accessToken, getAccessTokenExpiry(accessToken));
        refreshTokenRepository.delete(token);

        String newRefresh = createAndSaveRefreshToken(user, request);
        RefreshToken newRefreshToken = validateRefreshToken(newRefresh, user);
        String newAccess = createAccessToken(user, newRefreshToken.getSessionId());

        ResponseCookie refreshCookie = ResponseCookie.from("refresh-token", newRefresh)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return new TokenAuthResponse(newAccess);
    }

    public Instant getExpiration(String accessToken) {
        return getClaims(accessToken).getExpiration().toInstant();
    }

    public Instant getAccessTokenExpiry(String accessToken) {
        return getExpiration(accessToken);
    }

    public String extractUsername(String accessToken) {
        return getClaims(accessToken).getSubject();
    }

    public boolean isValid(String accessToken, UserDetails userDetails) {
        return extractUsername(accessToken).equals(userDetails.getUsername()) && !isExpired(accessToken);
    }

    private boolean isExpired(String accessToken) {
        return getClaims(accessToken)
                .getExpiration()
                .before(new Date());
    }

    public Claims getClaims(String accessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(keyStoreKeyProvider.loadKey(encryptionProperties.getJwtKeyAlias()))
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
    }
}
