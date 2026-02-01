# 로그인 부하 테스트 가이드 (Baseline)

## 목적
- 로그인 API의 처리량/지연/실패율을 일관된 기준으로 비교
- 대기열 적용 전/후의 효과를 수치로 검증
- CPU 포화 여부와 사용자 체감(대기 시간)을 함께 판단

## 테스트 전제
- 대상 API: `POST /api/login`
- 계정 패턴: `loadtest.user0001@example.com` 형태
- 실행 위치: bastion에서 Docker k6 실행
- 큐 비활성/활성 여부를 명확히 기록

---

## 실행 파라미터 정의

### 공통 파라미터
| 파라미터 | 의미 | 예시 |
| --- | --- | --- |
| `BASE_URL` | API 베이스 URL | `http://10.0.2.6:8080` |
| `EMAIL_PREFIX` | 테스트 계정 prefix | `loadtest.user` |
| `EMAIL_DOMAIN` | 테스트 계정 도메인 | `example.com` |
| `PASSWORD` | 테스트 계정 비밀번호 | `Passw0rd!123` |

### 부하 형태 파라미터
| 파라미터 | 의미 | 예시 |
| --- | --- | --- |
| `LOAD_VUS` | 동시 VU 수 | `20`, `1000` |
| `LOAD_DURATION` | 부하 지속 시간 | `2m`, `3m` |
| `LOAD_MODE` | 부하 모드 | `constant-vus`, `one-shot` |
| `LOAD_ITERATIONS` | VU당 반복 횟수(only one-shot) | `1` |

### 부하 동작 방식 요약
- `LOAD_MODE=constant-vus`: 지정된 VU가 `LOAD_DURATION` 동안 **로그인 요청을 계속 반복**한다.
- `LOAD_MODE=one-shot`: 지정된 VU가 `LOAD_ITERATIONS`만큼만 요청하고 종료한다.

### 대기열 파라미터
| 파라미터 | 의미 | 예시 |
| --- | --- | --- |
| `USE_LOGIN_QUEUE` | 대기열 사용 여부 | `true/false` |
| `LOGIN_QUEUE_POLL_MS` | 상태 폴링 간격(ms) | `1000`, `1500` |
| `LOGIN_QUEUE_MAX_WAIT_MS` | 대기 최대 시간(ms) | `60000`, `180000` |

### 서버 설정 (참고)
| 변수 | 의미 | 비고 |
| --- | --- | --- |
| `LOGIN_QUEUE_ENABLED` | 큐 활성화 여부 | 서버 환경 변수 |
| `LOGIN_QUEUE_CAPACITY` | 동시 로그인 허용 수 | 서버 환경 변수 |
| `LOGIN_QUEUE_TOKEN_TTL_MS` | 큐 토큰 TTL | 서버 환경 변수 |

---

## 테스트 시나리오 (권장 세트)

### 1) Baseline: 큐 비활성, 안정 지표 확보
- 목적: 로그인 자체의 순수 성능
- 설정:
  - `USE_LOGIN_QUEUE=false`
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`

### 2) Queue ON: 동일 조건 비교
- 목적: 큐 적용 전/후의 지연/실패율 변화 확인
- 설정:
  - `USE_LOGIN_QUEUE=true`
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`

### 3) Queue ON: 동시 폭주(One-shot)
- 목적: 동시 로그인 폭주 시 대기열 효과 확인
- 설정:
  - `USE_LOGIN_QUEUE=true`
  - `LOAD_MODE=one-shot`
  - `LOAD_VUS=1000`
  - `LOAD_ITERATIONS=1`
  - `LOAD_DURATION=3m`

### 4) Polling 튜닝 비교
- 목적: CPU vs 완료 시간 트레이드오프 확인
- 설정 예:
  - `LOGIN_QUEUE_POLL_MS=1000 / 1500 / 2000`
  - 동일 VU/Duration으로 비교

---

## 핵심 지표 (필수로 기록)

### k6 요약 지표
- `http_req_duration` (avg/p90/p95/max)
- `http_req_failed` (실패율)
- `iterations` (완주 여부)
- `dropped_iterations` (미완주 여부)
- `http_reqs` (총 요청 수, RPS)

