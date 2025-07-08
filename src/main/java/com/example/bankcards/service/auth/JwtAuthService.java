package com.example.bankcards.service.auth;

import com.example.bankcards.dto.auth.LoginAndPasswordAuthRequest;
import com.example.bankcards.dto.auth.TokenAuthResponse;
import com.example.bankcards.entity.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface JwtAuthService {
    TokenAuthResponse login(LoginAndPasswordAuthRequest authRequest,
                            HttpServletRequest request,
                            HttpServletResponse response);
    User register(LoginAndPasswordAuthRequest authRequest);
}
