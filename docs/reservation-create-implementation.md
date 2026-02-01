# 예약 생성 구현 문서

## 작업 목표
- 예약 생성 API의 전체 흐름(검증 → 좌석 확인 → 예약 저장 → 응답)을 정리
- 예약금 결제/선주문 선결제 타입의 분기 처리 기준 명시

## 관련 API
- 예약 생성: `POST /api/reservations`
- 슬롯 시간 조회: `GET /api/reservations/slots?restaurantId=...&slotDate=...`

## 요청/응답 형식

### Request (`ReservationCreateRequest`)
```json
{
  "userId": 1,
  "restaurantId": 10,
  "slotDate": "2026-01-20",
  "slotTime": "11:00:00",
  "partySize": 4,
  "reservationType": "RESERVATION_DEPOSIT",
  "requestMessage": "창가 자리 부탁"
}
```

### Request (선주문/선결제)
```json
{
  "userId": 1,
  "restaurantId": 10,
  "slotDate": "2026-01-20",
  "slotTime": "11:00:00",
  "partySize": 4,
  "reservationType": "PREORDER_PREPAY",
  "requestMessage": "맵기 낮게",
  "totalAmount": 52000,
  "menuItems": [
    { "menuId": 3, "quantity": 2 },
    { "menuId": 7, "quantity": 1 }
  ]
}
```

### Response (`ReservationCreateResponse`)
```json
{
  "reservationId": 123,
  "reservationCode": "R20260120-0123",
  "slotId": 456,
  "userId": 1,
  "restaurantId": 10,
  "slotDate": "2026-01-20",
  "slotTime": "11:00:00",
  "partySize": 4,
  "reservationType": "RESERVATION_DEPOSIT",
  "status": "TEMPORARY",
  "requestMessage": "창가 자리 부탁"
}
```

## 백엔드 처리 흐름

### 1) Controller 진입
- `ReservationController#create`
  - `ReservationFacade#createReservation` 호출

### 2) 사전 검증 (락 전에 수행)
- 필수값 검증
  - `userId`, `restaurantId`, `slotDate`, `slotTime`, `partySize`, `reservationType`
  - `requestMessage` 길이 50자 제한
- 예약 타입 분기
  - `PREORDER_PREPAY`: `menuItems` 필수, `totalAmount` 필수
  - `RESERVATION_DEPOSIT`: 인원수 기준 예약금 계산(7인 이상 10,000원, 그 외 5,000원)
- Redis 좌석 사전 검증
  - Redis 좌석 키가 없으면 식당 예약 한도 조회 후 초기화
  - `partySize` 만큼 decrement 후 음수면 복구 + 예외

### 3) 락 구간 (핵심 예약 생성)
- `ReservationServiceImpl#createReservationLocked`
  - 분산 락 AOP 적용(개인 락 + 식당/시간대 FairLock)
  - 슬롯 비관적 락 조회 → 없으면 생성 → 잔여석 계산
  - 동일 슬롯 중복 예약 사전 체크
  - 예약 저장: `reservations` (status = `TEMPORARY`)
  - 선주문일 경우 `reservation_menu_items` 저장
  - 예약 코드 생성 후 업데이트

### 4) 커밋 이후 처리
- 예약 시간까지 TTL을 가지는 Redis 방문 상태 저장(`VisitStatus.PENDING`)

## 동시성 제어
- 개인 락(5초): 동일 유저의 빠른 중복 요청 차단
- 식당 락(FairLock): 동일 슬롯 요청 순서 보장
- 대기열 진입 실패 시 `WaitingReservationException` → 409 반환
- 예외 발생 시 Redis 좌석 카운터 복구

## 데이터 접근 경로

### 주요 클래스
- `src/main/java/com/example/LunchGo/reservation/controller/ReservationController.java`
- `src/main/java/com/example/LunchGo/reservation/service/ReservationFacade.java`
- `src/main/java/com/example/LunchGo/reservation/service/ReservationServiceImpl.java`
- `src/main/java/com/example/LunchGo/reservation/service/ReservationSlotService.java`
- `src/main/java/com/example/LunchGo/reservation/aop/DistributedLockAop.java`

### Mapper/SQL
- `src/main/resources/mapper/ReservationMapper.xml`
  - `selectSlotForUpdate`
  - `upsertSlot`
  - `sumPartySizeBySlotId`
  - `insertReservation`
  - `insertReservationMenuItems`
  - `selectReservationCreateRow`
  - `updateReservationCode`

## 프론트엔드 연동 위치
- 예약금 생성
  - `frontend/src/views/restaurant/id/booking/RestaurantBookingPage.vue`
  - `/api/reservations` 호출 후 결제 페이지 이동
- 선주문/선결제 예약 생성
  - `frontend/src/views/restaurant/id/menu/MenuSelectionPage.vue`
  - `/api/reservations` 호출 후 결제 페이지 이동
- 대기열 UX
  - `frontend/src/composables/useReservationQueue.js`

## 참고 사항
- 좌석 검증은 Redis(사전 검증) + DB 슬롯 합산(최종 검증) 이중화 구조
- 생성 직후 상태는 `TEMPORARY`이며 결제 완료 시 상태 전이
