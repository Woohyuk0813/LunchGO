package com.example.LunchGo.restaurant.service;

import com.example.LunchGo.ai.service.AiChatService;
import com.example.LunchGo.reservation.dto.BusinessReservationItemResponse;
import com.example.LunchGo.reservation.service.BusinessReservationQueryService;
import com.example.LunchGo.restaurant.dto.WeeklyAiInsightsResponse;
import com.example.LunchGo.restaurant.dto.WeeklyAiInsightsResponse.DailyReservationPoint;
import com.example.LunchGo.restaurant.dto.WeeklyAiInsightsResponse.DailyStatsPoint;
import com.example.LunchGo.restaurant.dto.WeeklyAiInsightsResponse.SignalSummary;
import com.example.LunchGo.restaurant.dto.WeeklyDemandPrediction;
import com.example.LunchGo.restaurant.dto.WeeklyDemandSignalResponse;
import com.example.LunchGo.restaurant.entity.DailyRestaurantStats;
import com.example.LunchGo.restaurant.entity.WeeklyPrediction;
import com.example.LunchGo.restaurant.repository.DailyRestaurantStatsRepository;
import com.example.LunchGo.restaurant.repository.WeeklyPredictionRepository;
import com.example.LunchGo.common.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;

import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;
import java.awt.Color;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.redisson.api.RLock;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class RestaurantStatsService {
    private final BusinessReservationQueryService businessReservationQueryService;
    private final DailyRestaurantStatsRepository dailyRestaurantStatsRepository;
    private final WeeklyPredictionRepository weeklyPredictionRepository;
    private final AiChatService aiChatService;
    private final WeeklyDemandSignalService weeklyDemandSignalService;
    private final RuleBasedForecastService ruleBasedForecastService;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;
    @Value("${pdf.korean-font-path:}")
    private String koreanFontPath;
    @Value("${owner.ai.forecast.force-fallback:false}")
    private boolean forceForecastFallback;
    
    private static final String REDIS_CACHE_KEY_PREFIX = "ai_insights:restaurant:";
    private static final long CACHE_TTL_HOURS = 24;
    private static final long CACHE_TTL_MILLIS = CACHE_TTL_HOURS * 60 * 60 * 1000;
    private static final long REFRESH_COOLDOWN_MINUTES = 60;
    private static final long REFRESH_COOLDOWN_MILLIS = REFRESH_COOLDOWN_MINUTES * 60 * 1000;
    private static final String REFRESH_COOLDOWN_KEY_PREFIX = "ai-insights-refresh:";
    private static final Color COLOR_PRIMARY = new Color(30, 58, 95);
    private static final Color COLOR_ACCENT = new Color(255, 107, 74);
    private static final Color COLOR_ACCENT_LIGHT = new Color(255, 196, 184);
    private static final Color COLOR_BG = new Color(248, 249, 250);
    private static final Color COLOR_CARD_BG = new Color(255, 255, 255);
    private static final Color COLOR_BORDER = new Color(233, 236, 239);
    private static final Color COLOR_TEXT = new Color(33, 37, 41);
    private static final Color COLOR_MUTED = new Color(108, 117, 125);

    public byte[] generateWeeklyStatsPdf(Long restaurantId) {
        try {
            List<BusinessReservationItemResponse> allReservations =
                businessReservationQueryService.getList(restaurantId);

            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(6);
            LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<BusinessReservationItemResponse> weeklyReservations = allReservations.stream()
                .filter(item -> isWithinLast7Days(item.getDatetime(), start, end))
                .toList();

            List<DailyRestaurantStats> dailyStats =
                dailyRestaurantStatsRepository.findLast7DaysByRestaurantId(restaurantId);

            WeeklyDemandSignalResponse signal =
                weeklyDemandSignalService.buildWeeklySignal(restaurantId, weekStart, weekEnd);
            List<WeeklyDemandPrediction> predictions =
                ruleBasedForecastService.forecast(signal, weekStart, weekEnd);

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
            String predictionSection = buildPredictionMarkdown(predictions, aiFallbackUsed);
            String finalSummary = appendPredictionSection(aiSummary, predictionSection);
            if (!hasWeekdayBreakdown(finalSummary)) {
                String appendix = buildPredictionAppendix(predictions);
                if (!appendix.isBlank()) {
                    finalSummary = finalSummary + "\n" + appendix;
                }
            }

            return buildPdf(restaurantId, start, end, weeklyReservations, dailyStats, finalSummary);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("AI request failed for restaurantId={}", restaurantId, e);
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (message.contains("RESOURCE_EXHAUSTED") || message.contains("quota")) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "ai_quota_exceeded", e);
            }
            if (message.contains("NOT_FOUND")) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ai_model_not_found", e);
            }
            if (message.contains("timeout")) {
                throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "ai_timeout", e);
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ai_error", e);
        } catch (Exception e) {
            log.error("Weekly stats PDF generation failed for restaurantId={}", restaurantId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "stats_pdf_failed", e);
        }
    }

    /**
     * 주간 AI 인사이트 데이터를 조회합니다. 캐싱 로직을 포함하고 있으며, 캐시가 없을 경우 새로운 데이터를 생성하고 캐시에 저장합니다.
     * @param restaurantId 식당 ID
     * @return 주간 AI 인사이트 응답
     */
    public WeeklyAiInsightsResponse getWeeklyStatsInsight(Long restaurantId) {
        return getWeeklyStatsInsight(restaurantId, false);
    }

    /**
     * 주간 AI 인사이트 데이터를 조회합니다. refresh 요청 시 60분 쿨다운이 적용됩니다.
     * @param restaurantId 식당 ID
     * @param forceRefresh 캐시 무시 여부
     * @return 주간 AI 인사이트 응답
     */
    public WeeklyAiInsightsResponse getWeeklyStatsInsight(Long restaurantId, boolean forceRefresh) {
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        String cacheKey = REDIS_CACHE_KEY_PREFIX + restaurantId + ":" + weekStart.toString();
        String refreshKey = REFRESH_COOLDOWN_KEY_PREFIX + restaurantId + ":" + weekStart.toString();
        String lockKey = "lock:ai-insights:" + restaurantId + ":" + weekStart.toString();

        if (forceRefresh) {
            Optional<WeeklyAiInsightsResponse> cachedInsights =
                getCachedInsights(cacheKey, restaurantId, weekStart);
            if (redisUtil.existData(refreshKey)) {
                return cachedInsights.orElseGet(
                    () -> generateAndCacheInsights(restaurantId, weekStart, cacheKey)
                );
            }

            RLock lock = redisUtil.getFairLock(lockKey);
            try {
                boolean isLocked = lock.tryLock(5, -1, TimeUnit.SECONDS);
                if (!isLocked) {
                    throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "인사이트를 생성 중입니다. 잠시 후 다시 시도해주세요."
                    );
                }

                cachedInsights = getCachedInsights(cacheKey, restaurantId, weekStart);
                if (redisUtil.existData(refreshKey)) {
                    return cachedInsights.orElseGet(
                        () -> generateAndCacheInsights(restaurantId, weekStart, cacheKey)
                    );
                }

                WeeklyAiInsightsResponse response =
                    generateAndCacheInsights(restaurantId, weekStart, cacheKey);
                redisUtil.setDataExpire(refreshKey, "1", REFRESH_COOLDOWN_MILLIS);
                return response;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "인사이트 생성 중 오류가 발생했습니다."
                );
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

        Optional<WeeklyAiInsightsResponse> cachedInsights = getCachedInsights(cacheKey, restaurantId, weekStart);
        if (cachedInsights.isPresent()) {
            return cachedInsights.get();
        }

        return generateAndCacheInsights(restaurantId, weekStart, cacheKey);
    }

    /**
     * 캐시된 주간 AI 인사이트 데이터를 조회하고, 필요한 경우 예측 데이터를 업데이트합니다.
     * @param cacheKey Redis 캐시 키
     * @param restaurantId 식당 ID
     * @param weekStart 주의 시작일
     * @return 캐시된 데이터가 있으면 WeeklyAiInsightsResponse를 포함하는 Optional, 그렇지 않으면 빈 Optional
     */
    private Optional<WeeklyAiInsightsResponse> getCachedInsights(String cacheKey, Long restaurantId, LocalDate weekStart) {
        String cachedData = redisUtil.getData(cacheKey);
        if (cachedData == null || cachedData.isBlank()) {
            return Optional.empty();
        }

        try {
            WeeklyAiInsightsResponse cached = objectMapper.readValue(cachedData, WeeklyAiInsightsResponse.class);
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

    /**
     * 새로운 주간 AI 인사이트를 생성하고 Redis에 캐시합니다.
     * @param restaurantId 식당 ID
     * @param weekStart 주의 시작일
     * @param cacheKey Redis 캐시 키
     * @return 생성된 주간 AI 인사이트 응답
     */
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

    /**
     * DB에서 주간 예측 데이터를 조회하거나, 없을 경우 새로 생성합니다.
     * @param restaurantId 식당 ID
     * @param weekStart 주의 시작일
     * @param signal 수요 예측 시그널
     * @param fromCache 캐시 조회 로직에서 호출되었는지 여부
     * @return 주간 수요 예측 리스트
     */
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

    private boolean isWithinLast7Days(String datetime, LocalDate start, LocalDate end) {
        if (datetime == null || datetime.isBlank()) {
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US);
        LocalDate date = LocalDateTime.parse(datetime, formatter).toLocalDate();
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private String buildPrompt(
        Long restaurantId,
        LocalDate start,
        LocalDate end,
        LocalDate weekStart,
        LocalDate weekEnd,
        List<BusinessReservationItemResponse> reservations,
        List<DailyRestaurantStats> dailyStats,
        WeeklyDemandSignalResponse signal
    ) {
        Map<LocalDate, Integer> reservationCounts = new TreeMap<>();
        Map<LocalDate, Integer> reservationAmounts = new TreeMap<>();

        for (BusinessReservationItemResponse item : reservations) {
            LocalDate date = LocalDate.parse(item.getDatetime().substring(0, 10));
            reservationCounts.merge(date, 1, Integer::sum);
            int amount = item.getAmount() == null ? 0 : item.getAmount();
            reservationAmounts.merge(date, amount, Integer::sum);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("다음 데이터를 기반으로 주간 AI 인사이트를 한국어로 작성해 주세요.\n");
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

        builder.append("기간 정보: 지난 주=")
            .append(start)
            .append("~")
            .append(end)
            .append(", 이번 주 예측=")
            .append(weekStart)
            .append("~")
            .append(weekEnd)
            .append("\n");
        builder.append("이번 주 요일-날짜 매핑: ");
        String[] weekdayLabels = {"월", "화", "수", "목", "금", "토", "일"};
        for (int i = 0; i < weekdayLabels.length; i++) {
            LocalDate date = weekStart.plusDays(i);
            builder.append(weekdayLabels[i]).append("=")
                .append(date)
                .append(i == weekdayLabels.length - 1 ? "\n" : ", ");
        }

        builder.append("일자별 예약 요약 (건수, 합계금액): ");
        for (Map.Entry<LocalDate, Integer> entry : reservationCounts.entrySet()) {
            int amount = reservationAmounts.getOrDefault(entry.getKey(), 0);
            builder.append(entry.getKey()).append(" => ")
                .append(entry.getValue()).append(", ")
                .append(amount).append(" ");
        }

        builder.append("일자별 식당 통계: ");
        dailyStats.stream()
            .sorted(Comparator.comparing(stats -> stats.getId().getStatDate()))
            .forEach(stats -> builder.append(stats.getId().getStatDate()).append(" | ")
                .append("view=").append(nullToZero(stats.getViewCount())).append(", ")
                .append("try=").append(nullToZero(stats.getTryCount())).append(", ")
                .append("confirm=").append(nullToZero(stats.getConfirmCount())).append(", ")
                .append("visit=").append(nullToZero(stats.getVisitCount())).append(", ")
                .append("noshow=").append(nullToZero(stats.getDefendedNoshowCnt())).append(", ")
                .append("penalty=").append(nullToZero(stats.getPenaltyStlAmt())).append(", ")
                .append("revenue=").append(nullToZeroLong(stats.getRevenueTotal()))
                .append(" "));

        int totalConfirm = dailyStats.stream()
            .mapToInt(stats -> nullToZero(stats.getConfirmCount()))
            .sum();
        int totalVisit = dailyStats.stream()
            .mapToInt(stats -> nullToZero(stats.getVisitCount()))
            .sum();
        int conversionRate = totalConfirm == 0 ? 0 : Math.round((float) totalVisit / totalConfirm * 100);
        builder.append("방문 전환율 정의: visit 대비 confirm 비율. 현재 방문 전환율=")
            .append(conversionRate)
            .append("% ");

        builder.append("추가 참고 데이터 (요약 지표): ");
        builder.append(buildSignalSummary(signal, weekStart, weekEnd));
        builder.append("응답은 반드시 한국어로 작성하세요.");
        return builder.toString();
    }

    private String buildSignalSummary(
        WeeklyDemandSignalResponse signal,
        LocalDate weekStart,
        LocalDate weekEnd
    ) {
        if (signal == null) {
            return "";
        }
        SignalSummaryPayload payload = buildSignalSummaryPayloadInternal(signal, weekStart, weekEnd);
        StringBuilder builder = new StringBuilder();
        builder.append("공개 북마크=").append(payload.publicBookmarks).append(", ");
        builder.append("공유 링크=").append(payload.approvedLinks).append(", ");
        builder.append("우리 식당 메뉴 키워드=")
            .append(payload.restaurantMenuKeywords.isEmpty()
                ? "없음"
                : String.join("/", payload.restaurantMenuKeywords))
            .append(", ");
        builder.append("우리 식당 메뉴/취향 겹침=")
            .append(payload.restaurantMenuOverlap)
            .append(", ");
        builder.append("구내식당 메뉴 키워드=")
            .append(payload.menuKeywords.isEmpty() ? "없음" : String.join("/", payload.menuKeywords))
            .append(", ");
        builder.append("사용자 취향 키워드=")
            .append(payload.preferenceKeywords.isEmpty() ? "없음" : String.join("/", payload.preferenceKeywords))
            .append(", ");
        builder.append("키워드 겹침=").append(payload.overlapCount).append(", ");
        builder.append("구내식당 메뉴 불일치 높은 날짜=")
            .append(payload.mismatchTopDates.isEmpty() ? "없음" : String.join("/", payload.mismatchTopDates))
            .append(", ");
        builder.append("구내식당 미운영 날짜=")
            .append(payload.noMenuDates.isEmpty() ? "없음" : String.join("/", payload.noMenuDates))
            .append(", ");
        if (payload.topCompanyName != null) {
            builder.append("상위 회사=").append(payload.topCompanyName)
                .append("(").append(Math.round(payload.topCompanyShare * 100)).append("%) ");
        }
        return builder.toString();
    }

    private SignalSummary buildSignalSummaryPayload(
        WeeklyDemandSignalResponse signal,
        LocalDate weekStart,
        LocalDate weekEnd
    ) {
        SignalSummaryPayload payload = buildSignalSummaryPayloadInternal(signal, weekStart, weekEnd);
        return SignalSummary.builder()
            .publicBookmarkCount(payload.publicBookmarks)
            .approvedLinkCount(payload.approvedLinks)
            .restaurantMenuKeywords(payload.restaurantMenuKeywords)
            .restaurantMenuOverlap(payload.restaurantMenuOverlap)
            .menuKeywords(payload.menuKeywords)
            .preferenceKeywords(payload.preferenceKeywords)
            .keywordOverlap(payload.overlapCount)
            .mismatchDates(payload.mismatchTopDates)
            .noMenuDates(payload.noMenuDates)
            .topCompanyName(payload.topCompanyName)
            .topCompanyShare(payload.topCompanyShare)
            .build();
    }

    private SignalSummaryPayload buildSignalSummaryPayloadInternal(
        WeeklyDemandSignalResponse signal,
        LocalDate weekStart,
        LocalDate weekEnd
    ) {
        WeeklyDemandSignalResponse.ShareSignal share = signal.getShareSignal();
        int publicBookmarks = share == null ? 0 : share.getPublicBookmarkCount();
        int approvedLinks = share == null ? 0 : share.getApprovedLinkCount();

        List<String> restaurantMenuKeywords = Optional.ofNullable(signal.getRestaurantMenuKeywords())
            .orElseGet(List::of)
            .stream()
            .filter(name -> name != null && !name.isBlank())
            .map(name -> name.trim().toLowerCase(Locale.KOREA))
            .distinct()
            .limit(8)
            .toList();

        List<String> menuKeywords = signal.getCafeteriaMenuSignals().stream()
            .flatMap(menu -> menu.getMenuNames() == null
                ? java.util.stream.Stream.empty()
                : menu.getMenuNames().stream())
            .filter(name -> name != null && !name.isBlank())
            .map(name -> name.trim().toLowerCase(Locale.KOREA))
            .distinct()
            .limit(5)
            .toList();

        List<String> likedKeywords = signal.getPreferenceSignals().stream()
            .filter(WeeklyDemandSignalResponse.PreferenceSignal::isLiked)
            .map(WeeklyDemandSignalResponse.PreferenceSignal::getKeyword)
            .filter(keyword -> keyword != null && !keyword.isBlank())
            .map(keyword -> keyword.trim().toLowerCase(Locale.KOREA))
            .distinct()
            .limit(5)
            .toList();

        long overlapCount = menuKeywords.stream()
            .filter(likedKeywords::contains)
            .count();

        long restaurantMenuOverlap = restaurantMenuKeywords.stream()
            .filter(menu -> likedKeywords.stream().anyMatch(menu::contains))
            .count();

        List<String> dislikedKeywords = signal.getPreferenceSignals().stream()
            .filter(pref -> !pref.isLiked())
            .map(WeeklyDemandSignalResponse.PreferenceSignal::getKeyword)
            .filter(keyword -> keyword != null && !keyword.isBlank())
            .map(keyword -> keyword.trim().toLowerCase(Locale.KOREA))
            .distinct()
            .toList();

        Map<String, Long> mismatchByDate = signal.getCafeteriaMenuSignals().stream()
            .filter(menu -> menu.getServedDate() != null && menu.getMenuNames() != null)
            .collect(Collectors.groupingBy(
                WeeklyDemandSignalResponse.CafeteriaMenuSignal::getServedDate,
                Collectors.summingLong(menu -> menu.getMenuNames().stream()
                    .filter(name -> name != null && !name.isBlank())
                    .map(name -> name.trim().toLowerCase(Locale.KOREA))
                    .filter(menuName -> dislikedKeywords.stream().anyMatch(menuName::contains))
                    .count())
            ));

        List<String> mismatchTopDates = mismatchByDate.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(3)
            .map(entry -> entry.getKey() + ":" + entry.getValue())
            .toList();

        List<String> noMenuDates = new java.util.ArrayList<>();
        if (weekStart != null && weekEnd != null) {
            java.util.Set<String> servedDates = signal.getCafeteriaMenuSignals().stream()
                .map(WeeklyDemandSignalResponse.CafeteriaMenuSignal::getServedDate)
                .filter(date -> date != null && !date.isBlank())
                .collect(Collectors.toSet());
            LocalDate cursor = weekStart;
            while (!cursor.isAfter(weekEnd)) {
                String date = cursor.toString();
                if (!servedDates.contains(date)) {
                    noMenuDates.add(date);
                }
                cursor = cursor.plusDays(1);
            }
        }

        WeeklyDemandSignalResponse.CompanySignal topCompany = signal.getCompanySignals().stream()
            .findFirst()
            .orElse(null);

        return new SignalSummaryPayload(
            publicBookmarks,
            approvedLinks,
            restaurantMenuKeywords,
            restaurantMenuOverlap,
            menuKeywords,
            likedKeywords,
            overlapCount,
            mismatchTopDates,
            noMenuDates,
            topCompany == null ? null : topCompany.getCompanyName(),
            topCompany == null ? 0.0 : topCompany.getShare()
        );
    }

    private record SignalSummaryPayload(
        int publicBookmarks,
        int approvedLinks,
        List<String> restaurantMenuKeywords,
        long restaurantMenuOverlap,
        List<String> menuKeywords,
        List<String> preferenceKeywords,
        long overlapCount,
        List<String> mismatchTopDates,
        List<String> noMenuDates,
        String topCompanyName,
        double topCompanyShare
    ) {}

    private List<DailyReservationPoint> buildDailyReservationPoints(
        List<BusinessReservationItemResponse> reservations
    ) {
        Map<LocalDate, Integer> counts = new TreeMap<>();
        Map<LocalDate, Integer> amounts = new TreeMap<>();
        for (BusinessReservationItemResponse item : reservations) {
            LocalDate date = LocalDate.parse(item.getDatetime().substring(0, 10));
            counts.merge(date, 1, Integer::sum);
            int amount = item.getAmount() == null ? 0 : item.getAmount();
            amounts.merge(date, amount, Integer::sum);
        }
        return counts.entrySet().stream()
            .map(entry -> DailyReservationPoint.builder()
                .date(entry.getKey().toString())
                .count(entry.getValue())
                .amount(amounts.getOrDefault(entry.getKey(), 0))
                .build())
            .toList();
    }

    private List<DailyStatsPoint> buildDailyStatsPoints(List<DailyRestaurantStats> dailyStats) {
        return dailyStats.stream()
            .sorted(Comparator.comparing(stats -> stats.getId().getStatDate()))
            .map(stats -> DailyStatsPoint.builder()
                .date(stats.getId().getStatDate().toString())
                .viewCount(nullToZero(stats.getViewCount()))
                .tryCount(nullToZero(stats.getTryCount()))
                .confirmCount(nullToZero(stats.getConfirmCount()))
                .visitCount(nullToZero(stats.getVisitCount()))
                .noshowCount(nullToZero(stats.getDefendedNoshowCnt()))
                .penaltyAmount((long) nullToZero(stats.getPenaltyStlAmt()))
                .revenue(nullToZeroLong(stats.getRevenueTotal()))
                .build())
            .toList();
    }

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

    private String appendPredictionSection(String summary, String predictionSection) {
        if (predictionSection == null || predictionSection.isBlank()) {
            return summary;
        }
        if (summary != null && summary.contains("## 금주 방문 예측")) {
            return summary;
        }
        if (summary == null || summary.isBlank()) {
            return predictionSection;
        }
        return summary + "\n" + predictionSection;
    }


    private String buildPredictionMarkdown(
        List<WeeklyDemandPrediction> predictions,
        boolean isFallback
    ) {
        if (predictions == null || predictions.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("## 금주 방문 예측\n");
        if (isFallback) {
            builder.append("- 규칙 기반 추정 결과입니다.\n");
        }
        builder.append(buildPredictionLines(predictions, true));
        return builder.toString();
    }

    private String buildPredictionAppendix(List<WeeklyDemandPrediction> predictions) {
        if (predictions == null || predictions.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("### 요일별 예상 방문 범위(규칙 기반)\n");
        builder.append(buildPredictionLines(predictions, false));
        return builder.toString();
    }

    private String buildPredictionLines(
        List<WeeklyDemandPrediction> predictions,
        boolean includeEvidence
    ) {
        StringBuilder builder = new StringBuilder();
        for (WeeklyDemandPrediction prediction : predictions) {
            builder.append("- ")
                .append(formatWeekday(prediction.getWeekday()))
                .append(" ")
                .append(prediction.getExpectedMin())
                .append("~")
                .append(prediction.getExpectedMax())
                .append("명 (")
                .append(prediction.getConfidence())
                .append(")\n");
            if (includeEvidence && prediction.getEvidence() != null && !prediction.getEvidence().isEmpty()) {
                for (String evidence : prediction.getEvidence()) {
                    builder.append("  - ").append(evidence).append("\n");
                }
            }
        }
        return builder.toString();
    }

    private boolean hasWeekdayBreakdown(String summary) {
        if (summary == null || summary.isBlank()) {
            return false;
        }
        return summary.contains("월요일")
            || summary.contains("화요일")
            || summary.contains("수요일")
            || summary.contains("목요일")
            || summary.contains("금요일")
            || summary.contains("토요일")
            || summary.contains("일요일");
    }

    private String formatWeekday(int weekday) {
        return switch (weekday) {
            case 1 -> "일";
            case 2 -> "월";
            case 3 -> "화";
            case 4 -> "수";
            case 5 -> "목";
            case 6 -> "금";
            case 7 -> "토";
            default -> String.valueOf(weekday);
        };
    }

private byte[] buildPdf(
        Long restaurantId,
        LocalDate start,
        LocalDate end,
        List<BusinessReservationItemResponse> reservations,
        List<DailyRestaurantStats> dailyStats,
        String aiSummary
    ) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDType0Font font = loadKoreanFont(document);
            PDPage page = new PDPage();
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();
            float margin = 48f;
            float contentWidth = mediaBox.getWidth() - (margin * 2);
            float y = mediaBox.getHeight() - margin;

            PdfCursor cursor = new PdfCursor(document, page, y, margin, margin);
            PDPageContentStream content = null;
            try {
                content = new PDPageContentStream(document, page);
                drawPageBackground(content, mediaBox);
                cursor.setContent(content);
                cursor.setY(drawHeader(content, font, mediaBox, margin, cursor.getY(), restaurantId, start, end));
                cursor.setY(cursor.getY() - 12);
                cursor.setY(
                    drawSummaryCards(
                        content,
                        font,
                        margin,
                        contentWidth,
                        cursor.getY(),
                        reservations.size(),
                        dailyStats.size()
                    )
                );
                cursor.setY(cursor.getY() - 4);
                String safeSummary = sanitizeForPdf(aiSummary);
                cursor.setY(drawMarkdownSection(cursor, font, safeSummary, contentWidth, 14));
            } finally {
                if (cursor.getContent() != null) {
                    cursor.getContent().close();
                }
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate PDF", e);
        }
    }

    private static void drawPageBackground(PDPageContentStream content, PDRectangle mediaBox) throws IOException {
        content.setNonStrokingColor(COLOR_BG);
        content.addRect(0, 0, mediaBox.getWidth(), mediaBox.getHeight());
        content.fill();
    }

    private static String sanitizeForPdf(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sanitized = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); ) {
            int codePoint = s.codePointAt(i);
            i += Character.charCount(codePoint);
            // Drop any surrogate remnants, supplemental code points, joiners, or symbol emojis.
            if (codePoint >= 0xD800 && codePoint <= 0xDFFF) {
                continue;
            }
            if (codePoint > 0xFFFF) {
                continue;
            }
            if (codePoint == 0xFE0F || codePoint == 0xFE0E || codePoint == 0x200D) {
                continue;
            }
            if (Character.getType(codePoint) == Character.OTHER_SYMBOL) {
                continue;
            }
            sanitized.appendCodePoint(codePoint);
        }
        return sanitized.toString();
    }

    private List<String> wrapText(String text, PDType0Font font, int fontSize, float maxWidth) throws IOException {
        String safeText = sanitizeForPdf(text);
        if (safeText.isBlank()) {
            return List.of("");
        }
        String[] rawLines = safeText.replace("\r", "").split("\n");
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        for (String raw : rawLines) {
            String line = raw.trim();
            if (line.isEmpty()) {
                lines.add("");
                continue;
            }
            lines.addAll(wrapByWidth(line, font, fontSize, maxWidth));
        }
        return lines;
    }

    private List<String> wrapByWidth(String text, PDType0Font font, int fontSize, float maxWidth) throws IOException {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            line.append(text.charAt(i));
            if (getTextWidth(font, fontSize, line.toString()) > maxWidth) {
                if (line.length() > 1) {
                    lines.add(line.substring(0, line.length() - 1));
                    line.delete(0, line.length() - 1);
                } else {
                    lines.add(line.toString());
                    line.setLength(0);
                }
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    private float drawHeader(
        PDPageContentStream content,
        PDType0Font font,
        PDRectangle mediaBox,
        float margin,
        float y,
        Long restaurantId,
        LocalDate start,
        LocalDate end
    ) throws IOException {
        float headerHeight = 86f;
        float headerBottom = y - headerHeight + 12;

        content.setNonStrokingColor(COLOR_CARD_BG);
        content.addRect(0, headerBottom, mediaBox.getWidth(), headerHeight);
        content.fill();
        content.setStrokingColor(COLOR_BORDER);
        content.moveTo(0, headerBottom);
        content.lineTo(mediaBox.getWidth(), headerBottom);
        content.stroke();

        content.setNonStrokingColor(COLOR_ACCENT);
        content.addRect(margin, y - 40, 48, 4);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(COLOR_PRIMARY);
        content.setFont(font, 18);
        content.newLineAtOffset(margin, y - 30);
        showTextSafe(content, "주간 식당 통계 리포트");
        content.endText();

        content.beginText();
        content.setNonStrokingColor(COLOR_MUTED);
        content.setFont(font, 12);
        content.newLineAtOffset(margin, y - 54);
        showTextSafe(content, "기간: " + start + " ~ " + end);
        content.endText();

        String meta = "식당 ID: " + restaurantId;
        float metaWidth = getTextWidth(font, 10, meta);
        content.beginText();
        content.setNonStrokingColor(COLOR_MUTED);
        content.setFont(font, 10);
        content.newLineAtOffset(mediaBox.getWidth() - margin - metaWidth, y - 30);
        content.endText();
        return headerBottom - 8;
    }

    private float drawSummaryCards(
        PDPageContentStream content,
        PDType0Font font,
        float margin,
        float contentWidth,
        float y,
        int reservationCount,
        int statsCount
    ) throws IOException {
        float cardHeight = 48f;
        float gap = 12f;
        float cardWidth = (contentWidth - gap) / 2f;
        float cardY = y - cardHeight - 8;

        drawCard(content, margin, cardY, cardWidth, cardHeight, COLOR_CARD_BG);
        drawCard(content, margin + cardWidth + gap, cardY, cardWidth, cardHeight, COLOR_CARD_BG);
        content.setNonStrokingColor(COLOR_ACCENT);
        content.addRect(margin, cardY + cardHeight - 4, cardWidth, 4);
        content.fill();
        content.setNonStrokingColor(COLOR_ACCENT_LIGHT);
        content.addRect(margin + cardWidth + gap, cardY + cardHeight - 4, cardWidth, 4);
        content.fill();

        content.beginText();
        content.setNonStrokingColor(COLOR_MUTED);
        content.setFont(font, 10);
        content.newLineAtOffset(margin + 12, cardY + 30);
        showTextSafe(content, "예약 건수");
        content.newLineAtOffset(0, -16);
        content.setNonStrokingColor(COLOR_PRIMARY);
        content.setFont(font, 16);
        showTextSafe(content, String.valueOf(reservationCount));
        content.endText();

        content.beginText();
        content.setNonStrokingColor(COLOR_MUTED);
        content.setFont(font, 10);
        content.newLineAtOffset(margin + cardWidth + gap + 12, cardY + 30);
        showTextSafe(content, "일자별 통계 행 수");
        content.newLineAtOffset(0, -16);
        content.setNonStrokingColor(COLOR_PRIMARY);
        content.setFont(font, 16);
        showTextSafe(content, String.valueOf(statsCount));
        content.endText();

        return cardY - 16;
    }

    private void drawCard(
        PDPageContentStream content,
        float x,
        float y,
        float width,
        float height,
        Color fill
    ) throws IOException {
        content.setNonStrokingColor(fill);
        content.addRect(x, y, width, height);
        content.fill();
        content.setStrokingColor(COLOR_BORDER);
        content.addRect(x, y, width, height);
        content.stroke();
    }

    private float drawSectionTitle(
        PDPageContentStream content,
        PDType0Font font,
        float margin,
        float y,
        String title
    ) throws IOException {
        content.setNonStrokingColor(COLOR_ACCENT);
        content.addRect(margin, y - 3, 18, 3);
        content.fill();

        drawBoldText(content, font, 13, margin + 26, y - 1, title, COLOR_PRIMARY);

        content.setStrokingColor(COLOR_BORDER);
        content.moveTo(margin, y - 12);
        content.lineTo(margin + 260, y - 12);
        content.stroke();
        return y - 22;
    }

    private float drawParagraph(
        PdfCursor cursor,
        PDType0Font font,
        String text,
        float maxWidth,
        int fontSize,
        int lineHeight
    ) throws IOException {
        List<String> lines = wrapText(text, font, fontSize, maxWidth);
        for (String line : lines) {
            cursor.ensureSpace(lineHeight);
            PDPageContentStream content = cursor.getContent();
            content.beginText();
            content.setNonStrokingColor(COLOR_TEXT);
            content.setFont(font, fontSize);
            content.newLineAtOffset(cursor.getMargin(), cursor.getY());
            showTextSafe(content, line);
            content.endText();
            cursor.addY(-lineHeight);
        }
        return cursor.getY() - 8;
    }

    private float drawList(
        PdfCursor cursor,
        PDType0Font font,
        List<String> items,
        float maxWidth,
        int fontSize,
        int lineHeight
    ) throws IOException {
        for (String item : items) {
            List<String> lines = wrapText("- " + item, font, fontSize, maxWidth);
            for (String line : lines) {
                cursor.ensureSpace(lineHeight);
                PDPageContentStream content = cursor.getContent();
                content.beginText();
                content.setNonStrokingColor(COLOR_TEXT);
                content.setFont(font, fontSize);
                content.newLineAtOffset(cursor.getMargin(), cursor.getY());
                showTextSafe(content, line);
                content.endText();
                cursor.addY(-lineHeight);
            }
        }
        return cursor.getY() - 6;
    }

    private float drawMarkdownSection(
        PdfCursor cursor,
        PDType0Font font,
        String markdown,
        float maxWidth,
        int lineHeight
    ) throws IOException {
        if (markdown == null || markdown.isBlank()) {
            return drawParagraph(
                cursor,
                font,
                "AI 요약을 생성하지 못했습니다. 데이터만 참고해 주세요.",
                maxWidth,
                11,
                lineHeight
            );
        }

        String[] lines = markdown.replace("\r", "").split("\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                cursor.addY(-8);
                continue;
            }

            if (line.startsWith("### ")) {
                cursor.setY(drawSubheading(cursor, font, stripMarkdown(line.substring(4))));
                continue;
            }
            if (line.startsWith("## ")) {
                cursor.ensureSpace(22);
                cursor.setY(drawSectionTitle(cursor.getContent(), font, cursor.getMargin(), cursor.getY(), stripMarkdown(line.substring(3))));
                continue;
            }
            if (line.startsWith("# ")) {
                cursor.ensureSpace(24);
                cursor.setY(drawSectionTitle(cursor.getContent(), font, cursor.getMargin(), cursor.getY(), stripMarkdown(line.substring(2))));
                continue;
            }

            if (line.startsWith("- ") || line.startsWith("* ")) {
                drawList(cursor, font, List.of(stripMarkdown(line.substring(2))), maxWidth, 11, lineHeight);
                continue;
            }

            drawParagraph(cursor, font, stripMarkdown(line), maxWidth, 11, lineHeight);
        }
        return cursor.getY();
    }

    private float drawSubheading(
        PdfCursor cursor,
        PDType0Font font,
        String text
    ) throws IOException {
        int fontSize = 12;
        int lineHeight = 18;
        cursor.ensureSpace(lineHeight);
        drawBoldText(
            cursor.getContent(),
            font,
            fontSize,
            cursor.getMargin(),
            cursor.getY(),
            text,
            COLOR_PRIMARY
        );
        cursor.addY(-lineHeight);
        return cursor.getY();
    }

    private void drawBoldText(
        PDPageContentStream content,
        PDType0Font font,
        int fontSize,
        float x,
        float y,
        String text,
        Color color
    ) throws IOException {
        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        showTextSafe(content, text);
        content.endText();

        content.beginText();
        content.setNonStrokingColor(color);
        content.setFont(font, fontSize);
        content.newLineAtOffset(x + 0.35f, y);
        showTextSafe(content, text);
        content.endText();
    }

    private void showTextSafe(PDPageContentStream content, String text) throws IOException {
        content.showText(sanitizeForPdf(text));
    }

    private String stripMarkdown(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
            .replaceAll("`(.+?)`", "$1")
            .replaceAll("_([^_]+)_", "$1");
    }

    private List<String> buildReservationLines(List<BusinessReservationItemResponse> reservations) {
        Map<LocalDate, Integer> counts = new TreeMap<>();
        Map<LocalDate, Integer> amounts = new TreeMap<>();
        for (BusinessReservationItemResponse item : reservations) {
            LocalDate date = LocalDate.parse(item.getDatetime().substring(0, 10));
            counts.merge(date, 1, Integer::sum);
            int amount = item.getAmount() == null ? 0 : item.getAmount();
            amounts.merge(date, amount, Integer::sum);
        }
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        for (Map.Entry<LocalDate, Integer> entry : counts.entrySet()) {
            lines.add(entry.getKey() + "  |  예약 " + entry.getValue() + "건  |  합계 " +
                amounts.getOrDefault(entry.getKey(), 0) + "원");
        }
        if (lines.isEmpty()) {
            lines.add("최근 7일간 예약 데이터가 없습니다.");
        }
        return lines;
    }

    private List<String> buildStatsLines(List<DailyRestaurantStats> dailyStats) {
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        dailyStats.stream()
            .sorted(Comparator.comparing(stats -> stats.getId().getStatDate()))
            .forEach(stats -> lines.add(
                stats.getId().getStatDate() + "  |  조회 " + nullToZero(stats.getViewCount()) +
                    "  |  시도 " + nullToZero(stats.getTryCount()) +
                    "  |  확정 " + nullToZero(stats.getConfirmCount()) +
                    "  |  방문 " + nullToZero(stats.getVisitCount())
            ));
        if (lines.isEmpty()) {
            lines.add("최근 7일간 통계 데이터가 없습니다.");
        }
        return lines;
    }

    private PDType0Font loadKoreanFont(PDDocument document) throws IOException {
        if (koreanFontPath != null && !koreanFontPath.isBlank()) {
            Path path = Path.of(koreanFontPath);
            if (Files.exists(path)) {
                return PDType0Font.load(document, path.toFile());
            }
        }

        Path windowsFont = Path.of("C:\\Windows\\Fonts\\malgun.ttf");
        if (Files.exists(windowsFont)) {
            return PDType0Font.load(document, windowsFont.toFile());
        }

        try (InputStream stream = RestaurantStatsService.class.getResourceAsStream(
            "/fonts/NotoSansKR-Regular.ttf"
        )) {
            if (stream != null) {
                return PDType0Font.load(document, stream);
            }
        }

        throw new IllegalStateException(
            "Korean font not found. Set pdf.korean-font-path or add /fonts/NotoSansKR-Regular.ttf."
        );
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    private long nullToZeroLong(Long value) {
        return value == null ? 0L : value;
    }

    private float getTextWidth(PDType0Font font, int fontSize, String text) throws IOException {
        String safeText = sanitizeForPdf(text);
        return font.getStringWidth(safeText) / 1000f * fontSize;
    }

    /**
     * 예측 데이터를 MySQL에 저장
     */
    private void savePredictionsToDatabase(
        Long restaurantId,
        LocalDate weekStart,
        List<WeeklyDemandPrediction> predictions
    ) {
        try {
            // 기존 예측 데이터 삭제 (같은 주에 대한 예측이 이미 있는 경우)
            List<WeeklyPrediction> existing = weeklyPredictionRepository
                .findByRestaurantIdAndWeekStartDate(restaurantId, weekStart);
            if (!existing.isEmpty()) {
                weeklyPredictionRepository.deleteAll(existing);
            }

            // 새로운 예측 데이터 저장
            List<WeeklyPrediction> entities = predictions.stream()
                .map(prediction -> {
                    String evidenceJson = null;
                    if (prediction.getEvidence() != null && !prediction.getEvidence().isEmpty()) {
                        try {
                            evidenceJson = objectMapper.writeValueAsString(prediction.getEvidence());
                        } catch (JsonProcessingException e) {
                            log.warn("Failed to serialize evidence for prediction, restaurantId={}, weekday={}", 
                                restaurantId, prediction.getWeekday(), e);
                        }
                    }
                    return WeeklyPrediction.builder()
                        .id(WeeklyPrediction.WeeklyPredictionId.builder()
                            .restaurantId(restaurantId)
                            .weekStartDate(weekStart)
                            .weekday(prediction.getWeekday())
                            .build())
                        .expectedMin(prediction.getExpectedMin())
                        .expectedMax(prediction.getExpectedMax())
                        .confidence(prediction.getConfidence())
                        .evidence(evidenceJson)
                        .build();
                })
                .toList();
            
            weeklyPredictionRepository.saveAll(entities);
            log.info("Saved {} predictions to database for restaurantId={}, weekStart={}", 
                entities.size(), restaurantId, weekStart);
        } catch (Exception e) {
            log.error("Failed to save predictions to database, restaurantId={}, weekStart={}", 
                restaurantId, weekStart, e);
            // 예외가 발생해도 응답은 정상적으로 반환
        }
    }

    /**
     * 저번 주 예측 데이터를 조회하여 WeeklyPredictionPoint 리스트로 변환
     */
    private List<WeeklyAiInsightsResponse.WeeklyPredictionPoint> getLastWeekPredictions(
        Long restaurantId,
        LocalDate currentWeekStart
    ) {
        try {
            LocalDate lastWeekStart = currentWeekStart.minusDays(7);
            log.info("조회 저번 주 예측 데이터: restaurantId={}, currentWeekStart={}, lastWeekStart={}", 
                restaurantId, currentWeekStart, lastWeekStart);
            
            List<WeeklyPrediction> lastWeekPredictions = weeklyPredictionRepository
                .findLastWeekPredictions(restaurantId, currentWeekStart);
            
            log.info("조회된 저번 주 예측 데이터 개수: {}", lastWeekPredictions.size());
            
            if (lastWeekPredictions.isEmpty()) {
                log.warn("저번 주 예측 데이터가 없습니다: restaurantId={}, lastWeekStart={}", 
                    restaurantId, lastWeekStart);
                return List.of();
            }

            // 예측 데이터를 날짜별로 매핑
            Map<LocalDate, WeeklyPrediction> predictionMap = new HashMap<>();
            for (WeeklyPrediction pred : lastWeekPredictions) {
                // weekday를 실제 날짜로 변환
                // weekday: 1=일요일, 2=월요일, ..., 7=토요일
                LocalDate date = lastWeekStart;
                int weekday = pred.getId().getWeekday();
                // 월요일(2)이 기준이므로, weekday가 1(일요일)이면 -1일, 2(월요일)이면 0일, ...
                int offset = weekday == 1 ? 6 : weekday - 2;
                date = date.plusDays(offset);
                predictionMap.put(date, pred);
            }

            // 저번 주의 모든 날짜에 대해 예측 데이터 생성
            List<WeeklyAiInsightsResponse.WeeklyPredictionPoint> points = new ArrayList<>();
            LocalDate cursor = lastWeekStart;
            for (int i = 0; i < 7; i++) {
                WeeklyPrediction pred = predictionMap.get(cursor);
                if (pred != null) {
                    int expectedAvg = (pred.getExpectedMin() + pred.getExpectedMax()) / 2;
                    points.add(WeeklyAiInsightsResponse.WeeklyPredictionPoint.builder()
                        .date(cursor.toString())
                        .expectedMin(pred.getExpectedMin())
                        .expectedMax(pred.getExpectedMax())
                        .expectedAvg(expectedAvg)
                        .build());
                }
                cursor = cursor.plusDays(1);
            }
            
            return points;
        } catch (Exception e) {
            log.warn("Failed to get last week predictions for restaurantId={}", restaurantId, e);
            return List.of();
        }
    }

    /**
     * evidence JSON 문자열을 List<String>으로 파싱
     */
    private List<String> parseEvidenceJson(String evidenceJson) {
        if (evidenceJson == null || evidenceJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(evidenceJson, 
                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse evidence JSON: {}", evidenceJson, e);
            return List.of();
        }
    }

    private static class PdfCursor {
        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream content;
        private final float margin;
        private final float minY;
        private float y;

        private PdfCursor(PDDocument document, PDPage page, float y, float margin, float minY) {
            this.document = document;
            this.page = page;
            this.y = y;
            this.margin = margin;
            this.minY = minY;
        }

        private void setContent(PDPageContentStream content) {
            this.content = content;
        }

        private PDPageContentStream getContent() {
            return content;
        }

        private float getY() {
            return y;
        }

        private void setY(float y) {
            this.y = y;
        }

        private void addY(float delta) {
            this.y += delta;
        }

        private float getMargin() {
            return margin;
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed >= minY) {
                return;
            }
            if (content != null) {
                content.close();
            }
            page = new PDPage();
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();
            y = mediaBox.getHeight() - margin;
            content = new PDPageContentStream(document, page);
            RestaurantStatsService.drawPageBackground(content, mediaBox);
        }
    }
}
