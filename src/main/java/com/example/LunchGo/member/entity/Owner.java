package com.example.LunchGo.member.entity;

import com.example.LunchGo.member.domain.CustomRole;
import com.example.LunchGo.member.domain.OwnerStatus;
import com.example.LunchGo.member.domain.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "owners")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "login_id", nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "business_num", nullable = false, length = 30)
    private String businessNum;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "image", length = 255)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OwnerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private CustomRole role;

    @Column(name = "start_at", nullable = false)
    private LocalDate startAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "withdrawal_at")
    private LocalDateTime withdrawalAt;

    @Builder
    public Owner(String loginId, String password, String businessNum, String name,
                 String phone, LocalDate startAt,
                 OwnerStatus status, CustomRole role) {
        this.loginId = loginId;
        this.password = password;
        this.businessNum = businessNum;
        this.name = name;
        this.phone = phone;
        this.startAt = startAt;
        this.status = status;
        this.role = role;
    }
}