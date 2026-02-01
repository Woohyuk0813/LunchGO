# 일자별 조회/예약 확정 비동기 집계 설계

작성일: 2025-12-27
갱신일: 2025-12-27
담당: 추천/통계

## 🎯 목표

- 식당 상세 조회 및 예약 확정 이벤트를 Redis에 누적하고 DB에 비동기 반영
- 1~5분 지연 허용 범위 내에서 집계 성능과 DB 부하 최적화
- KST 기준으로 일자 통일

## ✅ 정책 요약

- 조회수 집계 기준: 사용자가 특정 식당 상세 페이지에 진입했을 때 1회 집계
- 중복 조회 필터: 동일 사용자(또는 세션/IP) 기준 일정 시간 내 중복 조회는 제외
- 예약 확정 집계 기준: 결제 완료 트랜잭션과 함께 확정 수 증가 (중복 처리 방지)
- 집계 저장소: Redis 카운터 누적 후 배치 작업으로 DB flush
- 일자 기준: KST (Asia/Seoul)

## 🧭 데이터 흐름

1) 사용자 상세 진입
- 중복 조회 필터를 통과하면 Redis 카운터 증가

2) 예약 결제 완료
- 결제 완료 트랜잭션 커밋 시 Redis 카운터 증가
- 결제/예약 중복 처리 방지 키로 idempotency 보장

3) 배치 집계(1~5분 주기)
- Redis 카운터를 읽어 DB 일자 집계 테이블에 bulk upsert
- 정상 반영 후 해당 Redis 키 초기화

## 🧱 Redis 키 설계

- 조회수 키
  - key: stats:view:{yyyyMMdd}:{restaurantId}
  - value: count (INCR)
- 예약 확정수 키
  - key: stats:confirm:{yyyyMMdd}:{restaurantId}
  - value: count (INCR)

중복 방지용 키(조회)
- key: stats:view:dedupe:{yyyyMMdd}:{restaurantId}:{userKey}
- TTL: 10~30분 (정책에 맞게 조정)

중복 방지용 키(예약 확정)
- key: stats:confirm:dedupe:{paymentId}
- TTL: 7~30일 (결제 기록 보존 정책에 맞춤)

## 🧱 Redis Hash 방식(대안)

대량 키 생성과 SCAN 비용을 줄이기 위해 Hash로 집계할 수 있다.

- 조회수 Hash
  - key: stats:view:{yyyyMMdd}
  - field: {restaurantId}
  - value: count (HINCRBY)
- 예약 확정수 Hash
  - key: stats:confirm:{yyyyMMdd}
  - field: {restaurantId}
  - value: count (HINCRBY)

장점

- 키 수 감소로 메모리와 SCAN 부담 완화
- 날짜 단위로 묶여 관리가 단순함

주의

- Hash field가 매우 커질 경우 단일 키 핫스팟 가능
- 배치 플러시 시 HGETALL 비용 증가 가능

권장 적용 기준

- 일자 단위 식당 수가 많고 Redis 키 폭증이 우려되는 경우
- 배치 작업에서 날짜별로 일괄 처리하는 흐름이 자연스러운 경우

## 📚 Redis 용어 정리 & 활용 예시

## 🧭 초보자용 흐름도(그림/순서도)

### 전체 흐름(조회/예약 -> Redis -> DB)

```text
[사용자]
   |
   v
[상세 조회 / 결제 완료]
   |
   v
[중복 체크(SETNX)]
   |
   +-- 실패(이미 처리됨) --> [끝]
   |
   v
[Redis 카운터 증가(INCR/HINCRBY)]
   |
   v
[배치 작업(1~5분)]
   |
   v
[DB 일자 집계 테이블 UPSERT]
   |
   v
[Redis 카운터 초기화]
```

### 조회수 증가 상세 흐름

```text
[상세 페이지 진입]
   |
   v
[중복 체크 키 생성 시도]
   |
   +-- 이미 존재 --> [조회수 증가 없음]
   |
   v
[INCR stats:view:yyyyMMdd:restaurantId]
```

### 예약 확정 증가 상세 흐름

```text
[결제 완료 트랜잭션 커밋]
   |
   v
[paymentId로 중복 체크]
   |
   +-- 이미 처리됨 --> [확정수 증가 없음]
   |
   v
[INCR stats:confirm:yyyyMMdd:restaurantId]
```

### 배치 플러시 흐름

