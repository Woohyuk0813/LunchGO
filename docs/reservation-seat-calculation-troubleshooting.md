# 동시성 제어 - 잔여석 확인

## 동시성 제어 정책 중 반영된 사항

- 비관적 락 설정
    - 예약 슬롯에 관한 `select * from … for update`문 사용
    - 여러 명의 예약자가 식당 한 곳에 몰리는 상황에서 오버부킹 문제가 발생하는 것을 방지하는 역할을 수행
- 예약 내역에 관한 멱등성 처리
    - Redis + DB Unique Index를 활용
      (관련 내용은 동일한 예약의 중복 처리 방지 로직 구현에 관한 트러블슈팅에 정리)
    - 1명의 예약자가 보낸 동일한 예약 요청에 대해 중복된 예약 내역을 생성하는 것을 방지하는 역할
- 트랜잭션 격리 수준 설정
    - 현재는 `@Transactional` 의 기본값 사용(→ `REPEATABLE READ`)
- 잔여석 계산 방식
    - `예약금 결제하기` 버튼을 누른 시점에 일시적으로 비관적 락을 걸어야 함
        - `예약금 결제하기` 버튼 클릭 시 호출되는 api는 `/api/reservations` 이고, 이 api 요청을 받은 예약 컨트롤러가 처리할 서비스 로직은 ReservationServiceImpl의 `create()` 메서드에 해당
        - **즉, 예약을 ‘임시’ 상태로 생성하는 작업을 수행하는 서비스 로직 내부에 잔여석 차감 로직을 추가해야 함**
    - `restaurant_reservation_slots`의 **슬롯에서는 잔여석 관련 정보를 저장하지 않고 예약 생성 과정에서 실시간 계산**
        - 특정 날짜의 시간대별 예약 슬롯을 미리 생성하는 것이 아닌, 예약이 발생했을 때만 예약 슬롯을 생성하는 방식이기 때문에 예약 슬롯을 활용한 잔여석 차감 로직 적용이 어려움
    - 식당이 설정한 최대인원수 - 특정 시간대 예약인원 총합 = 잔여석
        - ReservationMapper의 `sumPartySizeBySlotId`를 사용하여 예약 생성 시마다 특정시간대 예약인원의 총합을 계산
        - 현재는 식당에서 동일한 날짜 및 시간대에 서로 다른 예약을 받지 못하도록 제한했지만, 기능 확장을 고려하여 추가
    - reservations 테이블에 특정 슬롯에 관한 예약인원수를 저장하므로, reservations 테이블에 새로운 예약 내역이 추가되면 다른 예약자가 예약할 때는 해당 예약내역까지 반영하여 잔여석을 계산
- 예약 가능 여부의 판정
    - 선택한 식당, 날짜, 시간대에 해당하는 예약 슬롯의 slot_id를 찾았을 때, 해당 slot_id를 갖는 예약 내역의 인원수의 총합을 기준으로 판정
        - 구한 총합이 0 이상이면 이미 해당 예약 슬롯에 관한 예약 내역이 있다는 것이므로 예약 불가
          (동일 시간대에 1번의 예약 신청만 접수받는 경우에 해당)
        - 선택한 예약 슬롯에 관해 현재 예약석을 점유중인 인원의 총합이 식당이 설정한 최대수용인원을 초과하면 예약 불가
          (동일 시간대에 여러 개의 예약 신청을 접수받는 것에 해당, 현재 구현된 내용에 해당)
- 예약상태에 따라 중복 예약 요청 차단을 제어
  - 조건부 unique 키를 활용

---

## 문제 상황

- 여러 명의 예약자가 동시에 한 식당에 몰리는 상황에서 예약자 수가 식당이 설정한 최대 예약가능인원을 넘지 않아야 함.
- 식당의 예약 가능 여부를 판정하기 위해서는, 남은 예약석의 수를 구해야 함.
- 식당 예약 시 날짜뿐만 아니라 시간대까지 활용해서 예약을 받는 상황에서 식당의 최대 예약가능인원과 함께 연산할 값으로 무엇을 활용할 것인지에 관한 문제 발생
  - 모든 시간대에서의 예약 인원의 총합을 식당의 최대 예약 가능 인원과 비교할 것인지, 특정 시간대의 예약인원 총합만 식당의 최대 수용 인원과 비교할 것인지의 문제 

---

## 문제 해결 과정 - 잔여석 계산 방식 및 예약가능 조건 설정

