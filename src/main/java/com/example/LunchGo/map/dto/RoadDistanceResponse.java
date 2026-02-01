package com.example.LunchGo.map.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoadDistanceResponse {
    private List<DistanceResult> distances;

    @Getter
    @Builder
    public static class DistanceResult {
        private Long id;
        private Integer distanceMeters;
    }
}
