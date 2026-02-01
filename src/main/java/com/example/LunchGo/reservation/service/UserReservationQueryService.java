package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.dto.UserReservationDetailResponse;
import com.example.LunchGo.reservation.dto.UserReservationResponse;
import com.example.LunchGo.reservation.mapper.row.UserReservationDetailRow;
import com.example.LunchGo.reservation.mapper.row.UserReservationListRow;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserReservationQueryService {

    private static final String USER_LIST_STATEMENT =
            "com.example.LunchGo.reservation.mapper.ReservationMapper.selectUserReservationList";
    private static final String USER_DETAIL_STATEMENT =
            "com.example.LunchGo.reservation.mapper.ReservationMapper.selectUserReservationDetail";

    private final SqlSession sqlSession;

    public List<UserReservationResponse> getMyReservations(Long userId) {

        List<UserReservationListRow> rows = sqlSession.selectList(USER_LIST_STATEMENT, userId);

        return rows.stream()
                .map(row -> UserReservationResponse.builder()
                        .reservationId(row.getReservationId())
                        .restaurantId(row.getRestaurantId())
                        .confirmationNumber(row.getConfirmationNumber())
                        .restaurantName(row.getRestaurantName())
                        .date(row.getDate())
                        .time(row.getTime())
                        .partySize(row.getPartySize())
                        .status(row.getStatus())
                        .requestNote(row.getRequestNote())
                        .build())
                .toList();
    }

    public UserReservationDetailResponse getMyReservationDetail(Long userId, Long reservationId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("reservationId", reservationId);

        UserReservationDetailRow row = sqlSession.selectOne(USER_DETAIL_STATEMENT, params);
        if (row == null) {
            throw new ReservationNotFoundException("reservation not found: " + reservationId);
        }

        return UserReservationDetailResponse.builder()
                .reservationId(row.getReservationId())
                .confirmationNumber(row.getConfirmationNumber())
                .restaurantId(row.getRestaurantId())
                .restaurantName(row.getRestaurantName())
                .restaurantAddress(row.getRestaurantAddress())
                .restaurantPhone(row.getRestaurantPhone())
                .date(row.getDate())
                .time(row.getTime())
                .partySize(row.getPartySize())
                .requestNote(row.getRequestNote())
                .amount(row.getAmount())
                .paymentType(row.getPaymentType())
                .paymentMethod(row.getPaymentMethod())
                .paidAt(row.getPaidAt())
                .status(row.getStatus())
                .build();
    }
}
