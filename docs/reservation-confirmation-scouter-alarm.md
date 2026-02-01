# 예약 확인 Scouter 알람 분석

## 요약
Scouter WARN 알람은 OkHttp 내부 TaskRunner 스레드에서 300160 ms의 긴 elapsed
time을 보고했다. 같은 시점의 HTTP 서비스 트랜잭션
`GET /api/reservations/{reservationId}/confirmation`은 약 397 ms에 종료되었고
SQL/CPU 사용은 매우 작았다. 따라서 이 알람은 사용자가 확인 페이지를 열어둔
것 때문이 아니라, OkHttp가 처리한 외부 결제 API 호출이 오래 대기한 결과로
보는 것이 타당하다.

## Scouter 로그 관찰 사항
- Service: `GET /api/reservations/{reservationId}/confirmation`
- Elapsed: 397.0 ms
- SQL: 총 ~3 ms, CPU: 0 ms, OTHER: ~394 ms
- 알람에 표시된 스레드: `okhttp3.internal.concurrent.TaskRunner$runnable$1`
- 알람 elapsed time(300160 ms)은 HTTP 요청 시간보다 훨씬 길다

## 결론
알람은 지연되거나 블로킹된 OkHttp 작업과 일치하며, 결제 창을 오래 열어둔 뒤
취소한 상황과 부합한다. 즉, 긴 elapsed time은 외부 결제 요청이 완료 또는
타임아웃을 기다린 시간이며, 확인 엔드포인트의 HTTP 요청이 오래 열린 것이
아니다.

## 가능한 트리거
- 사용자가 결제 창을 열고 수 분간 대기한 뒤 취소
- 서버의 OkHttp 작업이 타임아웃 또는 취소 시점까지 유지됨

## 서버 로그 관찰 (결제창 진입 후 폴링)
결제하기 버튼 클릭 이후, 결제 창이 열린 상태에서 아래 순서로 로그가 지속 발생한다.
특히 `/confirmation/status` 폴링이 약 2초 간격으로 반복된다.

폴링이란 클라이언트가 일정 간격으로 같은 API를 반복 호출해 상태 변화를 확인하는 방식이다.

- 예약 생성: `POST /api/reservations` (201)
- 예약 요약: `GET /api/reservations/{reservationId}/summary` (200)
- 결제 생성: `POST /api/reservations/{reservationId}/payments` (200)
- 결제 요청 기록: `POST /api/payments/portone/requested` (200)
- 결제 상태 폴링: `GET /api/reservations/{reservationId}/confirmation/status` (반복)

예시 로그(UTC):
- 23:26:48 예약 생성
- 23:26:48 예약 요약
- 23:27:12 결제 생성
- 23:27:12 결제 요청 기록
- 23:27:12 ~ 23:27:49 결제 상태 폴링 반복(약 2초 주기)

### 로그 원문 발췌 (UTC)
```
2026-01-10 23:26:48.012 INFO  [http-nio-8080-exec-6] http.access traceId=c0f2aff9-cbbc-4365-82af-50c8a5215c0f userId=12 method=POST path=/api/reservations status=201 latencyMs=570 - request
2026-01-10 23:26:48.279 INFO  [http-nio-8080-exec-7] http.access traceId=eacae93e-e09c-4e28-bacd-0505e4ca9788 userId=12 method=GET path=/api/reservations/915692/summary status=200 latencyMs=235 - request
2026-01-10 23:27:12.319 INFO  [http-nio-8080-exec-8] http.access traceId=98158590-070c-4bb4-993c-fafc0fdeb8dc userId=12 method=POST path=/api/reservations/915692/payments status=200 latencyMs=433 - request
2026-01-10 23:27:12.463 INFO  [http-nio-8080-exec-9] http.access traceId=4a1526ef-de36-4d92-886e-9c29e2ca6a39 userId=12 method=POST path=/api/payments/portone/requested status=200 latencyMs=130 - request
2026-01-10 23:27:12.607 INFO  [http-nio-8080-exec-10] http.access traceId=008dd658-f50d-4e13-bd5f-3c632a47e433 userId=12 method=GET path=/api/reservations/915692/confirmation/status status=200 latencyMs=131 - request
2026-01-10 23:27:21.309 INFO  [http-nio-8080-exec-4] http.access traceId=69f57ca9-96dd-4dde-9851-a42d2076a20f userId=12 method=GET path=/api/reservations/915692/confirmation/status status=200 latencyMs=150 - request
2026-01-10 23:27:49.741 INFO  [http-nio-8080-exec-7] http.access traceId=06641d86-3e47-4bf3-a26e-c46d8fa838ee userId=12 method=GET path=/api/reservations/915692/confirmation/status status=200 latencyMs=195 - request
```

