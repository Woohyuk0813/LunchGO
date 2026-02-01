# 로그인 전용 k6 부하 테스트

## 목적
- 로그인 성능 개선 효과를 로그인 API 단독으로 측정
- 평균/p95/분산을 안정적으로 비교할 수 있도록 샘플 수 확대

## 테스트 전제
- 대상 API: `POST /api/login`
- 테스트 계정: `loadtest.user0001@example.com` 형태
- 실행 위치: bastion host (Docker k6)

## 권장 시나리오
### 기본 시나리오 (단독 로그인)
- VU: 20
- Duration: 2m
- 각 VU는 반복 로그인 수행
- 목표: 평균/p95 안정화

### 강화 시나리오 (스트레스)
- VU: 50
- Duration: 2m
- 목표: CPU/BCrypt 병목 여부 확인

## k6 실행 파라미터
```bash
# 기본 시나리오
docker run --rm -i grafana/k6 run - \
  -e BASE_URL=http://10.0.2.6:8080 \
  -e EMAIL_PREFIX=loadtest.user \
  -e EMAIL_DOMAIN=example.com \
  -e PASSWORD='Passw0rd!123' \
  -e LOAD_VUS=20 \
  -e LOAD_DURATION=2m \
  < /root/k6_login_loadtest.js

# 강화 시나리오
docker run --rm -i grafana/k6 run - \
  -e BASE_URL=http://10.0.2.6:8080 \
  -e EMAIL_PREFIX=loadtest.user \
  -e EMAIL_DOMAIN=example.com \
  -e PASSWORD='Passw0rd!123' \
  -e LOAD_VUS=50 \
  -e LOAD_DURATION=2m \
  < /root/k6_login_loadtest.js
```

## k6 스크립트 (login 전용)
경로: `scripts/k6_login_loadtest.js`
```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://127.0.0.1:8080';
const emailPrefix = __ENV.EMAIL_PREFIX || 'loadtest.user';
const emailDomain = __ENV.EMAIL_DOMAIN || 'example.com';
const password = __ENV.PASSWORD || 'Passw0rd!123';
const vus = Number(__ENV.LOAD_VUS || 10);
const duration = __ENV.LOAD_DURATION || '1m';

export const options = {
  vus,
  duration,
};

export default function () {
  const vuId = __VU.toString().padStart(4, '0');
  const email = `${emailPrefix}${vuId}@${emailDomain}`;
  const payload = JSON.stringify({ email, password });
  const params = { headers: { 'Content-Type': 'application/json' } };

  const res = http.post(`${baseUrl}/api/login`, payload, params);
  check(res, {
    'login status 200': (r) => r.status === 200,
  });

  sleep(1);
}
```

## 결과 기록 가이드
- k6 요약:
  - `http_req_duration` avg/p95
  - `http_req_failed`
- Scouter CSV:
  - `/api/login<POST>`의 평균/p95/분산
- 개선 전/후 비교 표 작성

## 로그인 전용 테스트 결과 (20 VU, 2분)
### 실행 파라미터
```bash
docker run --rm -i grafana/k6 run - \\
  -e BASE_URL=http://10.0.2.6:8080 \\
  -e EMAIL_PREFIX=loadtest.user \\
  -e EMAIL_DOMAIN=example.com \\
  -e PASSWORD='Passw0rd!123' \\
  -e LOAD_VUS=20 \\
  -e LOAD_DURATION=2m \\
  < /root/k6_login_loadtest.js
```

### k6 결과 요약
- iterations: 3,255
- http_req_duration: avg 738.96ms / p95 877.9ms / max 1.42s
- http_req_failed: 0%

### Scouter 요약
| Service | Count | Error | Avg Elapsed(ms) | Avg SQL Time(ms) |
| --- | --- | --- | --- | --- |
| `/api/login<POST>` | 3,255 | 0 | 731 | 6 |
| `BaseMemberService#updateLastLoginAt()` | 3,255 | 0 | 11 | 2 |

## 주의 사항
- 너무 적은 샘플(예: 10회)은 변동성이 커서 개선 효과가 작게 보일 수 있음
- 테스트 중 DB CPU 사용률도 함께 확인(BCrypt 영향 확인용)

## 개선 방향 (CPU 병목 기준)
- **BCrypt cost 조정**: cost를 낮추면 로그인 지연은 줄지만 보안 강도는 약화됨(운영 정책과 합의 필요).
- **CPU 스케일 업**: 코어/클럭 증설이 가장 직접적인 개선책.
- **로그인 트래픽 완화**: 동일 계정 반복 로그인 제한, 재시도 백오프 적용.
- **부하 테스트 분리**: 예약 부하 테스트 시 사전 토큰 발급으로 로그인 비용 분리.
- **지표 모니터링**: `/api/login<POST>` p95/p99와 CPU/GC를 함께 관찰해 개선 효과 판단.
