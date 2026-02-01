package com.example.LunchGo.account.helper;

import com.example.LunchGo.account.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AccountHelper {

    void userJoin(UserJoinRequest userReq);

    void checkEmail(String email);

    void ownerJoin(OwnerJoinRequest userReq);

    void checkLoginId(String loginId);

    String getEmail(UserFindRequest userReq);

    String getLoginId(OwnerFindRequest ownerReq);

    void checkMember(FindPwdRequest findPwdReq);

    void changePwd(FindPwdRequest findPwdReq);

    /**
     * Security 추가 후
     * */
    MemberLogin login(LoginRequest loginReq, HttpServletRequest request, HttpServletResponse response);

    boolean isLoggedIn(HttpServletRequest request);

    void logout(HttpServletRequest request, HttpServletResponse response);

    String regenerate(HttpServletRequest request, HttpServletResponse response);
}
