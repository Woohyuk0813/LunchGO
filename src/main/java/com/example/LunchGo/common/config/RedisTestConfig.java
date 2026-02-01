package com.example.LunchGo.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@Configuration
@RequiredArgsConstructor
public class RedisTestConfig {

    private final StringRedisTemplate redisTemplate;

    @Bean
    public CommandLineRunner testRedisConnection() {
        return args -> {
            try {
                System.out.println("============== Redis Connection Test Start ==============");
                ValueOperations<String, String> ops = redisTemplate.opsForValue();

                // 1. 데이터 저장
                ops.set("testKey", "Hello Redis!");

                // 2. 데이터 조회
                String value = ops.get("testKey");

                System.out.println("Redis Test Value: " + value); // "Hello Redis!"가 출력되어야 함

                if ("Hello Redis!".equals(value)) {
                    System.out.println("✅ Redis 연결 성공!");
                } else {
                    System.out.println("❌ Redis 데이터 불일치");
                }

                // 3. 데이터 삭제 (청소)
                redisTemplate.delete("testKey");
                System.out.println("============== Redis Connection Test End ==============");
            } catch (Exception e) {
                System.out.println("🔥 Redis 연결 실패! SSH 터널링을 확인하세요.");
                e.printStackTrace();
            }
        };
    }
}