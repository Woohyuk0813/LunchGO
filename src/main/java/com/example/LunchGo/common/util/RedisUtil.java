package com.example.LunchGo.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisUtil {
    /**
     * 스프링이 제공하는 Redis 클라이언트,  Redis 명령어를 자바 코드로 보낼 수 있음
     * */
    private final StringRedisTemplate template;
    private final RedissonClient redissonClient;

    public String getData(String key){
        ValueOperations<String, String> ops = template.opsForValue();
        return ops.get(key);
    }
    //Redis에 저장된 key에 해당하는 값(value) 가져오기
    //이메일로 발송된 인증코드 꺼낼 때 사용

    public boolean existData(String key){
        return Boolean.TRUE.equals(template.hasKey(key));
    }
    //key가 Redis에 살아있는 지 확인(유효한지, 시간 초과인지)

    public void setDataExpire(String key, String value, long duration){
        ValueOperations<String, String> ops = template.opsForValue();
        Duration expireDuration = Duration.ofMillis(duration);
        ops.set(key, value, expireDuration);
    }
    //데이터 저장+유효기간 저장
    //데이터를 저장하면서 언제 자동으로 삭제될지 TTL 설정
    //시간 지나면 Redis가 알아서 데이터 지움

    public void deleteData(String key){
        template.delete(key);
    }
    //데이터 즉시 삭제(인증 성공 시 사용하게 됨)

    public boolean updateData(String key, String newValue) {
        if (!existData(key)) { //데이터가 없는 경우
            return false;
        }

        long ttlMillis = template.getExpire(key, TimeUnit.MILLISECONDS);
        ValueOperations<String, String> ops = template.opsForValue();
        ops.set(key, newValue);

        if (ttlMillis > 0) {
            template.expire(key, Duration.ofMillis(ttlMillis)); //시간 남았으면 그대로 설정
        }

        return true;
    }

    public boolean setIfAbsent(String key, String value, long duration) {
        Boolean result = template.opsForValue().setIfAbsent(key, value, Duration.ofMillis(duration));
        return Boolean.TRUE.equals(result);
    }

    /**
     * 특정 키에 대한 분산 락을 가져옵니다.
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 특정 키에 대한 공정 락(Fair Lock)을 가져옵니다.
     * FairLock은 요청한 순서대로(FIFO) 락을 획득함을 보장합니다.
     * 선착순 예약 시스템의 공정성을 위해 사용합니다.
     */
    public RLock getFairLock(String lockKey) {
        return redissonClient.getFairLock(lockKey);
    }

    /**
     * 대기열(Sorted Set)을 가져옵니다.
     */
    public RScoredSortedSet<String> getScoredSortedSet(String key) {
        return redissonClient.getScoredSortedSet(key);
    }

    /**
     * 키의 값을 주어진 delta만큼 증가시키고 증가된 값을 반환합니다. (Redis INCRBY)
     */
    public Long increment(String key, long delta) {
        return template.opsForValue().increment(key, delta);
    }

    /**
     * 키의 값을 주어진 delta만큼 감소시키고 감소된 값을 반환합니다. (Redis DECRBY)
     */
    public Long decrement(String key, long delta) {
        return template.opsForValue().increment(key, -delta);
    }

    /**
     * 키의 현재 값을 조회합니다.
     */
    public Long getCount(String key) {
        String val = template.opsForValue().get(key);
        try {
            return (val == null) ? 0L : Long.parseLong(val);
        } catch (NumberFormatException e) {
            log.error("Redis key [{}] has invalid numeric value: [{}]. Returning 0L.", key, val);
            return 0L;
        }
    }

    /**
     * 예약 슬롯의 Redis 키를 생성합니다.
     * @param restaurantId 식당 ID
     * @param slotDate 예약 날짜
     * @param slotTime 예약 시간
     * @return 생성된 Redis 키 문자열 (예: "seats:1:2023-01-01:12:00")
     */
    public static String generateSeatKey(Long restaurantId, LocalDate slotDate, LocalTime slotTime) {
        return "seats:" + restaurantId + ":" + slotDate.toString() + ":" + slotTime.toString();
    }
}
