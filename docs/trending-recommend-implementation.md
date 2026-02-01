# 🍽️ 트렌딩 식당 추천 구현 정리 (Ver 1.2)

작성일: 2025-12-27
갱신일: 2025-12-27
담당: 추천 시스템

## 🎯 목표

- 최근 7일 기준 트렌드(인기순) 식당 추천
- 신규 입점 식당의 Cold Start 완화(Newbie Boost)
- 대표 이미지/태그 포함 응답

## 📌 정책 결정 사항

- 점수 공식
  - Score = (Confirm_recent * W_confirm) + (View_recent * W_view) + W_new
- Confirm_recent: 최근 7일 예약 확정 수
- View_recent: 최근 7일 조회 수
- W_new (신규성 가중치): 오픈 후 30일은 고정, 이후 30일간 선형 감쇠
  - Day_diff = 오늘 날짜 - created_at
  - 0~30일: W_new = C
  - 31~60일: W_new = C \* (60 - Day_diff) / 30
  - 61일 이후: W_new = 0
  - C 기본값: 50
- W_confirm 기본값: 0.8
- W_view 기본값: 0.2

## 🗂️ 사용 데이터

- daily_restaurant_stats
  - stat_date, restaurant_id, view_count, confirm_count
- restaurants
  - created_at, status, name, address
- restaurant_images
  - 대표 이미지 1장 사용(가장 낮은 PK)
- restaurant_tag_maps + search_tags
  - tag_id, content

## 🔗 API

- GET /api/restaurants/trending?days={n}&limit={m}
  - days 기본값 7, 최대 30
  - limit 기본값 10, 최대 50

## 📦 응답 필드

- restaurantId, name, roadAddress, detailAddress
- viewCount, confirmCount, reviewCount, rating, score
- imageUrl (없으면 기본 이미지 반환)
- tags: [{ tagId, content }]
- reviewTags: [{ tagId, content, count }]

## ⚙️ 환경 변수

- TREND_NEWBIE_WEIGHT (default: 50)
- TREND_CONFIRM_WEIGHT (default: 0.8)
- TREND_VIEW_WEIGHT (default: 0.2)
- TREND_VISIT_WEIGHT (legacy, default: 0.8)
- TREND_DEFAULT_IMAGE_URL (default: /placeholder.svg)

## 🧭 구현 위치

- 쿼리: src/main/java/com/example/LunchGo/restaurant/repository/RestaurantRepository.java
- 서비스: src/main/java/com/example/LunchGo/restaurant/service/RestaurantTrendService.java
- 컨트롤러: src/main/java/com/example/LunchGo/restaurant/controller/RestaurantTrendController.java
- DTO: src/main/java/com/example/LunchGo/restaurant/dto/TrendingRestaurantItem.java

## 🧩 구현 코드 요약

### 1) 트렌딩 쿼리 (최근 n일 집계 + 신규성 가중치 + 이미지/태그)

<details>
<summary>🧠 SQL 전문 보기</summary>

