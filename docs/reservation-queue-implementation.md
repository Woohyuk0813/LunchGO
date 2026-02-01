# 동시성 제어 - 예약 대기열 구현

## 문제 상황

- 잔여 예약석에 관한 이전의 동시성 제어 방식에서는 DB 비관적 락에만 의존했기 때문에 DB에 가해지는 부하가 증가하면서 아래의 문제가 발생
  - 예약 요청 시, DB 커넥션을 확보한 상태에서 DB 비관적 락을 걸고 DB 트랜잭션에 진입하는 구조
  - 위 상황에서, 동시에 들어온 예약 요청 수가 크게 증가(수백 ~ 수천 건)할 경우 DB 커넥션이 고갈되어 DeadLock이 발생
    - 'Pool-Locking (DB 커넥션 고갈 데드락)' 현상: 트랜잭션 내부에서 외부 리소스(락)를 대기하면, DB 커넥션을 점유한 채로 멈춰버리면서 발생하는 Deadlock

---

## 목표

- 따라서, 다음의 문제를 개선하기 위해 애플리케이션 계층에 예약 대기열을 추가
  - 동시에 발생한 예약 요청을 처리할 때 DB 커넥션이 고갈되어, 이로 인해 서버가 마비되는 것을 방지
- Redisson을 활용한 예약 대기열 구현을 통해 다음의 목표를 달성하는 것
    - DB 커넥션 고갈로 인해 Deadlock이 발생하게 되는 문제 방지
    - 특정 식당의 특정 시간대에 예약이 몰리는 상황을 공정하게 처리

---

## 구현 방향 설정
- 사용할 수 있는 서버의 디스크 용량이 부족했던 반면, 메모리 용량은 상대적으로 충분
  - 서버의 메모리를 효율적으로 활용하고 DB의 부하를 줄이기 위해 위해 Redis를 사용
- Redis 기반의 Java 라이브러리인 Redisson을 활용하는 방향으로 진행
  - 예약 대기열을 설정한 목적은 특정 식당의 특정 시간대에 예약이 몰리는 상황을 통제하는 것
  - 예약 신청을 하기 위한 기본 정보(예약 날짜, 시간, 인원, 주문할 메뉴 등)를 입력한 다음 바로 결제 단계로 진입해야 함.
    - 사용자가 버튼을 누르고 3~5초 이내에 즉각적인 성공/실패 피드백을 받는 것이 UX 적으로 더 중요
    - 예약 기능이므로 선착순으로 접수할 수 있어야 하기 때문에 일반 락 대신 FairLock을 사용
    - 구현 복잡도를 낮추고 즉시성을 보장하기 위해, Redisson의 FairLock을 이용한 동기식 대기열(Blocking Queue) 모델을 채택

---

## 예약 대기열 구현 - 프론트엔드

### 개요

- 프론트의 예약 대기열 관련 기능은 `frontend/src/composables/useReservationQueue.js` 에 구현
  - `RestaurantBookingPage.vue`, `MenuSelectionPage.vue`에서 활용
- 사용자가 식당을 예약할 때, 자신의 예약이 처리되고 있음을 시각적으로 파악할 수 있는 예약 대기 UI를 제공
- 실제 예약 생성 API(`/api/reservations`)가 호출되었을 때 409 상태코드를 반환하면서 '대기 중'이란 텍스트가 포함되면 예약 대기 UI 출력
  - `409 Conflict`: 특정 예약시간대가 다른 사용자에 의해 잠금 처리되어 있으므로, 현재 새로 들어온 예약 요청은 이 잠금 상태와 "충돌"
  - 중복된 예약이 아니면서, 특정 식당에 사용자가 몰릴 때 대기열 UI를 출력
  - 대기열 UI에서는 현재 대기자 수 및 예상 대기 시간을 출력

### 구현 배경

- 서버에서 재시도 로직을 전담할 경우 다음의 문제들이 발생
  - 서버의 스레드 풀에서 DB와 유사한 Pool Locking 현상이 발생
    - 대기 중인 예약 요청들이 서버 스레드를 모두 점유 -> 서버 스레드 풀 고갈 -> 서비스 전체 장애 유발
  - 서버가 락을 획득할 때까지 대기하게 되면서 사용자가 서버 응답을 받지 못한 상태로 HTTP Timeout 발생
  