```text
[스케줄러 실행]
   |
   v
[락 획득 시도]
   |
   +-- 실패 --> [이번 배치 스킵]
   |
   v
[Redis 카운터 읽기]
   |
   v
[DB UPSERT]
   |
   v
[Redis 카운터 삭제]
   |
   v
[락 해제]
```

### 실제 시나리오 예시

#### 시나리오 A: 같은 사용자가 10분 내 3번 재방문

- 조건: dedupe TTL 15분, restaurantId=101, userId=55
- 목표: 1회만 조회수 증가

```text
00:00 사용자가 상세 페이지 진입
  - SETNX stats:view:dedupe:20251227:101:user-55 (성공)
  - INCR stats:view:20251227:101 (1)

00:05 같은 사용자 재진입
  - SETNX 실패(키 존재)
  - 조회수 증가 없음

00:10 같은 사용자 재진입
  - SETNX 실패(키 존재)
  - 조회수 증가 없음

00:16 TTL 만료 후 재진입
  - SETNX 성공
  - INCR stats:view:20251227:101 (2)
```

#### 시나리오 B: 결제 완료 이벤트 중복 수신

- 조건: paymentId=pay-777, restaurantId=101
- 목표: 확정수 중복 증가 방지

```text
결제 완료 이벤트 1회 수신
  - SETNX stats:confirm:dedupe:pay-777 (성공)
  - INCR stats:confirm:20251227:101 (1)

결제 완료 이벤트 재전송
  - SETNX 실패(이미 처리됨)
  - 확정수 증가 없음
```

#### 시나리오 C: 배치 플러시 후 DB 반영

- 조건: stats:view:20251227:101=25, stats:confirm:20251227:101=3
- 목표: DB에 누적 반영 후 Redis 초기화

```text
배치 시작
  - 락 획득 성공
  - Redis 카운터 읽기
  - DB UPSERT (view +25, confirm +3)
  - Redis 카운터 삭제
  - 락 해제
```

### 기본 개념

- Key: Redis의 기본 식별자. 문자열로 관리한다.
- Value: Key에 매핑된 데이터. 타입(String/Hash/Set 등)에 따라 구조가 다르다.
- TTL: Key가 자동으로 만료되는 시간(초 단위). 만료 시 삭제된다.
- Atomic: Redis의 명령은 단일 스레드로 처리되어 원자성이 보장된다.

### 주요 데이터 타입

- String: 단일 값. 카운터에 자주 사용된다.
- Hash: 하나의 Key 아래 여러 field-value를 저장하는 구조.

### 자주 쓰는 명령

- INCR: String 값을 1 증가시킨다.
- HINCRBY: Hash의 field 값을 지정한 숫자만큼 증가시킨다.
- SETNX: Key가 없을 때만 값을 설정한다. 중복 방지에 사용한다.
- EXPIRE: TTL을 설정한다.
- SCAN: Key를 패턴으로 순회한다(대량 키 스캔용).
- HGETALL: Hash의 모든 field-value를 가져온다.
- DEL: Key를 삭제한다.

### 활용 예시 1) 조회수 카운팅 (String)

```text
Key: stats:view:20251227:101
Value: 42

INCR stats:view:20251227:101
```

### 활용 예시 2) 조회 중복 방지 (SETNX + TTL)

```text
Key: stats:view:dedupe:20251227:101:user-55
Value: 1

SETNX stats:view:dedupe:20251227:101:user-55 1
EXPIRE stats:view:dedupe:20251227:101:user-55 900
```

### 활용 예시 3) 예약 확정 카운팅 (String)

```text
Key: stats:confirm:20251227:101
Value: 5

INCR stats:confirm:20251227:101
```

### 활용 예시 4) Hash 기반 집계

```text
Key: stats:view:20251227
Field: 101 -> 42
Field: 205 -> 11

HINCRBY stats:view:20251227 101 1
HINCRBY stats:view:20251227 205 1
```

### 활용 예시 5) 배치 플러시용 락

```text
Key: stats:flush:lock:20251227
Value: instance-1

SETNX stats:flush:lock:20251227 instance-1
EXPIRE stats:flush:lock:20251227 120
```

## 🧾 DB 반영 방식

- 대상 테이블: daily_restaurant_stats
- 처리 방식: bulk upsert

예시 SQL

