package com.example.LunchGo.reservation.mapper.row;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantUserStatsRow {
    private Integer visitCount;
    private LocalDate lastVisitDate;
}
