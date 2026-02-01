# LunchGO 성능 최적화 정리

본 문서는 `docs/*improvement.md`, `docs/*optimization.md`, `docs/*implementation.md`를 기반으로
LunchGO에서 수행한 성능 최적화 작업을 계층별로 정리한 초안이다.
각 항목은 적용 내용, 기대 효과, 측정 지표를 포함하며, 출처 문서를 명시한다.

## 정리 기준
- 성능(응답시간, 쿼리 수, 요청 수, 부하/비용) 개선과 직접 관련된 항목만 포함
- 측정 지표는 출처 문서에 기록된 수치만 사용
- 대상 컴포넌트/API를 함께 표기해 범위를 명확히 한다

## 요약(수치 지표 포함)

### 네트워크/웹 계층
- Nginx keepalive + gzip + preflight 분리 → 지표: n/a (정성 개선)
- proxy timeout 명시 → 지표: n/a (정성 개선)

### 애플리케이션 계층
- 로그인 대기열(큐) → 지표: n/a (정성 개선)
- 예약 잔여석 Redis 사전 검증 → 지표: n/a (정성 개선)
- 리뷰 비즈니스 페이지 요청 N+1 제거 → 지표: 요청 130→79, 로딩 7.22s→0.34s, 리스트 600ms→173ms
- 리뷰 요약 캐시(TTL) → 지표: n/a (정성 개선)
- AI 인사이트 캐시/쿨다운 → 지표: n/a (정성 개선)

### DB 계층
- 태그 매핑 추천 쿼리 최적화 → 지표: 평균 쿼리 수 6~7→4~5
- 트렌딩 추천 쿼리 최적화 → 지표: EXPLAIN 26.6ms→6.41ms, dependent subquery 7→0
- 리뷰 조회 통합 + 인덱스 → 지표: avg 34.52→16.86ms, P95 256.9→25.65ms, SQL 6.73→4.71
- 댓글 수 집계 최적화 → 지표: 203ms→112ms (DataGrip)
- 예약 선주문/선결제 N+1 제거 + Bulk Insert → 지표: n/a (정성 개선)

## 1) 네트워크/웹 계층

### 1-1. Nginx 게이트웨이 성능 최적화
- 대상
  - Bastion Nginx (`/root/nginx-default.conf`)
- 적용 내용
  - upstream keepalive로 백엔드 커넥션 재사용
  - gzip 적용으로 JSON/텍스트 응답 압축
  - CORS preflight(OPTIONS) 전용 처리 + 204 응답
  - proxy timeout 명시로 지연/고착 연결 회수
  - CORS allowlist(map)로 허용 Origin 제한
- 기대 효과
  - TCP 재연결 비용 감소(핸드셰이크/소켓 관리 비용 절감)
  - 응답 페이로드 감소로 전송 시간 단축
  - OPTIONS 요청 캐시로 preflight 빈도 감소
  - 장애/지연 요청에서 워커 점유 시간 감소
- 측정 지표
  - 별도 수치 기록 없음(정성 개선)
- 출처
  - `docs/nginx-conf-improvement.md`

## 2) 애플리케이션 계층

### 2-0. 로그인 대기열(큐) 기반 부하 완화
- 대상
  - `/api/login/queue`, `/api/login`
- 적용 내용
  - Redis Sorted Set 기반 대기열로 동시 로그인 요청을 단계적 허용
  - 토큰 TTL 관리 및 폴링 기반 허용/차단 플로우
  - 프론트 대기 모달로 사용자 대기 UX 제공
- 기대 효과
  - 동시 로그인 시 CPU 스파이크 완화
  - 로그인 API 처리량을 안정적으로 유지
- 측정 지표
  - 별도 수치 기록 없음(완화 구조 목적)
- 출처
  - `docs/login-queue-implementation.md`

### 2-1. 예약 잔여석 Redis 사전 검증
- 대상
  - 예약 생성 플로우 (slot 잔여석 검증)
- 적용 내용
  - 예약 슬롯별 잔여석을 Redis 카운터로 캐싱하고, DB 합산 쿼리 전에 사전 검증
  - 취소/환불 완료 시 트랜잭션 커밋 후 Redis 잔여석을 복원
