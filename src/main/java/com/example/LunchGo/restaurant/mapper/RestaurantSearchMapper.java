package com.example.LunchGo.restaurant.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.example.LunchGo.restaurant.dto.RestaurantSearchParameter;

import java.util.List;

@Mapper
public interface RestaurantSearchMapper {

    List<Long> findAvailableRestaurantIds(RestaurantSearchParameter params);
}
