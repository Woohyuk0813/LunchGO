package com.example.LunchGo.restaurant.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "regular_holidays")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RegularHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reg_holiday_id")
    private Long regHolidayId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;
}