- 프론트엔드에서 재시도 로직을 수행할 시 아래의 이점이 존재
  - 재시도 로직을 프론트엔드에서 일부 분담하게 하여 서버에 가해지는 부하를 분산시켜 서버의 안정성 확보 가능
  - 예약 대기열에 관한 백엔드 서버와 프론트엔드 간의 역할을 아래와 같이 구분함으로써 UX 개선 가능
    - 백엔드
      - Redisson의 FairLock을 활용한 실질적인 예약 대기열 로직 수행 
      - 예약 대기 상태임을 나타내는 상태코드(409)를 프론트엔드에게 전송
    - 프론트엔드
      - 백엔드 서버로부터 전달받은 상태코드를 기반으로 예약 대기 현황에 관한 UI를 사용자에게 제공
      - 재시도 간격을 랜덤화하여, 예약 요청이 집중되면서 백엔드 서버에 가해지는 부하를 분산  

### 프론트엔드에서의 예약 대기 - polling

1) 최초 `/api/reservations` 요청이 전달된 시점에, 백엔드 서버의 FairLock에는 락 획득 여부에 관계없이 락을 요청한 스레드/클라이언트 식별자를 시간순으로 기록
    - 이 식별자는 Redisson 클라이언트의 고유 ID(UUID)와 현재 코드를 실행 중인 자바 스레드(Thread)의 ID를 조합하여 자동으로 생성 
2) 클라이언트가 락을 바로 획득하면 예약 생성 트랜잭션(ReservationServiceImpl의 create 메서드) 실행
3) 클라이언트가 락을 획득할 차례가 아니면 재시도 로직 수행
    - 프론트엔드-백엔드 간 짧은 통신(polling)을 진행하는 방식으로 수행
    - polling 양상: 현재 차례 확인 -> 상태코드 반환 -> 상태코드값에 따라 polling 반복 또는 종료

### 재시도 로직 관련 세부 구현 사항

- 예약 요청별 재시도 간격 랜덤화(2초~4초 사이)
  - 예약 대기시간을 초과했을 때의 예약 재시도 로직이 특정 시점에만 집중되는 것을 방지
- 최대 재시도 횟수를 10회로 설정 -> 무한루프 방지
- 중복 예약 요청이 들어온 경우에도 409 상태코드를 반환하기 때문에, 예약 대기와 관련 없는 에러일 경우에는 별도의 에러 모달을 출력하도록 처리
  - 이 부분은 리팩토링 진행 시 예약 대기열 관련 상태코드를 `429 Too Many Requests`로 변경하는 방향으로 개선 필요

### 구현 코드

- frontend/src/composables/useReservationQueue.js에서 구현

