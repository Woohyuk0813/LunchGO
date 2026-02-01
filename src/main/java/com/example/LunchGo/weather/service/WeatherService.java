package com.example.LunchGo.weather.service;

import com.example.LunchGo.weather.dto.WeatherResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${weather.openweather.base-url:https://api.openweathermap.org}")
    private String apiBase;

    @Value("${weather.openweather.api-key:}")
    private String apiKey;

    public WeatherResponse fetchCurrentWeather(double lat, double lon) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("OpenWeather API Key가 설정되지 않았습니다.");
        }

        String url =
            apiBase
                + "/data/2.5/weather?lat="
                + lat
                + "&lon="
                + lon
                + "&appid="
                + apiKey
                + "&units=metric&lang=kr";

        RestTemplate restTemplate = restTemplateBuilder.build();
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("OpenWeather 응답이 비어있습니다.");
        }

        Map main = castMap(body.get("main"));
        Double temp = numberValue(main != null ? main.get("temp") : null);
        Double feelsLike = numberValue(main != null ? main.get("feels_like") : null);

        Map weather = null;
        Object weatherObj = body.get("weather");
        if (weatherObj instanceof List) {
            List list = (List) weatherObj;
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                weather = (Map) list.get(0);
            }
        }

        String condition = stringValue(weather != null ? weather.get("main") : null);
        String description = stringValue(weather != null ? weather.get("description") : null);

        return new WeatherResponse(temp, feelsLike, condition, description);
    }

    private String stringValue(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Double numberValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map castMap(Object value) {
        return value instanceof Map ? (Map) value : null;
    }
}
