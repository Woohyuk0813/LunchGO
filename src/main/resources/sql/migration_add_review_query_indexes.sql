/* ==================================================
   리뷰 조회 성능 개선 인덱스
   ================================================== */
CREATE INDEX idx_reviews_restaurant_status_created
    ON reviews (restaurant_id, status, created_at, review_id);

CREATE INDEX idx_reviews_restaurant_status_rating_created
    ON reviews (restaurant_id, status, rating, created_at, review_id);

CREATE INDEX idx_review_tag_maps_tag_review
    ON review_tag_maps (tag_id, review_id);

CREATE INDEX idx_review_tag_maps_review_tag
    ON review_tag_maps (review_id, tag_id);

CREATE INDEX idx_review_images_review_sort
    ON review_images (review_id, sort_order);
