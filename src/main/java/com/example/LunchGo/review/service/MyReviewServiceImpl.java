package com.example.LunchGo.review.service;

import com.example.LunchGo.review.dto.MyReviewItem;
import com.example.LunchGo.review.forbidden.ForbiddenWordService;
import com.example.LunchGo.review.mapper.MyReviewMapper;
import com.example.LunchGo.review.mapper.row.MyReviewRow;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyReviewServiceImpl implements MyReviewService {

    private final MyReviewMapper myReviewMapper;
    private final ForbiddenWordService forbiddenWordService;

    @Override
    public List<MyReviewItem> getMyReviews(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }

        List<MyReviewRow> rows = myReviewMapper.selectMyReviews(userId);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        return rows.stream()
            .map(this::mapRow)
            .collect(Collectors.toList());
    }

    private MyReviewItem mapRow(MyReviewRow row) {
        return new MyReviewItem(
            row.getReviewId(),
            row.getReservationId(),
            new MyReviewItem.RestaurantInfo(
                row.getRestaurantId(),
                row.getRestaurantName(),
                row.getRestaurantAddress()
            ),
            row.getVisitCount(),
            row.getRating(),
            row.getCreatedAt(),
            row.getVisitDate(),
            forbiddenWordService.maskForbiddenWords(row.getContent()),
            row.getStatus(),
            splitList(row.getTags()),
            splitList(row.getImages())
        );
    }

    private List<String> splitList(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.asList(raw.split("\\|\\|"));
    }
}
