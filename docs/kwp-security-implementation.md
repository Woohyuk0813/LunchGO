# 스프링 시큐리티 적용 분석 및 구현 계획

## 분석 요약 (현재 상태)

- 인증 방식: JWT Access Token(헤더), Refresh Token(HttpOnly 쿠키)
- 필터/설정
  - `src/main/java/com/example/LunchGo/common/config/SecurityConfig.java`
  - `src/main/java/com/example/LunchGo/account/helper/JwtFilter.java`
  - `src/main/java/com/example/LunchGo/common/exception/JwtAuthenticationEntryPoint.java`
  - `src/main/java/com/example/LunchGo/common/exception/JwtAccessDeniedHandler.java`
- 토큰 유틸/저장
  - `src/main/java/com/example/LunchGo/common/util/TokenUtils.java` (Redis에 refresh 저장)
  - `src/main/java/com/example/LunchGo/common/util/HttpUtils.java` (Authorization/쿠키 처리)
- 로그인/갱신 흐름
  - 로그인: `/api/login` -> accessToken 응답 + refreshToken 쿠키 저장
  - 갱신: `/api/refresh` -> accessToken 재발급
  - 로그아웃: `/api/logout` -> Redis refresh 삭제 + 쿠키 제거
- 프론트 동작
  - `frontend/src/router/httpRequest.js`에서 accessToken 부착, 401 시 `/api/refresh` 자동 재발급
  - `frontend/src/router/index.js`에서 meta.roles 기반 라우트 가드 + `/api/auth/check` 호출

## 오늘의 구현 목표

- `api_specification.csv`에 내가 작업한 API들의 보안/권한 정보를 정확히 기재한다.
- 실제 인증 흐름(JWT, refresh cookie)과 권한 체계를 문서/스펙에 반영한다.
- CSV의 “필요 권한” 표기와 `SecurityConfig`의 접근 정책을 일치시키는 기준을 만든다.

## 작업 순서

1) permitAll 후보 확정(Any)  
   - `api_specification.csv`에서 `필요 권한=Any`만 추려 URI 리스트화
   - 컨트롤러 실제 경로와 매칭하여 permitAll 범위 확정

2) 기본 인증 규칙 확정  
   - permitAll 외는 기본 `authenticated()`로 처리
   - 역할 정의: `ROLE_USER`, `ROLE_OWNER`, `ROLE_STAFF`, `ROLE_ADMIN`

3) 역할 매핑 기준 수립  
   - 로그인 필요(Authenticated) vs 역할 제한(ROLE_*) 기준 정의
   - 복수 권한은 `|` 구분 표기 (예: `User|Owner`)

4) `api_specification.csv` 갱신  
   - “필요 권한” 컬럼을 위 기준으로 일관되게 업데이트

5) 보안 설정 정합성 확인  
   - `SecurityConfig`의 requestMatchers/권한 설정이 CSV와 어긋나지 않는지 확인
   - 필요 시 역할별 접근 제어 룰을 추가로 설계(메서드 보안 또는 URL 기반)

## 구현 진행 순서 (백엔드 → 프론트)

1) 백엔드 권한 매핑 적용  
   - `SecurityConfig`에 permitAll/ROLE_* 매핑 확정 적용
   - 401/403 처리 응답 확인

2) 프론트 인증 흐름 점검  
   - `httpRequest` 토큰 부착/리프레시 로직 확인
   - 라우터 `meta.roles`와 API 권한 정책 정합성 확인

3) 스펙 최종 검증  
   - `api_specification.csv` 권한 표기와 실제 보안 설정 일치 여부 확인

## SecurityConfig 적용 목록 (CSV 기준)

- permitAll
  - GET `/api/restaurants/*/reviews`
  - GET `/api/restaurants/*/reviews/*`
  - GET `/api/restaurants/trending`
  - POST `/api/payments/portone/webhook`
  - 기존 공개 경로: `/api/join/**`, `/api/auth/**`, `/api/sms/**`, `/api/login`
