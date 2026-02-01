# 예약 이력 쿼리 분석 및 개선안

## 문제 요약
현재 `ReservationHistoryMapper.xml`의 조회 쿼리는 `reviews`와 `review_tag_maps`를 조인한 뒤
윈도우 함수를 계산한다. 이 구조는 **태그 수만큼 중복 행이 생긴 상태에서 방문 횟수/간격이
계산되는 문제**가 발생할 수 있다. 결과적으로 `visitCount`와 `daysSinceLastVisit` 값이
실제보다 부풀어질 위험이 있다.

## 문제 발생 원인
현재 쿼리 구조(요약):
```sql
FROM reservations r
JOIN slots ...
JOIN restaurants ...
LEFT JOIN reviews rv ON rv.reservation_id = r.reservation_id
LEFT JOIN review_tag_maps rtm ON rtm.review_id = rv.review_id
LEFT JOIN review_tags rt ON rt.tag_id = rtm.tag_id
...
SELECT
  CASE WHEN r.status = 'COMPLETED'
       THEN SUM(...) OVER (PARTITION BY r.user_id, res.restaurant_id ORDER BY slot.slot_date, slot.slot_time)
  END AS visitCount,
  CASE WHEN r.status = 'COMPLETED'
       THEN DATEDIFF(slot.slot_date, MAX(...) OVER (...))
  END AS daysSinceLastVisit,
  GROUP_CONCAT(DISTINCT rt.name ...) AS reviewTags
```

### 핵심 원인
- `review_tag_maps` 조인으로 예약 1건이 **태그 개수만큼 복제**된다.
- 윈도우 함수는 **복제된 행 기준으로 계산**되므로 방문 횟수/간격이 왜곡된다.
- `GROUP BY`는 윈도우 계산 결과를 다시 줄일 뿐, 계산 과정의 왜곡은 해결하지 못한다.

## 개선 방향
윈도우 계산을 수행하는 **예약 기본 목록**과 태그 집계를 분리한다.
즉, 아래 순서로 조회한다:

1) 예약 + 슬롯 + 식당 + 리뷰(기본 정보) + 영수증 + 결제 합산  
2) 윈도우 함수(`visitCount`, `daysSinceLastVisit`) 계산  
3) 태그 집계는 별도 서브쿼리에서 `review_id` 기준으로 합쳐서 조인

이렇게 하면 윈도우 계산은 **중복 없는 예약 행** 기준으로 수행된다.

## 개선 예시 (의사 쿼리)
```sql
WITH base AS (
  SELECT
    r.reservation_id,
    r.reservation_code,
    r.user_id,
    res.restaurant_id,
    res.name AS restaurantName,
    CONCAT(res.road_address, ' ', res.detail_address) AS restaurantAddress,
    slot.slot_date,
    slot.slot_time,
    r.party_size,
    r.status,
    rc.confirmed_amount,
    pay.paid_amount,
    r.total_amount,
    rv.review_id,
    rv.rating,
    rv.content,
    rv.created_at
  FROM reservations r
  JOIN restaurant_reservation_slots slot ON slot.slot_id = r.slot_id
  JOIN restaurants res ON res.restaurant_id = slot.restaurant_id
  LEFT JOIN receipts rc ON rc.reservation_id = r.reservation_id
  LEFT JOIN reviews rv ON rv.reservation_id = r.reservation_id
  LEFT JOIN (
    SELECT reservation_id, SUM(amount) AS paid_amount
    FROM payments
    WHERE status = 'PAID'
    GROUP BY reservation_id
  ) pay ON pay.reservation_id = r.reservation_id
  WHERE r.user_id = #{userId}
),
tags AS (
  SELECT
    rtm.review_id,
    GROUP_CONCAT(rt.name ORDER BY rt.tag_id SEPARATOR '||') AS reviewTags
  FROM review_tag_maps rtm
  JOIN review_tags rt ON rt.tag_id = rtm.tag_id
  GROUP BY rtm.review_id
)
SELECT
  base.*,
  CASE
    WHEN base.status = 'COMPLETED' THEN
      SUM(CASE WHEN base.status = 'COMPLETED' THEN 1 ELSE 0 END) OVER (
        PARTITION BY base.user_id, base.restaurant_id
        ORDER BY base.slot_date, base.slot_time
      )
  END AS visitCount,
  CASE
    WHEN base.status = 'COMPLETED' THEN
      DATEDIFF(
        base.slot_date,
        MAX(CASE WHEN base.status = 'COMPLETED' THEN base.slot_date END) OVER (
          PARTITION BY base.user_id, base.restaurant_id
          ORDER BY base.slot_date, base.slot_time
          ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING
        )
      )
  END AS daysSinceLastVisit,
  tags.reviewTags
FROM base
LEFT JOIN tags ON tags.review_id = base.review_id
ORDER BY base.slot_date DESC, base.slot_time DESC;
```

