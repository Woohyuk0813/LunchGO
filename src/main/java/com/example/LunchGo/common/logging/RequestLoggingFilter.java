package com.example.LunchGo.common.logging;

import com.example.LunchGo.account.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger accessLogger = LoggerFactory.getLogger("http.access");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String traceId = resolveTraceId(request);

        MDC.put("traceId", traceId);
        MDC.put("method", request.getMethod());
        MDC.put("path", request.getRequestURI());
        String preUserId = resolveUserId();
        if (preUserId != null) {
            MDC.put("userId", preUserId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("latencyMs", String.valueOf(System.currentTimeMillis() - start));
            String userId = resolveUserId();
            if (userId != null) {
                MDC.put("userId", userId);
            }
            response.setHeader("X-Request-Id", traceId);
            if (!isPollingStatusRequest(request)) {
                accessLogger.info("request");
            }
            MDC.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String header = request.getHeader("X-Request-Id");
        if (header == null || header.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return header;
    }

    private String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return String.valueOf(userDetails.getId());
        }
        return null;
    }

    private boolean isPollingStatusRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "GET".equalsIgnoreCase(request.getMethod())
            && path != null
            && path.endsWith("/confirmation/status");
    }
}
