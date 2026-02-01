package com.example.LunchGo.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuTagMappingDTO {
    // N+1 문제 해결용 DTO
    private Long menuId;
    private Long tagId;
}
