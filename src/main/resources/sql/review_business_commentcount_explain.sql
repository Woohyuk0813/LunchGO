-- Review list EXPLAIN for commentCount before/after change.
-- Adjust literals (restaurantId, size, offset) to match the test case.
-- restaurantId: 96, size: 10, offset: 0

-- Before: correlated subquery for commentCount.
EXPLAIN
SELECT
    r.review_id AS reviewId,
    CASE
        WHEN u.nickname IS NOT NULL AND u.nickname != '' THEN u.nickname
        ELSE CONCAT(LEFT(u.name, 1), '**')
    END AS author,
    u.company_name AS company,
    rus.visit_cnt AS visitCount,
    r.rating AS rating,
    r.content AS content,
    DATE(r.created_at) AS createdAt,
    r.created_at AS createdAtTs,
    r.status AS status,
    CASE WHEN r.status = 'BLINDED' THEN 1 ELSE 0 END AS isBlinded,
    NULL AS blindReason,
    r.blind_request_tag_id AS blindRequestTagId,
    brt.name AS blindRequestTagName,
    r.blind_request_reason AS blindRequestReason,
    r.blind_requested_at AS blindRequestedAt,
    (SELECT COUNT(*) FROM comments c WHERE c.review_id = r.review_id) AS commentCount
FROM reviews r
JOIN (
    SELECT DISTINCT
        r.review_id,
        r.created_at,
        r.rating
    FROM reviews r
    WHERE r.restaurant_id = 96
      AND r.status IN ('PUBLIC', 'BLIND_REQUEST', 'BLIND_REJECTED')
    ORDER BY r.created_at DESC
    LIMIT 0, 10
) page ON page.review_id = r.review_id
JOIN users u ON u.user_id = r.user_id
LEFT JOIN restaurant_user_stats rus
    ON rus.restaurant_id = r.restaurant_id
   AND rus.user_id = r.user_id
LEFT JOIN review_tags brt ON brt.tag_id = r.blind_request_tag_id
ORDER BY page.created_at DESC;

-- After: pre-aggregated commentCount via LEFT JOIN.
EXPLAIN
SELECT
    r.review_id AS reviewId,
    CASE
        WHEN u.nickname IS NOT NULL AND u.nickname != '' THEN u.nickname
        ELSE CONCAT(LEFT(u.name, 1), '**')
    END AS author,
    u.company_name AS company,
    rus.visit_cnt AS visitCount,
    r.rating AS rating,
    r.content AS content,
    DATE(r.created_at) AS createdAt,
    r.created_at AS createdAtTs,
    r.status AS status,
    CASE WHEN r.status = 'BLINDED' THEN 1 ELSE 0 END AS isBlinded,
    NULL AS blindReason,
    r.blind_request_tag_id AS blindRequestTagId,
    brt.name AS blindRequestTagName,
    r.blind_request_reason AS blindRequestReason,
    r.blind_requested_at AS blindRequestedAt,
    COALESCE(c_count.cnt, 0) AS commentCount
FROM reviews r
JOIN (
    SELECT DISTINCT
        r.review_id,
        r.created_at,
        r.rating
    FROM reviews r
    WHERE r.restaurant_id = 96
      AND r.status IN ('PUBLIC', 'BLIND_REQUEST', 'BLIND_REJECTED')
    ORDER BY r.created_at DESC
    LIMIT 0, 10
) page ON page.review_id = r.review_id
LEFT JOIN (
    SELECT review_id, COUNT(*) AS cnt
    FROM comments
    GROUP BY review_id
) c_count ON c_count.review_id = r.review_id
JOIN users u ON u.user_id = r.user_id
LEFT JOIN restaurant_user_stats rus
    ON rus.restaurant_id = r.restaurant_id
   AND rus.user_id = r.user_id
LEFT JOIN review_tags brt ON brt.tag_id = r.blind_request_tag_id
ORDER BY page.created_at DESC;
