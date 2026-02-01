package com.example.LunchGo.member.service;

import com.example.LunchGo.account.dto.FindPwdRequest;
import com.example.LunchGo.account.dto.OwnerJoinRequest;
import com.example.LunchGo.account.dto.UserJoinRequest;
import com.example.LunchGo.member.dto.*;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MemberService {
    void save(UserJoinRequest userReq);

    void existsByEmail(String email);

    void save(OwnerJoinRequest ownerReq);

    void existsByLoginId(String loginId);

    User find(String name, String phone);

    Owner find(String name, String businessNum, String phone);

    void check(FindPwdRequest findPwdReq);

    void updatePwd(FindPwdRequest findPwdReq);

    MemberInfo getMemberInfo(Long userId);

    void updateMemberInfo(Long userId, MemberUpdateInfo memberUpdateInfo, MultipartFile image);

    OwnerInfo getOwnerInfo(Long ownerId);

    void updateOwnerInfo(Long ownerId, OwnerUpdateInfo ownerUpdateInfo, MultipartFile image);

    List<String> getEmails(Long ownerId);

    void save(StaffInfo staffInfo);

    void delete(StaffInfo staffInfo);

    List<StaffInfo> getStaffs(Long ownerId);

    void updateLastLoginAt(String userType, Long memberId);

    List<OwnerInfo> getOwners();
}
