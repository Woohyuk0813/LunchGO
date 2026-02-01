# 동시성 제어 - 중복 예약 처리

## 문제 1: 이미 생성된 중복 예약으로 인한 unique 키 생성 불가

### 증상

- reservations 테이블에 user_id, slot_id에 관한 unique 복합 키를 사용하여 중복 예약을 방지하려 했으나, unique 키를 적용할 수 없는 문제가 발생함

### 원인

- 이미 reservations 테이블에 user_id, slot_id가 중복되는 데이터 행들이 존재하여 unique 키를 적용하지 못 함.

### 해결 방안 1 - reservations 테이블의 중복 데이터 삭제(적용)

- reservations 테이블에 이미 저장된 중복 데이터만 찾아서 삭제하는 방식
- reservations 테이블의 중복 데이터를 그대로 삭제한다는 아래의 방안들은 위험부담이 컸음.
  - 방안1) reservations 테이블에 설정된 외래키 참조 비활성화 후 중복 데이터 삭제 
    - 문제점: reservations 테이블을 참조 중인 다수의 테이블들이 존재하여, 단순히 reservations 테이블의 중복된 데이터만을 삭제하면 다른 테이블과의 데이터 정합성에 문제가 생길 가능성이 있음
  - 방안2) reservations 테이블의 fk 키에 `ON DELETE CASCADE` 옵션을 적용한 후 중복 데이터 삭제
    - 문제점: 이 방식을 사용할 경우, reservations 테이블의 중복 데이터뿐만 아니라, 해당 데이터를 참조하는 다른 테이블의 데이터까지 삭제되어 잠재적인 문제가 발생할 가능성이 있음
  - 방안3) 기존 외래키 관계를 유지하면서, reservations 테이블에서 참조하지 않는 중복 데이터를 찾아 삭제
    - 1단계: 아래의 sql 쿼리를 사용하여 reservations 테이블에서 user_id, slot_id가 중복된 데이터를 탐색
      ```
      SELECT r.reservation_id, r.slot_id
      FROM reservations r
        INNER JOIN
        (SELECT user_id, slot_id, IF(status IN ('TEMPORARY', 'CONFIRMED', 'PREPAY_CONFIRM'), 1, NULL) AS is_active, COUNT(*)
         FROM reservations
         GROUP BY user_id, slot_id, is_active
         HAVING COUNT(*) > 1) r2
      on r.user_id = r2.user_id and r.slot_id = r2.slot_id;
      ```
    - 2단계: reservations 테이블의 기본키를 외래키로 사용하는 테이블들을 파악한 후, 찾은 각각의 테이블에서 참조하는 reservation_id를 조회
      ```sql
      SELECT
        TABLE_NAME,
        COLUMN_NAME,
        CONSTRAINT_NAME
      FROM
        INFORMATION_SCHEMA.KEY_COLUMN_USAGE
      WHERE
        REFERENCED_TABLE_NAME = 'reservations'
          AND REFERENCED_TABLE_SCHEMA = DATABASE();
      
      
      select review_id, reservation_id from reviews where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      select payment_id, reservation_id from payments where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      select receipt_id, reservation_id from receipts where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      select cancellation_id, reservation_id from reservation_cancellations where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      select reservation_id from reservation_visit_stats where reservation_id in (1, 7, 8, 9, 11, 12, 13, 233, 244, 262, 258, 259, 260, 271, 267);
      ```
      - 위의 쿼리를 사용하여 외래키로 사용된 reservation_id를 파악한 다음, reservations에서 user_id, slot_id가 중복된 데이터 행 중 아래 조건에 해당하는 행을 삭제
        - reservations 테이블에서 deposit_amount, prepay_amount 속성값이 모두 null인 중복 데이터 삭제
          (다행히 이 기준을 적용하는 것만으로도 unique 제약조건을 적용할 때 문제가 되었던 중복 데이터를 삭제하는 데 성공함)

### 해결 방안 2 - Redis 활용 및 예약에 관한 중복 데이터 방지 로직 추가(1차 방어-적용)

- reservations 테이블에 이미 저장된 중복 데이터는 삭제하지 않고, 향후 동일 예약 요청을 중복 처리하는 것을 방지하는 것에 집중
- 다음의 장점으로 인해, Redis를 활용하면서, 애플리케이션 레벨에서 중복 요청을 처리하는 방향으로 진행
  - 애플리케이션 레벨에서 사용자의 중복된 예약 요청이 DB에 도달하기 전에 미리 방어
  - 따라서, 기존의 DB 비관적 락과 Redis 락을 모두 활용해야 함
    - DB 비관적 락: 예약 인원이 식당의 최대 수용가능인원을 초과하는 것을 방지(여러 명이 동시에 한 식당을 예약할 때의 동시성 제어 담당)
    - Redis : 짧은 시간 동안 동일한 예약 요청이 중복 발생하는 것을 방지(1명이 식당 한 곳에 중복된 예약 신청을 넣지 않도록 제어)
