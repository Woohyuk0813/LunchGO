/* ==================================================
   1. 정산 데이터 (Settlements)
   - 플랫폼이 식당에게 지급해야 할 돈
   - 계산 로직: 결제액 - PG수수료(3.3%) - 플랫폼수수료(3.0%) - 부가세(수수료의 10%) = 실입금액
   ================================================== */

-- Case 1: 정산 완료된 건 (숙성도 강남점 - 선결제 예약금 정산)
-- 고객결제: 100,000원
-- PG수수료: 3,300원
-- 플랫폼수익: 3,000원
-- 부가세: 300원
-- 실지급액: 93,400원
INSERT INTO settlements (settlement_id, restaurant_id, type, payout_date, payment_amt, pg_fee, platform_fee, vat_on_fee, stl_amount, status, scheduled_date, created_at) VALUES
    (1, 1, 'PRE_ORDER', DATE_SUB(CURDATE(), INTERVAL 1 DAY), 100000, 3300, 3000, 300, 93400, 'COMPLETED', DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- Case 2: 정산 예정 건 (바다향기 횟집 - 노쇼 위약금 정산)
-- 고객결제: 20,000원 (위약금)
-- PG수수료: 660원
-- 플랫폼수익: 600원
-- 부가세: 60원
-- 실지급액: 18,680원
INSERT INTO settlements (settlement_id, restaurant_id, type, payout_date, payment_amt, pg_fee, platform_fee, vat_on_fee, stl_amount, status, scheduled_date, created_at) VALUES
    (2, 2, 'PENALTY', NULL, 20000, 660, 600, 60, 18680, 'SCHEDULED', DATE_ADD(CURDATE(), INTERVAL 1 DAY), NOW());


/* ==================================================
   2. 청구서 데이터 (Invoices)
   - 플랫폼이 식당에게 받아야 할 돈 (구독료, 매칭 수수료 등)
   ================================================== */

-- Case 1: 납부 완료된 구독료 청구서 (숙성도 강남점)
-- 공급가: 30,000원, 부가세: 3,000원, 합계: 33,000원
INSERT INTO invoices (invoice_id, restaurant_id, type, status, supply_amount, vat_amount, total_amount, billing_ym, due_date, latest_alert_sent, paid_at, created_at) VALUES
    (1, 1, 'SUBSCRIPTION', 'PAID', 30000, 3000, 33000, DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH), '%Y-%m'), DATE_SUB(CURDATE(), INTERVAL 5 DAY), NULL, DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY));

-- Case 2: 연체된 매칭 수수료 청구서 (바다향기 횟집)
-- 공급가: 50,000원, 부가세: 5,000원, 합계: 55,000원
INSERT INTO invoices (invoice_id, restaurant_id, type, status, supply_amount, vat_amount, total_amount, billing_ym, due_date, latest_alert_sent, paid_at, created_at) VALUES
    (2, 2, 'MATCHING', 'OVERDUE', 50000, 5000, 55000, DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH), '%Y-%m'), DATE_SUB(CURDATE(), INTERVAL 1 DAY), CURDATE(), NULL, DATE_SUB(NOW(), INTERVAL 7 DAY));


/* ==================================================
   3. 청구서 납부 내역 (Invoice Payments)
   - Case 1(invoice_id=1)에 대한 실제 납부 로그
   ================================================== */
INSERT INTO invoice_payments (pay_id, invoice_id, amount, paid_at, method, status, transaction_key) VALUES
    (1, 1, 33000, DATE_SUB(NOW(), INTERVAL 6 DAY), 'CREDIT_CARD', 'PAID', 'imp_1234567890');


/* ==================================================
   4. 청구현황 스냅샷 (Invoice Snapshot)
   - Invoices 테이블과 데이터가 동기화되어 있어야 함
   - JOIN 성능 비용을 줄이기 위해 식당 정보를 포함
   ================================================== */

-- Snapshot 1: 납부 완료된 건
INSERT INTO invoice_snapshot (invoice_id, restaurant_id, restaurant_name, owner_phone, total_amount, due_date, overdue_days, status) VALUES
    (1, 1, '숙성도 강남점', '010-1111-1111', 33000, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 0, 'PAID');

-- Snapshot 2: 연체된 건 (Overdue calculation logic test)
INSERT INTO invoice_snapshot (invoice_id, restaurant_id, restaurant_name, owner_phone, total_amount, due_date, overdue_days, status) VALUES
    (2, 2, '바다향기 횟집', '010-1111-1111', 55000, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 'OVERDUE');


/* ==================================================
   5. 일별 식당 통계 (Daily Restaurant Stats)
   - 주의: 현재 PK가 stat_date 단일 컬럼이라 하루에 한 식당만 입력 가능
   - 테스트를 위해 날짜를 다르게 설정함
   ================================================== */

-- 어제 날짜 통계 (숙성도)
INSERT INTO daily_restaurant_stats (stat_date, restaurant_id, view_count, try_count, confirm_count, visit_count, defended_noshow_cnt, penalty_stl_amt, revenue_total) VALUES
    (DATE_SUB(CURDATE(), INTERVAL 1 DAY), 1, 150, 20, 18, 15, 0, 0, 1500000);

