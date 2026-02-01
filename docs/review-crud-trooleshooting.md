# Review CRUD Troubleshooting

## 목차
1) 리뷰 생성 중복 방지
2) 영수증 메뉴 수정값 저장
3) 예약금 vs 선결제 리뷰 UX 분기
4) 선결제 리뷰 방문 정보 카드 노출
5) 리뷰 상세 401 인증 오류
6) 리뷰 상세 뒤로가기 탭 유지
7) 예약 내역 → 예약 완료 페이지 바인딩
8) 리뷰 생성 지연 체감

## 1) 리뷰 생성 중복 방지
### 증상
- 리뷰 작성 버튼을 연속 클릭하면 동일 예약에 대해 리뷰가 2개 생성됨
- 느린 응답 환경에서 중복 생성 재현

### 영향 범위
- 동일 예약/사용자 리뷰 중복으로 내 리뷰/상세 화면 혼란
- 리뷰 집계/통계 왜곡 가능성

### 원인
- 프론트에서 제출 중복 클릭 방지 로직 부재
- 리뷰 테이블에 `reservation_id + user_id` 유니크 제약 없음
- 동시 요청 시 백엔드에서 중복 생성 방지 실패

### 확인 방법
1) DB 중복 조회
```sql
SELECT review_id, reservation_id, user_id, created_at
FROM reviews
WHERE user_id = 7211 AND reservation_id = 201;
```
2) 프론트에서 네트워크 지연 후 "등록" 버튼 연속 클릭
3) 서버 응답 확인 (409 + reviewId)

### 해결
1) 프론트 이중 제출 방지
2) DB 유니크 제약 추가
3) 백엔드 409 응답 + reviewId

### 적용 파일
- `frontend/src/views/restaurant/id/reviews/WriteReviewPage.vue`
- `src/main/java/com/example/LunchGo/review/service/ReviewServiceImpl.java`
- `src/main/java/com/example/LunchGo/review/exception/ReviewDuplicateException.java`
- `src/main/java/com/example/LunchGo/review/exception/ReviewValidationExceptionHandler.java`
- `src/main/resources/sql/tables_create_review_cafeteria.sql`
- `src/main/resources/sql/migration_add_review_unique_reservation_user.sql`

### 결과
- 중복 클릭 시 더 이상 중복 리뷰가 생성되지 않음
- 중복 감지 시 409 응답으로 기존 리뷰 상세로 이동 가능

## 2) 영수증 메뉴 수정값 저장
### 증상
- 예약금-only 리뷰에서 영수증 업로드 후 메뉴/금액 수정이 저장되지 않음

### 영향 범위
- 사용자가 수정 확정한 금액이 저장되지 않고 OCR 기본값으로 남음

### 원인
- 리뷰 생성 요청에 `receiptItems` 미전송
- 백엔드가 create 시 영수증 항목 업데이트를 수행하지 않음

### 확인 방법
1) 영수증 업로드 → 메뉴 수정 → 수정 확정 → 리뷰 생성
2) 리뷰 상세 재진입 시 메뉴/총액이 OCR 기본값으로 표시되는지 확인

### 해결
1) 프론트 생성 요청에 `receiptItems` 추가(영수증 업로드된 경우만)
2) 백엔드 create에서 `receipt_id` 기준으로 영수증 항목/총액 업데이트

### 적용 파일
- `frontend/src/views/restaurant/id/reviews/WriteReviewPage.vue`
- `src/main/java/com/example/LunchGo/review/dto/CreateReviewRequest.java`
- `src/main/java/com/example/LunchGo/review/service/ReviewServiceImpl.java`

### 결과
- 영수증 업로드 후 수정 확정한 메뉴/금액이 리뷰 생성 시 DB에 저장됨

## 3) 예약금 vs 선결제 리뷰 UX 분기
### 증상
- 선결제 예약에서 영수증 업로드를 요구
- 예약금-only에서 메뉴 편집이 허용됨
- 선결제 리뷰 수정 화면에 영수증 업로드/수정 UI 노출

