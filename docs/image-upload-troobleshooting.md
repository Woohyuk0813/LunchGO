# Image Upload Troubleshooting (NCP VPC)

## 목차
1) 업로드 실패 + CORS 에러 (Access-Control-Allow-Origin 중복)
2) 업로드 용량 제한 (1MB로 보이는 현상)
3) Nginx 설정 수정 실패/반영 안됨

## 1) 업로드 실패 + CORS 에러 (중복 헤더)
### 증상
- 브라우저 에러:
  - `Access-Control-Allow-Origin` header contains multiple values
- 요청은 200이지만 브라우저가 CORS로 차단

### 원인
- Nginx가 CORS 헤더를 추가
- 백엔드(WebConfig + SecurityConfig)가 CORS 헤더를 추가
- 결과적으로 동일 헤더가 2번 붙음

### 확인 방법
```bash
curl -i -H "Origin: http://lunchgo-test-bucket.s3-website.kr.object.ncloudstorage.com" \
  http://101.79.9.218/api/login | rg -i "access-control-allow-origin"
```
- 두 줄이면 중복

### 해결 (Nginx에서 CORS 제거, 백엔드만 사용)
`/root/nginx-default.conf`를 아래 내용으로 덮어쓰기:
```nginx
server {
    listen       80;
    server_name  localhost;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }

    location /api/ {
        client_max_body_size 10m;
        proxy_pass http://10.0.2.6:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location = /scouter {
        return 302 /scouter/extweb/index.html;
    }

    location /scouter/ {
        proxy_pass http://10.0.2.6:6180/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}
```

적용:
```bash
docker exec -it my-nginx nginx -t
docker exec -it my-nginx nginx -s reload
```

### 결과
- 로그인 및 이미지 업로드 정상 동작
- `Access-Control-Allow-Origin` 중복 해소

## 2) 업로드 용량 제한 (1MB로 보이는 현상)
### 증상
- 1MB 이상 업로드 시 실패
- 브라우저에서 CORS 에러처럼 보이거나 413이 발생

### 원인
- Nginx 기본 `client_max_body_size`가 1m

### 해결
```nginx
location /api/ {
    client_max_body_size 10m;
    ...
}
```

### 함께 확인할 설정
- Spring Boot:
  - `spring.servlet.multipart.max-file-size=10MB`
  - `spring.servlet.multipart.max-request-size=10MB`
- Object Storage:
  - `ncp.object-storage.max-size-bytes=10485760`

## 3) Nginx 설정 수정 실패/반영 안됨
### 증상
- 컨테이너 내부에서 설정 수정 실패
- 변경했는데 `nginx -T`에 반영되지 않음

### 원인
- 컨테이너가 `/etc/nginx/conf.d/default.conf`를
  `/root/nginx-default.conf`에 read-only bind mount로 사용

### 확인
```bash
docker inspect my-nginx --format '{{json .Mounts}}' | jq .
```

### 해결
- 호스트 파일(`/root/nginx-default.conf`)을 수정 후 reload
```bash
docker exec -it my-nginx nginx -t
docker exec -it my-nginx nginx -s reload
```

### 검증
```bash
docker exec -it my-nginx nginx -T | rg -n "location /api/|client_max_body_size"
```
