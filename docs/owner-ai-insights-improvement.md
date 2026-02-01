# 사업자 AI 인사이트 개선 설계

## 목표
기존 주간 PDF 인사이트에 "금주 방문 추이 예측" 섹션을 추가한다. 예측은 금주 구내식당 메뉴, 사용자 취향, 즐겨찾기 공유, 회사 정보 데이터를 활용하며 AI 응답 실패 시 규칙 기반으로 대체한다.

## 현재 엔드포인트 파악
`GET /api/business/restaurants/{id}/stats/weekly.pdf`

### 처리 흐름
1. `BusinessRestaurantController`에서 소유자 검증 후 서비스 호출.
2. `RestaurantStatsService.generateWeeklyStatsPdf`에서 최근 7일 예약 + 일별 통계 조회.
3. `AiChatService`로 주간 요약 프롬프트 생성 후 AI 응답 수신.
4. AI 요약을 PDF로 렌더링하여 다운로드 응답 반환.

### 현재 입력 데이터
- 예약 리스트: `BusinessReservationQueryService.getList(restaurantId)`
- 일별 통계: `DailyRestaurantStatsRepository.findLast7DaysByRestaurantId`

### 현재 출력 구성
AI 요약 섹션: `## 핵심 요약`, `## 상세 분석`, `## 통합 분석 및 추천`

## 개선 목표 범위
- 기존 PDF 요약에 "금주 방문 추이 예측" 섹션을 추가.
- 예측 데이터 근거를 PDF 내 요약으로 설명.
- AI 장애 시 규칙 기반 예측 결과 제공.

## 데이터 소스 매핑
| 데이터 | 테이블/스키마 |
| --- | --- |
| 금주 구내식당 메뉴 | `cafeteria_menus` |
| 사용자 취향 | `specialities`, `speciality_mappings` |
| 즐겨찾기 공유 | `bookmarks`, `bookmark_links` |
| 회사 정보 | `users.company_name`, `users.company_address` |
| 방문/예약 히스토리 | `reservations`, `restaurant_reservation_slots`, `reservation_visit_stats` |

## 단계별 설계

### 1) 요구 정의 및 출력 포맷 확정
- PDF에 `## 금주 방문 예측` 섹션 추가.
- 예측 결과 형식: 요일별 예상 방문 범위 + 핵심 근거 2~3줄.
- 프론트 다운로드 플로우는 기존 엔드포인트 유지.

### 2) 데이터 수집 레이어 확장
- `RestaurantStatsService`에 금주 예측용 데이터 조회 추가.
- 신규 Repository/Mapper 추가:
  - `cafeteria_menus` (금주 범위, user_id 기준)
  - `speciality_mappings` + `specialities` (사용자 취향)
  - `bookmarks`, `bookmark_links` (공유 네트워크 및 공개 북마크)
  - `reservations`, `restaurant_reservation_slots`, `reservation_visit_stats` (과거 예약 추이)

### 3) 피처(지표) 계산
- 기본 수요: 최근 8~12주 요일별 예약 평균 + 변동성.
- 구내식당 메뉴 시그널:
  - `cafeteria_menus.main_menu_names`에서 키워드 추출.
  - 사용자 취향 키워드와 매칭 비율 산출.
- 공유 네트워크 시그널:
  - `bookmark_links` 승인 관계 + 공개 북마크 확산 지수.
  - 네트워크 밀도에 따라 가중치 부여.
- 회사 그룹 시그널:
  - `users.company_name` 기준 동일 회사 방문 비율 추정.

### 4) AI 프롬프트 확장
- 기존 주간 통계 프롬프트에 예측 지표를 추가.
- 요구 섹션에 `## 금주 방문 예측`을 명시.
- 입력 데이터는 요약 지표만 전달 (원시 데이터 최소화).

### 5) 규칙 기반 fallback 설계
- AI 실패/타임아웃 시 아래 로직으로 예측치 생성:
  - 기본 수요(주간 평균) * (메뉴 매칭 지수) * (공유 네트워크 가중치).
  - 가중치는 0.9~1.1 범위로 제한하여 과대 예측 방지.
  - 결과에 "규칙 기반 추정" 문구 포함.

