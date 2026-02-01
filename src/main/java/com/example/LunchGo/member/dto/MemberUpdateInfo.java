package com.example.LunchGo.member.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberUpdateInfo {
    private String nickname;
    private LocalDate birth;
    private String gender;
    private String phone;
    private String companyName;
    private String companyAddress;
    private String image;
    private Boolean emailAuthentication;
    private List<Long> specialities; // 특이사항 id만
}
