package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.ReminderSendRow;
import com.example.LunchGo.sms.event.SystemSmsSendEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationReminderService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final SecureRandom RND = new SecureRandom();

    private final ReservationMapper reservationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Transactional
    public int sendDueReminders() {
        List<ReminderSendRow> targets = reservationMapper.selectReminderTargets();
        int sent = 0;

        for (ReminderSendRow t : targets) {
            String token = newToken();

            int marked = reservationMapper.tryMarkReminderSent(t.getReservationId(), token);
            if (marked != 1) continue;

            String visitUrl  = publicBaseUrl + "/reminder-visit.html?token=" + enc(token);
            String cancelUrl = publicBaseUrl + "/reminder-cancel.html?token=" + enc(token);

            LocalDateTime slot = LocalDateTime.of(t.getSlotDate(), t.getSlotTime());
            String when = slot.format(DT);

            String content =
                    "[런치고] 예약 1시간 전 방문 확인\n" +
                            t.getRestaurantName() + " (" + when + ")\n" +
                            "방문 확인: " + visitUrl + "\n" +
                            "미응답 시 메뉴 준비가 늦어질 수 있습니다. 위 링크를 통해 응답해주세요.";

            eventPublisher.publishEvent(new SystemSmsSendEvent(t.getUserPhone(), content));
            sent++;
        }

        return sent;
    }

    private static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String newToken() {
        byte[] bytes = new byte[24];
        RND.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
