package com.example.LunchGo.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantCreateRequest {
    private String name;
    private String phone;
    private String roadAddress;
    private String detailAddress;
    private String description;
    private Integer reservationLimit;

    private boolean holidayAvailable;
    private boolean preorderAvailable;

    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalDate openDate;

    private List<String> imageUrls;
    private List<Integer> regularHolidayNumbers;    // 1이면 일요일, 7이면 토요일
    private List<Long> selectedTagIds;
    private List<MenuDTO> menus;
    private Integer avgMainPrice;
}
