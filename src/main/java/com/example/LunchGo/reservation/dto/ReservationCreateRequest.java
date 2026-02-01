package com.example.LunchGo.reservation.dto;

import com.example.LunchGo.reservation.domain.ReservationType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReservationCreateRequest {

    private Long userId;

    private Long restaurantId;
    private LocalDate slotDate;
    private LocalTime slotTime;

    private Integer partySize;

    private ReservationType reservationType;

    // 선택 입력 (<= 50)
    private String requestMessage;

    // 선주문/선결제 총액(프론트가 보내는 값, 서버는 메뉴 기준으로 재계산 가능)
    private Integer totalAmount;

    /**
     * 선주문/선결제 시 선택된 메뉴 목록
     */
    private List<MenuItem> menuItems;

    @Getter
    @NoArgsConstructor
    public static class MenuItem {
        private Long menuId;
        private Integer quantity;
    }
}