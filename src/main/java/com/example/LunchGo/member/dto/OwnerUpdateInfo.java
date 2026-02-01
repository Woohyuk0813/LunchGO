package com.example.LunchGo.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OwnerUpdateInfo {
    private String phone;
    private String image;
}
