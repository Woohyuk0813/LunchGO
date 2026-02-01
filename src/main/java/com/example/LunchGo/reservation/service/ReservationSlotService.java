package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.domain.ReservationSlot;
import com.example.LunchGo.reservation.exception.SlotCapacityExceededException;
import com.example.LunchGo.reservation.mapper.ReservationMapper;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private static final int DEFAULT_MAX_CAPACITY = 20;

    private final RestaurantRepository restaurantRepository;
    private final ReservationMapper reservationMapper;

    /**
     * 슬롯을 비관적 락으로 조회하고, 없으면 생성하며, 잔여석이 충분한지 검증까지 마친 후 반환
     */
    @Transactional
    public ReservationSlot getValidatedSlot(Long restaurantId, LocalDate date, LocalTime time, int requestedPartySize) {
        // 1. 비관적 락을 건 상태에서 슬롯 조회
        ReservationSlot slot = reservationMapper.selectSlotForUpdate(restaurantId, date, time);

        // 2. 슬롯이 없으면 생성
        if (slot == null) {
            log.info("지정한 시간대의 예약 슬롯이 없습니다. 슬롯을 생성합니다.");
            Optional<Integer> reservationLimit = restaurantRepository.findReservationLimitByRestaurantId(restaurantId);
            reservationMapper.upsertSlot(restaurantId, date, time, reservationLimit.orElse(DEFAULT_MAX_CAPACITY));

            // 생성 후 다시 락을 걸고 조회
            slot = reservationMapper.selectSlotForUpdate(restaurantId, date, time);

            if (slot == null) {
                throw new IllegalStateException("System Error: Slot creation failed.");
            }
        }
        log.info("{} {} 예약 슬롯을 불러옵니다.", date, time);

        // 3. 잔여석 계산
        // 잔여석 정보는 별도의 DB 속성값으로 저장하지 않고 계산 -> 예약 취소 시 별도의 잔여석 처리 없이 예약상태만 변경하면 끝
        // (추후 테이블에 잔여석 정보를 저장할 속성을 추가한다면, 예약 취소 시에도 잔여석 처리 로직을 추가해야 함)
        // 예약석을 점유하는 예약상태, 즉 TEMPORARY(임시예약), CONFIRMED(예약비 결제완료), PREPAID_CONFIRMED(선결제/선주문 결제완료)인 경우에만 예약한 인원수를 합산
        int currentTotal = reservationMapper.sumPartySizeBySlotId(slot.getSlotId());

        // 4. 잔여석 검증: 이미 해당 시간대에 예약한 사람이 있거나, 사용자가 지정한 인원수가 잔여석 개수를 초과할 경우 예약 불가
        if (currentTotal + requestedPartySize > slot.getMaxCapacity()) {
            log.info("잔여석이 부족합니다.");
            throw new SlotCapacityExceededException("잔여석이 부족합니다. (남은 좌석: " + (slot.getMaxCapacity() - currentTotal) + ")");
        }
        log.info("선택한 인원수: {}명, 현재 잔여석: {}석", requestedPartySize, slot.getMaxCapacity() - (currentTotal+requestedPartySize));

        return slot;
    }
}
