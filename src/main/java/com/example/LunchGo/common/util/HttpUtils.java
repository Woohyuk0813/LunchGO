package com.example.LunchGo.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class HttpUtils {
    // 쿠키 입력
    public static void setCookie(HttpServletResponse res, String name, String value, int expSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        // 로컬 개발(localhost)에서는 주석 처리하거나 false로 해도 되지만, 배포 시엔 true여야 함
//        cookie.setSecure(true);

        if (expSeconds > 0) {
            cookie.setMaxAge(expSeconds);
        }

        res.addCookie(cookie);
    }

    // 쿠키 값 조회
    public static String getCookieValue(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    // 쿠키 삭제
    public static void removeCookie(HttpServletResponse res, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        res.addCookie(cookie);
    }

    // 토큰 조회
    public static String getBearerToken(HttpServletRequest req) {
        String authorization = req.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.replace("Bearer ", "").trim(); // Bearer 값 조회
        }

        return null;
    }
}