### 대기열 지표 (queue on)
- `queue_allowed_rate`
- `queue_timeout_rate`
- `queue_expired_rate`
- `queue_status_failed_rate`
- 완료 시간(One-shot 기준)

### Scouter/XLog 지표 (가능하면)
- `/api/login<POST>` 평균/최대 응답시간
- SQL 평균 시간/횟수
- 오류 카운트

---

## 결과 기록 템플릿

### 테스트 파라미터
- 실행 시각:
- 서버 설정: `LOGIN_QUEUE_ENABLED/LOGIN_QUEUE_CAPACITY/LOGIN_QUEUE_TOKEN_TTL_MS`
- k6 파라미터:
  - `LOAD_MODE`:
  - `LOAD_VUS`:
  - `LOAD_DURATION`:
  - `LOAD_ITERATIONS`:
  - `USE_LOGIN_QUEUE`:
  - `LOGIN_QUEUE_POLL_MS`:
  - `LOGIN_QUEUE_MAX_WAIT_MS`:

### k6 결과 요약
- `http_req_duration` avg / p90 / p95 / max:
- `http_req_failed`:
- `queue_wait_ms` avg / p95:
- `login_flow_ms` avg / p95:
- `login_req_ms` avg / p95:
- `iterations` / `dropped_iterations`:
- `http_reqs`:
- `queue_*` rates (queue on):

### Scouter 요약 (있으면)
- `/api/login<POST>` avg/p95:
- SQL avg time:
- 오류 건수:

---

## 2-1차 실행 결과 (Queue ON, constant-vus)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=10` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1000`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 7.09ms / p90 9.77ms / p95 14.53ms / max 235.56ms
- `http_req_failed` 0.0000
- `iterations` 40 / `dropped_iterations` n/a
- `http_reqs` 2440 (20.17/s)
- `queue_allowed_rate` 0.00%
- `queue_timeout_rate` 100.00%

### 해석 메모
- 대기열 통과가 전혀 발생하지 않아 로그인 시도는 대부분 대기열 대기만 수행됨
  - 원인: Redis 대기열 키(`login:queue*`)가 비워지지 않아 대기열이 막힌 상태로 추정
  - 조치: Redis 큐 키 삭제 후 재실행

### Scouter 요약 (있으면)
- `/api/login/queue<GET>` avg 3ms (2,400건, error 0)
- `/api/login/queue<POST>` avg 83ms (40건, error 0)

---

## 2-2차 실행 결과 (Queue ON, constant-vus, 큐 정리 후)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=10` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1000`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 38.00ms / p90 86.63ms / p95 131.70ms / max 1064.89ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 1015.07ms / p95 1023.00ms
- `login_flow_ms` avg 1118.83ms / p95 1288.20ms
- `login_req_ms` avg 103.73ms / p95 270.00ms
- `iterations` 2155 / `dropped_iterations` n/a
- `http_reqs` 6473 (53.48/s)
- `queue_allowed_rate` 100.00%

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 102ms (2,155건, error 0)
- `/api/login/queue<POST>` avg 5ms (2,155건, error 0)
- `/api/login/queue<GET>` avg 3ms (2,163건, error 0)

---

## 2-3차 실행 결과 (Queue ON, constant-vus, poll=1500ms)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=10` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1500`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 28.37ms / p90 69.27ms / p95 72.22ms / max 427.64ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 1522.05ms / p95 1510.00ms
- `login_flow_ms` avg 1602.31ms / p95 1640.60ms
- `login_req_ms` avg 80.24ms / p95 147.00ms
- `iterations` 1507 / `dropped_iterations` n/a
- `http_reqs` 4537 (37.33/s)
- `queue_allowed_rate` 100.00%

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 79ms (1,507건, error 0)
- `/api/login/queue<POST>` avg 2ms (1,507건, error 0)
- `/api/login/queue<GET>` avg 1ms (1,523건, error 0)

### 해석 메모
- CPU 관찰: 50~60% 수준으로 유지

---