### 6) PDF 출력 구성 개선
- PDF에 예측 섹션 추가:
  - 요일별 예상 방문 범위 (예: 월 18~24명)
  - 주요 근거 (메뉴 매칭 상위 키워드, 공유 증가율)
  - 신뢰도(LOW/MEDIUM/HIGH)

### 7) 검증 및 운영 지표
- A/B 또는 기간 전후 비교로 예측 적중률 체크.
- 데이터 공백(메뉴 미업로드, 공유 없음) 시 fallback 동작 확인.
- PDF 생성 시간 및 AI 호출 실패율 모니터링.

## 개발 체크리스트
- [x] 데이터 수집 쿼리/리포지토리 추가
- [x] 예측 지표 계산 로직 구현
- [x] 프롬프트에 예측 섹션 포함
- [x] fallback 메시지 생성
- [x] PDF 렌더링 섹션 추가
- [x] 로그/모니터링 지표 추가

## 리스크 및 대응
- 메뉴 데이터 누락: 기본 수요만으로 예측 (가중치 1.0).
- 사용자 취향 적재 부족: 회사/예약 기반 가중치 중심으로 보정.
- 공유 네트워크 희소: 공유 가중치 무시.
- AI 오류: 즉시 규칙 기반으로 전환하고 문구 명시.

## 작업 계획 (쿼리/DTO/서비스)

### 1) 쿼리 설계
- 예약 기반 수요 베이스라인
  - 최근 8~12주 요일별 예약 수 집계 (restaurant_id 기준)
  - `reservations` + `restaurant_reservation_slots` 조인
  - 출력: `day_of_week`, `avg_count`, `stddev` (가능하면)
- 구내식당 메뉴 데이터
  - `cafeteria_menus`에서 금주(월~금) 데이터 조회
  - `user_id`, `served_date`, `main_menu_names` (json)
- 사용자 취향 데이터
  - `speciality_mappings` + `specialities`로 user_id별 취향 키워드
  - 출력: `user_id`, `keyword`, `is_liked`
- 즐겨찾기 공유 신호
  - `bookmark_links` (승인 상태)로 네트워크 연결 수
  - `bookmarks`에서 restaurant_id 기준 공개 북마크 수
- 회사 정보
  - `users`에서 `company_name`, `company_address` 추출
  - 같은 회사 사용자군 비중 계산용

### 2) DTO 설계
- `WeeklyDemandSignalResponse`
  - `baselineByWeekday` (요일별 평균/변동성)
  - `cafeteriaMenuSignals` (메뉴 키워드, 날짜)
  - `preferenceSignals` (키워드 매칭율)
  - `shareSignals` (공유 네트워크 지수)
  - `companySignals` (회사별 방문 비중)
- `WeeklyDemandPrediction`
  - `weekday`, `expectedMin`, `expectedMax`
  - `confidence` (LOW/MEDIUM/HIGH)
  - `evidence` (문장 리스트)

### 3) 서비스 분해
- `RestaurantStatsService`
  - 기존 주간 요약 + 예측 결과를 함께 생성
  - `buildPrompt`에 예측 지표 요약 추가
  - AI 실패 시 `RuleBasedForecastService` 호출
- `WeeklyDemandSignalService`
  - 데이터 수집/집계 전담
  - 쿼리 실행 후 `WeeklyDemandSignalResponse` 구성
- `RuleBasedForecastService`
  - 베이스라인 * (메뉴 매칭) * (공유 가중치) * (회사 가중치)
  - 가중치 범위 제한 및 증거 문구 생성

### 4) PDF 렌더링 변경
- `buildPdf`에 `## 금주 방문 예측` 섹션 추가
- 예측 테이블(요일/최소/최대) + 근거 bullet 렌더

### 5) 로깅/모니터링
- AI 요청 성공/실패 메트릭 추가
- fallback 사용 여부 로깅 (restaurant_id, week_range)

---

## 구현 내용 업데이트

### 1. 하드코딩된 날짜 로직 제거