## 비교 분석 요약
| 항목 | 기존 쿼리 | 개선 쿼리 |
| --- | --- | --- |
| 방문 횟수 계산 | 윈도우 함수로 계산 | 스냅샷 테이블(reservation_visit_stats) 조인 |
| 태그 집계 | 동일 쿼리 내 GROUP_CONCAT | 동일 (필요 시 분리 가능) |
| 정확성 | 태그 조인으로 윈도우 결과 왜곡 위험 | 스냅샷 값 사용으로 정확 |
| 성능 | 윈도우/임시테이블 비용 발생 | 윈도우 제거로 비용 감소 |

## 결론
현재 쿼리는 **태그 조인으로 인한 행 복제** 때문에 윈도우 계산이 왜곡될 수 있다.
윈도우 계산을 제거하고 예약 완료 시점에 스냅샷을 저장하면 정확성과 성능을 함께 개선할 수 있다.

## 성능 개선 포인트

- **상관 서브쿼리 제거**: 예약 목록 쿼리 내부에서 반복되는 집계를 파생 테이블로 분리해 1회 집계 후 JOIN.
- **윈도우 함수 제거**: 방문 정보를 스냅샷 테이블에 저장하고 조회는 조인으로 처리.
- **집계 재사용**: 태그 집계는 `review_id` 기준으로 1회 수행 후 JOIN(필요 시 분리).
- **정렬 비용 최소화**: 필요한 정렬 기준(예약일시)만 유지하고, 불필요한 ORDER BY 제거.

## EXPLAIN 비교 템플릿

아래 템플릿에 전/후 실행 계획을 기록한다.

### 성능 비교 방법
- 동일 파라미터(`userId`, `statuses`)로 전/후 쿼리를 실행한다.
- MySQL 8.0이라면 `EXPLAIN ANALYZE`를 사용해 실제 실행 시간을 확인한다.
- 비교 지표는 `rows examined`, `temporary`, `filesort`, `join type`, `execution time` 기준으로 기록한다.

EXPLAIN 요약 비교표

| 항목                   | 전(현재)                               | 후(개선)                               | 개선 포인트                     |
| ---------------------- | -------------------------------------- | -------------------------------------- | ------------------------------ |
| 실행 시간(ms)          | 12.2 (Sort actual time)                | 3.14 (Sort actual time)                | 윈도우 제거 효과               |
| actual time 범위(ms)   | 3.94..12.1 (window max)                | 해당 없음 (윈도우 제거)                | 윈도우 비용 감소               |
| rows examined          | 131 (group 단계 rows)                  | 131 (group 단계 rows)                  | 스냅샷 조인                    |
| rows sent              | 124                                    | 124                                    | 동일                           |
| dependent subquery     | 없음                                   | 없음                                   | -                              |
| temporary table        | 있음 (윈도우 단계 2회)                 | 없음                                   | 스냅샷으로 윈도우 제거         |
| filesort               | 있음                                   | 있음                                   | GROUP_CONCAT 정렬 유지         |
| join type              | Nested Loop                            | Nested Loop                            | 조인 순서 단순화               |
| 주요 병목              | 태그 조인 후 윈도우 계산/집계           | GROUP_CONCAT 정렬 비용                 | 중복 행/윈도우 제거            |

전(현재 쿼리)