## 2-4차 실행 결과 (Queue ON, constant-vus, poll=1500ms, capacity=8)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=8` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1500`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 25.81ms / p90 70.98ms / p95 84.40ms / max 453.90ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 2958.41ms / p95 3025.00ms
- `login_flow_ms` avg 3043.81ms / p95 3148.10ms
- `login_req_ms` avg 85.36ms / p95 164.05ms
- `iterations` 800 / `dropped_iterations` n/a
- `http_reqs` 3168 (25.75/s)
- `queue_allowed_rate` 100.00%

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 84ms (800건, error 0)
- `/api/login/queue<POST>` avg 7ms (800건, error 0)
- `/api/login/queue<GET>` avg 2ms (1,568건, error 0)

### 해석 메모
- CPU 관찰: 25~35% 수준으로 유지

---

## 2-5차 실행 결과 (Queue ON, constant-vus, poll=1500ms, capacity=12)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=12` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1500`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 34.05ms / p90 71.63ms / p95 78.07ms / max 1357.19ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 1495.82ms / p95 1518.00ms
- `login_flow_ms` avg 1587.02ms / p95 1674.90ms
- `login_req_ms` avg 91.16ms / p95 177.00ms
- `iterations` 1522 / `dropped_iterations` n/a
- `http_reqs` 4550 (37.44/s)
- `queue_allowed_rate` 100.00%

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 90ms (1,522건, error 0)
- `/api/login/queue<POST>` avg 5ms (1,522건, error 0)
- `/api/login/queue<GET>` avg 2ms (1,506건, error 0)
- `BaseMemberService#updateLastLoginAt()` avg 6ms (1,522건, error 0)

### 해석 메모
- CPU 관찰: 50~60% 수준으로 유지

---

## 2-6차 실행 결과 (Queue ON, constant-vus, poll=1500ms, capacity=15)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=15` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1500`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 33.85ms / p90 72.22ms / p95 82.61ms / max 856.38ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 1487.12ms / p95 1517.40ms
- `login_flow_ms` avg 1577.49ms / p95 1646.20ms
- `login_req_ms` avg 90.33ms / p95 173.00ms
- `iterations` 1533 / `dropped_iterations` n/a
- `http_reqs` 4574 (37.63/s)
- `queue_allowed_rate` 100.00%

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 89ms (1,533건, error 0)
- `/api/login/queue<POST>` avg 6ms (1,533건, error 0)
- `/api/login/queue<GET>` avg 2ms (1,508건, error 0)
- `BaseMemberService#updateLastLoginAt()` avg 6ms (1,533건, error 0)

### 해석 메모
- CPU 관찰: 55~65% 수준으로 유지

---

## 3-1차 실행 결과 (Queue ON, one-shot, poll=1500ms, capacity=12)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=12` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=one-shot`
  - `LOAD_VUS=1000`
  - `LOAD_DURATION=3m`
  - `LOAD_ITERATIONS=1`
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1500`
  - `LOGIN_QUEUE_MAX_WAIT_MS=180000`

### k6 결과 요약
- `http_req_duration` avg 61.00ms / p90 47.30ms / p95 279.98ms / max 2408.37ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 48901.57ms / p95 89787.45ms
- `login_flow_ms` avg 49188.32ms / p95 89892.90ms
- `login_req_ms` avg 286.73ms / p95 601.00ms
- `iterations` 1000 / `dropped_iterations` n/a
- `http_reqs` 33367 (348.37/s)
- `queue_allowed_rate` 100.00%
- 완료 시간: 1m35.8s

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 275ms (1,000건, error 0)
- `/api/login/queue<POST>` avg 135ms (1,000건, error 0)
- `/api/login/queue<GET>` avg 8ms (31,367건, error 0)
- `BaseMemberService#updateLastLoginAt()` avg 9ms (1,000건, error 0)

### 해석 메모
- CPU 관찰: 최대 91%, 평균 약 60% 수준

---

