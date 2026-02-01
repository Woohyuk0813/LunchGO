/* ==================================================
   0. 초기화 (기존 데이터 삭제)
   ================================================== */
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE review_tag_maps;
TRUNCATE TABLE review_images;
TRUNCATE TABLE comments;
TRUNCATE TABLE reviews;
TRUNCATE TABLE review_tags;
TRUNCATE TABLE receipt_items;
TRUNCATE TABLE receipts;
SET FOREIGN_KEY_CHECKS = 1;


/* ==================================================
   1. 기초 데이터 (예약, 영수증)
   ================================================== */

-- 1-2. 영수증 (Receipts)
INSERT INTO receipts (receipt_id, reservation_id, confirmed_amount, image_url, created_at) VALUES
    (1, 1, 120000, 'https://s3-bucket.com/receipts/receipt_001.jpg', DATE_SUB(NOW(), INTERVAL 2 DAY));

INSERT INTO receipts (receipt_id, reservation_id, confirmed_amount, image_url, created_at) VALUES
    (2, 2, 60000, 'https://s3-bucket.com/receipts/receipt_002.jpg', DATE_SUB(NOW(), INTERVAL 2 DAY));


-- 1-3. 영수증 메뉴 상세 (Receipt Items)
INSERT INTO receipt_items (receipt_item_id, receipt_id, menu_name, qty, unit_price, total_price) VALUES
    (1, 1, '숙성 삼겹살', 4, 18000, 72000),
    (2, 1, '김치찌개', 1, 8000, 8000),
    (3, 1, '소주', 4, 5000, 20000),
    (4, 1, '맥주', 4, 5000, 20000);


/* ==================================================
   2. 리뷰 태그 (Review Tags) - ID 명시적 지정
   - 중복 제거 및 카테고리별 정렬
   ================================================== */
INSERT INTO review_tags (tag_id, name, category, tag_type)
VALUES
-- EFFICIENT
(1, '주문 즉시 조리 시작해요', 'EFFICIENT', 'USER'),
(2, '계산이 빨라요', 'EFFICIENT', 'USER'),
(3, '웨이팅 관리가 잘 돼요', 'EFFICIENT', 'USER'),
(4, '음식이 동시에 나와요', 'EFFICIENT', 'USER'),

-- ATMOSPHERE
(5, '인테리어가 세련돼요', 'ATMOSPHERE', 'USER'),
(6, '조명이 아늑해요', 'ATMOSPHERE', 'USER'),
(7, '아이 동반하기 좋아요', 'ATMOSPHERE', 'USER'),
(8, '야외 테라스가 있어요', 'ATMOSPHERE', 'USER'),
(9, '음악이 적당해요', 'ATMOSPHERE', 'USER'),

-- TASTE
(10, '재료가 신선해요', 'TASTE', 'USER'),
(11, '가격 대비 만족스러워요', 'TASTE', 'USER'),
(12, '시그니처 메뉴가 있어요', 'TASTE', 'USER'),
(13, '디저트가 맛있어요', 'TASTE', 'USER'),
(14, '술과 안주 궁합이 좋아요', 'TASTE', 'USER'),

-- SERVICE
(15, '직원들이 적극적으로 도와줘요', 'SERVICE', 'USER'),
(16, '메뉴 설명을 잘 해줘요', 'SERVICE', 'USER'),
(17, '결제 방식이 다양해요 (QR, 간편결제 등)', 'SERVICE', 'USER'),
(18, '반려동물 동반 가능해요', 'SERVICE', 'USER'),
(19, '청결 관리가 잘 돼요', 'SERVICE', 'USER'),
(20, '아기랑 같이 오기 편해요', 'SERVICE', 'USER'),

-- BLINDED (ADMIN 전용)
(21, '욕설/비속어 포함', 'BLINDED', 'ADMIN'),
(22, '인신공격/명예훼손', 'BLINDED', 'ADMIN'),
(23, '허위 사실 유포', 'BLINDED', 'ADMIN'),
(24, '도배/스팸/광고', 'BLINDED', 'ADMIN'),
(25, '경쟁 업체 비방', 'BLINDED', 'ADMIN');