```sql
SELECT
  r.restaurant_id AS restaurantId,
  r.name AS name,
  r.road_address AS roadAddress,
  r.detail_address AS detailAddress,
  -- 최근 n일 조회/확정 합계
  COALESCE(s.view_recent, 0) AS viewCount,
  COALESCE(s.confirm_recent, 0) AS confirmCount,
  COALESCE((
    -- 리뷰 수 집계
    SELECT COUNT(*)
    FROM reviews rv
    WHERE rv.restaurant_id = r.restaurant_id
      AND rv.status = 'PUBLIC'
  ), 0) AS reviewCount,
  COALESCE((
    -- 평점 평균(소수 1자리)
    SELECT ROUND(AVG(rv.rating), 1)
    FROM reviews rv
    WHERE rv.restaurant_id = r.restaurant_id
      AND rv.status = 'PUBLIC'
  ), 0) AS rating,
  (
    -- 점수 = 확정*가중치 + 조회*가중치 + 신규 보너스
    (COALESCE(s.confirm_recent, 0) * :confirmWeight) +
    (COALESCE(s.view_recent, 0) * :viewWeight) +
    CASE
      WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
      THEN :newbieWeight
      WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 60 DAY)
      THEN :newbieWeight * (60 - DATEDIFF(CURDATE(), r.created_at)) / 30
      ELSE 0
    END
  ) AS score,
  (
    -- 대표 이미지 1장
    SELECT ri.image_url
    FROM restaurant_images ri
    WHERE ri.restaurant_id = r.restaurant_id
    ORDER BY ri.restaurant_image_id
    LIMIT 1
  ) AS imageUrl,
  (
    -- 식당 태그 ID 목록
    SELECT GROUP_CONCAT(st.tag_id ORDER BY st.tag_id SEPARATOR ',')
    FROM restaurant_tag_maps rtm
    JOIN search_tags st ON st.tag_id = rtm.tag_id
    WHERE rtm.restaurant_id = r.restaurant_id
  ) AS tagIds,
  (
    -- 식당 태그 이름 목록
    SELECT GROUP_CONCAT(st.content ORDER BY st.tag_id SEPARATOR ',')
    FROM restaurant_tag_maps rtm
    JOIN search_tags st ON st.tag_id = rtm.tag_id
    WHERE rtm.restaurant_id = r.restaurant_id
  ) AS tagContents,
  (
    -- 리뷰 태그 Top3 ID
    SELECT GROUP_CONCAT(t.tag_id ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',')
    FROM (
      SELECT rt.tag_id, COUNT(*) AS tag_count
      FROM reviews rv
      JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
      JOIN review_tags rt ON rt.tag_id = rtm.tag_id
      WHERE rv.restaurant_id = r.restaurant_id
        AND rv.status = 'PUBLIC'
        AND rt.tag_type = 'USER'
      GROUP BY rt.tag_id
      ORDER BY tag_count DESC, rt.tag_id
      LIMIT 3
    ) t
  ) AS reviewTagIds,
  (
    -- 리뷰 태그 Top3 이름
    SELECT GROUP_CONCAT(t.name ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',')
    FROM (
      SELECT rt.tag_id, rt.name, COUNT(*) AS tag_count
      FROM reviews rv
      JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
      JOIN review_tags rt ON rt.tag_id = rtm.tag_id
      WHERE rv.restaurant_id = r.restaurant_id
        AND rv.status = 'PUBLIC'
        AND rt.tag_type = 'USER'
      GROUP BY rt.tag_id, rt.name
      ORDER BY tag_count DESC, rt.tag_id
      LIMIT 3
    ) t
  ) AS reviewTagContents,
  (
    -- 리뷰 태그 Top3 카운트
    SELECT GROUP_CONCAT(t.tag_count ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',')
    FROM (
      SELECT rt.tag_id, COUNT(*) AS tag_count
      FROM reviews rv
      JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
      JOIN review_tags rt ON rt.tag_id = rtm.tag_id
      WHERE rv.restaurant_id = r.restaurant_id
        AND rv.status = 'PUBLIC'
        AND rt.tag_type = 'USER'
      GROUP BY rt.tag_id
      ORDER BY tag_count DESC, rt.tag_id
      LIMIT 3
    ) t
  ) AS reviewTagCounts
FROM restaurants r
LEFT JOIN (
  -- 최근 n일 집계
  SELECT restaurant_id,
         CAST(SUM(IFNULL(view_count, 0)) AS SIGNED) AS view_recent,
         CAST(SUM(IFNULL(confirm_count, 0)) AS SIGNED) AS confirm_recent
  FROM daily_restaurant_stats
  WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL :days DAY)
  GROUP BY restaurant_id
) s ON r.restaurant_id = s.restaurant_id
WHERE r.status = 'OPEN'
ORDER BY score DESC
LIMIT :limit
```

</details>
핵심 요약

- 기본 집계: `daily_restaurant_stats`를 최근 `:days` 기준으로 합산해 `viewCount/confirmCount` 생성.
- 리뷰 지표: `reviews`에서 `PUBLIC`만 대상으로 `reviewCount`, `rating`(소수 1자리) 계산.
- 신규성 가중치: `created_at` 기준 30일은 고정 보너스, 31~60일 선형 감쇠.
- 대표 이미지: `restaurant_images`에서 가장 낮은 PK 1장을 대표 이미지로 선택.
- 식당 태그: `restaurant_tag_maps + search_tags`를 통해 식당 자체 태그를 문자열로 집계.
- 리뷰 태그 상위 3개: `review_tag_maps + review_tags` 집계 후 `COUNT` 기준 상위 3개만 추출.
- GROUP_CONCAT 동작: 집계된 행을 하나의 문자열로 합치며, `SEPARATOR ','` 기준으로 콤마 구분 문자열 생성.
  - 예: tag_id 10,11,5 → `"10,11,5"` / tag_content `"재료가 신선해요,가격 대비 만족스러워요,인테리어가 세련돼요"`