- 기대 효과
  - 비관적 락 구간에서 잔여석 합산 쿼리 호출 감소
  - 예약 생성 락 대기 시간 감소
- 측정 지표
  - 별도 수치 기록 없음(락 구간 단축 목적)
- 적용 코드/파일
  - `src/main/java/com/example/LunchGo/reservation/service/ReservationFacade.java`
  - `src/main/java/com/example/LunchGo/reservation/service/ReservationRefundService.java`
  - `src/main/java/com/example/LunchGo/reservation/service/ReservationPaymentExpiryService.java`
- 출처
  - `docs/reservation-query-troubleshooting.md`

### 2-2. 리뷰 비즈니스 페이지 N+1 요청 제거
- 대상
  - 리뷰 관리 페이지 + `/api/owners/restaurants/{restaurantId}/reviews`
- 적용 내용
  - API 요청 N+1 패턴 해소를 위해 리스트 응답에 필요한 상세 정보를 포함
  - 상세 API는 모달 오픈 시에만 호출 (lazy load)
  - 상세 결과는 프론트 로컬 캐시로 재사용
  - 통계 로딩 중복 호출 제거
- 기대 효과
  - 페이지당 요청 수 감소, 초기 로딩 지연 축소
  - 불필요한 상세 API 호출 제거로 서버 부하 감소
- 측정 지표
  - 총 요청 수: 130 → 79
  - 전체 로딩 완료 시간: 7.22s → 0.34s
  - 리스트 API 응답: 약 600ms → 173ms
  - 측정 출처: 브라우저 네트워크/타이밍 기록
- 출처
  - `docs/review-business-query-optimization.md`

### 2-3. 리뷰 요약 캐시(TTL)
- 대상
  - 리뷰 요약 응답(평점/리뷰 수/상위 태그)
- 적용 내용
  - 평균 평점/리뷰 수/상위 태그 요약을 서비스 레이어 캐시(TTL 60초)로 처리
- 기대 효과
  - 반복 조회 시 계산 비용 감소
  - DB 부하 완화
- 측정 지표
  - 개별 캐시 효과 수치 없음
- 출처
  - `docs/review-query-optimization.md`

### 2-4. AI 인사이트 생성 캐시/쿨다운
- 대상
  - 주간 AI 인사이트 생성/새로고침
- 적용 내용
  - AI 인사이트 생성 결과를 Redis 캐시로 저장
  - 강제 새로고침 시 60분 쿨다운 적용
  - 캐시 데이터와 DB 예측 데이터를 병합하는 로직 분리
- 기대 효과
  - AI 호출/생성 비용 감소
  - 동일 주차 반복 조회 부하 감소
- 측정 지표
  - 수치 기록 없음(비용/부하 감소 목적)
- 출처
  - `docs/owner-ai-insights-improvement.md`
  - `docs/owner-ai-insights-prompt-improvement.md`

## 3) DB 계층

### 3-0. 태그 매핑 추천 쿼리 최적화
- 대상
  - 취향 기반 추천 조회 쿼리 (tag mapping)
- 적용 내용
  - `tag_maps` 인덱스 추가로 조인 탐색 비용 감소
  - restaurant 태그 집계를 선행해 `COUNT(DISTINCT ...)` 반복 제거
  - 필터 조건(`OPEN`)을 집계 단계로 이동해 조인 대상 축소
  - `liked/disliked` 카운트를 서비스에서 선계산해 CROSS JOIN 최소화
- 기대 효과
  - 추천 조회 쿼리 CPU/IO 감소
  - 동시 추천 호출 시 DB 부하 완화
- 측정 지표
  - 평균 쿼리 수: 6~7 → 4~5 (관측 기준)
- 적용 코드/파일
  - `src/main/java/com/example/LunchGo/algorithm/service/TagMapServiceImpl.java`
  - `src/main/java/com/example/LunchGo/restaurant/repository/RestaurantRepository.java`
- 출처
  - `docs/Tag-Mapping-Recommendation-Summary.md`

### 3-1. 트렌딩 추천 쿼리 최적화
- 대상
  - `GET /api/restaurants/trending`
