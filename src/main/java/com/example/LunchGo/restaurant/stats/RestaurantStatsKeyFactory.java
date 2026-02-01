package com.example.LunchGo.restaurant.stats;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class RestaurantStatsKeyFactory {
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    public LocalDate todayKst() {
        return LocalDate.now(KST);
    }

    public String viewHash(LocalDate date) {
        return "stats:view:" + format(date);
    }

    public String confirmHash(LocalDate date) {
        return "stats:confirm:" + format(date);
    }

    public String confirmProcessingHash(LocalDate date) {
        return "stats:confirm:" + format(date) + ":processing";
    }

    public String viewDedupe(LocalDate date, Long restaurantId, String userKey) {
        return "stats:view:dedupe:" + format(date) + ":" + restaurantId + ":" + userKey;
    }

    public String confirmDedupe(String paymentId) {
        return "stats:confirm:dedupe:" + paymentId;
    }

    public String flushLock(LocalDate date) {
        return "stats:flush:lock:" + format(date);
    }

    private String format(LocalDate date) {
        return DATE_FORMAT.format(date);
    }
}
