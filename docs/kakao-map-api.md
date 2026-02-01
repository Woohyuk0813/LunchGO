# Kakao Map API 사용 정리

## 개요
- 프론트엔드: Kakao Maps JavaScript SDK 로딩 및 주소 -> 좌표 변환
- 백엔드: Kakao Local/Navigation API로 좌표 변환 및 거리/경로 계산

## 프론트엔드 사용

### SDK 로딩
- 파일: `frontend/src/utils/kakao.js`
- 로딩 방식: `https://dapi.kakao.com/v2/maps/sdk.js?appkey=...&autoload=false&libraries=services`
- `loadKakaoMaps()` 호출 시 SDK를 1회 로딩하고 `window.kakao.maps`를 반환

### 주소 -> 좌표 변환
- 함수: `geocodeAddress(address)`
- SDK 서비스: `kakao.maps.services.Geocoder().addressSearch`
- 반환: `{ lat, lng }`

### 주요 사용 위치
- 홈 지도 로딩/마커 렌더링: `frontend/src/composables/useHomeMap.js`
- 식당 상세 지도: `frontend/src/views/restaurant/id/RestaurantDetailPage.vue`

## 백엔드 사용

### 주소 -> 좌표 변환 (Kakao Local API)
- 서비스: `src/main/java/com/example/LunchGo/map/service/KakaoGeoService.java`
- 엔드포인트: `GET https://dapi.kakao.com/v2/local/search/address.json?query={address}`
- 헤더: `Authorization: KakaoAK {REST_API_KEY}`
- 캐시: `@Cacheable("geoCoordinates")`, Redis TTL 1시간

### 도로 거리/경로 계산 (Kakao Mobility Directions API)
- 서비스: `src/main/java/com/example/LunchGo/map/service/KakaoDirectionsService.java`
- 엔드포인트: `GET https://apis-navi.kakaomobility.com/v1/directions`
- 파라미터: `origin`, `destination`, `priority=RECOMMEND`
- 헤더: `Authorization: KakaoAK {REST_API_KEY}`

### 내부 API
- 거리 일괄 조회: `POST /api/map/distance`
  - 컨트롤러: `src/main/java/com/example/LunchGo/map/controller/MapDistanceController.java`
- 경로 조회: `POST /api/map/route`
  - 컨트롤러: `src/main/java/com/example/LunchGo/map/controller/MapDistanceController.java`

## 설정 키
- `kakao.rest-api-key`:
  - 사용처: `KakaoDirectionsService`
  - 설정 위치: `src/main/resources/application.yml`
- `kakao.rest-api-map-key`:
  - 사용처: `KakaoGeoService`
  - 주의: 설정 키 이름이 다르므로 환경 변수 이름을 구분해서 등록 필요

## 참고 파일
- 프론트 SDK 로더: `frontend/src/utils/kakao.js`
- 홈 지도: `frontend/src/composables/useHomeMap.js`
- 식당 상세 지도: `frontend/src/views/restaurant/id/RestaurantDetailPage.vue`
- 좌표 변환: `src/main/java/com/example/LunchGo/map/service/KakaoGeoService.java`
- 거리/경로: `src/main/java/com/example/LunchGo/map/service/KakaoDirectionsService.java`
- 지도 API 컨트롤러: `src/main/java/com/example/LunchGo/map/controller/MapDistanceController.java`
