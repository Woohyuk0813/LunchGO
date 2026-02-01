# 로그인 부하 테스트 요약 (발표용)

## 1) 목적/환경
- 대상: `POST /api/login`
- 실행 위치: Bastion (Docker k6)
- 비교 축: Queue OFF vs Queue ON

## 2) 시나리오 구성
- Baseline: Queue OFF, constant‑vus, VU 20, 2m
- Queue ON: constant‑vus, VU 20, 2m
  - Poll 1000ms / 1500ms 비교
  - Capacity 10~15 튜닝

### 용어 정리
- Baseline: 기준선(비교용 기본 시나리오)
- Queue OFF/ON: 로그인 대기열 비활성/활성
- constant-vus: 고정 동시 사용자 수로 지속 부하
- VU: Virtual User(가상 사용자)
- 2m: 2분
- Poll: 대기열 상태 조회 간격
- Capacity: 동시 로그인 허용 수(큐 처리 한도)
- tuning: 설정값 조정
- ms: 밀리초

## 3) 결과 요약 (대표 수치)

### Queue OFF (1차)
- k6 avg 743.76ms / p95 880.20ms
- Scouter `/api/login` avg 736ms
- CPU: 거의 100%

### Queue ON (poll=1000ms, capacity=10)
- k6 avg 38.00ms / p95 131.70ms
- Scouter `/api/login` avg 102ms
- CPU: 70~80%

### Queue ON (poll=1500ms, capacity=10)
- k6 avg 28.37ms / p95 72.22ms
- Scouter `/api/login` avg 79ms
- CPU: 50~60%

## 4) 해석
- Queue ON은 로그인 지연과 CPU를 낮추는 대신 처리량이 감소한다.
- Poll 간격을 늘리면 대기 UX는 길어지지만 서버 부하/GET 호출 수는 감소한다.
- 로그인 성능 비교는 k6 전체 흐름보다 Scouter `/api/login` 지표가 더 정확하다.

## 5) 핵심 문장 (발표용)
“큐 적용 시 CPU/지연은 안정화되지만 처리량은 감소했고, 폴링 간격이 UX·처리량에 직접 영향을 줍니다.”
