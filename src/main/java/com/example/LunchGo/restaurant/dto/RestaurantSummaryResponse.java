package com.example.LunchGo.restaurant.dto;

import lombok.*;

/**
 * 식당 목록 조회 시 사용되는 요약 정보 DTO.
 * 데이터 일관성을 위해 불변(Immutable) 객체로 설계되었으며,
 * 필드 변경이 필요한 경우 toBuilder()를 사용하여 새로운 객체를 생성합니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RestaurantSummaryResponse {

    private Long id;
    private String name;
    private String image;
    private String roadAddress;
    private String detailAddress;
    private Double rating;
    private Long reviews;
    private String category;
    private Integer price;
    private Double latitude;
    private Double longitude;

}
