# Payment Redirection Troubleshooting (PortOne)

## 증상
- 결제 완료 후 예약 확정 페이지로 리다이렉션되지 않음
- 개발자 도구를 열면 정상 리다이렉션되지만 닫으면 지연/정지되는 것처럼 보임

## 영향 범위
- 프론트 결제 완료 흐름이 간헐적으로 끊겨 확정 페이지로 이동하지 못함
- 백엔드 결제 완료(`/api/payments/portone/complete`)는 정상 처리될 수 있음

## 원인
- PortOne SDK 콜백/리다이렉트 타이밍이 브라우저 상태(DevTools on/off)에 따라 달라짐
- 리다이렉트 파라미터가 없는 상태에서 `completePaymentFromRedirect()`가 실행되며 경고 로그 발생

## 확인 방법
1) 결제 요청 파라미터 로그 확인
- `frontend/src/views/restaurant/id/payment/RestaurantPaymentPage.vue`
- `[PortOne] requestPayment params`에 `redirectUrl`과 `redirected=1` 포함 여부

2) 확정 페이지 쿼리 파라미터 확인
- `frontend/src/views/restaurant/id/confirmation/RestaurantConfirmationPage.vue`
- `[PortOne] confirmation route.query`에 `reservationId`, `paymentId`, `totalAmount`, `redirected=1` 포함 여부

3) 결제 완료 API 호출 확인
- 네트워크 탭에서 `/api/payments/portone/complete` 상태 확인

## 해결
### 1) Redirect 플로우 고정
- `VITE_PORTONE_OPEN_TYPE=redirect`로 설정
- 리다이렉트 URL에 `redirected=1` 추가

### 2) Redirect 감지 조건 추가
- `redirected=1`인 경우에만 `completePaymentFromRedirect()` 실행

### 3) 콜백/리다이렉트 실패 대비 폴링 추가
- 결제 시작 직후 confirmation API 폴링
- 결제 완료 확인 시 확정 페이지로 이동


## 적용 파일
- `frontend/src/views/restaurant/id/payment/RestaurantPaymentPage.vue`
- `frontend/src/views/restaurant/id/confirmation/RestaurantConfirmationPage.vue`
- `frontend/.env.local`

## 결과
- DevTools on/off 모두에서 결제 완료 후 확정 페이지로 정상 이동
- 콜백/리다이렉트가 실패해도 폴링으로 자동 복구

## 참고
- PortOne 결제 완료는 웹훅으로도 확정되므로, 프론트는 리다이렉션 복구 경로를 보유하는 것이 안전함
- `VITE_*` 값은 번들에 포함되므로 민감정보를 넣지 않는다
