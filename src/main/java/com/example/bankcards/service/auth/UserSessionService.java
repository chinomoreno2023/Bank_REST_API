package com.example.bankcards.service.auth;

import com.example.bankcards.dto.auth.SessionInfoDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public interface UserSessionService {
    void logout(String refreshToken, HttpServletRequest request, HttpServletResponse response);
    void logoutCurrentSession(HttpServletRequest request, HttpServletResponse response);
    void logoutFromAllDevices(HttpServletResponse response);
    List<SessionInfoDto> getActiveSessions();
    void logoutBySessionId(String sessionId, HttpServletRequest request, HttpServletResponse response);
}
