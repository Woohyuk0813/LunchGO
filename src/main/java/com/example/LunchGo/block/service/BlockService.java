package com.example.LunchGo.block.service;

public interface BlockService {
    void add(Long userId, String reason);

    boolean has(Long userId);
}
