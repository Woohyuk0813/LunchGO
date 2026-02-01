# 리뷰 비즈니스 쿼리 최적화

## 요약
리뷰 관리 페이지는 리스트 조회와 동시에 각 리뷰의 상세를 N회 이상 요청하면서 응답이 반복되고,
상세 API가 태그/이미지/댓글/방문정보 등 여러 테이블을 조회해서 전체 로딩이 느려집니다.
이 문서는 패치 적용 전 현상과 개선 방향, 측정값을 정리한 문서입니다.

## 현황 문제
1. 리뷰별 상세 요청의 N+1 문제
   - `BusinessReviewsPage.vue`에서 `/api/owners/restaurants/{restaurantId}/reviews`를 호출한 뒤
     `item.reviewId`마다 `/reviews/{reviewId}`를 별도 호출합니다.
   - 상세 API가 리뷰 태그, 이미지, 댓글, 방문 정보, 영수증 등을 추가 조회하므로 네트워크·DB 부하가 가중됩니다.

2. 통계용 페이지가 전체 데이터를 다시 불러옴
   - `loadReviewStats`가 별도로 전체 페이지를 다시 요청하고 상세를 N회 더 호출해서 작업량을 두 배로 늘립니다.
   - `loadReviews`와 함께 마운트 시 동시에 실행되어 동시에 대량 요청이 발생합니다.

3. 상세 응답을 리스트 렌더링에 과도하게 활용
   - 리스트는 `ReviewDetailResponse`의 대부분 데이터를 다루는데, 이 중 visit/receipt 상세는 모달 열고 볼 때만 필요합니다.

4. 서버에서 이미 있는 summary를 무시하고 재계산
   - 리스트 API는 `summary`를 내려주지만 `loadReviewStats`는 이를 활용하지 않고 전체 페이지 재로딩으로 총괄 통계를 다시 계산합니다.

## 개선 방향
1. 리스트와 상세를 하나의 최적화된 리스트 응답으로 통합
   - 리스트 DTO에 평점, 작성자, 등록일, 태그, 이미지 개수, 댓글 수, 신고 상태, 방문 요약 등 UI에서 쓰는 필드를 포함합니다.
   - 리스트 로딩 시 리뷰별 상세 API를 호출하지 않습니다.

2. 상세 데이터는 모달이 열릴 때만 로드
   - `/reviews/{reviewId}`는 모달을 열 때만 호출하고, 한 번 받아온 데이터는 로컬 캐시로 재사용합니다.

3. 통계는 summary와 현재 리스트 기반으로 계산
   - `RestaurantReviewListResponse.summary`를 그대로 평가에 사용합니다.
   - `loadReviewStats`는 삭제하거나 값을 보완하는 가벼운 전용 엔드포인트로 대체합니다.

4. 동시 요청 버스트 줄이기
   - 리뷰 로딩이 끝난 후 통계를 필요할 때만 계산합니다.
   - 리스트/통계 모두 상세 호출을 동시에 열지 않도록 합니다.

## 다음 단계
- 백엔드: 1번을 위해 리스트 DTO와 매퍼를 확장하고 불필요한 상세 쿼리를 제거합니다.
- 프론트엔드: 2~4번을 적용해 로딩 흐름을 개선하고, 상세 모달을 지연 로딩으로 전환합니다.
- 측정: 네트워크 인스펙터로 요청 건수·총 latency를 기록하며 개선 효과를 확인합니다.

## 패치 적용 사항
- 댓글 수 조회는 상관 서브쿼리 대신 `comments` 집계 서브쿼리를 `LEFT JOIN`으로 연결해 commentCount를 계산합니다.

## EXPLAIN 비교 (commentCount 개선)
| 항목 | Before (상관 서브쿼리) | After (집계 JOIN) |
| --- | --- | --- |
| 경고/특이사항 | `Field or reference ... resolved in SELECT #1` 경고 | 경고 없음 |
| 서브쿼리 형태 | `DEPENDENT SUBQUERY` | `DERIVED` + `LEFT JOIN` |
| comments 접근 | reviewId마다 상관 서브쿼리 실행 | 집계 서브쿼리 1회 스캔 |
| 측정 (DataGrip) | 203 ms (exec 73 ms, fetch 130 ms) | 112 ms (exec 55 ms, fetch 57 ms) |
## 추가 개선 방향
5. 다이내믹화한 리스트 응답의 상세 정보 최소화
   - 방문/영수증 조인을 아예 제외하고, 댓글 개수와 방문 날짜/인원/총액 같은 요약만 포함합니다.
   - 서버에선 해당 필드만 한번에 조회해서 프론트가 상세 호출 없이 리스트만으로 통계 가능한 구조를 유지합니다.
   - 필요시 모달 열릴 때 사용자 행동을 기록해 두고 추가적인 접속 통계 캐싱 전략을 적용합니다.

## 측정값 (패치 전/후 비교)
| Metric | Baseline (before) | After Patch 1 (stats 제거) | After Patch 2 (lazy detail) | After Patch 3 (backend DTO) | Recent |
| --- | --- | --- | --- | --- | --- |
| 총 요청 수 | 130 | 89 | 79 | 79 | 79 |
| 전체 로딩 완료 시간 | 7.22초 | 1.12초 | 0.47초 | 0.35초 | 0.34초 |
| 리스트 API 응답 시간 | 약 600ms | 약 197ms | 약 219ms | 약 201ms | 173ms |
| 상세 API 응답 시간 | 500~1000ms | 400~700ms | 약 274ms (모달에서만, 첫 요청 후 캐시) | 약 274ms (모달에서만, 캐시 유지) | - |
| 페이지당 리스트 API 호출 수 | 2 | 1 | 1 | 1 | 1 |
| 페이지당 상세 API 호출 수 | 약 2×N | N | 0 (모달 열릴 때만 호출) | 0 (모달 열릴 때만 호출) | 0 (모달 열릴 때만 호출) |

## 추가 측정 (최근)
- 요청: 79 requests, 19.2 kB transferred, 6.4 MB resources
- 타이밍: Finish 340 ms, DOMContentLoaded 122 ms, Load 129 ms
- 리스트 API: `GET /api/owners/restaurants/96/reviews?page=1&size=10&sort=LATEST` 173 ms