```sql
INSERT INTO daily_restaurant_stats (stat_date, restaurant_id, view_count, confirm_count)
VALUES
  (?, ?, ?, ?),
  ...
ON DUPLICATE KEY UPDATE
  view_count = view_count + VALUES(view_count),
  confirm_count = confirm_count + VALUES(confirm_count);
```

## 🛠️ 구현 계획

### 1) 조회수 증가 API

- 식당 상세 조회 컨트롤러/서비스에서 실행
- 중복 조회 필터 통과 시 Redis INCR

의사 코드

```java
if (isNotDuplicateView(userKey, restaurantId, todayKst)) {
    redis.incr("stats:view:yyyyMMdd:restaurantId");
}
```

중복 조회 필터 방식

- userKey 우선순위: userId > sessionId > ip
- Redis SETNX로 dedupe 키 생성 후 TTL 부여
- 실패 시(이미 존재) 조회 카운트 증가 없음

### 2) 예약 확정 증가 로직

- 결제 완료 트랜잭션 커밋 이후 실행
- 결제 식별자 기준으로 중복 확인 후 Redis INCR

의사 코드

```java
if (markConfirmProcessed(paymentId)) {
    redis.incr("stats:confirm:yyyyMMdd:restaurantId");
}
```

### 3) 배치 플러시 작업 (Safe Batch)

#### 전략 1: 조회수 - 차감 방식(Differential Update)

- Hash 구조 전환 권장: stats:view:{yyyyMMdd}
- DB 반영 성공 건만 차감하여 중복 집계를 방지

로직

- HGETALL stats:view:20251227로 전체 식당 카운트 조회
- 조회된 Map을 순회하며 DB Bulk Upsert 실행
- 성공한 식당 ID/카운트만 HINCRBY로 음수 차감
- 값이 0 이하인 field는 HDEL로 정리 (Lua Script로 일괄 처리 가능)

의사 코드

```java
Map<Long, Long> viewCounts = redis.hgetAll("stats:view:yyyyMMdd");
upsertDailyStats(viewCounts);
for (Map.Entry<Long, Long> entry : viewCounts.entrySet()) {
    redis.hincrby("stats:view:yyyyMMdd", entry.getKey(), -entry.getValue());
}
redis.hdelIfZeroOrLess("stats:view:yyyyMMdd");
```

#### 전략 2: 예약 확정 - RENAME + 검증 방식

- 예약 확정은 건수가 적으므로 RENAME 방식 채택
- RENAME은 원자적이며, 실패 시 processing 키가 남아 안전하다

로직

- RENAME stats:confirm:{date} → stats:confirm:{date}:processing
- processing 키를 읽어 DB 반영 (idempotency 체크 포함)
- DB 반영 완료 후 processing 키 삭제
- processing 키가 남아 있으면 다음 배치에서 재처리/알람

의사 코드

```java
redis.rename("stats:confirm:yyyyMMdd", "stats:confirm:yyyyMMdd:processing");
Map<Long, Long> confirmCounts = redis.hgetAll("stats:confirm:yyyyMMdd:processing");
upsertDailyStats(confirmCounts);
redis.del("stats:confirm:yyyyMMdd:processing");
```

## 🔒 락/중복 방지 전략

### 1) 조회수 중복 방지(SETNX)

- dedupe 키를 SETNX로 생성하고 TTL 부여
- 성공 시에만 조회수 카운터 증가

의사 코드

```java
if (redis.setnx(dedupeKey, "1", ttlMinutes)) {
    redis.incr(viewKey);
}
```

### 2) 예약 확정 중복 방지(idempotency)

- paymentId 기준 dedupe 키로 1회 처리 보장
- 결제 완료 이벤트가 재전송되어도 중복 증가 방지

의사 코드

```java
if (redis.setnx(confirmDedupeKey, "1", ttlDays)) {
    redis.incr(confirmKey);
}
```

### 3) 배치 플러시 락

동일 시각에 배치가 중복 실행되는 경우를 막기 위해 분산 락을 사용한다.

- 락 키: stats:flush:lock:{yyyyMMdd}
- TTL: 배치 최대 수행 시간 + 여유(예: 2~5분)
- 락 획득 실패 시 배치 skip

의사 코드

```java
if (!redis.setnx(lockKey, instanceId, lockTtlSeconds)) {
    return;
}
try {
    flushToDb();
} finally {
    redis.del(lockKey);
}
```

### 4) 안정성 고려

