-- Sample seed data for owner AI insights signals.
-- Adjust IDs as needed before running.

-- Params (edit values inline)
-- restaurant_id: 96
-- user_id: 1001, 1002, 1003, 1004
-- company_name: LunchGo

-- 1) Users (company info)
INSERT INTO users (user_id, email, password, name, nickname, phone, company_name, company_address, status, role, marketing_agree, email_authentication)
VALUES
  (1001, 'u1001@example.com', 'hashed', 'User One', 'u1', '010-0000-1001', 'LunchGo', 'Seoul', 'ACTIVE', 'USER', 0, 0),
  (1002, 'u1002@example.com', 'hashed', 'User Two', 'u2', '010-0000-1002', 'LunchGo', 'Seoul', 'ACTIVE', 'USER', 0, 0),
  (1003, 'u1003@example.com', 'hashed', 'User Three', 'u3', '010-0000-1003', 'LunchGo', 'Seoul', 'ACTIVE', 'USER', 0, 0),
  (1004, 'u1004@example.com', 'hashed', 'User Four', 'u4', '010-0000-1004', 'LunchGo', 'Seoul', 'ACTIVE', 'USER', 0, 0);

-- 2) Specialities + mappings
INSERT IGNORE INTO specialities (speciality_id, keyword, is_liked) VALUES
  (501, '매운', 1),
  (502, '고기', 1),
  (503, '해산물', 0),
  (504, '채식', 0),
  (505, '치즈', 1),
  (506, '돈가스', 1),
  (507, '카츠', 1);

INSERT IGNORE INTO speciality_mappings (user_id, speciality_id) VALUES
  (1001, 501),
  (1001, 502),
  (1001, 505),
  (1002, 502),
  (1002, 506),
  (1003, 501),
  (1003, 507),
  (1004, 504);

-- 3) Cafeteria menus (this week)
INSERT IGNORE INTO cafeteria_menus (user_id, served_date, main_menu_names, raw_text, image_url) VALUES
  (1001, '2026-01-19', '["매운 돼지불고기","김치찌개"]', '매운 돼지불고기, 김치찌개', NULL),
  (1002, '2026-01-20', '["불고기","비빔밥"]', '불고기, 비빔밥', NULL),
  (1003, '2026-01-21', '["고등어구이","된장찌개","돈가스"]', '고등어구이, 된장찌개, 돈가스', NULL),
  (1004, '2026-01-22', '["채소볶음","두부조림"]', '채소볶음, 두부조림', NULL);

-- 3-1) Extra cafeteria menus for mismatch signal (disliked-focused days)
INSERT IGNORE INTO cafeteria_menus (user_id, served_date, main_menu_names, raw_text, image_url) VALUES
  (1001, '2026-01-20', '["해산물파스타","오징어볶음","치즈카츠"]', '해산물파스타, 오징어볶음, 치즈카츠', NULL),
  (1002, '2026-01-21', '["채식샐러드","두부스테이크"]', '채식샐러드, 두부스테이크', NULL),
  (1003, '2026-01-22', '["해물탕","굴전","로스카츠"]', '해물탕, 굴전, 로스카츠', NULL),
  (1004, '2026-01-23', '["채소비빔밥","버섯볶음"]', '채소비빔밥, 버섯볶음', NULL);

-- 3-2) Menu matches for restaurant keywords (cafeteria)
INSERT INTO cafeteria_menus (user_id, served_date, main_menu_names, raw_text, image_url) VALUES
  (1002, '2026-01-19', '["치즈카츠","미니우동"]', '치즈카츠, 미니우동', NULL),
  (1003, '2026-01-20', '["돈가스김치나베","카레카츠"]', '돈가스김치나베, 카레카츠', NULL);

-- 4) Bookmarks (public)
INSERT INTO bookmarks (user_id, restaurant_id, promotion_agree, is_public)
VALUES
  (1001, 96, 1, 1),
  (1002, 96, 0, 1),
  (1003, 96, 1, 1);

-- 5) Bookmark links (approved)
INSERT INTO bookmark_links (requester_id, receiver_id, status)
VALUES
  (1001, 1002, 'APPROVED'),
  (1001, 1003, 'APPROVED'),
  (1002, 1004, 'APPROVED');

