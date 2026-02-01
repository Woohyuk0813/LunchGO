package com.example.LunchGo.restaurant.mapper.row;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PreferenceSignalRow {
    private Long userId;
    private String keyword;
    private boolean liked;
}
