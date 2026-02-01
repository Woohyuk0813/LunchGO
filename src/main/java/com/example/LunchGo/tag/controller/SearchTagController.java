package com.example.LunchGo.tag.controller;

import com.example.LunchGo.tag.dto.SearchTagDTO;
import com.example.LunchGo.tag.service.SearchTagService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchTagController {

    private final SearchTagService searchTagService;

    @GetMapping("/tags/search")
    public ResponseEntity<Map<String, List<SearchTagDTO>>> getTags(@RequestParam(required = false) List<String> categories) {
        Map<String, List<SearchTagDTO>> groupedTags = searchTagService.getGroupedTags(categories);
        return ResponseEntity.ok(groupedTags);
    }
}
