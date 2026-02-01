# Payment Troubleshooting (PortOne) - Deployment

## 증상
- 결제 화면에서 다음 경고가 표시되고 결제가 진행되지 않음
  - "PortOne 설정값이 누락되었습니다. 관리자에게 문의해 주세요."

## 영향 범위
- 프론트(브라우저)에서 PortOne 결제 호출이 차단됨
- 백엔드는 정상 동작하더라도 결제 요청이 시작되지 않음

## 원인
- 프론트는 `VITE_*` 환경변수를 **빌드 시점**에만 주입받음
- 운영 배포 시 `VITE_PORTONE_STORE_ID`, `VITE_PORTONE_CHANNEL_KEY`가 주입되지 않아
  `import.meta.env` 값이 빈 문자열로 남았음
- 결과적으로 프론트에서 설정 누락 오류가 발생

## 확인 방법
1) 백엔드 환경변수는 정상인지 확인
```bash
docker exec lunchgo-backend env | rg "PORTONE_"
```

2) 프론트에서 사용하는 키 확인
```bash
grep -Rni "VITE_PORTONE" frontend/src
```

3) 결제 화면 코드에서 누락 체크 확인
- `frontend/src/views/restaurant/id/payment/RestaurantPaymentPage.vue`
- `VITE_PORTONE_STORE_ID`, `VITE_PORTONE_CHANNEL_KEY`가 비어 있으면 오류 발생

## 해결 (권장: GitHub Actions에서 주입)
1) GitHub Secrets 등록
- `VITE_PORTONE_STORE_ID`
- `VITE_PORTONE_CHANNEL_KEY`
- `VITE_PORTONE_OPEN_TYPE` (예: `popup`)

2) 워크플로우에 env 주입
`.github/workflows/frontend-deploy.yml`
```yaml
env:
  VITE_API_BASE_URL: http://${{ secrets.SERVER_IP }}
  VITE_PORTONE_STORE_ID: ${{ secrets.VITE_PORTONE_STORE_ID }}
  VITE_PORTONE_CHANNEL_KEY: ${{ secrets.VITE_PORTONE_CHANNEL_KEY }}
  VITE_PORTONE_OPEN_TYPE: ${{ secrets.VITE_PORTONE_OPEN_TYPE }}
```

3) dev 브랜치로 푸시 → 프론트 빌드/배포 자동 실행

## 해결 (로컬 빌드 후 수동 업로드)
1) 빌드 시 환경변수 주입
```bash
cd frontend
export VITE_API_BASE_URL=http://<bastion-ip>
export VITE_PORTONE_STORE_ID=store-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
export VITE_PORTONE_CHANNEL_KEY=channel-key-xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
export VITE_PORTONE_OPEN_TYPE=popup
npm ci
npm run build
```

2) Object Storage 업로드
```bash
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
export BUCKET=lunchgo-test-bucket
export ENDPOINT_URL=http://kr.object.ncloudstorage.com
export REGION=kr-standard

bash scripts/upload_frontend_object_storage.sh
```

## 참고
- `VITE_*` 값은 번들에 포함되므로 민감정보를 넣지 않는다.
- `VITE_PORTONE_SDK_URL`은 기본값(`https://cdn.portone.io/v2/browser-sdk.js`)이 있으므로
  별도 주입 없이도 동작한다.

## 웹훅 트러블슈팅

### 증상
- PortOne 콘솔에서 웹훅 발송 실패 (HTTP 400)
- 서버 로그: `결제 정보를 찾을 수 없습니다.`

### 원인
- 웹훅 테스트 payload의 결제 식별자가 실제 DB 결제 데이터와 불일치
- `/api/payments/portone/requested`가 저장되기 전에 테스트 웹훅이 도착

### 해결
1) 웹훅 URL을 운영 엔드포인트로 변경
   - 예: `http://<bastion-ip>/api/payments/portone/webhook`
2) **실 결제 플로우**로 테스트
   - 프론트에서 결제를 진행하면 `requested -> complete` 흐름이 DB에 저장됨
   - 이후 웹훅이 동일한 식별자로 도착하면 200 응답

### 확인 방법
1) 백엔드 로그에서 서명 검증 확인
   - `Webhook Signature Check`의 Received/Calculated가 일치해야 함
