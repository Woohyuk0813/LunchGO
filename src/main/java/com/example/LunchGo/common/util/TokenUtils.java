package com.example.LunchGo.common.util;

import com.example.LunchGo.account.dto.CustomUserDetails;
import com.example.LunchGo.account.dto.TokenDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Log4j2
@Component
public class TokenUtils {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; //30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; //1일
    private final Key key;
    private final RedisUtil redisUtil;

    public TokenUtils(@Value("${custom.jwt.secret}") String secretKey, RedisUtil redisUtil) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisUtil = redisUtil;
    }

    //accessToken 발급
    private String generateAccessToken(Long id, String authority){
        long now = (new Date()).getTime();
        Date accessTokenExpireTime = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim(AUTHORITIES_KEY, authority)
                .setExpiration(accessTokenExpireTime)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //refreshToken 발급
    private String generateRefreshToken(Long id, String authority){
        long now = (new Date()).getTime();
        Date refreshTokenExpireTime = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim(AUTHORITIES_KEY, authority)
                .setExpiration(refreshTokenExpireTime)
                .claim("isRefreshToken", true)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //body 가져오기
    public Claims parseClaims(String accessToken){
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        }catch(ExpiredJwtException e){
            return e.getClaims();
        }
    }

    public TokenDTO generateTokenDTO(Authentication authentication){
        String authority = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        Long id = Long.valueOf(authentication.getName());

        // 중요: UserDetailsService에서 loadUserByUsername 할 때
        // UserDetails의 username 자리에 'DB의 PK(String)'를 넣어야함
        String accessToken = generateAccessToken(id, authority);
        String refreshToken= generateRefreshToken(id, authority);

        long now = (new Date()).getTime();

        //redis에 저장
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String redisKey = userDetails.getRole()+":"+userDetails.getId();
        redisUtil.setDataExpire(redisKey, refreshToken, REFRESH_TOKEN_EXPIRE_TIME);

        return TokenDTO.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(new Date(now + ACCESS_TOKEN_EXPIRE_TIME).getTime())
                .refreshToken(refreshToken)
                .build();
    }

    public TokenDTO reissueAccessToken(String refreshToken){
        Claims claims = parseClaims(refreshToken);

        if(!validateToken(refreshToken) || claims.get("isRefreshToken") == null || !Boolean.TRUE.equals(claims.get("isRefreshToken"))){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        Long id = Long.parseLong(claims.getSubject());
        String authority = claims.get(AUTHORITIES_KEY).toString();
        long now = (new Date()).getTime();

        String newAccessToken = generateAccessToken(id, authority);
        String newRefreshToken = generateRefreshToken(id, authority);

        //redis KEY!!
        String redisKey = authority + ":" + id;

        //redis에서 일치 여부 검사
        String savedRefreshToken = redisUtil.getData(redisKey);
        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"); //401
        }

        //redisUtil에 저장해야함
        redisUtil.setDataExpire(redisKey, newRefreshToken, REFRESH_TOKEN_EXPIRE_TIME);

        return TokenDTO.builder()
                .grantType(BEARER_TYPE)
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessTokenExpiresIn(new Date(now + ACCESS_TOKEN_EXPIRE_TIME).getTime())
                .build();
    }

    public boolean validateToken(String token){
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                log.warn("만료된 JWT 토큰입니다. exp={}", expiration);
                return false;
            }
            String subject = claims.getSubject();
            if (subject == null) {
                log.warn("JWT sub 누락");
                return false;
            }
            try {
                Long.parseLong(subject);
            } catch (NumberFormatException e) {
                log.warn("JWT sub 파싱 실패(숫자 아님): sub={}", subject);
                return false;
            }
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명/포맷입니다. cause={}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다. exp={}", e.getClaims().getExpiration());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다. cause={}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다. cause={}", e.getMessage());
        }
        return false;
    }

    //인증 객체 생성
    public Authentication getAuthentication(String accessToken){
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        Long memberId;
        try {
            memberId = Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            log.warn("JWT subject 파싱 실패: sub={}", claims.getSubject());
            throw e;
        }

        //CustomUserDetails 객체 만들고 Authentication 리턴
        CustomUserDetails principal = new CustomUserDetails(
                memberId,
                claims.get(AUTHORITIES_KEY).toString() //ROLE_
        );

        return new UsernamePasswordAuthenticationToken(principal,"", authorities);
    }

    //로그아웃 시 RefreshToken 삭제
    public void deleteRefreshToken(Long id, String authority){
        if(id != null){
            String redisKey = authority + ":" + id;
            redisUtil.deleteData(redisKey);
        }
    }
}
