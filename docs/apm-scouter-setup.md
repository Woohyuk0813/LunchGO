# Scouter APM 설치 및 현황 (Private Server)

## 현재 컨테이너 구성
- backend: `lunchgo-backend` (8080)
- scouter collector: `scouter-server` (6100 TCP/UDP, 6188 Web API)
- scouter webapp: `scouter-webapp` (6180)
- redis: `lunchgo-redis` (6379)
- mysql: `lunchgo-db` (3306)

## 설치 소스
- 아카이브: `/home/scouter-all-2.21.1.tar.gz`
- 설치 루트: `/opt/scouter/scouter`

## Docker Compose
- 파일: `/opt/scouter/docker-compose.scouter.yml`
- scouter-server 포트:
  - `6100:6100`
  - `6100:6100/udp`
  - `6188:6188`
- scouter-webapp 포트:
  - `6180:6180`

## Collector Web API 설정
파일: `/opt/scouter/scouter/server/conf/scouter.conf`
```
net_http_server_enabled=true
net_http_api_enabled=true
net_http_port=6188
```

## Web UI 및 Paper
- webapp: `http://localhost:6180/extweb/`
- Paper 리소스: `/opt/scouter/scouter/webapp/extweb`
- Web API 기준 주소: `http://localhost:6188`

## Host Agent 설치 (Docker)
MySQL/Redis 컨테이너의 CPU/IO를 보기 위해 Host Agent가 필요하며, 서버에 1회 실행한다.

1) host agent 확인
```bash
ls /opt/scouter/scouter/agent.host
```

2) host agent 컨테이너 실행
```bash
docker rm -f scouter-host || true

docker run -d --name scouter-host --restart=always \
  --pid=host --net=host \
  -v /proc:/host/proc:ro \
  -v /sys:/host/sys:ro \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  -v /opt/scouter/scouter/agent.host:/scouter/agent.host \
  eclipse-temurin:11-jre \
  java -Xms128m -Xmx128m \
    -Dscouter.config=/scouter/agent.host/conf/scouter.conf \
    -Dscouter.host.proc.dir=/host/proc \
    -Dscouter.host.sys.dir=/host/sys \
    -cp /scouter/agent.host/scouter.host.jar \
    scouter.boot.Boot
```

3) 최소 설정 (`/opt/scouter/scouter/agent.host/conf/scouter.conf`)
```
net_collector_ip=127.0.0.1
net_collector_udp_port=6100
net_collector_tcp_port=6100
obj_name=private-webserver-host
```

4) 로그 확인
```bash
docker logs --tail=100 scouter-host
```

## 터널/ACG 요약
### SSH 터널 (로컬)
```
ssh -i /Users/user/Documents/key/lunchgo.pem \
  -o ExitOnForwardFailure=yes \
  -L 6100:10.0.2.6:6100 \
  -L 6180:10.0.2.6:6180 \
  -L 6188:10.0.2.6:6188 \
  root@101.79.9.218 -N
```

### ACG 인바운드 (private server)
- TCP `6100`, `6180`, `6188` 허용 (bastion ACG 또는 bastion private IP/CIDR)
- UDP `6100` 허용 (bastion ACG 또는 bastion private IP/CIDR)

## 결과
- Host agent 정상 기동 (Scouter Host Agent Version 2.21.1)
- Scouter Web에서 `private-webserver-host`로 Host/Container 메트릭 확인 가능

## 운영 체크리스트
- collector DB lock 해제 여부 (`lock.dat` 없음)
- scouter-server 포트 리슨: TCP/UDP 6100, Web API 6188
- scouter-webapp 포트 리슨: 6180
- ACG 인바운드: TCP 6100/6180/6188, UDP 6100
- SSH 터널: 6100/6180/6188 포워딩
- Web API 응답 확인: `/scouter/v1/info/server`
- Paper 접속: `/extweb/`에서 서버 `localhost:6188` 설정
- 오브젝트 등록 확인: `/scouter/v1/object?serverId=<ID>`

### 체크리스트 확인 명령어
```bash
# collector DB lock
ls -l /opt/scouter/scouter/server/database/lock.dat

# 포트 리슨 확인
sudo ss -lntp | grep -E '6100|6180|6188'
sudo ss -lunp | grep 6100

# Web API 응답
curl http://127.0.0.1:6188/scouter/v1/info/server

# 오브젝트 등록 확인 (serverId는 위 응답에서 확인)
curl "http://127.0.0.1:6188/scouter/v1/object?serverId=<ID>"

# Paper 설정 검증 (브라우저 기준 주소 확인)
curl -I http://127.0.0.1:6180/extweb/
curl http://127.0.0.1:6188/scouter/v1/info/server
```

## 서버 주소/포트가 잘못됐을 때 증상
- Paper 접속 시 `CAN'T CONNECT TO SERVER` 메시지 표시
- 브라우저 URL이 `...:6188` 대신 `...:6180` 또는 반대로 고정 리다이렉트
- `curl http://127.0.0.1:6188/scouter/v1/info/server`가 `Connection refused`
- Scouter Web 로그인 직후 오브젝트 목록이 비어 있음

## 원인별 대응
### 1) Web API 포트 불일치
- 증상: `6188` 연결 실패, Paper가 6180으로만 접근
- 대응: `scouter.conf`의 `net_http_port`를 `6188`로 맞추고 재기동