## 3-2차 실행 결과 (Queue ON, constant-vus, 10m, poll=1500ms, capacity=12)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=12` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=10m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1500`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 27.07ms / p90 68.51ms / p95 70.07ms / max 801.45ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 1503.69ms / p95 1510.00ms
- `login_flow_ms` avg 1578.93ms / p95 1584.00ms
- `login_req_ms` avg 75.20ms / p95 76.00ms
- `iterations` 7610 / `dropped_iterations` n/a
- `http_reqs` 22814 (37.92/s)
- `queue_allowed_rate` 100.00%
- 완료 시간: 10m0s

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 74ms (7,610건, error 0)
- `/api/login/queue<POST>` avg 3ms (7,601건, error 0)
- `/api/login/queue<GET>` avg 2ms (7,186건, error 0)
- `BaseMemberService#updateLastLoginAt()` avg 5ms (7,610건, error 0)

### 해석 메모
- TPS 관찰: 37~38 수준
- CPU 관찰: 50~55% 범위 유지

---

## 3-3차 실행 결과 (Queue ON, constant-vus, 2m, poll=1000ms, capacity=12)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=12` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1000`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 32.46ms / p90 70.67ms / p95 81.33ms / max 440.11ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 1001.82ms / p95 1011.00ms
- `login_flow_ms` avg 1093.64ms / p95 1287.00ms
- `login_req_ms` avg 91.79ms / p95 293.80ms
- `iterations` 2204 / `dropped_iterations` n/a
- `http_reqs` 6602 (54.54/s)
- `queue_allowed_rate` 100.00%
- 완료 시간: 2m0s

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 90ms (2,204건, error 0)
- `/api/login/queue<POST>` avg 2ms (2,204건, error 0)
- `/api/login/queue<GET>` avg 1ms (2,194건, error 0)
- `BaseMemberService#updateLastLoginAt()` avg 4ms (2,204건, error 0)

### 해석 메모
- TPS 관찰: 55 수준
- CPU 관찰: 평균 70% 수준

---

## 3-4차 실행 결과 (Queue ON, constant-vus, VU=50, poll=1000ms, capacity=12)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=true` / `LOGIN_QUEUE_CAPACITY=12` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=50`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=true`
  - `LOGIN_QUEUE_POLL_MS=1000`
  - `LOGIN_QUEUE_MAX_WAIT_MS=60000`

### k6 결과 요약
- `http_req_duration` avg 26.40ms / p90 70.73ms / p95 107.04ms / max 497.76ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 2926.67ms / p95 3028.35ms
- `login_flow_ms` avg 3044.08ms / p95 3341.00ms
- `login_req_ms` avg 117.38ms / p95 348.70ms
- `iterations` 1994 / `dropped_iterations` n/a
- `http_reqs` 9795 (79.65/s)
- `queue_allowed_rate` 100.00%
- 완료 시간: 2m0s

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 116ms (1,994건, error 0)
- `/api/login/queue<POST>` avg 2ms (1,994건, error 0)
- `/api/login/queue<GET>` avg 2ms (5,807건, error 0)
- `BaseMemberService#updateLastLoginAt()` avg 5ms (1,994건, error 0)

### 해석 메모
- TPS 관찰: 80 수준
- CPU 관찰: 평균 70% 수준

---

## 폴링 비교 요약 (Queue ON)
| 구분 | capacity | Poll(ms) | iterations | http_reqs/s | avg(ms) | p95(ms) | /api/login avg(ms) | queue GET count |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2-2차 | 10 | 1000 | 2155 | 53.48 | 38.00 | 131.70 | 102 | 2,163 |
| 2-3차 | 10 | 1500 | 1507 | 37.33 | 28.37 | 72.22 | 79 | 1,523 |
| 3-3차 | 12 | 1000 | 2204 | 54.54 | 32.46 | 81.33 | 90 | 2,194 |

### 요약
- Poll 간격을 늘리면 큐 GET 호출 수와 처리량이 함께 감소함
- 3-3차는 capacity=12 조건이므로 capacity=10 결과와 직접 비교 시 주의

---

## Queue ON/OFF 비교 요약
| 구분 | Queue OFF (1차) | Queue ON (2-2차, poll=1000ms) | Queue ON (2-3차, poll=1500ms) |
| --- | --- | --- | --- |
| 로그인 완료 수 (iterations) | 3233 | 2155 | 1507 |
| 처리량 (iterations/s) | 26.86 | 17.80 | 12.40 |
| k6 avg(ms) | 743.76 | 38.00 | 28.37 |
| k6 p95(ms) | 880.20 | 131.70 | 72.22 |
| Scouter `/api/login` avg(ms) | 736 | 102 | 79 |
| CPU 관찰 | 거의 2분 내내 100% | 70~80% | 50~60% |

