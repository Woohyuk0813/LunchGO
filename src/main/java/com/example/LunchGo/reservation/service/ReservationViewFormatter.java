package com.example.LunchGo.reservation.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ReservationViewFormatter {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter PAID_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm");

    private ReservationViewFormatter() {
    }

    public static String formatAddress(String roadAddress, String detailAddress) {
        if (detailAddress == null || detailAddress.isBlank()) {
            return roadAddress;
        }
        return roadAddress + " " + detailAddress;
    }

    public static String formatMethod(String cardType) {
        if (cardType == null || cardType.isBlank()) {
            return "신용카드";
        }
        if ("CORPORATE".equalsIgnoreCase(cardType)) {
            return "법인카드";
        }
        if ("PERSONAL".equalsIgnoreCase(cardType)) {
            return "개인카드";
        }
        return "신용카드";
    }

    public static String formatPaidAt(LocalDateTime approvedAt) {
        if (approvedAt == null) {
            return "";
        }
        return approvedAt.format(PAID_AT_FORMATTER);
    }

    public static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }
}