- 적용 내용
  - 리뷰 태그 Top3 집계를 상관 서브쿼리에서 사전 집계 + JOIN으로 전환
  - 리뷰/이미지/태그 집계를 식당 단위로 1회 계산
  - 상관 서브쿼리 반복(N×3) 제거
- 기대 효과
  - 반복 집계 비용 감소, 실행 시간 단축
  - 대량 식당 데이터에서 안정적인 응답 시간 유지
- 측정 지표
  - EXPLAIN 기준(개선안): 실행 시간 26.6ms → 6.41ms, dependent subquery 7 → 0
- 적용 코드/파일
  - `src/main/java/com/example/LunchGo/restaurant/repository/RestaurantRepository.java`
  - `src/main/java/com/example/LunchGo/restaurant/service/RestaurantTrendService.java`
  - `src/main/java/com/example/LunchGo/restaurant/controller/RestaurantTrendController.java`
  - `src/main/java/com/example/LunchGo/restaurant/dto/TrendingRestaurantItem.java`
- 출처
  - `docs/trending-recommend-implementation.md`

### 3-2. 리뷰 조회 쿼리 통합 + 인덱스 보강
- 대상
  - `GET /api/restaurants/{restaurantId}/reviews`
- 적용 내용
  - 리뷰 ID 페이징 + 상세 조회를 단일 쿼리로 통합
  - 태그/이미지 조회는 IN 절 2회 유지
  - 인덱스 추가:
    - `reviews(restaurant_id, status, created_at, review_id)`
    - `reviews(restaurant_id, status, rating, created_at, review_id)`
    - `review_tag_maps(review_id)`
    - `review_tag_maps(tag_id, review_id)`
    - `review_images(review_id, sort_order)`
- 기대 효과
  - 쿼리 수 감소로 네트워크 round-trip 감소
  - 정렬/필터 인덱스 활용률 개선
- 측정 지표
  - 평균 응답 시간: 34.52ms → 16.86ms (2.05x 개선)
  - P95 응답 시간: 256.9ms → 25.65ms (10.01x 개선)
  - 평균 SQL Count: 6.73 → 4.71 (29.92% 감소)
  - 측정 출처: Scouter XLog (220건/126건 평균)
- 출처
  - `docs/review-query-optimization.md`

### 3-3. 댓글 수 집계 쿼리 최적화
- 대상
  - `/api/owners/restaurants/{restaurantId}/reviews` 상세/리스트 집계
- 적용 내용
  - 상관 서브쿼리(DEPENDENT SUBQUERY) → 집계 서브쿼리 + LEFT JOIN으로 변경
- 기대 효과
  - 리뷰 단위 반복 스캔 제거
  - CPU/IO 부담 감소
- 측정 지표
  - DataGrip 측정: 203ms → 112ms
- 출처
  - `docs/review-business-query-optimization.md`

### 3-4. 예약 선주문/선결제 메뉴 조회 + Bulk Insert
- 대상
  - 예약 생성 내 선주문/선결제 처리
- 적용 내용
  - 메뉴 ID 리스트를 IN 절로 단건 조회하여 N+1 제거
  - `reservation_menu_items`를 forEach 기반 Bulk Insert로 단일 쿼리 등록
- 기대 효과
  - 예약 생성 트랜잭션 내 쿼리 수 감소
  - DB round-trip 감소로 지연 완화
- 측정 지표
  - 별도 수치 기록 없음(쿼리 수 감소 목적)
- 적용 코드/파일
  - `src/main/java/com/example/LunchGo/reservation/mapper/ReservationMapper.java`
  - `src/main/java/com/example/LunchGo/reservation/service/ReservationFacade.java`
- 출처
  - `docs/reservation-query-troubleshooting.md`

## 참고
- 본 문서는 성능 최적화 관점만 요약한다.
- 실제 운영 반영 여부/버전은 각 출처 문서와 코드를 기준으로 재확인한다.

## 부록: 로그인 부하 테스트 결과(참고)
- 목적: 로그인 API 성능 지표 확보 및 병목 확인
- 시나리오: 20 VU, 2분, `/api/login<POST>`
- 결과 요약
  - k6 avg 738.96ms / p95 877.9ms
  - Scouter avg 731ms / SQL avg 6ms
- 출처
  - `docs/login-load-test-implementation.md`
