package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.reservation.dto.AdminReservationItemResponse;
import com.example.LunchGo.reservation.service.AdminReservationQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {

    private final AdminReservationQueryService adminReservationQueryService;

    @GetMapping
    public ResponseEntity<List<AdminReservationItemResponse>> getList() {
        return ResponseEntity.ok(adminReservationQueryService.getList());
    }
}