- 배치 실패 시 카운터 키는 삭제하지 않음
- 락 TTL 만료 전에 작업이 끝나지 않으면 재실행 위험이 있으므로
  최대 수행 시간을 모니터링하고 TTL을 보수적으로 설정

## 🧩 원자성 보장 전략 (Safe Batch Processing)

배치가 DB 반영 후 Redis 삭제 전에 종료되면 중복 집계가 발생할 수 있다.
아래 전략으로 원자성을 높인다.

### 1) 조회수: 차감(Differential Update) 방식

- Hash에서 읽은 값을 DB 반영 후 동일 값을 음수로 차감
- 장애 발생 시에도 “이미 반영된 수치”만큼만 감소하므로 중복 집계 위험 감소
- 값이 0 이하가 된 field는 정리(HDEL)

장점: 중복 집계 방지, Hash 키 수 관리에 유리  
단점: 부분 실패 시 재처리 로직이 필요

### 2) 예약 확정: RENAME + 검증 방식

- 키를 processing으로 원자 이동 후 배치 처리
- 처리 성공 후 삭제, 실패 시 processing 키가 남아 재처리 가능

장점: 구현 단순, 안전성 높음  
단점: 키 수가 많으면 RENAME 비용 증가

### 3) 버킷 스왑(working key) 방식

- 쓰기용 키와 처리용 키를 분리한다
- 배치 시작 시 키 네임스페이스를 스왑해 새 쓰기를 다른 버킷으로 받는다
- 배치는 이전 버킷만 처리하므로 중복 집계 위험이 줄어든다

### 4) 배치 처리용 중간 저장(가장 안전)

- Redis에서 읽은 값을 별도 테이블(또는 로그)에 먼저 기록
- DB 집계 반영 완료 후 완료 플래그를 업데이트
- 장애 발생 시 “미완료 로그”만 재처리

장점: 재처리 안전성 최고  
단점: 구현 복잡도 및 저장 비용 증가

### 5) 보류 전략

- Lua Script(Get+Del) 방식은 삭제 이후 DB 반영 실패 시 데이터 유실 위험이 있어 보류

### 6) 권장 조합

- 조회수: Hash + 차감(Differential) + 배치 락
- 예약 확정: RENAME + idempotency 체크
- 높은 정합성 요구: 중간 저장 + 재처리 큐

## ⏱️ 운영 파라미터

- 배치 주기: 1~5분
- 조회 중복 필터 TTL: 10~30분
- 예약 확정 중복 방지 TTL: 7~30일

## 🧩 배치 구현 코드 설계(초안)

### 패키지/클래스 구성

- `restaurant.stats`
  - `RestaurantStatsEventService`
    - 조회/예약 이벤트 Redis 누적 처리
  - `RestaurantStatsBatchScheduler`
    - 1~5분 주기로 배치 실행
  - `RestaurantStatsBatchService`
    - 조회수/예약 확정 배치 플러시 로직
  - `DailyRestaurantStatsRepository`
    - DB bulk upsert (MyBatis 또는 JPA)
  - `RedisKeyFactory`
    - KST 기준 key 생성 유틸
- `RedisLockService`
  - 배치 락 획득/해제

### 연동 TODO (실제 사용자 상세 API)

- 현재 사용자용 식당 상세 조회 API는 별도 구현 예정
- 구현 시, 상세 조회 진입 지점에서 `RestaurantStatsEventService.recordView(...)` 호출 필요
- userKey 우선순위: userId > sessionId > ip

## 🧩 상세 구현 코드 설명(현재 반영 버전)

### 1) 조회수/예약 확정 이벤트 누적

- 위치: `src/main/java/com/example/LunchGo/restaurant/stats/RestaurantStatsEventService.java`
- 역할: 중복 필터 후 Redis Hash에 카운트 누적
- 주의사항: userKey는 `userId > sessionId > ip` 순으로 생성하며, 빈 값이면 누적하지 않음

핵심 로직

```java
public void recordView(Long restaurantId, String userKey) {
    LocalDate today = keyFactory.todayKst();
    String dedupeKey = keyFactory.viewDedupe(today, restaurantId, userKey);
    if (!redisRepository.setIfAbsent(dedupeKey, "1", Duration.ofMinutes(viewDedupeTtlMinutes))) {
        return;
    }
    redisRepository.hincrBy(keyFactory.viewHash(today), restaurantId.toString(), 1L);
}

public void recordConfirm(Long restaurantId, String paymentId) {
    LocalDate today = keyFactory.todayKst();
    String dedupeKey = keyFactory.confirmDedupe(paymentId);
    if (!redisRepository.setIfAbsent(dedupeKey, "1", Duration.ofDays(confirmDedupeTtlDays))) {
        return;
    }
    redisRepository.hincrBy(keyFactory.confirmHash(today), restaurantId.toString(), 1L);
}
```