### 2) ACG/터널 누락
- 증상: bastion에서 6188 연결 실패, 로컬 터널에서 6188 접속 불가
- 대응: ACG 인바운드에 TCP 6188 추가, SSH 터널에 `-L 6188:10.0.2.6:6188` 추가

### 3) Collector DB lock
- 증상: `Connection reset by peer`, `Can't lock the database` 로그
- 대응: 컨테이너 down → `lock.dat` 삭제 → 재기동

### 4) UDP 6100 매핑 누락
- 증상: `/scouter/v1/object`가 빈 배열
- 대응: `6100/udp` 포트 매핑 추가 후 재기동

## 정상 상태 체크 예시 출력
### Web API 응답
```bash
$ curl http://127.0.0.1:6188/scouter/v1/info/server
{"status":"200","requestId":"#aok0","resultCode":"0","message":"success","result":[{"id":"-1082951330","name":"scouter-server","connected":true,"serverTime":"...","version":"2.21.1"}]}
```

### 오브젝트 목록
```bash
$ curl "http://127.0.0.1:6188/scouter/v1/object?serverId=-1082951330"
{"status":"200","requestId":"#chd324","resultCode":"0","message":"success","result":[{"objType":"linux","objName":"/private-webserver-host","alive":true},{"objType":"tomcat","objName":"/<container-id>/lunchgo-backend","alive":true}]}
```

## Paper 연결 성공 화면 캡처 포인트
- 상단 **SERVER NAVIGATOR**에 `localhost:6188` 서버가 표시됨
- **OBJECTS**에서 `lunchgo-backend`(tomcat)가 선택되어 있음
- 중앙 대시보드에 TPS/응답시간 그래프가 갱신됨

## 단계별 체크 스크립트
### 1) 포트/프로세스 확인
```bash
sudo ss -lntp | grep -E '6100|6180|6188'
sudo ss -lunp | grep 6100
docker ps | grep scouter
```

### 2) Web API 상태 확인
```bash
curl http://127.0.0.1:6188/scouter/v1/info/server
```

### 3) 에이전트 재등록 확인
```bash
docker restart lunchgo-backend
sleep 5
curl "http://127.0.0.1:6188/scouter/v1/object?serverId=<ID>"
```

### 4) 터널/접속 확인 (로컬)
```bash
curl http://localhost:6188/scouter/v1/info/server
curl -I http://localhost:6180/extweb/
```

## Java Agent XLog 강화 옵션
아래 옵션은 XLog에서 SQL/Redis 오류 정보를 더 자세히 보이도록 하는 최소 설정이다.
로그/메모리 사용량 증가가 큰 옵션은 제외했다.

```properties
# SQL/Redis 오류 시 스택 수집
profile_fullstack_sql_error_enabled=true
profile_fullstack_sql_commit_enabled=true
profile_fullstack_redis_error_enabled=true

# SQL 파라미터 표시 범위
trace_sql_parameter_max_length=100
_trace_sql_parameter_max_count=256

# 느린 SQL 기준(에러 마킹 기준)
xlog_error_sql_time_max_ms=3000
```

| 옵션 | 역할 |
| --- | --- |
| `profile_fullstack_sql_error_enabled` | SQL 실행 오류가 발생했을 때 스택을 XLog에 남긴다. |
| `profile_fullstack_sql_commit_enabled` | 커밋 오류가 발생했을 때 스택을 XLog에 남긴다. |
| `profile_fullstack_redis_error_enabled` | Redis 오류가 발생했을 때 스택을 XLog에 남긴다. |
| `trace_sql_parameter_max_length` | 바인딩 파라미터 문자열 최대 길이 제한. |
| `_trace_sql_parameter_max_count` | XLog에 표시할 SQL 파라미터 최대 개수. |
| `xlog_error_sql_time_max_ms` | 이 시간 이상인 쿼리를 XLog 에러로 마킹한다. |

## 환경 변수/플래그 정리
`SCOUTER_OBJ_NAME` 및 `-Dscouter.obj_name`은 사용하지 않으며, 현재는 `scouter.conf`의 `obj_name`이 최종 적용된다.
| 구분 | 이름 | 예시 값 | 설명 |
| --- | --- | --- | --- |
| 시스템 프로퍼티 | `-Dscouter.config` | `/app/scouter/conf/scouter.conf` | 에이전트 설정 파일 경로 |
| 설정 | `scouter.conf: obj_name` | `lunchgo-backend` | 에이전트 객체명(기본값) |
| 설정 | `net_collector_ip` | `10.0.2.6` | collector IP |
| 설정 | `net_collector_tcp_port` | `6100` | collector TCP 포트 |
| 설정 | `net_collector_udp_port` | `6100` | collector UDP 포트 |
| 설정 | `net_http_port` | `6188` | collector Web API 포트 |

## 로컬/운영 값 매핑 표
| 항목 | 로컬 (터널 기준) | 운영 (VPC 내부) |
| --- | --- | --- |
| Scouter Webapp | `http://localhost:6180` | `http://10.0.2.6:6180` |
| Scouter Web API | `http://localhost:6188` | `http://10.0.2.6:6188` |
| Collector TCP | `localhost:6100` | `10.0.2.6:6100` |
| Collector UDP | `localhost:6100/udp` | `10.0.2.6:6100/udp` |