#### 배경
기존 `src/main/java/com/example/LunchGo/restaurant/service/RestaurantStatsService.java` 파일에는 테스트 또는 디버깅 목적으로 삽입된 `LocalDate.of(2026, 1, 10)`과 같은 하드코딩된 날짜 로직이 존재했습니다. 이러한 코드는 프로덕션 환경에서 실제 날짜 기반의 로직을 방해하고, 잘못된 데이터 조회 및 심각한 오작동을 유발할 수 있는 잠재적인 위험을 가지고 있었습니다.

#### 변경 내용
`RestaurantStatsService.java` 파일 내 `getWeeklyStatsInsight` 메서드에서 특정 날짜(`2026-01-10`)를 참조하여 추가 조회를 시도하던 코드 블록을 완전히 제거했습니다. 이를 통해 서비스가 항상 현재 날짜를 기준으로 정상적인 주간 통계 및 예측 데이터를 처리하도록 보장합니다.

### 2. `getWeeklyStatsInsight` 메서드 리팩토링

#### 배경
`RestaurantStatsService` 내 `getWeeklyStatsInsight` 메서드는 주간 AI 인사이트 데이터를 조회하는 핵심 로직을 담당했지만, 캐싱 처리, 데이터 조회, AI 요약 생성, 예측 데이터 생성 및 저장 등 너무 많은 책임을 한 메서드 내에서 처리하고 있었습니다. 이는 코드의 가독성을 저해하고 유지보수를 어렵게 만들었으며, 테스트 용이성 또한 낮추는 요인이었습니다.

#### 목표
메서드를 기능별로 더 작은 단위로 분리하여 각 메서드가 단일 책임을 갖도록(Single Responsibility Principle) 개선하고, 코드의 응집도를 높여 가독성, 유지보수성, 테스트 용이성을 향상시킵니다.

#### 변경 내용 및 구조
`getWeeklyStatsInsight` 메서드는 다음 네 가지 메서드로 분할 및 재구성되었습니다.

1.  **`public WeeklyAiInsightsResponse getWeeklyStatsInsight(Long restaurantId)`**
    *   **역할:** 이 메서드는 주간 AI 인사이트 조회 API의 주요 진입점 역할을 합니다. 주로 캐싱 로직을 관리하며, Redis 캐시에 데이터가 있는지 확인하고, 없을 경우 새로운 인사이트를 생성하도록 위임합니다.
    *   **주요 로직:**
        *   현재 주(Week)의 시작일을 기준으로 캐시 키를 생성합니다.
        *   `getCachedInsights`를 호출하여 캐시된 데이터가 있는지 확인합니다.
        *   캐시 적중 시, 캐시된 `WeeklyAiInsightsResponse`를 즉시 반환합니다.
        *   캐시 미스 시, `generateAndCacheInsights`를 호출하여 새로운 인사이트를 생성하고 캐싱한 후 반환합니다.

    ```java
    // RestaurantStatsService.java
    public WeeklyAiInsightsResponse getWeeklyStatsInsight(Long restaurantId) {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        String cacheKey = REDIS_CACHE_KEY_PREFIX + restaurantId + ":" + weekStart.toString();

        // 1. 캐시에서 데이터 조회 시도
        Optional<WeeklyAiInsightsResponse> cachedInsights = getCachedInsights(cacheKey, restaurantId, weekStart);
        if (cachedInsights.isPresent()) {
            return cachedInsights.get();
        }

        // 2. 캐시가 없으면 새로 생성하고 캐시에 저장
        return generateAndCacheInsights(restaurantId, weekStart, cacheKey);
    }
    ```

