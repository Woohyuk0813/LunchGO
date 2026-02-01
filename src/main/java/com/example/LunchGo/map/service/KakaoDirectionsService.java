package com.example.LunchGo.map.service;

import com.example.LunchGo.map.dto.RoadDistanceRequest;
import com.example.LunchGo.map.dto.RoadRouteRequest;
import com.example.LunchGo.map.dto.RoadRouteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class KakaoDirectionsService {
    private static final String DIRECTIONS_URL = "https://apis-navi.kakaomobility.com/v1/directions";

    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;

    @Value("${kakao.rest-api-key:}")
    private String kakaoRestApiKey;

    public Map<Long, Integer> fetchDistances(
        RoadDistanceRequest.Coordinate origin,
        List<RoadDistanceRequest.Destination> destinations
    ) {
        Map<Long, Integer> result = new HashMap<>();
        if (origin == null || destinations == null || destinations.isEmpty()) {
            return result;
        }
        if (kakaoRestApiKey == null || kakaoRestApiKey.isBlank()) {
            return result;
        }

        RestTemplate restTemplate = restTemplateBuilder.build();
        for (RoadDistanceRequest.Destination destination : destinations) {
            Integer distance = fetchDistance(origin, destination, restTemplate);
            if (destination.getId() != null && distance != null) {
                result.put(destination.getId(), distance);
            }
        }
        return result;
    }

    public RoadRouteResponse fetchRoute(
        RoadRouteRequest.Coordinate origin,
        RoadRouteRequest.Coordinate destination
    ) {
        if (origin == null || destination == null) {
            return RoadRouteResponse.builder().path(List.of()).build();
        }
        if (origin.getLat() == null || origin.getLng() == null) {
            return RoadRouteResponse.builder().path(List.of()).build();
        }
        if (destination.getLat() == null || destination.getLng() == null) {
            return RoadRouteResponse.builder().path(List.of()).build();
        }
        if (kakaoRestApiKey == null || kakaoRestApiKey.isBlank()) {
            return RoadRouteResponse.builder().path(List.of()).build();
        }

        RestTemplate restTemplate = restTemplateBuilder.build();
        String originParam = origin.getLng() + "," + origin.getLat();
        String destinationParam = destination.getLng() + "," + destination.getLat();

        String url = UriComponentsBuilder.fromHttpUrl(DIRECTIONS_URL)
            .queryParam("origin", originParam)
            .queryParam("destination", destinationParam)
            .queryParam("priority", "RECOMMEND")
            .build()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return RoadRouteResponse.builder().path(List.of()).build();
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode routeNode = root.path("routes").path(0);
            int distance = routeNode.path("summary").path("distance").asInt(0);
            int duration = routeNode.path("summary").path("duration").asInt(0);

            List<RoadRouteResponse.Coordinate> path = new ArrayList<>();
            JsonNode sections = routeNode.path("sections");
            if (sections.isArray()) {
                for (JsonNode section : sections) {
                    JsonNode roads = section.path("roads");
                    if (!roads.isArray()) continue;
                    for (JsonNode road : roads) {
                        JsonNode vertexes = road.path("vertexes");
                        if (!vertexes.isArray()) continue;
                        for (int i = 0; i + 1 < vertexes.size(); i += 2) {
                            double lng = vertexes.get(i).asDouble();
                            double lat = vertexes.get(i + 1).asDouble();
                            path.add(
                                RoadRouteResponse.Coordinate.builder()
                                    .lat(lat)
                                    .lng(lng)
                                    .build()
                            );
                        }
                    }
                }
            }

            return RoadRouteResponse.builder()
                .distanceMeters(distance > 0 ? distance : null)
                .durationSeconds(duration > 0 ? duration : null)
                .path(path)
                .build();
        } catch (Exception error) {
            return RoadRouteResponse.builder().path(List.of()).build();
        }
    }

    private Integer fetchDistance(
        RoadDistanceRequest.Coordinate origin,
        RoadDistanceRequest.Destination destination,
        RestTemplate restTemplate
    ) {
        if (origin.getLat() == null || origin.getLng() == null) return null;
        if (destination.getLat() == null || destination.getLng() == null) return null;

        String originParam = origin.getLng() + "," + origin.getLat();
        String destinationParam = destination.getLng() + "," + destination.getLat();

        String url = UriComponentsBuilder.fromHttpUrl(DIRECTIONS_URL)
            .queryParam("origin", originParam)
            .queryParam("destination", destinationParam)
            .queryParam("priority", "RECOMMEND")
            .build()
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode distanceNode = root.path("routes").path(0).path("summary").path("distance");
            if (distanceNode.isMissingNode()) {
                return null;
            }
            int distance = distanceNode.asInt(-1);
            return distance > 0 ? distance : null;
        } catch (Exception error) {
            return null;
        }
    }
}