/* ==================================================
   3. 리뷰 데이터 (Reviews)
   ================================================== */

-- Case 1: [Best] 영수증 인증 + 포토 + 칭찬 (이대리 -> 숙성도)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (1, 1, 2, 1, 5, '팀 회식으로 갔는데 고기 질이 정말 좋네요! 술이랑 궁합도 좋고, 직원분들이 고기를 다 구워주셔서 편했습니다. 재방문 의사 100%입니다.', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'PUBLIC');

-- Case 2: [Normal] 영수증 없음 + 텍스트만 (박과장 -> 숙성도)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (2, 1, 3, NULL, 4, '맛은 있는데 저녁 시간대라 웨이팅이 좀 있었어요. 그래도 기다린 보람이 있는 맛입니다.', NOW(), NOW(), 'PUBLIC');

-- Case 3: [Soso] 영수증 없음 + 포토 (최신입 -> 바다향기)
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (3, 2, 4, NULL, 3, '회는 신선한데 가격대가 좀 있네요. 법카 쓸 때만 올 수 있을 듯...', NOW(), NOW(), 'PUBLIC');

-- Case 4: [Blinded] 블라인드 처리된 악성 리뷰 (익명/User2 -> 바다향기)
-- 관리자에 의해 숨김 처리된 상태 테스트용
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (4, 2, 2, NULL, 1, '야이 $*!@ 주인장 나와라 장사를 이따위로 하냐?', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'BLINDED');

INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (6, 1, 2, 2, 3, '영수증 이미지 테스트용 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'BLINDED');

INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (7, 1, 2, 2, 3, '테스트 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (8, 1, 2, 2, 3, '테스트 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (9, 1, 2, 2, 3, '테스트 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (10, 1, 2, 2, 3, '테스트 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (11, 1, 2, 2, 3, '테스트 신고 요청 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'BLIND_REQUEST');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (12, 1, 2, 2, 3, '테스트 신고 요청 거부된 리뷰', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 'BLIND_REJECTED');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (13, 1, 1, NULL, 5, '예약 시간에 맞춰 입장했고, 직원 응대가 친절했습니다.', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 'PUBLIC');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (14, 1, 2, NULL, 2, '리뷰 내용이 공격적이라 블라인드가 필요해 보입니다.', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 'BLINDED');
INSERT INTO reviews (review_id, restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (15, 1, 3, NULL, 4, '음식이 빨리 나와서 회식 진행이 수월했습니다.', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 'PUBLIC');




/* ==================================================
   4. 리뷰 태그 매핑 (Review Tag Maps)
   ================================================== */

-- Review 1 (회식, 칭찬): 술궁합(14), 직원친절(15), 청결(19)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (1, 14), (1, 15), (1, 19);

-- Review 2 (웨이팅, 맛): 재료신선(10), 웨이팅관리(3)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (2, 10), (2, 3);

-- Review 3 (가격, 신선): 재료신선(10), 가격대비(11) - 리뷰 내용은 비싸다지만 태그는 보통 사용자가 선택하므로
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (3, 10);

-- Review 4 (블라인드): 욕설(21), 인신공격(22) - 관리자가 태그를 달았다고 가정
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (4, 21), (4, 22);

-- Review 6 (블라인드): 욕설(21)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (6, 21);

-- Review 7~10 (일반): 재료신선(10), 가격대비(11), 분위기(5)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES
    (7, 10), (7, 11),
    (8, 5),
    (9, 10),
    (10, 11);

-- Review 11 (블라인드 요청): 도배/스팸(24)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (11, 24);

-- Review 12 (블라인드 거부): 인신공격(22)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES (12, 22);

-- Review 13~15 (일반/블라인드): 직원친절(15), 인테리어(5), 욕설(21)
INSERT INTO review_tag_maps (review_id, tag_id) VALUES
    (13, 15),
    (14, 21),
    (15, 5);


/* ==================================================
   5. 리뷰 이미지 & 답글
   ================================================== */

-- 리뷰 1번 이미지 (2장)


/* ==================================================
   6. 자동 생성 샘플 (restaurant_id 1~125)
   - 식당당 0~10개 리뷰
   - 리뷰 태그 1~3개 매핑
   - 일부 리뷰에 이미지 1~2장 첨부
   ================================================== */

-- 6-1. 리뷰 자동 생성 (AUTO_INCREMENT 사용)
INSERT INTO reviews (restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status)
SELECT
    r.restaurant_id,
    u.user_id AS user_id,
    NULL,
    3 + (s.n % 3) AS rating,
    CONCAT('샘플 리뷰 ', r.restaurant_id, '-', s.n),
    DATE_SUB(NOW(), INTERVAL (s.n + (r.restaurant_id % 7)) DAY),
    DATE_SUB(NOW(), INTERVAL (s.n + (r.restaurant_id % 7)) DAY),
    'PUBLIC'
FROM restaurants r
JOIN (
    SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
    UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10
) s ON s.n <= (r.restaurant_id % 11)
JOIN (SELECT COUNT(*) AS cnt FROM users) user_cnt
JOIN (
    SELECT u.user_id, (@rownum := @rownum + 1) AS rn
    FROM users u
    CROSS JOIN (SELECT @rownum := 0) vars
    ORDER BY u.user_id
) u
  ON u.rn = ((r.restaurant_id + s.n - 1) % user_cnt.cnt) + 1
WHERE r.restaurant_id BETWEEN 1 AND 125;

-- 6-2. 리뷰 태그 매핑/이미지 생성
-- 아래 블록은 신규로 생성된 리뷰 ID를 선택해 태그/이미지를 매핑합니다.
-- 필요 시 실행 전에 @review_seed_since 값을 기준으로 조정하세요.

TRUNCATE TABLE review_tag_maps;
SET @review_seed_since = DATE_SUB(NOW(), INTERVAL 7 DAY);

INSERT INTO review_tag_maps (review_id, tag_id)
SELECT
    r.review_id,
    ((r.restaurant_id + r.review_id + t.t) % 20) + 1 AS tag_id
FROM reviews r
JOIN (
    SELECT 1 AS t UNION ALL SELECT 2 UNION ALL SELECT 3
) t ON t.t <= ((r.restaurant_id + r.review_id) % 3) + 1
WHERE r.created_at >= @review_seed_since;

/* ==================================================
   7. 기존 리뷰 평점 분포 업데이트
   - 기존 데이터는 유지하고 rating만 재분포
   - RAND(review_id)로 재실행 시 동일 분포 유지
   ================================================== */
UPDATE reviews
SET rating = CASE
    WHEN RAND(review_id) < 0.40 THEN 5
    WHEN RAND(review_id) < 0.70 THEN 4
    WHEN RAND(review_id) < 0.90 THEN 3
    WHEN RAND(review_id) < 0.97 THEN 2
    ELSE 1
END
WHERE review_id IS NOT NULL;

/* ==================================================
   8. 리뷰 태그 재매핑 (키워드 기반 + 랜덤 분포 혼합)
   - 키워드 매칭 후, 부족한 태그는 랜덤으로 보강
   ================================================== */
DELETE FROM review_tag_maps
WHERE review_id IN (SELECT review_id FROM reviews);

-- 8-1. 키워드 기반 태그 매핑
INSERT IGNORE INTO review_tag_maps (review_id, tag_id)
SELECT
    r.review_id,
    m.tag_id
FROM reviews r
JOIN (
    SELECT 3 AS tag_id, '웨이팅' AS kw UNION ALL
    SELECT 2, '빠르' UNION ALL
    SELECT 2, '빨리' UNION ALL
    SELECT 4, '동시' UNION ALL
    SELECT 5, '인테리어' UNION ALL
    SELECT 6, '조명' UNION ALL
    SELECT 7, '아이' UNION ALL
    SELECT 20, '아기' UNION ALL
    SELECT 8, '테라스' UNION ALL
    SELECT 9, '음악' UNION ALL
    SELECT 10, '신선' UNION ALL
    SELECT 11, '가성비' UNION ALL
    SELECT 11, '가격' UNION ALL
    SELECT 12, '시그니처' UNION ALL
    SELECT 13, '디저트' UNION ALL
    SELECT 14, '궁합' UNION ALL
    SELECT 14, '안주' UNION ALL
    SELECT 15, '친절' UNION ALL
    SELECT 16, '설명' UNION ALL
    SELECT 17, '결제' UNION ALL
    SELECT 17, '간편' UNION ALL
    SELECT 17, 'QR' UNION ALL
    SELECT 18, '반려' UNION ALL
    SELECT 18, '애견' UNION ALL
    SELECT 19, '청결' UNION ALL
    SELECT 19, '깔끔'
) m ON r.content LIKE CONCAT('%', m.kw, '%');

-- 8-2. 랜덤 보강 태그 (USER 태그 1~20)
INSERT IGNORE INTO review_tag_maps (review_id, tag_id)
SELECT r.review_id,
       ((r.review_id + t.seed) % 20) + 1 AS tag_id
FROM reviews r
         JOIN (SELECT 1 AS seed
               UNION ALL
               SELECT 2
               UNION ALL
               SELECT 3) t
WHERE RAND(r.review_id + t.seed) < 0.45;


-- 8-3. 블라인드/신고 요청 리뷰에 ADMIN 태그 혼합
INSERT IGNORE INTO review_tag_maps (review_id, tag_id)
SELECT
    r.review_id,
    m.tag_id
FROM reviews r
JOIN (
    SELECT 21 AS tag_id, '욕설' AS kw UNION ALL
    SELECT 21, '비속어' UNION ALL
    SELECT 22, '인신' UNION ALL
    SELECT 22, '명예훼손' UNION ALL
    SELECT 23, '허위' UNION ALL
    SELECT 24, '도배' UNION ALL
    SELECT 24, '스팸' UNION ALL
    SELECT 24, '광고' UNION ALL
    SELECT 25, '경쟁' UNION ALL
    SELECT 25, '비방'
) m ON r.content LIKE CONCAT('%', m.kw, '%')
WHERE r.status IN ('BLINDED', 'BLIND_REQUEST');

-- 8-4. 키워드 미매칭 시 ADMIN 태그 랜덤 보강 (블라인드 계열만)
INSERT IGNORE INTO review_tag_maps (review_id, tag_id)
SELECT
    r.review_id,
    20 + ((r.review_id + t.seed) % 5) + 1 AS tag_id
FROM reviews r
JOIN (
    SELECT 1 AS seed UNION ALL SELECT 2
) t
WHERE r.status IN ('BLINDED', 'BLIND_REQUEST')
  AND RAND(r.review_id + t.seed + 1000) < 0.35;

/* ==================================================
   9. 기존 리뷰 내용 현실화 업데이트
   - 평점/상태에 따라 문장 톤 조정
   - RAND(review_id)로 재실행 시 동일 문장 유지
   ================================================== */
UPDATE reviews r
SET r.content = CONCAT(
    ELT(1 + FLOOR(RAND(review_id) * 6),
        '오늘 예약하고 방문했어요. ',
        '주말에 다녀왔습니다. ',
        '점심시간에 방문했어요. ',
        '퇴근 후 방문했습니다. ',
        '가족이랑 같이 갔어요. ',
        '친구들과 모임으로 갔습니다. '
    ),
    ELT(1 + FLOOR(RAND(review_id + 11) * 6),
        '음식이 전체적으로 ',
        '메뉴가 전반적으로 ',
        '분위기가 생각보다 ',
        '직원 응대가 ',
        '가격대가 ',
        '매장이 '
    ),
    CASE
        WHEN rating >= 4 THEN ELT(1 + FLOOR(RAND(review_id + 22) * 6),
            '만족스러웠어요. ',
            '좋았습니다. ',
            '기분 좋게 먹고 나왔어요. ',
            '재방문 의사 있어요. ',
            '추천할 만합니다. ',
            '기대 이상이었어요. '
        )
        WHEN rating = 3 THEN ELT(1 + FLOOR(RAND(review_id + 22) * 6),
            '무난했어요. ',
            '나쁘지 않았습니다. ',
            '보통 수준이었어요. ',
            '기대만큼은 아니었어요. ',
            '특별하진 않았습니다. ',
            '그럭저럭 괜찮았어요. '
        )
        ELSE ELT(1 + FLOOR(RAND(review_id + 22) * 6),
            '아쉬웠어요. ',
            '기대 이하였습니다. ',
            '다시 갈지는 모르겠어요. ',
            '생각보다 별로였어요. ',
            '만족하지 못했어요. ',
            '재방문은 고민됩니다. '
        )
    END,
    CASE
        WHEN RAND(review_id + 33) < 0.35 THEN ELT(1 + FLOOR(RAND(review_id + 44) * 5),
            '웨이팅이 조금 있었지만 금방 들어갔어요. ',
            '테이블 회전이 빨랐어요. ',
            '직원이 메뉴 설명을 잘 해줬어요. ',
            '양이 넉넉한 편이었어요. ',
            '반찬/사이드가 깔끔했어요. '
        )
        ELSE ''
    END,
    CASE
        WHEN RAND(review_id + 55) < 0.25 THEN ELT(1 + FLOOR(RAND(review_id + 66) * 5),
            '다음엔 다른 메뉴도 먹어볼게요.',
            '근처 오면 또 들를게요.',
            '모임 장소로 괜찮네요.',
            '가성비는 나쁘지 않습니다.',
            '서비스가 인상적이었습니다.'
        )
        ELSE ''
    END
)
WHERE r.review_id IN (SELECT review_id FROM (SELECT review_id FROM reviews) r2);

/* ==================================================
   10. 리뷰 작성자 재배정 (전체 users 기준)
   - 기존 리뷰 유지, user_id만 랜덤 재배정
   - RAND(review_id) 기반이라 재실행 시 동일 분포
   ================================================== */
UPDATE reviews r
JOIN (SELECT COUNT(*) AS cnt FROM users) uc
JOIN (
    SELECT u.user_id, (@rownum := @rownum + 1) AS rn
    FROM users u
    CROSS JOIN (SELECT @rownum := 0) vars
    ORDER BY u.user_id
) u
  ON u.rn = 1 + FLOOR(RAND(r.review_id) * uc.cnt)
SET r.user_id = u.user_id
WHERE r.review_id IN (SELECT review_id FROM (SELECT review_id FROM reviews) r2);

/* ==================================================
   11. 유저 프로필 현실화 (name/nickname/company_name)
   - 지정된 user_id(1~12, 7211)만 업데이트
   ================================================== */
UPDATE users
SET
    name = CASE user_id
        WHEN 1 THEN '김판교'
        WHEN 2 THEN '이강남'
        WHEN 3 THEN '박휴면'
        WHEN 4 THEN '최서연'
        WHEN 5 THEN '전예원'
        WHEN 6 THEN '김철수'
        WHEN 7 THEN '이영희'
        WHEN 8 THEN '박민수'
        WHEN 9 THEN '최지우'
        WHEN 10 THEN '정재훈'
        WHEN 11 THEN '김유미'
        WHEN 12 THEN '손지은'
        WHEN 7211 THEN '박기웅'
        ELSE name
    END,
    nickname = CASE user_id
        WHEN 1 THEN '판교러너'
        WHEN 2 THEN '맛집레이더'
        WHEN 3 THEN '느린점심'
        WHEN 4 THEN '세모맛집'
        WHEN 5 THEN 'yewoni'
        WHEN 6 THEN 'IronSoo'
        WHEN 7 THEN 'ZeroHee'
        WHEN 8 THEN 'CodeMaster'
        WHEN 9 THEN 'ArtChoi'
        WHEN 10 THEN 'BizJung'
        WHEN 11 THEN '유미로그'
        WHEN 12 THEN '지은픽'
        WHEN 7211 THEN '기웅킴'
        ELSE nickname
    END,
    company_name = CASE user_id
        WHEN 1 THEN '런치고'
        WHEN 2 THEN '네이버'
        WHEN 3 THEN '삼성전자'
        WHEN 4 THEN '프리랜서'
        WHEN 5 THEN '카카오'
        WHEN 6 THEN '테크솔루션'
        WHEN 7 THEN '픽셀스튜디오'
        WHEN 8 THEN '넥스트레벨 소프트'
        WHEN 9 THEN '크리에이티브 랩'
        WHEN 10 THEN '글로벌 무역'
        WHEN 11 THEN '라인플러스'
        WHEN 12 THEN '쿠팡'
        WHEN 7211 THEN '런치고'
        ELSE company_name
    END
WHERE user_id IN (1,2,3,4,5,6,7,8,9,10,11,12,7211);

/* ==================================================
   12. 유저 프로필 보정 (nickname 비어있거나 company_name='COMP')
   - 지정 조건만 업데이트
   ================================================== */
UPDATE users
SET
    nickname = CASE
        WHEN nickname IS NULL OR nickname = '' THEN ELT(1 + (user_id % 20),
            '런치메이트', '점심탐정', '맛집수집가', '판교러너',
            '오피스피플', '한끼러버', '직장인밥상', '리뷰로그',
            '점메추달인', '식도락러', '밥친구', '오늘뭐먹지',
            '혼밥러', '든든한끼', '점심메이트', '식사모드',
            '미식가', '알뜰미식', '맛집탐험', '판교직장인'
        )
        ELSE nickname
    END,
    company_name = CASE
        WHEN company_name = 'COMP' OR company_name IS NULL OR company_name = '' THEN ELT(1 + (user_id % 24),
            '네이버', '카카오', '라인플러스', '네이버웹툰',
            'NHN', 'SK hynix', '한글과컴퓨터', '엔씨소프트',
            '스마일게이트', '넥슨코리아', '넷마블', '위메이드',
            '카카오엔터프라이즈', '카카오뱅크', '토스', '배달의민족',
            '야놀자', '무신사', '당근마켓', '쿠팡',
            '11번가', '지마켓', 'LG CNS', 'CJ올리브네트웍스'
        )
        ELSE company_name
    END
WHERE (nickname IS NULL OR nickname = '' OR company_name = 'COMP' OR company_name IS NULL OR company_name = '');

/* ==================================================
   13. 리뷰 추가 생성 (약 2000개, restaurant_id 4~125 불균등 분포)
   ================================================== */
SET @review_seed_start = (SELECT IFNULL(MAX(review_id), 0) FROM reviews);
INSERT INTO reviews (restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status)
SELECT
    CASE
        WHEN s.rand_val < 0.50 THEN 4 + FLOOR(s.rand_rest1 * 17)
        WHEN s.rand_val < 0.80 THEN 21 + FLOOR(s.rand_rest2 * 40)
        WHEN s.rand_val < 0.95 THEN 61 + FLOOR(s.rand_rest3 * 40)
        ELSE 101 + FLOOR(s.rand_rest4 * 25)
    END AS restaurant_id,
    u.user_id,
    NULL AS receipt_id,
    CASE
        WHEN s.rand_rating < 0.40 THEN 5
        WHEN s.rand_rating < 0.70 THEN 4
        WHEN s.rand_rating < 0.90 THEN 3
        WHEN s.rand_rating < 0.97 THEN 2
        ELSE 1
    END AS rating,
    CONCAT('추가 리뷰 ', s.n) AS content,
    DATE_SUB(NOW(), INTERVAL FLOOR(s.rand_days * 90) DAY) AS created_at,
    DATE_SUB(NOW(), INTERVAL FLOOR(s.rand_days * 90) DAY) AS updated_at,
    'PUBLIC' AS status
FROM (
    SELECT
        seq.n,
        RAND(seq.n * 13) AS rand_val,
        RAND(seq.n * 17) AS rand_rating,
        RAND(seq.n * 19) AS rand_days,
        RAND(seq.n * 23) AS rand_rest1,
        RAND(seq.n * 29) AS rand_rest2,
        RAND(seq.n * 31) AS rand_rest3,
        RAND(seq.n * 37) AS rand_rest4
    FROM (
        SELECT (d1.n + d2.n * 10 + d3.n * 100 + d4.n * 1000) + 1 AS n
        FROM (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) d1
        CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) d2
        CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) d3
        CROSS JOIN (
            SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
        ) d4
        WHERE (d1.n + d2.n * 10 + d3.n * 100 + d4.n * 1000) + 1 <= 2000
    ) seq
) s
JOIN (SELECT COUNT(*) AS cnt FROM users) user_cnt
JOIN (
    SELECT u.user_id, (@rownum3 := @rownum3 + 1) AS rn
    FROM users u
    CROSS JOIN (SELECT @rownum3 := 0) vars3
    ORDER BY u.user_id
) u
  ON u.rn = 1 + FLOOR(RAND(s.n * 41) * user_cnt.cnt);
SET @review_seed_end = (SELECT MAX(review_id) FROM reviews);

/* ==================================================
   14. 추가 리뷰 내용/태그 보강 (추가 리뷰 대상만)
   ================================================== */
UPDATE reviews r
SET r.content = CONCAT(
    ELT(1 + FLOOR(RAND(r.review_id) * 6),
        '오늘 예약하고 방문했어요. ',
        '주말에 다녀왔습니다. ',
        '점심시간에 방문했어요. ',
        '퇴근 후 방문했습니다. ',
        '가족이랑 같이 갔어요. ',
        '친구들과 모임으로 갔습니다. '
    ),
    ELT(1 + FLOOR(RAND(r.review_id + 11) * 6),
        '음식이 전체적으로 ',
        '메뉴가 전반적으로 ',
        '분위기가 생각보다 ',
        '직원 응대가 ',
        '가격대가 ',
        '매장이 '
    ),
    CASE
        WHEN r.rating >= 4 THEN ELT(1 + FLOOR(RAND(r.review_id + 22) * 6),
            '만족스러웠어요. ',
            '좋았습니다. ',
            '기분 좋게 먹고 나왔어요. ',
            '재방문 의사 있어요. ',
            '추천할 만합니다. ',
            '기대 이상이었어요. '
        )
        WHEN r.rating = 3 THEN ELT(1 + FLOOR(RAND(r.review_id + 22) * 6),
            '무난했어요. ',
            '나쁘지 않았습니다. ',
            '보통 수준이었어요. ',
            '기대만큼은 아니었어요. ',
            '특별하진 않았습니다. ',
            '그럭저럭 괜찮았어요. '
        )
        ELSE ELT(1 + FLOOR(RAND(r.review_id + 22) * 6),
            '아쉬웠어요. ',
            '기대 이하였습니다. ',
            '다시 갈지는 모르겠어요. ',
            '생각보다 별로였어요. ',
            '만족하지 못했어요. ',
            '재방문은 고민됩니다. '
        )
    END,
    CASE
        WHEN RAND(r.review_id + 33) < 0.35 THEN ELT(1 + FLOOR(RAND(r.review_id + 44) * 5),
            '웨이팅이 조금 있었지만 금방 들어갔어요. ',
            '테이블 회전이 빨랐어요. ',
            '직원이 메뉴 설명을 잘 해줬어요. ',
            '양이 넉넉한 편이었어요. ',
            '반찬/사이드가 깔끔했어요. '
        )
        ELSE ''
    END,
    CASE
        WHEN RAND(r.review_id + 55) < 0.25 THEN ELT(1 + FLOOR(RAND(r.review_id + 66) * 5),
            '다음엔 다른 메뉴도 먹어볼게요.',
            '근처 오면 또 들를게요.',
            '모임 장소로 괜찮네요.',
            '가성비는 나쁘지 않습니다.',
            '서비스가 인상적이었습니다.'
        )
        ELSE ''
    END
)
WHERE r.review_id > @review_seed_start
  AND r.review_id <= @review_seed_end;

DELETE FROM review_tag_maps
WHERE review_id > @review_seed_start
  AND review_id <= @review_seed_end;

-- 14-1. 키워드 기반 태그 매핑 (추가 리뷰 대상만)
INSERT IGNORE INTO review_tag_maps (review_id, tag_id)
SELECT
    r.review_id,
    m.tag_id
FROM reviews r
JOIN (
    SELECT 3 AS tag_id, '웨이팅' AS kw UNION ALL
    SELECT 2, '빠르' UNION ALL
    SELECT 2, '빨리' UNION ALL
    SELECT 4, '동시' UNION ALL
    SELECT 5, '인테리어' UNION ALL
    SELECT 6, '조명' UNION ALL
    SELECT 7, '아이' UNION ALL
    SELECT 20, '아기' UNION ALL
    SELECT 8, '테라스' UNION ALL
    SELECT 9, '음악' UNION ALL
    SELECT 10, '신선' UNION ALL
    SELECT 11, '가성비' UNION ALL
    SELECT 11, '가격' UNION ALL
    SELECT 12, '시그니처' UNION ALL
    SELECT 13, '디저트' UNION ALL
    SELECT 14, '궁합' UNION ALL
    SELECT 14, '안주' UNION ALL
    SELECT 15, '친절' UNION ALL
    SELECT 16, '설명' UNION ALL
    SELECT 17, '결제' UNION ALL
    SELECT 17, '간편' UNION ALL
    SELECT 17, 'QR' UNION ALL
    SELECT 18, '반려' UNION ALL
    SELECT 18, '애견' UNION ALL
    SELECT 19, '청결' UNION ALL
    SELECT 19, '깔끔'
) m ON r.content LIKE CONCAT('%', m.kw, '%')
WHERE r.review_id > @review_seed_start
  AND r.review_id <= @review_seed_end;

-- 14-2. 랜덤 보강 태그 (추가 리뷰 대상만, USER 태그 1~20)
INSERT IGNORE INTO review_tag_maps (review_id, tag_id)
SELECT
    r.review_id,
    ((r.review_id + t.seed) % 20) + 1 AS tag_id
FROM reviews r
JOIN (
    SELECT 1 AS seed UNION ALL SELECT 2 UNION ALL SELECT 3
) t
WHERE r.content LIKE '추가 리뷰 %'
  AND r.review_id > @review_seed_start
  AND r.review_id <= @review_seed_end
  AND RAND(r.review_id + t.seed) < 0.45;

/* ==================================================
   15. 카페 리뷰 샘플 (restaurant_id 126, 127)
   ================================================== */
INSERT INTO reviews (restaurant_id, user_id, receipt_id, rating, content, created_at, updated_at, status) VALUES
    (126, 1, NULL, 5, '회식 끝나고 2차로 들렀는데 아메리카노가 깔끔하고 자리도 편했습니다. 대화하기 좋아요.', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 'PUBLIC'),
    (126, 2, NULL, 4, '회식 후 마무리로 아포가또 먹었어요. 달달하고 진해서 디저트 대신 딱입니다.', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 'PUBLIC'),
    (126, 3, NULL, 5, '회식하고 이동하기 좋은 위치라 자주 올 듯해요. 라떼 고소하고 분위기 조용해요.', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), 'PUBLIC'),
    (126, 4, NULL, 4, '퇴근 후 회식팀이랑 갔는데 자리 넉넉하고 음악이 적당했습니다. 커피 맛도 안정적이에요.', DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), 'PUBLIC'),
    (126, 5, NULL, 3, '회식 뒤라 피곤했는데 카페모카가 당 충전에 도움 됐어요. 다만 늦은 시간엔 조금 붐볐습니다.', DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), 'PUBLIC'),
    (126, 6, NULL, 5, '원두향이 좋아서 회식 마무리로 들르기 딱이에요. 바닐라라떼 추천합니다.', DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 11 DAY), 'PUBLIC'),
    (127, 7, NULL, 5, '회식 후 가볍게 들렀는데 원두 선택이 가능해서 좋았어요. 아인슈페너 만족!', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 'PUBLIC'),
    (127, 8, NULL, 4, '회식 2차로 갔습니다. 콜드브루라떼 깔끔하고 매장도 쾌적했어요.', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 'PUBLIC'),
    (127, 9, NULL, 5, '회식 끝나고 디저트 찾다가 방문했는데 말차딸기라떼가 포인트 있어요. 사진도 잘 나옵니다.', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY), 'PUBLIC'),
    (127, 10, NULL, 4, '단체로 들렀는데 회전이 빨라서 좋았어요. 카페라떼 무난합니다.', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), 'PUBLIC'),
    (127, 11, NULL, 5, '회식 마무리로 레몬에이드랑 디저트 먹었어요. 상큼하고 깔끔합니다.', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), 'PUBLIC'),
    (127, 12, NULL, 4, '회식 후 늦게 갔는데 좌석이 넉넉하고 조명이 아늑했어요. 스윗라떼 괜찮네요.', DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), 'PUBLIC');
