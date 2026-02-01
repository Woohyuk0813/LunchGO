# Scouter 선택 기술적 의사결정 요약

## 목적
LunchGO 운영 모니터링을 위해 APM 도입이 필요했고, 후보 도구를 비교한 뒤
Scouter를 선택한 근거와 트레이드오프를 정리한다.

## 배경/요구 사항
- Private Subnet의 WAS, DB, Redis 상태를 관측해야 함
- 로그인/예약 등 주요 API의 지연 원인을 XLog/SQL 레벨로 추적 필요
- Bastion 기반 운영(SSH 터널 포함)과 NCP VPC 구조에 맞는 구성 필요
- 비용/운영 부담이 과도하지 않아야 함
- 장애/성능 이벤트 알림(Slack 연동) 필요

## 비교 대상(대표)
아래 도구는 실제 운영 환경에서 고려 가능한 대표군으로 비교 기준을 정리한다.

### 1) SaaS APM (Datadog/New Relic 등)
- 장점
  - 설치/대시보드 구성 간편
  - 풍부한 UI, 자동 인사이트 제공
- 단점
  - 비용 증가 가능(트래픽/호스트 수 기준 과금)
  - Private Subnet에서 외부 SaaS로 데이터 전송 필요
  - 네트워크/보안 정책 제약이 있을 수 있음

### 2) Prometheus + Grafana
- 장점
  - 메트릭 수집/시각화에 강점
  - 비용 효율적, OSS 생태계 풍부
- 단점
  - APM 레벨의 트레이스/XLog를 직접 제공하지 않음
  - 애플리케이션 요청 단위 분석에는 추가 도구 필요

### 3) 기타 OSS APM (Pinpoint/Elastic APM 등)
- 장점
  - 자체 호스팅 가능
  - APM 기능 제공(트레이스/호출 그래프)
- 단점
  - 설치/운영 복잡도 증가 가능
  - JVM 버전/에이전트 호환성 검증 필요

### 4) Scouter (선정)
- 장점
  - OSS, 자체 호스팅 가능
  - JVM/HTTP 트레이스, SQL 시간, XLog 제공
  - Collector/Webapp 구조로 Private Subnet 구성 적합
  - Slack 플러그인으로 알림 연동 가능
  - Bastion에서 SSH 터널 또는 Nginx 프록시로 접근 가능
- 단점
  - 초기 설정(포트/UDP/락 파일) 주의 필요
  - SaaS 대비 UI/자동 분석 기능은 제한적

## 의사결정 핵심 요약
1) **APM 기능 필요**
   - 단순 메트릭이 아니라 요청 단위 지연/SQL 확인이 필요해 APM이 필수였다.
2) **Private Subnet 중심 운영**
   - 외부 SaaS로의 데이터 전송 제약을 피하고 자체 호스팅이 가능한 도구가 유리했다.
3) **비용/운영 부담**
   - OSS 기반으로 비용을 통제하고, Bastion 기반 운영과 맞는 구성이 필요했다.
4) **알림/운영 연계**
   - Slack 알림 연동이 가능한 도구가 필요했고 Scouter 플러그인이 이를 충족했다.

## Scouter 적용 범위
- WAS APM (XLog, SQL 시간, API 지연 분석)
- Host/Container 메트릭 (Host Agent)
- Slack 알림 연동 (지연/GC/스레드 임계치)
- SSH 터널 및 /scouter 프록시로 접근

## 비교 표(요약)
| 기준 | SaaS APM | Prometheus + Grafana | 기타 OSS APM | Scouter |
| --- | --- | --- | --- | --- |
| APM 트레이스/XLog | 강함 | 약함 | 가능 | 가능 |
| 요청 단위 SQL/지연 | 가능 | 제한적 | 가능 | 가능 |
| 자체 호스팅 | 제한적 | 가능 | 가능 | 가능 |
| Private Subnet 적합성 | 보통 | 높음 | 보통 | 높음 |
| 비용 통제 | 낮음 | 높음 | 높음 | 높음 |
| 설치/운영 난이도 | 낮음 | 중간 | 높음 | 중간 |
| 알림 연동 | 강함 | 중간 | 중간 | 가능(Slack 플러그인) |
| 선택 근거 부합 | 일부 | 일부 | 일부 | 높음 |

## 참고 문서
- `docs/apm-scouter-setup.md`
- `docs/scouter-setup-troobleshooting.md`
- `docs/scouter-slack-alerting.md`
- `docs/reservation-confirmation-scouter-alarm.md`
- DB 모니터링 의사결정: `docs/db-monitoring-technical-decision.md`
