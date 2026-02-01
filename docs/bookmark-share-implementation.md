# 즐겨찾기 공유(링크) 기능 구현 문서

## 작업 목표
- 사용자 간 즐겨찾기 목록을 공유하기 위해 이메일 기반 검색과 링크(공유 요청) 기능을 제공한다.
- 링크 승인 시 상대방의 즐겨찾기 목록을 폴더 형태로 구분하여 조회할 수 있게 한다.
- 동기화 요청을 통해 상대방의 최신 공개 즐겨찾기 목록을 업데이트할 수 있게 한다.
- 사용자가 공개로 설정한 즐겨찾기만 공유되도록 공개 여부 토글 기능을 제공한다.

## 세부 기능
### 1) 링크(공유 요청) 흐름
- 이메일로 사용자 검색
- 링크 요청 발송
- 링크 요청 수락/거절
- 링크 상태 조회(요청함/수신함)
- 동일 사용자 쌍은 단일 링크만 허용(양방향 중복 금지)

### 2) 즐겨찾기 공개 범위
- 즐겨찾기 항목 단위 공개 여부 토글
- 공개된 항목만 공유 목록에 포함
- 비공개로 전환 시 공유 목록에서 제외

### 3) 공유 폴더 및 목록 조회
- 링크 승인 시 상대방을 공유 폴더로 노출
- 폴더 진입 시 상대방 공개 즐겨찾기 목록을 실시간 조회
- 폴더별 최신화 이력 표시는 옵션

### 4) 동기화 정책
- 링크된 사용자의 공개 즐겨찾기 추가 시 자동 반영
- 비공개 전환/삭제 시 공유 목록에서도 제외
- 동기화 요청 버튼은 제공하지 않음

## 작업 순서 및 태스크 체크리스트

### A. 요구사항 확정
- [x] 링크 요청/수락/거절 상태 모델 정의
- [x] 공개 범위 단위 확정(항목 단위)
- [x] 동기화 정책 확정(자동 반영)
- [ ] 공유 폴더/목록 UI 동작 정의

### B. DB 설계/마이그레이션
- [x] 즐겨찾기 공개 여부 필드 추가 (예: `bookmark.is_public`)
- [x] 링크 관계 테이블 설계
  - [x] 요청자/수신자, 상태(PENDING/APPROVED/REJECTED), 요청/처리 시각
  - [x] 양방향 중복 링크 방지를 위한 유니크 제약
- [x] SQL 파일 업데이트
  - [x] `src/main/resources/sql/*`

### C. 백엔드 도메인/리포지토리
- [x] 링크 엔티티/리포지토리 추가
- [x] 즐겨찾기 공개 여부 필드 반영

### D. 백엔드 API 설계/구현
- [x] 사용자 이메일 검색 API
- [x] 링크 요청/수락/거절 API
- [x] 링크 상태 조회 API (요청함/수신함)
- [x] 즐겨찾기 공개 여부 토글 API
- [x] 공유 폴더/목록 조회 API
- [x] 자동 반영 정책에 맞는 조회 기준 정의

### E. 서비스 로직
- [x] 링크 승인 시 공유 폴더 노출
- [x] 공개 항목만 조회되도록 필터링
- [x] 비공개 전환/삭제 시 공유 목록에서 제외
- [x] 링크 거절/해제 시 공유 폴더 처리 정책 결정

### F. 프론트 UI/UX
- [x] 이메일 검색 및 링크 요청 UI
- [x] 링크 요청 수신/발신 목록 UI
- [x] 공유 폴더 리스트 UI
- [x] 공유 폴더 상세 목록 UI
- [x] 자동 동기화 안내 문구/상태 표시
- [x] 즐겨찾기 공개 여부 토글 UI

### G. 프론트 API 연동
- [x] 링크 요청/수락/거절/목록 API 연동
- [x] 공유 폴더/목록 조회 API 연동
- [x] 자동 동기화 처리 방식에 맞는 UI 갱신
- [x] 공개 여부 토글 API 연동
- [ ] 상태 관리(Pinia) 적용

### H. 검증
- [x] 링크 요청 → 수락 → 폴더 생성 확인
- [x] 공개 토글 on/off에 따른 공유 목록 반영 확인
- [x] 공개 추가/비공개 전환/삭제 시 공유 목록 자동 반영 확인
- [x] 링크 거절/해제 시 목록 처리 확인