## 후속 확인 사항
- 알람 시각에 어떤 결제 API 호출이 진행 중이었는지 확인
- OkHttp `callTimeout` / `readTimeout` / `writeTimeout` 설정 점검
- 타임아웃을 단축하거나 콜백/웹훅 중심 비동기 흐름으로 전환 여부 검토
- 결제 대기처럼 장시간이 가능한 구간은 Scouter 임계치/필터 조정 검토

## 알람 발생 코드 및 영향 정리
### 원인 코드
PortOne 결제 상태를 검증하는 외부 호출에서 알람이 발생한다.

- `src/main/java/com/example/LunchGo/reservation/service/PortoneVerificationService.java`
  - `RestTemplate.exchange()`로 PortOne API 호출
  - 내부 HTTP 클라이언트(OkHttp 등)가 응답을 오래 대기하면서 Scouter `TaskRunner` 경고 발생

### 호출 위치
- `ReservationPaymentService.completePayment()`
- `ReservationPaymentService.handleWebhookPaid()`

### 영향
- 외부 결제 API 응답이 지연되면 서버 스레드가 오래 대기하면서 경고/슬랙 알림 발생 가능
- 치명적 장애는 아니지만 요청 처리 시간이 길어지고 스레드 점유가 늘 수 있음

## 해결책 및 장단점
### 1) RestTemplate 타임아웃 설정
- 장점: 가장 빠른 완화책, 긴 대기 경고 감소, 스레드 점유 시간 제한
- 단점: 일시적 지연에도 실패율 증가 가능, 재시도 로직 없으면 사용자 실패 체감

### 2) 웹훅 중심 비동기 처리로 전환
- 장점: 서버가 결제 완료를 기다리지 않아도 됨, 요청 지연/경고 최소화
- 단점: 구현 범위 큼, 웹훅 신뢰성/서명 검증/재처리 설계 필요

### 3) 비동기 처리 + 큐/리스너로 외부 조회 분리
- 장점: 사용자 요청과 결제 검증을 분리, 서버 스레드 블로킹 감소
- 단점: 인프라/운영 복잡도 증가, 지연 허용 UX 설계 필요

### 4) 알람 임계치/필터 조정
- 장점: 운영 알람 소음 감소, 코드 변경 최소
- 단점: 실제 지연 문제는 남음, 근본 해결 아님

## 타임아웃 발생 시 사용자 영향 (1번 기준)
- 서버가 외부 결제 API 응답을 기다리다 타임아웃되면 해당 요청은 실패로 처리된다.
- 사용자가 즉시 브라우저에서 튕겨 나가는 것은 아니며, 프론트는 실패/재시도 UI를 보여준다.
- 결제창 자체는 PG 측 상태에 따라 계속 열려 있을 수 있고, 이후 웹훅으로 결제가 완료되면 서버에서 상태를 갱신할 수 있다.

## 타임아웃/재시도 설정 의미
- `portone.http.connect-timeout-ms`: PortOne 서버에 연결을 맺는 데 허용하는 최대 시간(ms)
- `portone.http.read-timeout-ms`: 연결 후 응답 본문을 기다리는 최대 시간(ms)
- `portone.http.max-attempts`: 외부 호출 재시도 최대 횟수 (1이면 재시도 없음)
- `portone.http.retry-backoff-ms`: 재시도 사이 대기 시간(ms)
