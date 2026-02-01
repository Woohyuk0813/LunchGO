package com.example.LunchGo.common.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 1. 상태 코드 설정 (403)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        // 2. 응답 타입 설정 (JSON)
        response.setContentType("application/json;charset=UTF-8");

        // 3. JSON 데이터 만들기
        String jsonResponse = "{\"message\": \"인증에 실패했습니다.\", \"code\": \"FORBIDDEN\"}";

        response.getWriter().write(jsonResponse);
    }
}
