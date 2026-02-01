USE lunchgo;

CREATE TABLE IF NOT EXISTS reservation_visit_stats (
    reservation_id BIGINT NOT NULL COMMENT '예약 ID (PK)',
    user_id BIGINT NOT NULL COMMENT '유저 ID',
    restaurant_id BIGINT NOT NULL COMMENT '식당 ID',
    visit_number INT NOT NULL COMMENT '해당 예약의 n번째 방문',
    prev_visit_date DATE NULL COMMENT '직전 방문일',
    days_since_last_visit INT NULL COMMENT '직전 방문과의 일수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '스냅샷 생성 시각',
    PRIMARY KEY (reservation_id),
    KEY idx_rvs_user_restaurant (user_id, restaurant_id),
    CONSTRAINT fk_rvs_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예약별 방문 스냅샷';

-- ----------------------------------------------------------------------
-- Backfill: 기존 COMPLETED 예약에 방문 스냅샷 생성
-- ----------------------------------------------------------------------
INSERT INTO reservation_visit_stats (
    reservation_id,
    user_id,
    restaurant_id,
    visit_number,
    prev_visit_date,
    days_since_last_visit
)
SELECT
    c.reservation_id,
    c.user_id,
    c.restaurant_id,
    c.visit_number,
    c.prev_visit_date,
    CASE
        WHEN c.prev_visit_date IS NULL THEN NULL
        ELSE DATEDIFF(c.slot_date, c.prev_visit_date)
    END AS days_since_last_visit
FROM (
    SELECT
        r.reservation_id,
        r.user_id,
        slot.restaurant_id,
        slot.slot_date,
        slot.slot_time,
        ROW_NUMBER() OVER (
            PARTITION BY r.user_id, slot.restaurant_id
            ORDER BY slot.slot_date, slot.slot_time
        ) AS visit_number,
        LAG(slot.slot_date) OVER (
            PARTITION BY r.user_id, slot.restaurant_id
            ORDER BY slot.slot_date, slot.slot_time
        ) AS prev_visit_date
    FROM reservations r
    JOIN restaurant_reservation_slots slot ON slot.slot_id = r.slot_id
    WHERE r.status = 'COMPLETED'
) c
WHERE NOT EXISTS (
    SELECT 1
    FROM reservation_visit_stats rvs
    WHERE rvs.reservation_id = c.reservation_id
);

-- (선택) restaurant_user_stats를 COMPLETED 기준으로 맞춘다.
INSERT INTO restaurant_user_stats (
    restaurant_id,
    user_id,
    visit_cnt,
    last_visit_date
)
SELECT
    slot.restaurant_id,
    r.user_id,
    COUNT(*) AS visit_cnt,
    MAX(slot.slot_date) AS last_visit_date
FROM reservations r
JOIN restaurant_reservation_slots slot ON slot.slot_id = r.slot_id
WHERE r.status = 'COMPLETED'
GROUP BY slot.restaurant_id, r.user_id
ON DUPLICATE KEY UPDATE
    visit_cnt = VALUES(visit_cnt),
    last_visit_date = VALUES(last_visit_date);
