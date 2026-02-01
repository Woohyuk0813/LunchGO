package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.dto.AdminReservationItemResponse;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.reservation.mapper.row.AdminReservationListRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReservationQueryService {

    private final ReservationMapper reservationMapper;

    public List<AdminReservationItemResponse> getList() {
        List<AdminReservationListRow> rows = reservationMapper.selectAdminReservationList();
        return rows.stream()
                .map(row -> AdminReservationItemResponse.builder()
                        .id(row.getId())
                        .restaurantName(row.getRestaurantName())
                        .customerName(row.getCustomerName())
                        .reservationDateTime(row.getReservationDateTime())
                        .partySize(row.getPartySize())
                        .type(row.getType())
                        .status(row.getStatus())
                        .build())
                .toList();
    }
}
