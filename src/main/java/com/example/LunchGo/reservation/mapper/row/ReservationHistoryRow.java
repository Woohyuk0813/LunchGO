package com.example.LunchGo.reservation.mapper.row;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationHistoryRow {

    private Long reservationId;
    private String reservationCode;

    private Long restaurantId;
    private String restaurantName;
    private String restaurantAddress;

    private LocalDate slotDate;
    private LocalTime slotTime;
    private Integer partySize;
    private String reservationStatus;

    private Integer visitCount;
    private Integer daysSinceLastVisit;

    private Integer receiptAmount;
    private Integer paidAmount;
    private Integer totalAmount;

    private String cancelledBy;
    private String cancelledReason;
    private LocalDateTime cancelledAt;

    private Long reviewId;
    private Integer reviewRating;
    private String reviewContent;
    private LocalDate reviewCreatedAt;
    private String reviewTags;
}
