package com.example.LunchGo.reservation.mapper.row;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationDailyTypeStatsRow {
    private LocalDate date;
    private Integer depositCount;
    private Integer preorderCount;
    private Integer cancellationCount;
    private Integer totalCount;
    private Integer refundCount;

}
