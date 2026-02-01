## Reservation Query Implementation

### Scope
- 사용자/사업자/관리자 예약 조회 API를 MyBatis 조회 기반으로 구현.
- 프론트 flat DTO(UserReservationResponse)는 유지하고 상세는 별도 DTO로 분리.

### Endpoints
- `GET /api/reservations/my?userId=...`
  - `UserReservationResponse` 리스트 반환.
- `GET /api/reservations/{reservationId}?userId=...`
  - `UserReservationDetailResponse` 반환.
- `GET /api/business/reservations?restaurantId=...`
  - `BusinessReservationItemResponse` 리스트 반환.
- `GET /api/admin/reservations`
  - `AdminReservationItemResponse` 리스트 반환.

### Mapper/SQL
- `ReservationMapper.xml`에 조회 전용 select 추가.
- 사업자 리스트는 `restaurants.restaurant_id = #{restaurantId}` 기준 필터.
- 관리자 리스트는 기본 최신순 정렬만 적용(제한 없음).
- 사용자 상세 조회는 최신 결제 이력 1건을 서브쿼리로 조인.

### DTO/Row 매핑
- `UserReservationListRow` → `UserReservationResponse` (flat).
- `UserReservationDetailRow` → `UserReservationDetailResponse` (상세).
- `BusinessReservationListRow`, `AdminReservationListRow` → 각 리스트 응답 DTO.

### Frontend Wiring
- `frontend/src/views/my-reservations/MyReservationsPage.vue`에서 `/api/reservations/my` 호출 후 화면용 모델로 변환.
- `frontend/src/views/business/reservations/BusinessReservationsPage.vue`에서 `/api/business/reservations` 호출(restaurantId 쿼리 필요).
- `frontend/src/views/business/dashboard/BusinessDashboardPage.vue`에서 `/api/business/reservations` 호출 후 대시보드 상태값으로 매핑.
- `frontend/src/views/business/reservations/ReservationDetailPage.vue`에서 `/api/business/reservations/{reservationId}` 호출.
- `frontend/src/views/admin/reservations/AdminReservationsPage.vue`에서 `/api/admin/reservations` 호출.

### Notes
- 조회 흐름은 MyBatis(SQL/Row) 기반 유지.
- 상태 값은 SQL에서 내려오는 문자열 기준으로 매핑(도메인 enum 추가 금지).
