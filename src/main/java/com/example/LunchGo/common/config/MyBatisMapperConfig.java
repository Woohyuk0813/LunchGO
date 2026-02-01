package com.example.LunchGo.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = {
        "com.example.LunchGo.review.mapper",
        "com.example.LunchGo.member.mapper",
        "com.example.LunchGo.reservation.mapper",
        "com.example.LunchGo.restaurant.mapper"
})
public class MyBatisMapperConfig {
}
