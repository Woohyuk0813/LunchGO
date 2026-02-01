package com.example.LunchGo.cafeteria.controller;

import com.example.LunchGo.cafeteria.dto.CafeteriaMenuConfirmRequest;
import com.example.LunchGo.cafeteria.dto.CafeteriaMenuWeekResponse;
import com.example.LunchGo.cafeteria.dto.CafeteriaOcrResponse;
import com.example.LunchGo.cafeteria.dto.CafeteriaRecommendationResponse;
import com.example.LunchGo.cafeteria.service.CafeteriaMenuService;
import com.example.LunchGo.cafeteria.service.CafeteriaRecommendationService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cafeteria")
@RequiredArgsConstructor
public class CafeteriaMenuController {

    private final CafeteriaMenuService cafeteriaMenuService;
    private final CafeteriaRecommendationService cafeteriaRecommendationService;

    @PostMapping("/menus/ocr")
    public ResponseEntity<CafeteriaOcrResponse> recognizeMenu(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "userId", required = false) Long userId,
        @RequestParam(value = "baseDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
    ) {
        CafeteriaOcrResponse response = cafeteriaMenuService.recognizeMenus(userId, baseDate, file);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/menus/confirm")
    public ResponseEntity<CafeteriaMenuWeekResponse> confirmMenus(
        @RequestBody CafeteriaMenuConfirmRequest request
    ) {
        CafeteriaMenuWeekResponse response = cafeteriaMenuService.saveWeeklyMenus(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/menus/week")
    public ResponseEntity<CafeteriaMenuWeekResponse> getWeeklyMenus(
        @RequestParam("userId") Long userId,
        @RequestParam(value = "baseDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
    ) {
        CafeteriaMenuWeekResponse response = cafeteriaMenuService.getWeeklyMenus(userId, baseDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<CafeteriaRecommendationResponse> getRecommendations(
        @RequestParam("userId") Long userId,
        @RequestParam(value = "baseDate", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate,
        @RequestParam(value = "limit", required = false) Integer limit
    ) {
        CafeteriaRecommendationResponse response = limit == null
            ? cafeteriaRecommendationService.recommend(userId, baseDate)
            : cafeteriaRecommendationService.recommend(userId, baseDate, limit);
        return ResponseEntity.ok(response);
    }
}
