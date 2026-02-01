# 서버 로그 정책 및 구현

## 목적
- 장애 대응에 필요한 핵심 로그만 남긴다.
- 소용량 서버 디스크 사용량을 엄격히 제한한다.
- 요청 단위 추적을 유지한다.

## 보관/용량 정책
- 보관 기간: 3일
- 총 로그 상한: 200MB
- 롤링 방식: 일 단위 + 파일 크기 기준
- 압축: 롤링된 파일은 gzip 압축

## 로그 분리
- `app.log` (INFO/WARN)
- `error.log` (ERROR 전용)

## 필수 필드(MDC)
- `traceId`
- `userId` (인증된 경우만)
- `method`
- `path`
- `status`
- `latencyMs`

## 구현 내용 (Spring Boot)

### Logback 설정
파일: `src/main/resources/logback-spring.xml`

- prod 프로필: 파일 로그만 기록 (롤링 없음)
- non-prod: 콘솔 로그
- 레벨 정책:
  - `com.example.LunchGo=INFO`
  - `org.springframework=WARN`
  - `org.hibernate=ERROR`

### 요청 로그 필터
파일: `src/main/java/com/example/LunchGo/common/logging/RequestLoggingFilter.java`

- `X-Request-Id`가 있으면 그대로 사용, 없으면 생성
- 모든 요청 종료 시 1줄 로그 기록
- MDC에 요청 컨텍스트를 채워서 패턴에 반영
- 응답 헤더에 `X-Request-Id`를 내려 상관관계 추적 가능

### userId 로그 보강 (필터 순서 + pre-handle)
파일: `src/main/java/com/example/LunchGo/common/config/SecurityConfig.java`

- RequestLoggingFilter를 JwtFilter 뒤에 배치해 인증 후 userId를 읽도록 함
- RequestLoggingFilter에서 필터 진입 시점에도 userId를 MDC에 채움 (중간 로그 반영)

#### 접근 방법
- 방법 1: RequestLoggingFilter에서 pre-handle로 userId 세팅
- 방법 2: JwtFilter에서 인증 성공 시 MDC userId 세팅
- 방법 3: 필터 순서를 보장 (RequestLoggingFilter를 JwtFilter 뒤로)

#### 장단점 요약
- 방법 1: 구현 단순, 중간 로그 개선 / 순서 의존
- 방법 2: 인증 성공 즉시 정확 / MDC clear 책임 분산
- 방법 3: 구조적으로 안정 / 필터 등록 및 순서 관리 필요

#### 적용된 수정 코드 (요약)
```java
// SecurityConfig: JwtFilter 다음에 RequestLoggingFilter 실행
http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
    .addFilterAfter(requestLoggingFilter(), JwtFilter.class);
```

```java
// RequestLoggingFilter: 요청 시작 시점에도 userId 세팅
String preUserId = resolveUserId();
if (preUserId != null) {
    MDC.put("userId", preUserId);
}
```

## 로그 파일 경로
- 기본 경로: `./logs`
- 운영 권장 경로: `/var/log/lunchgo`
- 환경 변수로 변경: `LOG_PATH=/var/log/lunchgo`
- 운영 권장: 호스트 경로를 컨테이너로 마운트

## 로그 로테이트 (호스트)
템플릿: `docs/logrotate-lunchgo.conf`

운영은 logrotate만 사용한다. logback은 롤링 없이 파일에만 기록하고,
회전/압축/보관은 호스트 logrotate가 담당한다.
logrotate 템플릿은 로그별로 size 기준을 다르게 둔다:
- `app.log`: `rotate 3` + `size 20M`
- `error.log`: `rotate 3` + `size 5M`

