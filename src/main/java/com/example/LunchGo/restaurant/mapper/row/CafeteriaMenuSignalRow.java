package com.example.LunchGo.restaurant.mapper.row;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CafeteriaMenuSignalRow {
    private Long userId;
    private LocalDate servedDate;
    private String mainMenuNames;
    private String rawText;
}
