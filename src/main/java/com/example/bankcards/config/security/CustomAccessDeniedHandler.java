package com.example.bankcards.config.security;

import com.example.bankcards.util.exception.ExceptionResponseManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static com.example.bankcards.util.auth.HttpUtils.getClientIp;
import static net.logstash.logback.argument.StructuredArguments.keyValue;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied",
                keyValue("path", request.getRequestURI()),
                keyValue("message", accessDeniedException.getMessage()),
                keyValue("ip", getClientIp(request)),
                keyValue("auth_account", SecurityContextHolder.getContext().getAuthentication().getName()),
                keyValue("timestamp", LocalDateTime.now())
        );

        Map<String, Object> responseBody = ExceptionResponseManager.buildBody(
                FORBIDDEN,
                accessDeniedException.getMessage()
        );
        String responseJson = objectMapper.writeValueAsString(responseBody);

        response.setStatus(FORBIDDEN.value());
        response.setContentType(APPLICATION_JSON_VALUE);
        response.getWriter().write(responseJson);
    }
}