package com.example.bankcards.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static com.example.bankcards.util.auth.HttpUtils.getClientIp;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {

        log.warn("Unauthorized access attempt",
                keyValue("timestamp", LocalDateTime.now()),
                keyValue("path", request.getRequestURI()),
                keyValue("error_type", authException.getClass().getSimpleName()),
                keyValue("message", authException.getMessage()),
                keyValue("ip", getClientIp(request)),
                keyValue("user_agent", request.getHeader("User-Agent"))
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorBody = Map.of(
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", "Invalid access token format"
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
