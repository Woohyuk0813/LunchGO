# 결제 API 플로우 다이어그램

본 문서는 `docs/payment-db-changes.md`를 기준으로 예약금/선결제 결제 흐름을 API 관점에서 정리합니다.

## 전체 흐름 (Mermaid)

```mermaid
flowchart TD
  A[예약 생성 요청] --> B{결제 유형}
  B -->|예약금| C[예약 TEMPORARY 생성<br/>deposit_amount 저장]
  B -->|선결제| D[예약 TEMPORARY 생성<br/>prepay_amount 저장]

  C --> E[결제 요청 생성<br/>payments READY]
  D --> E

  E --> F[결제 요청 시작 기록<br/>payments REQUESTED]
  F --> G[PG 결제 요청]
  G -->|승인 성공| H[결제 승인 처리<br/>imp_uid, pg_tid 저장]
  G -->|승인 실패| I[결제 실패 처리<br/>payments FAILED]

  H --> J{결제 타입}
  J -->|예약금| K[예약 CONFIRMED]
  J -->|선결제| L[예약 PREPAID_CONFIRMED]

  K --> M[예약 완료]
  L --> M

  M --> N{취소/환불 발생?}
  N -->|아니오| O[종료]
  N -->|예| P[환불 요청<br/>refunds REQUESTED]
  P --> Q[환불 승인 처리<br/>refunds SUCCEEDED]
  Q --> R[예약 CANCELLED]
```

## 주요 API 단계 요약

1) 예약 생성
   - 입력: 예약금 또는 선결제 금액
   - 저장: `reservations.deposit_amount` 또는 `reservations.prepay_amount`
   - 상태: `TEMPORARY`

2) 결제 요청 생성
   - `payments` 생성 (`READY`)
   - `merchant_uid`, `idempotency_key` 부여

3) 결제 요청 시작 기록
   - `/api/payments/portone/requested`
   - `payments.status=REQUESTED`, `requested_at` 업데이트

4) PG 결제 요청/응답 처리
   - 승인 시: `/payments/portone/complete` 처리, 웹훅과 상태 동기화
   - `PAID`, `imp_uid`, `pg_tid` 저장
   - 실패 시: `FAILED`

5) 예약 상태 전이
   - 예약금 승인: `CONFIRMED`
   - 선결제 승인: `PREPAID_CONFIRMED`

6) 취소/환불
   - 환불 시: `refunds` 생성 및 상태 업데이트
   - 예약 상태: `CANCELLED`

## 참고

- 결제 타입은 예약금/선결제 중 하나만 허용
- 금액 검증은 서비스 레벨에서 강제
