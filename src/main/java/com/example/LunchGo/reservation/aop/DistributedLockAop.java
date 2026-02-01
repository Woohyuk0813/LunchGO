package com.example.LunchGo.reservation.aop;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.annotation.DistributedLock;
import com.example.LunchGo.reservation.exception.DuplicateReservationException;
import com.example.LunchGo.reservation.exception.WaitingReservationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 분산 락 AOP
 * @Order(Ordered.HIGHEST_PRECEDENCE) 설정 이유:
 * - @Transactional 보다 락 AOP가 먼저 실행되어야 함.
 * - 락 대기 시간(waitTime) 동안 DB 커넥션을 점유하지 않도록 하기 위함 (DB 커넥션 고갈 방지).
 * - 트랜잭션 시작 전에 락을 잡고, 트랜잭션 종료 후에 락을 해제하는 순서를 보장함.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {

    private final RedisUtil redisUtil;

    @Around("@annotation(com.example.LunchGo.reservation.annotation.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        // SpEL 파싱하여 키 생성
        String lockKey = (String) CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(), joinPoint.getArgs(), distributedLock.lockKey());
        
        String userLockKey = null;
        if (StringUtils.hasText(distributedLock.userLockKey())) {
            userLockKey = (String) CustomSpringELParser.getDynamicValue(
                    signature.getParameterNames(), joinPoint.getArgs(), distributedLock.userLockKey());
        }

        // 1. [개인 락] Lettuce (Fail-Fast)
        if (userLockKey != null) {
            if (!redisUtil.setIfAbsent(userLockKey, "processing", distributedLock.userLockTime())) {
                throw new DuplicateReservationException("이미 처리 중인 예약 요청입니다. 잠시 후 다시 시도해주세요.");
            }
        }

        // 2. [식당 락] Redisson FairLock (선착순 보장)
        // 일반 Lock 대신 FairLock을 사용하여 요청 순서대로 락을 획득하도록 함 (FIFO)
        RLock rLock = redisUtil.getFairLock(lockKey);
        String waitingCountKey = "waiting_count:" + lockKey;

        try {
            // 대기열 진입: 카운트 증가
            redisUtil.increment(waitingCountKey, 1L);

            boolean available;
            if (distributedLock.leaseTime() == -1L) {
                // leaseTime이 -1L이면 Redisson Watchdog 사용
                // Watchdog은 락을 획득한 스레드가 살아있는 동안 락 만료 시간을 자동으로 연장함.
                // Redisson의 기본 Watchdog 타임아웃은 30초이며, 10초마다 갱신 시도.
                // (참고: Watchdog은 락을 연장해주지만, 비즈니스 로직 및 트랜잭션 완료 후 finally 블록에서
                // rLock.unlock()을 통해 명시적으로 해제되므로 불필요하게 락이 유지되지 않음.)
                available = rLock.tryLock(distributedLock.waitTime(), distributedLock.timeUnit());
            } else {
                // leaseTime이 명시된 경우 해당 시간만큼만 락 점유
                available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            }
            
            if (!available) {
                // 대기열 진입 실패 시 (타임아웃)
                // 개인 락 해제하여 재시도 허용
                if (userLockKey != null) {
                    redisUtil.deleteData(userLockKey);
                }
                
                // 현재 대기 인원 조회
                long currentWaitingCount = redisUtil.getCount(waitingCountKey);
                throw new WaitingReservationException("접속자가 많아 대기 중입니다. 잠시 후 자동으로 재시도합니다.", currentWaitingCount);
            }

            // --- 비즈니스 로직 실행 ---
            return joinPoint.proceed();

        } catch (InterruptedException e) {
            // 인터럽트 발생 시 락 해제 및 상태 복구
            if (userLockKey != null) {
                redisUtil.deleteData(userLockKey);
            }
            Thread.currentThread().interrupt(); // 인터럽트 상태 복구
            throw new IllegalStateException("DistributedLock AOP: Lock acquisition interrupted", e); // 런타임 예외로 래핑
        } finally {
            // 식당 락 해제
            if (rLock != null && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
            // 작업 종료(성공/실패/포기) 후 대기열 이탈: 카운트 감소
            redisUtil.decrement(waitingCountKey, 1L);
            
            // 개인 락은 성공 시 해제하지 않음 (TTL 유지)
            // 단, 예외가 발생해서 여기까지 왔다면(catch 블록을 거치지 않은 런타임 예외 등) 해제해야 할 수도 있지만,
            // 현재 로직상 성공 시에는 유지하는 것이 정책이므로 그대로 둠.
            // (비즈니스 예외 발생 시에도 개인 락이 유지되는 부작용은 있으나, 이는 5초 후 해소됨)
        }
    }
}
