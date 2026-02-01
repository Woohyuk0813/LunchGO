-- Seed data for reservation history performance comparison
-- 목적: 예약/리뷰/태그 데이터를 늘려 EXPLAIN ANALYZE 비교에 활용한다.

USE lunchgo;

SET @target_user = 11;
SET @target_restaurant = 1;
SET @start_date = '2024-01-01';
SET @days = 120;
SET @party_size = 2;
SET @status = 'COMPLETED';

-- 1) 예약 슬롯 생성 (일자별 1개)
INSERT IGNORE INTO restaurant_reservation_slots (restaurant_id, slot_date, slot_time, max_capacity)
SELECT
    @target_restaurant,
    DATE_ADD(@start_date, INTERVAL seq.n DAY),
    '12:00:00',
    30
FROM (
    SELECT (a.n + b.n * 10 + c.n * 100) AS n
    FROM (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) a
    CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) b
    CROSS JOIN (
        SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
        UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) c
    WHERE (a.n + b.n * 10 + c.n * 100) < @days
) seq;

-- 2) 예약 생성 (슬롯 기준, reservation_id는 AUTO_INCREMENT)
INSERT INTO reservations
    (reservation_code, slot_id, user_id, party_size, reservation_type, status, request_message,
     deposit_amount, prepay_amount, total_amount, currency, hold_expires_at, payment_deadline_at, created_at, updated_at)
SELECT
    CONCAT('RPERF', DATE_FORMAT(slot.slot_date, '%Y%m%d'), '-', LPAD(slot.slot_id, 5, '0')),
    slot.slot_id,
    @target_user,
    @party_size,
    'RESERVATION_DEPOSIT',
    @status,
    NULL,
    10000,
    NULL,
    50000,
    'KRW',
    NULL,
    NULL,
    CONCAT(slot.slot_date, ' 11:00:00'),
    CONCAT(slot.slot_date, ' 11:00:00')
FROM restaurant_reservation_slots slot
LEFT JOIN reservations r
    ON r.slot_id = slot.slot_id
   AND r.user_id = @target_user
WHERE slot.restaurant_id = @target_restaurant
  AND slot.slot_date BETWEEN @start_date AND DATE_ADD(@start_date, INTERVAL @days - 1 DAY)
  AND r.reservation_id IS NULL;

-- 3) 리뷰 생성 (예약 대비 1:1, 기존 리뷰는 제외)
INSERT INTO reviews
    (restaurant_id, user_id, reservation_id, receipt_id, rating, content, created_at, updated_at, status)
SELECT
    @target_restaurant,
    @target_user,
    r.reservation_id,
    NULL,
    4,
    CONCAT('성능 테스트 리뷰 ', r.reservation_id),
    r.created_at,
    r.updated_at,
    'PUBLIC'
FROM reservations r
LEFT JOIN reviews rv ON rv.reservation_id = r.reservation_id
WHERE r.user_id = @target_user
  AND r.status = 'COMPLETED'
  AND rv.review_id IS NULL;

-- 4) 리뷰 태그 매핑 (리뷰당 2개 태그 부여)
INSERT INTO review_tag_maps (review_id, tag_id)
SELECT rv.review_id, 1 + (rv.review_id % 5)
FROM reviews rv
LEFT JOIN review_tag_maps m ON m.review_id = rv.review_id
WHERE rv.user_id = @target_user
  AND m.review_id IS NULL;

INSERT INTO review_tag_maps (review_id, tag_id)
SELECT rv.review_id, 6 + (rv.review_id % 5)
FROM reviews rv
LEFT JOIN review_tag_maps m ON m.review_id = rv.review_id
WHERE rv.user_id = @target_user
  AND m.review_id IS NULL;

-- 5) 영수증 생성 (예약당 1건, 기존 영수증은 제외)
INSERT INTO receipts
    (reservation_id, confirmed_amount, image_url, created_at)
SELECT
    r.reservation_id,
    r.total_amount,
    NULL,
    r.created_at
FROM reservations r
LEFT JOIN receipts rc ON rc.reservation_id = r.reservation_id
WHERE r.user_id = @target_user
  AND r.status = 'COMPLETED'
  AND rc.receipt_id IS NULL;

-- 6) 결제 데이터 (예약당 1건, 기존 결제는 제외)
INSERT INTO payments
(reservation_id, payment_type, status, method, card_type, amount, currency, pg_provider, requested_at, created_at, updated_at)
SELECT
    r.reservation_id,
    'DEPOSIT',
    'PAID',
    'CARD',
    'CORPORATE',
    r.deposit_amount,
    'KRW',
    'PORTONE',
    r.created_at,
    r.created_at,
    r.updated_at
FROM reservations r
         LEFT JOIN payments p
                   ON p.reservation_id = r.reservation_id
                       AND p.payment_type = 'DEPOSIT'
WHERE r.user_id = @target_user
  AND r.status = 'COMPLETED'
  AND r.deposit_amount IS NOT NULL
  AND p.payment_id IS NULL;