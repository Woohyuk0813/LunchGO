# Scouter Setup Troubleshooting

## 핵심 원인 요약
1) **Collector DB lock 문제**
- `lock.dat`가 남아 있어 collector가 정상 기동하지 못함.
- 결과: Web API 연결 reset, webapp 로그인 실패, 오브젝트 미등록.

2) **UDP 6100 포트 매핑 누락**
- `docker-compose.scouter.yml`에서 TCP만 열려 있어 agent 등록/오브젝트 전파가 안 됨.
- 결과: `/scouter/v1/object`가 빈 배열로 반환.

## 추가로 얽힌 문제들
- **alert.plug / ErrorRate.alert 문법 오류**
  - 플러그인 문법 오류로 collector 로그에 `[source error]` 반복.
  - `alert.plug`/`ErrorRate.alert` 비활성화 후 안정화 필요.
- **Web API 포트/URL 불일치**
  - Paper 기본 포트 6188인데 Web API가 6180으로 열려 접속 실패.
  - Web API 포트(6188)와 Paper 설정 일치 필요.
- **ACG/터널 포트 누락**
  - 6188을 ACG 인바운드에 허용하지 않음.
  - SSH 터널에 6188 포트 포워딩 누락.

## 확인 및 복구 체크리스트
### 1) Collector 기동 확인
```bash
tail -n 50 /opt/scouter/scouter/server/nohup.out
```
- `Can't lock the database`가 있으면 아래 수행:
```bash
docker compose -f /opt/scouter/docker-compose.scouter.yml down
sudo rm -f /opt/scouter/scouter/server/database/lock.dat
docker compose -f /opt/scouter/docker-compose.scouter.yml up -d
```

### 2) UDP 6100 포트 매핑
`/opt/scouter/docker-compose.scouter.yml`의 scouter-server ports:
```yaml
ports:
  - "6100:6100"
  - "6100:6100/udp"
  - "6188:6188"
```

### 3) Web API 포트 확인
`/opt/scouter/scouter/server/conf/scouter.conf`:
```
net_http_server_enabled=true
net_http_api_enabled=true
net_http_port=6188
```

### 4) ACG/터널 확인
- ACG 인바운드: TCP 6188 허용 (bastion → private)
- SSH 터널: `-L 6188:10.0.2.6:6188` 포함

### 5) 오브젝트 등록 확인
```bash
curl http://127.0.0.1:6188/scouter/v1/info/server
curl "http://127.0.0.1:6188/scouter/v1/object?serverId=<ID>"
```

### 6) Paper 접속 설정
- URL: `http://localhost:6180/extweb/`
- Server: `http://localhost:6188`

