package com.example.LunchGo.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfo {
    private String email;
    private String name;
    private String nickname;
    private String phone;
    private LocalDate birth;
    private String gender;
    private String image;
    private String companyName;
    private String companyAddress;
    private Boolean emailAuthentication;
    private List<Speciality> specialities;
}