-- 6) Reservation slots + reservations (last 8 weeks)
INSERT IGNORE INTO restaurant_reservation_slots (slot_id, restaurant_id, slot_date, slot_time, max_capacity)
VALUES
  (9001, 96, '2025-11-24', '12:00:00', 20),
  (9002, 96, '2025-12-01', '12:00:00', 20),
  (9003, 96, '2025-12-08', '12:00:00', 20),
  (9004, 96, '2025-12-15', '12:00:00', 20),
  (9005, 96, '2025-12-22', '12:00:00', 20),
  (9006, 96, '2025-12-29', '12:00:00', 20),
  (9007, 96, '2026-01-05', '12:00:00', 20),
  (9008, 96, '2026-01-12', '12:00:00', 20);

INSERT INTO reservations (reservation_id, reservation_code, slot_id, user_id, party_size, reservation_type, status, total_amount, currency)
VALUES
  (8001, 'R-8001', 9001, 1001, 2, 'RESERVATION_DEPOSIT', 'COMPLETED', 20000, 'KRW'),
  (8002, 'R-8002', 9002, 1002, 3, 'RESERVATION_DEPOSIT', 'COMPLETED', 30000, 'KRW'),
  (8003, 'R-8003', 9003, 1003, 2, 'RESERVATION_DEPOSIT', 'COMPLETED', 25000, 'KRW'),
  (8004, 'R-8004', 9004, 1004, 2, 'RESERVATION_DEPOSIT', 'COMPLETED', 22000, 'KRW'),
  (8005, 'R-8005', 9005, 1001, 4, 'RESERVATION_DEPOSIT', 'COMPLETED', 40000, 'KRW'),
  (8006, 'R-8006', 9006, 1002, 2, 'RESERVATION_DEPOSIT', 'COMPLETED', 18000, 'KRW'),
  (8007, 'R-8007', 9613, 1003, 3, 'RESERVATION_DEPOSIT', 'COMPLETED', 33000, 'KRW'),
  (8008, 'R-8008', 9008, 1004, 2, 'RESERVATION_DEPOSIT', 'COMPLETED', 21000, 'KRW');

-- 7) Daily restaurant stats with visits (funnel conversion sample)
INSERT IGNORE INTO daily_restaurant_stats (
  stat_date,
  restaurant_id,
  view_count,
  try_count,
  confirm_count,
  visit_count,
  defended_noshow_cnt,
  penalty_stl_amt,
  revenue_total
)
VALUES
  ('2026-01-11', 96, 6, 4, 2, 1, 0, 0, 12000),
  ('2026-01-12', 96, 9, 7, 5, 3, 0, 0, 81000),
  ('2026-01-13', 96, 4, 3, 1, 1, 0, 0, 30000),
  ('2026-01-14', 96, 7, 8, 7, 4, 0, 0, 56000),
  ('2026-01-15', 96, 5, 5, 4, 2, 0, 0, 200000)
ON DUPLICATE KEY UPDATE
  view_count = VALUES(view_count),
  try_count = VALUES(try_count),
  confirm_count = VALUES(confirm_count),
  visit_count = VALUES(visit_count),
  defended_noshow_cnt = VALUES(defended_noshow_cnt),
  penalty_stl_amt = VALUES(penalty_stl_amt),
  revenue_total = VALUES(revenue_total);



-- 기존 데이터 삭제 (중복 방지)
DELETE FROM weekly_predictions WHERE restaurant_id = 96;