| 항목               | 값 |
| ------------------ | --- |
| 실행 시간(ms)      | 12.2 (Sort actual time) |
| rows examined      | 131 (Stream results rows) |
| rows sent          | 124 |
| temporary table    | 있음 (윈도우 단계 2회) |
| filesort           | 있음 |
| join type          | Nested Loop |
| 주요 테이블/인덱스 | reservations(idx_reservation_slot_status), slot(uk_slot), reviews(idx_reviews_reservation_id), review_tag_maps(PRIMARY), review_tags(PRIMARY), receipts(uk_receipts_reservation), payments(idx_payment_status) |

후(개선 쿼리)

| 항목               | 값 |
| ------------------ | --- |
| 실행 시간(ms)      | 3.14 (Sort actual time) |
| rows examined      | 131 (Stream results rows) |
| rows sent          | 124 |
| temporary table    | 없음 |
| filesort           | 있음 |
| join type          | Nested Loop |
| 주요 테이블/인덱스 | reservations(idx_reservation_user), reservation_visit_stats(PRIMARY), slot(PRIMARY), reviews(idx_reviews_reservation_id), review_tag_maps(PRIMARY), review_tags(PRIMARY), receipts(uk_receipts_reservation), payments(idx_payment_reservation) |

### EXPLAIN ANALYZE 실행 예시
```sql
EXPLAIN ANALYZE
SELECT
    r.reservation_id AS reservationId,
    r.reservation_code AS reservationCode,
    res.restaurant_id AS restaurantId,
    res.name AS restaurantName,
    CONCAT(res.road_address, ' ', res.detail_address) AS restaurantAddress,
    slot.slot_date AS slotDate,
    slot.slot_time AS slotTime,
    r.party_size AS partySize,
    r.status AS reservationStatus,
    CASE
        WHEN r.status = 'COMPLETED' THEN
            SUM(CASE WHEN r.status = 'COMPLETED' THEN 1 ELSE 0 END) OVER (
                PARTITION BY r.user_id, res.restaurant_id
                ORDER BY slot.slot_date, slot.slot_time
            )
        ELSE NULL
    END AS visitCount,
    CASE
        WHEN r.status = 'COMPLETED' THEN
            DATEDIFF(
                slot.slot_date,
                MAX(CASE WHEN r.status = 'COMPLETED' THEN slot.slot_date END) OVER (
                    PARTITION BY r.user_id, res.restaurant_id
                    ORDER BY slot.slot_date, slot.slot_time
                    ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING
                )
            )
        ELSE NULL
    END AS daysSinceLastVisit,
    rc.confirmed_amount AS receiptAmount,
    pay.paid_amount AS paidAmount,
    r.total_amount AS totalAmount,
    rv.review_id AS reviewId,
    rv.rating AS reviewRating,
    rv.content AS reviewContent,
    DATE(rv.created_at) AS reviewCreatedAt,
    GROUP_CONCAT(DISTINCT rt.name ORDER BY rt.tag_id SEPARATOR '||') AS reviewTags
FROM reservations r
JOIN restaurant_reservation_slots slot ON slot.slot_id = r.slot_id
JOIN restaurants res ON res.restaurant_id = slot.restaurant_id
LEFT JOIN receipts rc ON rc.reservation_id = r.reservation_id
LEFT JOIN reviews rv ON rv.reservation_id = r.reservation_id
LEFT JOIN review_tag_maps rtm ON rtm.review_id = rv.review_id
LEFT JOIN review_tags rt ON rt.tag_id = rtm.tag_id
LEFT JOIN (
    SELECT reservation_id, SUM(amount) AS paid_amount
    FROM payments
    WHERE status = 'PAID'
    GROUP BY reservation_id
) pay ON pay.reservation_id = r.reservation_id
WHERE r.user_id = 11
  AND r.status IN ('COMPLETED', 'REFUND_PENDING', 'REFUNDED')
GROUP BY
    r.reservation_id,
    r.reservation_code,
    res.restaurant_id,
    res.name,
    res.road_address,
    res.detail_address,
    slot.slot_date,
    slot.slot_time,
    r.party_size,
    r.status,
    rc.confirmed_amount,
    pay.paid_amount,
    r.total_amount,
    rv.review_id,
    rv.rating,
    rv.content,
    rv.created_at
ORDER BY slot.slot_date DESC, slot.slot_time DESC;
```

