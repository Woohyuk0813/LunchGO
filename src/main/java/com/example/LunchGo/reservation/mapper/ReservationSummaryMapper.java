package com.example.LunchGo.reservation.mapper;

import com.example.LunchGo.reservation.mapper.row.ReservationMenuItemRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReservationSummaryMapper {
    List<ReservationMenuItemRow> selectReservationMenuItems(@Param("reservationId") Long reservationId);

    Integer selectVisitCount(@Param("restaurantId") Long restaurantId, @Param("userId") Long userId);
}
