package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.dto.BusinessVisitNotificationResponse;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.BusinessVisitNotificationRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessVisitNotificationService {

    private final ReservationMapper reservationMapper;

    public List<BusinessVisitNotificationResponse> getList(Long restaurantId) {
        List<BusinessVisitNotificationRow> rows = reservationMapper.selectBusinessVisitNotifications(restaurantId);

        return rows.stream().map(r -> {
            String raw = r.getResponseStatus();
            String responseStatus;
            String responseNote;

            if (raw == null) {
                responseStatus = "pending";
                responseNote = "응답 대기";
            } else {
                switch (raw) {
                    case "PENDING" -> {
                        responseStatus = "pending";
                        responseNote = "응답 대기";
                    }
                    case "CONFIRMED" -> {
                        responseStatus = "will_visit";
                        responseNote = "방문 예정";
                    }
                    case "CANCELLED" -> {
                        responseStatus = "cancel_visit";
                        responseNote = "방문 취소";
                    }
                    default -> {
                        responseStatus = "pending";
                        responseNote = "응답 대기";
                    }
                }
            }

            return new BusinessVisitNotificationResponse(
                    r.getId(),
                    r.getReservationId(),
                    r.getCustomerName(),
                    r.getPhone(),
                    r.getReservationDatetime(),
                    r.getMessageSentAt(),
                    responseStatus,
                    r.getResponseAt(),
                    responseNote,
                    false
            );
        }).toList();
    }
}
