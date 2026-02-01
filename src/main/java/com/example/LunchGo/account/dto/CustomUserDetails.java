package com.example.LunchGo.account.dto;

import com.example.LunchGo.member.entity.Manager;
import com.example.LunchGo.member.entity.Owner;
import com.example.LunchGo.member.entity.Staff;
import com.example.LunchGo.member.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {
    private final Long id; // PK
    private final String email;
    private final String password;
    private final String role;
    private final String status; //user, owner의 상태
    private final String name; //user, owner, staff 필요
    private final String image; //user 필요

    public CustomUserDetails(Long id, String email, String password, String role, String status, String name, String image) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.name = name;
        this.image = image;
    }

    // JWT 인증 필터용 생성자
    // id와 role만 받아서 처리
    public CustomUserDetails(Long id, String role) {
        this.id = id;
        this.role = role;
        this.email = "";      // 토큰엔 이메일이 없으니 빈 값 (필요하면 토큰에 email도 넣어야 함)
        this.password = "";   // 인증된 토큰이므로 비밀번호 불필요
        this.status = "ACTIVE"; // 토큰이 유효하다는 건 활동 가능한 상태로 간주 (isEnabled 통과를 위해 ACTIVE 필수)
        this.name = "";
        this.image = "";
    }

    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(
                user.getUserId(), user.getEmail(), user.getPassword(),"ROLE_USER", user.getStatus().name(), user.getName(), user.getImage()
        );
    }

    public static CustomUserDetails from(Owner owner){
        return new CustomUserDetails(
                owner.getOwnerId(), owner.getLoginId(), owner.getPassword(),"ROLE_OWNER", owner.getStatus().name(), owner.getName(), null
        );
    }

    public static CustomUserDetails from(Staff staff) {
        return new CustomUserDetails(
                staff.getStaffId(), staff.getEmail(), staff.getPassword(),"ROLE_STAFF", "ACTIVE", staff.getName(), null
        );
    }

    public static CustomUserDetails from(Manager manager) {
        return new CustomUserDetails(
                manager.getManagerId(), manager.getLoginId(), manager.getPassword(),"ROLE_ADMIN", "ACTIVE", null, null
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status.equals("ACTIVE");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return String.valueOf(id); //pk를 반환
    }
}
