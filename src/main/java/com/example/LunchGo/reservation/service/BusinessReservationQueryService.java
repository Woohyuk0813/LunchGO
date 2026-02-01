package com.example.LunchGo.reservation.service;

import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.repository.UserRepository;
import com.example.LunchGo.reservation.dto.BusinessReservationDetailResponse;
import com.example.LunchGo.reservation.dto.BusinessReservationItemResponse;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.BusinessReservationListRow;
import com.example.LunchGo.reservation.mapper.row.ReservationMenuItemRow;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.LunchGo.reservation.mapper.row.ReservationMenuItemRow;
import java.util.stream.Collectors;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessReservationQueryService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final UserRepository userRepository;
    private final ReservationMapper reservationMapper;

    public List<BusinessReservationItemResponse> getList(Long restaurantId) {
        List<BusinessReservationListRow> rows = reservationMapper.selectBusinessReservationList(restaurantId);
        return rows.stream()
                .map(row -> BusinessReservationItemResponse.builder()
                        .id(row.getId())
                        .name(row.getName())
                        .phone(row.getPhone())
                        .datetime(row.getDatetime())
                        .guests(row.getGuests())
                        .amount(row.getAmount())
                        .status(row.getStatus())
                        .build())
                .toList();
    }

    public List<BusinessReservationItemResponse> getListByDate(Long restaurantId, LocalDate slotDate) {
        List<BusinessReservationListRow> rows =
                reservationMapper.selectBusinessReservationListByDate(restaurantId, slotDate);

        return rows.stream()
                .map(row -> BusinessReservationItemResponse.builder()
                        .id(row.getId())
                        .name(row.getName())
                        .phone(row.getPhone())
                        .datetime(row.getDatetime())
                        .guests(row.getGuests())
                        .amount(row.getAmount())
                        .status(row.getStatus())
                        .build())
                .toList();
    }

    public BusinessReservationDetailResponse getDetail(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(String.valueOf(reservationId)));

        ReservationSlot slot = reservationSlotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("slot not found: " + reservation.getSlotId()));

        User user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + reservation.getUserId()));
        String reservationType = String.valueOf(reservation.getReservationType());
        String status = mapStatus(reservation.getStatus());

        boolean isPreorder = "PREORDER_PREPAY".equals(String.valueOf(reservation.getReservationType()))
                || "PREPAID_CONFIRMED".equals(String.valueOf(reservation.getStatus()));

        String paymentType = isPreorder ? "prepaid" : "onsite";

        List<BusinessReservationDetailResponse.PreorderItem> preorderItems = Collections.emptyList();
        if (isPreorder) {
            List<ReservationMenuItemRow> rows = reservationMapper.selectReservationMenuItems(reservationId);

            if (rows != null && !rows.isEmpty()) {
                preorderItems = rows.stream()
                        .map(r -> BusinessReservationDetailResponse.PreorderItem.builder()
                                .name(r.getMenuName())
                                .qty(r.getQuantity())
                                .price(r.getUnitPrice())
                                .build())
                        .toList();
            }
        }
        Integer amount = reservation.getTotalAmount();
        if (amount == null) {
            amount = reservation.getPrepayAmount() != null
                    ? reservation.getPrepayAmount()
                    : reservation.getDepositAmount();
        }
        if (amount == null) amount = 0;

        return BusinessReservationDetailResponse.builder()
                .id(reservation.getReservationId())
                .name(user.getName())
                .phone(user.getPhone())
                .date(slot.getSlotDate().toString())
                .time(slot.getSlotTime().toString())
                .guests(reservation.getPartySize())
                .amount(amount)
                .status(status)
                .requestNote(reservation.getRequestMessage())
                .paymentType(paymentType)
                .preorderItems(preorderItems)
                .build();
    }

    private String mapStatus(Object s) {
        if (s == null) return "pending";
        String status = String.valueOf(s);
        return switch (status) {
            case "TEMPORARY" -> "pending";
            case "CONFIRMED", "PREPAID_CONFIRMED" -> "confirmed";
            case "CANCELLED" -> "cancelled";
            case "EXPIRED", "NO_SHOW" -> "refunded";
            default -> "pending";
        };
    }
}