package com.example.LunchGo.reservation.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reservation {

    private Long reservationId;
    private String reservationCode;

    private Long slotId;
    private Long userId;

    private Integer partySize;

    private ReservationType reservationType;
    private ReservationStatus status;

    private String requestMessage;

    private Integer depositAmount;
    private Integer prepayAmount;
    private Integer totalAmount;

    private LocalDateTime holdExpiresAt;
    private LocalDateTime paymentDeadlineAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private VisitStatus visitStatus;
    private String reminderToken;
    private LocalDateTime reminderSentAt;
    private LocalDateTime visitRespondedAt;
}
