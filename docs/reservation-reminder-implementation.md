# 예약 리마인더 구현 문서

## 작업 목표
- 예약 1시간 전 사용자에게 방문 확인 리마인더를 발송
- 링크 클릭으로 방문 확정/취소 응답을 기록

## 관련 API/페이지

### 리마인더 응답 API
- 방문 확정: `GET /api/reminders/visit?token=...`
- 방문 취소: `GET /api/reminders/cancel?token=...`

### 리마인더 랜딩 페이지
- 방문/취소 페이지: `/reminders/visit`, `/reminders/cancel`
- 결과 페이지: `/reminders/result?result=...`
- 프론트 정적 페이지: `frontend/public/reminder-visit.html`

## 처리 흐름

### 1) 스케줄러 실행
- `ReservationReminderScheduler`가 매 분 실행
- `ReservationReminderService#sendDueReminders` 호출

### 2) 대상 조회
- MyBatis `selectReminderTargets`로 발송 대상 조회
  - 상태: `CONFIRMED`, `PREPAID_CONFIRMED`
  - 아직 발송되지 않은 건(`reminder_sent_at`/`reminder_token` null)
  - 예약 시간 55~60분 전

### 3) 발송 처리
- 발송 토큰 생성 (`Base64 URL-safe`)
- `tryMarkReminderSent`로 동시성 방어(1회 업데이트)
- 방문/취소 URL 생성
  - `publicBaseUrl + "/reminder-visit.html?token=..."`
  - `publicBaseUrl + "/reminder-cancel.html?token=..."`
- SMS 이벤트 발행(`SystemSmsSendEvent`)

### 4) 사용자 응답 처리
- `ReminderResponseController`
  - `updateVisitStatusByToken` 실행
  - 방문 확정: `visit_status=CONFIRMED`
  - 방문 취소: `visit_status=CANCELLED`
- 응답 후 리다이렉트
  - `app.reminder.redirect-url` 설정 시 해당 URL로 이동
  - 미설정 시 텍스트 응답(`will_visit`, `cancel_visit`, `invalid`)

## 데이터 접근 경로

### 주요 클래스
- `src/main/java/com/example/LunchGo/reservation/service/ReservationReminderService.java`
- `src/main/java/com/example/LunchGo/reservation/service/ReservationReminderScheduler.java`
- `src/main/java/com/example/LunchGo/reservation/controller/ReminderResponseController.java`
- `src/main/java/com/example/LunchGo/reservation/controller/ReminderPageController.java`

### Mapper/SQL
- `src/main/resources/mapper/ReservationMapper.xml`
  - `selectReminderTargets`
  - `tryMarkReminderSent`
  - `updateVisitStatusByToken`

## 프론트엔드 연동
- 정적 페이지에서 API 호출 링크 구성
  - `frontend/public/reminder-visit.html`
  - `API_BASE + /api/reminders/visit|cancel?token=...`

## 참고 사항
- `reminder-cancel.html`은 현재 존재하지 않으며, 방문 취소 링크는 API로 직접 호출하도록 구성됨
- 보안 설정에서 `/api/reminders/**`, `/reminders/**`는 인증 없이 접근 가능
