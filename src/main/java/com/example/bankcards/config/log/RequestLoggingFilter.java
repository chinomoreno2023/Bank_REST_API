package com.example.bankcards.config.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;
import static com.example.bankcards.util.auth.HttpUtils.IGNORED_URIS;
import static com.example.bankcards.util.auth.HttpUtils.getClientIp;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return IGNORED_URIS.stream()
                .anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        try {
            String requestId = UUID.randomUUID().toString();
            String ip = getClientIp(request);
            MDC.put("request_id", requestId);
            MDC.put("method", request.getMethod());
            MDC.put("uri", request.getRequestURI());
            MDC.put("ip", ip);
            MDC.put("user_agent", request.getHeader("User-Agent"));

            log.info("Incoming request",
                    keyValue("request_id", requestId),
                    keyValue("method", request.getMethod()),
                    keyValue("uri", request.getRequestURI()),
                    keyValue("ip", ip),
                    keyValue("user_agent", request.getHeader("User-Agent"))
            );
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