### 실행 예시

<details>
<summary>🧪 API 응답 예시 보기</summary>

```http
GET /api/restaurants/trending?days=7&limit=5
```

```json
[
  {
    "restaurantId": 1,
    "name": "숙성도 강남점",
    "viewCount": 220,
    "confirmCount": 16,
    "reviewCount": 8,
    "rating": 4.4,
    "score": 123.5,
    "imageUrl": "https://.../restaurant-1.jpg",
    "tags": [
      { "tagId": 3, "content": "룸" },
      { "tagId": 9, "content": "단체" }
    ],
    "reviewTags": [
      { "tagId": 10, "content": "재료가 신선해요", "count": 4 },
      { "tagId": 11, "content": "가격 대비 만족스러워요", "count": 3 },
      { "tagId": 5, "content": "인테리어가 세련돼요", "count": 2 }
    ]
  }
]
```

</details>

### 2) 서비스 매핑 (기본 이미지 + 태그 구조화)

<details>
<summary>🧩 Java 매핑 코드 보기</summary>

```java
return rows.stream()
    .map(row -> TrendingRestaurantItem.builder()
        .restaurantId(row.getRestaurantId())
        .name(row.getName())
        .roadAddress(row.getRoadAddress())
        .detailAddress(row.getDetailAddress())
        // 집계 지표 매핑
        .viewCount(valueOrZero(row.getViewCount()))
        .confirmCount(valueOrZero(row.getConfirmCount()))
        .reviewCount(valueOrZero(row.getReviewCount()))
        .rating(valueOrZero(row.getRating()))
        .score(valueOrZero(row.getScore()))
        // 대표 이미지 fallback
        .imageUrl(resolveImageUrl(row.getImageUrl()))
        // 식당 태그 / 리뷰 태그 구조화
        .tags(buildTags(row.getTagIds(), row.getTagContents(), null))
        .reviewTags(buildTags(
            row.getReviewTagIds(),
            row.getReviewTagContents(),
            row.getReviewTagCounts()
        ))
        .build())
    .toList();
```

</details>
핵심 요약

- `reviewCount/rating`을 DTO에 포함해 트렌딩 카드에서 즉시 표시 가능.
- `tags`는 식당 태그, `reviewTags`는 리뷰 기반 상위 태그(카운트 포함).
- `resolveImageUrl()`로 대표 이미지가 없을 때 기본 이미지 URL을 보장.

### 3) 태그 파싱 로직 (식당 태그/리뷰 태그 공용)

<details>
<summary>🏷️ 태그 파싱 코드 보기</summary>

```java
private List<TrendingTagItem> buildTags(String tagIds, String tagContents, String tagCounts) {
    // 콤마 구분 문자열을 배열로 변환
    if (tagIds == null || tagIds.isBlank() || tagContents == null || tagContents.isBlank()) {
        return Collections.emptyList();
    }
    String[] idParts = tagIds.split(",");
    String[] contentParts = tagContents.split(",");
    String[] countParts = tagCounts == null ? new String[0] : tagCounts.split(",");
    // 길이 불일치 방지
    int size = Math.min(idParts.length, contentParts.length);
    return IntStream.range(0, size)
        .mapToObj(index -> {
            String idPart = idParts[index].trim();
            String contentPart = contentParts[index].trim();
            if (idPart.isEmpty() || contentPart.isEmpty()) {
                return null;
            }
            try {
                // 리뷰 태그는 count 포함
                Integer countValue = null;
                if (countParts.length > index) {
                    String countPart = countParts[index].trim();
                    if (!countPart.isEmpty()) {
                        countValue = Integer.parseInt(countPart);
                    }
                }
                return TrendingTagItem.builder()
                    .tagId(Long.parseLong(idPart))
                    .content(contentPart)
                    .count(countValue)
                    .build();
            } catch (NumberFormatException ex) {
                return null;
            }
        })
        .filter(Objects::nonNull)
        .toList();
}
```

</details>
핵심 요약

- `GROUP_CONCAT`으로 받은 콤마 구분 문자열을 각각 분리.
  - tagIds: `"10,11,5"` → `["10","11","5"]`
  - tagContents: `"재료가 신선해요,가격 대비 만족스러워요,인테리어가 세련돼요"`
  - tagCounts: `"4,3,2"`
