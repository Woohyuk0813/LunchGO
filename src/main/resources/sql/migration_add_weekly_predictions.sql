-- 예측 데이터를 저장할 테이블 생성
CREATE TABLE IF NOT EXISTS weekly_predictions (
    restaurant_id BIGINT NOT NULL,
    week_start_date DATE NOT NULL,
    weekday INT NOT NULL,
    expected_min INT NOT NULL,
    expected_max INT NOT NULL,
    confidence VARCHAR(10) NOT NULL,
    evidence TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (restaurant_id, week_start_date, weekday),
    INDEX idx_restaurant_week (restaurant_id, week_start_date),
    INDEX idx_week_start_date (week_start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

