package com.example.LunchGo.account.helper;

import com.example.LunchGo.account.dto.*;
import com.example.LunchGo.block.service.BlockService;
import com.example.LunchGo.common.util.HttpUtils;
import com.example.LunchGo.common.util.TokenUtils;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.service.MemberService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@Log4j2
public class AccountHelperImpl implements AccountHelper {

    private final BlockService blockService;
    private final AuthenticationManager authenticationManager;
    private final MemberService memberService;
    private final TokenUtils tokenUtils;

    private static final int REFRESH_TOKEN_EXPIRE_TIME = 60 * 60 * 24; //second 단위 = 하루

    @Override
    public void userJoin(UserJoinRequest userReq) {
        memberService.save(userReq); //회원가입
    }

    @Override
    public void checkEmail(String email) {
        memberService.existsByEmail(email);
    }

    @Override
    public void ownerJoin(OwnerJoinRequest ownerReq) {
        memberService.save(ownerReq);
    }

    @Override
    public void checkLoginId(String loginId) {
        memberService.existsByLoginId(loginId);
    }

    @Override
    public String getEmail(UserFindRequest userReq) {
        //인증번호 전송 및 확인 로직

        User user = memberService.find(userReq.getName(), userReq.getPhone()); //없으면 404처리 완료
        return user.getEmail();
    }

    @Override
    public String getLoginId(OwnerFindRequest ownerReq) {
        //인증번호 전송 및 확인 로직

        Owner owner = memberService.find(ownerReq.getName(), ownerReq.getBusinessNum(), ownerReq.getPhone());
        return owner.getLoginId();
    }

    @Override
    public void checkMember(FindPwdRequest findPwdReq) {
        //인증번호 전송 및 확인 로직

        memberService.check(findPwdReq);
    }

    @Override
    public void changePwd(FindPwdRequest findPwdReq) {
        memberService.updatePwd(findPwdReq);
    }

    @Override
    public MemberLogin login(LoginRequest loginReq, HttpServletRequest request, HttpServletResponse response) {
        String combine = loginReq.getUserType()+":"+loginReq.getEmail();

        //토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(combine, loginReq.getPassword());

        //여기서 CustomUserDetailsService.loadUserByUsername 실행됨
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        TokenDTO tokenDTO = tokenUtils.generateTokenDTO(authentication);
        HttpUtils.setCookie(response, "refreshToken", tokenDTO.getRefreshToken(),REFRESH_TOKEN_EXPIRE_TIME);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        memberService.updateLastLoginAt(loginReq.getUserType(), userDetails.getId());

        return MemberLogin.builder()
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .role(userDetails.getRole())
                .name(userDetails.getName())
                .image(userDetails.getImage())
                .accessToken(tokenDTO.getAccessToken())
                .build();
    }

    @Override
    public boolean isLoggedIn(HttpServletRequest request) {
        String accessToken = getAccessToken(request);

        return accessToken != null && tokenUtils.validateToken(accessToken);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = getAccessToken(request);
        String refreshToken = getRefreshToken(request);

        if(accessToken != null && tokenUtils.validateToken(accessToken)) {
            Claims claims = tokenUtils.parseClaims(accessToken);

            Long id = Long.parseLong(claims.getSubject());
            String role = claims.get("auth").toString();

            tokenUtils.deleteRefreshToken(id, role);
        }

        if(refreshToken != null) {
            HttpUtils.removeCookie(response, "refreshToken");
        }
    }

    private String getAccessToken(HttpServletRequest req){
        return HttpUtils.getBearerToken(req);
    }

    private String getRefreshToken(HttpServletRequest req){
        return HttpUtils.getCookieValue(req, "refreshToken");
    }

    @Override
    public String regenerate(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshToken(request);

        if(refreshToken == null) return null;

        try {
            //TokenUtils를 통해 토큰 재발급 (검증 및 Redis 업데이트는 TokenUtils가 알아서 함)
            TokenDTO newTokenDTO = tokenUtils.reissueAccessToken(refreshToken);

            // 4. ★ 중요: 새 Refresh Token을 쿠키에 다시 덮어쓰기
            HttpUtils.setCookie(response, "refreshToken", newTokenDTO.getRefreshToken(), REFRESH_TOKEN_EXPIRE_TIME);

            // 5. 새 Access Token만 반환 (컨트롤러가 JSON으로 내려줄 것임)
            return newTokenDTO.getAccessToken();
        } catch (RuntimeException e) {
            // 토큰 위조, 만료, Redis 불일치 등 예외 발생 시 쿠키 삭제 후 null 반환
            log.error("토큰 재발급 실패: {}", e.getMessage());
            HttpUtils.removeCookie(response, "refreshToken");
            return null;
        }
    }
}