### 해석 메모
- 큐 ON은 로그인 지연과 CPU를 낮추는 대신 처리량이 감소함
- k6 지표는 큐 상태 조회까지 포함되므로, 로그인 자체 비교는 Scouter 지표가 더 정확함

---

## 체감/로그인 처리 시간 비교 (ms)
| 지표 | Queue OFF (1차) | Queue ON (2-2차, poll=1000ms) | Queue ON (2-3차, poll=1500ms) |
| --- | --- | --- | --- |
| `login_flow_ms` avg | 743.88 | 1118.83 | 1602.31 |
| `login_flow_ms` p95 | 880.40 | 1288.20 | 1640.60 |
| `queue_wait_ms` avg | 0.00 | 1015.07 | 1522.05 |
| `queue_wait_ms` p95 | 0.00 | 1023.00 | 1510.00 |
| `login_req_ms` avg | 743.86 | 103.73 | 80.24 |
| `login_req_ms` p95 | 880.40 | 270.00 | 147.00 |

### 해석 메모
- `login_flow_ms`가 사용자 체감(큐 대기 + 로그인 처리)을 가장 잘 반영
- `login_req_ms`는 실제 로그인 처리 시간에 가장 가까움

---

## Capacity 비교 요약 (poll=1500ms)
| capacity | iterations/s | login_flow_ms avg/p95 | queue_wait_ms avg/p95 | login_req_ms avg/p95 | CPU 관찰 |
| --- | --- | --- | --- | --- | --- |
| 10 | 12.40 | 1602.31 / 1640.60 | 1522.05 / 1510.00 | 80.24 / 147.00 | 50~60% |
| 12 | 12.52 | 1587.02 / 1674.90 | 1495.82 / 1518.00 | 91.16 / 177.00 | 50~60% |
| 15 | 12.61 | 1577.49 / 1646.20 | 1487.12 / 1517.40 | 90.33 / 173.00 | 55~65% |
| 8 | 6.50 | 3043.81 / 3148.10 | 2958.41 / 3025.00 | 85.36 / 164.05 | 25~35% |

---

## 현재까지 테스트 결과 요약 (constant-vus)
| 구분 | Queue | capacity | poll(ms) | iterations/s | login_flow avg/p95 (ms) | queue_wait avg/p95 (ms) | login_req avg/p95 (ms) | CPU 관찰 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1차 | OFF | N/A | N/A | 26.86 | 743.88 / 880.40 | 0.00 / 0.00 | 743.86 / 880.40 | 거의 2분 내내 100% |
| 2-2차 | ON | 10 | 1000 | 17.80 | 1118.83 / 1288.20 | 1015.07 / 1023.00 | 103.73 / 270.00 | 70~80% |
| 2-3차 | ON | 10 | 1500 | 12.40 | 1602.31 / 1640.60 | 1522.05 / 1510.00 | 80.24 / 147.00 | 50~60% |
| 3-3차 | ON | 12 | 1000 | 18.21 | 1093.64 / 1287.00 | 1001.82 / 1011.00 | 91.79 / 293.80 | 70% 평균 |
| 2-4차 | ON | 8 | 1500 | 6.50 | 3043.81 / 3148.10 | 2958.41 / 3025.00 | 85.36 / 164.05 | 25~35% |
| 2-5차 | ON | 12 | 1500 | 12.52 | 1587.02 / 1674.90 | 1495.82 / 1518.00 | 91.16 / 177.00 | 50~60% |
| 2-6차 | ON | 15 | 1500 | 12.61 | 1577.49 / 1646.20 | 1487.12 / 1517.40 | 90.33 / 173.00 | 55~65% |

### 해석 메모
- `capacity=10~15` 범위는 처리량/지연/CPU가 유사한 수준으로 유지됨
- `capacity=8`은 처리량이 크게 낮아져 체감 지연이 증가함
- capacity를 낮추면 대기 시간이 증가하고 처리량이 감소함
- CPU는 낮아지지만 체감 대기 시간이 빠르게 증가함

