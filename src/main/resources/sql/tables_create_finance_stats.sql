use lunchgo;
# 01. 정산 테이블 (플랫폼이 식당에게 돈을 주는 데이터)
DROP TABLE IF EXISTS settlements;
CREATE TABLE settlements (
    settlement_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    restaurant_id	bigint	NOT NULL,
    type	varchar(20)	NOT NULL	DEFAULT 'PRE_ORDER' COMMENT 'PRE_ORDER or PENALTY(위약금)',
	payout_date	datetime	NULL	COMMENT '실제 입금완료일',
	payment_amt	int	NULL	COMMENT '고객 결제액(포인트활용 후)',
	pg_fee	int	NULL	COMMENT '3.3% PG사 수수료',
	platform_fee	int	NOT NULL COMMENT '3% 플랫폼 수익',
	vat_on_fee	int	NOT NULL	COMMENT '플랫폼 수익 부가세',
	stl_amount	int	NOT NULL	COMMENT '식당 실입금액',
	status	varchar(20)	NOT NULL	DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED, PROCESSING, COMPLETED, FAILED, ON_HOLD',
	scheduled_date	date	NOT NULL,
	created_at	datetime	NOT NULL default current_timestamp,
    updated_at datetime NOT NULL default current_timestamp
);

# 02. 청구서 테이블 (플랫폼이 식당에게 받아야 하는 돈에 관한 데이터)
DROP TABLE IF EXISTS invoices;
CREATE TABLE invoices (
    invoice_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    restaurant_id	bigint	NOT NULL,
    type	varchar(20)	NOT NULL	DEFAULT 'MATCHING' COMMENT 'MATCHING or SUBSCRIPTION',
    status	VARCHAR(20)	NOT NULL	DEFAULT 'ISSUED' COMMENT 'ISSUED, PAID, OVERDUE, CANCELLED (시스템 오류|프로모션), PARTIAL',
    supply_amount	int	NOT NULL COMMENT '공급가액',
    vat_amount	int	NOT NULL	COMMENT '공급가액*0.1(소수점은 내림처리)',
    total_amount	int	NOT NULL	COMMENT '공급가액+부가세',
    billing_ym	varchar(7)	NOT NULL	COMMENT '2024-06',
    due_date	date	NOT NULL,
    latest_alert_sent	date	NULL	COMMENT '독촉알림 날짜',
    paid_at	datetime	NULL,
    created_at	datetime	NOT NULL	DEFAULT CURRENT_TIMESTAMP,
    updated_at	datetime	NULL	DEFAULT CURRENT_TIMESTAMP
);

# 03. 청구서 납부 내역
CREATE TABLE invoice_payments (
    pay_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    invoice_id	bigint	NOT NULL,
    amount	int	NOT NULL,
    paid_at	datetime	NOT NULL,
    method	varchar(20)	NOT NULL	DEFAULT 'CREDIT_CARD' COMMENT 'CREDIT_CART, ACCOUNT_TRANSFER',
    status	varchar(20)	NULL	DEFAULT 'READY' COMMENT 'READY, PAID, FAILED',
    transaction_key	varchar(100)	NULL COMMENT 'PG거래 고유번호'
);

#### 비정규화 테이블

# 식당 테이블 조인 없이 연체 식당 정보 바로 제공하기 위해,
# 청구서 생성 시점에 식당 정보(이름, 연락처)를 별도 제공
# 04. 청구현황 스냅샷 테이블
DROP TABLE IF EXISTS invoice_snapshot;
CREATE TABLE invoice_snapshot (
invoice_id	bigint	NOT NULL PRIMARY KEY ,
restaurant_id	bigint	NULL,
restaurant_name	varchar(50)	NULL,
owner_phone	varchar(20)	NULL	COMMENT '조인 제거용 - 바로 문자 발송',
total_amount	int	NULL,
due_date	date	NULL,
overdue_days	int	NULL,
status	varchar(20)	NULL COMMENT 'ISSUED, PAID, OVERDUE, CANCELLED (시스템 오류|프로모션), PARTIAL'
);


# 05. 일별 식당 통계 테이블
DROP TABLE IF EXISTS daily_restaurant_stats;
CREATE TABLE daily_restaurant_stats (
    stat_date	date	NOT NULL,
    restaurant_id	bigint	NOT NULL,
    view_count	int	NULL,
    try_count	int	NULL,
    confirm_count	int	NULL,
    visit_count	int	NULL,
    defended_noshow_cnt	int	NULL,
    penalty_stl_amt	int	NULL,
    revenue_total	bigint	NULL
);

# 06. 식당별 단골 요약 테이블
CREATE TABLE restaurant_user_stats (
    restaurant_id	bigint	NOT NULL,
    user_id	bigint	NOT NULL,
    visit_cnt	int	NULL COMMENT '누적방문횟수',
    total_spend_amt	int	NULL	COMMENT '객단가 산출용',
    last_visit_date	date	NULL
);


# 07. 일별 자금 통계 테이블
DROP TABLE IF EXISTS daily_global_finance;
CREATE TABLE daily_global_finance (
    stat_date	date	NOT NULL PRIMARY KEY ,
    revenue_subscription	bigint	NULL,
    revenue_matching_fee	bigint	NULL,
    revenue_preorder_fee	bigint	NULL,
    revenue_penalty	bigint	NULL,
    revenue_total	bigint	NULL,
    float_deposit_total	bigint	NULL	COMMENT '당일 마감 기준',
    receivable_total	bigint	NULL	COMMENT '당일 마감 기준(연체포함)',
    payment_success_cnt	int	NULL,
    payment_fail_cnt	int	NULL
);









