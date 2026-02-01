package com.example.LunchGo.map.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoadRouteRequest {
    private Coordinate origin;
    private Coordinate destination;

    @Getter
    @Setter
    public static class Coordinate {
        private Double lat;
        private Double lng;
    }
}
