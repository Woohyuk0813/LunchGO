# 예약 환불 구현 문서

## 작업 목표
- 예약 취소/환불 API의 흐름과 환불 정책을 정리
- 사용자 취소와 점주 취소의 차이를 명확히 구분

## 관련 API

### 사용자
- 예약 취소: `POST /api/reservations/{reservationId}/cancel`
  - 요청: `CancelReservationRequest` (reason/detail/refund 포함 가능)
  - 처리: `ReservationRefundService.cancelWithRefundPolicy`

### 점주
- 예약 강제 취소: `POST /api/business/reservations/{reservationId}/cancel`
  - 요청: `BusinessCancelReservationRequest`
  - 처리: `BusinessReservationCancelService.cancel`

## 취소/환불 처리 흐름

### 1) 사용자 취소
- 컨트롤러: `ReservationController#cancel`
- 서비스: `ReservationRefundService.cancelWithRefundPolicy`
  - 예약 상태 검증 (이미 취소/만료/완료인 경우 예외)
  - 결제 내역 조회
    - `PREPAID_FOOD` 결제
    - `DEPOSIT` 결제
  - 결제가 없으면 `CANCELLED` 처리 후 종료
  - 결제가 있으면 `REFUND_PENDING`으로 변경 후 환불 계산/호출
  - 환불 결과에 따라 `REFUNDED` 또는 `CANCELLED`로 최종 반영
  - 트랜잭션 커밋 후 Redis 좌석 카운터 복구

### 2) 점주 취소
- 컨트롤러: `BusinessReservationController#cancel`
- 서비스: `BusinessReservationCancelService#cancel`
  - 소유 식당 여부 검증
  - 취소 가능 상태(TEMPORARY/CONFIRMED/PREPAID_CONFIRMED) 확인
  - `reservation_cancellations` 레코드 저장(OWNER, 사유 포함)
  - `ReservationRefundService.cancelByOwner` 호출
  - 사용자에게 취소 SMS 발송 이벤트 발생

## 환불 정책

### 공통 원칙
- `ReservationRefundService`에서 최종 환불 금액을 계산
- `PortoneCancelService.cancelPayment`로 환불 호출
- 환불 금액이 0이면 환불 호출/상태 변경 없이 종료

### 예약금(Deposit) 환불
- 기본: 예약금 * 환불율
- 예외 1) 점주 취소: 100% 환불
- 예외 2) 회원 취소 + 법인카드: 100% 환불

### 선결제(Prepaid) 환불
- 메뉴 금액: 100% 환불
- 예약금 가정액: 환불율 적용
- 예외 1) 점주 취소: 100% 환불
- 예외 2) 회원 취소 + 법인카드: 100% 환불

### 환불율 기준(예약 시간 기준)
- 전일 취소: 100%
- 당일 2시간 전까지: 50%
- 2시간 이내: 20% (8인 이상은 10%)
- 노쇼/예약 시간 이후: 0%

## 상태 전이
- 결제 없음: `CANCELLED`
- 결제 있음:
  - `REFUND_PENDING` -> 환불 성공 시 `REFUNDED`
  - 환불 금액 0 또는 환불 미발생: `CANCELLED`

## Redis 좌석 복구
- DB 트랜잭션 커밋 이후 Redis 좌석 카운터에 `partySize` 만큼 반환
- 키: `RedisUtil.generateSeatKey(restaurantId, slotDate, slotTime)`

## 데이터 접근 경로

### 주요 클래스
- `src/main/java/com/example/LunchGo/reservation/service/ReservationRefundService.java`
- `src/main/java/com/example/LunchGo/reservation/service/BusinessReservationCancelService.java`
- `src/main/java/com/example/LunchGo/reservation/service/PortoneCancelService.java`
- `src/main/java/com/example/LunchGo/reservation/controller/ReservationController.java`
- `src/main/java/com/example/LunchGo/reservation/controller/BusinessReservationController.java`

### Repository/Entity
- `src/main/java/com/example/LunchGo/reservation/repository/ReservationRepository.java`
- `src/main/java/com/example/LunchGo/reservation/repository/ReservationSlotRepository.java`
- `src/main/java/com/example/LunchGo/reservation/repository/PaymentRepository.java`
- `src/main/java/com/example/LunchGo/reservation/entity/ReservationCancellation.java`

## 프론트엔드 연동 위치
- 사용자 취소
  - `frontend/src/views/my-reservations/ReservationCancelView.vue`
- 점주 취소
  - `frontend/src/views/business/reservations/BusinessReservationsPage.vue`
  - `frontend/src/views/business/reservations/ReservationDetailPage.vue`

## 참고 사항
- `CancelReservationRequest`의 `refund` 필드는 현재 환불 로직에서 직접 사용하지 않음
- 결제 상태는 `payments.status=PAID`인 경우에만 환불 대상
