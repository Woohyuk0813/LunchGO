# 사업자 AI 인사이트 프롬프트 개선 계획

## 목표

- 주간 AI 요약을 “액션 중심”으로 재구성해 점주가 바로 실행할 수 있게 한다.
- 프론트에서 안정적으로 파싱 가능한 고정 포맷을 강제한다.

## 현행 구조 요약

- 백엔드에서 `RestaurantStatsService`가 AI 프롬프트 생성 → 요약 반환.
- 프론트는 `##` 섹션 헤더를 기준으로 파싱 (`BusinessAiInsightsPage.vue`의 `summarySections`).
- 현재는 “통합 분석 및 추천”이 장문 설명 중심이라 실행 지시가 약함.

## 개선 포인트 (프롬프트 규칙)

1. 섹션 고정 포맷

   - 반드시 아래 섹션만 출력:
     - `## 핵심 요약` (3줄)
     - `## 통합 분석 및 추천` (3~5개)
     - `## 이번 주 TODO` (필수/선택 각 3개 이내)
     - `## 데이터 격차` (필요 데이터 및 이유)

2. 추천 항목 템플릿 강제

   - 형식: `근거 → 기대효과 → 우선순위(High/Med/Low + 이모지) → 실행`
   - 각 항목 1~2문장, 구체적 지표 포함.

3. TODO 체크리스트 구조 고정

   - `필수:` / `선택:` 라벨 사용.
   - 항목은 동사로 시작(예: “발송”, “준비”, “강조”).

4. 불릿/이모지 가독성 규칙

   - 모든 항목은 `- `로 시작하고, 바로 뒤에 관련 이모지 1개를 붙인다.

5. 주간 구분 + 날짜 명시

   - 지난주 실측 / 이번주 예측을 문장에 명시.
   - 요일 언급 시 반드시 `요일(YYYY-MM-DD)` 형식으로 병기.
   - 예: “목요일(2026-01-15) 예약/매출 상승”.

6. 데이터 결측 처리

   - 부족한 데이터는 “데이터 격차” 섹션에 명시.
   - “필요 데이터 / 이유 / 기대효과”까지 작성.

7. 근거 지표 사용 규칙

   - 전환율, 예약/방문, 매출, mismatch/no-menu, keyword overlap, 공유/북마크 중 최소 2개 이상 사용.
   - 수치가 없으면 “데이터 부족” 처리.

8. 우선순위 기준 예시

   - 전환율 < 70% 또는 예약/방문 하락: High
   - 불일치/미운영일 2일 이상: Med
   - 개선 효과가 제한적이면: Low

9. 길이/형식 제한
   - 총 글자 수 제한(예: 1200자 내).
   - 불릿은 `- `로만 출력.

## 단계별 구현 계획

### 1단계: 프롬프트 템플릿 정리 (백엔드)

- 대상: `RestaurantStatsService`의 프롬프트 생성 로직.
- 작업:
  - 섹션 헤더를 고정하고 불릿 형식을 `- `로 통일.
  - 추천/ TODO 템플릿을 문장 규칙으로 강제.
  - 수치가 없을 경우 “데이터 부족” 응답 강제.
- 기대효과: 프론트 파싱 안정성 + 실행 가능성 향상.

### 2단계: 프론트 파서 검증/보강

- 대상: `BusinessAiInsightsPage.vue`의 `summarySections`/`cleanSummaryBody`.
- 작업:
  - “## 이번 주 TODO” 섹션이 들어와도 누락되지 않게 파서 확인.
  - `## 데이터 격차` 섹션 렌더링 추가 여부 결정.
- 기대효과: 고정 포맷이 깨져도 UI 안정 유지.

### 3단계: 행동형 문장/우선순위 가이드 추가

- 대상: 프롬프트 본문 규칙.
- 작업:
  - 우선순위 기준 예시 삽입(전환율 < 70% 또는 예약/방문 하락 → High, 불일치/미운영일 2일 이상 → Med, 개선 효과 제한 → Low).
  - “동사 시작” 규칙을 명확히 안내.
- 기대효과: TODO 품질 및 일관성 개선.

### 4단계: 신호 필드 의무 반영

- 대상: 프롬프트 입력과 출력 검증.
- 작업:
  - 값이 있는 신호(0/없음 제외) 중 1개 이상 반드시 사용하도록 규칙 추가.
  - 모든 신호가 0/없음이면 `## 데이터 격차`에 "신호 데이터 부족" 명시.
- 기대효과: 데이터 기반 설명 강화.

### 5단계: QA 및 예시 확인

- 대상: 테스트 데이터로 주간 보고서 생성.
- 작업:
  - 정상/결측/편차 큰 케이스 각각 1회 이상 검증.
  - 프론트에서 섹션별 표시가 의도대로 되는지 확인.

