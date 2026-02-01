package com.example.LunchGo.restaurant.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeeklyBaselineRow {
    private int weekday;
    private double avgCount;
    private double stddev;
}