- 인덱스 기준으로 같은 위치의 값들을 매칭해 하나의 태그로 묶는다.
  - index 0 → tagId=10, content=재료가 신선해요, count=4
  - index 1 → tagId=11, content=가격 대비 만족스러워요, count=3
  - index 2 → tagId=5, content=인테리어가 세련돼요, count=2
- 파싱 실패(빈 문자열/숫자 변환 오류)는 `null` 처리 후 제거.
- 식당 태그는 count가 없으므로 `tagCounts`가 `null`일 때도 동작하도록 처리.

### 4) 환경 변수 적용 (신규성 가중치/기본 이미지)

<details>
<summary>⚙️ application.yml 예시 보기</summary>

```yaml
trend:
  # 신규 가중치(기본 50)
  newbie-weight: ${TREND_NEWBIE_WEIGHT:50}
  # 예약 확정 가중치(기본 0.8)
  confirm-weight: ${TREND_CONFIRM_WEIGHT:0.8}
  # 조회 가중치(기본 0.2)
  view-weight: ${TREND_VIEW_WEIGHT:0.2}
  # 기본 이미지 URL
  default-image-url: ${TREND_DEFAULT_IMAGE_URL:/placeholder.svg}
```

</details>

## 🧮 스코어 산출 로직(요약)

- 최근 n일의 view_count, confirm_count 집계
- 신규성 보너스: created_at 기준 30일 유지 + 이후 30일 선형 감쇠
- score 내림차순 정렬 후 상위 limit 반환

## 🖼️ 대표 이미지 처리

- restaurant_images가 비어있으면 기본 이미지 URL 반환

## 🖥️ 프론트 표시 규칙

- 트렌딩 응답의 reviewCount, rating, reviewTags를 우선 사용
- 리뷰 태그가 3개 이상(카운트 >= 1)일 때 리뷰 태그 표시
- 그렇지 않으면 식당 태그 표시 (신규 식당 대응)
- 트렌딩 리스트 상단 + 기존 리스트(중복 제거) 하단 구성

## 🧪 테스트 데이터 보강

- 리뷰/태그/이미지 샘플 데이터 생성 쿼리 추가
  - 파일: src/main/resources/sql/insert_mock_data_review.sql
- review_images_seed.sql은 기존 URL을 유지하고 현재 리뷰 목록에 순서 매핑
  - 파일: src/main/resources/sql/review_images_seed.sql

## 🧱 현재 쿼리 구조(성능 비교 기준)

- 메인 조인: `restaurants` + `daily_restaurant_stats` 집계 서브쿼리 (LEFT JOIN 1회)
- 상관 서브쿼리:
  - 대표 이미지: `restaurant_images` 단일 테이블 조회 1회
  - 리뷰 지표: `reviews` 단일 테이블 집계 2회 (count, avg)
  - 식당 태그: `restaurant_tag_maps` ↔ `search_tags` 조인 2회 (tagIds, tagContents)
  - 리뷰 태그 Top3: `reviews` ↔ `review_tag_maps` ↔ `review_tags` 조인 3회 (ids, contents, counts)
- 정렬: `score` 기준 내림차순 + `LIMIT :limit`

## 📝 메모

- 데이터가 커질수록 리뷰 태그 Top3 집계가 가장 큰 비용이 될 수 있음.
- 이유(구체)
  - 식당별 리뷰 수가 늘어날수록 `reviews → review_tag_maps → review_tags` 조인 대상이 급격히 증가.
  - 상관 서브쿼리 구조라서 **각 식당마다 Top3 집계를 반복 수행**(N개 식당 × 3회 집계).
  - `COUNT` 후 `ORDER BY tag_count DESC LIMIT 3`를 식당별로 수행하므로 정렬 비용이 누적됨.
  - `GROUP_CONCAT`는 집계 결과를 문자열로 합치는 추가 비용이 발생.
- 성능 개선 버전 적용 전 비교 기준으로 유지.

## 🚀 성능 개선 쿼리(초안)

아래 쿼리는 리뷰 태그 Top3 집계를 한 번만 수행하고, 결과를 조인해 상관 서브쿼리 반복을 줄인다.

<details>
<summary>🚀 개선 SQL 전문 보기</summary>

