USE lunchgo;

SET @pwd_hash = '$2y$10$t4UwtOLvLipklqEUg6SnjePE62yzPrt3z6I.bGy9l3ONYcG/CQCyi';

-- MySQL 5.7 호환: 숫자 생성용 파생 테이블 사용
INSERT INTO users (
  email, password, name, nickname, phone, birth, gender, image,
  company_name, company_address, status, marketing_agree, email_authentication, role
)
SELECT
  CONCAT('loadtest.user', LPAD(seq, 4, '0'), '@example.com'),
  @pwd_hash,
  CONCAT('LoadTest', LPAD(seq, 4, '0')),
  NULL,
  CONCAT('010-9000-', LPAD(seq, 4, '0')),
  NULL,
  'NONE',
  NULL,
  'LoadTest Co',
  'Seoul',
  'ACTIVE',
  0,
  0,
  'USER'
FROM (
  SELECT ones.n + tens.n * 10 + 1 AS seq
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) ones
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) tens
  WHERE ones.n + tens.n * 10 < 100
) seqs
ON DUPLICATE KEY UPDATE email = email;

-- 추가 1000명 생성 (1001 ~ 2000)
INSERT INTO users (
  email, password, name, nickname, phone, birth, gender, image,
  company_name, company_address, status, marketing_agree, email_authentication, role
)
SELECT
  CONCAT('loadtest.user', LPAD(seq, 4, '0'), '@example.com'),
  @pwd_hash,
  CONCAT('LoadTest', LPAD(seq, 4, '0')),
  NULL,
  CONCAT('010-9000-', LPAD(seq, 4, '0')),
  NULL,
  'NONE',
  NULL,
  'LoadTest Co',
  'Seoul',
  'ACTIVE',
  0,
  0,
  'USER'
FROM (
  SELECT ones.n + tens.n * 10 + hundreds.n * 100 + thousands.n * 1000 + 1 AS seq
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) ones
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) tens
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) hundreds
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1
  ) thousands
  WHERE ones.n + tens.n * 10 + hundreds.n * 100 + thousands.n * 1000 < 2000
) seqs
WHERE seq BETWEEN 1001 AND 2000
ON DUPLICATE KEY UPDATE email = email;

-- 추가 900명 생성 (101 ~ 1000)
INSERT INTO users (
  email, password, name, nickname, phone, birth, gender, image,
  company_name, company_address, status, marketing_agree, email_authentication, role
)
SELECT
  CONCAT('loadtest.user', LPAD(seq, 4, '0'), '@example.com'),
  @pwd_hash,
  CONCAT('LoadTest', LPAD(seq, 4, '0')),
  NULL,
  CONCAT('010-9000-', LPAD(seq, 4, '0')),
  NULL,
  'NONE',
  NULL,
  'LoadTest Co',
  'Seoul',
  'ACTIVE',
  0,
  0,
  'USER'
FROM (
  SELECT ones.n + tens.n * 10 + hundreds.n * 100 + 1 AS seq
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) ones
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) tens
  CROSS JOIN (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
  ) hundreds
  WHERE ones.n + tens.n * 10 + hundreds.n * 100 < 1000
) seqs
WHERE seq BETWEEN 101 AND 1000
ON DUPLICATE KEY UPDATE email = email;
