package com.example.LunchGo.block.service;

import com.example.LunchGo.common.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BaseBlockService implements BlockService {
    private final RedisUtil redisUtil;

    //block임을 구분하기 위한 접두사
    private static final String BLOCK_PREFIX = "BLOCK:";

    private static final long BLOCK_DURATION_MS = 3600L * 24 * 30 * 1000; //30ㅇㅣㄹ

    @Override
    public void add(Long userId, String reason) {
        String key = BLOCK_PREFIX + userId;

        redisUtil.setDataExpire(key, reason, BLOCK_DURATION_MS);
    }

    @Override
    public boolean has(Long userId) {
        String key = BLOCK_PREFIX + userId;

        return redisUtil.existData(key);
    }
}
