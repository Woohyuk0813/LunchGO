package com.example.LunchGo.reservation.mapper;

import com.example.LunchGo.reservation.mapper.row.ReservationHistoryRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReservationHistoryMapper {
    List<ReservationHistoryRow> selectReservationHistory(
        @Param("userId") Long userId,
        @Param("statuses") List<String> statuses,
        @Param("orderDirection") String orderDirection
    );
}
