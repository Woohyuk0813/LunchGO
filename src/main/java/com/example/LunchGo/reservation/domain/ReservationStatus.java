package com.example.LunchGo.reservation.domain;
// 박기웅 추가 : 결제 모듈과 연동을 위해 예약상태를 다음과 같이 정의함
// CONFIRMED(예약금 결제 성공), PREPAID_CONFIRM(선결제 결제 성공), EXPIRED (결제 실패/만료)
public enum ReservationStatus {
    TEMPORARY,
    CONFIRMED,
    PREPAID_CONFIRMED,
    EXPIRED,
    COMPLETED,
    REFUND_PENDING,
    REFUNDED,
    NO_SHOW,
    CANCELLED
}