- 예약 슬롯 생성 직전 3초 간 Redis 락을 설정 
  - 1명의 사용자가 짧은 시간 동안 중복된 예약 요청을 보냈을 때, 이미 처리된 예약을 다시 처리하게 되는 것을 방지하는 용도
  - ReservationServiceImpl의 `create` 메서드 내부에서는 예약 슬롯 생성 직전에 아래의 코드를 실행
  - ```java
    String lockKey = String.format(RESERVATION_LOCK_KEY_FORMAT, request.getUserId(), request.getRestaurantId(), request.getSlotDate(), request.getSlotTime());
    if (!redisUtil.setIfAbsent(lockKey, RESERVATION_LOCK_VALUE, RESERVATION_LOCK_TIMEOUT_MS)) {
        throw new DuplicateReservationException("이미 처리 중인 예약 요청입니다. 잠시 후 다시 시도해주세요.");
    }
    ```

---

## 문제 2 - 예약 취소 후 다시 예약하는 상황에 관한 처리

### 문제 상황 가정

- 위와 같이 중복 예약 방지 로직을 구현하더라도, 사용자1이 A식당을 2025-12-31 13:00에 예약하였으나, 인원수 변동으로 인해 예약을 취소하고 다시 예약할 때는 허용할 수 있어야 함.

### 기존 unique 키의 문제

- 초기 구상대로 reservations 테이블의 user_id, slot_id만을 활용하여 unique 키를 생성할 경우, 현재 예약상태를 반영하지 못함

### 해결 방안: 조건부 unique 인덱스의 활용

- 동일한 예약 요청을 중복 처리하는 것을 방지하면서도, 동일한 사용자가 취소 후 다시 예약할 때는 예약이 가능해야 함
  - 현재 예약상태에 따라 선택적으로 멱등성 처리를 수행할 수 있는 조건부 unique 키를 사용
    - 예약석을 점유 중인 사용자는 동일한 예약 요청을 중복해서 보낼 수 없지만, 인원수 변동 등의 이유로 기존 예약을 취소하면서 점유한 예약석이 풀리면 다시 예약이 가능해야 함.
    - user_id, slot_id, is_active 속성으로 구성된 복합 키
    - user_id, slot_id는 reservations 테이블을 구성하는 실제 속성이지만, is_active는 reservations의 status 속성값에 의해 값이 결정되는 가상 칼럼
  - is_active(타입: boolean)
    - 사용자가 이미 예약 완료된 상태에서 동일한 예약을 중복 요청하는 것인지, 기존 예약을 취소한 후 다시 예약하는 것인지를 식별하기 위한 가상 칼럼
    - true: reservations 테이블의 status 속성값 중 예약석을 점유하게 되는 예약상태('TEMPORARY', 'CONFIRMED', 'PREPAY_CONFIRM', 'REFUND_PENDING')일 때
    - NULL
      - 사용자가 점유 중이던 예약석이 풀리는 나머지 상태('EXPIRED', 'COMPLETED', 'REFUNDED', 'NO_SHOW', 'CANCELED')일 때에 해당
      - 인덱스는 NULL값을 중복된 값으로 처리하지 않는다는 점을 활용해야 하므로, **절대 false값을 사용하지 않음**
      - 즉, 예약석의 점유가 풀리는 횟수에는 제한을 두지 않아야 예약석이 문제 없이 풀릴 수 있음
  - DB의 가상 컬럼
    - DB 테이블을 구성하는 물리적인 컬럼으로부터 불러오는 값이 아닌, 다른 컬럼들의 값을 토대로 계산된 결과값을 제공하는 컬럼
    - DB 인덱스를 구성하는 속성으로 가상 칼럼을 활용할 수 있음

### 실제 적용 과정
  
