package com.example.LunchGo.review.controller;

import com.example.LunchGo.review.dto.TagResponse;
import com.example.LunchGo.review.entity.ReviewTag;
import com.example.LunchGo.review.repository.ReviewTagRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews/tags")
public class ReviewTagController {

    private final ReviewTagRepository reviewTagRepository;

    @GetMapping
    public ResponseEntity<List<TagResponse>> list() {
        List<TagResponse> tags = reviewTagRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(tags);
    }

    private TagResponse toResponse(ReviewTag tag) {
        return new TagResponse(tag.getTagId(), tag.getName());
    }
}