### 2) 배치 플러시 (조회수: 차감 방식)

- 위치: `src/main/java/com/example/LunchGo/restaurant/stats/RestaurantStatsBatchService.java`
- 역할: Hash에서 읽고 DB 반영 후 음수 차감으로 중복 집계 방지
- 주의사항: chunk 단위로 DB upsert 후 차감하며, 0 이하 field는 정리

```java
public void flushViews(LocalDate date) {
    Map<String, String> rawCounts = redisRepository.hGetAll(keyFactory.viewHash(date));
    List<Map.Entry<Long, Long>> entries = toLongEntries(rawCounts);
    for (List<Map.Entry<Long, Long>> chunk : chunk(entries, chunkSize)) {
        statsRepository.upsertViewCounts(date, chunk);
        for (Map.Entry<Long, Long> entry : chunk) {
            long newValue = redisRepository.hincrBy(
                keyFactory.viewHash(date), entry.getKey().toString(), -entry.getValue()
            );
            if (newValue <= 0L) {
                redisRepository.hDelete(keyFactory.viewHash(date), entry.getKey().toString());
            }
        }
    }
}
```

### 3) 배치 플러시 (예약 확정: RENAME 방식)

- 위치: `src/main/java/com/example/LunchGo/restaurant/stats/RestaurantStatsBatchService.java`
- 역할: RENAME으로 processing 키 이동 후 안전 처리
- 주의사항: processing 키가 남아있으면 재처리 대상이므로 우선 처리

```java
public void flushConfirms(LocalDate date) {
    String sourceKey = keyFactory.confirmHash(date);
    String processingKey = keyFactory.confirmProcessingHash(date);
    if (!redisRepository.renameIfPresent(sourceKey, processingKey)) {
        return;
    }
    Map<String, String> rawCounts = redisRepository.hGetAll(processingKey);
    List<Map.Entry<Long, Long>> entries = toLongEntries(rawCounts);
    for (List<Map.Entry<Long, Long>> chunk : chunk(entries, chunkSize)) {
        statsRepository.upsertConfirmCounts(date, chunk);
    }
    redisRepository.delete(processingKey);
}
```

### 4) 배치 스케줄러

- 위치: `src/main/java/com/example/LunchGo/restaurant/stats/RestaurantStatsBatchScheduler.java`
- 역할: 락 획득 후 주기적으로 flush 실행
- 주의사항: 락 TTL은 배치 최대 소요 시간보다 길게 설정

```java
@Scheduled(fixedDelayString = "${stats.flush.interval-ms:180000}")
public void flushStats() {
    LocalDate today = keyFactory.todayKst();
    String lockKey = keyFactory.flushLock(today);
    if (!redisRepository.tryLock(lockKey, lockValue, Duration.ofSeconds(lockTtlSeconds))) {
        return;
    }
    try {
        batchService.flushViews(today);
        batchService.flushConfirms(today);
    } finally {
        redisRepository.releaseLock(lockKey, lockValue);
    }
}
```

### 5) 상세 조회/결제 완료 연동

- 상세 조회(사업자용): `BusinessRestaurantController#getRestaurantDetail`
  - userKey 생성 후 `recordView` 호출
  - TODO: 사용자용 상세 API 구현 시 이동 예정
- 결제 완료: `ReservationPaymentService`
  - 결제 완료 트랜잭션 commit 후 `recordConfirm` 호출
  - `TransactionSynchronization`으로 after-commit 보장
- 주의사항: 결제 완료 이벤트 중복 호출은 paymentId dedupe로 차단

### 핵심 메서드 설계

#### 1) 이벤트 누적 (조회/예약)

