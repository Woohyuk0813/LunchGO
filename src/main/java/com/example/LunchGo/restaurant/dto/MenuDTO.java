package com.example.LunchGo.restaurant.dto;

import com.example.LunchGo.restaurant.domain.MenuCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List; // List 임포트 추가

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {

    @JsonProperty("id")
    private Long menuId;
    private String name;
    private MenuCategory category;
    private Integer price;

    private String description;
    private String imageUrl;

    private List<MenuTagDTO> tags; // 메뉴 태그 리스트 필드 추가
}
