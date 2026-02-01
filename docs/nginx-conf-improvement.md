# Nginx 설정 개선 기록

## 목표
- API 요청 성능 개선(keep-alive, gzip)
- CORS preflight(OPTIONS) 처리 최적화
- Object Storage에서의 cross-origin 호출 안정화

## 적용 환경
- NCP VPC Bastion Host
- Docker 컨테이너: `my-nginx`
- 호스트 바인드 경로: `/root/nginx-default.conf` → 컨테이너 `/etc/nginx/conf.d/default.conf`
- 적용 대상 도메인: `http://lunchgo.s3-website.kr.object.ncloudstorage.com`

## 최종 설정 (default.conf)
```nginx
map $http_origin $cors_origin {
  default "";
  "~^https?://lunchgo\\.s3-website\\.kr\\.object\\.ncloudstorage\\.com$" $http_origin;
}

upstream backend {
  server 10.0.2.6:8080;
  keepalive 64;
}

upstream scouter_backend {
  server 10.0.2.6:6180;
  keepalive 16;
}

gzip on;
gzip_comp_level 4;
gzip_min_length 1024;
gzip_types application/json text/plain text/css application/javascript;
gzip_vary on;

server {
  listen       80;
  server_name  localhost;

  location / {
    root   /usr/share/nginx/html;
    index  index.html index.htm;
  }

  location /api/ {
    client_max_body_size 10m;

    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_connect_timeout 2s;
    proxy_send_timeout 30s;
    proxy_read_timeout 30s;

    proxy_pass http://backend;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;

    # OPTIONS는 막아서 preflight 전용 위치로 위임
    limit_except GET POST PUT PATCH DELETE HEAD {
      deny all;
    }
  }

  # preflight 전용 처리 (여기서만 CORS 헤더 부여)
  error_page 403 = @cors_preflight;
  location @cors_preflight {
    add_header Access-Control-Allow-Origin $cors_origin always;
    add_header Access-Control-Allow-Credentials "true" always;
    add_header Access-Control-Allow-Methods "GET, POST, PUT, PATCH, DELETE, OPTIONS" always;
    add_header Access-Control-Allow-Headers "Authorization, Content-Type, X-Requested-With" always;
    add_header Access-Control-Max-Age 86400 always;
    add_header Vary Origin always;
    add_header Content-Length 0;
    return 204;
  }

  location = /scouter {
    return 302 /scouter/extweb/index.html;
  }

  location /scouter/ {
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_pass http://scouter_backend/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
  }

  error_page 500 502 503 504 /50x.html;
  location = /50x.html {
    root /usr/share/nginx/html;
  }
}
```

## 설정 항목 설명

### 1) CORS Origin 매핑
```nginx
map $http_origin $cors_origin {
  default "";
  "~^https?://lunchgo\\.s3-website\\.kr\\.object\\.ncloudstorage\\.com$" $http_origin;
}
```
- 의미: 허용된 Origin만 CORS 응답에 반영.
- 이유: 허용되지 않은 Origin에는 빈 문자열을 반환하여 CORS 헤더를 주지 않음.
- 포인트: 정규식으로 도메인 정확히 매칭(https/http 모두 허용).
- 기대 효과: 비인가 Origin 차단으로 보안 강화, CORS 오류 감소.

### 2) Upstream + keepalive
```nginx
upstream backend { server 10.0.2.6:8080; keepalive 64; }
upstream scouter_backend { server 10.0.2.6:6180; keepalive 16; }
```
- 의미: Nginx ↔ WAS/Scouter 연결 재사용.
- 효과: 연결 재설정 비용 감소, 지연 감소.
- 값: API 트래픽이 높아 `64`로, Scouter는 낮아 `16`으로 설정.
- 기대 효과: 피크 트래픽 시 신규 커넥션 생성 부담 완화, 응답 지연 감소.
- 설명: keepalive는 매 요청마다 새 TCP 연결을 만들지 않고 기존 연결을 재사용한다는 뜻이다. 그 결과 3-way handshake 비용이 줄고, 커널/소켓 관리 비용도 감소해 RTT와 CPU 부담이 함께 낮아진다.

### 3) gzip 압축
```nginx
gzip on;
gzip_comp_level 4;
gzip_min_length 1024;
gzip_types application/json text/plain text/css application/javascript;
gzip_vary on;
```
- 의미: JSON/텍스트 리소스 압축 전송.
- 값 설명:
  - `gzip_comp_level 4`: CPU 과부하 없이 적절한 압축률.
  - `gzip_min_length 1024`: 1KB 미만 응답은 압축하지 않음(효율 최적화).
  - `gzip_vary on`: `Vary: Accept-Encoding` 추가로 캐시 안정화.