2.  **`private Optional<WeeklyAiInsightsResponse> getCachedInsights(String cacheKey, Long restaurantId, LocalDate weekStart)`**
    *   **역할:** Redis 캐시에서 AI 인사이트 데이터를 조회하고, 캐시된 데이터가 유효한 경우 반환합니다. 특히, 캐시 데이터가 있더라도 예측 데이터는 최신성을 위해 DB에서 다시 조회하여 기존 캐시 데이터와 병합하는 로직을 포함합니다.
    *   **주요 로직:**
        *   제공된 `cacheKey`로 Redis에서 데이터를 조회합니다.
        *   데이터가 없거나 역직렬화에 실패하면 빈 `Optional`을 반환합니다.
        *   캐시된 데이터가 있으면 이를 `WeeklyAiInsightsResponse` 객체로 역직렬화합니다.
        *   `getOrGenerateWeeklyPredictions`를 호출하여 DB에 저장된 최신 예측 데이터를 가져오고, 이를 캐시된 인사이트와 결합하여 최종 응답 객체를 재구성합니다.

    ```java
    // RestaurantStatsService.java
    private Optional<WeeklyAiInsightsResponse> getCachedInsights(String cacheKey, Long restaurantId, LocalDate weekStart) {
        String cachedData = redisUtil.getData(cacheKey);
        if (cachedData == null || cachedData.isBlank()) {
            return Optional.empty();
        }

        try {
            WeeklyAiInsightsResponse cached = objectMapper.readValue(cachedData, WeeklyAiInsightsResponse.class);
            // 캐시 데이터가 있어도 예측 데이터는 DB에서 최신으로 조회
            List<WeeklyDemandPrediction> predictions = getOrGenerateWeeklyPredictions(restaurantId, weekStart, null, true);

            // 캐시된 예측 데이터와 DB의 예측 데이터를 조합
            List<WeeklyDemandPrediction> finalPredictions = !predictions.isEmpty() ? predictions : cached.getPredictions();

            List<WeeklyAiInsightsResponse.WeeklyPredictionPoint> lastWeekPredictions = getLastWeekPredictions(restaurantId, weekStart);

            return Optional.of(WeeklyAiInsightsResponse.builder()
                .startDate(cached.getStartDate())
                .endDate(cached.getEndDate())
                .predictionWeekStart(cached.getPredictionWeekStart())
                .predictionWeekEnd(cached.getPredictionWeekEnd())
                .aiSummary(cached.getAiSummary())
                .aiFallbackUsed(cached.isAiFallbackUsed())
                .reservations(cached.getReservations())
                .stats(cached.getStats())
                .predictions(finalPredictions)
                .lastWeekPredictions(lastWeekPredictions)
                .signalSummary(cached.getSignalSummary())
                .build());
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize cached AI insights for restaurantId={}", restaurantId, e);
            return Optional.empty();
        }
    }
    ```

3.  **`private WeeklyAiInsightsResponse generateAndCacheInsights(Long restaurantId, LocalDate weekStart, String cacheKey)`**
    *   **역할:** Redis 캐시에 데이터가 없는 경우, 필요한 모든 정보를 수집하여 새로운 주간 AI 인사이트를 생성하고 이를 Redis에 캐시합니다.
    *   **주요 로직:**
        *   현재 주간의 `start`, `end`, `weekEnd` 날짜를 계산합니다.
        *   `businessReservationQueryService`와 `dailyRestaurantStatsRepository`를 통해 예약 및 일별 통계 데이터를 수집합니다.
        *   `weeklyDemandSignalService`를 통해 주간 수요 시그널을 빌드합니다.
        *   `getOrGenerateWeeklyPredictions`를 호출하여 예측 데이터를 생성하거나 조회합니다.
        *   `buildPrompt`를 통해 AI 프롬프트를 구성하고, `aiChatService`를 호출하여 AI 요약을 생성합니다 (실패 시 폴백 요약 사용).
        *   생성된 예측 데이터를 `savePredictionsToDatabase`를 통해 DB에 저장합니다.
        *   수집된 모든 정보를 바탕으로 최종 `WeeklyAiInsightsResponse` 객체를 빌드합니다.
        *   빌드된 응답 객체를 Redis에 직렬화하여 캐시합니다.

    ```java
    // RestaurantStatsService.java
    private WeeklyAiInsightsResponse generateAndCacheInsights(Long restaurantId, LocalDate weekStart, String cacheKey) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        LocalDate weekEnd = weekStart.plusDays(6);

        // 데이터 수집
        List<BusinessReservationItemResponse> weeklyReservations = businessReservationQueryService.getList(restaurantId).stream()
            .filter(item -> isWithinLast7Days(item.getDatetime(), start, end))
            .toList();
        List<DailyRestaurantStats> dailyStats = dailyRestaurantStatsRepository.findLast7DaysByRestaurantId(restaurantId);
        WeeklyDemandSignalResponse signal = weeklyDemandSignalService.buildWeeklySignal(restaurantId, weekStart, weekEnd);

        // 예측 데이터 생성 또는 조회
        List<WeeklyDemandPrediction> predictions = getOrGenerateWeeklyPredictions(restaurantId, weekStart, signal, false);

        // AI 요약 생성
        String prompt = buildPrompt(restaurantId, start, end, weekStart, weekEnd, weeklyReservations, dailyStats, signal);
        String aiSummary;
        boolean aiFallbackUsed = false;
        try {
            if (forceForecastFallback) {
                aiFallbackUsed = true;
                aiSummary = buildFallbackSummary();
            } else {
                aiSummary = aiChatService.chat(prompt);
            }
        } catch (RuntimeException e) {
            log.warn("AI summary failed, fallback enabled for restaurantId={}", restaurantId, e);
            aiFallbackUsed = true;
            aiSummary = buildFallbackSummary();
        }

        // 예측 데이터를 DB에 저장
        savePredictionsToDatabase(restaurantId, weekStart, predictions);

        // 최종 응답 객체 생성
        WeeklyAiInsightsResponse response = WeeklyAiInsightsResponse.builder()
            .startDate(start.toString())
            .endDate(end.toString())
            .predictionWeekStart(weekStart.toString())
            .predictionWeekEnd(weekEnd.toString())
            .aiSummary(aiSummary)
            .aiFallbackUsed(aiFallbackUsed)
            .reservations(buildDailyReservationPoints(weeklyReservations))
            .stats(buildDailyStatsPoints(dailyStats))
            .predictions(predictions)
            .lastWeekPredictions(getLastWeekPredictions(restaurantId, weekStart))
            .signalSummary(buildSignalSummaryPayload(signal, weekStart, weekEnd))
            .build();

        // Redis에 캐싱
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            redisUtil.setDataExpire(cacheKey, responseJson, CACHE_TTL_MILLIS);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize AI insights for caching, restaurantId={}", restaurantId, e);
        }

        return response;
    }
    ```

