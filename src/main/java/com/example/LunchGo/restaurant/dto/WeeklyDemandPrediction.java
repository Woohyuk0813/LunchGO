package com.example.LunchGo.restaurant.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyDemandPrediction {
    private int weekday; // 1=Sun..7=Sat
    private int expectedMin;
    private int expectedMax;
    private String confidence; // LOW/MEDIUM/HIGH
    private List<String> evidence;
}
