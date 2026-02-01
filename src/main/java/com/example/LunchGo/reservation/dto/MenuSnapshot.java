package com.example.LunchGo.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenuSnapshot {
    private final Long menuId;
    private final String menuName;
    private final Integer unitPrice;
    private final Integer quantity;
    private final Integer lineAmount;
}
