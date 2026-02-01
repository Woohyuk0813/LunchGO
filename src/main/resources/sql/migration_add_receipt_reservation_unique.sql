USE lunchgo;

UPDATE reviews r
JOIN (
    SELECT reservation_id, MAX(receipt_id) AS keep_id
    FROM receipts
    GROUP BY reservation_id
) keep ON keep.reservation_id = (
    SELECT reservation_id
    FROM receipts
    WHERE receipt_id = r.receipt_id
)
SET r.receipt_id = keep.keep_id;

DELETE FROM receipts
WHERE receipt_id IN (
    SELECT receipt_id FROM (
        SELECT
            receipt_id,
            ROW_NUMBER() OVER (
                PARTITION BY reservation_id
                ORDER BY receipt_id DESC
            ) AS rn
        FROM receipts
    ) t
    WHERE t.rn > 1
);

ALTER TABLE receipts
    ADD UNIQUE KEY uk_receipts_reservation (reservation_id);
