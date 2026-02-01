package com.example.LunchGo.member.dto;

import com.example.LunchGo.member.domain.OwnerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerInfo {
    private String loginId;
    private String name;
    private String phone;
    private String businessNum;
    private LocalDate startAt;
    private String image;
    private OwnerStatus status;
}
