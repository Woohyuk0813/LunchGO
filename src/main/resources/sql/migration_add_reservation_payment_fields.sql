USE lunchgo;

ALTER TABLE reservations
  ADD COLUMN deposit_amount INT NULL COMMENT '예약금 스냅샷(원)' AFTER request_message,
  ADD COLUMN prepay_amount INT NULL COMMENT '선결제 합계 스냅샷(원)' AFTER deposit_amount,
  ADD COLUMN total_amount INT NULL COMMENT '총 결제 예정액 스냅샷(원)' AFTER prepay_amount,
  ADD COLUMN currency CHAR(3) NOT NULL DEFAULT 'KRW' COMMENT '통화(KRW)' AFTER total_amount;

-- 테스트용: 예약 ID 1, 7에 예약금 세팅
UPDATE reservations
SET deposit_amount = 20000,
    total_amount = 20000,
    currency = 'KRW'
WHERE reservation_id = 1;

UPDATE reservations
SET deposit_amount = 20000,
    total_amount = 20000,
    currency = 'KRW'
WHERE reservation_id = 7;