2) DB에 결제 레코드가 존재하는지 확인

### 증상 (로컬에서 로그가 안 찍히는데 상태가 바뀜)
- 로컬 서버 로그에 웹훅 수신 로그가 없음
- DB 상태는 `EXPIRED`/`CANCELLED`로 변경됨

### 원인
- PortOne 웹훅 URL이 배포 환경을 가리키고 있음
- 로컬이 같은 원격 DB를 사용 중이라, 배포 서버가 상태를 갱신해버림

### 해결 (로컬 터널링)
1) 로컬 서버가 동작 중인지 확인 (예: `http://localhost:8080`)
2) ngrok 실행
```bash
ngrok http 8080
```
3) PortOne 웹훅 URL을 ngrok HTTPS 주소로 변경
   - 예: `https://xxxx.ngrok-free.app/api/payments/portone/webhook`
4) 로컬 로그에서 `PortOne webhook received`가 찍히는지 확인

### 증상 (만료 스케줄러 동작 시 DB 부하 증가)
- 결제 만료 스케줄러 실행 시 DB 부하가 증가하거나 쿼리가 과도하게 발생

### 원인
- 만료 대상 예약을 루프에서 처리하면서 각 예약마다
  `findTopByReservationIdOrderByCreatedAtDesc(reservationId)`를 호출함
- 예: 만료 대상이 100건이면
  - 1회: 만료 대상 예약 목록 조회
  - +100회: 각 예약의 최신 결제 조회
  - 총 101회 쿼리가 발생하는 N+1 패턴

### 해결
- 만료 대상 예약 ID 목록을 모아서 **한 번의 쿼리**로 결제 정보를 조회
- 결과를 `reservationId` 기준으로 정렬한 뒤, 메모리에서 첫 번째 결제만 사용해 최신 결제로 매핑
- 즉, 예약 ID들을 자바에서 리스트로 모아 메모리에 올린 뒤 그 리스트를 IN 조건으로 조회한다
- 조회 결과를 `Map<Long, Payment>`에 담아 예약 ID → 최신 결제 1건으로 바로 접근한다
- 이렇게 하면
  - 1회: 만료 대상 예약 목록 조회
  - +1회: 모든 대상 예약의 결제 목록 조회
  - 총 2회 쿼리로 고정되어 N+1 패턴이 제거됨
- JPA 메서드: `findByReservationIdInOrderByReservationIdAscCreatedAtDesc`

```java
import java.util.Objects;

// PaymentRepository: 만료 대상 예약 ID 목록으로 결제 목록을 한 번에 조회
List<Payment> findByReservationIdInOrderByReservationIdAscCreatedAtDesc(List<Long> reservationIds);

// ReservationPaymentExpiryService: 최신 결제를 메모리에서 매핑
private Map<Long, Payment> loadLatestPayments(List<Reservation> reservations) {
    Map<Long, Payment> latestMap = new HashMap<>();
    List<Long> reservationIds = reservations.stream()
        .filter(Objects::nonNull)
        .map(Reservation::getReservationId)
        .filter(Objects::nonNull)
        .toList();
    if (reservationIds.isEmpty()) {
        return latestMap;
    }

    // reservation_id 오름차순 + created_at 내림차순으로 가져오므로
    // 같은 reservation_id 내 첫 번째가 최신 결제
    List<Payment> payments =
        paymentRepository.findByReservationIdInOrderByReservationIdAscCreatedAtDesc(reservationIds);
    for (Payment payment : payments) {
        if (payment == null || payment.getReservationId() == null) {
            continue;
        }
        latestMap.putIfAbsent(payment.getReservationId(), payment);
    }
    return latestMap;
}
```

### 증상 (배포 환경에서 결제 만료가 즉시 발생)
- 배포 환경에서 예약 생성 직후 `EXPIRED`로 바로 변경됨
- `hold_expires_at`가 `created_at`보다 과거로 저장됨

### 원인
- 앱/JVM 타임존이 UTC로 동작하면서 KST 기준 컬럼에 UTC 시간이 저장됨
- 결과적으로 `hold_expires_at`가 현재보다 과거가 되어 만료 스케줄러가 즉시 처리

### 해결
- 배포 컨테이너에 KST 타임존 고정
- `docker run`에 `TZ=Asia/Seoul`, `JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Seoul` 추가