---

## One-shot 결과 요약
| 구분 | Queue | capacity | poll(ms) | 완료 시간 | queue_wait avg/p95 (ms) | login_flow avg/p95 (ms) | login_req avg/p95 (ms) | CPU 관찰 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 3-1차 | ON | 12 | 1500 | 1m35.8s | 48901.57 / 89787.45 | 49188.32 / 89892.90 | 286.73 / 601.00 | 최대 91%, 평균 ~60% |

---

## 장시간 안정성 요약
| 구분 | Queue | capacity | poll(ms) | 완료 시간 | iterations/s | login_flow avg/p95 (ms) | login_req avg/p95 (ms) | CPU 관찰 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 3-2차 | ON | 12 | 1500 | 10m0s | 12.65 | 1578.93 / 1584.00 | 75.20 / 76.00 | 50~55% |

---

## VU 상향 결과 요약 (poll=1000ms, capacity=12)
| 구분 | VU | iterations/s | login_flow avg/p95 (ms) | queue_wait avg/p95 (ms) | login_req avg/p95 (ms) | CPU 관찰 | TPS 관찰 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 3-3차 | 20 | 18.21 | 1093.64 / 1287.00 | 1001.82 / 1011.00 | 91.79 / 293.80 | 70% 평균 | 55 |
| 3-4차 | 50 | 16.21 | 3044.08 / 3341.00 | 2926.67 / 3028.35 | 117.38 / 348.70 | 70% 평균 | 80 |

### 해석 메모
- VU가 증가하면서 `queue_wait_ms`와 `login_flow_ms`가 크게 증가함
- `/api/login/queue<GET>` 호출 증가로 `http_reqs/s`는 높아지지만 `iterations/s`는 감소함

---

## 추천 판단 (확정)
- 운영 확정값: `LOGIN_QUEUE_CAPACITY=12`, `LOGIN_QUEUE_POLL_MS=1500`, `LOGIN_QUEUE_ENABLED=true`
  - 이유: `capacity=10~15` 구간 성능이 유사하며, 12는 CPU 50~60%로 안정적임
- 부하 여유가 있고 처리량을 조금 더 확보하고 싶으면 `capacity=15`도 선택 가능
- `poll=1500ms`는 CPU 여유를 확보하는 대신 체감 대기 시간이 늘어나는 트레이드오프임
  - CPU 여유가 중요한 상황: 1500ms 유지
  - 처리량/체감 지연 우선: 1000ms로 낮추는 재검증 필요
- `poll=1000ms` 재검증 결과(3-3차): 처리량은 증가했지만 CPU가 70% 수준으로 상승함

---

## 결론
- 큐 OFF는 평균 지연은 낮지만 CPU 100% 지속으로 안정성이 떨어짐
- 큐 ON은 CPU를 안정화시키는 대신 체감 대기(`login_flow_ms`)가 증가함
- `capacity=10~15`는 성능/지연/CPU가 유사하므로 운영 기본값으로 적합
- `poll=1500ms`는 안정성 우선, `poll=1000ms`는 체감/처리량 우선
- VU 상향 시 대기열 누적이 빠르게 증가하므로 폭주 대응 기준 필요

## 1차 실행 결과 (Baseline, queue off)
### 테스트 파라미터
- 실행 시각: 2026-01-16
- 서버 설정: `LOGIN_QUEUE_ENABLED=false` / `LOGIN_QUEUE_CAPACITY=10` / `LOGIN_QUEUE_TOKEN_TTL_MS=120000`
- k6 파라미터:
  - `LOAD_MODE=constant-vus`
  - `LOAD_VUS=20`
  - `LOAD_DURATION=2m`
  - `LOAD_ITERATIONS` (N/A)
  - `USE_LOGIN_QUEUE=false`
  - `LOGIN_QUEUE_POLL_MS` (N/A)
  - `LOGIN_QUEUE_MAX_WAIT_MS` (N/A)

