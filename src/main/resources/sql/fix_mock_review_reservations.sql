use lunchgo;

/* ==================================================
   Fix mock reviews missing reservation_id / receipt_id
   - Step 1: Backfill reservation_id from receipts
   - Step 2: Create mock reservations + slots for remaining reviews
   ================================================== */

/* Step 1: reviews with receipt_id already have a reservation */
UPDATE reviews r
JOIN receipts rc ON rc.receipt_id = r.receipt_id
SET r.reservation_id = rc.reservation_id
WHERE r.reservation_id IS NULL
  AND r.receipt_id IS NOT NULL;

/* Step 2: create mock reservations for reviews without receipt/reservation */
SET @slot_offset := 900000;
SET @reservation_offset := 900000;

DROP TEMPORARY TABLE IF EXISTS tmp_reviews_without_reservation;
CREATE TEMPORARY TABLE tmp_reviews_without_reservation AS
SELECT r.review_id, r.restaurant_id, r.user_id, r.created_at
FROM reviews r
WHERE r.reservation_id IS NULL
  AND r.receipt_id IS NULL;

INSERT INTO restaurant_reservation_slots
    (slot_id, restaurant_id, slot_date, slot_time, max_capacity, created_at, updated_at)
SELECT
    review_id + @slot_offset,
    restaurant_id,
    DATE(created_at),
    SEC_TO_TIME((review_id % 86400)),
    20,
    NOW(),
    NOW()
FROM tmp_reviews_without_reservation
ON DUPLICATE KEY UPDATE slot_id = slot_id;

INSERT INTO reservations
    (reservation_id, reservation_code, slot_id, user_id, party_size, reservation_type, status,
     request_message, currency, created_at, updated_at)
SELECT
    review_id + @reservation_offset,
    CONCAT('R-MOCK-', review_id),
    review_id + @slot_offset,
    user_id,
    2,
    'RESERVATION_DEPOSIT',
    'COMPLETED',
    NULL,
    'KRW',
    created_at,
    created_at
FROM tmp_reviews_without_reservation
ON DUPLICATE KEY UPDATE reservation_id = reservation_id;

UPDATE reviews r
JOIN tmp_reviews_without_reservation t ON t.review_id = r.review_id
SET r.reservation_id = t.review_id + @reservation_offset;

DROP TEMPORARY TABLE IF EXISTS tmp_reviews_without_reservation;

SELECT COUNT(*) AS missing_count
FROM reviews
WHERE reservation_id IS NULL
  AND receipt_id IS NULL;
