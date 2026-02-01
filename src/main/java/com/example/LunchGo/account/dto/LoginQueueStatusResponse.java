package com.example.LunchGo.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginQueueStatusResponse {
    private String queueToken;
    private boolean allowed;
    private boolean expired;
    private long position;
    private long waitingCount;
    private long capacity;
    private String message;
}
