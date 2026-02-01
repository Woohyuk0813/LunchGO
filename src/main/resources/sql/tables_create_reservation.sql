use lunchgo;


# drop table restaurant_reservation_slots;
CREATE TABLE `restaurant_reservation_slots` (
  `slot_id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '슬롯 PK',
  `restaurant_id` BIGINT NOT NULL COMMENT '식당 ID',
  `slot_date`     DATE   NOT NULL COMMENT '예약 날짜',
  `slot_time`     TIME   NOT NULL COMMENT '예약 시간',
  `max_capacity`  INT    NOT NULL COMMENT '최대 수용 인원',

  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',

  PRIMARY KEY (`slot_id`),
  UNIQUE KEY `uk_slot` (`restaurant_id`, `slot_date`, `slot_time`),
  KEY `idx_slot_lookup` (`restaurant_id`, `slot_date`, `slot_time`),

  CONSTRAINT `fk_slot_restaurant`
    FOREIGN KEY (`restaurant_id`) REFERENCES `restaurants`(`restaurant_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예약 슬롯(락 대상 1행)';





CREATE TABLE `reservations` (
                                `reservation_id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약 PK',
                                `reservation_code`     VARCHAR(32) NOT NULL COMMENT '예약번호(외부노출, 유니크)',

                                `slot_id`              BIGINT NOT NULL COMMENT '슬롯 ID(FK, 락/정원 기준)',
                                `user_id`              BIGINT NOT NULL COMMENT '예약자 ID',

                                `party_size`           INT NOT NULL COMMENT '예약 인원',

                                `reservation_type`     VARCHAR(30) NOT NULL COMMENT '예약 유형(예: RESERVATION_DEPOSIT, PREORDER_PREPAY)',
                                `status`               VARCHAR(20) NOT NULL DEFAULT 'TEMPORARY' COMMENT '예약 상태(TEMPORARY/CONFIRMED/PREPAID_CONFIRMED/EXPIRED/CANCELLED)',

                                `request_message`      VARCHAR(50) NULL COMMENT '요청사항(최대 50자)',

                                `deposit_amount`       INT NULL COMMENT '예약금 스냅샷(원)',
                                `prepay_amount`        INT NULL COMMENT '선결제 합계 스냅샷(원)',
                                `total_amount`         INT NULL COMMENT '총 결제 예정액 스냅샷(원)',
                                `currency`             CHAR(3) NOT NULL DEFAULT 'KRW' COMMENT '통화(KRW)',

                                `hold_expires_at`      DATETIME NULL COMMENT '홀드 만료(now+7분)',
                                `payment_deadline_at`  DATETIME NULL COMMENT '결제 마감(PG요청+10분)',

                                `created_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
                                `updated_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',

                                PRIMARY KEY (`reservation_id`),
                                UNIQUE KEY `uk_reservation_code` (`reservation_code`),

                                KEY `idx_reservation_slot_status` (`slot_id`, `status`),
                                KEY `idx_reservation_user` (`user_id`, `created_at`),
                                KEY `idx_hold_expire` (`status`, `hold_expires_at`),
                                KEY `idx_payment_deadline` (`status`, `payment_deadline_at`),

                                CONSTRAINT `fk_reservation_slot`
                                    FOREIGN KEY (`slot_id`) REFERENCES `restaurant_reservation_slots`(`slot_id`)
                                        ON DELETE RESTRICT,

                                CONSTRAINT `fk_reservation_user`
                                    FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
                                        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예약(슬롯 기반, 중복 컬럼 없음)';

# 한 식당에 대해 동일 날짜, 시간대에 동일한 사용자가 중복 예약하는 것을 방지하기 위한 멱등성 처리 필요
CREATE UNIQUE INDEX uk_user_slot_active
    ON reservations (
        user_id,
        slot_id,
        (IF(status IN ('TEMPORARY', 'CONFIRMED', 'PREPAY_CONFIRM', 'REFUND_PENDING'), 1, NULL)
    ) -- 수식을 직접 작성
);


# drop table reservation_menu_items;
CREATE TABLE `reservation_menu_items` (
                                          `reservation_menu_item_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '예약-메뉴 항목 PK',
                                          `reservation_id`           BIGINT NOT NULL COMMENT '예약 ID(FK)',
                                          `menu_id`                  BIGINT NOT NULL COMMENT '메뉴 ID',

                                          `menu_name`                VARCHAR(100) NOT NULL COMMENT '메뉴명 스냅샷',
                                          `unit_price`               INT NOT NULL COMMENT '단가 스냅샷(원)',
                                          `quantity`                 INT NOT NULL COMMENT '수량',
                                          `line_amount`              INT NOT NULL COMMENT '합계(단가*수량)',

                                          `created_at`               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',

                                          PRIMARY KEY (`reservation_menu_item_id`),
                                          KEY `idx_rmi_reservation` (`reservation_id`),
                                          KEY `idx_rmi_menu` (`menu_id`),

                                          CONSTRAINT `fk_rmi_reservation`
                                              FOREIGN KEY (`reservation_id`) REFERENCES `reservations`(`reservation_id`)
                                                  ON DELETE CASCADE,

                                          CONSTRAINT `fk_rmi_menu`
                                              FOREIGN KEY (`menu_id`) REFERENCES `menus`(`menu_id`)
                                                  ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='선결제 주문 메뉴 항목(스냅샷)';



drop table if exists payments;
CREATE TABLE `payments` (
                            `payment_id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '결제ID(PK)',
                            `reservation_id`  BIGINT NOT NULL COMMENT '예약ID(FK)',

                            `payment_type`    VARCHAR(20) NOT NULL COMMENT '결제유형(DEPOSIT/PREPAID_FOOD)',
                            `status`          VARCHAR(20) NOT NULL DEFAULT 'READY' COMMENT '결제상태(READY/REQUESTED/PAID/FAILED/CANCELLED/REFUNDED/PARTIALLY_REFUNDED)',

                            `method`          VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN' COMMENT '결제수단(CARD/TRANSFER/VIRTUAL_ACCOUNT/UNKNOWN)',
                            `card_type`       VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN' COMMENT '카드구분(PERSONAL/CORPORATE/UNKNOWN)',

                            `amount`          INT NOT NULL COMMENT '결제금액(원)',
                            `currency`        CHAR(3) NOT NULL DEFAULT 'KRW' COMMENT '통화(KRW)',

                            `pg_provider`     VARCHAR(50) NOT NULL COMMENT 'PG사',
                            `pg_payment_key`  VARCHAR(128) NULL COMMENT 'PG결제키',
                            `pg_order_id`     VARCHAR(64)  NULL COMMENT '주문번호(가맹점 주문번호)',
                            `merchant_uid`    VARCHAR(64) NULL COMMENT '가맹점 주문번호(포트원)',
                            `imp_uid`         VARCHAR(64) NULL COMMENT '포트원 결제 고유번호',
                            `pg_tid`          VARCHAR(64) NULL COMMENT 'PG 거래 ID',
                            `receipt_url`     VARCHAR(255) NULL COMMENT '영수증 URL',
                            `idempotency_key` VARCHAR(64) NULL COMMENT '중복 방지 키',

                            `requested_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '결제요청시각',
                            `approved_at`     DATETIME NULL COMMENT '결제승인시각',
                            `failed_at`       DATETIME NULL COMMENT '결제실패시각',
                            `cancelled_at`    DATETIME NULL COMMENT '결제취소시각',

                            `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
                            `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

                            PRIMARY KEY (`payment_id`),
                            KEY `idx_payment_reservation` (`reservation_id`),
                            KEY `idx_payment_status` (`status`, `created_at`),
                            UNIQUE KEY `uk_pg_key` (`pg_provider`, `pg_payment_key`),
                            UNIQUE KEY `uk_payments_merchant_uid` (`merchant_uid`),
                            UNIQUE KEY `uk_payments_imp_uid` (`imp_uid`),
                            UNIQUE KEY `uk_payments_idempotency` (`idempotency_key`),
                            UNIQUE KEY `uk_reservation_payment_type` (`reservation_id`, `payment_type`),

                            CONSTRAINT `fk_payment_reservation`
                                FOREIGN KEY (`reservation_id`) REFERENCES `reservations`(`reservation_id`)
                                    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결제(예약금/선결제)';



drop table if exists refunds;
CREATE TABLE `refunds` (
                           `refund_id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '환불ID(PK)',
                           `payment_id`      BIGINT NOT NULL COMMENT '결제ID(FK)',

                           `requested_by`    VARCHAR(20) NOT NULL COMMENT '환불요청주체(USER/OWNER/ADMIN/SYSTEM)',
                           `policy_case`     VARCHAR(30) NOT NULL COMMENT '정책케이스(DAY_BEFORE/SAME_DAY_BEFORE_2H/WITHIN_2H/NOSHOW/OWNER_FORCED/TIMEOUT/CORPORATE_CARD)',
                           `payment_type`   VARCHAR(20) NOT NULL COMMENT '결제유형 스냅샷(DEPOSIT/PREPAID_FOOD)',
                           `card_type`      VARCHAR(20) NOT NULL COMMENT '카드구분 스냅샷(PERSONAL/CORPORATE/UNKNOWN)',

                           `refund_rate`     DECIMAL(5,4) NOT NULL COMMENT '환불율(1.0000/0.5000/0.2000 등)',

                           `base_amount`     INT NOT NULL COMMENT '기준금액(원)',
                           `refund_amount`   INT NOT NULL COMMENT '환불금액(원)',
                           `penalty_amount`  INT NOT NULL COMMENT '위약금(원)',

                           `status`          VARCHAR(20) NOT NULL DEFAULT 'REQUESTED' COMMENT '환불상태(REQUESTED/SUCCEEDED/FAILED)',

                           `requested_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '환불요청시각',
                           `completed_at`    DATETIME NULL COMMENT '환불완료시각',

                           `pg_refund_key`   VARCHAR(128) NULL COMMENT 'PG환불키',

                           `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

                           PRIMARY KEY (`refund_id`),
                           KEY `idx_refund_payment` (`payment_id`, `requested_at`),

                           CONSTRAINT `fk_refund_payment`
                               FOREIGN KEY (`payment_id`) REFERENCES `payments`(`payment_id`)
                                   ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='환불(정책 계산 스냅샷)';


# drop table reservation_cancellations;
CREATE TABLE `reservation_cancellations` (
                                             `cancellation_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '취소ID(PK)',
                                             `reservation_id`  BIGINT NOT NULL COMMENT '예약ID(FK)',

                                             `cancelled_by`    VARCHAR(20) NOT NULL COMMENT '취소주체(USER/OWNER/ADMIN/SYSTEM)',
                                             `cancelled_by_id` BIGINT NULL COMMENT '취소주체ID(시스템이면 NULL 가능)',
                                             `reason`          VARCHAR(255) NOT NULL COMMENT '취소사유',

                                             `cancelled_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '취소시각',

                                             PRIMARY KEY (`cancellation_id`),
                                             UNIQUE KEY `uk_cancel_reservation` (`reservation_id`),

                                             CONSTRAINT `fk_cancel_reservation`
                                                 FOREIGN KEY (`reservation_id`) REFERENCES `reservations`(`reservation_id`)
                                                     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예약 취소 이력';


ALTER TABLE reservations
    ADD COLUMN visit_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '리마인더 응답 상태(PENDING/CONFIRMED/CANCELLED)' AFTER request_message,
  ADD COLUMN reminder_token VARCHAR(64) NULL COMMENT '리마인더 링크 토큰(유니크)' AFTER visit_status,
  ADD COLUMN reminder_sent_at DATETIME NULL COMMENT '리마인더 발송 시각' AFTER reminder_token,
  ADD COLUMN visit_responded_at DATETIME NULL COMMENT '방문/취소 응답 시각' AFTER reminder_sent_at;

CREATE UNIQUE INDEX uk_reservations_reminder_token ON reservations(reminder_token);
CREATE INDEX idx_reservations_reminder_sent_at ON reservations(reminder_sent_at);
CREATE INDEX idx_reservations_visit_status ON reservations(visit_status);
