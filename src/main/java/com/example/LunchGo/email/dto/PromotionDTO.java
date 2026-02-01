package com.example.LunchGo.email.dto;

import lombok.Data;

@Data
public class PromotionDTO {
    private Long ownerId;
    private String title;
    private String content;
}