```java
// 조회수 누적(중복 필터)
void recordView(Long restaurantId, String userKey) {
    String dateKey = keyFactory.kstDateKey();
    String dedupeKey = keyFactory.viewDedupe(dateKey, restaurantId, userKey);
    if (redis.setnx(dedupeKey, "1", viewTtlMinutes)) {
        redis.hincrby(keyFactory.viewHash(dateKey), restaurantId, 1);
    }
}

// 예약 확정 누적(idempotency)
void recordConfirm(Long restaurantId, String paymentId) {
    String dateKey = keyFactory.kstDateKey();
    String dedupeKey = keyFactory.confirmDedupe(paymentId);
    if (redis.setnx(dedupeKey, "1", confirmTtlDays)) {
        redis.hincrby(keyFactory.confirmHash(dateKey), restaurantId, 1);
    }
}
```

#### 2) 배치 스케줄러

```java
@Scheduled(fixedDelayString = "${stats.flush.interval-ms:180000}")
void flushJob() {
    if (!lockService.tryLock("stats:flush:lock:" + todayKst)) {
        return;
    }
    try {
        batchService.flushViews(todayKst);
        batchService.flushConfirms(todayKst);
    } finally {
        lockService.unlock("stats:flush:lock:" + todayKst);
    }
}
```

#### 3) 조회수 배치 (차감 방식)

```java
void flushViews(String dateKey) {
    Map<Long, Long> counts = redis.hgetAll(viewHash(dateKey));
    if (counts.isEmpty()) return;

    // chunk 단위 upsert 성공 후 차감
    for (List<Entry<Long, Long>> chunk : chunked(counts, 500)) {
        upsertDailyStats(dateKey, chunk, StatType.VIEW);
        for (Entry<Long, Long> e : chunk) {
            redis.hincrby(viewHash(dateKey), e.getKey(), -e.getValue());
        }
    }
    redis.hdelIfZeroOrLess(viewHash(dateKey));
}
```

#### 4) 예약 확정 배치 (RENAME)

```java
void flushConfirms(String dateKey) {
    String source = confirmHash(dateKey);
    String processing = confirmProcessingHash(dateKey);
    if (!redis.renameIfPresent(source, processing)) return;

    Map<Long, Long> counts = redis.hgetAll(processing);
    upsertDailyStats(dateKey, counts, StatType.CONFIRM);
    redis.del(processing);
}
```

### DB Upsert 설계

```sql
INSERT INTO daily_restaurant_stats (stat_date, restaurant_id, view_count, confirm_count)
VALUES (?, ?, ?, ?), ...
ON DUPLICATE KEY UPDATE
  view_count = view_count + VALUES(view_count),
  confirm_count = confirm_count + VALUES(confirm_count);
```

### 구현 포인트

- KST 기준 날짜: `ZonedDateTime.now(ZoneId.of("Asia/Seoul"))`
- Hash 기반 조회수는 key 폭증 방지에 유리
- 예약 확정은 RENAME 처리로 안전성 확보
- 조회수는 chunk upsert 후 차감 처리로 중복 집계 방지
- `renameIfPresent`는 소스 키가 없으면 no-op 처리

## 🧪 검증 체크리스트

- 동일 사용자가 동일 식당을 재진입 시 조회수 중복 증가가 제거되는지
- 결제 완료 이벤트가 재전송되어도 확정수가 중복 증가하지 않는지
- 배치 실패 시 Redis 누적값이 유지되는지
- KST 기준 날짜가 일관되게 반영되는지

## ✅ 기능 테스트 절차(단계별)

### 1) 사전 준비

- Redis 실행 확인
- DB에 테스트용 식당/예약/결제 데이터가 준비되어 있는지 확인
- `application.yml`에서 통계 설정 확인
  - `stats.flush.interval-ms`
  - `stats.view.dedupe-ttl-minutes`
  - `stats.confirm.dedupe-ttl-days`
  - `stats.flush.chunk-size`

### 2) 조회수 집계 테스트

1. 식당 상세 조회 API 호출(동일 userKey)
2. Redis Hash 키 확인
   - `stats:view:{yyyyMMdd}`에 `restaurantId` field가 증가했는지 확인
3. 동일 userKey로 재호출
   - TTL 내에서는 증가하지 않아야 함
4. TTL 만료 후 재호출
   - 1회 증가하는지 확인

### 3) 예약 확정 집계 테스트

1. 결제 완료 플로우 호출 (completePayment 또는 webhookPaid)
2. Redis Hash 키 확인
   - `stats:confirm:{yyyyMMdd}`에 `restaurantId` field가 증가했는지 확인
3. 같은 paymentId로 재호출
   - dedupe 키로 인해 증가하지 않아야 함

### 4) 배치 플러시 테스트

