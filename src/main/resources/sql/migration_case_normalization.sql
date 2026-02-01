USE lunchgo;

-- Normalize table names to lowercase without data loss.
-- Safe to run multiple times; uses dynamic SQL to skip missing objects.

SET FOREIGN_KEY_CHECKS = 0;

-- 1) Rename tables (if uppercase versions exist).
SET @schema := DATABASE();

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = @schema AND table_name = 'Managers'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = @schema AND table_name = 'managers'
    ),
    'RENAME TABLE Managers TO managers',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = @schema AND table_name = 'Speciality_mappings'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = @schema AND table_name = 'speciality_mappings'
    ),
    'RENAME TABLE Speciality_mappings TO speciality_mappings',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = @schema AND table_name = 'Invoice_snapshot'
    )
    AND NOT EXISTS (
      SELECT 1 FROM information_schema.tables
      WHERE table_schema = @schema AND table_name = 'invoice_snapshot'
    ),
    'RENAME TABLE Invoice_snapshot TO invoice_snapshot',
    'SELECT 1'
  )
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- 2) Drop/recreate FKs that used uppercase references.
-- reviews.user_id -> users
SET @fk := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = @schema
    AND table_name = 'reviews'
    AND column_name = 'user_id'
    AND referenced_table_name IS NOT NULL
  LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE reviews DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE reviews
  ADD CONSTRAINT fk_reviews_users
  FOREIGN KEY (user_id) REFERENCES users(user_id);

-- reviews.receipt_id -> receipts
SET @fk := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = @schema
    AND table_name = 'reviews'
    AND column_name = 'receipt_id'
    AND referenced_table_name IS NOT NULL
  LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE reviews DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE reviews
  ADD CONSTRAINT fk_reviews_receipts
  FOREIGN KEY (receipt_id) REFERENCES receipts(receipt_id);

-- review_tag_maps.review_id -> reviews
SET @fk := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = @schema
    AND table_name = 'review_tag_maps'
    AND column_name = 'review_id'
    AND referenced_table_name IS NOT NULL
  LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE review_tag_maps DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE review_tag_maps
  ADD CONSTRAINT fk_review_tag_maps_reviews
  FOREIGN KEY (review_id) REFERENCES reviews(review_id)
  ON DELETE CASCADE ON UPDATE CASCADE;

-- restaurant_user_stats.user_id -> users
SET @fk := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = @schema
    AND table_name = 'restaurant_user_stats'
    AND column_name = 'user_id'
    AND referenced_table_name IS NOT NULL
  LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE restaurant_user_stats DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE restaurant_user_stats
  ADD CONSTRAINT fk_restaurant_user_stats_users
  FOREIGN KEY (user_id) REFERENCES users(user_id);

-- staffs.owner_id -> owners
SET @fk := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = @schema
    AND table_name = 'staffs'
    AND column_name = 'owner_id'
    AND referenced_table_name IS NOT NULL
  LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE staffs DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE staffs
  ADD CONSTRAINT fk_staffs_owners
  FOREIGN KEY (owner_id) REFERENCES owners(owner_id);

-- speciality_mappings.user_id -> users
SET @fk := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = @schema
    AND table_name = 'speciality_mappings'
    AND column_name = 'user_id'
    AND referenced_table_name IS NOT NULL
  LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE speciality_mappings DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE speciality_mappings
  ADD CONSTRAINT fk_speciality_mappings_users
  FOREIGN KEY (user_id) REFERENCES users(user_id);

-- bookmarks.user_id -> users
SET @fk := (
  SELECT constraint_name
  FROM information_schema.key_column_usage
  WHERE table_schema = @schema
    AND table_name = 'bookmarks'
    AND column_name = 'user_id'
    AND referenced_table_name IS NOT NULL
  LIMIT 1
);
SET @sql := IF(@fk IS NULL, 'SELECT 1', CONCAT('ALTER TABLE bookmarks DROP FOREIGN KEY `', @fk, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE bookmarks
  ADD CONSTRAINT fk_bookmarks_users
  FOREIGN KEY (user_id) REFERENCES users(user_id);
