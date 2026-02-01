package com.example.LunchGo.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class ReceiptDTO {
    private Long receiptId;
    private String receiptImageKey;
    private String date;        // 2023-07-11
    private int totalAmount;    // 5500
    private List<Item> items;   // 메뉴 목록

    @Data
    @AllArgsConstructor
    public static class Item {
        private String name;    // 에스프레소
        private int quantity;   // 1
        private int price;      // 2500
    }
}
