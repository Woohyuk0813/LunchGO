# 구내식당 대체 추천 트러블슈팅 기록

## 문제 1: OCR 결과 날짜/요일 분리 실패

### 증상
- OCR 응답에서 `days[].menus`가 비어 있고, `unassignedMenus`에만 전체 메뉴가 들어감.
- 영문 요일(MON/TUE...)과 날짜 라인(1월 20일 (월))을 기존 로직이 인식하지 못함.

### 원인
- 요일 분리 정규식이 한글 요일(월~금) 시작 라인만 처리함.

### 해결
- 영문 요일(`MON~FRI`), 괄호 요일(`(월)`), 날짜 라인 필터링 로직 추가.
- 요일 인식 실패 시에도 주중(월~금)으로 분배하는 fallback 추가.

### 해결 과정
- OCR 원문에서 `MON/TUE`가 분리된 개별 라인으로 들어오는 점을 확인.
- 기존 정규식이 한글 요일만 처리한다는 점을 확인하고 패턴을 확장.
- 날짜 라인(`1월`, `20일`)이 메뉴로 섞이는 문제를 재현 후 필터링 추가.
- 요일이 하나로 몰리는 케이스를 대비해 주중 분배 로직을 추가하고 재테스트.

### 적용 파일
- `src/main/java/com/example/LunchGo/cafeteria/service/CafeteriaMenuService.java`

---

## 문제 2: 모달 길이가 길어 버튼 클릭 불가

### 증상
- 메뉴 목록이 길어지면 모달 하단 버튼이 화면 밖으로 밀려 클릭 불가.

### 해결
- 모달 컨테이너에 `max-height` 설정
- 본문 영역에 `overflow-y-auto` 적용
- 버튼 영역은 고정으로 유지

### 해결 과정
- OCR 결과가 길어질수록 버튼 영역이 화면 밖으로 밀리는 문제를 재현.
- 모달을 상단/본문/하단 구조로 나누고, 본문만 스크롤되게 변경.
- 버튼 영역이 항상 보이는지 스크롤 테스트로 확인.

### 적용 파일
- `frontend/src/components/ui/CafeteriaMenuUploadModal.vue`

---

## 문제 3: 구내식당 메뉴 수정 시 이미지 미표시

### 증상
- 메뉴 수정으로 모달을 열어도 기존에 업로드한 구내식당 이미지가 보이지 않음.

### 원인
- 모달이 신규 파일 미리보기만 표시하고, 저장된 이미지 URL을 받지 못함.

### 해결
- `initialImageUrl` 프롭 추가 및 모달에서 기존 이미지 표시.
- 수정 버튼 클릭 시 기존 주간 메뉴 조회 후 모달 오픈.

### 해결 과정
- 기존 미리보기는 `ObjectURL`만 참조하는 구조라 저장된 URL이 반영되지 않는 것을 확인.
- 모달에 `initialImageUrl`을 추가하고, 파일 미선택 시 이를 표시하도록 변경.
- 수정 진입 시 주간 메뉴를 읽어 `imageUrl`과 `days`를 주입한 뒤 모달을 오픈.

### 적용 파일
- `frontend/src/components/ui/CafeteriaMenuUploadModal.vue`
- `frontend/src/components/ui/CafeteriaRecommendationSection.vue`
- `frontend/src/composables/useCafeteriaRecommendation.js`
- `frontend/src/views/HomeView.vue`

---

## 문제 4: avoidMenu가 전체 메뉴로 표시됨

### 증상
- `avoidMenu`가 요일별 전체 메뉴로 노출됨.

### 원인
- avoidMenu 생성 로직이 기피 키워드 매칭 없이 전체 메뉴를 조인함.

### 해결
- 기피 키워드와 매칭된 메뉴만 필터링하여 표시.
- 매칭이 없으면 `-` 출력.

### 해결 과정
- `avoidMenu` 생성 시 전체 메뉴를 그대로 조인하는 부분을 확인.
- 기피 키워드 목록과 메뉴 문자열을 비교하도록 필터 조건을 추가.
- 매칭된 항목만 조인되며, 매칭이 없으면 `-`로 표시되는지 확인.

### 적용 파일
- `src/main/java/com/example/LunchGo/cafeteria/service/CafeteriaRecommendationService.java`

---

## 문제 4-1: 기피 재료가 요리명에 포함되지 않아 매칭 누락

### 증상
- `돼지고기`를 기피로 설정했는데 `제육/돈가스` 같은 메뉴가 `avoidMenu`에 잡히지 않음.

### 원인
- 문자열 매칭만 사용하면 요리명과 재료 키워드가 직접 일치하지 않음.

### 해결
- 메뉴 키워드를 재료 키워드로 매핑하는 `MENU_INGREDIENT_MAP` 사전 추가.
- `containsAnyKeyword()`에서 직접 매칭 + 매핑 매칭을 모두 검사.

