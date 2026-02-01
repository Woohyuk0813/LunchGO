-- EXPLAIN ANALYZE (current query)
-- userId/status 값을 테스트 환경에 맞게 변경해서 실행한다.
-- 예시: userId=11, status IN ('COMPLETED','REFUND_PENDING','REFUNDED')
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

-- EXPLAIN ANALYZE (optimized query)
-- 동일한 userId/status 조건으로 전/후 비교한다.
-- 예시: userId=11, status IN ('COMPLETED','REFUND_PENDING','REFUNDED')
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
        WHEN r.status = 'COMPLETED' THEN rvs.visit_number
        ELSE NULL
    END AS visitCount,
    CASE
        WHEN r.status = 'COMPLETED' THEN rvs.days_since_last_visit
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
LEFT JOIN reservation_visit_stats rvs ON rvs.reservation_id = r.reservation_id
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
    rvs.visit_number,
    rvs.days_since_last_visit,
    rc.confirmed_amount,
    pay.paid_amount,
    r.total_amount,
    rv.review_id,
    rv.rating,
    rv.content,
    rv.created_at
ORDER BY slot.slot_date DESC, slot.slot_time DESC;
