package com.example.LunchGo.reservation.controller;

import com.example.LunchGo.account.dto.CustomUserDetails;
import com.example.LunchGo.reservation.dto.CancelReservationRequest;
import com.example.LunchGo.reservation.dto.CancelReservationResponse;
import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import com.example.LunchGo.reservation.dto.ReservationHistoryItem;
import com.example.LunchGo.reservation.dto.UserReservationDetailResponse;
import com.example.LunchGo.reservation.dto.UserReservationResponse;
import com.example.LunchGo.reservation.service.*; // Removed specific ReservationService import as Facade will be used

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    // Facade 패턴 적용: ReservationService 대신 ReservationFacade를 주입받아 예약 생성 로직을 처리합니다.
    private final ReservationFacade reservationFacade; // 기존 reservationService 대신 Facade 주입
    private final ReservationService reservationService; // slotTimes 메서드 때문에 필요
    private final UserReservationQueryService userReservationQueryService;
    private final ReservationHistoryService reservationHistoryService;
    private final ReservationCompletionService reservationCompletionService;
    private final ReservationCancelService reservationCancelService;
    private final ReservationPaymentService reservationPaymentService;
    private final ReservationRefundService reservationRefundService;


    // 예약 생성
    @PostMapping
    public ResponseEntity<ReservationCreateResponse> create(@RequestBody ReservationCreateRequest request) {
        try {
            // Facade를 통해 예약 생성 로직 호출
            ReservationCreateResponse response = reservationFacade.createReservation(request); // reservationService.create() 대신 Facade 메서드 호출
            if (response == null) {
                // Facade에서 null을 반환할 일은 없겠지만, 방어적인 코드 유지
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // 요청 데이터 유효성 검증 실패 시
            return ResponseEntity.badRequest().body(null); // HTTP 400 Bad Request와 함께 null 반환
        }
    }

    // 슬롯 시간 조회 - 이 메서드는 ReservationService의 slotTimes를 직접 사용하므로 그대로 유지
    @GetMapping("/slots")
    public ResponseEntity<List<LocalTime>> slotTimes(
            @RequestParam Long restaurantId,
            @RequestParam LocalDate slotDate
    ) {
        return ResponseEntity.ok(reservationService.slotTimes(restaurantId, slotDate));
    }

    /**
     * 사용자 예약 내역 (본인 것만)
     * GET /api/reservations/my
     */
    @GetMapping("/my")
    public ResponseEntity<List<UserReservationResponse>> myReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userReservationQueryService.getMyReservations(userDetails.getId()));
    }

    /**
     * 사용자 예약 상세 (본인 것만)
     * GET /api/reservations/{reservationId}?userId=3
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<UserReservationDetailResponse> myReservationDetail(
            @PathVariable Long reservationId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(userReservationQueryService.getMyReservationDetail(userId, reservationId));
    }

    /**
     * 사용자 예약 취소
     * POST /api/reservations/{reservationId}/cancel
     */
    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<CancelReservationResponse> cancel(
            @PathVariable Long reservationId,
            @RequestBody CancelReservationRequest request
    ) {
        String reason = (request != null) ? request.getReason() : null;
        reservationRefundService.cancelWithRefundPolicy(reservationId, reason);
        return ResponseEntity.ok(new CancelReservationResponse(true));
    }

    /**
     * 지난 예약 / 예정 예약 히스토리
     * GET /api/reservations/history?userId=1&type=past
     */
    @GetMapping("/history")
    public ResponseEntity<List<ReservationHistoryItem>> history(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "past") String type
    ) {
        List<ReservationHistoryItem> items = reservationHistoryService.getHistory(userId, type);
        return ResponseEntity.ok(items);
    }

    /**
     * 이용완료 처리
     * PATCH /api/reservations/{reservationId}/complete
     */
    @PatchMapping("/{reservationId}/complete")
    public ResponseEntity<Void> complete(@PathVariable Long reservationId) {
        reservationCompletionService.complete(reservationId);
        return ResponseEntity.noContent().build();
    }
}
