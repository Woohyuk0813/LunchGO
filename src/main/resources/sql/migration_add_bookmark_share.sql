USE lunchgo;

ALTER TABLE bookmarks
  ADD COLUMN is_public TINYINT NOT NULL DEFAULT 0 AFTER promotion_agree;

CREATE TABLE IF NOT EXISTS bookmark_links (
  link_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  requester_id BIGINT NOT NULL,
  receiver_id BIGINT NOT NULL,
  pair_min BIGINT GENERATED ALWAYS AS (LEAST(requester_id, receiver_id)) STORED,
  pair_max BIGINT GENERATED ALWAYS AS (GREATEST(requester_id, receiver_id)) STORED,
  status CHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  responded_at DATETIME NULL,
  CONSTRAINT chk_bookmark_links_self CHECK (requester_id <> receiver_id),
  CONSTRAINT uq_bookmark_links_pair UNIQUE (pair_min, pair_max),
  FOREIGN KEY (requester_id) REFERENCES users(user_id),
  FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);

CREATE INDEX idx_bookmarks_user_public ON bookmarks (user_id, is_public);
