# 즐겨찾기 공유 트러블슈팅 기록

## 문제 1: 즐겨찾기 목록 조회 500 에러 (Boolean/Byte 변환 오류)

### 증상
- `GET /api/bookmark/list?userId=...` 호출 시 500 에러
- 서버 로그에 `Cannot project java.lang.Boolean/Byte to java.lang.*` 발생

### 원인
- `native query` 결과에서 `TINYINT(0/1)` 컬럼이 JDBC에서 `Byte` 또는 `Boolean`으로 반환됨
- Projection 인터페이스(`BookmarkListRow`) 타입과 실제 반환 타입이 불일치하여 변환 실패

### 해결
- SQL에서 `promotionAgree`, `isPublic`을 `CASE WHEN ... THEN 1 ELSE 0` 형태로 고정
- Projection 타입을 `Integer`로 고정하고, 서비스에서 `== 1`로 boolean 변환

### 해결 과정
- 동일 API가 조회 데이터에 따라 `Byte` 또는 `Boolean`으로 반환되는 것을 로그로 확인
- Projection 타입을 `Boolean` → `Byte` → `Integer`로 변경하며 재현
- SQL에서 숫자로 강제 반환하도록 변경 후 안정화 확인

### 적용 파일
- `src/main/java/com/example/LunchGo/bookmark/repository/BookmarkRepository.java`
- `src/main/java/com/example/LunchGo/bookmark/repository/BookmarkListRow.java`
- `src/main/java/com/example/LunchGo/bookmark/service/BaseBookmarkService.java`

---

## 문제 2: 중복 링크 요청이 401로 보이는 현상

### 증상
- 중복 링크 요청 시 브라우저에 `401 Unauthorized`만 표시됨
- 서버 로그에는 `POST /api/bookmark-links`가 `409`로 찍힘
- 이어서 `/api/refresh`가 불필요하게 호출됨

### 원인
- `/api/bookmark-links`에서 409가 발생하면 내부적으로 `/error`로 디스패치됨
- `/error`가 인증을 요구하면서 401로 응답이 덮임
- 브라우저는 최종 401만 보게 되어 중복 판단이 실패한 것처럼 보임

### 해결
- `/error` 경로는 인증 없이 접근하도록 허용

### 적용 파일
- `src/main/java/com/example/LunchGo/common/config/SecurityConfig.java`

### 참고 로그
- 409 이후 `/error`에서 401 발생
- `/error` permitAll 적용 후 브라우저에서 409 확인됨
