# 결제 모듈 DB 변경 내역 정리

본 문서는 예약금/선결제 결제 모듈 연동을 위한 DB 변경 사항과 상태 정의, 상태 전이 규칙을 팀 공유용으로 정리한 문서입니다.

## 배경/정책 요약

- 결제 방식은 **예약금** 또는 **선결제** 중 하나만 허용
- 핵심 검증은 서비스 레벨에서 수행
- DB는 유니크 키 및 기본 유효성 위주로 유지

## 적용 파일

- `src/main/resources/sql/tables_create_reservation.sql`
- `src/main/java/com/example/LunchGo/reservation/domain/ReservationStatus.java`
- `docs/db-migration-case-normalization.md`

## 1) 테이블 변경 내역

### 1-1. `reservations` 변경

추가 컬럼:

- `deposit_amount` (예약금 스냅샷, 원)
- `prepay_amount` (선결제 합계 스냅샷, 원)
- `total_amount` (총 결제 예정액 스냅샷, 원)
- `currency` (통화, 기본 KRW)

상태 정의(코멘트 기준):

- `TEMPORARY`
- `CONFIRMED`
- `PREPAID_CONFIRMED`
- `EXPIRED`
- `CANCELLED`

### 1-2. `payments` 변경

추가 컬럼(포트원 연동 최소 셋):

- `merchant_uid` (가맹점 주문번호, 포트원)
- `imp_uid` (포트원 결제 고유번호)
- `pg_tid` (PG 거래 ID)
- `receipt_url` (영수증 URL)
- `idempotency_key` (중복 방지 키)

추가 유니크 키:

- `uk_payments_merchant_uid` (`merchant_uid`)
- `uk_payments_imp_uid` (`imp_uid`)
- `uk_payments_idempotency` (`idempotency_key`)
- `uk_reservation_payment_type` (`reservation_id`, `payment_type`)

### 1-3. `refunds` 변경

추가 컬럼(정책 스냅샷 보강):

- `payment_type` (결제유형 스냅샷, DEPOSIT/PREPAID_FOOD)
- `card_type` (카드구분 스냅샷, PERSONAL/CORPORATE/UNKNOWN)

정책 케이스 보강:

- `policy_case`에 `CORPORATE_CARD` 추가

### 1-4. FK 대소문자 정규화

운영 환경에서 FK 생성 실패를 방지하기 위해 테이블명 대소문자를 소문자로 통일했습니다.

관련 문서:

- `docs/db-migration-case-normalization.md`

## 2) 상태 정의 및 상수

### 2-1. 예약 상태 enum

파일: `src/main/java/com/example/LunchGo/reservation/domain/ReservationStatus.java`

```java
public enum ReservationStatus {
    TEMPORARY,
    CONFIRMED,
    PREPAID_CONFIRMED,
    EXPIRED,
    CANCELLED
}
```

### 2-2. 결제 상태(서비스 레벨 정의)

- `READY`
- `REQUESTED`
- `PAID`
- `FAILED`
- `CANCELLED`
- `REFUNDED`
- `PARTIALLY_REFUNDED`

### 2-3. 결제 타입(서비스 레벨 정의)

- `DEPOSIT`
- `PREPAID_FOOD`

## 3) 서비스 레벨 검증 규칙

### 3-1. 금액 검증

- `DEPOSIT` 결제: `payments.amount == reservations.deposit_amount`
- `PREPAID_FOOD` 결제: `payments.amount == reservations.prepay_amount`
- `reservations.total_amount == deposit_amount + prepay_amount`
- `payments.currency == reservations.currency`

### 3-2. 결제 중복 방지

- `reservation_id + payment_type` 조합은 1회만 허용
- 재시도는 동일 `merchant_uid` 또는 `idempotency_key`로 제한

### 3-3. 상태 전이 기준

- 예약 생성: `TEMPORARY`
- 예약금 결제 성공: `CONFIRMED`
- 선결제 성공: `PREPAID_CONFIRMED`
- 결제 만료: `EXPIRED`
- 취소 완료: `CANCELLED`
- 결제 요청 시작 기록: `READY` → `REQUESTED` (`/api/payments/portone/requested`)

## 4) 상태 전이표

### 4-1. 예약 상태 전이

| 현재 상태 | 이벤트 | 다음 상태 |
| --- | --- | --- |
| TEMPORARY | 예약금 결제 성공 | CONFIRMED |
| TEMPORARY | 선결제 성공 | PREPAID_CONFIRMED |
| CONFIRMED | 선결제 성공 | PREPAID_CONFIRMED |
| TEMPORARY | 결제 만료 | EXPIRED |
| CONFIRMED | 사용자/점주 취소 | CANCELLED |
| PREPAID_CONFIRMED | 사용자/점주 취소 + 환불 성공 | CANCELLED |

### 4-2. 결제 상태 전이

| 현재 상태 | 이벤트 | 다음 상태 |
| --- | --- | --- |
| READY | 결제 요청 | REQUESTED |
| REQUESTED | 승인 성공 | PAID |
| REQUESTED | 승인 실패 | FAILED |
| PAID | 취소 요청 성공 | CANCELLED |
| PAID | 환불 성공 | REFUNDED |
| PAID | 부분 환불 성공 | PARTIALLY_REFUNDED |

## 5) 정책 특이사항

- 예약금 또는 선결제 중 **하나만** 결제하는 정책
- 두 결제가 동시에 발생하지 않도록 서비스에서 검증
- DB에서는 유니크 키와 기본 유효성만 보장

## 6) 마이그레이션 실행 가이드

### refunds 스냅샷 컬럼 추가

```
mysql -u root -p lunchgo < src/main/resources/sql/migration_add_refund_snapshot.sql
```

주의:

- 기존 `refunds` 레코드가 있는 경우, `payment_type`/`card_type` 기본값이 없으므로
  실행 전에 기본값을 채우는 보정 SQL을 추가하거나, 컬럼을 NULL 허용으로 바꾸는 방안을 검토하세요.
