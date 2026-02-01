package com.example.LunchGo.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemResponse {
    private String name;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;
}
