package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.account.dto.CustomUserDetails;
import com.example.LunchGo.member.entity.Staff;
import com.example.LunchGo.member.repository.StaffRepository;
import com.example.LunchGo.reservation.dto.BusinessVisitNotificationResponse;
import com.example.LunchGo.reservation.service.BusinessVisitNotificationService;
import com.example.LunchGo.restaurant.service.BusinessRestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business/notifications")
public class BusinessNotificationController {

    private final BusinessVisitNotificationService notificationService;
    private final BusinessRestaurantService businessRestaurantService;
    private final StaffRepository staffRepository;

    @PreAuthorize("hasAnyAuthority('ROLE_OWNER','ROLE_STAFF')")
    @GetMapping
    public ResponseEntity<List<BusinessVisitNotificationResponse>> getList(
            @RequestParam(required = false) Long restaurantId
    ) {
        Long resolvedRestaurantId = (restaurantId != null)
                ? restaurantId
                : resolveRestaurantIdFromToken();

        return ResponseEntity.ok(notificationService.getList(resolvedRestaurantId));
    }

    private Long resolveRestaurantIdFromToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth != null ? auth.getPrincipal() : null;

        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }

        Long ownerId;
        if ("ROLE_OWNER".equals(userDetails.getRole())) {
            ownerId = userDetails.getId();
        } else if ("ROLE_STAFF".equals(userDetails.getRole())) {
            Staff staff = staffRepository.findByStaffId(userDetails.getId())
                    .orElseThrow(() -> new IllegalArgumentException("임직원 정보를 찾을 수 없습니다."));
            ownerId = staff.getOwnerId();
        } else {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        return businessRestaurantService.findRestaurantIdByOwnerId(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("식당 정보를 찾을 수 없습니다."));
    }
}
