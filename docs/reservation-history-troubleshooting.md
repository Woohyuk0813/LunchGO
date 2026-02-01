# 지난 예약 방문 횟수 트러블슈팅 기록

## 문제 1: 동일 식당 방문 횟수가 모든 카드에 동일하게 표시됨

### 증상
- 지난 예약 목록에서 같은 식당 방문 카드들이 모두 동일한 `n번째 방문`으로 표시됨
- `-10일만의 방문`, `-5일만의 방문`처럼 음수 혹은 이상한 간격이 표시됨

### 원인
- `restaurant_user_stats`의 누적 방문 횟수/마지막 방문일을 그대로 사용해서, 예약 건별 누적값이 아닌 최신 통계값이 모든 카드에 적용됨
- 예약별 순서를 고려하지 않아 `daysSinceLastVisit` 계산이 부정확하게 표시됨

### 해결
- MySQL 8.0 윈도우 함수로 예약 목록 쿼리에서 방문 누적/간격을 예약 건별로 계산
- `COMPLETED` 상태만 누적되도록 `SUM(CASE ...) OVER (...)` 사용
- 직전 방문일은 `MAX(CASE ...) OVER (... ROWS BETWEEN ... AND 1 PRECEDING)`로 계산

### 해결 과정
- 통계 테이블(`restaurant_user_stats`)은 사용자/식당 기준 "최신 누적값"만 제공함을 확인
- 예약 건별 정렬 기준(예약 일시)으로 윈도우 함수를 적용해 각 카드별 방문 정보를 계산

### 적용 파일
- `src/main/resources/mapper/ReservationHistoryMapper.xml`

### 적용 코드
```sql
-- 완료 예약만 누적 방문 횟수로 계산하여 카드마다 다른 방문 차수를 표시한다.
CASE
    WHEN r.status = 'COMPLETED' THEN
        SUM(CASE WHEN r.status = 'COMPLETED' THEN 1 ELSE 0 END) OVER (
            PARTITION BY r.user_id, res.restaurant_id
            ORDER BY slot.slot_date, slot.slot_time
        )
    ELSE NULL
END AS visitCount,
-- 완료 예약 직전의 완료 방문일만 기준으로 간격을 계산한다.
CASE
    WHEN r.status = 'COMPLETED' THEN
        DATEDIFF(
            slot.slot_date,
            MAX(CASE WHEN r.status = 'COMPLETED' THEN slot.slot_date END) OVER (
                PARTITION BY r.user_id, res.restaurant_id
                ORDER BY slot.slot_date, slot.slot_time
                ROWS BETWEEN UNBOUNDED PRECEDING AND 1 PRECEDING
            )
        )
    ELSE NULL
END AS daysSinceLastVisit
```

### 설명
통계 테이블은 최신 누적치만 제공하므로, 목록 카드용 누적/간격 계산은 쿼리에서 처리한다.

## 문제 2: 동일 예약의 영수증이 중복 생성되어 지난 예약 카드가 중복 노출됨

### 증상
- 리뷰 수정 화면에서 영수증을 재업로드할 때마다 `receipts`에 신규 레코드가 추가됨
- `receipts`가 `reservation_id`로 중복되면서 지난 예약 목록에서 동일 예약이 여러 건으로 보임

### 원인
- OCR 업로드는 항상 `receipts`에 신규 레코드를 생성함
- `receipts` 테이블에 `reservation_id` 유니크 제약이 없어서 동일 예약에 다중 영수증이 누적됨
- 지난 예약 조회가 `receipts`를 `reservation_id`로 조인하면서 중복 행이 생성됨

### 해결
- `receipts.reservation_id`에 유니크 제약 추가 또는 업로드 시 기존 영수증 갱신 방식으로 변경
- OCR 업로드 시 기존 영수증이 있으면 `confirmed_amount`, `image_url`, `receipt_items`를 교체
- 지난 예약 조회는 예약당 1건만 선택하도록 최신 영수증 기준으로 조인

### 해결 과정
- 동일 예약에서 OCR 재업로드 시 `receipts`에 신규 PK가 계속 생성되는 것을 DB에서 확인
- `reservation_id` 기준 중복이 지난 예약 목록 중복 노출의 직접 원인임을 확인

### 적용 파일
- `src/main/java/com/example/LunchGo/review/controller/OcrController.java`
- `src/main/java/com/example/LunchGo/review/service/ReceiptService.java`
- `src/main/resources/mapper/ReservationHistoryMapper.xml`
- `src/main/resources/sql/migration_add_receipt_reservation_unique.sql`

### 적용 코드
```sql
-- 예약당 최신 영수증만 남기기 위해 중복을 제거한다.
DELETE r1
FROM receipts r1
JOIN receipts r2
  ON r1.reservation_id = r2.reservation_id
 AND r1.receipt_id < r2.receipt_id;

-- 예약당 영수증 1건만 유지하도록 유니크 제약을 추가한다.
ALTER TABLE receipts
    ADD UNIQUE KEY uk_receipts_reservation (reservation_id);
```