## 샘플 출력 예시 (요약형)

```
## 핵심 요약
- 📊 지난주 방문 전환율 58%로 보통 수준, 예약→방문 이탈이 존재합니다.
- 📈 목요일(2026-01-15) 예약/매출 상승으로 주간 최고 성과가 확인됩니다.
- ⚠️ 구내식당 불일치일이 있는 주간은 외식 수요 기회로 보입니다.

## 통합 분석 및 추천
- 📌 근거: 전환율 58% → 기대효과: 방문율 5~10% 개선 → 우선순위: High 🔴 → 실행: 예약 확정 고객 리마인드 메시지 발송
- 🍽️ 근거: 불일치일 다수 → 기대효과: 예약 증가 → 우선순위: Med 🟡 → 실행: 해당 날짜 프로모션 강조

## 이번 주 TODO
- ✅ 필수: 리마인드 메시지 템플릿 업데이트 (전환율 58% 개선 목적)
- ✍️ 선택: 불일치일 맞춤 프로모션 문구 준비

## 데이터 격차
- ℹ️ 리뷰 노출 클릭률 데이터 없음 → 신뢰도 높은 후기 운영 전략 수립 불가 → 다음 주 수집 필요
```

## 다음 단계 제안

1. 샘플 데이터로 QA 후 운영 반영 (5단계).

## 적용된 변경 사항 (2026-01-17)

### 1) 프롬프트 템플릿 적용

- 적용 위치: `src/main/java/com/example/LunchGo/restaurant/service/RestaurantStatsService.java`
- 변경 요약:
  - 섹션 고정: `## 핵심 요약 / ## 통합 분석 및 추천 / ## 이번 주 TODO / ## 데이터 격차`
  - bullet 규칙 강제: 모든 항목 `- ` 시작 + 이모지 1개 부착
  - 추천 템플릿 강제: `근거 / 기대효과 / 우선순위 / 실행`
  - TODO 템플릿 강제: `필수/선택` 항목
  - 요일 언급 시 날짜 병기 + 요일-날짜 매핑 제공
  - 우선순위 기준 예시 추가
  - 구내식당 미운영일 실행은 “우리 식당 정보 제공/특별 프로모션 기획”으로 제한
  - 신호 요약 의무 반영 규칙 추가(값 있는 신호 1개 이상, 없으면 데이터 격차 명시)
  - 지표 사용 규칙 및 데이터 부족 처리 규칙 추가

#### 코드 스니펫 (buildPrompt)

```java
builder.append("출력 규칙:\n");
builder.append("1) 섹션은 아래 4개만 출력: ## 핵심 요약 / ## 통합 분석 및 추천 / ## 이번 주 TODO / ## 데이터 격차\n");
builder.append("2) 각 섹션의 모든 항목은 반드시 '- ' 로 시작하며, 이어서 관련 이모지 1개를 붙일 것\n");
builder.append("3) ## 핵심 요약: 3줄 고정\n");
builder.append("4) ## 통합 분석 및 추천: 3~5개, 형식은 '근거: ... / 기대효과: ... / 우선순위: High|Med|Low 🔴/🟡/🟢 / 실행: ...'\n");
builder.append("5) ## 이번 주 TODO: '- 필수: ...', '- 선택: ...' 형식으로 각 3개 이내\n");
builder.append("6) ## 데이터 격차: 부족한 데이터와 이유를 작성(없으면 '- 데이터 부족 없음')\n");
builder.append("7) 요일을 언급할 때는 반드시 해당 날짜(YYYY-MM-DD)를 괄호로 병기하되, 일자별 나열은 금지\n");
builder.append("8) 동사로 시작하고 실행 가능한 문장만 작성\n");
builder.append("9) 아래 지표 중 최소 2개 이상을 근거로 사용: 전환율, 예약/방문, 매출, 구내식당 불일치/미운영, 키워드 겹침, 공유/북마크, 상위 회사\n");
builder.append("10) 신호 요약에 값이 있는 항목(0/없음 제외) 중 최소 1개는 반드시 근거 또는 실행에 반영\n");
builder.append("11) 모든 신호가 0/없음이면 '## 데이터 격차'에 '신호 데이터 부족'을 포함\n");
builder.append("12) 우선순위 기준 예시: 전환율 < 70% 또는 예약/방문 하락 → High, 불일치/미운영일 2일 이상 → Med, 개선 효과가 제한적이면 Low\n");
builder.append("13) 구내식당 미운영일 대응 실행은 '우리 식당 정보 제공' 또는 '특별 프로모션 기획'으로 작성\n");
builder.append("14) 일자별 예약/통계 데이터는 참고용이며 그대로 나열하지 말 것\n");

builder.append("이번 주 요일-날짜 매핑: 월=..., 화=..., 수=..., 목=..., 금=..., 토=..., 일=...\n");
```

