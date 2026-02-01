package com.example.LunchGo.restaurant.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantCacheScheduler {

    private final PublicRestaurantService publicRestaurantService;
    private final Executor taskExecutor;

    /**
     * 캐시 만료 시간(1시간)보다 짧은 주기로 캐시를 갱신합니다. (55분)
     * 이를 통해 사용자는 항상 Cache Hit 상태의 빠른 응답을 받을 수 있습니다. (Cache Warming)
     */
    @Scheduled(fixedRate = 1000 * 60 * 55)
    public void refreshCachePeriodically() {
        log.debug("Starting periodic cache refresh for restaurant summaries...");
        publicRestaurantService.refreshRestaurantSummaries();
        log.debug("Cache refresh completed.");
    }

    /**
     * 애플리케이션 시작 직후에 캐시를 즉시 로드합니다.
     * 첫 번째 사용자가 느린 응답을 겪는 것을 방지합니다.
     * 
     * [개선] new Thread() 대신 Spring의 TaskExecutor를 사용하여 스레드를 효율적으로 관리합니다.
     */
    @PostConstruct
    public void initCache() {
        log.debug("Initializing restaurant summaries cache on startup...");
        // Spring 관리 스레드 풀에 작업을 위임하여 실행
        taskExecutor.execute(() -> {
            try {
                publicRestaurantService.refreshRestaurantSummaries();
                log.debug("Cache initialization completed.");
            } catch (Exception e) {
                log.error("Failed to initialize cache", e);
            }
        });
    }
}
