package com.example.LunchGo.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StaffInfo {
    private Long ownerId; //사업자 id
    private String email; //사용자 이메일
    private Long staffId; //삭제시 필요한 staffId
    private String name;
}
