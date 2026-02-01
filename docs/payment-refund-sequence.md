# 환불/취소 API 시퀀스 다이어그램

환불 정책(예약금/선결제 및 예외)을 기준으로 취소/환불 흐름을 시퀀스 다이어그램으로 정리합니다.

## 환불 시퀀스 (기본 정책)

```mermaid
sequenceDiagram
  participant U as User
  participant A as API Server
  participant P as PortOne
  participant D as DB

  U->>A: 예약 취소 요청
  A->>D: reservations 조회 + 정책 판정
  A->>D: refunds 생성(REQUESTED, base_amount, refund_rate)
  A->>D: reservation_cancellations 생성
  A->>P: 환불 요청(imp_uid, refund_amount)
  P-->>A: 환불 결과 콜백
  A->>D: refunds 업데이트(SUCCEEDED/FAILED)
  A->>D: reservations 상태 변경(CANCELLED)
  A-->>U: 취소/환불 결과 응답
```

## 예외 시퀀스 (법인카드/점주취소)

```mermaid
sequenceDiagram
  participant U as User
  participant O as Owner
  participant A as API Server
  participant P as PortOne
  participant D as DB

  O->>A: 예약 취소 요청(점주)
  A->>D: 결제/카드 타입 확인(card_type)
  A->>D: 예외 정책 판정(위약금 면제)
  A->>D: refunds 생성(REQUESTED, refund_rate=1.0)
  A->>D: reservation_cancellations 생성(cancelled_by=OWNER)
  A->>P: 환불 요청(전액)
  P-->>A: 환불 결과 콜백
  A->>D: refunds 업데이트(SUCCEEDED/FAILED)
  A->>D: reservations 상태 변경(CANCELLED)
  A-->>O: 취소/환불 결과 응답
```

## 참고

- 예약금 환불: `예약금 × 환불비율`
- 선결제 환불: `(메뉴금액 - 예약금 가정액) × 환불비율`
- 예외 정책: 법인카드 및 점주취소는 위약금 면제(전액 환불)