#### 코드 스니펫 (fallback)

```java
private String buildFallbackSummary() {
    return "## 핵심 요약\n" +
        "- ⚠️ AI 요약 생성에 실패하여 기본 요약으로 대체했습니다.\n" +
        "- 📊 지난 주 대비 변화 요인을 확인해 주세요.\n" +
        "- ✅ 이번 주 운영 메시지/프로모션 준비가 필요합니다.\n" +
        "## 통합 분석 및 추천\n" +
        "- 📌 근거: 데이터 부족 / 기대효과: 운영 안정화 / 우선순위: Med 🟡 / 실행: 예약 확정 고객 리마인드 메시지 점검\n" +
        "## 이번 주 TODO\n" +
        "- ✅ 필수: 리마인드 메시지 템플릿 점검\n" +
        "- ✍️ 선택: 프로모션 문구 1건 개선\n" +
        "## 데이터 격차\n" +
        "- ℹ️ 데이터 부족 없음\n";
}
```

### 2) AI 인사이트 강제 새로고침 + 60분 쿨다운

- 적용 위치:
  - `src/main/java/com/example/LunchGo/restaurant/service/RestaurantStatsService.java`
  - `src/main/java/com/example/LunchGo/restaurant/controller/BusinessRestaurantController.java`
- 변경 요약:
  - `GET /api/business/restaurants/{id}/stats/weekly?refresh=true` 지원
  - refresh 요청 시 캐시 무시, 60분 쿨다운 내 재요청은 캐시 응답

#### 코드 스니펫 (서비스)

```java
private static final long REFRESH_COOLDOWN_MINUTES = 60;
private static final long REFRESH_COOLDOWN_MILLIS = REFRESH_COOLDOWN_MINUTES * 60 * 1000;
private static final String REFRESH_COOLDOWN_KEY_PREFIX = "ai-insights-refresh:";

public WeeklyAiInsightsResponse getWeeklyStatsInsight(Long restaurantId, boolean forceRefresh) {
    LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
    String cacheKey = REDIS_CACHE_KEY_PREFIX + restaurantId + ":" + weekStart.toString();
    String refreshKey = REFRESH_COOLDOWN_KEY_PREFIX + restaurantId + ":" + weekStart.toString();

    if (forceRefresh) {
        Optional<WeeklyAiInsightsResponse> cachedInsights =
            getCachedInsights(cacheKey, restaurantId, weekStart);
        if (redisUtil.existData(refreshKey) && cachedInsights.isPresent()) {
            return cachedInsights.get();
        }
        WeeklyAiInsightsResponse response =
            generateAndCacheInsights(restaurantId, weekStart, cacheKey);
        redisUtil.setDataExpire(refreshKey, "1", REFRESH_COOLDOWN_MILLIS);
        return response;
    }
    ...
}
```

#### 코드 스니펫 (컨트롤러)

```java
@GetMapping("/restaurants/{id}/stats/weekly")
public ResponseEntity<WeeklyAiInsightsResponse> getWeeklyStatsInsight(
    @PathVariable("id") Long id,
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestParam(value = "refresh", defaultValue = "false") boolean refresh
) {
    ...
    return ResponseEntity.ok(restaurantStatsService.getWeeklyStatsInsight(id, refresh));
}
```

### 3) 프론트 UI 개선 (AI 인사이트 화면)

- 적용 위치: `frontend/src/views/business/insights/BusinessAiInsightsPage.vue`
- 변경 요약:
  - “통합 분석 및 추천”을 표 형식으로 렌더링하고, 우선순위만 배지 강조.
  - 표 컬럼 순서를 `근거 / 기대효과 / 실행 / 우선순위`로 정렬하고 우선순위 컬럼을 좁게 설정.
  - AI 요약 탭의 “통합 분석 및 추천”도 동일한 표 렌더링 적용.
  - 요약/핵심 지표에 이모지 배지 스타일 적용.
  - “이번주 TODO 체크 리스트”를 독립 카드로 분리 배치.

## 진행 상태 체크 (2026-01-17 기준)

- [x] 1단계: 프롬프트 템플릿 정리 (섹션 고정/이모지/날짜 규칙 적용)
- [x] 2단계: 프론트 파서 검증/보강 (표 렌더/섹션 파싱 안정화)
- [x] 3단계: 행동형 문장/우선순위 가이드 추가 (우선순위 기준 예시 추가)
- [x] 4단계: 신호 필드 의무 반영 (값 있는 신호 1개 이상 반영)
- [x] 5단계: QA 및 예시 확인 (테스트 케이스 검증 완료)
