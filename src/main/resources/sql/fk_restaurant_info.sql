-- 2. 식당 (owner 테이블 연결 시 주석 해제)
-- ALTER TABLE restaurant ADD CONSTRAINT fk_restaurant_owner FOREIGN KEY (owner_id) REFERENCES owner(owner_id);

-- 3. 식당 이미지 FK
ALTER TABLE restaurant_images
    ADD CONSTRAINT fk_restaurant_image_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE;

-- 4. 식당 메뉴 FK
ALTER TABLE menus
    ADD CONSTRAINT fk_menu_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE;

-- 5. 식당 메뉴 이미지 FK
ALTER TABLE menu_images
    ADD CONSTRAINT fk_menu_image_menu
        FOREIGN KEY (menu_id) REFERENCES menus(menu_id) ON DELETE CASCADE;

-- 6. 식당-태그 매핑 FK
ALTER TABLE restaurant_tag_maps
    ADD CONSTRAINT fk_restaurant_tag_map_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE;

ALTER TABLE restaurant_tag_maps
    ADD CONSTRAINT fk_restaurant_tag_map_tag
        FOREIGN KEY (tag_id) REFERENCES search_tags(tag_id) ON DELETE CASCADE;

-- 7. 식당메뉴-태그 매핑 FK
ALTER TABLE menu_tag_maps
    ADD CONSTRAINT fk_menu_tag_map_menu
        FOREIGN KEY (menu_id) REFERENCES menus(menu_id) ON DELETE CASCADE;

ALTER TABLE menu_tag_maps
    ADD CONSTRAINT fk_menu_tag_map_tag
        FOREIGN KEY (tag_id) REFERENCES search_tags(tag_id) ON DELETE CASCADE;

-- 8. 정기 휴무일 FK
ALTER TABLE regular_holidays
    ADD CONSTRAINT fk_regular_holiday_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE;

-- 9. 임시 휴무일 FK
ALTER TABLE temporary_holidays
    ADD CONSTRAINT fk_temporary_holiday_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants(restaurant_id) ON DELETE CASCADE;

-- 10. 사용자 즐겨찾기
alter table bookmarks add constraint fk_bookmarks_restaurants foreign key (restaurant_id) references restaurants(restaurant_id) on delete cascade ;