<details>
  <summary>전체 구현 코드</summary>
  
  ```javascript
  import { ref, computed } from 'vue';
  import { useRouter } from 'vue-router';
  
  const MAX_RETRIES = 10;
  const RETRY_INTERVAL_MIN = 2000;
  const RETRY_INTERVAL_MAX = 4000;
  const ESTIMATED_TIME_PER_PERSON = 3;
  const DEFAULT_ESTIMATED_TIME = 5;
  
  export function useReservationQueue() {
    const router = useRouter();
    
    const isWaiting = ref(false);
    const modalType = ref('waiting'); // 'waiting' | 'error'
    const modalMessage = ref('');
    const queueErrorMessage = ref(''); // 컴포넌트의 에러 메시지와 구분하기 위해 이름 변경
    const currentWaitingCount = ref(0);
    const estimatedWaitTime = ref(0);
  
    // 예상 대기 시간 포맷팅 (초 -> 분/초)
    const formattedWaitTime = computed(() => {
      const totalSeconds = estimatedWaitTime.value;
      if (totalSeconds <= 0) return `약 ${DEFAULT_ESTIMATED_TIME}초`;
      
      const mins = Math.floor(totalSeconds / 60);
      const secs = totalSeconds % 60;
      
      if (mins > 0) {
        return `약 ${mins}분 ${secs > 0 ? secs + '초' : ''}`.trim();
      }
      return `약 ${totalSeconds}초`;
    });
    
    /**
     * 예약 대기열 처리 로직
     * @param {Function} apiCall 예약 생성 API 호출 함수 (Promise 반환)
     * @param {Function} onSuccess 성공 시 실행할 콜백
     * @param {Ref<Boolean>} isSubmitting 외부 로딩 상태 Ref
     */
    const processQueue = async (apiCall, onSuccess, isSubmitting) => {
      queueErrorMessage.value = '';
      modalType.value = 'waiting';
      let retryCount = 0;
  
      const attempt = async () => {
        try {
          const result = await apiCall();
          if (onSuccess) onSuccess(result);
        } catch (e) {
          const msg = e.response?.data?.message || e?.message || '';
          const waitingCount = e.response?.data?.waitingCount || 0;
  
          // 1. 중복 예약 등 에러 모달 ("대기 중" 없는 409)
          if (e.response?.status === 409 && !msg.includes('대기 중')) {
            modalType.value = 'error';
            modalMessage.value = (msg || '이미 처리된 예약이거나 잔여석이 부족합니다.').replace('. ', '.\n');
            isWaiting.value = true;
            return;
          }
  
          // 2. 대기열 진입 ("대기 중" 포함)
          if (e.response?.status === 409 && msg.includes('대기 중')) {
            if (retryCount < MAX_RETRIES) {
              retryCount++;
              currentWaitingCount.value = waitingCount;
              // 대기 시간 시뮬레이션: 인원당 약 3초 + 기본 5초
              estimatedWaitTime.value = waitingCount > 0 
                ? (waitingCount * ESTIMATED_TIME_PER_PERSON) 
                : DEFAULT_ESTIMATED_TIME;
              
              modalType.value = 'waiting';
              modalMessage.value = '예약 요청이 많아 대기 중입니다.\n잠시만 기다려주세요...';
              isWaiting.value = true;
              
              // 재시도 간격 분산 (Random Backoff/Jitter)
              const retryDelay = RETRY_INTERVAL_MIN + Math.floor(Math.random() * (RETRY_INTERVAL_MAX - RETRY_INTERVAL_MIN));
              setTimeout(attempt, retryDelay);
              return;
            } else {
              modalType.value = 'error';
              modalMessage.value = '현재 예약 요청이 많습니다.\n잠시 후 다시 시도해 주세요.';
              isWaiting.value = true;
            }
          } else {
            // 3. 기타 에러
            queueErrorMessage.value = msg || '예약 생성 중 오류가 발생했습니다.';
          }
        } finally {
          // 재시도 중이 아니고, 에러 모달 상태도 아닐 때만 로딩 해제
          if (modalType.value !== 'error' && (!isWaiting.value || retryCount >= MAX_RETRIES)) {
            isWaiting.value = false;
            if (isSubmitting) isSubmitting.value = false;
          }
        }
      };
  
      await attempt();
    };
  
    const handleQueueModalClose = () => {
      isWaiting.value = false;
      if (modalType.value === 'error') {
        router.push('/');
      }
    };
  
    return {
      isWaiting,
      modalType,
      modalMessage,
      queueErrorMessage,
      currentWaitingCount,
      estimatedWaitTime,
      formattedWaitTime,
      processQueue,
      handleQueueModalClose
    };
  }
  ```
</details>

---

## 예약 대기열 구현 - 백엔드

### 개요

- DistributedLockAop 클래스의 `lock()` 메서드가 담당
- 실질적인 예약 대기열 기능을 수행
- 예약 대기열 관련 기능은 Redisson의 FairLock을 활용
  - Redisson 라이브러리에서는 FairLock 등 예약 대기열 구현에 필요한 기능들을 제공
  - 구현한 코드에서 사용한 getFairLock 메서드는 RedissonFairLock을 반환
    - RedissonFairLock은 선착순을 보장하는 내부 로직으로 인해 Queue와 유사하게 동작하면서도 원자적 연산을 보장
    - 따라서, RedissonFairLock을 사용할 경우, 애플리케이션 레벨에서는 Queue 자료구조 사용 불필요

