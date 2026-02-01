-- 식당 요약 정보를 제공하는 뷰
-- 서브쿼리 대신 JOIN을 사용하여 성능 최적화
CREATE OR REPLACE VIEW restaurant_summary_view AS
SELECT
    r.restaurant_id AS id,
    r.name,
    r.road_address,
    r.detail_address,
    r.avg_main_price AS price,
    img.image_url AS image,
    COALESCE(review_stats.rating, 0.0) AS rating,
    COALESCE(review_stats.reviews, 0) AS reviews,
    COALESCE(cat.category, '기타') AS category
FROM restaurants r
-- 식당별 첫 번째 이미지만 추출
LEFT JOIN (
    SELECT 
        restaurant_id, 
        image_url,
        ROW_NUMBER() OVER(PARTITION BY restaurant_id ORDER BY restaurant_image_id ASC) as rn
    FROM restaurant_images
) img ON r.restaurant_id = img.restaurant_id AND img.rn = 1
-- 리뷰 통계 (평점, 리뷰 수) 계산
LEFT JOIN (
    SELECT 
        restaurant_id,
        AVG(rating) as rating,
        COUNT(*) as reviews
    FROM reviews
    WHERE status = 'PUBLIC'
    GROUP BY restaurant_id
) review_stats ON r.restaurant_id = review_stats.restaurant_id
-- 대표 카테고리(MENUTYPE 태그) 매핑
LEFT JOIN (
    SELECT
        rtm.restaurant_id,
        MIN(st.content) as category
    FROM restaurant_tag_maps rtm
    JOIN search_tags st ON st.tag_id = rtm.tag_id
    WHERE st.category = 'MENUTYPE'
    GROUP BY rtm.restaurant_id
) cat ON r.restaurant_id = cat.restaurant_id
-- 영업 중인 식당만 포함
WHERE r.status = 'OPEN';