---

## 백엔드 구현 요약

### DB 변경
- `bookmarks.is_public` 컬럼 추가 (기본값 0)
- `bookmark_links` 테이블 추가
  - 요청자/수신자, 상태(PENDING/APPROVED/REJECTED), 생성/응답 시각
  - 양방향 중복 금지: `pair_min/pair_max` 유니크
  - 자기 자신 링크 방지 체크
- 마이그레이션: `src/main/resources/sql/migration_add_bookmark_share.sql`
- 시드 업데이트: `src/main/resources/sql/insert_data_member_info.sql`

### 엔티티/도메인
- `Bookmark.isPublic` 추가
- `BookmarkLink` 엔티티 및 `BookmarkLinkStatus` enum 추가

### 주요 API
#### 링크 요청/응답
- `POST /api/bookmark-links`
  - 요청: `{ "requesterId": 1, "receiverId": 6 }`
  - 응답: `{ "linkId": 4 }`
- `PATCH /api/bookmark-links/{linkId}`
  - 요청: `{ "status": "APPROVED" | "REJECTED" }`

#### 링크 목록 조회
- `GET /api/bookmark-links/sent?requesterId=1[&status=PENDING]`
- `GET /api/bookmark-links/received?receiverId=6[&status=PENDING]`
  - 응답 필드: `linkId`, `requesterId`, `receiverId`, `status`, `createdAt`, `respondedAt`,
    `counterpartId`, `counterpartEmail`, `counterpartNickname`, `counterpartName`

#### 이메일 검색
- `GET /api/bookmark-links/search?email=...`
  - 응답: `{ "userId", "email", "nickname", "name" }`
- `GET /api/bookmark-links/search/list?query=...`
  - 응답: `[{ "userId", "email", "nickname", "name" }, ...]`

#### 링크 해제
- `DELETE /api/bookmark-links?requesterId=1&receiverId=2`

#### 즐겨찾기 공개 토글
- `PATCH /api/bookmark/visibility`
  - 요청: `{ "userId": 1, "restaurantId": 1, "isPublic": true }`

#### 즐겨찾기 프로모션 토글
- `PATCH /api/bookmark/promotion?userId=1&restaurantId=1&promotionAgree=true`

#### 상대방 공개 즐겨찾기 조회
- `GET /api/bookmark/shared?requesterId=1&targetUserId=6`
  - 승인된 링크가 있어야 조회 가능 (없으면 403)
  - 응답 필드: `restaurantId`, `name`, `roadAddress`, `detailAddress`, `imageUrl`, `rating`, `reviewCount`

#### 내 즐겨찾기 목록 조회
- `GET /api/bookmark/list?userId=1`
  - 응답 필드: `restaurantId`, `name`, `description`, `avgMainPrice`, `reservationLimit`,
    `promotionAgree`, `isPublic`, `imageUrl`, `rating`, `reviewCount`

### 서비스/레포지토리 포인트
- 링크 요청 시 중복/자기자신 요청 검증
- 이메일 검색으로 존재 사용자만 링크 가능
- 공유 목록은 `APPROVED` 상태 링크에서만 조회 가능
- 공개 토글은 중복 즐겨찾기 대비 최신 1건 기준 처리
- 즐겨찾기 목록은 식당별 최신 1건만 노출

### 핵심 구현 코드

#### 링크 요청/수락 API
```java
@PostMapping
public ResponseEntity<BookmarkLinkResponse> requestLink(@RequestBody BookmarkLinkRequest request) {
    // 링크 요청 생성 (중복/자기자신 요청은 서비스에서 차단)
    return ResponseEntity.status(HttpStatus.CREATED).body(bookmarkLinkService.requestLink(request));
}

@PatchMapping("/{linkId}")
public ResponseEntity<Void> respondLink(@PathVariable Long linkId,
                                        @RequestBody BookmarkLinkRespondRequest request) {
    // PENDING 상태만 승인/거절 가능
    bookmarkLinkService.respondLink(linkId, request);
    return ResponseEntity.ok().build();
}
```