1. 배치 스케줄러 대기 또는 수동 호출
2. DB의 `daily_restaurant_stats` 확인
   - view_count/confirm_count가 누적 반영되는지 확인
3. Redis 상태 확인
   - 조회수 Hash: 처리된 field가 0 이하로 정리되었는지 확인
   - 예약 확정 Hash: processing 키가 삭제되었는지 확인

### 5) 장애 시나리오 테스트(선택)

1. 배치 중단(의도적으로 예외 발생) 시 Redis 키가 유지되는지 확인
2. 재실행 시 누적값이 정상 반영되는지 확인

## ✅ 테스트 결과 요약

- 조회수 집계
  - `/api/business/restaurants/1` 호출 후 `stats:view:{yyyyMMdd}`의 `1` 필드가 `1` 증가
  - 동일 userKey로 재호출 시 값 증가 없음(중복 필터 정상)
- 배치 플러시(조회수)
  - DB `daily_restaurant_stats`에 `stat_date=2025-12-28`, `view_count=1` 반영
  - Redis `stats:view:{yyyyMMdd}`에서 해당 field 정리됨
- 예약 확정 집계(시뮬레이션)
  - `HINCRBY stats:confirm:{yyyyMMdd} 1 1` 후 배치 반영
  - DB `daily_restaurant_stats`에 `confirm_count=1` 반영
  - Redis `stats:confirm:{yyyyMMdd}` 및 `:processing` 키 정리됨

## 🧪 테스트 입력/실행 로그(요약)

### 1) 조회수 집계

입력

```bash
curl -i -H "X-User-Id: 101" http://localhost:8080/api/business/restaurants/1
redis-cli -h 127.0.0.1 -p 6379 -a 'userredis1234!' HGET stats:view:$(date +%Y%m%d) 1
```

결과(요약)

```text
HTTP/1.1 200
HGET -> "1"
```

중복 확인

```bash
curl -i -H "X-User-Id: 101" http://localhost:8080/api/business/restaurants/1
redis-cli -h 127.0.0.1 -p 6379 -a 'userredis1234!' HGET stats:view:$(date +%Y%m%d) 1
```

결과(요약)

```text
HGET -> "1"
```

### 2) 배치 플러시(조회수)

입력

```bash
/opt/homebrew/opt/mysql@8.0/bin/mysql --protocol=TCP -h 127.0.0.1 -P 3307 -u lunchgo_user -p -D lunchgo \
  -e "SELECT stat_date, restaurant_id, view_count, confirm_count FROM daily_restaurant_stats WHERE restaurant_id = 1 ORDER BY stat_date DESC LIMIT 5;"
```

결과(요약)

```text
2025-12-28 | restaurant_id=1 | view_count=1 | confirm_count=0
```

### 3) 예약 확정 집계(시뮬레이션)

입력

```bash
redis-cli -h 127.0.0.1 -p 6379 -a 'userredis1234!' HINCRBY stats:confirm:$(date +%Y%m%d) 1 1
redis-cli -h 127.0.0.1 -p 6379 -a 'userredis1234!' HGET stats:confirm:$(date +%Y%m%d) 1
```

결과(요약)

```text
HGET -> "1"
```

배치 반영 확인

```bash
/opt/homebrew/opt/mysql@8.0/bin/mysql --protocol=TCP -h 127.0.0.1 -P 3307 -u lunchgo_user -p -D lunchgo \
  -e "SELECT stat_date, restaurant_id, view_count, confirm_count FROM daily_restaurant_stats WHERE restaurant_id = 1 ORDER BY stat_date DESC LIMIT 5;"
```

결과(요약)

```text
2025-12-28 | restaurant_id=1 | view_count=1 | confirm_count=1
```

정리 확인

```bash
redis-cli -h 127.0.0.1 -p 6379 -a 'userredis1234!' HGET stats:confirm:$(date +%Y%m%d) 1
redis-cli -h 127.0.0.1 -p 6379 -a 'userredis1234!' HGETALL stats:confirm:$(date +%Y%m%d):processing
```

결과(요약)

```text
HGET -> (nil)
HGETALL -> (empty array)
```

## 📌 추후 고려 사항

- Redis 장애 시 임시 로컬 큐/로그로 fallback 여부
- 대량 key scan 최적화를 위한 패턴 분리 또는 Redis Hash 사용 검토
- 집계 테이블 인덱스 최적화 (stat_date, restaurant_id)
