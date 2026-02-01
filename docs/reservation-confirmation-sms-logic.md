# 예약 확인 SMS 로직 분리 및 폴링 개선

## 배경
`GET /api/reservations/{reservationId}/confirmation`는 예약 확인 화면을 위한 조회 API지만,
내부에서 점주 알림 문자(SMS)를 발송하고 있었다. 프론트 결제 화면은 결제 완료를
확인하기 위해 동일 엔드포인트를 2초 주기로 폴링하므로, 결제 대기 중에 SMS가
여러 번 발송될 위험이 있다.

## 문제점
- 조회 API가 부수 효과(SMS 발송)를 포함하고 있어 폴링 시 반복 트리거됨
- 결제 완료 경로가 콜백/리다이렉트/웹훅으로 중복 호출될 수 있어 중복 발송 위험

## 개선 방향
### A. SMS 발송 시점 분리 (결제 확정 1회)
- 결제 확정 처리(complete/webhook)에서만 SMS를 발송
- 결제 확정 상태로 최초 전환될 때만 발송하도록 가드
- 트랜잭션 커밋 이후에 발송해 롤백 시 잘못된 알림을 방지

### B. 폴링 전용 API 분리
- 폴링 전용 상태 조회 API를 별도로 제공
- 조회는 조회만 수행하도록 역할 분리

## 적용 사항
- `/api/reservations/{reservationId}/confirmation`에서 SMS 발송 제거
- 결제 확정 처리 시점에 SMS 발송 (트랜잭션 커밋 후 1회)
- 폴링 전용 API 추가: `GET /api/reservations/{reservationId}/confirmation/status`
- 프론트 폴링 호출을 위 상태 조회 API로 변경

## 기대 효과
- 폴링으로 인한 SMS 중복 발송 방지
- 결제 확정 시점의 단일 발송 보장
- 조회/사이드이펙트 분리로 유지보수성 향상
