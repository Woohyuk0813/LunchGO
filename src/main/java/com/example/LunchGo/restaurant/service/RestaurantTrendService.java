package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.TrendingRestaurantItem;
import com.example.LunchGo.restaurant.dto.TrendingTagItem;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import com.example.LunchGo.restaurant.repository.TrendingRestaurantProjection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RestaurantTrendService {

    private static final int DEFAULT_DAYS = 7;
    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_DAYS = 30;
    private static final int MAX_LIMIT = 50;

    private final RestaurantRepository restaurantRepository;
    private final double newbieWeight;
    private final double confirmWeight;
    private final double viewWeight;
    private final String defaultImageUrl;

    public RestaurantTrendService(
            RestaurantRepository restaurantRepository,
            Environment environment,
            @Value("${trend.newbie-weight:50}") double newbieWeight,
            @Value("${trend.confirm-weight:0.8}") double confirmWeight,
            @Value("${trend.visit-weight:0.8}") double visitWeight,
            @Value("${trend.view-weight:0.2}") double viewWeight,
            @Value("${trend.default-image-url:/placeholder.svg}") String defaultImageUrl
    ) {
        this.restaurantRepository = restaurantRepository;
        this.newbieWeight = newbieWeight;
        this.confirmWeight = resolveConfirmWeight(environment, confirmWeight, visitWeight);
        this.viewWeight = viewWeight;
        this.defaultImageUrl = defaultImageUrl;
    }

    public List<TrendingRestaurantItem> getTrendingRestaurants(Integer days, Integer limit) {
        int safeDays = normalize(days, DEFAULT_DAYS, 1, MAX_DAYS);
        int safeLimit = normalize(limit, DEFAULT_LIMIT, 1, MAX_LIMIT);

        List<TrendingRestaurantProjection> rows = restaurantRepository.findTrendingRestaurants(
                safeDays,
                safeLimit,
                newbieWeight,
                confirmWeight,
                viewWeight
        );
        return rows.stream()
                .map(row -> TrendingRestaurantItem.builder()
                        .restaurantId(row.getRestaurantId())
                        .name(row.getName())
                        .roadAddress(row.getRoadAddress())
                        .detailAddress(row.getDetailAddress())
                        .viewCount(valueOrZero(row.getViewCount()))
                        .confirmCount(valueOrZero(row.getConfirmCount()))
                        .reviewCount(valueOrZero(row.getReviewCount()))
                        .rating(valueOrZero(row.getRating()))
                        .score(valueOrZero(row.getScore()))
                        .imageUrl(resolveImageUrl(row.getImageUrl()))
                        .tags(buildTags(row.getTagIds(), row.getTagContents(), null))
                        .reviewTags(buildTags(
                                row.getReviewTagIds(),
                                row.getReviewTagContents(),
                                row.getReviewTagCounts()
                        ))
                        .build())
                .toList();
    }

    private int normalize(Integer value, int fallback, int min, int max) {
        if (value == null) {
            return fallback;
        }
        return Math.min(Math.max(value, min), max);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private List<TrendingTagItem> buildTags(String tagIds, String tagContents, String tagCounts) {
        if (tagIds == null || tagIds.isBlank() || tagContents == null || tagContents.isBlank()) {
            return Collections.emptyList();
        }
        String[] idParts = tagIds.split(",");
        String[] contentParts = tagContents.split(",");
        String[] countParts = tagCounts == null ? new String[0] : tagCounts.split(",");
        int size = Math.min(idParts.length, contentParts.length);
        if (size == 0) {
            return Collections.emptyList();
        }
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> {
                    String idPart = idParts[index].trim();
                    String contentPart = contentParts[index].trim();
                    if (idPart.isEmpty() || contentPart.isEmpty()) {
                        return null;
                    }
                    try {
                        Integer countValue = null;
                        if (countParts.length > index) {
                            String countPart = countParts[index].trim();
                            if (!countPart.isEmpty()) {
                                countValue = Integer.parseInt(countPart);
                            }
                        }
                        return TrendingTagItem.builder()
                                .tagId(Long.parseLong(idPart))
                                .content(contentPart)
                                .count(countValue)
                                .build();
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(tag -> tag != null)
                .toList();
    }

    private String resolveImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return defaultImageUrl;
        }
        return imageUrl;
    }

    private double resolveConfirmWeight(
            Environment environment,
            double confirmWeight,
            double visitWeight
    ) {
        if (environment != null && environment.containsProperty("trend.confirm-weight")) {
            return confirmWeight;
        }
        if (environment != null && environment.containsProperty("trend.visit-weight")) {
            return visitWeight;
        }
        return confirmWeight;
    }
}