4.  **`private List<WeeklyDemandPrediction> getOrGenerateWeeklyPredictions(Long restaurantId, LocalDate weekStart, WeeklyDemandSignalResponse signal, boolean fromCache)`**
    *   **역할:** DB에서 주간 예측 데이터를 조회하고, 만약 데이터가 없을 경우 `RuleBasedForecastService`를 사용하여 새로운 예측 데이터를 생성합니다. 이 메서드는 `fromCache` 플래그를 통해 호출 맥락에 따라 다르게 동작할 수 있습니다.
    *   **주요 로직:**
        *   `weeklyPredictionRepository`를 통해 DB에서 `restaurantId`와 `weekStart`에 해당하는 예측 데이터를 조회합니다.
        *   DB에 예측 데이터가 존재하면 해당 데이터를 `WeeklyDemandPrediction` 리스트로 변환하여 반환합니다.
        *   DB에 예측 데이터가 없고, `fromCache`가 `true`(즉, 캐시 조회 과정에서 호출됨)인 경우, 빈 리스트를 반환하여 캐시된 예측 데이터를 사용하도록 합니다.
        *   DB에 예측 데이터가 없고, `fromCache`가 `false`(즉, 새로운 인사이트 생성 과정에서 호출됨)인 경우, `ruleBasedForecastService.forecast`를 호출하여 새로운 예측 데이터를 생성하고 이를 반환합니다.

    ```java
    // RestaurantStatsService.java
    private List<WeeklyDemandPrediction> getOrGenerateWeeklyPredictions(Long restaurantId, LocalDate weekStart, WeeklyDemandSignalResponse signal, boolean fromCache) {
        List<WeeklyPrediction> savedPredictions = weeklyPredictionRepository.findByRestaurantIdAndWeekStartDate(restaurantId, weekStart);

        if (!savedPredictions.isEmpty()) {
            log.info("DB에서 현재 주 예측 데이터 사용: restaurantId={}, weekStart={}, count={}",
                restaurantId, weekStart, savedPredictions.size());
            return savedPredictions.stream()
                .map(pred -> WeeklyDemandPrediction.builder()
                    .weekday(pred.getId().getWeekday())
                    .expectedMin(pred.getExpectedMin())
                    .expectedMax(pred.getExpectedMax())
                    .confidence(pred.getConfidence())
                    .evidence(parseEvidenceJson(pred.getEvidence()))
                    .build())
                .toList();
        }

        if (fromCache) {
            // 캐시 로직에서는 DB에 데이터가 없으면 빈 리스트를 반환하여 캐시된 예측을 사용하도록 함
            return Collections.emptyList();
        }

        log.info("DB에 예측 데이터 없음, 새로 생성: restaurantId={}, weekStart={}", restaurantId, weekStart);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<WeeklyDemandPrediction> newPredictions = ruleBasedForecastService.forecast(signal, weekStart, weekEnd);
        log.info("새로 생성된 예측 데이터 개수: {}", newPredictions.size());
        return newPredictions;
    }
    ```

