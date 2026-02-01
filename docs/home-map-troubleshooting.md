# 홈 지도 트러블슈팅 기록

## 문제 1: 지도 줌 인/아웃 시 kakao.js 에러 스팸 발생

### 증상
- 홈 지도에서 줌 인/아웃 할 때 콘솔에 `kakao.js:127 Uncaught TypeError: Cannot read properties of null (reading 'b')`가 대량 발생
- 에러 발생 후에도 지도는 동작하지만 콘솔이 지속적으로 오염됨

### 원인
- 줌/드래그 중에도 마커를 매번 제거/재생성하는 렌더링 로직이 실행됨
- Kakao Maps 내부 상태가 전환되는 타이밍에 `renderMapMarkers`가 호출되면서 내부 객체가 null이 되어 예외 발생
- 특히 `idle` 이벤트에서 마커 재렌더를 트리거하던 흐름이 줌 애니메이션과 충돌함

### 해결
- 줌/드래그 중에는 마커 렌더링을 수행하지 않도록 차단
- `idle` 이벤트에서 마커 재렌더 호출을 제거하고, 필요 시점에만 명시적으로 렌더링

### 해결 과정
- 경로 렌더링/마커 포커싱 로직과의 충돌을 의심하고 `setBounds` 및 줌 스케줄링을 제거
- 줌 이벤트 중 마커 렌더링이 호출되는 것을 확인 후, 인터랙션 중 렌더링 차단으로 안정화

### 적용 파일
- `frontend/src/composables/useHomeMap.js`

---

## 문제 2: 경로 렌더링 시 기존 마커가 숨겨지지 않음

### 증상
- “경로 및 거리 확인” 버튼 클릭 시에도 기존 식당 마커가 모두 표시됨
- 기대 동작: 회사 + 선택 식당 마커만 노출

### 원인
- 경로 조회 응답을 기다리는 동안 `routeFocus`가 아직 null인 상태에서 마커 렌더링이 수행됨
- `routeFocus`가 null이면 기본 마커 렌더링 로직이 실행되어 전체 마커가 유지됨
- 좌표가 문자열 형태로 들어와 `isValidCoords` 검증 실패 → routeFocus가 설정되지 않는 문제도 있었음

### 해결
- 경로 API 호출 전, 좌표가 확보되는 즉시 `setRouteFocus`를 먼저 실행하여 기존 마커 렌더링을 차단
- `setRouteFocus`에서 위경도를 숫자로 정규화해 `isValidCoords` 통과 보장

### 해결 과정
- 경로 API 응답 완료 전/후 렌더링 흐름 추적
- 좌표 타입 검증 추가 및 routeFocus 설정 타이밍을 앞당겨 해결

### 적용 파일
- `frontend/src/views/HomeView.vue`
- `frontend/src/composables/useHomeMap.js`

---

## 문제 3: 경로 포커스 해제 후 줌/드래그 시 kakao.js 에러 재발

### 증상
- “경로 및 거리 확인” 후 추천 해제(포커스 해제) 시 마커는 복원되지만,
  이후 줌/드래그에서 `kakao.js:127 Uncaught TypeError: Cannot read properties of null (reading 'b')`가 발생
- 포커스 해제 전에는 문제가 없고, 포커스 해제 이후에만 재현

### 원인
- 포커스 상태 전환 시 기존 마커를 `setMap(null)`로 제거했다가 다시 복원하는 흐름이 반복됨
- Kakao Maps 내부 마커 객체가 줌/드래그 중 상태 전환과 충돌하면서 null 참조가 발생
- 특히 포커스 해제 직후 재렌더 타이밍이 줌/드래그 이벤트와 겹칠 때 재현

### 해결
- 포커스 전환 시 마커 제거 대신 `setVisible(false/true)`로 숨김/표시 처리
- 마커 객체를 유지해 클릭 리스너를 보존하고, 내부 참조가 끊기지 않도록 보호
- `setVisible`이 없는 환경을 고려해 fallback으로만 `setMap(null)` 사용

### 해결 과정
- 포커스 모드 전환 시 `setMap(null)` 호출을 제거하고, 숨김 처리로 전환
- 마커 레지스트리를 통해 동일 객체를 재사용하도록 정리
- 포커스 해제 후 줌/드래그 시 에러 재현 여부 확인

### 적용 파일
- `frontend/src/composables/useHomeMap.js`

---

## 문제 4: 홈 추천 스티키 헤더 틈(줌 94%) 발생

### 증상
- 100% 줌에서는 정상이나 94% 줌에서 추천 리스트 스크롤 중 헤더 상단에 1px 틈이 발생
- 스크롤을 멈추면 틈이 사라짐
- 구내식당/취향/인기 추천에서만 발생하고 예산 추천은 상대적으로 영향이 적음

### 원인
- 스크롤 컨테이너 내부의 `position: sticky` 헤더가 줌 비율에서 서브픽셀 라운딩을 일으킴
- 헤더가 리스트 흐름 안에 있어서 스크롤 중 layout round-off가 반복 발생

### 해결
- 스티키 헤더를 스크롤 컨테이너 밖으로 분리해 상단 고정 영역에서 렌더링
- 리스트 내부 헤더는 렌더링하지 않도록 `hideHeaders` 플래그로 비활성화

### 해결 과정
- 스티키 헤더의 pseudo-element/보더/섀도우로 틈 덮기 시도 → 줌 94%에서 지속 발생
- 스크롤 컨테이너 바깥으로 헤더를 분리하는 방식으로 구조 변경 후 해결

### 적용 파일
- `frontend/src/views/HomeView.vue`
- `frontend/src/components/ui/HomeRecommendationContent.vue`
- `frontend/src/components/ui/CafeteriaRecommendationSection.vue`
- `frontend/src/components/ui/TrendingRecommendationSection.vue`

---

## 문제 5: 개발자 모드(디바이스 툴바)에서 지도 드래그 불가

### 증상
- 일반 브라우저에서는 마우스 스크롤/드래그로 지도 이동 가능
- Chrome DevTools 디바이스 모드에서는 줌은 되지만 드래그 이동이 되지 않음

### 원인
- 디바이스 모드에서는 입력이 터치 이벤트로 에뮬레이트됨
- 페이지 스크롤이 터치 드래그를 우선 처리해 지도 드래그가 차단됨

### 해결
- 터치 드래그를 지도에 전달하기 위해 `touch-action: none` 적용
- 데스크톱 드래그를 유지하기 위해 터치 가능 여부(`maxTouchPoints`, media query)로 조건 적용
- DevTools 디바이스 모드 전환은 새로고침 시점 기준으로 반영
- 줌 동작은 기존처럼 유지(휠 줌)

### 적용 파일
- `frontend/src/views/HomeView.vue`
- `frontend/src/composables/useHomeMap.js`
