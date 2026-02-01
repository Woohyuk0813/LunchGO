package com.example.LunchGo.map.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoadRouteResponse {
    private Integer distanceMeters;
    private Integer durationSeconds;
    private List<Coordinate> path;

    @Getter
    @Builder
    public static class Coordinate {
        private Double lat;
        private Double lng;
    }
}
