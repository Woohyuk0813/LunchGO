package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.reservation.domain.VisitStatus;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderResponseController {

    private final ReservationMapper reservationMapper;

    @Value("${app.reminder.redirect-url:}")
    private String redirectUrl;

    @GetMapping("/visit")
    public void visit(@RequestParam("token") String token, HttpServletResponse resp) throws IOException {
        int updated = reservationMapper.updateVisitStatusByToken(token, VisitStatus.CONFIRMED.name());
        redirect(resp, updated == 1 ? "will_visit" : "invalid");
    }

    @GetMapping("/cancel")
    public void cancel(@RequestParam("token") String token, HttpServletResponse resp) throws IOException {
        int updated = reservationMapper.updateVisitStatusByToken(token, VisitStatus.CANCELLED.name());
        redirect(resp, updated == 1 ? "cancel_visit" : "invalid");
    }

    private void redirect(HttpServletResponse resp, String result) throws IOException {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            resp.setContentType("text/plain; charset=utf-8");
            resp.getWriter().write(result);
            return;
        }
        String sep = redirectUrl.contains("?") ? "&" : "?";
        resp.sendRedirect(redirectUrl + sep + "result=" + URLEncoder.encode(result, StandardCharsets.UTF_8));
    }
}
