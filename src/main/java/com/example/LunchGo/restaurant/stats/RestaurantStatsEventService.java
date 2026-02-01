package com.example.LunchGo.restaurant.stats;

import java.time.Duration;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantStatsEventService {
    private final RestaurantStatsRedisRepository redisRepository;
    private final RestaurantStatsKeyFactory keyFactory;

    @Value("${stats.view.dedupe-ttl-minutes:15}")
    private long viewDedupeTtlMinutes;

    @Value("${stats.confirm.dedupe-ttl-days:14}")
    private long confirmDedupeTtlDays;

    public void recordView(Long restaurantId, String userKey) {
        if (restaurantId == null || userKey == null || userKey.isBlank()) {
            return;
        }
        LocalDate today = keyFactory.todayKst();
        String dedupeKey = keyFactory.viewDedupe(today, restaurantId, userKey);
        boolean firstView = redisRepository.setIfAbsent(
                dedupeKey,
                "1",
                Duration.ofMinutes(viewDedupeTtlMinutes)
        );
        if (!firstView) {
            return;
        }
        String viewHashKey = keyFactory.viewHash(today);
        redisRepository.hincrBy(viewHashKey, restaurantId.toString(), 1L);
    }

    public void recordConfirm(Long restaurantId, String paymentId) {
        if (restaurantId == null || paymentId == null || paymentId.isBlank()) {
            return;
        }
        LocalDate today = keyFactory.todayKst();
        String dedupeKey = keyFactory.confirmDedupe(paymentId);
        boolean firstConfirm = redisRepository.setIfAbsent(
                dedupeKey,
                "1",
                Duration.ofDays(confirmDedupeTtlDays)
        );
        if (!firstConfirm) {
            return;
        }
        String confirmHashKey = keyFactory.confirmHash(today);
        redisRepository.hincrBy(confirmHashKey, restaurantId.toString(), 1L);
    }
}