### 실행 결과(현재 쿼리 요약)
EXPLAIN ANALYZE 출력에서 확인된 주요 포인트:
- `Temporary table`와 `filesort`가 존재하며, 윈도우 함수가 `multi-pass aggregate with buffering`으로 실행됨.
- `review_tag_maps` 조인 이후 `group_concat(distinct ...)`로 집계되며, 그 이전 단계에서 행이 확장됨.
- 실행 흐름상 `GROUP BY` → `윈도우 함수` 순서로 처리되며, 태그 조인으로 인한 행 증식 후 윈도우 계산이 수행됨.

EXPLAIN ANALYZE 원문 (요약):
```
Sort: slot.slot_date DESC, slot.slot_time DESC  (actual time=12.2..12.2 rows=124 loops=1)
  -> Table scan on <temporary>  (cost=2.5..2.5 rows=0) (actual time=12.1..12.1 rows=124 loops=1)
    -> Temporary table  (cost=0..0 rows=0) (actual time=12.1..12.1 rows=124 loops=1)
      -> Window multi-pass aggregate with buffering: max((case when (r.`status` = 'COMPLETED') then slot.slot_date end)) OVER (PARTITION BY r.user_id,res.restaurant_id ORDER BY slot.slot_date,slot.slot_time ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING)   (actual time=3.94..12.1 rows=124 loops=1)
        -> Table scan on <temporary>  (cost=2.5..2.5 rows=0) (actual time=3.93..3.95 rows=124 loops=1)
          -> Temporary table  (cost=0..0 rows=0) (actual time=3.93..3.93 rows=124 loops=1)
            -> Window aggregate with buffering: sum((case when (r.`status` = 'COMPLETED') then 1 else 0 end)) OVER (PARTITION BY r.user_id,res.restaurant_id ORDER BY slot.slot_date,slot.slot_time )   (actual time=3.58..3.89 rows=124 loops=1)
              -> Sort: r.user_id, res.restaurant_id, slot.slot_date, slot.slot_time  (actual time=3.31..3.33 rows=124 loops=1)
                -> Stream results  (actual time=2.93..3.27 rows=124 loops=1)
                  -> Group aggregate: group_concat(distinct review_tags.`name` order by review_tags.tag_id ASC separator '||')  (actual time=2.93..3.14 rows=124 loops=1)
                    -> Sort: r.reservation_id, r.reservation_code, res.restaurant_id, res.`name`, res.road_address, res.detail_address, slot.slot_date, slot.slot_time, r.party_size, r.`status`, rc.confirmed_amount, pay.paid_amount, r.total_amount, rv.review_id, rv.rating, rv.content, rv.created_at  (actual time=2.91..2.94 rows=131 loops=1)
                      -> Stream results  (cost=2.7e+6 rows=21.4e+6) (actual time=0.288..1.71 rows=131 loops=1)
                        -> Nested loop left join  (cost=2.7e+6 rows=21.4e+6) (actual time=0.277..1.48 rows=131 loops=1)
                          -> Nested loop left join  (cost=111204 rows=175501) (actual time=0.0743..1.18 rows=131 loops=1)
                            -> Nested loop left join  (cost=49779 rows=175501) (actual time=0.0708..1.07 rows=131 loops=1)
                              -> Nested loop left join  (cost=10506 rows=86546) (actual time=0.0679..0.911 rows=124 loops=1)
                                -> Nested loop left join  (cost=175 rows=140) (actual time=0.0577..0.626 rows=124 loops=1)
                                  -> Nested loop inner join  (cost=126 rows=140) (actual time=0.0498..0.416 rows=124 loops=1)
                                    -> Nested loop inner join  (cost=77.4 rows=140) (actual time=0.0439..0.376 rows=124 loops=1)
                                      -> Covering index scan on slot using uk_slot  (cost=14.8 rows=145) (actual time=0.0277..0.0409 rows=145 loops=1)
                                      -> Filter: (r.user_id = 11)  (cost=0.309 rows=0.963) (actual time=0.00193..0.00217 rows=0.855 loops=145)
                                        -> Index lookup on r using idx_reservation_slot_status (slot_id=slot.slot_id), with index condition: (r.`status` in ('COMPLETED','REFUND_PENDING','REFUNDED'))  (cost=0.309 rows=1.24) (actual time=0.00179..0.00199 rows=0.862 loops=145)
                                    -> Single-row index lookup on res using PRIMARY (restaurant_id=slot.restaurant_id)  (cost=0.251 rows=1) (actual time=147e-6..174e-6 rows=1 loops=124)
                                  -> Single-row index lookup on rc using uk_receipts_reservation (reservation_id=r.reservation_id)  (cost=0.251 rows=1) (actual time=0.00152..0.00155 rows=0.984 loops=124)
                                -> Index lookup on rv using idx_reviews_reservation_id (reservation_id=r.reservation_id)  (cost=12.4 rows=620) (actual time=0.00192..0.00214 rows=0.984 loops=124)
                              -> Covering index lookup on rtm using PRIMARY (review_id=rv.review_id)  (cost=0.251 rows=2.03) (actual time=798e-6..0.00113 rows=1.04 loops=124)
                            -> Single-row index lookup on rt using PRIMARY (tag_id=rtm.tag_id)  (cost=0.25 rows=1) (actual time=691e-6..718e-6 rows=0.985 loops=131)
                          -> Index lookup on pay using <auto_key0> (reservation_id=r.reservation_id)  (cost=37.1..39.4 rows=10.2) (actual time=0.00192..0.00206 rows=0.954 loops=131)
                            -> Materialize  (cost=36.9..36.9 rows=122) (actual time=0.2..0.2 rows=122 loops=1)
                              -> Group aggregate: sum(payments.amount)  (cost=24.7 rows=122) (actual time=0.0194..0.164 rows=122 loops=1)
                                -> Filter: (payments.`status` = 'PAID')  (cost=12.5 rows=122) (actual time=0.016..0.147 rows=122 loops=1)
                                  -> Index scan on payments using idx_payment_reservation  (cost=12.5 rows=122) (actual time=0.0155..0.135 rows=122 loops=1)
```

