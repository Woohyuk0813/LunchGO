package com.example.LunchGo.common.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 1. 상태 코드 설정 (401)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 2. 응답 타입 설정
        response.setContentType("application/json;charset=UTF-8");

        // 3. JSON 데이터 만들기
        String jsonResponse = "{\"message\": \"인증에 실패했습니다.\", \"code\": \"UNAUTHORIZED\"}";

        response.getWriter().write(jsonResponse);
    }
}
