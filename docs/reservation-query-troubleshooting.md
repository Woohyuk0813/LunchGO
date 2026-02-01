# 예약 생성 기능 관련 개선

[관련 내용 - refactor(reservation): 예약 생성 로직 개선 #537](https://github.com/SSG9-FINAL-LunchGO/LunchGO/pull/537)

## 개선사항 1 - 식당 잔여석 처리 기능

### 기존 잔여석 처리 방식

- 특정 예약 슬롯에 관한 예약 요청이 발생할 때마다 DB에서 남은 예약석 수를 계산
  - 예약 슬롯에 관한 restaurant_reservation_slots 테이블과, 예약에 관한 reservations 테이블에는 잔여석 정보를 별도로 저장하지 않았기 때문에 예약 요청이 들어올 때마다 잔여석 계산을 수행
    - 즉, 예약 가능 여부를 판정하기 위해 매번 예약 요청이 들어올 때마다 아래의 DB의 쿼리를 실행
  - 이 쿼리는 예약 요청이 DB 비관적 락을 획득한 이후 실행
    - 잔여석 계산으로 인해 예약 등록 작업에 소요되는 시간이 증가 -> 락 획득을 위한 대기 시간 증가
  ```xml
  <!--  특정 슬롯에 관한 예약자 수의 총합을 계산한 후 반환  -->
    <!--  예약상태가 임시 또는 예약/선결제 확정인 예약 내역에서의 인원수만 집계  -->
    <select id="sumPartySizeBySlotId" resultType="int">
        SELECT COALESCE(SUM(party_size), 0)
        FROM reservations
        WHERE slot_id = #{slotId}
          AND status IN ('TEMPORARY', 'CONFIRMED', 'PREPAID_CONFIRMED', 'REFUND_PENDING')
    </select>
  ```
### 개선 방식 - 예약 슬롯별 잔여석 정보 캐싱

- 애플리케이션 계층에서의 개선 내용
- 이미 예약된 적 있는 예약 슬롯일 경우, DB에서 잔여석 계산 쿼리를 수행할 필요 없이 Redis에 캐싱된 잔여석을 갱신하도록 개선
  - Redis에 캐싱한 잔여석 정보를 활용하여, DB의 잔여석 계산 쿼리를 실행하기 전에 1차적으로 필터링
    - ReservationFacade의 createReservation 메서드에서 예약 요청 시 입력값의 유효성을 검증한 직후 아래의 코드를 실행하여 남은 예약석 수를 사전 검증
    - 이 검증 과정에서 필터링되지 않은 예약 요청에 관해서만 상단의 DB 쿼리를 활용한 잔여석 검증 진행
      ```java
          // Just-in-Time Redis Counter Initialization & Pre-Check
          // 1. 이 슬롯의 최대 정원을 DB에서 조회 (락 없이)
          Integer maxCapacity = restaurantRepository.findReservationLimitByRestaurantId(request.getRestaurantId())
                  .orElse(DEFAULT_MAX_CAPACITY); // 기본값 DEFAULT_MAX_CAPACITY (ReservationFacade 내부에 정의)
    
          // 2. Redis 키 정의
          String redisSeatKey = RedisUtil.generateSeatKey(request.getRestaurantId(), request.getSlotDate(),
                  request.getSlotTime());
    
          // 3. SETNX를 사용한 원자적 초기화 (키가 없을 때만 실행)
          // TTL은 24시간으로 넉넉하게 설정
          redisUtil.setIfAbsent(redisSeatKey, String.valueOf(maxCapacity), REDIS_SEAT_KEY_TTL_MILLIS);
    
          // 4. Redis 좌석 사전 검증 (빠른 Fail-Fast)
          Long remainingSeats = redisUtil.decrement(redisSeatKey, request.getPartySize());
    
          if (remainingSeats < 0) {
              // 좌석이 부족하면 Redis 카운터를 다시 원상 복구하고 예외 발생
              redisUtil.increment(redisSeatKey, request.getPartySize());
              throw new SlotCapacityExceededException("잔여석이 부족합니다. (Redis 사전 검증)");
          }
      ```
      
    - 잔여석 검증 시, 취소/환불로 인한 잔여석 변동 상황을 반영하도록 개선
      - 예약 취소/환불 트랜잭션이 커밋될 때 아래의 코드를 실행하여 Redis에 캐싱된 잔여석 정보를 업데이트
      ```java
      // --- Redis 좌석 카운터 업데이트 (트랜잭션 커밋 후) ---
      // DB 트랜잭션이 성공적으로 커밋된 후에만 Redis 좌석 카운터를 증가시켜 일관성을 유지합니다.
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
          @Override
          public void afterCommit() {
              // 예약 슬롯 정보 (restaurantId, slotDate, slotTime) 및 partySize를 가져와 Redis 키 생성
              String redisSeatKey = RedisUtil.generateSeatKey(slot.getRestaurantId(), slot.getSlotDate(), slot.getSlotTime());
              int partySize = reservation.getPartySize() != null ? reservation.getPartySize() : 0;

              // Redis 좌석 카운터에 partySize만큼 좌석 반환 (증가)
              // 만약 Redis 키가 중간에 만료되었더라도, increment는 0에서부터 시작하여 값을 추가하므로 문제 없음
              redisUtil.increment(redisSeatKey, partySize);
          }
      });
      ```
      

## 개선사항 2 - 선주문/선결제 관련 쿼리 최적화

### 개선 과정

- 기존의 선주문/선결제 메뉴 조회 및 등록 기능에서는 for문 내부에서 단일 조회/등록 쿼리를 반복하는 방식이었기 때문에 N+1 문제가 발생
  - 특히, 선주문/선결제 메뉴 등록은 예약 생성 트랜잭션 내부에서 수행되는 작업이었기 때문에 개선이 필요했음. 
- 선주문/선결제 금액 계산용 메뉴 조회 시 아래의 절차대로 진행하도록 수정
  1. 선주문/선결제 주문한 식당 메뉴 ID 리스트 조회
  2. 1.에서 조회한 메뉴 ID 리스트에 관한 IN 절을 사용하여 1개의 쿼리로 주문한 모든 메뉴 정보를 조회
  3. 조회한 메뉴 정보를 Map 형태로 저장
- 예약 생성 트랜잭션 내부에서 선주문/선결제 메뉴, 수량 등의 주문 내역을 리스트 단위로 등록하도록 insert 쿼리문을 수정
  - Bulk Insert 활용: MyBatis의 <forEach> 태그를 활용하여 1개의 insert 쿼리로 한번에 여러 메뉴를 등록 가능하도록 개선

### 구현 코드

<details>

<summary>선주문/선결제 메뉴 조회 쿼리 개선 - ReservationFacade 내 해당 부분 발췌</summary>

```java
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
```

</details>

<details>

<summary>선주문/선결제 주문 메뉴 등록 쿼리 개선 - ReservationMapper의 insertReservationMenuItems </summary>

```xml
    <insert id="insertReservationMenuItems">
        INSERT INTO reservation_menu_items
            (reservation_id, menu_id, menu_name, unit_price, quantity, line_amount)
        VALUES
        <foreach collection="items" item="item" separator=",">
            (#{reservationId}, #{item.menuId}, #{item.menuName}, #{item.unitPrice}, #{item.quantity}, #{item.lineAmount})
        </foreach>
    </insert>
```

</details>