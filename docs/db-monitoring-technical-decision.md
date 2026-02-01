# DB 모니터링 기술적 의사결정 요약

## 목적
LunchGO의 DB/Redis 모니터링 체계를 구축하기 위해 Scouter(APM)와 역할을 분리한
지표 기반 모니터링 도구를 선정하고, 그 근거를 정리한다.

## 배경/요구 사항
- MySQL/Redis의 상태(QPS, 커넥션, 캐시 히트율, CPU/IO)를 지속 관측
- Private Subnet 환경에서 외부 전송 없이 자체 호스팅 가능해야 함
- 운영 비용을 예측 가능하게 통제해야 함
- Scouter는 요청/트레이스 중심이므로 DB 레벨 지표는 별도 체계 필요

## 비교 대상(대표)
### 1) SaaS 모니터링 (CloudWatch/Datadog 등)
- 장점: 빠른 도입, 관리 부담 낮음
- 단점: 비용 증가 가능, Private Subnet 데이터 외부 전송 필요

### 2) Prometheus + Grafana (선정)
- 장점
  - Exporter 기반으로 MySQL/Redis 지표 수집 표준화
  - 자체 호스팅으로 Private Subnet 내부에서 완결
  - Grafana 템플릿으로 구축 속도와 확장성 확보
  - 비용 통제 용이
- 단점
  - 알람/대시보드 초기 설정 필요
  - APM 수준의 트레이스는 제공하지 않음

### 3) Scouter 단일 사용
- 장점: 단일 도구로 운영 단순화
- 단점: DB/Redis 지표 수집·시각화 범위가 제한적

## 의사결정 핵심 요약
1) **역할 분리**
   - Scouter는 요청/트레이스, Prometheus는 DB/인프라 지표를 담당.
2) **표준화된 지표 수집**
   - Exporter로 MySQL/Redis 핵심 지표를 일관되게 수집 가능.
3) **Private Subnet 적합성**
   - Bastion 기반 자체 호스팅으로 외부 전송 없이 운영 가능.
4) **비용/운영 통제**
   - OSS 기반으로 비용을 예측 가능하게 유지.

## 현재 구성 요약
- Bastion: Prometheus + Grafana
- Private Server: MySQL/Redis Exporter
- 수집 주기: 15s
- 접근: Bastion 공인 IP에서 3000/9090 접속

## Scouter와의 구분
- Scouter: 요청 단위 XLog/SQL/트레이스 중심, APM 역할
- Prometheus/Grafana: 시스템/DB 지표 수집 및 시각화 역할
- 결론: 성격이 다른 지표를 분리해 운영 효율을 높임

## 참고 문서
- `docs/db-monitoring-implementation.md`
- Scouter 의사결정: `docs/scouter-technical-decision.md`