- ROLE_USER
  - 리뷰: POST `/api/restaurants/*/reviews`, GET `/api/restaurants/*/reviews/*/edit`, PUT/DELETE `/api/restaurants/*/reviews/*`
  - 이미지/영수증: POST `/api/v1/images/upload/*`, GET `/api/v1/images/presign`, POST `/api/ocr/receipt`
  - 결제/예약: POST `/api/reservations/*/payments`, POST `/api/payments/portone/complete|fail|requested`, POST `/api/reservations/*/payments/expire`, GET `/api/reservations/*/confirmation|summary`
  - 구내식당: POST `/api/cafeteria/menus/ocr|confirm`, GET `/api/cafeteria/menus/week`, GET `/api/cafeteria/recommendations`
  - 즐겨찾기: POST `/api/bookmark-links`, PATCH `/api/bookmark-links/*`, GET `/api/bookmark-links/sent|received|search|search/list`, DELETE `/api/bookmark-links`, PATCH `/api/bookmark/visibility|promotion`, GET `/api/bookmark/shared|list`
- ROLE_OWNER
  - 댓글/블라인드: POST `/api/owners/restaurants/*/reviews/*/comments`, DELETE `/api/owners/restaurants/*/reviews/*/comments/*`, POST `/api/owners/restaurants/*/reviews/*/blind-requests`
  - 사업자 식당 상세: GET `/api/business/restaurants/*`
- ROLE_ADMIN
  - 리뷰 관리: GET `/api/admin/reviews`, PATCH `/api/admin/reviews/*/blind-requests`

## 프론트 인증 대상 파일 (CSV 기준)

- 인증 필요(ROLE_USER)
  - `frontend/src/composables/useCafeteriaRecommendation.js`
  - `frontend/src/composables/useBookmarkShare.js`
  - `frontend/src/views/restaurant/id/reviews/WriteReviewPage.vue`
  - `frontend/src/views/restaurant/id/payment/RestaurantPaymentPage.vue`
  - `frontend/src/views/restaurant/id/confirmation/RestaurantConfirmationPage.vue`
  - `frontend/src/views/my-reservations/ReservationCancelView.vue`
  - `frontend/src/components/ui/FavoriteHeart.vue`
- 인증 필요(ROLE_OWNER)
  - `frontend/src/views/business/reviews/BusinessReviewsPage.vue`
- 인증 필요(ROLE_ADMIN)
  - `frontend/src/views/admin/reviews/AdminReviewsPage.vue`
- public(API Any, axios 사용 유지)
  - `frontend/src/views/restaurant/id/reviews/RestaurantReviewsPage.vue`
  - `frontend/src/views/HomeView.vue`
  - `frontend/src/composables/useTrendingRestaurants.js`

---

## 이번 작업 반영 내역 (상세)

### 1) SecurityConfig 권한 매핑 적용

- permitAll 추가(공개 API 및 리프레시)
  - `/api/join/**`, `/api/auth/**`, `/api/sms/**`, `/api/login`
  - GET `/api/restaurants/*/reviews`, GET `/api/restaurants/*/reviews/*`
  - GET `/api/restaurants/trending`
  - POST `/api/payments/portone/webhook`
  - POST `/api/refresh` (refresh 토큰 재발급)

- ROLE_USER / ROLE_OWNER / ROLE_ADMIN 매핑  
  - CSV의 “필요 권한” 기준으로 requestMatchers에 추가