- 기대 효과: 응답 페이로드 감소, 네트워크 전송 시간 단축.

### 4) API 프록시 타임아웃
```nginx
proxy_connect_timeout 2s;
proxy_send_timeout 30s;
proxy_read_timeout 30s;
```
- 의미: 연결/전송/응답 지연 시 적절히 종료.
- 값: 일반 API 응답 패턴 기준.
- 효과: 무한 대기 방지, 리소스 회수.
- 기대 효과: 장애 시 연결 고착 방지, 워커 점유 시간 감소.
- 설명: 백엔드와의 연결/응답이 일정 시간 이상 지연되면 요청을 종료해 워커가 오래 붙잡히는 상황을 방지한다. 느린 요청이 누적될 때도 전체 처리량을 안정적으로 유지하는 데 도움이 된다.
- timeout별 의미:
  - `proxy_connect_timeout`: 백엔드 연결이 이 시간 내에 안 되면 중단.
  - `proxy_read_timeout`: 백엔드 응답이 이 시간 내에 안 오면 중단.
  - `proxy_send_timeout`: 요청 전달이 이 시간 내에 안 되면 중단.

### 5) preflight 처리 (if 없는 방식)
```nginx
location /api/ {
  limit_except GET POST PUT PATCH DELETE HEAD {
    deny all;
  }
}
error_page 403 = @cors_preflight;
location @cors_preflight {
  ...
  return 204;
}
```
- 의미: OPTIONS 요청을 403으로 유도해 preflight 전용 위치에서 204 응답.
- 이유:
  - `if`/`return` 문법 이슈 제거.
  - 일반 요청에는 **백엔드 CORS 헤더만** 사용하여 중복 헤더 방지.
- 효과: CORS preflight 캐싱(1일)으로 OPTIONS 요청 감소.
- 기대 효과: OPTIONS 요청 응답 지연 감소, 백엔드 CORS 중복 헤더 방지.
- 설명: CORS 헤더는 preflight(OPTIONS)에서만 Nginx가 추가하고, 실제 GET/POST 요청은 WAS가 CORS 헤더를 내려준다. 따라서 동일 응답에 헤더가 두 번 붙는 문제가 해소된다.

### 6) CORS 헤더 값 설명
```nginx
Access-Control-Allow-Origin: 허용 도메인
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type, X-Requested-With
Access-Control-Max-Age: 86400
```
- `Max-Age=86400`: 브라우저가 24시간 preflight 결과 캐시.
- `Allow-Credentials`: 쿠키 기반 인증 시 필수.
- 기대 효과: preflight 요청 빈도 감소, 로그인/예약 등 인증 요청 안정화.

## 적용 절차
1) **인플레이스 덮어쓰기**
```bash
sudo tee /root/nginx-default.conf > /dev/null <<'EOF'
(위 최종 설정)
EOF
```
2) **컨테이너 재시작**
```bash
docker restart my-nginx
```

## 검증 방법

### 1) 설정 반영 확인
```bash
docker exec my-nginx nginx -T | rg -n "map \\$http_origin|upstream backend|gzip on|Access-Control-Max-Age"
```

### 2) preflight 동작 확인
```bash
curl -i -X OPTIONS http://101.79.9.218/api/login/queue \
  -H "Origin: http://lunchgo.s3-website.kr.object.ncloudstorage.com" \
  -H "Access-Control-Request-Method: POST"
```
- 기대 결과: `HTTP/1.1 204` + `Access-Control-Max-Age: 86400`

### 3) gzip 적용 확인
```bash
curl -I --compressed http://101.79.9.218/api/business/restaurants/96/stats/weekly \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```
- `Content-Encoding: gzip` 확인
- 응답이 작으면 gzip이 붙지 않을 수 있음(`gzip_min_length=1024`).

## 주의사항
- 일반 요청에 Nginx에서 CORS 헤더를 추가하면 백엔드와 중복될 수 있음.
- `map`에서 허용되지 않은 Origin은 빈 값으로 처리되므로 CORS 실패가 정상 동작.
- `/root/nginx-default.conf`는 **bind 마운트 + read-only**이므로 컨테이너 내부 수정은 반영되지 않음.

## 개선 효과 요약
- CORS 중복 헤더 제거로 로그인/예약 API의 브라우저 차단 오류 해소.
- preflight 캐시(`Access-Control-Max-Age=86400`)로 OPTIONS 요청 빈도 감소.
- upstream keepalive로 API 커넥션 재사용 → RTT/CPU 부담 감소.
- gzip 적용으로 JSON/텍스트 응답 크기 감소 → 전송 시간 단축.
- proxy 타임아웃 명시로 지연/고착 연결 회수 → 워커 점유 시간 감소.
