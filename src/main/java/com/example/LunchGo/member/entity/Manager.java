package com.example.LunchGo.member.entity;

import com.example.LunchGo.member.domain.CustomRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "managers")
public class Manager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "login_id", nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private CustomRole role;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder
    public Manager(String loginId, String password, CustomRole role, LocalDateTime lastLoginAt) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.lastLoginAt = lastLoginAt;
    }
}