### 백엔드에서의 예약 대기 - waiting

1. 1차 락 설정: 개인 중복 요청 방지 (Lettuce - Fail-Fast) - DistributedLockAop 클래스의 54~58번 라인
  * userLockKey를 사용하여 Redis에 SETNX 명령을 실행 (setIfAbsent 메서드)
    * 락의 유효 시간(`userLockTime`)은 5초로 짧게 설정
  * 목적: 한 명의 사용자가 예약 버튼을 빠르게 두 번 이상 누르는 것과 같은 중복 요청을 즉시 차단
  * 동작: 만약 userLockKey가 이미 존재하면, DuplicateReservationException을 발생시켜 즉시 요청을 실패 처리하고 에러 메시지를 반환
    ```java
      // 1. [개인 락] Lettuce (Fail-Fast)
      if (userLockKey != null) {
          if (!redisUtil.setIfAbsent(userLockKey, "processing", distributedLock.userLockTime())) {
              throw new DuplicateReservationException("이미 처리 중인 예약 요청입니다. 잠시 후 다시 시도해주세요.");
          }
      }
    ```


2. 2차 잠금: 식당 시간대별 대기열 (Redisson FairLock - FIFO) - DistributedLockAop 클래스의 try 블록
  * 서버 내부에서 신규 예약 생성용 락을 획득하기 위한 실질적인 예약 대기열
  * 대기열 진입: lockKey(식당+시간대)에 대한 락을 시도하기 직전, waiting_count: 라는 접두사가 붙은 Redis 카운터의 값을 1 증가
    * 지정한 시간대에 예약을 시도하는 총 사용자 수를 의미
  * 선착순 락 획득 시도
    * Redisson의 FairLock을 사용하여 락 획득을 시도
      * 최초 `/api/reservations` 요청 전달 시점에, FairLock에서는 락 획득 여부에 관계없이 락을 요청한 스레드/클라이언트 식별자를 시간순으로 기록
        - Redisson FairLock의 식별자는 대기열 내부의 '사용자'를 식별
          - 특정 대기열 내부에서 사용자들 간 순서를 관리하기 위해 사용되는 식별자 -> Redisson FairLock이 순서를 보장할 수 있는 이유
          - Redisson 클라이언트의 고유 ID(UUID)와 현재 코드를 실행 중인 자바 스레드(Thread) ID를 조합하여 자동 생성
        - `@DistributedLock`에서 명시적으로 사용한 `lockKey`는 사용자가 대기하게 될 '대기열'을 식별
      * FairLock은 FIFO(선입선출)를 보장하므로, 먼저 요청한 사용자가 먼저 락을 획득 -> 예약의 공정성 확보
    * 이미 락을 획득한 사용자가 있다면, 락 획득을 위해 대기
      * 지정한 waitTime(기본 2초)이 끝나기 전에 락을 획득하면 예약 생성 트랜잭션 실행
      * 락을 획득하지 못하고 waitTime이 종료되면 `WaitReservationException`을 발생시켜 프론트엔드에게 409 상태코드 전송
        * 프론트엔드에서는 백엔드 서버로부터 전달받은 409 상태코드를 바탕으로 재시도 로직 진행
      ```java
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

        }
      ```

3. 최종 처리 - DistributedLockAop 클래스의 finally 블록 부분

- finally 블록 내부에서 예약 대기열 로직의 성공/실패에 관계없이 항상 실행
  * waiting_count: 카운터를 1 감소시켜 사용자가 대기열에서 이탈했음을 나타냄.
  * 획득했던 FairLock이 있다면 unlock() -> 다음 대기자가 락 획득
    * 락 유효시간(leaseTime)을 별도로 지정하지 않았더라도 예약 생성 트랜잭션이 종료되면 락도 자동 반납됨을 보장
```java
  ... 코드 생략
  
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
```

### 구현 코드

- src/main/java/com/example/LunchGo/reservation/aop/DistributedLockAop.java

<details>

  <summary>전체 구현 코드</summary>

  ```java
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
  ```

</details>

---
