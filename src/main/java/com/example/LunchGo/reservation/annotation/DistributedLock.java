package com.example.LunchGo.reservation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 식당 대기열 락 (Redisson) 키 (SpEL 지원)
     */
    String lockKey();

    /**
     * 개인 중복 방지 락 (Lettuce) 키 (SpEL 지원)
     * 값이 비어있으면 개인 락은 사용하지 않음
     */
    String userLockKey() default "";

    /**
     * 식당 락 대기 시간 (Redisson)
     * 2L로 설정하여 가벼운 경합은 서버 내부에서 대기 후 처리하고(Throughput 향상),
     * 대기가 길어질 경우에만 예외를 발생시켜 클라이언트 대기 모달을 유도함 (Fail-Fast).
     * AOP 우선순위 조정(@Order)과 결합하여 대기 시 DB 커넥션 낭비를 방지함.
     */
    long waitTime() default 2L;

    /**
     * 식당 락 점유 시간 (Redisson)
     * Watchdog을 사용하려면 이 값을 -1로 설정하거나 비워둬야 합니다.
     * Watchdog은 락을 점유한 스레드가 살아있는 동안 자동으로 락 만료 시간을 연장하여,
     * 트랜잭션이 길어지더라도 락이 조기 해제되는 것을 방지하는 가장 안전한 방법입니다.
     */
    long leaseTime() default -1L;

    /**
     * 개인 락 점유 시간 (Lettuce)
     */
    long userLockTime() default 3000L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
