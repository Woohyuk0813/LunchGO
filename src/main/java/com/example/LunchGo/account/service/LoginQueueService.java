package com.example.LunchGo.account.service;

import com.example.LunchGo.account.dto.LoginQueueStatusResponse;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoginQueueService {
    private static final String QUEUE_KEY = "login:queue";
    private static final String TOKEN_PREFIX = "login:queue:token:";
    private static final String SEQ_KEY = "login:queue:seq";

    private final StringRedisTemplate redisTemplate;

    @Value("${login.queue.enabled:true}")
    private boolean enabled;

    @Value("${login.queue.capacity:20}")
    private int capacity;

    @Value("${login.queue.token-ttl-ms:120000}")
    private long tokenTtlMillis;

    public boolean isEnabled() {
        return enabled;
    }

    public LoginQueueStatusResponse joinQueue() {
        if (!enabled) {
            return buildImmediateAllowed();
        }

        String token = UUID.randomUUID().toString();
        Long seq = redisTemplate.opsForValue().increment(SEQ_KEY);
        double score = seq == null ? System.currentTimeMillis() : seq;

        redisTemplate.opsForZSet().add(QUEUE_KEY, token, score);
        redisTemplate.opsForValue().set(tokenKey(token), "queued", Duration.ofMillis(tokenTtlMillis));

        return getStatus(token);
    }

    public LoginQueueStatusResponse getStatus(String token) {
        if (!enabled) {
            return buildImmediateAllowed();
        }

        if (!StringUtils.hasText(token)) {
            return LoginQueueStatusResponse.builder()
                    .queueToken(null)
                    .allowed(false)
                    .expired(true)
                    .position(-1)
                    .waitingCount(0)
                    .capacity(normalizedCapacity())
                    .message("로그인 대기열 토큰이 필요합니다.")
                    .build();
        }

        String tokenKey = tokenKey(token);
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey))) {
            redisTemplate.opsForZSet().remove(QUEUE_KEY, token);
            return buildExpired(token);
        }

        redisTemplate.expire(tokenKey, Duration.ofMillis(tokenTtlMillis));
        return buildStatus(token, false, null);
    }

    public void release(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        redisTemplate.opsForZSet().remove(QUEUE_KEY, token);
        redisTemplate.delete(tokenKey(token));
    }

    private LoginQueueStatusResponse buildExpired(String token) {
        return buildStatus(token, true, "로그인 대기열이 만료되었습니다.");
    }

    private LoginQueueStatusResponse buildImmediateAllowed() {
        return LoginQueueStatusResponse.builder()
                .queueToken(null)
                .allowed(true)
                .expired(false)
                .position(0)
                .waitingCount(0)
                .capacity(normalizedCapacity())
                .message(null)
                .build();
    }

    private LoginQueueStatusResponse buildStatus(String token, boolean expired, String message) {
        long capacityValue = normalizedCapacity();
        Long rank = redisTemplate.opsForZSet().rank(QUEUE_KEY, token);
        Long total = redisTemplate.opsForZSet().zCard(QUEUE_KEY);

        long position = rank == null ? -1 : rank + 1;
        boolean allowed = !expired && rank != null && rank < capacityValue;
        long waitingCount;
        if (position > 0) {
            waitingCount = Math.max(0, position - capacityValue);
        } else {
            long totalCount = total == null ? 0L : total;
            waitingCount = Math.max(0, totalCount - capacityValue);
        }

        return LoginQueueStatusResponse.builder()
                .queueToken(token)
                .allowed(allowed)
                .expired(expired)
                .position(position)
                .waitingCount(waitingCount)
                .capacity(capacityValue)
                .message(message)
                .build();
    }

    private long normalizedCapacity() {
        return Math.max(1L, capacity);
    }

    private String tokenKey(String token) {
        return TOKEN_PREFIX + token;
    }
}