- DB 차원에서는 reservations 테이블에 조건부 unique 인덱스를 사용
  - 새로운 테이블 속성을 추가하여 기존 reservations 테이블 스키마를 변경하지 않고도 가상 칼럼을 활용한 unique 제약 조건을 적용하기 위해 사용
  - (MySQL 8.0.13 버전 이후부터 제공되는 함수 기반 인덱스 문법 사용)
  ```sql
  CREATE UNIQUE INDEX uk_user_slot_active
  ON reservations (
        user_id,
        slot_id,
        (IF(status IN ('TEMPORARY', 'CONFIRMED', 'PREPAY_CONFIRM', 'REFUND_PENDING'), 1, NULL)) -- 수식을 직접 작성
  );
  ```
  - 중복 예약 방지 로직에서 조건부 unique 인덱스가 동작하는 방식 
    - 예약석을 점유하는 상태일 경우(is_active=true): 해당 예약 내역이 오직 1개만 존재하도록 unique 제약조건을 엄격하게 적용
    - 예약석을 점유하지 않는 상태(is_active=NULL)에서는 unique 제약 조건 적용 대상에서 제외 
      - sql 표준에서의 null은 '알 수 없는 값'이란 의미이기 때문에, unique 제약조건에서는 null값의 중복을 허용
      - 따라서, 예약 취소로 인해 is_active의 값이 null이 되면 사용자가 인원수 수정 등의 이유로 예약을 취소하고 다시 예약 신청하는 것이 가능해짐

### 보충 정리: unique 인덱스와 Redis를 함께 활용하는 이유

- **요약: 둘 다 적용하는 것이 최선**
- unique 키/인덱스와 Redis는 다음과 같은 부분에서 각각 장점을 발휘
  - Redis : 중복된 예약 요청이 DB에 도달하기 전, 애플리케이션 레벨에서 미리 차단 가능
  - unique 키/인덱스 : 1개의 예약 내역을 DB에 저장할 때 데이터 무결성을 빠르게 검증할 수 있다는 점에서 유리
- redis 없이 unique 인덱스만 적용할 경우, 모든 예약 요청이 애플리케이션 레벨에서의 필터링 없이 DB에 도달
  - 모든 예약 요청이 DB에 도달하게 되면 DB에 가해지는 부하의 증가로 인해 전체 서비스가 느려지는 등의 성능 문제가 발생할 수 있음
  - 예약 생성 과정에서 오류가 발생한 경우에도 DB의 auto increment id값(주로 pk)이 증가하는 등의 부수적인 문제도 발생
- redis를 사용하면 애플리케이션 레벨에서 일부 중복 예약 요청을 먼저 차단하게 되어 DB의 부담이 감소
  - 중복 예약 처리와는 별개로, redis는 예약 프로세스 만료시간을 설정할 때도 활용될 수 있음

---

## 문제 3: SQLIntegrityConstraintViolationException 예외 처리

### 증상

- 현재 로그인한 사용자가 같은 식당을 대상으로 중복된 예약 요청을 보내면 SQLIntegrityConstraintViolationException 예외가 발생하면서 강제 로그아웃되는 문제 발생


### 원인

- 중복된 예약 요청 자체가 reservations 테이블을 대상으로 이전에 설정했던 unique 제약조건을 충족하지 못하면서 SQLIntegrityConstraintViolationException이 발생
- 그러나, 발생한 SQLIntegrityConstraintViolationException에 관한 예외 처리를 수행하지 않아 강제 로그아웃되는 문제 발생

### 해결

- ReservationServiceImpl.java의 `create` 메서드에서 예약 생성용 insert문 쿼리를 실행 중 SQLIntegrityConstraintViolationException 발생 시 409 상태코드를 반환하도록 예외 처리

```java
try {
    reservationMapper.insertReservation(reservation);
} catch (DataIntegrityViolationException e) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 예약 요청입니다.");
}
```

- RestaurantBookingPage.vue 파일의 handleProceed 함수에서 409 상태코드를 처리하도록 catch 블록 내부를 아래와 같이 수정 
  - 409 상태코드를 반환받을 시, 에러 메시지를 띄운 후 메인 페이지로 이동하도록 처리
  - 그 외에는 이전처럼 에러 처리

```javascript
if (e.response?.status === 409) {
  alert('이미 예약 처리 중인 식당입니다. 예약 내역을 확인해 주세요.');
  router.push('/');
} else {
  createErrorMessage.value = e.response?.data?.message || e?.message || '예약 생성 중 오류가 발생했습니다.';
}
```

---

## 문제 4: 409 에러 처리 로직 수행 오류

### 증상

- 중복 예약 발생 시 반환된 409 상태코드를 처리하는 로직을 추가했음에도 불구하고, 지정한 에러 처리 로직 대신 "인증에 실패했습니다."라는 메시지만 예약 정보 입력 페이지 하단에 뜨는 문제 발생

### 원인

- ReservationServiceImpl에서 중복 예약 발생 시 409 예외를 던졌을 때 스프링 부트가 내부적으로 에러 처리를 수행하기 위해 `/error` 경로로 요청을 포워딩
- 하지만 SecurityConfig에서는 `/error`를 별도로 인가하지 않았기 때문에 이 요청이 `anyRequest().authenticated()`에 막혀버리는 문제 발생

### 해결방안 1: SecurityConfig 설정 변경

