use lunchgo;
# drop database lunchgo;
# create schema lunchgo collate utf8mb4_general_ci;

# 01. 리뷰 테이블 생성
DROP TABLE IF EXISTS reviews;
CREATE TABLE reviews (
    review_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    restaurant_id	bigint	NOT NULL,
    user_id	bigint	NOT NULL,
    receipt_id	bigint	NULL,
    reservation_id	bigint	NULL COMMENT '예약 ID',
    rating	int	NOT NULL	DEFAULT 5,
    content	text	NULL,
    created_at	datetime	NOT NULL DEFAULT current_timestamp,
    updated_at	datetime	NULL DEFAULT current_timestamp,
    status	varchar(20)	NOT NULL	DEFAULT 'PUBLIC' COMMENT 'PUBLIC, BLIND_REQUEST, BLINDED, BLIND_REJECTED',
    blind_request_tag_id	bigint	NULL COMMENT '사업자 신고 태그',
    blind_request_reason	text	NULL COMMENT '사업자 신고 사유',
    blind_requested_at	datetime	NULL COMMENT '사업자 신고 시각',
    UNIQUE KEY uk_reviews_reservation_user (reservation_id, user_id)
);

# 02. 리뷰 태그 테이블 생성
DROP TABLE IF EXISTS review_tags;
CREATE TABLE review_tags (
    tag_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    name	varchar(255)	NOT NULL,
    category	varchar(20)	NULL COMMENT 'EFFICIENT, ATMOSPHERE, TASTE, SERVICE, BLINDED',
    tag_type	varchar(20)	NOT NULL	DEFAULT 'USER' COMMENT 'USER or ADMIN'
);


# 03. 리뷰 태그 매핑 테이블 생성
DROP TABLE IF EXISTS review_tag_maps;
CREATE TABLE review_tag_maps (
    review_id	bigint	NOT NULL,
    tag_id	bigint	NOT NULL
);

# 04. 리뷰 이미지 테이블 생성
DROP TABLE IF EXISTS review_images;
CREATE TABLE review_images (
    image_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    review_id	bigint	NOT NULL,
    image_url	varchar(500)	NOT NULL,
    sort_order	int	NOT NULL	DEFAULT 0
);

# 05. 리뷰 댓글 테이블 생성
DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
    comment_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    review_id	bigint	NOT NULL,
    content	text	NULL,
    writer_type	varchar(20)	NOT NULL	DEFAULT 'OWNER' COMMENT 'OWNER or ADMIN',
    created_at	datetime	NOT NULL DEFAULT current_timestamp,
    updated_at datetime NULL DEFAULT current_timestamp
);

# 06. 영수증 테이블 생성
DROP TABLE IF EXISTS receipts;
CREATE TABLE receipts (
    receipt_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT ,
    reservation_id	bigint	NOT NULL,
    confirmed_amount	int	NOT NULL,
    image_url	varchar(500)	NULL,
    created_at	datetime	NOT NULL DEFAULT current_timestamp
);

# 07. 영수증 메뉴 상세 테이블 생성
DROP TABLE IF EXISTS receipt_items;
CREATE TABLE receipt_items (
    receipt_item_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    receipt_id	bigint	NOT NULL,
    menu_name	varchar(255)	NOT NULL,
    qty	int	NOT NULL,
    unit_price	int	NOT NULL,
    total_price	int	NOT NULL
);

# 08. 구내식당 메뉴 테이블
DROP TABLE IF EXISTS cafeteria_menus;
CREATE TABLE cafeteria_menus (
    cafeteria_menu_id	bigint	NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id	bigint	NOT NULL,
    served_date	date	NULL,
    main_menu_names	json	NULL,
    raw_text	text	NULL,
    image_url	varchar(500)	NULL
);
