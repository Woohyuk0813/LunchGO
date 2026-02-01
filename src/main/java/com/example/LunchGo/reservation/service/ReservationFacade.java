package com.example.LunchGo.reservation.service;

import com.example.LunchGo.common.util.RedisUtil;
import com.example.LunchGo.reservation.domain.ReservationType;
import com.example.LunchGo.reservation.domain.VisitStatus;
import com.example.LunchGo.reservation.dto.MenuSnapshot;
import com.example.LunchGo.reservation.dto.ReservationCreateRequest;
import com.example.LunchGo.reservation.dto.ReservationCreateResponse;
import com.example.LunchGo.restaurant.entity.Menu;
import com.example.LunchGo.restaurant.repository.MenuRepository;
import com.example.LunchGo.reservation.exception.SlotCapacityExceededException;
import com.example.LunchGo.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationFacade {

    // 예약금 관련 상수 (ServiceImpl에서 이동)
    private static final int DEPOSIT_PER_PERSON_DEFAULT = 5000;
    private static final int DEPOSIT_PER_PERSON_LARGE = 10000;
    private static final int DEPOSIT_LARGE_THRESHOLD = 7;
    private static final int DEFAULT_MAX_CAPACITY = 20; // ReservationFacade 내부에서 사용할 기본 최대 정원
    private static final long REDIS_SEAT_KEY_TTL_MILLIS = Duration.ofDays(15).toMillis(); // Redis 좌석 키의 TTL - 예약은 현재 날짜 기준 약 2주 뒤 날짜까지 가능

    private final ReservationService reservationService; // 수정된 ReservationService (락 핵심 로직 담당)
    private final MenuRepository menuRepository; // 메뉴 정보 조회 (선주문 처리용)
    private final RestaurantRepository restaurantRepository; // 식당의 최대 정원 조회용
    private final RedisUtil redisUtil; // 후처리: Redis 저장용

    /**
     * Facade 패턴의 새로운 예약 생성 진입점. 예약 생성의 전체적인 흐름을 관리하며, 락이 필요 없는 로직은 여기서 처리하고 락이 필요한 핵심 로직은 ReservationServiceImpl에
     * 위임합니다.
     *
     * @param request 예약 생성 요청 DTO
     * @return 예약 생성 응답 DTO
     */
    public ReservationCreateResponse createReservation(ReservationCreateRequest request) {
        // 1. 전처리 단계: 락이 필요 없는 로직 (ReservationServiceImpl에서 이동)
        //    핵심 락이 걸리기 전에 최대한 많은 작업을 처리하여 락 점유 시간을 단축합니다.

        // 요청 유효성 검증 (ServiceImpl에서 이동)
        validate(request);

        // 1. Redis 키 정의
        String redisSeatKey = RedisUtil.generateSeatKey(request.getRestaurantId(), request.getSlotDate(),
                request.getSlotTime());

        if (!redisUtil.existData(redisSeatKey)) {
            // Just-in-Time Redis Counter Initialization & Pre-Check
            // 2. 이 슬롯의 최대 정원을 DB에서 조회 (락 없이)
            Integer maxCapacity = restaurantRepository.findReservationLimitByRestaurantId(request.getRestaurantId())
                    .orElse(DEFAULT_MAX_CAPACITY); // 기본값 DEFAULT_MAX_CAPACITY (ReservationFacade 내부에 정의)

            // 3. SETNX를 사용한 원자적 초기화 (키가 없을 때만 실행)
            // TTL은 24시간으로 넉넉하게 설정
            redisUtil.setIfAbsent(redisSeatKey, String.valueOf(maxCapacity), REDIS_SEAT_KEY_TTL_MILLIS);
            log.info("예약 슬롯 데이터 캐싱: {}", redisSeatKey);
        }

        // 4. Redis 좌석 사전 검증 (빠른 Fail-Fast)
        Long remainingSeats = redisUtil.decrement(redisSeatKey, request.getPartySize());

        if (remainingSeats < 0) {
            // 좌석이 부족하면 Redis 카운터를 다시 원상 복구하고 예외 발생
            redisUtil.increment(redisSeatKey, request.getPartySize());
            throw new SlotCapacityExceededException("잔여석이 부족합니다. (Redis 사전 검증)");
        }

        try {
            // 선주문/선결제 메뉴 처리 및 금액 계산 (ServiceImpl에서 이동)
            List<MenuSnapshot> menuSnapshots = new ArrayList<>();
            Integer precalculatedPrepaySum = null;
            if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType())) {
                // N+1 쿼리 방지를 위해 메뉴 ID 목록을 먼저 추출
                List<Long> menuIds = request.getMenuItems().stream()
                        .map(ReservationCreateRequest.MenuItem::getMenuId)
                        .collect(Collectors.toList());

                // IN 절을 사용해 한 번의 쿼리로 모든 메뉴 정보를 조회
                List<Menu> foundMenus = menuRepository.findAllByMenuIdInAndRestaurantIdAndIsDeletedFalse(menuIds,
                        request.getRestaurantId());

                // 조회된 메뉴를 Map으로 변환하여 빠른 조회를 지원
                Map<Long, Menu> menuMap = foundMenus.stream()
                        .collect(Collectors.toMap(Menu::getMenuId, menu -> menu));

                // 요청된 모든 메뉴가 실제로 조회되었는지 검증
                if (menuMap.size() != menuIds.size()) {
                    throw new IllegalArgumentException(
                            "One or more menus not found or do not belong to the restaurant.");
                }

                int sum = 0;
                for (ReservationCreateRequest.MenuItem mi : request.getMenuItems()) {
                    if (mi == null || mi.getMenuId() == null) {
                        throw new IllegalArgumentException("menuId is required");
                    }
                    if (mi.getQuantity() == null || mi.getQuantity() <= 0) {
                        throw new IllegalArgumentException("quantity must be positive");
                    }

                    // Map에서 메뉴 정보를 가져옴
                    Menu menu = menuMap.get(mi.getMenuId());

                    int unitPrice = menu.getPrice() == null ? 0 : menu.getPrice();
                    int qty = mi.getQuantity();
                    int lineAmount = unitPrice * qty;
                    sum += lineAmount;

                    menuSnapshots.add(new MenuSnapshot(menu.getMenuId(), menu.getName(), unitPrice, qty, lineAmount));
                }
                precalculatedPrepaySum = sum;
            }

            // 예약금 계산 (ServiceImpl에서 이동)
            Integer precalculatedDepositAmount = null;
            if (ReservationType.RESERVATION_DEPOSIT.equals(request.getReservationType())) {
                int perPerson = request.getPartySize() >= DEPOSIT_LARGE_THRESHOLD
                        ? DEPOSIT_PER_PERSON_LARGE
                        : DEPOSIT_PER_PERSON_DEFAULT;
                precalculatedDepositAmount = perPerson * request.getPartySize();
            }

            // 2. 핵심 로직 호출 단계: 락이 필요한 로직은 ServiceImpl에 위임
            //    락이 걸린 ServiceImpl의 메서드를 호출하여, 동시성 제어가 필요한 DB 작업을 수행합니다.
            ReservationCreateResponse response = reservationService.createReservationLocked(
                    request,
                    precalculatedPrepaySum,
                    menuSnapshots,
                    precalculatedDepositAmount
            );

            // 3. 후처리 단계: 트랜잭션 커밋 후 실행될 로직은 ReservationServiceImpl로 이동됨.
            return response;
        } catch (Exception e) {
            // 핵심 로직 실패 시, 미리 감소시킨 Redis 좌석 수를 원상 복구 (보상 트랜잭션)
            redisUtil.increment(redisSeatKey, request.getPartySize());
            // 원래 예외를 다시 던져서 클라이언트에게 실패를 알림
            throw e;
        }
    }

    /**
     * 예약 요청 유효성 검증 메서드. (ServiceImpl에서 이동) 락이 걸리기 전에 호출되어 기본적인 요청 데이터의 유효성을 확인합니다.
     *
     * @param request 예약 생성 요청 DTO
     */
    private static void validate(ReservationCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getRestaurantId() == null) {
            throw new IllegalArgumentException("restaurantId is required");
        }
        if (request.getSlotDate() == null) {
            throw new IllegalArgumentException("slotDate is required");
        }
        if (request.getSlotTime() == null) {
            throw new IllegalArgumentException("slotTime is required");
        }

        if (request.getPartySize() == null || request.getPartySize() <= 0) {
            throw new IllegalArgumentException("partySize must be positive");
        }

        if (request.getReservationType() == null) {
            throw new IllegalArgumentException("reservationType is required");
        }

        if (ReservationType.PREORDER_PREPAY.equals(request.getReservationType())) {
            if (request.getMenuItems() == null || request.getMenuItems().isEmpty()) {
                throw new IllegalArgumentException("menuItems is required for preorder");
            }
            if (request.getTotalAmount() == null || request.getTotalAmount() <= 0) {
                // 프론트가 보내는 값이지만, 일단 기존 로직 유지 (검증만)
                throw new IllegalArgumentException("totalAmount is required for preorder");
            }
        }

        if (request.getRequestMessage() != null && request.getRequestMessage().length() > 50) {
            throw new IllegalArgumentException("requestMessage must be <= 50 chars");
        }
    }
}