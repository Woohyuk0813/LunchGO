use lunchgo;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS owners;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS review_tag_maps;
DROP TABLE IF EXISTS receipts;
DROP TABLE IF EXISTS review_tags;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS receipt_items;
DROP TABLE IF EXISTS review_images;
DROP TABLE IF EXISTS search_tags;

CREATE TABLE users (
                       user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       nickname VARCHAR(100),
                       phone VARCHAR(20) NOT NULL,
                       birth DATE,
                       gender VARCHAR(10),
                       image VARCHAR(255),
                       company_name VARCHAR(255) NOT NULL,
                       company_address VARCHAR(255) NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       last_login_at DATETIME,
                       withdrawal_at DATETIME,
                       marketing_agree TINYINT NOT NULL DEFAULT 0,
                       email_authentication TINYINT NOT NULL DEFAULT 0,
                       role CHAR(20) NOT NULL DEFAULT 'USER',
                       CONSTRAINT chk_user_status
                           CHECK (status IN ('ACTIVE', 'DORMANT', 'WITHDRAWAL'))
);

ALTER TABLE users ALTER COLUMN gender SET DEFAULT 'NONE';

CREATE TABLE owners (
                        owner_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        login_id VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        business_num VARCHAR(30) NOT NULL,
                        name VARCHAR(50) NOT NULL,
                        phone VARCHAR(20) NOT NULL,
                        image VARCHAR(255),
                        status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        last_login_at DATETIME,
                        role CHAR(20) NOT NULL DEFAULT 'OWNER',
                        start_at date not null,
                        CONSTRAINT chk_owner_status
                            CHECK (status IN ('PENDING', 'ACTIVE', 'WITHDRAWAL'))
);

drop table if exists managers;

CREATE TABLE managers (
                          manager_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          login_id VARCHAR(50) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          role CHAR(20) NOT NULL,
                          last_login_at DATETIME NOT NULL
);

CREATE TABLE specialities (
                              speciality_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              keyword VARCHAR(50) NOT NULL,
                              is_liked TINYINT NOT NULL DEFAULT 0 -- 0: 싫어함, 1: 좋아함
);

-- 회원관리 부분은 pk, fk 관계가 복잡하지 않기 때문에 바로 관계성 연결
CREATE TABLE staffs(
                       staff_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       last_login_at DATETIME,
                       role CHAR(20) NOT NULL DEFAULT 'ROLE_STAFF',
                       owner_id BIGINT NOT NULL,
                       FOREIGN KEY (owner_id) REFERENCES owners(owner_id)
);

CREATE TABLE speciality_mappings (
                                     user_id BIGINT NOT NULL,
                                     speciality_id BIGINT NOT NULL,
                                     PRIMARY KEY (user_id, speciality_id),
                           FOREIGN KEY (user_id) REFERENCES users(user_id),
                           FOREIGN KEY (speciality_id) REFERENCES specialities(speciality_id)
);


CREATE TABLE bookmarks (
                           bookmark_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           restaurant_id BIGINT NOT NULL,
                           promotion_agree TINYINT NOT NULL DEFAULT 0,
                           is_public TINYINT NOT NULL DEFAULT 0,
                           FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX idx_bookmarks_user_public ON bookmarks (user_id, is_public);

CREATE TABLE bookmark_links (
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

-- 프로모션 받기 등록한 사용자 email 찾는 방법
delimiter ##
create procedure getRestaurantIdFromOwnerID(in in_ownerId bigint)
begin
SELECT u.email
FROM bookmarks b
         JOIN users u ON b.user_id = u.user_id
         JOIN restaurants r ON b.restaurant_id = r.restaurant_id
WHERE r.owner_id = in_ownerId
  AND b.promotion_agree = 1;
end ##
delimiter ;
