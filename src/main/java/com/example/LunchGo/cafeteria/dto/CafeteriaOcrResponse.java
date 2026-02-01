package com.example.LunchGo.cafeteria.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CafeteriaOcrResponse {
    private boolean ocrSuccess;
    private String imageUrl;
    private String imageKey;
    private String rawText;
    private List<String> detectedMenus;
    private List<String> unassignedMenus;
    private List<CafeteriaDayMenuDto> days;
}