코드 변경 요약 예시:
```java
// src/main/java/com/example/LunchGo/common/config/SecurityConfig.java
.requestMatchers("/api/join/**", "/api/auth/**", "/api/sms/**", "/api/login").permitAll()
.requestMatchers("/api/refresh").permitAll()
.requestMatchers(HttpMethod.GET,
        "/api/restaurants/*/reviews",
        "/api/restaurants/*/reviews/*",
        "/api/restaurants/trending"
).permitAll()
.requestMatchers(HttpMethod.POST, "/api/payments/portone/webhook").permitAll()

// USER 권한
.requestMatchers(HttpMethod.POST, "/api/restaurants/*/reviews").hasAuthority("ROLE_USER")
.requestMatchers(HttpMethod.GET, "/api/restaurants/*/reviews/*/edit").hasAuthority("ROLE_USER")
.requestMatchers(HttpMethod.PUT, "/api/restaurants/*/reviews/*").hasAuthority("ROLE_USER")
.requestMatchers(HttpMethod.DELETE, "/api/restaurants/*/reviews/*").hasAuthority("ROLE_USER")
```

### 2) refresh 응답 형식 정합성 수정

- 프론트는 `{ accessToken }` 형태를 기대하므로, 백엔드 응답을 Map으로 통일.
```java
// src/main/java/com/example/LunchGo/account/controller/AccountController.java
@PostMapping("/refresh")
public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
    String newAccessToken = accountHelper.regenerate(request, response);
    if (newAccessToken == null) {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    Map<String, String> responseBody = Collections.singletonMap("accessToken", newAccessToken);
    return new ResponseEntity<>(responseBody, HttpStatus.OK);
}
```

### 3) 프론트 httpRequest 통일

- 인증 필요 API 호출을 `axios` → `httpRequest`로 통일하여 토큰 자동 부착/리프레시 적용.
- `httpRequest`에 `patch` 메서드 추가.

예시:
```js
// frontend/src/router/httpRequest.js
patch(url, params, options = {}) {
  return instance.patch(url, params, generateConfig(options));
}
```

```js
// frontend/src/views/restaurant/id/reviews/WriteReviewPage.vue
import httpRequest from "@/router/httpRequest";

const response = await httpRequest.post(
  `/api/restaurants/${restaurantId}/reviews`,
  payload
);
```

### 4) /api/refresh 호출 방식 정합성 수정

- 프론트 refresh 재발급 요청은 POST로 통일.
```js
// frontend/src/router/httpRequest.js
const res = await axios.post('/api/refresh', {}, { withCredentials: true });
```

---

## 참고: 커밋 대상 변경 파일

- `src/main/java/com/example/LunchGo/common/config/SecurityConfig.java`
- `src/main/java/com/example/LunchGo/account/controller/AccountController.java`
- `frontend/src/router/httpRequest.js`
- `frontend/src/composables/useCafeteriaRecommendation.js`
- `frontend/src/composables/useBookmarkShare.js`
- `frontend/src/views/restaurant/id/reviews/WriteReviewPage.vue`
- `frontend/src/views/restaurant/id/payment/RestaurantPaymentPage.vue`
- `frontend/src/views/restaurant/id/confirmation/RestaurantConfirmationPage.vue`
- `frontend/src/views/my-reservations/ReservationCancelView.vue`
- `frontend/src/components/ui/FavoriteHeart.vue`
- `frontend/src/views/business/reviews/BusinessReviewsPage.vue`
- `frontend/src/views/admin/reviews/AdminReviewsPage.vue`
- `api_specification.csv`

## 참고 파일

- 보안 설정: `src/main/java/com/example/LunchGo/common/config/SecurityConfig.java`
- JWT/토큰: `src/main/java/com/example/LunchGo/account/helper/JwtFilter.java`, `src/main/java/com/example/LunchGo/common/util/TokenUtils.java`
- 로그인/갱신: `src/main/java/com/example/LunchGo/account/controller/AccountController.java`
- 프론트 인증 처리: `frontend/src/router/httpRequest.js`, `frontend/src/router/index.js`

## 결정/주의사항

- `/api/login`, `/api/join/**`, `/api/auth/**`, `/api/sms/**`는 공개 API로 유지
- `/api/refresh`는 refresh cookie 기반이므로 접근 정책(permitAll 여부)을 명확히 결정
- 프론트 라우트 권한(meta.roles)와 백엔드 권한 요구사항을 일치시킬 것
