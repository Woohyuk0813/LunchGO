package com.example.LunchGo.map.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * 카카오 로컬 API를 이용해 주소를 좌표로 변환하는 서비스.
 * Spring Cache(@Cacheable)를 사용하여 변환 결과를 Redis에 캐싱합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoGeoService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kakao.rest-api-map-key:}")
    private String kakaoRestApiKey;

    private static final String KAKAO_API_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    /**
     * 주소를 좌표(위도, 경도)로 변환합니다.
     * 결과가 Redis에 존재하면 즉시 반환하고, 없으면 카카오 API를 호출한 뒤 저장합니다. (TTL: 1시간)
     */
    @Cacheable(value = "geoCoordinates", key = "#address", unless = "#result == null")
    public GeoCoordinate getCoordinateByAddress(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }

        if (kakaoRestApiKey == null || kakaoRestApiKey.isBlank()) {
            log.debug("Kakao REST API Key is missing. Skipping geocoding for: {}", address);
            return null;
        }

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(KAKAO_API_URL)
                    .queryParam("query", address)
                    .build()
                    .encode()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode documents = root.path("documents");

            if (documents.isArray() && !documents.isEmpty()) {
                JsonNode firstMatch = documents.get(0);
                double longitude = firstMatch.path("x").asDouble();
                double latitude = firstMatch.path("y").asDouble();

                return new GeoCoordinate(latitude, longitude);
            }

        } catch (RestClientException | JsonProcessingException e) {
            log.warn("Failed to geocode address due to API error or parsing error: {} - {}", address, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during geocoding for address: {}", address, e);
        }

        return null;
    }

    public record GeoCoordinate(double latitude, double longitude) {}
}