```sql
SELECT
  r.restaurant_id AS restaurantId,
  r.name AS name,
  r.road_address AS roadAddress,
  r.detail_address AS detailAddress,
  -- 최근 n일 집계
  COALESCE(s.view_recent, 0) AS viewCount,
  COALESCE(s.confirm_recent, 0) AS confirmCount,
  COALESCE(rv.reviewCount, 0) AS reviewCount,
  COALESCE(rv.rating, 0) AS rating,
  (
    -- 점수 산정
    (COALESCE(s.confirm_recent, 0) * :confirmWeight) +
    (COALESCE(s.view_recent, 0) * :viewWeight) +
    CASE
      WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
      THEN :newbieWeight
      WHEN r.created_at >= DATE_SUB(CURDATE(), INTERVAL 60 DAY)
      THEN :newbieWeight * (60 - DATEDIFF(CURDATE(), r.created_at)) / 30
      ELSE 0
    END
  ) AS score,
  -- 대표 이미지/태그/리뷰 태그는 집계 후 조인
  img.imageUrl AS imageUrl,
  rt.tagIds AS tagIds,
  rt.tagContents AS tagContents,
  rvt.reviewTagIds AS reviewTagIds,
  rvt.reviewTagContents AS reviewTagContents,
  rvt.reviewTagCounts AS reviewTagCounts
FROM restaurants r
LEFT JOIN (
  SELECT restaurant_id,
         CAST(SUM(IFNULL(view_count, 0)) AS SIGNED) AS view_recent,
         CAST(SUM(IFNULL(confirm_count, 0)) AS SIGNED) AS confirm_recent
  FROM daily_restaurant_stats
  WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL :days DAY)
  GROUP BY restaurant_id
) s ON r.restaurant_id = s.restaurant_id
LEFT JOIN (
  -- 리뷰 지표 집계(식당별 1회)
  SELECT restaurant_id,
         COUNT(*) AS reviewCount,
         ROUND(AVG(rating), 1) AS rating
  FROM reviews
  WHERE status = 'PUBLIC'
  GROUP BY restaurant_id
) rv ON r.restaurant_id = rv.restaurant_id
LEFT JOIN (
  -- 대표 이미지 1장 추출
  SELECT restaurant_id,
         MIN(restaurant_image_id) AS imageId,
         SUBSTRING_INDEX(GROUP_CONCAT(image_url ORDER BY restaurant_image_id), ',', 1) AS imageUrl
  FROM restaurant_images
  GROUP BY restaurant_id
) img ON r.restaurant_id = img.restaurant_id
LEFT JOIN (
  -- 식당 태그 집계
  SELECT rtm.restaurant_id,
         GROUP_CONCAT(st.tag_id ORDER BY st.tag_id SEPARATOR ',') AS tagIds,
         GROUP_CONCAT(st.content ORDER BY st.tag_id SEPARATOR ',') AS tagContents
  FROM restaurant_tag_maps rtm
  JOIN search_tags st ON st.tag_id = rtm.tag_id
  GROUP BY rtm.restaurant_id
) rt ON r.restaurant_id = rt.restaurant_id
LEFT JOIN (
  -- 리뷰 태그 Top3 집계(식당별 1회)
  SELECT restaurant_id,
         GROUP_CONCAT(tag_id ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagIds,
         GROUP_CONCAT(name ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagContents,
         GROUP_CONCAT(tag_count ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagCounts
  FROM (
    -- 식당별 태그 카운트 + 순위
    SELECT
      rv.restaurant_id,
      rt.tag_id,
      rt.name,
      COUNT(*) AS tag_count,
      ROW_NUMBER() OVER (
        PARTITION BY rv.restaurant_id
        ORDER BY COUNT(*) DESC, rt.tag_id
      ) AS rn
    FROM reviews rv
    JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
    JOIN review_tags rt ON rt.tag_id = rtm.tag_id
    WHERE rv.status = 'PUBLIC' AND rt.tag_type = 'USER'
    GROUP BY rv.restaurant_id, rt.tag_id, rt.name
  ) ranked
  WHERE rn <= 3
  GROUP BY restaurant_id
) rvt ON r.restaurant_id = rvt.restaurant_id
WHERE r.status = 'OPEN'
ORDER BY score DESC
LIMIT :limit
```

</details>

## 성능 개선 포인트(코드 레벨 비교)

기존 쿼리는 리뷰 태그 Top3를 식당마다 3번(IDs/Contents/Counts) 계산했다.

기존(상관 서브쿼리 반복)

<details>
<summary>🐢 기존 SQL 조각 보기</summary>

