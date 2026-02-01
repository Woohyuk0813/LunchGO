package com.example.LunchGo.account.repository;

import com.example.LunchGo.account.domain.TokenInfo;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<TokenInfo, String> {
    // 기본적인 save, findById, delete, existsById 존재
}
