package com.example.LunchGo.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Speciality {
    private Long specialityId;
    private String keyword;
    private boolean isLiked;
}