```sql
(
  SELECT GROUP_CONCAT(t.tag_id ORDER BY t.tag_count DESC, t.tag_id SEPARATOR ',')
  FROM (
    SELECT rt.tag_id, COUNT(*) AS tag_count
    FROM reviews rv
    JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
    JOIN review_tags rt ON rt.tag_id = rtm.tag_id
    WHERE rv.restaurant_id = r.restaurant_id
      AND rv.status = 'PUBLIC'
      AND rt.tag_type = 'USER'
    GROUP BY rt.tag_id
    ORDER BY tag_count DESC, rt.tag_id
    LIMIT 3
  ) t
) AS reviewTagIds,
... (Contents/Counts도 동일 구조 반복)
```

</details>
핵심 요약

- `r.restaurant_id`에 종속된 서브쿼리가 **식당마다 반복 실행**됨.
- Top3를 얻기 위한 정렬/임시테이블이 **식당 수 × 3회** 발생.

개선(한 번 집계 후 조인)

<details>
<summary>⚡ 개선 SQL 조각 보기</summary>

```sql
LEFT JOIN (
  SELECT restaurant_id,
         GROUP_CONCAT(tag_id ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagIds,
         GROUP_CONCAT(name ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagContents,
         GROUP_CONCAT(tag_count ORDER BY tag_count DESC, tag_id SEPARATOR ',') AS reviewTagCounts
  FROM (
    SELECT
      rv.restaurant_id,
      rt.tag_id,
      rt.name,
      COUNT(*) AS tag_count,
      ROW_NUMBER() OVER (
        PARTITION BY rv.restaurant_id
        ORDER BY COUNT(*) DESC, rt.tag_id
      ) AS rn
    FROM reviews rv
    JOIN review_tag_maps rtm ON rv.review_id = rtm.review_id
    JOIN review_tags rt ON rt.tag_id = rtm.tag_id
    WHERE rv.status = 'PUBLIC' AND rt.tag_type = 'USER'
    GROUP BY rv.restaurant_id, rt.tag_id, rt.name
  ) ranked
  WHERE rn <= 3
  GROUP BY restaurant_id
) rvt ON r.restaurant_id = rvt.restaurant_id
```

</details>
핵심 요약

- 전체 리뷰/태그를 **한 번만 집계**하고, 식당별 상위 3개만 남긴 뒤 조인.
- `ROW_NUMBER()`로 식당별 상위 3개를 필터링하여 **정렬/집계 비용을 단일화**.
- 상관 서브쿼리 제거로 **N×3 반복 비용 제거**, 실제 실행 시간 감소.

<details>
<summary>단계별 쿼리 최적화 비교</summary>

기존 쿼리문과 개선 쿼리문의 가장 큰 차이는 **데이터를 가져오는 방식**
(SELECT 절의 상관 서브쿼리 vs JOIN을 통한 집합 처리)이다.

### 1) 리뷰 통계 (평점 및 개수)

**기존 방식 (상관 서브쿼리)**

- SELECT 절에서 COUNT/AVG를 각각 실행
- 결과 행이 100개면 리뷰 집계도 100번씩 실행 (N+1 유사)

```sql
-- 기존: 매 행마다 실행됨
SELECT ..., (SELECT COUNT(*) FROM reviews ...) AS reviewCount
```

**개선 방식 (사전 집계 + JOIN)**

- reviews에서 restaurant_id 기준으로 미리 GROUP BY 집계
- 집계 결과를 LEFT JOIN으로 결합

```sql
-- 개선: 미리 집계 후 결합
LEFT JOIN (SELECT ... GROUP BY restaurant_id) rv ...
```

---

### 2) 단순 목록 조회 (이미지, 검색 태그)

**기존 방식**

- imageUrl, tagIds, tagContents를 각각 서브쿼리로 조회
- tag 조인 로직이 중복됨

**개선 방식**

- restaurant_images, restaurant_tag_maps에서 미리 GROUP BY/CONCAT
- 메인 쿼리에서 한 번씩만 JOIN

---

### 3) 리뷰 태그 Top 3 (핵심 병목)

**기존 방식**

- reviewTagIds/Contents/Counts를 위해 동일 집계를 3번 반복
- 식당 수가 늘수록 급격히 느려짐

**개선 방식 (윈도우 함수)**

- ROW_NUMBER()로 식당별 순위를 한 번에 매김
- rn <= 3으로 필터 후 GROUP_CONCAT

