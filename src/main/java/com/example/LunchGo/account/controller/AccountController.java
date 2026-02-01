package com.example.LunchGo.account.controller;

import com.example.LunchGo.account.dto.*;
import com.example.LunchGo.account.helper.AccountHelper;
import com.example.LunchGo.account.service.LoginQueueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api")
public class AccountController {
    private final AccountHelper accountHelper;
    private final LoginQueueService loginQueueService;

    @PostMapping("/join/user")
    public ResponseEntity<?> userJoin(@RequestBody UserJoinRequest userReq) {
        if(!StringUtils.hasLength(userReq.getEmail()) || !StringUtils.hasLength(userReq.getPassword()) || !StringUtils.hasLength(userReq.getName()) ||
           !StringUtils.hasLength(userReq.getPhone()) || !StringUtils.hasLength(userReq.getCompanyName()) || !StringUtils.hasLength(userReq.getCompanyAddress())) {
            //이메일 수신 동의는 필수 아님
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        accountHelper.userJoin(userReq);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/email")
    public ResponseEntity<?> checkEmail(@RequestBody UserJoinRequest userReq) {
        if(!StringUtils.hasLength(userReq.getEmail())) { //이메일 검증이니 이메일만 체크
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        accountHelper.checkEmail(userReq.getEmail()); //존재하면 여기서 409 에러 던짐
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/join/owner")
    public ResponseEntity<?> ownerJoin(@RequestBody OwnerJoinRequest ownerReq) {
        if(!StringUtils.hasLength(ownerReq.getLoginId()) || !StringUtils.hasLength(ownerReq.getPassword()) || !StringUtils.hasLength(ownerReq.getName()) ||
            !StringUtils.hasLength(ownerReq.getPhone()) || !StringUtils.hasLength(ownerReq.getBusinessNum())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        accountHelper.ownerJoin(ownerReq);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/loginId")
    public ResponseEntity<?> checkLoginId(@RequestBody OwnerJoinRequest ownerReq) {
        if(!StringUtils.hasLength(ownerReq.getLoginId())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        accountHelper.checkLoginId(ownerReq.getLoginId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/auth/search/email")
    public ResponseEntity<?> searchEmail(@RequestBody UserFindRequest userReq) {
        if(!StringUtils.hasLength(userReq.getName()) || !StringUtils.hasLength(userReq.getPhone())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String findEmail = accountHelper.getEmail(userReq);

        Map<String, String> response = Collections.singletonMap("email", findEmail); //찾은 이메일 객체에 담아 보내기

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/search/loginId")
    public ResponseEntity<?> searchLoginId(@RequestBody OwnerFindRequest ownerReq) {
        if(!StringUtils.hasLength(ownerReq.getName()) || !StringUtils.hasLength(ownerReq.getBusinessNum())||!StringUtils.hasLength(ownerReq.getPhone())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String findLoginId = accountHelper.getLoginId(ownerReq);

        Map<String, String> response = Collections.singletonMap("loginId", findLoginId); //찾은 아이디 객체에 담아 보내기

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/auth/search/pwd")
    public ResponseEntity<?> searchPwd(@RequestBody FindPwdRequest findPwdReq) {
        if(!StringUtils.hasLength(findPwdReq.getPhone()) || !StringUtils.hasLength(findPwdReq.getName())){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        accountHelper.checkMember(findPwdReq); //사용자나 사업자가 없으면 404, 이메일이나 아이디를 입력하지 않았으면 400 던짐
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/auth/pwd")
    public ResponseEntity<?> changePwd(@RequestBody FindPwdRequest findPwdReq) {
        if(!StringUtils.hasLength(findPwdReq.getPassword())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        accountHelper.changePwd(findPwdReq); //update 시 문제 404, 이메일이나 아이디가 전달되지 않았으면 400 던짐
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Spring Security
     * */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = accountHelper.regenerate(request, response);

        if(newAccessToken == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); //401
        }

        Map<String, String> responseBody = Collections.singletonMap("accessToken", newAccessToken);
        return new ResponseEntity<>(responseBody, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response, @RequestBody LoginRequest loginReq) {
        if(!StringUtils.hasLength(loginReq.getEmail()) || !StringUtils.hasLength(loginReq.getPassword()) ||
        !StringUtils.hasLength(loginReq.getUserType())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        LoginQueueStatusResponse queueStatus = null;
        if (loginQueueService.isEnabled()) {
            queueStatus = loginQueueService.getStatus(loginReq.getQueueToken());
            if (!queueStatus.isAllowed()) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(queueStatus);
            }
        }

        boolean shouldReleaseQueue = queueStatus != null && queueStatus.isAllowed();
        try {
            MemberLogin memberLogin = accountHelper.login(loginReq, request, response);
            return new ResponseEntity<>(memberLogin, HttpStatus.OK);
        }catch(BadCredentialsException e) {
            //password 틀렸거나 id가 존재하지 않음
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); //401 return
        }catch(Exception e){ //그 외의 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 중 오류가 발생했습니다.");
        } finally {
            if (shouldReleaseQueue) {
                loginQueueService.release(loginReq.getQueueToken());
            }
        }
    }

    @PostMapping("/login/queue")
    public ResponseEntity<LoginQueueStatusResponse> joinLoginQueue() {
        return ResponseEntity.ok(loginQueueService.joinQueue());
    }

    @GetMapping("/login/queue")
    public ResponseEntity<LoginQueueStatusResponse> loginQueueStatus(@RequestParam("token") String token) {
        return ResponseEntity.ok(loginQueueService.getStatus(token));
    }

    @DeleteMapping("/login/queue")
    public ResponseEntity<?> leaveLoginQueue(@RequestParam("token") String token) {
        loginQueueService.release(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/auth/check")
    public ResponseEntity<?> check(HttpServletRequest request) {
        //프론트에서 true, false 처리해야함
        return new ResponseEntity<>(accountHelper.isLoggedIn(request), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        accountHelper.logout(request, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
