package com.example.LunchGo.reservation.service;

import com.example.LunchGo.member.entity.User;
import com.example.LunchGo.member.repository.UserRepository;
import com.example.LunchGo.reservation.domain.ReservationType;
import com.example.LunchGo.reservation.dto.ReservationSummaryResponse;
import com.example.LunchGo.reservation.entity.Reservation;
import com.example.LunchGo.reservation.entity.ReservationSlot;
import com.example.LunchGo.reservation.repository.ReservationRepository;
import com.example.LunchGo.reservation.repository.ReservationSlotRepository;
import com.example.LunchGo.restaurant.entity.Restaurant;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationSummaryQueryService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public ReservationSummaryResponse getSummary(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(String.valueOf(reservationId)));

        ReservationSlot slot = reservationSlotRepository.findById(reservation.getSlotId())
                .orElseThrow(() -> new IllegalArgumentException("slot not found: " + reservation.getSlotId()));

        Restaurant restaurant = restaurantRepository.findById(slot.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found: " + slot.getRestaurantId()));

        String requestNote = reservation.getRequestMessage();

        String paymentType = "deposit";
        if (reservation.getReservationType() != null) {
            try {
                ReservationType type = ReservationType.valueOf(reservation.getReservationType());
                if (type == ReservationType.PREORDER_PREPAY) paymentType = "prepaid";
            } catch (IllegalArgumentException ignore) {
            }
        }

        Integer amount = reservation.getTotalAmount();
        if (amount == null) {
            amount = reservation.getPrepayAmount() != null ? reservation.getPrepayAmount() : reservation.getDepositAmount();
        }
        if (amount == null) amount = 0;

        User user = null;
        try {
            user = userRepository.findById(reservation.getUserId()).orElse(null);
        } catch (Exception ignore) {
        }

        String address = restaurant.getRoadAddress();
        if (restaurant.getDetailAddress() != null && !restaurant.getDetailAddress().isBlank()) {
            address = address + " " + restaurant.getDetailAddress();
        }

        return ReservationSummaryResponse.builder()
                .restaurant(ReservationSummaryResponse.RestaurantInfo.builder()
                        .name(restaurant.getName())
                        .address(address)
                        .build())
                .booking(ReservationSummaryResponse.BookingInfo.builder()
                        .date(slot.getSlotDate().toString())
                        .time(slot.getSlotTime().toString())
                        .partySize(reservation.getPartySize())
                        .requestNote(requestNote)
                        .build())
                .payment(ReservationSummaryResponse.PaymentInfo.builder()
                        .type(paymentType)
                        .amount(amount)
                        .build())
                .requestNote(requestNote)
                .build();
    }
}