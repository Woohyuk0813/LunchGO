package com.example.LunchGo.restaurant.stats;

import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RestaurantStatsRedisRepository {
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('GET', KEYS[1]) == ARGV[1] then " +
                            "return redis.call('DEL', KEYS[1]) " +
                            "else return 0 end",
                    Long.class
            );

    private final StringRedisTemplate template;

    public boolean setIfAbsent(String key, String value, Duration ttl) {
        ValueOperations<String, String> ops = template.opsForValue();
        return Boolean.TRUE.equals(ops.setIfAbsent(key, value, ttl));
    }

    public long hincrBy(String key, String field, long delta) {
        HashOperations<String, String, String> ops = template.opsForHash();
        Long result = ops.increment(key, field, delta);
        return result == null ? 0L : result;
    }

    public Map<String, String> hGetAll(String key) {
        HashOperations<String, String, String> ops = template.opsForHash();
        return ops.entries(key);
    }

    public void hDelete(String key, String field) {
        template.opsForHash().delete(key, field);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(template.hasKey(key));
    }

    public void delete(String key) {
        template.delete(key);
    }

    public boolean renameIfPresent(String source, String target) {
        if (!hasKey(source)) {
            return false;
        }
        try {
            template.rename(source, target);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean tryLock(String key, String value, Duration ttl) {
        return setIfAbsent(key, value, ttl);
    }

    public void releaseLock(String key, String value) {
        template.execute(RELEASE_LOCK_SCRIPT, java.util.List.of(key), value);
    }

}
