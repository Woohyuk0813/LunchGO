package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.restaurant.dto.RegularHolidayDTO;
import com.example.LunchGo.restaurant.entity.RegularHoliday;
import com.example.LunchGo.restaurant.repository.RegularHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RegularHolidayService {

    private final RegularHolidayRepository regularHolidayRepository;
    private final ModelMapper modelMapper;

    /**
     * 특정 식당의 정기 휴무일 리스트를 조회합니다.
     * @param restaurantId 조회할 식당 ID
     * @return 정기 휴무일 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<RegularHolidayDTO> getRegularHolidaysByRestaurant(Long restaurantId) {
        return regularHolidayRepository.findAllByRestaurantId(restaurantId)
                .stream()
                .map(holiday -> modelMapper.map(holiday, RegularHolidayDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * 특정 식당의 정기 휴무일 리스트를 생성합니다.
     * @param restaurantId 정기 휴무일을 추가할 식당 ID
     * @param regularHolidayNumbers 생성할 정기 휴무일(요일 숫자) 리스트
     */
    public void createRegularHolidaysForRestaurant(Long restaurantId, List<Integer> regularHolidayNumbers) {
        if (regularHolidayNumbers == null || regularHolidayNumbers.isEmpty()) {
            return;
        }
        regularHolidayNumbers.forEach(dayOfWeek -> {
            RegularHoliday holiday = RegularHoliday.builder()
                    .restaurantId(restaurantId)
                    .dayOfWeek(dayOfWeek)
                    .build();
            regularHolidayRepository.save(holiday);
        });
    }

    /**
     * 특정 식당의 정기 휴무일을 업데이트합니다. (기존 정보 전체 삭제 후 새로 추가)
     * @param restaurantId 업데이트할 식당 ID
     * @param regularHolidayNumbers 새로운 정기 휴무일(요일 숫자) 리스트
     */
    public void updateRegularHolidaysForRestaurant(Long restaurantId, List<Integer> regularHolidayNumbers) {
        regularHolidayRepository.deleteAllByRestaurantId(restaurantId);
        createRegularHolidaysForRestaurant(restaurantId, regularHolidayNumbers);
    }
}
