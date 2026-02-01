package com.example.LunchGo.reservation.service;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.domain.VisitStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserConfirmedVisitService {
    private final RedisUtil redisUtil;

    /**
     * 사용자가 리마인더에서 방문 예정으로 선택한 경우 (해당 예약 id)
     * */
    public void setVisitConfirmed(Long reservationId){
        if(!redisUtil.updateData(String.valueOf(reservationId), VisitStatus.CONFIRMED.name())){
            throw new IllegalStateException("reservation not found"); //예약이 만료된 건을 바꾸는 경우
        }
        //방문 확정으로 변경 성공
    }

    /**
     * 사용자가 리마인더에서 방문 취소를 선택한 경우 (해당 예약 id)
     * */
    public void setVisitCancelled(Long reservationId){
        if(!redisUtil.updateData(String.valueOf(reservationId), VisitStatus.CANCELLED.name())){
            throw new IllegalStateException("reservation not found"); //예약이 만료되었는데 취소 못함
        }
        //redis 취소 여부 반영은 성공

        //이후의 코드...
    }

    //ttl이 지나면 redis에서 자동으로 key와 value가 사라지므로 기간 만료에 대해서는 생각하지 않아도 됨
}
