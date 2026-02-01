package com.example.LunchGo.algorithm.dto;

import com.example.LunchGo.restaurant.repository.RestaurantSimilarityProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagMapDTO {
    private Long restaurantId;
    private double likeScore;
    private double dislikePenalty;
    private double finalScore;

    public static TagMapDTO fromProjection(RestaurantSimilarityProjection projection) {
        if (projection == null) {
            return null;
        }
        return TagMapDTO.builder()
            .restaurantId(projection.getRestaurantId())
            .likeScore(valueOrZero(projection.getLikeScore()))
            .dislikePenalty(valueOrZero(projection.getDislikePenalty()))
            .finalScore(valueOrZero(projection.getFinalScore()))
            .build();
    }

    private static double valueOrZero(Double value) {
        return value == null ? 0.0 : value;
    }
}