---

### 4) 확장성 및 유지보수

**기존 쿼리**

- SELECT 절이 길고 로직이 분산됨
- 리뷰 태그 로직 변경 시 3곳 수정 필요

**개선 쿼리**

- 데이터 생성 로직이 JOIN 절로 모듈화됨
- 변경 시 해당 JOIN 내부만 수정

---

### 요약 비교표

| 구분 | 기존 쿼리문 (SELECT Subquery) | 개선 쿼리문 (JOIN & Derived Table) |
| --- | --- | --- |
| 실행 빈도 | 결과 행(N)마다 서브쿼리 반복 실행 (N * M) | 테이블별로 1회 집계 후 결합 |
| 리뷰 태그 | 동일 집계를 3번 중복 실행 | 윈도우 함수로 1회 집계 후 추출 |
| DB 부하 | I/O 및 CPU 낭비가 큼 | 대량 데이터 처리에 유리 |
| 코드 구조 | SELECT 절이 비대함 | JOIN 절에 로직 집중, SELECT 단순 |
</details>

## 📊 EXPLAIN 비교 템플릿

아래 템플릿에 전/후 실행 계획을 기록한다.

EXPLAIN 요약 비교표

| 항목               | 전(현재)                                             | 후(개선)                                                       | 개선 포인트                         |
| ------------------ | ---------------------------------------------------- | -------------------------------------------------------------- | ----------------------------------- |
| 실행 시간(ms)      | 26.6                                                 | 6.41                                                           | 상관 서브쿼리 제거, 집계 1회화      |
| rows examined      | restaurants 125, daily 25 + 리뷰/태그 서브쿼리 125회 | restaurants 125, daily 25, reviews 2478, tag_maps 1153, tag 25 | 반복 집계 → 단일 집계               |
| rows sent          | 10                                                   | 10                                                             | 동일                                |
| dependent subquery | 7개(식당별 반복)                                     | 0개                                                            | 반복 제거                           |
| temporary table    | 있음                                                 | 있음                                                           | 리뷰 태그 집계는 유지되나 반복 제거 |
| filesort           | 있음                                                 | 있음                                                           | score/태그 정렬 유지                |
| join type          | Nested Loop + Dependent Subquery                     | Hash Join + Nested Loop                                        | 상관 서브쿼리 제거                  |
| 주요 병목          | 리뷰 태그 Top3 3회 반복                              | 리뷰 태그 Top3 1회 집계                                        | 반복 비용 축소                      |

전(현재 쿼리)

| 항목               | 값                                                                                                                          |
| ------------------ | --------------------------------------------------------------------------------------------------------------------------- |
| 실행 시간(ms)      | 26.6 (Limit 노드 actual time 기준)                                                                                          |
| rows examined      | restaurants 125, daily_restaurant_stats 25 + 리뷰/태그 서브쿼리 125회 반복                                                  |
| rows sent          | 10                                                                                                                          |
| temporary table    | 있음 (reviewTag 집계에서 Aggregate using temporary table)                                                                   |
| filesort           | 있음 (score 정렬 + reviewTag 상위 3 정렬)                                                                                   |
| join type          | Nested Loop + Dependent Subquery                                                                                            |
| 주요 테이블/인덱스 | reviews(FK_reviews_restaurants), review_tag_maps(PRIMARY), review_tags(PRIMARY), daily_restaurant_stats(PRIMARY range scan) |

후(개선 쿼리)

| 항목               | 값                                                                                                                                 |
| ------------------ | ---------------------------------------------------------------------------------------------------------------------------------- |
| 실행 시간(ms)      | 6.41 (Limit 노드 actual time 기준)                                                                                                 |
| rows examined      | restaurants 125, daily_restaurant_stats 25, reviews 2478 (집계 1회), review_tag_maps 1153, review_tags 25                          |
| rows sent          | 10                                                                                                                                 |
| temporary table    | 있음 (reviewTag 집계/윈도우 정렬 단계에서 temporary 사용)                                                                          |
| filesort           | 있음 (score 정렬 + reviewTag 집계 정렬)                                                                                            |
| join type          | Hash Join + Nested Loop (상관 서브쿼리 제거)                                                                                       |
| 주요 테이블/인덱스 | reviews(FK_reviews_restaurants), review_tag_maps(FK_review_tags), review_tags(PRIMARY), daily_restaurant_stats(PRIMARY range scan) |
