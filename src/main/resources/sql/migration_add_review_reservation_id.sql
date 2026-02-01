USE lunchgo;

ALTER TABLE reviews
    ADD COLUMN reservation_id BIGINT NULL COMMENT '예약 ID';

UPDATE reviews r
JOIN receipts rc ON rc.receipt_id = r.receipt_id
SET r.reservation_id = rc.reservation_id
WHERE r.receipt_id IS NOT NULL
  AND r.reservation_id IS NULL;

ALTER TABLE reviews
    ADD INDEX idx_reviews_reservation_id (reservation_id),
    ADD CONSTRAINT fk_reviews_reservation
        FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
        ON DELETE SET NULL;
