# 01. 리뷰 테이블 외래키 관계
ALTER TABLE reviews
    ADD CONSTRAINT FK_reviews_restaurants
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id) ON DELETE CASCADE;

ALTER TABLE reviews
    ADD CONSTRAINT FK_Reviews_users
        FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE reviews
    ADD CONSTRAINT FK_Reviews_receipts
        FOREIGN KEY (receipt_id) REFERENCES receipts (receipt_id);

ALTER TABLE reviews
    ADD CONSTRAINT fk_reviews_blind_request_tag
        FOREIGN KEY (blind_request_tag_id) REFERENCES review_tags (tag_id);

# 03. 리뷰 태그 매핑 외래키 관계
ALTER TABLE review_tag_maps
    ADD CONSTRAINT PK_review_tag_maps
    PRIMARY KEY (review_id, tag_id); -- 복합키

ALTER TABLE review_tag_maps
    ADD CONSTRAINT FK_review
        FOREIGN KEY (review_id) REFERENCES reviews(review_id)
            ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE review_tag_maps
    ADD CONSTRAINT FK_review_tags
        FOREIGN KEY (tag_id) REFERENCES review_tags(tag_id)
            ON DELETE CASCADE ON UPDATE CASCADE;

# 04. 리뷰 이미지 테이블 외래키 관계
ALTER TABLE review_images
    ADD CONSTRAINT fk_reviews
        FOREIGN KEY (review_id) REFERENCES reviews(review_id)
            ON DELETE CASCADE ON UPDATE CASCADE;

# 05. 리뷰 댓글 테이블 외래키 관계
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_reviews
        FOREIGN KEY (review_id) REFERENCES reviews(review_id)
            ON DELETE CASCADE ON UPDATE CASCADE;

# 06. 영수증 테이블 외래키 관계
ALTER TABLE receipts
    ADD CONSTRAINT fk_receipts_reservations
        FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
            ON DELETE CASCADE ON UPDATE CASCADE;

# 07. 영수증 메뉴 상세 테이블 외래키 관계
ALTER TABLE receipt_items
    ADD CONSTRAINT fk_receipt_items
        FOREIGN KEY (receipt_id) REFERENCES receipts(receipt_id)
            ON DELETE CASCADE ON UPDATE CASCADE;

# 08. 구내식당 메뉴 테이블 외래키 관계
ALTER TABLE cafeteria_menus
    ADD CONSTRAINT fk_cafeteria_menus_users
        FOREIGN KEY (user_id) REFERENCES users(user_id)
            ON DELETE CASCADE ON UPDATE CASCADE;








