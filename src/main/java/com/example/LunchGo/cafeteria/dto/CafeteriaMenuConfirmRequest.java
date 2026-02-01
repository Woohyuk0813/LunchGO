package com.example.LunchGo.cafeteria.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CafeteriaMenuConfirmRequest {
    private Long userId;
    private String imageUrl;
    private String rawText;
    private List<CafeteriaDayMenuDto> days;
}
