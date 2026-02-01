USE lunchgo;

DELETE r1
FROM reviews r1
JOIN reviews r2
  ON r1.user_id = r2.user_id
 AND r1.reservation_id = r2.reservation_id
 AND r1.review_id > r2.review_id
WHERE r1.reservation_id IS NOT NULL;

ALTER TABLE reviews
    ADD UNIQUE KEY uk_reviews_reservation_user (reservation_id, user_id);
