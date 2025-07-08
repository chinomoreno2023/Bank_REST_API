package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.auth.LoginAndPasswordAuthRequest;
import com.example.bankcards.dto.auth.SessionInfoDto;
import com.example.bankcards.dto.auth.TokenAuthResponse;
import com.example.bankcards.dto.user.UserDto;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.security.auth.JwtService;
import com.example.bankcards.service.auth.JwtAuthService;
import com.example.bankcards.service.auth.UserSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static net.logstash.logback.argument.StructuredArguments.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class JwtAuthController {
    private final JwtService jwtService;
    private final JwtAuthService jwtAuthService;
    private final UserSessionService userSessionService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid LoginAndPasswordAuthRequest request) {
        log.info("User registration",
                keyValue("username", request.getUsername())
        );
        User user = jwtAuthService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserDto.fromEntity(user));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenAuthResponse> login(
            @RequestBody @Valid LoginAndPasswordAuthRequest loginAndPasswordAuthRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        log.info("User login",
                keyValue("username", loginAndPasswordAuthRequest.getUsername())
        );
        return ResponseEntity
                .ok(jwtAuthService.login(loginAndPasswordAuthRequest, request, response));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/refresh")
    public ResponseEntity<TokenAuthResponse> refresh(@CookieValue(name = "refresh-token") String refreshToken,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        return ResponseEntity
                .ok(jwtService.refresh(refreshToken, request, response));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refresh-token") String refreshToken,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        userSessionService.logout(refreshToken, request, response);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/logout-from-all-devices")
    public ResponseEntity<Void> logoutFromAllDevices(HttpServletResponse response) {
        userSessionService.logoutFromAllDevices(response);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/logout-current")
    public ResponseEntity<Void> logoutCurrentSession(HttpServletRequest request, HttpServletResponse response) {
        userSessionService.logoutCurrentSession(request, response);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/logout-by-session/{sessionId}")
    public ResponseEntity<Void> logoutBySessionId(@PathVariable String sessionId,
                                                    HttpServletRequest request,
                                                    HttpServletResponse response) {
        userSessionService.logoutBySessionId(sessionId, request, response);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionInfoDto>> getAllActiveSessions() {
        return ResponseEntity
                .ok(userSessionService.getActiveSessions());
    }
}