### k6 결과 요약
- `http_req_duration` avg 743.76ms / p90 850.97ms / p95 880.20ms / max 1695.04ms
- `http_req_failed` 0.0000
- `queue_wait_ms` avg 0.00ms / p95 0.00ms
- `login_flow_ms` avg 743.88ms / p95 880.40ms
- `login_req_ms` avg 743.86ms / p95 880.40ms
- `iterations` 3233 / `dropped_iterations` n/a
- `http_reqs` 3233 (26.86/s)

### Scouter 요약 (있으면)
- `/api/login<POST>` avg 736ms (3,233건, error 0)
- SQL avg time 6ms (login) / 2ms (`updateLastLoginAt`)
- 오류 건수: 0
- 기타: `/api/vendor/phpunit/phpunit/src/Util/PHP/eval-stdin.php` 1건 (77ms)

---

## 큐 ON 전환 재기동 예시
```bash
docker rm -f lunchgo-backend
docker run --env-file /opt/lunchgo/.env \
  -v /opt/lunchgo/fonts:/app/fonts:ro \
  -e LOG_PATH=/var/log/lunchgo \
  -e LOGIN_QUEUE_ENABLED=true \
  -e LOGIN_QUEUE_CAPACITY=10 \
  -e LOGIN_QUEUE_TOKEN_TTL_MS=120000 \
  -e TZ=Asia/Seoul \
  -e JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Seoul \
  -v /opt/scouter/scouter/agent.java:/app/scouter/agent.java:ro \
  -v /opt/scouter/scouter/agent.java/conf/scouter.conf:/app/scouter/conf/scouter.conf:ro \
  -v /var/log/lunchgo:/var/log/lunchgo \
  -p 8080:8080 --name lunchgo-backend -d pgw10243/lunchgo-backend:dev
```

## 비교 테스트 실행 (Queue ON, constant-vus)
```bash
docker run --rm -i grafana/k6 run - \
  -e BASE_URL=http://10.0.2.6:8080 \
  -e EMAIL_PREFIX=loadtest.user \
  -e EMAIL_DOMAIN=example.com \
  -e PASSWORD='Passw0rd!123' \
  -e LOAD_VUS=20 \
  -e LOAD_DURATION=2m \
  -e LOAD_MODE=constant-vus \
  -e USE_LOGIN_QUEUE=true \
  -e LOGIN_QUEUE_POLL_MS=1000 \
  -e LOGIN_QUEUE_MAX_WAIT_MS=60000 \
  < /root/k6_login_loadtest.js
```

## 폴링 튜닝 테스트 실행 순서
1) Redis 큐 정리
```bash
docker exec -e REDISCLI_AUTH=$REDIS_PASS lunchgo-redis redis-cli del login:queue login:queue:seq
docker exec -e REDISCLI_AUTH=$REDIS_PASS lunchgo-redis sh -c 'redis-cli --scan --pattern "login:queue:token:*" | xargs -r redis-cli del'
```

2) k6 실행 (poll=1500ms)
```bash
docker run --rm -i grafana/k6 run - \
  -e BASE_URL=http://10.0.2.6:8080 \
  -e EMAIL_PREFIX=loadtest.user \
  -e EMAIL_DOMAIN=example.com \
  -e PASSWORD='Passw0rd!123' \
  -e LOAD_VUS=20 \
  -e LOAD_DURATION=2m \
  -e LOAD_MODE=constant-vus \
  -e USE_LOGIN_QUEUE=true \
  -e LOGIN_QUEUE_POLL_MS=1500 \
  -e LOGIN_QUEUE_MAX_WAIT_MS=60000 \
  < /root/k6_login_loadtest.js
```

---

## 다음 테스트 계획
### 1) 추가 검증 (선택)
- 목적: VU 상향(예: 50) 또는 `LOAD_DURATION=30m`로 장시간 변동 확인
- 조건: CPU/GC 여유가 허용되는지 확인하면서 비교

---

## 판단 기준 (간단)
- 실패율이 1% 이상이면 원인 분류(4xx vs 5xx) 필요
- One-shot 완주 시간이 길면 `capacity` 또는 `polling` 재조정
- CPU 100% 지속 시 capacity 하향 또는 poll interval 증가 고려