### logrotate 설정 의미
- `daily`: 하루에 한 번 회전 대상 여부를 평가한다.
- `rotate 3`: 회전된 파일을 최대 3개(3일치)까지만 보관한다.
- `size 20M` / `size 5M`: 파일이 지정 크기를 넘으면 즉시 회전한다.
- `missingok`: 로그 파일이 없어도 에러 없이 넘어간다.
- `notifempty`: 빈 파일이면 회전을 건너뛴다.
- `compress`: 회전된 파일을 gzip으로 압축한다.
- `delaycompress`: 직전 회전 파일은 압축을 다음 회전에 수행한다.
- `copytruncate`: 현재 파일을 복사해 회전하고 원본을 비워, 재기동 없이 계속 기록한다.
- `dateext`: 회전 파일명에 날짜를 붙인다.
- `dateformat -%Y-%m-%d`: 날짜 포맷을 `-YYYY-MM-DD`로 지정한다.

### logback 설정 의미 (운영)
- `FileAppender`: 롤링 없이 지정 파일로만 기록한다.
- `ERROR_FILE`의 `LevelFilter`: ERROR 레벨만 `error.log`에 기록한다.
- `root=INFO`, `org.springframework=WARN`, `org.hibernate=ERROR`: 불필요한 프레임워크 로그를 줄인다.

## 200MB 상한과 실제 사용량
현재 logrotate 설정은 200MB 하드캡이 아니라, 더 보수적으로 낮은 수준에서 로그를 유지하도록 설계되어 있다.

### 최대치 계산(압축 전 기준)
- `app.log`: 현재 20MB + 회전 3개(각 20MB) = 80MB
- `error.log`: 현재 5MB + 회전 3개(각 5MB) = 20MB
- 합계: 약 100MB

실제 디스크 사용량은 gzip 압축으로 더 작아진다.
즉, 정책 상한 200MB보다 충분히 낮은 수준으로 유지된다.

## 로그 확인 방법

### 실시간 로그
```bash
tail -f /var/log/lunchgo/app.log
tail -f /var/log/lunchgo/error.log
```

### 압축된 로그 확인
```bash
zless /var/log/lunchgo/app.log-YYYY-MM-DD.gz
zcat /var/log/lunchgo/error.log-YYYY-MM-DD.gz | less
```

### 운영 선택지 및 장단점

#### 선택지 A: logback 롤링만 사용
- 장점: 애플리케이션 설정만으로 완결, 컨테이너 내부에서 동작, 설정 이원화 없음
- 단점: 호스트 공통 정책 적용이 어려움, 여러 컨테이너 로그를 중앙 관리하기 어려움

#### 선택지 B: logrotate만 사용 (호스트)
- 장점: 호스트에서 일괄 관리 가능, 표준 운영 도구 사용, 컨테이너 교체와 무관
- 단점: 컨테이너 로그 경로 마운트 필요, copytruncate로 순간적인 로그 누락 가능, 설정/운영 분리 필요

#### 선택지 C: logback + logrotate 동시 사용
- 장점: 이중 안전장치로 로그 폭주 시 보호 강화
- 단점: 이중 로테이션으로 파일명/보관이 혼란, 불필요한 I/O 증가

권장: 단일 방식만 사용. 현재 운영은 B를 선택한다.

### 실제 서버 적용 방법 (예시 경로 기준)
1) 로그 디렉터리 생성
```bash
sudo mkdir -p /var/log/lunchgo
sudo chown -R root:root /var/log/lunchgo
```

2) 컨테이너 실행 시 볼륨 마운트
```bash
-v /var/log/lunchgo:/var/log/lunchgo
```

3) logrotate 설정 배치
```bash
sudo cp /path/to/LunchGO/docs/logrotate-lunchgo.conf /etc/logrotate.d/lunchgo
```

4) 설정 테스트 (즉시 실행)
```bash
sudo logrotate -f /etc/logrotate.d/lunchgo
```

5) 스케줄 확인 (기본 cron/주기)
```bash
sudo systemctl status logrotate.timer || true
```

## 체크리스트
- `SPRING_PROFILES_ACTIVE=prod` 적용
- `LOG_PATH=/var/log/lunchgo` 적용
- 컨테이너 볼륨 마운트 적용
- `app.log`에 요청 로그가 남는지 확인
- `error.log`가 ERROR만 포함하는지 확인
- 3일 보관/200MB 상한이 지켜지는지 확인
