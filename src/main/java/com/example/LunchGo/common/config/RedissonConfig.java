package com.example.LunchGo.common.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 분산 락(Distributed Lock) 및 예약 대기열 등 복잡한 분산 처리를 위한 Redisson 설정 클래스입니다.
 */
@Configuration
@RequiredArgsConstructor
public class RedissonConfig {

    private static final String REDIS_PROTOCOL_FORMAT = "redis://%s:%d";

    @Value("${spring.redis.redisson.pool.size}")
    private int connectionPoolSize;

    @Value("${spring.redis.redisson.pool.min-idle-size}")
    private int connectionMinIdleSize;

    private final RedisProperties redisProperties;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // Redis 주소 형식 구성 (redis://host:port)
        String address = String.format(REDIS_PROTOCOL_FORMAT, redisProperties.getHost(), redisProperties.getPort());

        // Single Server 모드 설정
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(address)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinIdleSize);

        // 비밀번호가 설정된 경우에만 설정
        if (StringUtils.hasText(redisProperties.getPassword())) {
            serverConfig.setPassword(redisProperties.getPassword());
        }
        
        // 사용자 이름이 설정된 경우 (ACL 사용 시 등)
        if (StringUtils.hasText(redisProperties.getUsername())) {
             serverConfig.setUsername(redisProperties.getUsername());
        }

        return Redisson.create(config);
    }
}
