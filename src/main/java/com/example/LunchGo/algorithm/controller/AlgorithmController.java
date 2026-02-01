package com.example.LunchGo.algorithm.controller;

import com.example.LunchGo.algorithm.dto.TagMapDTO;
import com.example.LunchGo.algorithm.service.TagMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AlgorithmController {
    private final TagMapService tagMapService;

    @GetMapping("/restaurants/mapping/{userId}")
    public ResponseEntity<?> tagSimilarityByUser(@PathVariable Long userId) {
        List<TagMapDTO> list = tagMapService.getTagSimilarityByUser(userId);
        if (list.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND); //결과가 0일때 = 특이사항 등록하지 않은 경우
        
        return new ResponseEntity<>(list,HttpStatus.OK);
    }
}
