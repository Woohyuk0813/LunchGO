# 로그인 대기열 구현 정리

## 목표
- 동시 로그인 부하로 인한 CPU 스파이크를 완화
- Redis 기반 진짜 대기열(토큰/순번)로 로그인 요청을 단계적으로 허용
- 프론트에 대기 화면을 제공하고 순번이 되면 자동 진행

## 구현 개요
- Redis Sorted Set으로 대기열 관리 (순번 점수)
- 토큰별 TTL로 만료 관리
- `/api/login`에서 토큰 검증 후 허용/차단
- 프론트는 `/api/login/queue`로 진입하고 상태 폴링

## 서버 구성
### 1) Redis 키 설계
- 대기열 Sorted Set: `login:queue`
- 토큰 상태 키: `login:queue:token:{token}`
- 시퀀스 카운터: `login:queue:seq`

### 2) 환경 설정
`src/main/resources/application.yml`
- `LOGIN_QUEUE_ENABLED` (default `true`)
- `LOGIN_QUEUE_CAPACITY` (default `20`)
- `LOGIN_QUEUE_TOKEN_TTL_MS` (default `120000`)

### 3) API 엔드포인트
- `POST /api/login/queue` : 대기열 진입 + 토큰 발급
- `GET /api/login/queue?token=...` : 대기열 상태 조회
- `DELETE /api/login/queue?token=...` : 대기열 이탈/정리
- `POST /api/login` : `queueToken` 검증 후 로그인 진행

### 4) 로그인 처리 흐름
1. 클라이언트가 `POST /api/login/queue`로 대기열 진입
2. 서버는 토큰을 발급하고 순번/대기인원 반환
3. 클라이언트는 `GET /api/login/queue` 폴링
4. `allowed=true`가 되면 로그인 요청으로 진행
5. `/api/login`은 토큰 유효성/순번 확인 후 처리
6. 로그인 완료 시 토큰 제거

## 프론트 구성
### 1) 대기열 컴포저블
`frontend/src/composables/useLoginQueue.js`
- `POST /api/login/queue`로 진입
- `GET /api/login/queue` 폴링 (기본 2초)
- 대기 인원에 따른 예상 대기 시간 표시
- 토큰 만료/오류 시 에러 모달 표시

### 2) 대기 모달
`frontend/src/components/ui/WaitingModal.vue`
- 대기 상태 / 에러 상태 공통 사용

### 3) 로그인 페이지 연동
- `frontend/src/views/login/LoginPage.vue`
- `frontend/src/views/login/AdminLoginPage.vue`

흐름:
1. 로그인 클릭 → `waitForTurn()` 실행
2. 허용되면 `queueToken` 포함해 `/api/login` 호출
3. 대기 중이면 모달 표시 및 자동 폴링

## 응답 예시
### 대기열 진입
```json
{
  "queueToken": "uuid",
  "allowed": false,
  "expired": false,
  "position": 42,
  "waitingCount": 22,
  "capacity": 20,
  "message": null
}
```

### 허용 상태
```json
{
  "queueToken": "uuid",
  "allowed": true,
  "expired": false,
  "position": 5,
  "waitingCount": 0,
  "capacity": 20,
  "message": null
}
```

## 유의사항
- 토큰 TTL 만료 시 대기열 재진입 필요
- 대기열 수용량은 동시 로그인 허용 수로 조정
- 단일 인스턴스 기준 (멀티 인스턴스에서도 Redis 기반으로 동작 가능)