#### 링크 요청 검증
```java
if (request.getRequesterId().equals(request.getReceiverId())) {
    // 자기 자신에게 링크 요청 불가
    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "자기 자신에게 링크 요청을 보낼 수 없습니다.");
}

if (bookmarkLinkRepository.existsBetweenUsers(request.getRequesterId(), request.getReceiverId())) {
    // 양방향 중복 요청 차단
    throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 링크 요청이 존재합니다.");
}
```

#### 링크 목록(상대방 정보 포함) 조회
```java
@Query("""
    SELECT new com.example.LunchGo.bookmark.dto.BookmarkLinkListItem(
        b.linkId, b.requesterId, b.receiverId, b.status, b.createdAt, b.respondedAt,
        u.userId, u.email, u.nickname, u.name
    )
    FROM BookmarkLink b
    JOIN User u ON b.receiverId = u.userId
    WHERE b.requesterId = :requesterId
    ORDER BY b.createdAt DESC
    """)
List<BookmarkLinkListItem> findSentWithCounterpart(Long requesterId);
```

#### 즐겨찾기 공개 토글
```java
Bookmark bookmark = bookmarkRepository.findTopByUserIdAndRestaurantIdOrderByBookmarkIdDesc(
        request.getUserId(), request.getRestaurantId())
    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "즐겨찾기를 찾을 수 없습니다."));

// 공개/비공개 토글 반영
bookmark.updatePublic(request.getIsPublic());
```

#### 상대방 공개 즐겨찾기 조회
```java
if (!bookmarkLinkRepository.existsApprovedBetweenUsers(requesterId, targetUserId)) {
    // 링크 승인 상태가 아니면 공유 목록 접근 불가
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "링크가 승인되지 않았습니다.");
}

return bookmarkRepository.findPublicBookmarksWithRestaurant(targetUserId);
```

#### 즐겨찾기 목록 조회(식당별 최신)
```java
SELECT
    b.bookmark_id AS bookmarkId,
    r.restaurant_id AS restaurantId,
    r.name AS name,
    r.description AS description,
    r.avg_main_price AS avgMainPrice,
    r.reservation_limit AS reservationLimit,
    CASE WHEN b.promotion_agree = 1 THEN 1 ELSE 0 END AS promotionAgree,
    CASE WHEN b.is_public = 1 THEN 1 ELSE 0 END AS isPublic,
    ri.image_url AS imageUrl,
    COALESCE(AVG(rv.rating), 0) AS rating,
    COUNT(rv.review_id) AS reviewCount
FROM bookmarks b
JOIN restaurants r ON b.restaurant_id = r.restaurant_id
LEFT JOIN restaurant_images ri ON ri.restaurant_id = r.restaurant_id
LEFT JOIN reviews rv ON rv.restaurant_id = r.restaurant_id
WHERE b.user_id = :userId
  AND b.bookmark_id = (
    SELECT MAX(b2.bookmark_id)
    FROM bookmarks b2
    WHERE b2.user_id = b.user_id AND b2.restaurant_id = b.restaurant_id
  )
GROUP BY
  b.bookmark_id, r.restaurant_id, r.name, r.description, r.avg_main_price,
  r.reservation_limit, b.promotion_agree, b.is_public, ri.image_url
ORDER BY b.bookmark_id DESC
```

### 변경 파일
- `src/main/java/com/example/LunchGo/bookmark/entity/Bookmark.java`
- `src/main/java/com/example/LunchGo/bookmark/entity/BookmarkLink.java`
- `src/main/java/com/example/LunchGo/bookmark/domain/BookmarkLinkStatus.java`
- `src/main/java/com/example/LunchGo/bookmark/controller/BookmarkController.java`
- `src/main/java/com/example/LunchGo/bookmark/controller/BookmarkLinkController.java`
- `src/main/java/com/example/LunchGo/bookmark/service/BaseBookmarkService.java`
- `src/main/java/com/example/LunchGo/bookmark/service/BaseBookmarkLinkService.java`
- `src/main/java/com/example/LunchGo/bookmark/repository/BookmarkRepository.java`
- `src/main/java/com/example/LunchGo/bookmark/repository/BookmarkLinkRepository.java`
- `src/main/java/com/example/LunchGo/bookmark/dto/*`
- `src/main/resources/sql/tables_create_member_info.sql`
- `src/main/resources/sql/migration_add_bookmark_share.sql`
- `src/main/resources/sql/insert_data_member_info.sql`
