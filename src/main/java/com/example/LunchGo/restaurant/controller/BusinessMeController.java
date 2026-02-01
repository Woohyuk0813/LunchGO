package com.example.LunchGo.restaurant.controller;

import com.example.LunchGo.account.dto.CustomUserDetails;
import com.example.LunchGo.member.entity.Staff;
import com.example.LunchGo.member.repository.StaffRepository;
import com.example.LunchGo.restaurant.service.BusinessRestaurantService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business/me")
public class BusinessMeController {

    private final BusinessRestaurantService businessRestaurantService;
    private final StaffRepository staffRepository;

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_STAFF')")
    @GetMapping("/restaurant")
    public ResponseEntity<?> getMyRestaurantId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;

        if (!(principal instanceof CustomUserDetails userDetails)) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        Long ownerId;
        if ("ROLE_OWNER".equals(userDetails.getRole())) {
            ownerId = userDetails.getId();
        } else {
            Staff staff = staffRepository.findByStaffId(userDetails.getId())
                    .orElseThrow(() -> new IllegalArgumentException("임직원 정보를 찾을 수 없습니다."));
            ownerId = staff.getOwnerId();
        }

        Long restaurantId = businessRestaurantService.findRestaurantIdByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("식당 정보를 찾을 수 없습니다."));

        return ResponseEntity.ok(Map.of("restaurantId", restaurantId));
    }
}
