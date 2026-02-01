package com.example.LunchGo.restaurant.stats;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class RestaurantStatsBatchScheduler {
    private final RestaurantStatsBatchService batchService;
    private final RestaurantStatsRedisRepository redisRepository;
    private final RestaurantStatsKeyFactory keyFactory;

    @Value("${stats.flush.lock-ttl-seconds:180}")
    private long lockTtlSeconds;

    @Scheduled(fixedDelayString = "${stats.flush.interval-ms:180000}")
    public void flushStats() {
        LocalDate today = keyFactory.todayKst();
        String lockKey = keyFactory.flushLock(today);
        String lockValue = UUID.randomUUID().toString();

        boolean locked = redisRepository.tryLock(lockKey, lockValue, Duration.ofSeconds(lockTtlSeconds));
        if (!locked) {
            return;
        }

        try {
            batchService.flushViews(today);
            batchService.flushConfirms(today);
        } catch (Exception ex) {
            log.error("Failed to flush restaurant stats", ex);
        } finally {
            redisRepository.releaseLock(lockKey, lockValue);
        }
    }
}