- SecurityConfig 내부에 `.requestMatchers("/error").permitAll()` 설정 추가
  - 이 설정을 추가하는 것은 보안을 해제하는 것이 아닌, 내부 서비스 로직에서 클라이언트에게 전달할 에러 응답이 보안 필터에 가로막히지 않게 한다는 것을 의미
  - 위 설정을 추가하더라도 API 요청이 백엔드로 전달될 때의 보안 검사는 여전히 유효
- 코드 변경을 최소화한다는 측면에서 위의 방안을 적용
  - 구조적인 측면에서는 `@ControllerAdvice를 도입하는 것이 좋지만, 서비스 계층에서 발생한 모든 예외에 관한 처리 로직을 작성해야 하므로 코드 변경 범위가 큼

### 해결방안 2: 예약 기능 전용 예외 처리 핸들러 추가

- SecurityConfig에 `.requestMatchers("/error").permitAll()`란 설정을 추가하는 방안을 적용했지만 다음의 측면에서 개선의 필요성을 느낌
  - 서비스 계층 내부에 새로운 예외 처리 코드를 추가할 때마다 서비스 계층의 로직을 수정해야 하는 번거로움
  - 서비스 계층의 코드 분량이 늘어나면서 가독성이 떨어지고, 예외 처리 코드를 파악하고 관리하는 것이 어려움
  - SecurityConfig에 `.requestMatchers("/error").permitAll()`이란 설정을 포함시켰을 때, 해당 설정이 가진 의미가 명확하지 않음
- 이전에는 `@ControllerAdvice` 클래스를 도입할 경우 전역적인 예외 처리 핸들러를 작성하는 것의 부담으로 인해 해당 방안을 사용하지 않았으나, 다음의 절충안을 사용
  - `@ControllerAdvice` 어노테이션의 `basePackage` 또는 `assignableTypes` 속성값을 사용하여 적용 범위를 줄이는 것이 가능
    - `basePackage`
      - `@ControllerAdvice` 또는 `@RestControllerAdvice`가 붙은 핸들러 클래스에서 사용
      - 예외 처리 핸들러를 적용할 특정 패키지를 지정
      - 특정 도메인에 관한 여러 개의 컨트롤러에 대해 공통으로 예외 처리 가능 
    - `assignableTypes`
      - `@ControllerAdvice` 또는 `@RestControllerAdvice`가 붙은 핸들러 클래스에서 사용
      - 특정 컨트롤러에서 발생한 예외만 처리하도록 명시적으로 지정
    - 컨트롤러 내부에서 `@ExceptionHandler` 정의
      - 별도의 핸들러 클래스 생성 불필요
      - 예외 처리를 수행할 컨트롤러의 메서드에 사용, 해당 컨트롤러 내부에서 발생한 예외만 처리
      - 적용 범위가 가장 좁은 방식 -> 다른 컨트롤러에서는 재사용 불가
- 예약 생성 중 발생한 409 에러에 관한 예외 처리 시 `@RestControllerAdvice`를 사용
  - 이미 RESTful API 환경에서 기능 구성을 진행하고 있었고, JSON 형식으로 반환된 예외 메시지를 활용하여 409 에러 발생 시 그에 관한 에러 메시지를 화면에 출력해야 했기 때문
  - 따라서, 아래의 핸들러 클래스를 사용
  - 현재는 잔여석 부족, 중복 예약 처리 요청으로 인한 예외 발생 시 409 상태코드를 반환하는 예외 처리 핸들러만 추가한 상태
  ```java
  import org.springframework.http.HttpStatus;
  import org.springframework.http.ResponseEntity;
  import org.springframework.web.bind.annotation.ExceptionHandler;
  import org.springframework.web.bind.annotation.RestControllerAdvice;
  
  import java.util.HashMap;
  import java.util.Map;
  
  @RestControllerAdvice(basePackages = "com.example.LunchGo.reservation")
  public class ReservationExceptionHandler {
  
      @ExceptionHandler({DuplicateReservationException.class, SlotCapacityExceededException.class})
      public ResponseEntity<Map<String, String>> handleReservationExceptions(RuntimeException e) {
          Map<String, String> response = new HashMap<>();
          response.put("message", e.getMessage());
          
          return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
      }
  }
  ```

- 발생한 예외가 무엇인지를 명확히 표현하기 위해, ReservationServiceImpl 클래스 내부의 예외 생성 로직에서는 기존에 사용했던 예외를 커스텀 예외 클래스로 교체
  - SQLIntegrityConstraintViolationException -> DuplicateReservationException
  - IllegalStateException -> SlotCapacityExceedException

- 이 해결방안을 적용한 후 기존에 SecurityConfig에 추가했던 `.requestMatchers("/error").permitAll()` 설정은 불필요하여 삭제