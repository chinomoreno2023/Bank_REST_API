package com.example.bankcards.service.auth;

import com.example.bankcards.dto.auth.LoginAndPasswordAuthRequest;
import com.example.bankcards.dto.auth.TokenAuthResponse;
import com.example.bankcards.entity.auth.RefreshToken;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.validation.UsernameAlreadyExistsException;
import com.example.bankcards.repository.user.UserRepository;
import com.example.bankcards.security.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class JwtAuthServiceImpl implements JwtAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private static final String REFRESH_TOKEN_HEADER = "refresh-token";

    @Override
    public TokenAuthResponse login(LoginAndPasswordAuthRequest loginAndPasswordAuthRequest,
                                   HttpServletRequest httpServletRequest,
                                   HttpServletResponse response) {
        User user = userRepository.findByUsername(loginAndPasswordAuthRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(loginAndPasswordAuthRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String rawRefreshToken = jwtService.createAndSaveRefreshToken(user, httpServletRequest);
        RefreshToken refreshToken = jwtService.validateRefreshToken(rawRefreshToken, user);
        String accessToken = jwtService.createAccessToken(user, refreshToken.getSessionId());

        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_HEADER, rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new TokenAuthResponse(accessToken);
    }

    @Override
    @Transactional
    public User register(LoginAndPasswordAuthRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
    }
}
