package com.example.bankcards.util.auth;

import com.example.bankcards.exception.security.TokenNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.util.List;
import java.util.Optional;

public class HttpUtils {
    public static final List<String> IGNORED_URIS = List.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/favicon.ico",
            "/openapi.yaml"
    );

    private HttpUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String extractJwtAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Authorization"))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .orElseThrow(() -> new TokenNotFoundException("Access token not found or malformed"));
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = request.getHeader("Proxy-Client-IP");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        }
        return ip;
    }

    public static void clearCookie(String name, HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }
}
