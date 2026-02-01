# 즐겨찾기 연동 구현 문서

## 작업 목표
- 모든 애플리케이션 화면에서 사용자 즐겨찾기 상태를 일관되게 노출

## 구현 계획
- 로그인 사용자
  - 즐겨찾기된 경우 표시 상태를 반영
  - 즐겨찾기 삭제 시 "진짜 삭제할지" 확인 후 삭제
- 비회원 사용자
  - 즐겨찾기 버튼 클릭 시 작은 모달 표시
  - 안내 문구: "즐겨찾기 추가를 위해서는 로그인이 필요합니다."
  - "로그인" 버튼 클릭 시 로그인 페이지로 리다이렉트

## 작업 현황
- API 구현 완료
  - 즐겨찾기 추가: `POST /api/bookmark`
  - 즐겨찾기 삭제: `DELETE /api/bookmark`
  - 즐겨찾기 목록 조회: `GET /api/bookmark/list`
- 프론트 일부 구현됨
  - 지난 예약 현황에서 즐겨찾기 추가/삭제 가능
  - 관련 코드 위치
    - `frontend/src/components/ui/UserFavorites.vue`
    - `frontend/src/components/ui/FavoriteHeart.vue`
    - `frontend/src/components/ui/UsageHistory.vue`
    - `frontend/src/composables/useBookmarkShare.js`
- 전역 연동 기반 추가
  - `frontend/src/stores/favorites.js`
  - `frontend/src/composables/useFavorites.js`
  - `frontend/src/components/ui/LoginRequiredModal.vue`
  - `frontend/src/components/ui/FavoriteStarButton.vue`

## 작업 내역 파악용 체크리스트
- [ ] 전역(모든 화면)에서 즐겨찾기 상태 표시 로직 공통화
- [ ] 즐겨찾기 삭제 시 확인 모달(또는 confirm UX) 적용
- [ ] 비회원 클릭 시 로그인 유도 모달/팝업 구현
- [ ] 로그인 유도 모달 내 "로그인" 버튼 라우팅 연결
- [ ] 즐겨찾기 목록 조회 API 연동 및 초기 상태 동기화
- [ ] 즐겨찾기 상태 변경 시 UI 즉시 반영 및 오류 처리
- [ ] 관련 컴포넌트/컴포저블 구조 정리 및 재사용성 점검

## 로그인 유도 모달 UX 요구사항
- 표시 조건: 비회원이 즐겨찾기 버튼을 클릭할 때
- 형태: 작은 모달/팝업, 배경은 최소한의 dim 처리
- 문구: "즐겨찾기 추가를 위해서는 로그인이 필요합니다."
- 버튼: "로그인" 1개 (기본 강조 스타일)
- 동작:
  - "로그인" 클릭 시 `/login`으로 리다이렉트
  - 모달 바깥 클릭 또는 닫기 버튼으로 닫기 가능
- 상태 보존: 로그인 완료 후 원래 화면으로 돌아올 수 있도록 `redirect` 쿼리 파라미터 사용 권장

## 전역 즐겨찾기 상태 관리 구조 제안
### Option A: Pinia Store (권장)
- 파일 제안: `frontend/src/stores/favorites.js`
- 상태
  - `favoriteRestaurantIds: number[]`
  - `isLoading: boolean`
  - `error: string | null`
- 액션
  - `fetchFavorites(userId)` -> `GET /api/bookmark/list`
  - `addFavorite(userId, restaurantId)` -> `POST /api/bookmark`
  - `removeFavorite(userId, restaurantId)` -> `DELETE /api/bookmark`
  - `toggleFavorite(userId, restaurantId)` -> add/remove 분기 + 낙관적 업데이트 + 실패 롤백
  - `clearFavorites()` -> 로그아웃/비회원 처리
- 헬퍼
  - `isFavorite(restaurantId)` -> boolean
- 로그인 유도
  - 비회원 토글 시 `openLoginPrompt()` 호출(모달 컴포넌트와 연계)

### Option B: Composable
- 파일 제안: `frontend/src/composables/useFavorites.js`
- 구조는 Pinia와 동일, 다만 컴포넌트에서 `provide/inject` 또는 상위에서 싱글턴 보장 필요

### Option C: 하이브리드(현재 선택)
- Pinia Store를 기본으로 사용하고, 사용 불가 환경에서는 composable fallback 사용
- 구현 파일
  - `frontend/src/stores/favorites.js`
  - `frontend/src/composables/useFavorites.js`
  - `frontend/src/components/ui/LoginRequiredModal.vue`
  - `frontend/src/components/ui/FavoriteStarButton.vue`
- App.vue 변경 없이 즐겨찾기 UI 컴포넌트 내부에서 로그인 유도 모달 표시

## 페이지별 연동 방식 요약 (전역 상태 기준)
- Home (`frontend/src/views/HomeView.vue`)
  - 마운트/로그인 변화 시 `fetchFavorites(userId)`
  - `favoriteRestaurantIds`는 store에서 가져와 섹션 컴포넌트에 전달
  - `toggleRestaurantFavorite`는 `toggleFavorite(userId, restaurantId)`로 변경
- Restaurant Detail (`frontend/src/views/restaurant/id/RestaurantDetailPage.vue`)
  - `isFavorite(restaurantId)`로 초기 상태 표시
  - 버튼 클릭 시 `toggleFavorite`
  - 비회원이면 로그인 유도 모달 표시
- My Reservations (`frontend/src/views/my-reservations/MyReservationsPage.vue`)
  - 로컬 `favorites` 대신 store의 `favoriteRestaurantIds` 사용
  - `FavoriteHeart` 이벤트는 store 업데이트로 연결
- MyPage Favorites (`frontend/src/views/mypage/UserMyPage.vue`)
  - `UserFavorites`가 store 상태로 목록 구성
  - 삭제 시 store 업데이트 + 로컬 목록 제거

## 구현 전 체크포인트
- 로그인 상태와 `userId` 확보 방식 확정 (Pinia `account` store 기준)
- 로그인 유도 모달 공통 컴포넌트 위치/호출 방식 결정
- `FavoriteHeart`의 confirm/모달 정책 통일 여부 결정