- 요약: 식당이 설정한 최대 수용인원에서 특정 시간대의 예약인원의 총합만큼을 빼는 방식으로 잔여석을 계산 
- 특정 시간대에 관한 예약은 다음 시간대의 최대 예약 가능 인원 수에 영향을 주지 않음
  - 예약 단위와 예약 간격이 모두 1시간이 되도록 통일했기 때문에, 실제로 식당을 이용하는 동안 다음 시간대의 예약 요청이 발생하지 않음
  - 즉, 이전 시간대에서의 예약자 수는 다음 시간대의 예약자 수와 독립적이므로, 각 시간대별 최대 예약가능 인원은 식당이 설정한 값으로 항상 일정
- 위의 고려사항을 바탕으로 아래의 부등식을 충족하면 예약가능한 것으로 판정
  - `(최대 예약가능 인원) >= (특정 시간대 예약인원 총합) + (사용자가 지정한 예약인원)`

---

## 구현 과정

### Mapper 추가

**ReservationMapper 인터페이스**

- 관련 설명은 주석을 참조

```java
@Mapper
public interface ReservationMapper {

    ... 기존 코드

    // 비관적 락을 적용하여 특정 예약 슬롯 조회
    ReservationSlot selectSlotForUpdate(
            @Param("restaurantId") Long restaurantId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") LocalTime slotTime);

    ... 기존 코드

		// reservations 테이블에서 특정 시간대에 특정 식당을 예약한 인원의 총합 계산(기능 확장용)
    int sumPartySizeBySlotId(@Param("slotId") Long slotId);
}
```

**ReservationMapper.xml**

- ReservationMapper 인터페이스에 새로 추가한 메서드에 해당하는 부분만 발췌

```xml
		<!--  특정 슬롯을 대상으로 비관적 락 설정  -->
		<!-- JPA에서 @Lock(LockModeType.PESSIMISTIC_WRITE)를 사용하는 것과 같은 비관적 락 설정 -->
    <select id="selectSlotForUpdate" resultType="com.example.LunchGo.reservation.domain.ReservationSlot">
        SELECT
            slot_id AS slotId,
            restaurant_id AS restaurantId,
            slot_date AS slotDate,
            slot_time AS slotTime,
            max_capacity AS maxCapacity,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM restaurant_reservation_slots
        WHERE restaurant_id = #{restaurantId}
          AND slot_date = #{slotDate}
          AND slot_time = #{slotTime}
            FOR UPDATE
    </select>

    <!--  특정 슬롯에 관한 예약자 수의 총합을 계산한 후 반환  -->
    <!--  예약석을 점유하는 예약상태, 즉 TEMPORARY(임시예약), CONFIRMED(예약비 결제완료), PREPAID_CONFIRMED(선결제/선주문 결제완료)인 경우에만 예약인원수를 합산 -->
    <!--  예약상태가 임시, 예약금 결제완료, 선결제/선주문 결제완료인 예약 내역에서만 인원수 합산  -->
    <select id="sumPartySizeBySlotId" resultType="int">
        SELECT COALESCE(SUM(party_size), 0)
        FROM reservations
        WHERE slot_id = #{slotId}
          AND status IN ('TEMPORARY', 'CONFIRMED', 'PREPAY_CONFIRM', 'REFUND_PENDING')
    </select>
```

---

### 서비스 계층 잔여석 계산 & 예약가능 여부 판정 로직 구현

**ReservationServiceImpl.java**

- 예약 슬롯에 관한 서비스 로직을 별도의 ReservationSlotService로 분리
- 구현한 코드에서는 insert문 등으로 예약 내역을 생성하기 전에 반드시 `reservationSlotService.getValidatedSlot()` 메서드를 호출하여 사용자가 지정한 식당, 날짜, 시간대에 해당하는 예약 슬롯에 락을 부여
    - 트랜잭션 전파로 인해 `reservationSlotService.getValidatedSlot()` 이 종료되더라도 설정한 락은 유지됨
      (`@Transactional`에서 propagation의 기본값은 `Propagation.REQUIRED`)
- 락을 걸고 예약을 진행 중인 트랜잭션에서는 해당하는 예약 슬롯을 조회하여 예약 내역 생성에 필요한 slotId를 확보해야 예약 생성 가능
    - 실질적은 락을 건 곳은 restaurant_reservation_slots이지만 결과적으로는 reservations 테이블에서의 동시성을 제어하는 효과가 발생
      (단, 중복 요청 처리를 방지하기 위한 멱등성 처리는 별도로 수행해야 함)
- ReservationServiceImpl의 `create()` 메서드 내부의 아래 코드에서 예약 생성 이전에 예약 슬롯에 관한 비관적 락을 거는 작업을 수행한 다음, 새로 생성된 예약 데이터를 DB에 추가

