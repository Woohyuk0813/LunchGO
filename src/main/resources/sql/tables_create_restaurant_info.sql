# 테스트용 로컬 db

#create database testdb;
#use testdb;
use lunchgo;

# 외래키는 하단에 따로 모아뒀습니다.

-- 1. 검색 태그
DROP TABLE IF EXISTS search_tags;
CREATE TABLE search_tags
(
    tag_id        BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '태그ID',
    content       VARCHAR(50) NOT NULL COMMENT '태그 내용',
    category      VARCHAR(50) NOT NULL COMMENT '태그 카테고리',

    -- 1. 카테고리 유효성 검사 (기존 유지)
    CONSTRAINT chk_tag_category CHECK (category IN ('MENUTYPE', 'TABLETYPE', 'ATMOSPHERE', 'FACILITY', 'INGREDIENT'))

) COMMENT '검색태그';

-- 중복 태그 방지 (Unique Constraint) 추가
ALTER TABLE search_tags
    ADD CONSTRAINT uk_search_tags_content UNIQUE (content);

-- 빈 문자열/공백 방지 (Check Constraint) 추가
ALTER TABLE search_tags
    ADD CONSTRAINT chk_tag_content_length CHECK (LENGTH(TRIM(content)) > 0);

-- 인덱스 추가 (카테고리별 조회 성능 향상)
CREATE INDEX idx_search_tags_category ON search_tags (category);


-- 2. 식당
DROP TABLE IF EXISTS restaurants;
CREATE TABLE restaurants
(
    restaurant_id      BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '식당 ID',
    owner_id           BIGINT       NOT NULL COMMENT '사업자(점주) ID',
    name               VARCHAR(50)  NOT NULL COMMENT '식당명',
    phone              VARCHAR(15)  NOT NULL COMMENT '식당전화번호',
    road_address       VARCHAR(255) NOT NULL COMMENT '도로명주소',
    detail_address     VARCHAR(255) NOT NULL COMMENT '상세주소',
    status             VARCHAR(50) DEFAULT 'OPEN' COMMENT '운영상태',
    description        TEXT COMMENT '식당소개문',
    avg_main_price     INT          NOT NULL COMMENT '주메뉴 평균가',
    reservation_limit  INT          NOT NULL COMMENT '예약가능인원 상한',
    holiday_available  TINYINT(1)  DEFAULT 0 COMMENT '공휴일 운영 여부 (0:false, 1:true)',
    preorder_available TINYINT(1)  DEFAULT 0 COMMENT '선주문/선결제 여부 (0:false, 1:true)',
    open_time          TIME         NOT NULL COMMENT '영업시작시간',
    close_time         TIME         NOT NULL COMMENT '영업종료시간',
    open_date          DATE         NOT NULL COMMENT '개업일',
    created_at         DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at         DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    CONSTRAINT chk_phone_format CHECK (phone REGEXP '^(050[0-9]|[0-9]{2,3})-[0-9]{3,4}-[0-9]{4}$'),
    CONSTRAINT chk_restaurant_status CHECK (status IN ('OPEN', 'CLOSED', 'DELETED'))
) COMMENT '식당 정보';

-- 인덱스 추가
CREATE INDEX idx_restaurants_owner_id ON restaurants (owner_id);
CREATE INDEX idx_restaurants_name ON restaurants (name);


-- 3. 식당 이미지
DROP TABLE IF EXISTS restaurant_images;
CREATE TABLE restaurant_images
(
    restaurant_image_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '식당이미지ID',
    restaurant_id       BIGINT       NOT NULL COMMENT '식당ID',
    image_url           LONGTEXT NULL COMMENT '식당이미지 URL'
) COMMENT '식당 이미지';


-- 4. 식당 메뉴
CREATE TABLE menus
(
    menu_id       BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '식당메뉴ID',
    restaurant_id BIGINT      NOT NULL COMMENT '식당ID',
    name          VARCHAR(50) NOT NULL COMMENT '식당메뉴명',
    category      VARCHAR(50) DEFAULT 'MAIN' COMMENT '식당메뉴종류',
    description   TEXT NOT NULL COMMENT '메뉴 설명',
    price         INT         NOT NULL COMMENT '가격(1인분 기준)',
    is_deleted    TINYINT(1)  DEFAULT 0 COMMENT '삭제여부',

    CONSTRAINT chk_menu_category CHECK (category IN ('MAIN', 'SUB', 'OTHER'))
) COMMENT '식당 메뉴';


-- 5. 식당 메뉴 이미지
DROP TABLE IF EXISTS menu_images;
CREATE TABLE menu_images
(
    menu_image_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '식당메뉴이미지ID',
    menu_id       BIGINT       NOT NULL COMMENT '식당메뉴ID',
    image_url     LONGTEXT NULL COMMENT '식당메뉴이미지 URL'
) COMMENT '식당 메뉴 이미지';


-- 6. 식당-태그 매핑 (복합키 이름 지정: pk_restaurant_tag_map)
DROP TABLE IF EXISTS restaurant_tag_maps;
CREATE TABLE restaurant_tag_maps
(
    restaurant_id BIGINT NOT NULL COMMENT '식당ID',
    tag_id        BIGINT NOT NULL COMMENT '태그ID',
    CONSTRAINT pk_restaurant_tag_map PRIMARY KEY (restaurant_id, tag_id)
) COMMENT '식당-태그 매핑';


-- 7. 식당메뉴-태그 매핑 (복합키 이름 지정: pk_menu_tag_map)
DROP TABLE IF EXISTS menu_tag_maps;
CREATE TABLE menu_tag_maps
(
    menu_id BIGINT NOT NULL COMMENT '식당메뉴ID',
    tag_id  BIGINT NOT NULL COMMENT '태그ID',
    CONSTRAINT pk_menu_tag_map PRIMARY KEY (menu_id, tag_id)
) COMMENT '식당메뉴-태그 매핑';


-- 8. 정기 휴무일
DROP TABLE IF EXISTS regular_holidays;
CREATE TABLE regular_holidays
(
    reg_holiday_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '정기휴무일ID',
    restaurant_id  BIGINT NOT NULL COMMENT '식당ID',
    day_of_week    INT    NOT NULL COMMENT '휴무요일 (1:일 ~ 7:토)',
    CONSTRAINT chk_day_of_week CHECK (day_of_week BETWEEN 1 AND 7)
) COMMENT '정기 휴무일';


-- 9. 임시 휴무일
DROP TABLE IF EXISTS temporary_holidays;
CREATE TABLE temporary_holidays
(
    temp_holiday_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '임시휴무ID',
    restaurant_id   BIGINT       NOT NULL COMMENT '식당ID',
    start_date      DATE         NOT NULL COMMENT '임시휴무시작일자',
    end_date        DATE         NOT NULL COMMENT '임시휴무종료일자',
    reason          VARCHAR(255) NOT NULL COMMENT '휴업사유'
) COMMENT '임시 휴무일';
