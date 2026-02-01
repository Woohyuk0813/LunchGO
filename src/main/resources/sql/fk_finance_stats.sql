# 01. 정산 테이블 외래키
ALTER TABLE settlements
    ADD CONSTRAINT FK_restaurants_settlements
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id)

;


# 02. 청구서 테이블 외래키
ALTER TABLE invoices
    ADD CONSTRAINT FK_restaurants_invoices
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id)
;

# 03. 청구서 납부 내역 테이블 외래키
ALTER TABLE invoice_payments
    ADD CONSTRAINT FK_invoice_payments
    FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id);

# 04. 청구 현황 스냅샷 테이블 외래키
ALTER TABLE invoice_snapshot
    ADD CONSTRAINT FK_invoice_snapshot
    FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id);

# 05. 일별 식당 통계 테이블 외래키
ALTER TABLE daily_restaurant_stats
    ADD CONSTRAINT PK_daily_restaurant_stats
        PRIMARY KEY (stat_date, restaurant_id);

ALTER TABLE daily_restaurant_stats
    ADD CONSTRAINT FK_daily_restaurant_stats
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id);

# 06. 식당별 단골 요약 테이블 외래키
ALTER TABLE restaurant_user_stats
    ADD CONSTRAINT PK_RESTAURANT_USER_STATS
        PRIMARY KEY (restaurant_id,user_id);

ALTER TABLE restaurant_user_stats
    ADD CONSTRAINT FK_Users_Restaurant_user_stats
        FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE restaurant_user_stats
    ADD CONSTRAINT FK_Restaurants_Restaurant_user_stats
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id);