```java
// 지정한 날짜+시간대의 예약슬롯을 불러오는 서비스 로직(없으면 신규 생성)
// 내부적으로 비관적 락(FOR UPDATE)을 사용하여 슬롯 정원(Capacity) 동시성 제어 수행
ReservationSlot slot = reservationSlotService.getValidatedSlot(
        request.getRestaurantId(),
        request.getSlotDate(),
        request.getSlotTime(),
        request.getPartySize()
);
```


**ReservationSlotService.java**

- `getValidatedSlot` 메서드
    - 사용자가 지정한 식당, 날짜, 시간대에 해당하는 예약 슬롯 정보를 조회
    - 예약 내역을 생성하기 전에 예약 슬롯에 관한 락을 설정하는 로직을 포함
    - 실행이 종료되더라도 여전히 ReservationServiceImpl의 `create`에서 트랜잭션을 처리하고 있으므로 락은 계속 유지(→ 트랜잭션 전파)
- 새로운 예약 내역을 reservations 테이블에 추가할 때 필요한 slotId를 확보하려면 사용자가 지정한 식당, 날짜, 시간대에 관한 예약 슬롯을 생성해야 함.
    - 예약 생성 과정에서 예약 슬롯을 조회하는 Mapper의 쿼리를 요청할 시 일반 select문 대신 `select … from … for update` 문을 사용하면, 실질적으로는 조회만 수행하더라도 DB의 관점에서는 데이터 변경 작업을 수행할 것으로 간주하여 락을 부여
    - 예약 생성 과정에서 여러 개의 트랜잭션들이 예약 슬롯을 조회하는 작업은 모두 데이터 쓰기 작업으로 간주되어, 이미 예약을 진행 중인 트랜잭션이 있다면 대기
    - 예약 생성 작업을 수행하는 다수의 트랜잭션에서 각각 예약 슬롯을 조회할 때는 항상 `select … from … for update` 문을 수행하도록 구현

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private static final int DEFAULT_MAX_CAPACITY = 20;

    private final RestaurantRepository restaurantRepository;
    private final ReservationMapper reservationMapper;

    /**
     * 슬롯을 비관적 락으로 조회하고, 없으면 생성하며, 잔여석이 충분한지 검증까지 마친 후 반환
     */
    @Transactional
    public ReservationSlot getValidatedSlot(Long restaurantId, LocalDate date, LocalTime time, int requestedPartySize) {
        // 1. 비관적 락을 건 상태에서 슬롯 조회
        ReservationSlot slot = reservationMapper.selectSlotForUpdate(restaurantId, date, time);

        // 2. 슬롯이 없으면 생성
        if (slot == null) {
            log.info("지정한 시간대의 예약 슬롯이 없습니다. 슬롯을 생성합니다.");
            Optional<Integer> reservationLimit = restaurantRepository.findReservationLimitByRestaurantId(restaurantId);
            reservationMapper.upsertSlot(restaurantId, date, time, reservationLimit.orElse(DEFAULT_MAX_CAPACITY));

            // 생성 후 다시 락을 걸고 조회
            slot = reservationMapper.selectSlotForUpdate(restaurantId, date, time);

            if (slot == null) {
                throw new IllegalStateException("System Error: Slot creation failed.");
            }
        }
        log.info("{} {} 예약 슬롯을 불러옵니다.", date, time);

        // 3. 잔여석 계산
        // 잔여석 정보는 별도의 DB 속성값으로 저장하지 않고 계산 -> 예약 취소 시 별도의 잔여석 처리 없이 예약상태만 변경하면 끝
        // (추후 테이블에 잔여석 정보를 저장할 속성을 추가한다면, 예약 취소 시에도 잔여석 처리 로직을 추가해야 함)
        // 예약석을 점유하는 예약상태, 즉 TEMPORARY(임시예약), CONFIRMED(예약비 결제완료), PREPAID_CONFIRMED(선결제/선주문 결제완료)인 경우에만 예약한 인원수를 합산
        int currentTotal = reservationMapper.sumPartySizeBySlotId(slot.getSlotId());

        // 4. 잔여석 검증: 이미 해당 시간대에 예약한 사람이 있거나, 사용자가 지정한 인원수가 잔여석 개수를 초과할 경우 예약 불가
        if (currentTotal + requestedPartySize > slot.getMaxCapacity()) {
            log.info("잔여석이 부족합니다.");
            throw new IllegalStateException("잔여석이 부족합니다. (남은 좌석: " + (slot.getMaxCapacity() - currentTotal) + ")");
        }
        log.info("선택한 인원수: {}명, 현재 잔여석: {}석", requestedPartySize, slot.getMaxCapacity() - (currentTotal+requestedPartySize));

        return slot;
    }
}
```