### 원인
- 예약 요약 `menuItems` 존재 여부에 따른 UI 분기 없음
- edit 로드 시 선결제 메뉴 판단 플래그 미설정

### 해결
1) 예약 요약 `menuItems`가 있으면 선결제 메뉴로 표시
2) 선결제 메뉴는 읽기 전용, 영수증 업로드 버튼/수정 확정 숨김
3) 예약금-only는 영수증 업로드 전 메뉴 편집 비활성화

### 적용 파일
- `frontend/src/views/restaurant/id/reviews/WriteReviewPage.vue`

### 결과
- 예약금-only: 영수증 업로드 후에만 메뉴 편집 가능
- 선결제: 메뉴 표시 + 영수증 업로드 불필요 + 읽기 전용 UI

## 4) 선결제 리뷰 방문 정보 카드 노출
### 증상
- 선결제 리뷰 상세에서 방문 정보 카드가 표시되지 않음
- SQL UNION 오류 및 collation 에러 발생

### 원인
- 방문 정보 조회가 `receipts` 전제
- `UNION` 컬럼 수 불일치
- `menu_name` collation 불일치

### 해결
1) 방문 정보에서 `reservation_menu_items` 합계 사용
2) `receipt_items`/`reservation_menu_items` UNION 컬럼 수 맞춤
3) `menu_name` collation 통일

### 적용 파일
- `src/main/resources/mapper/ReviewReadMapper.xml`

### 결과
- 선결제 리뷰도 방문 정보 카드 + 메뉴/총액 표시 가능

## 5) 리뷰 상세 401 인증 오류
### 증상
- 리뷰 상세 페이지 401 Unauthorized 발생

### 원인
- 인증 헤더 없이 `axios` 직접 호출

### 해결
- `httpRequest` 사용으로 토큰 포함 호출

### 적용 파일
- `frontend/src/views/restaurant/id/reviews/reviewId/ReviewDetailPage.vue`

### 결과
- 리뷰 상세 API 인증 정상 처리

## 6) 리뷰 상세 뒤로가기 탭 유지
### 증상
- 리뷰 상세 뒤로가기 시 지난 예약 탭이 유지되지 않음

### 원인
- `/my-reservations` 진입 힌트가 없을 때 기본 `router.back()` 사용

### 해결
- `from=my-reservations` 또는 `document.referrer` 조건으로 past 탭 강제 이동

### 적용 파일
- `frontend/src/views/restaurant/id/reviews/reviewId/ReviewDetailPage.vue`

### 결과
- 지난 예약에서 들어온 리뷰 상세는 뒤로가기 시 past 탭 유지

## 7) 예약 내역 → 예약 완료 페이지 바인딩
### 증상
- 지난 예약 → 예약 내역 버튼 클릭 시 예약 완료 페이지 데이터 미바인딩

### 원인
- confirmation 페이지가 `reservationId` 쿼리 요구
- 링크에서 `reservationId` 누락

### 해결
- `/restaurant/{id}/confirmation?reservationId={id}`로 이동

### 적용 파일
- `frontend/src/components/ui/UsageHistory.vue`

### 결과
- 예약 완료 페이지 데이터 정상 로드

## 8) 리뷰 생성 지연 체감
### 증상
- 리뷰 생성 대기 시간이 길어짐

### 영향 범위
- 리뷰 생성 UX 지연

### 확인 방법
1) 네트워크 탭에서 요청 구간별 시간 확인
   - 이미지 업로드(`/api/v1/images/upload/reviews`)
   - 리뷰 생성(`/api/restaurants/{id}/reviews`)
2) 서버 로그에서 요청 처리 시간 확인

### 원인 후보
- 이미지 업로드를 파일별로 순차 처리
- 영수증 OCR 업로드 대기
- 대용량 이미지/느린 네트워크

### 개선 아이디어
- 이미지 업로드 병렬 처리
- 업로드 완료 전 리뷰 작성 요청 분리(비동기 저장)