-- 오늘 날짜 통계 (바다향기)
INSERT INTO daily_restaurant_stats (stat_date, restaurant_id, view_count, try_count, confirm_count, visit_count, defended_noshow_cnt, penalty_stl_amt, revenue_total) VALUES
    (CURDATE(), 2, 80, 5, 2, 0, 1, 20000, 0);

-- 트렌딩 테스트용 추가 데이터 (최근 7일, 식당 1~5)
INSERT INTO daily_restaurant_stats (stat_date, restaurant_id, view_count, try_count, confirm_count, visit_count, defended_noshow_cnt, penalty_stl_amt, revenue_total) VALUES
    (DATE_SUB(CURDATE(), INTERVAL 2 DAY), 1, 120, 18, 14, 12, 0, 0, 1200000),
    (DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1, 90, 12, 10, 9, 0, 0, 900000),
    (DATE_SUB(CURDATE(), INTERVAL 4 DAY), 1, 70, 10, 8, 7, 0, 0, 700000),
    (DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, 60, 8, 6, 5, 0, 0, 600000),
    (DATE_SUB(CURDATE(), INTERVAL 6 DAY), 1, 50, 6, 5, 4, 0, 0, 500000),
    (DATE_SUB(CURDATE(), INTERVAL 2 DAY), 2, 95, 7, 4, 3, 1, 10000, 300000),
    (DATE_SUB(CURDATE(), INTERVAL 3 DAY), 2, 85, 6, 3, 2, 0, 0, 200000),
    (DATE_SUB(CURDATE(), INTERVAL 4 DAY), 2, 75, 5, 2, 1, 0, 0, 150000),
    (DATE_SUB(CURDATE(), INTERVAL 5 DAY), 2, 65, 4, 2, 1, 0, 0, 120000),
    (DATE_SUB(CURDATE(), INTERVAL 6 DAY), 2, 55, 3, 1, 1, 0, 0, 90000),
    (DATE_SUB(CURDATE(), INTERVAL 1 DAY), 3, 140, 22, 20, 18, 0, 0, 1600000),
    (DATE_SUB(CURDATE(), INTERVAL 2 DAY), 3, 130, 20, 18, 16, 0, 0, 1500000),
    (DATE_SUB(CURDATE(), INTERVAL 3 DAY), 3, 110, 18, 15, 13, 0, 0, 1300000),
    (DATE_SUB(CURDATE(), INTERVAL 4 DAY), 3, 100, 16, 13, 11, 0, 0, 1200000),
    (DATE_SUB(CURDATE(), INTERVAL 5 DAY), 3, 90, 14, 12, 10, 0, 0, 1100000),
    (DATE_SUB(CURDATE(), INTERVAL 2 DAY), 4, 200, 6, 3, 2, 0, 0, 400000),
    (DATE_SUB(CURDATE(), INTERVAL 3 DAY), 4, 180, 5, 2, 1, 0, 0, 300000),
    (DATE_SUB(CURDATE(), INTERVAL 4 DAY), 4, 160, 4, 1, 1, 0, 0, 200000),
    (DATE_SUB(CURDATE(), INTERVAL 5 DAY), 4, 140, 3, 1, 0, 0, 0, 100000),
    (DATE_SUB(CURDATE(), INTERVAL 6 DAY), 4, 120, 2, 0, 0, 0, 0, 50000),
    (DATE_SUB(CURDATE(), INTERVAL 2 DAY), 5, 60, 10, 7, 6, 0, 0, 800000),
    (DATE_SUB(CURDATE(), INTERVAL 3 DAY), 5, 55, 9, 6, 5, 0, 0, 700000),
    (DATE_SUB(CURDATE(), INTERVAL 4 DAY), 5, 50, 8, 6, 5, 0, 0, 650000),
    (DATE_SUB(CURDATE(), INTERVAL 5 DAY), 5, 45, 7, 5, 4, 0, 0, 600000),
    (DATE_SUB(CURDATE(), INTERVAL 6 DAY), 5, 40, 6, 4, 3, 0, 0, 500000);


/* ==================================================
   6. 식당별 단골 요약 (Restaurant User Stats)
   - CRM 마케팅 등을 위한 데이터
   ================================================== */

-- 숙성도 강남점(1)의 단골: 이대리(2)
INSERT INTO restaurant_user_stats (restaurant_id, user_id, visit_cnt, total_spend_amt, last_visit_date) VALUES
    (1, 2, 5, 250000, DATE_SUB(CURDATE(), INTERVAL 7 DAY));

-- 숙성도 강남점(1)의 방문객: 박과장(3)
INSERT INTO restaurant_user_stats (restaurant_id, user_id, visit_cnt, total_spend_amt, last_visit_date) VALUES
    (1, 3, 1, 50000, DATE_SUB(CURDATE(), INTERVAL 1 MONTH));


/* ==================================================
   7. 일별 자금 통계 (Daily Global Finance)
   - 관리자 대시보드용 전체 합산 데이터
   ================================================== */
INSERT INTO daily_global_finance (stat_date, revenue_subscription, revenue_matching_fee, revenue_preorder_fee, revenue_penalty, revenue_total, float_deposit_total, receivable_total, payment_success_cnt, payment_fail_cnt) VALUES
    (DATE_SUB(CURDATE(), INTERVAL 1 DAY), 300000, 500000, 150000, 20000, 970000, 5000000, 55000, 10, 1);