-- 저번 주 월요일 날짜 계산 (현재 주 월요일에서 7일 전)
-- 현재 주 월요일 = DATE(CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY)
-- 저번 주 월요일 = DATE_SUB(DATE(CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY)
SET @last_week_monday = DATE_SUB(DATE(CURDATE() - INTERVAL WEEKDAY(CURDATE()) DAY), INTERVAL 7 DAY);

-- 저번 주 예측 데이터 삽입
-- weekday: 1=일요일, 2=월요일, 3=화요일, 4=수요일, 5=목요일, 6=금요일, 7=토요일
INSERT INTO weekly_predictions (
    restaurant_id,
    week_start_date,
    weekday,
    expected_min,
    expected_max,
    confidence,
    evidence,
    created_at
)
VALUES
    -- 일요일
    (96, @last_week_monday, 1, 2, 5, 'MEDIUM', '["구내식당 미운영으로 인한 외식 수요 증가"]', NOW()),

    -- 월요일
    (96, @last_week_monday, 2, 3, 7, 'HIGH', '["식당 메뉴/취향 매칭 지수 높음", "공유 네트워크 확산"]', NOW()),

    -- 화요일
    (96, @last_week_monday, 3, 2, 6, 'MEDIUM', '["구내식당 메뉴 불일치로 인한 외식 수요 증가"]', NOW()),

    -- 수요일
    (96, @last_week_monday, 4, 4, 8, 'HIGH', '["식당 메뉴/취향 겹침 높음", "공개 북마크 증가"]', NOW()),

    -- 목요일
    (96, @last_week_monday, 5, 3, 7, 'HIGH', '["회사 그룹 방문 패턴", "구내식당 미운영"]', NOW()),

    -- 금요일
    (96, @last_week_monday, 6, 5, 10, 'HIGH', '["주말 전 외식 수요 증가", "공유 링크 승인 증가"]', NOW()),

    -- 토요일
    (96, @last_week_monday, 7, 1, 4, 'LOW', '["주말 외식 수요 감소"]', NOW());

-- 현재 주 월요일 날짜 계산 (1월 10일이 포함된 주)
-- 1월 10일이 포함된 주의 월요일을 계산
-- MySQL의 WEEKDAY() 함수는 0=월요일, 1=화요일, ..., 6=일요일
-- 2026-01-10은 토요일이므로 WEEKDAY('2026-01-10') = 5
-- 따라서 월요일은 1월 10일 - 5일 = 1월 5일
-- 하지만 실제로는 1월 10일이 금요일이어야 하므로, 직접 계산
-- 2026-01-10이 금요일이라면 그 주의 월요일은 2026-01-06
SET @current_week_monday = '2026-01-06';

-- 현재 주 예측 데이터가 이미 있으면 삭제
DELETE FROM weekly_predictions
WHERE restaurant_id = 96
  AND week_start_date = @current_week_monday;

-- 현재 주 예측 데이터 삽입 (1월 10일이 포함된 주)
INSERT INTO weekly_predictions (
    restaurant_id,
    week_start_date,
    weekday,
    expected_min,
    expected_max,
    confidence,
    evidence,
    created_at
)
VALUES
    -- 일요일
    (96, @current_week_monday, 1, 2, 6, 'MEDIUM', '["구내식당 미운영으로 인한 외식 수요 증가"]', NOW()),

    -- 월요일
    (96, @current_week_monday, 2, 4, 8, 'HIGH', '["식당 메뉴/취향 매칭 지수 높음", "공유 네트워크 확산"]', NOW()),

    -- 화요일
    (96, @current_week_monday, 3, 5, 10, 'HIGH', '["구내식당 메뉴 불일치로 인한 외식 수요 증가", "주간 중 외식 수요 증가"]', NOW()),

    -- 수요일
    (96, @current_week_monday, 4, 6, 12, 'HIGH', '["식당 메뉴/취향 겹침 높음", "공개 북마크 증가"]', NOW()),

    -- 목요일
    (96, @current_week_monday, 5, 4, 9, 'HIGH', '["회사 그룹 방문 패턴", "구내식당 미운영"]', NOW()),

    -- 금요일 (1월 10일)
    (96, @current_week_monday, 6, 7, 13, 'HIGH', '["1월 10일 특별 이벤트", "주말 전 외식 수요 증가", "공유 링크 승인 증가"]', NOW()),

    -- 토요일
    (96, @current_week_monday, 7, 1, 5, 'LOW', '["주말 외식 수요 감소"]', NOW());

-- 삽입된 데이터 확인
SELECT
    restaurant_id,
    week_start_date,
    weekday,
    expected_min,
    expected_max,
    confidence
FROM weekly_predictions
WHERE restaurant_id = 96
ORDER BY week_start_date, weekday;