### 3. 프론트엔드 디버그용 `console.log` 주석 처리

#### 배경
`frontend/src/views/business/insights/BusinessAiInsightsPage.vue` 파일의 `loadInsights` 메서드에는 개발 및 디버깅 과정에서 사용된 여러 `console.log` 문들이 포함되어 있었습니다. 이러한 디버그용 로그는 프로덕션 환경에서 불필요한 정보를 브라우저 콘솔에 출력하여 성능에 미미한 영향을 주거나 보안상 민감한 정보를 노출할 가능성이 있었습니다. 또한, 코드를 지저분하게 만들고 실제 오류 메시지 파악을 어렵게 합니다.

#### 변경 내용
`BusinessAiInsightsPage.vue` 파일 내 `loadInsights` 메서드에 있던 모든 `console.log` 문들을 주석 처리했습니다. 이를 통해 프로덕션 환경 배포 시 불필요한 콘솔 출력을 방지하고 코드 베이스를 깔끔하게 유지합니다. 필요한 경우 개발 환경에서만 다시 주석을 해제하여 디버깅에 활용할 수 있습니다.

#### 주석 처리된 코드 예시 (주요 부분 발췌)

```javascript
// frontend/src/views/business/insights/BusinessAiInsightsPage.vue
const loadInsights = async () => {
  // ... (기존 로직) ...
  try {
    const response = await httpRequest.get(
      `/api/business/restaurants/${rid}/stats/weekly`
    );
    insight.value = response.data;

    // [MEDIUM] 디버깅 목적으로 사용된 console.log 문입니다.
    // 프로덕션 코드에 포함되지 않도록 병합 전에 제거하는 것이 좋습니다.
    // 브라우저 콘솔에 불필요한 로그가 출력되는 것을 방지하고 코드를 깔끔하게 유지할 수 있습니다.
    // console.log("=== AI 인사이트 데이터 디버깅 ===");
    // console.log("예측 주 시작일:", insight.value?.predictionWeekStart);
    // console.log("예측 주 종료일:", insight.value?.predictionWeekEnd);
    // console.log("예측 데이터 개수:", insight.value?.predictions?.length || 0);
    // console.log("예측 데이터 상세:", insight.value?.predictions);

    // // 각 예측 데이터의 날짜 확인
    // if (insight.value?.predictions && insight.value?.predictionWeekStart) {
    //   const weekStart = new Date(insight.value.predictionWeekStart);
    //   console.log("예측 주 시작일 (Date 객체):", weekStart);
    //   insight.value.predictions.forEach((pred) => {
    //     const mondayIndex = 2;
    //     const offset = pred.weekday === 1 ? 6 : pred.weekday - mondayIndex;
    //     const date = new Date(weekStart);
    //     date.setDate(date.getDate() + offset);
    //     const dateStr = `${date.getFullYear()}-${String(
    //       date.getMonth() + 1
    //     ).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
    //     console.log(
    //       `요일 ${pred.weekday} (${formatWeekday(
    //         pred.weekday
    //       )}): ${dateStr}, 예측: ${pred.expectedMin}~${pred.expectedMax}`
    //     );
    //   });
    // }
    // console.log("================================");
  } catch (error) {
    // ... (에러 처리 로직) ...
  } finally {
    // ... (finally 로직) ...
  }
};
```