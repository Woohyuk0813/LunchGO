package com.example.LunchGo.reservation.service;

import com.example.LunchGo.reservation.dto.ReservationHistoryItem;
import java.util.List;

public interface ReservationHistoryService {
    List<ReservationHistoryItem> getHistory(Long userId, String type);
}