### 해석
- 태그 조인으로 인해 행이 늘어난 상태에서 윈도우 함수가 계산되어 `visitCount/daysSinceLastVisit`가 왜곡될 수 있다.
- `Temporary table`이 두 번 생성되며, 윈도우 계산용 정렬 비용이 발생한다.
- 개선 쿼리는 태그 집계를 분리하여 윈도우 계산 대상 행 수를 줄이는 방향이 적합하다.

### 실행 결과(개선 쿼리 요약)
EXPLAIN ANALYZE 출력에서 확인된 주요 포인트:
- `reservation_visit_stats` 조인으로 윈도우 함수를 제거해 `temporary table`이 사라짐.
- `idx_reservation_user` 기반으로 예약을 먼저 좁혀서 조인 범위를 줄임.
- `GROUP_CONCAT` 정렬로 인한 `filesort`는 유지됨.

EXPLAIN ANALYZE 원문 (요약 자리):
```
Sort: slot.slot_date DESC, slot.slot_time DESC  (actual time=3.12..3.14 rows=124 loops=1)
  -> Stream results  (actual time=2.78..3.08 rows=124 loops=1)
    -> Group aggregate: group_concat(distinct review_tags.`name` order by review_tags.tag_id ASC separator '||')  (actual time=2.77..2.96 rows=124 loops=1)
      -> Sort: r.reservation_id, r.reservation_code, res.restaurant_id, res.`name`, res.road_address, res.detail_address, slot.slot_date, slot.slot_time, r.party_size, r.`status`, rvs.visit_number, rvs.days_since_last_visit, rc.confirmed_amount, pay.paid_amount, r.total_amount, rv.review_id, rv.rating, rv.content, rv.created_at  (actual time=2.75..2.78 rows=131 loops=1)
        -> Stream results  (cost=2.23e+6 rows=17.7e+6) (actual time=0.235..1.55 rows=131 loops=1)
          -> Nested loop left join  (cost=2.23e+6 rows=17.7e+6) (actual time=0.226..1.37 rows=131 loops=1)
            -> Nested loop left join  (cost=91735 rows=144840) (actual time=0.0683..1.12 rows=131 loops=1)
              -> Nested loop left join  (cost=41041 rows=144840) (actual time=0.0649..1.02 rows=131 loops=1)
                -> Nested loop left join  (cost=8629 rows=71426) (actual time=0.0597..0.858 rows=124 loops=1)
                  -> Nested loop inner join  (cost=167 rows=110) (actual time=0.05..0.61 rows=124 loops=1)
                    -> Nested loop inner join  (cost=129 rows=110) (actual time=0.0457..0.571 rows=124 loops=1)
                      -> Nested loop left join  (cost=90.2 rows=110) (actual time=0.0425..0.46 rows=124 loops=1)
                        -> Nested loop left join  (cost=51.7 rows=110) (actual time=0.0384..0.332 rows=124 loops=1)
                          -> Filter: (r.`status` in ('COMPLETED','REFUND_PENDING','REFUNDED'))  (cost=13.2 rows=110) (actual time=0.0309..0.14 rows=124 loops=1)
                            -> Index lookup on r using idx_reservation_user (user_id=11)  (cost=13.2 rows=124) (actual time=0.0294..0.112 rows=124 loops=1)
                          -> Single-row index lookup on rc using uk_receipts_reservation (reservation_id=r.reservation_id)  (cost=0.251 rows=1) (actual time=0.00137..0.0014 rows=0.984 loops=124)
                        -> Single-row index lookup on rvs using PRIMARY (reservation_id=r.reservation_id)  (cost=0.251 rows=1) (actual time=860e-6..886e-6 rows=0.984 loops=124)
                      -> Single-row index lookup on slot using PRIMARY (slot_id=r.slot_id)  (cost=0.251 rows=1) (actual time=726e-6..753e-6 rows=1 loops=124)
                    -> Single-row index lookup on res using PRIMARY (restaurant_id=slot.restaurant_id)  (cost=0.251 rows=1) (actual time=139e-6..166e-6 rows=1 loops=124)
                  -> Index lookup on rv using idx_reviews_reservation_id (reservation_id=r.reservation_id)  (cost=12.6 rows=650) (actual time=0.00163..0.00185 rows=0.984 loops=124)
                -> Covering index lookup on rtm using PRIMARY (review_id=rv.review_id)  (cost=0.251 rows=2.03) (actual time=787e-6..0.00113 rows=1.04 loops=124)
              -> Single-row index lookup on rt using PRIMARY (tag_id=rtm.tag_id)  (cost=0.25 rows=1) (actual time=594e-6..621e-6 rows=0.985 loops=131)
            -> Index lookup on pay using <auto_key0> (reservation_id=r.reservation_id)  (cost=37.1..39.4 rows=10.2) (actual time=0.00156..0.00171 rows=0.954 loops=131)
              -> Materialize  (cost=36.9..36.9 rows=122) (actual time=0.154..0.154 rows=122 loops=1)
                -> Group aggregate: sum(payments.amount)  (cost=24.7 rows=122) (actual time=0.019..0.118 rows=122 loops=1)
                  -> Filter: (payments.`status` = 'PAID')  (cost=12.5 rows=122) (actual time=0.0151..0.1 rows=122 loops=1)
                    -> Index scan on payments using idx_payment_reservation  (cost=12.5 rows=122) (actual time=0.0142..0.0875 rows=122 loops=1)
```

### 분석 코멘트(개선 쿼리)
- 윈도우 함수가 제거되면서 `temporary table` 생성 비용이 사라져 전체 실행 시간이 크게 단축되었다.
- `reservation_visit_stats`는 PK 조인이라 비용이 낮고, `idx_reservation_user`로 먼저 필터링되어 드라이빙 테이블 선택이 안정적이다.
- 여전히 `GROUP_CONCAT`가 정렬을 요구하므로 `filesort`는 남아 있다. 태그를 상세 페이지에서만 필요로 한다면 별도 API로 분리하는 것도 고려할 수 있다.