```java
// OCR 재업로드 시 기존 영수증을 갱신하고, 항목은 전부 교체한다.
Receipt saved = receiptRepository.findTopByReservationIdOrderByCreatedAtDesc(reservationId)
    .map(existing -> {
        existing.updateConfirmedAmount(receiptDTO.getTotalAmount());
        existing.updateImageUrl(receiptImageKey);
        receiptItemRepository.deleteByReceiptId(existing.getReceiptId());
        return existing;
    })
    .orElseGet(() -> receiptRepository.save(
        new Receipt(reservationId, receiptDTO.getTotalAmount(), receiptImageKey)
    ));
```

```sql
-- 지난 예약 조회는 최신 영수증만 조인하여 중복 카드를 막는다.
LEFT JOIN (
    SELECT r1.*
    FROM receipts r1
    JOIN (
        SELECT reservation_id, MAX(receipt_id) AS max_id
        FROM receipts
        GROUP BY reservation_id
    ) r2 ON r2.max_id = r1.receipt_id
) rc ON rc.reservation_id = r.reservation_id
```

### 설명
중복 영수증은 업로드 단계에서 갱신하고, 목록 조회는 최신 1건만 조인한다.

## 문제 3: 리뷰 수정 시 태그 변경이 저장되지 않음

### 증상
- 리뷰 수정 화면에서 새 태그 선택 후 저장 시 경고 알림만 뜨고 저장이 되지 않음
- 기존 태그가 그대로 유지되거나, 태그 변경이 DB에 반영되지 않음

### 원인
- 저장 요청 시점에 `tagIdByName` 매핑이 비어 있는 경우가 있음
- 선택 태그는 UI에 남아 있지만, `tagIds`가 빈 배열로 전송되어 업데이트가 무시됨

### 해결
- 저장 직전에 태그 매핑이 비어 있으면 `/api/reviews/tags`를 재호출하여 매핑을 복구
- 응답이 `{ data: [...] }` 형식이더라도 정상 파싱되도록 보강

### 해결 과정
- 수정 화면에서 기존 태그를 모두 해제 후 새 태그만 선택하는 시나리오에서 재현
- `tagIdsForSubmit`이 빈 배열로 전송되는 것을 확인

### 적용 파일
- `frontend/src/views/restaurant/id/reviews/WriteReviewPage.vue`

### 적용 코드
```js
// 저장 직전에 태그 ID 매핑이 비어 있으면 재조회해 복구한다.
let tagIdsForSubmit = selectedTags.value
  .map((tag) => tagIdByName.value[tag])
  .filter((id) => id);
if (selectedTags.value.length > 0 && tagIdsForSubmit.length === 0) {
  const refreshedMap = await loadTagMap();
  tagIdsForSubmit = selectedTags.value
    .map((tag) => refreshedMap[tag])
    .filter((id) => id);
}
if (selectedTags.value.length > 0 && tagIdsForSubmit.length === 0) {
  alert("리뷰 태그 정보를 불러오지 못했습니다. 다시 시도해주세요.");
  return;
}
```

### 설명
태그 변경은 `selectedTags` 기준으로 `tagIds`를 재계산하며, 매핑이 비면 재조회로 복구한다.

## 문제 4: 리뷰 수정에서 새로 추가한 태그가 DB에 저장되지 않음

### 증상
- 수정 화면에서 기존 태그는 보이지만, 새로 추가한 태그가 저장되지 않음
- 저장 후에도 DB(`review_tag_maps`)에 새 태그가 반영되지 않음

### 원인
- `loadTagMap()`으로 전체 태그 매핑을 받아오더라도,
  `loadExistingReview()`가 `tagIdByName`을 **기존 리뷰 태그만**으로 덮어씀
- 그 결과 신규 태그의 ID 매핑이 사라져 `tagIds`가 빈 값으로 전송됨

### 해결
- 기존 리뷰 태그 매핑을 덮어쓰지 않고 **merge**로 보존
- `loadTagMap()`을 먼저 실행한 뒤 `loadExistingReview()`를 수행하도록 순서 보장

### 해결 과정
- 수정 화면에서 `tagIdByName` 값이 기존 태그만 남는 것을 확인
- 저장 요청의 `tagIds`가 신규 태그를 포함하지 않는 것을 확인

### 적용 파일
- `frontend/src/views/restaurant/id/reviews/WriteReviewPage.vue`

### 적용 코드
```js
// 기존 태그 매핑을 덮어쓰지 않고 merge하여 신규 태그도 ID 매핑을 유지한다.
tagIdByName.value = {
  ...tagIdByName.value,
  ...tags.reduce((acc, tag) => {
    acc[tag.name] = tag.tagId;
    return acc;
  }, {}),
};

// 태그 전체 매핑을 먼저 로드한 뒤 기존 리뷰 데이터를 읽는다.
onMounted(async () => {
  setupDragScroll();
  await loadTagMap();
  await loadExistingReview();
  loadReservationSummary();
});
```

### 설명
태그 매핑은 전체 목록을 먼저 읽고, 리뷰 태그는 병합 처리하여 신규 태그도 유지한다.
