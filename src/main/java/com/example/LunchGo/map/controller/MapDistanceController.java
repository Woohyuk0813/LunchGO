package com.example.LunchGo.map.controller;

import com.example.LunchGo.map.dto.RoadDistanceRequest;
import com.example.LunchGo.map.dto.RoadDistanceResponse;
import com.example.LunchGo.map.dto.RoadRouteRequest;
import com.example.LunchGo.map.dto.RoadRouteResponse;
import com.example.LunchGo.map.service.KakaoDirectionsService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapDistanceController {
    private final KakaoDirectionsService kakaoDirectionsService;

    @PostMapping("/distance")
    public ResponseEntity<RoadDistanceResponse> getRoadDistances(
        @RequestBody RoadDistanceRequest request
    ) {
        if (request == null || request.getOrigin() == null || request.getDestinations() == null) {
            return new ResponseEntity<>(RoadDistanceResponse.builder().distances(List.of()).build(), HttpStatus.OK);
        }
        Map<Long, Integer> distances =
            kakaoDirectionsService.fetchDistances(request.getOrigin(), request.getDestinations());
        List<RoadDistanceResponse.DistanceResult> results = distances.entrySet().stream()
            .map(entry -> RoadDistanceResponse.DistanceResult.builder()
                .id(entry.getKey())
                .distanceMeters(entry.getValue())
                .build())
            .collect(Collectors.toList());
        return new ResponseEntity<>(
            RoadDistanceResponse.builder().distances(results).build(),
            HttpStatus.OK
        );
    }

    @PostMapping("/route")
    public ResponseEntity<RoadRouteResponse> getRoadRoute(
        @RequestBody RoadRouteRequest request
    ) {
        if (request == null || request.getOrigin() == null || request.getDestination() == null) {
            return new ResponseEntity<>(
                RoadRouteResponse.builder().path(List.of()).build(),
                HttpStatus.OK
            );
        }
        RoadRouteResponse response =
            kakaoDirectionsService.fetchRoute(request.getOrigin(), request.getDestination());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
