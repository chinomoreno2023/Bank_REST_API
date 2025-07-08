package com.example.bankcards.security.auth;

import com.example.bankcards.exception.security.TokenNotFoundException;
import com.example.bankcards.service.auth.BlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import static com.example.bankcards.util.auth.HttpUtils.*;
import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final BlacklistService tokenBlacklistService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return IGNORED_URIS.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt;
        try {
            jwt = extractJwtAccessToken(request);
        } catch (TokenNotFoundException e) {
            log.warn("Access token not found",
                    keyValue("uri", request.getRequestURI()),
                    keyValue("ip", getClientIp(request)),
                    keyValue("user_agent", request.getHeader("User-Agent"))
            );
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractUsername(jwt);
        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean valid = jwtService.isValid(jwt, userDetails);
            boolean blacklisted = tokenBlacklistService.isTokenBlacklisted(jwt);
            boolean sessionBlacklisted = tokenBlacklistService.isSessionBlacklisted(
                    jwtService.getClaims(jwt).get("sessionId", String.class)
            );

            if (valid && !blacklisted && !sessionBlacklisted) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.info("User authenticated via JWT",
                        keyValue("user", username),
                        keyValue("roles", userDetails.getAuthorities().toString()),
                        keyValue("uri", request.getRequestURI()),
                        keyValue("ip", getClientIp(request))
                );
            } else {
                log.warn("Invalid JWT access attempt",
                        keyValue("user", username),
                        keyValue("valid", valid),
                        keyValue("blacklisted", blacklisted),
                        keyValue("session_blacklisted", sessionBlacklisted),
                        keyValue("uri", request.getRequestURI()),
                        keyValue("ip", getClientIp(request))
                );
            }
        }
        filterChain.doFilter(request, response);
    }
}