### 해결 과정
- 기피 키워드 목록과 실제 OCR 메뉴 텍스트를 비교하며 누락 케이스 확인.
- 메뉴 키워드 → 재료 키워드 사전을 설계하고 서비스 로직에 반영.
- 돼지고기/소고기/계란/해물/유제품 등 매핑 확장 후 재테스트.

### 적용 파일
- `src/main/java/com/example/LunchGo/cafeteria/service/CafeteriaRecommendationService.java`

---

## 문제 5: avoidMenu가 항상 '-'로 표시됨

### 증상
- 기피 키워드를 추가했는데도 `avoidMenu`가 항상 `-`로 표시됨.

### 원인
- `MemberMapper.selectUser`가 `ACTIVE` 사용자만 조회하여 `DORMANT` 사용자(테스트용 userId=2)의 기피 키워드를 불러오지 못함.

### 해결
- 테스트 환경에서는 userId=2를 `ACTIVE` 상태로 변경 후 재테스트.
- 별도 status 무시 쿼리 추가는 정책상 적용하지 않음.

### 해결 과정
- `speciality_mappings`는 정상인데 API 결과가 `-`로 나오는 현상을 재현.
- `MemberMapper.selectUser`가 `ACTIVE`만 조회한다는 조건을 확인.
- 테스트 계정 `user_id=2`가 `DORMANT`임을 확인 후 `ACTIVE`로 변경.
- 재호출 시 `avoidMenu`가 정상적으로 매칭되는지 확인.

### 적용 파일
- `src/main/resources/sql/insert_data_member_info.sql`

---

## 문제 6: HomeView에서 리스트 미노출

### 증상
- 홈 화면에서 기본 식당 리스트 및 인기순(트렌딩) 리스트가 보이지 않음.

### 원인
- 템플릿 조건이 뒤집혀 `cafeteriaRecommendations.length`가 없을 때 리스트를 숨김.

### 해결
- 조건을 `v-if="!cafeteriaRecommendations.length"`로 수정.

### 해결 과정
- 홈 리스트가 렌더링되지 않는 상황을 재현.
- 템플릿 조건이 `v-else`로 뒤집혀 있는 것을 확인.
- 조건 수정 후 기본 리스트와 트렌딩이 정상 노출되는지 확인.

### 적용 파일
- `frontend/src/views/HomeView.vue`

---

## 문제 7: Vue warn - instance undefined

### 증상
- `cafeteriaImageUrl`이 템플릿에서 정의되지 않았다는 경고 발생.

### 원인
- composable에서 받아온 ref를 템플릿에 직접 바인딩할 때 Vue가 제대로 인식하지 못함.

### 해결
- computed로 래핑해 템플릿 바인딩 안정화.

### 해결 과정
- 콘솔 경고가 `instance undefined`로 나오는 부분을 확인.
- composable ref가 템플릿에서 직접 참조되는 케이스를 분리.
- `computed`로 감싸서 템플릿 노출을 명시하고 경고가 사라지는지 확인.

### 적용 파일
- `frontend/src/views/HomeView.vue`

---

## 문제 8: 금요일 12시 이후 추천이 비어 보임

### 증상
- 금요일 12시(KST) 이후 추천 요청 시 `recommendations`가 비어 있음.
- 주간 메뉴 조회도 현재 주(금요일 포함) 기준으로만 조회되어 빈 배열 반환.

### 원인
- 주간 기준 계산이 "요청 날짜 주"에 고정되어 있어, 금요일 오후 이후에도 같은 주를 조회.
- 실제 운영 정책은 **금요일 12시 이후 다음 주 기준**으로 전환해야 함.

### 해결
- KST 기준으로 금요일 12시 이후면 `baseDate`를 다음 주로 보정하는 로직 추가.
- `weekStart()` 계산 전에 기준 날짜를 보정하도록 적용.

### 적용 파일
- `src/main/java/com/example/LunchGo/cafeteria/service/CafeteriaMenuService.java`

---

## 문제 9: 마이페이지 저장 후 홈 추천이 즉시 갱신되지 않음

### 증상
- 마이페이지에서 기피 키워드/특이사항 저장 후 홈으로 이동해도 추천이 이전 상태로 보임.

### 원인
- 홈 진입 시점에 추천 재호출이 지연되거나 조건상 실행되지 않음.

### 해결
- 마이페이지 저장 성공 직후 추천을 미리 호출해 `sessionStorage(cafeteriaRecommendations)`에 저장.
- 홈 화면에서 즉시 캐시를 읽어 최신 추천이 표시되도록 처리.

### 적용 파일
- `frontend/src/views/mypage/UserMyPage.vue`
