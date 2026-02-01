package com.example.LunchGo.map.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoadDistanceRequest {
    private Coordinate origin;
    private List<Destination> destinations;

    @Getter
    @Setter
    public static class Coordinate {
        private Double lat;
        private Double lng;
    }

    @Getter
    @Setter
    public static class Destination {
        private Long id;
        private Double lat;
        private Double lng;
    }
}
