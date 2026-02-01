package com.example.LunchGo.reservation.mapper;

import com.example.LunchGo.reservation.domain.Reservation;
import com.example.LunchGo.reservation.domain.ReservationSlot;
import com.example.LunchGo.reservation.dto.MenuSnapshot;
import com.example.LunchGo.reservation.mapper.row.ReservationCreateRow;
import java.time.LocalDate;
import java.time.LocalTime;

import com.example.LunchGo.reservation.mapper.row.ReservationDailyTypeStatsRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import com.example.LunchGo.reservation.mapper.row.ReservationMenuItemRow;
import org.apache.ibatis.annotations.Param;
import com.example.LunchGo.reservation.mapper.row.ReminderSendRow;


@Mapper
public interface ReservationMapper {

    ReservationSlot selectSlot(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") LocalTime slotTime
    );

    // 비관적 락을 적용하여 특정 예약 슬롯 조회
    ReservationSlot selectSlotForUpdate(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") LocalTime slotTime);

    int insertReservation(Reservation reservation);

    int updateReservationCode(
            @Param("reservationId") Long reservationId,
            @Param("reservationCode") String reservationCode
    );

    ReservationCreateRow selectReservationCreateRow(@Param("reservationId") Long reservationId);

    java.util.List<java.time.LocalTime> selectSlotTimesByDate(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") java.time.LocalDate slotDate
    );

    int upsertSlot(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") LocalTime slotTime,
            @Param("maxCapacity") Integer maxCapacity
    );

    int sumPartySizeBySlotId(@Param("slotId") Long slotId);

    java.util.List<com.example.LunchGo.reservation.mapper.row.BusinessReservationListRow>
    selectBusinessReservationList(
            @org.apache.ibatis.annotations.Param("restaurantId") java.lang.Long restaurantId
    );

    java.util.List<com.example.LunchGo.reservation.mapper.row.AdminReservationListRow>
    selectAdminReservationList();

    java.util.List<com.example.LunchGo.reservation.mapper.row.BusinessReservationListRow>
    selectBusinessReservationListByDate(
            @Param("restaurantId") java.lang.Long restaurantId,
            @Param("slotDate") java.time.LocalDate slotDate
    );

    int insertReservationMenuItems(
            @Param("reservationId") Long reservationId,
            @Param("items") List<MenuSnapshot> items
    );
    List<ReservationMenuItemRow> selectReservationMenuItems(@Param("reservationId") Long reservationId);

    int countActiveReservation(
            @Param("userId") Long userId,
            @Param("slotId") Long slotId
    );

    // --- reminder ---
    List<com.example.LunchGo.reservation.mapper.row.ReminderSendRow> selectReminderTargets();

    int tryMarkReminderSent(
            @Param("reservationId") Long reservationId,
            @Param("reminderToken") String reminderToken
    );

    int updateVisitStatusByToken(
            @Param("token") String token,
            @Param("visitStatus") String visitStatus
    );

    List<com.example.LunchGo.reservation.mapper.row.BusinessVisitNotificationRow>
    selectBusinessVisitNotifications(@Param("restaurantId") Long restaurantId);

    int sumMonthlyCapacityByRestaurant(Long restaurantId);

    List<ReservationDailyTypeStatsRow> selectLast30DaysReservationTypeStats(@Param("restaurantId") Long restaurantId);

    ReminderSendRow selectCancelNoticeTarget(@Param("reservationId") Long reservationId);
}
