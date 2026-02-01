# Scouter Slack 알림 연동 가이드

## 목적
- Scouter에서 감지한 장애/성능 이상을 Slack으로 즉시 알림한다.
- 운영 중 빠른 인지와 대응 속도를 확보한다.

## 전제 조건
- Scouter Server(collector)와 Webapp이 정상 구동 중
- Slack Incoming Webhook URL 준비

## 적용 방식
- **Scouter Server(collector)에 Slack 플러그인 JAR을 추가**하는 방식
- `alert.plug`는 사용하지 않음 (문법 오류/컴파일 에러 방지 목적)

## 적용 경로(현재 서버 기준)
- JAR 위치: `/opt/scouter/scouter/server/lib`
- 설정 파일: `/opt/scouter/scouter/server/conf/scouter.conf`
- 플러그인 폴더: `/opt/scouter/scouter/server/plugin`

## 설치/적용 절차
1) JAR 배치
- `scouter-plugin-server-alert-slack-1.1.0.jar`
- `gson-2.6.2.jar`
- 플러그인 버전에 따라 아래 의존성 JAR이 추가로 필요할 수 있다.
  - `commons-codec-1.9.jar`
  - `commons-logging-1.2.jar`
  - `httpclient-4.5.2.jar`
  - `httpcore-4.4.4.jar`

2) `alert.plug` 비활성화
```bash
mv /opt/scouter/scouter/server/plugin/alert.plug \
   /opt/scouter/scouter/server/plugin/alert.plug.disabled
```

3) `scouter.conf`에 Slack 플러그인 설정 추가
```ini
# scouter slack plugin configure
ext_plugin_slack_send_alert=true
ext_plugin_slack_debug=true
ext_plugin_slack_level=1
ext_plugin_slack_webhook_url=YOUR_WEBHOOK_URL
ext_plugin_slack_channel=C0A0V8Y8S7N
ext_plugin_slack_botName=scouter
ext_plugin_slack_icon_emoji=:computer:
ext_plugin_slack_icon_url=http://XXX.XXX.XXX/XXX.gif
ext_plugin_slack_xlog_enabled=true
ext_plugin_slack_object_alert_enabled=true

ext_plugin_elapsed_time_threshold=5000
ext_plugin_gc_time_threshold=5000
ext_plugin_thread_count_threshold=300
```

4) 재기동
```bash
docker compose -f /opt/scouter/docker-compose.scouter.yml down
sudo rm -f /opt/scouter/scouter/server/database/lock.dat
docker compose -f /opt/scouter/docker-compose.scouter.yml up -d
```
`lock.dat`은 Scouter DB 락 파일로, 비정상 종료 시 남아 있으면 서버가 "Can't lock the database"로 기동 실패할 수 있어 재기동 전에 제거한다.

## 알림 조건(현재 설정 기준)
- **오브젝트 상태 변화**  
  `ext_plugin_slack_object_alert_enabled=true`  
  → 오브젝트 연결/해제 시 알림 전송

- **느린 요청(XLog)**  
  `ext_plugin_slack_xlog_enabled=true`  
  `ext_plugin_elapsed_time_threshold=5000`  
  → 요청 처리 시간이 **5초 이상**이면 알림 전송

- **GC 지연**  
  `ext_plugin_gc_time_threshold=5000`  
  → GC가 **5초 이상**이면 알림 전송

- **스레드 과다**  
  `ext_plugin_thread_count_threshold=300`  
  → 스레드 수가 **임계치 이상**이면 알림 전송

- **레벨 필터**  
  `ext_plugin_slack_level=1`  
  → 플러그인 레벨 기준에 맞는 알림만 전송

## 기본 제공 알림 유형(참고)
- CPU 경고/치명
- Memory 경고/치명
- Disk 사용량 경고/치명
- 신규 Agent 연결
- Agent 연결 해제
- Agent 재연결

## 테스트 방법
### 빠른 트리거(임시로 낮추기)
```ini
ext_plugin_slack_level=0
ext_plugin_elapsed_time_threshold=1
ext_plugin_gc_time_threshold=1
```
재기동 후 요청 1~2건을 보내면 Slack 알림이 도착한다.

### 오브젝트 이벤트 트리거
```bash
docker restart lunchgo-backend
```

## 확인 커맨드
### 플러그인 로드 확인
```bash
grep -n "SlackPlugin" /opt/scouter/scouter/server/logs/server-*.log | tail -n 20
```

### 전송 실패 원인 확인
```bash
grep -n "slack" /opt/scouter/scouter/server/logs/server-*.log | tail -n 20
```

## 문제별 증상 및 대응
### `channel_not_found` 발생
- 원인: 채널명이 깨져 전달되거나 Webhook이 채널 권한이 없음
- 대응: **채널 ID** 사용
```ini
ext_plugin_slack_channel=C0A0V8Y8S7N
```

### Slack 알림이 전혀 안 옴
- 원인: 설정이 컨테이너 내부에 반영되지 않음
- 대응:
```bash
docker exec -it scouter-server sh -lc 'grep -n "ext_plugin_slack" /opt/scouter/server/conf/scouter.conf'
```

### 플러그인 로드는 되었지만 전송 로그가 없음
- 원인: 트리거 조건 미충족
- 대응: 테스트용 임계치로 낮춘 뒤 재확인

## 운영 팁
- 정상 운영 복귀 시 임계치 원복 필수
  - `ext_plugin_elapsed_time_threshold=5000`
  - `ext_plugin_gc_time_threshold=5000`
- 알림 채널은 **채널 ID** 사용을 권장
