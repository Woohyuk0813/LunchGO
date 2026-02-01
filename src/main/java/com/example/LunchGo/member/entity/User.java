package com.example.LunchGo.member.entity;

import com.example.LunchGo.member.domain.CustomRole;
import com.example.LunchGo.member.domain.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_address", nullable = false, length = 255)
    private String companyAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "withdrawal_at")
    private LocalDateTime withdrawalAt;

    @Column(name = "marketing_agree", nullable = false)
    private Boolean marketingAgree;

    @Column(name = "email_authentication", nullable = false)
    private Boolean emailAuthentication;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private CustomRole role;

    @Builder
    public User(String email, String password, String name, String phone,
                String companyName, String companyAddress,
                UserStatus status, CustomRole role, Boolean marketingAgree, Boolean emailAuthentication) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.status = status;
        this.role = role;
        this.marketingAgree = marketingAgree;
        this.emailAuthentication = emailAuthentication;
    }
}
