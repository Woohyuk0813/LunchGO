package com.example.LunchGo.cafeteria.service;

import com.example.LunchGo.cafeteria.dto.CafeteriaDayMenuDto;
import com.example.LunchGo.cafeteria.dto.CafeteriaMenuConfirmRequest;
import com.example.LunchGo.cafeteria.dto.CafeteriaMenuWeekResponse;
import com.example.LunchGo.cafeteria.dto.CafeteriaOcrResponse;
import com.example.LunchGo.cafeteria.entity.CafeteriaMenu;
import com.example.LunchGo.cafeteria.repository.CafeteriaMenuRepository;
import com.example.LunchGo.image.dto.ImageUploadResponse;
import com.example.LunchGo.image.service.ObjectStorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeteriaMenuService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final LocalTime FRIDAY_CUTOFF = LocalTime.NOON;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern KOREAN_DAY_PATTERN = Pattern.compile("^(월|화|수|목|금)(요일)?(\\s|:|$)");
    private static final Pattern ENGLISH_DAY_PATTERN = Pattern.compile(
        "^(MON|TUE|WED|THU|FRI)(DAY)?(\\s|:|$)",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern PAREN_DAY_PATTERN = Pattern.compile("\\((월|화|수|목|금)\\)");
    private static final Pattern DATE_LINE_PATTERN = Pattern.compile("^\\d{1,2}월(\\s*\\d{1,2}일)?$");
    private static final Pattern DAY_ONLY_LINE_PATTERN = Pattern.compile("^\\d{1,2}일$");
    private static final List<String> WEEK_DAYS = List.of("월", "화", "수", "목", "금");

    private static final Map<String, String> ENGLISH_DAY_MAP = Map.ofEntries(
        Map.entry("MON", "월"),
        Map.entry("TUE", "화"),
        Map.entry("WED", "수"),
        Map.entry("THU", "목"),
        Map.entry("FRI", "금")
    );

    private static final Map<String, String> MENU_SYNONYMS = Map.ofEntries(
        Map.entry("돈까스", "돈가스"),
        Map.entry("돈까쓰", "돈가스"),
        Map.entry("돈카츠", "돈가스"),
        Map.entry("제육볶음", "제육")
    );

    private final CafeteriaMenuRepository cafeteriaMenuRepository;
    private final ObjectStorageService objectStorageService;
    private final CafeteriaOcrService cafeteriaOcrService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CafeteriaOcrResponse recognizeMenus(Long userId, LocalDate baseDate, MultipartFile file) {
        objectStorageService.validateUpload(file);
        ImageUploadResponse uploadResponse = objectStorageService.upload("cafeteria", file);

        CafeteriaOcrService.OcrResult ocrResult = cafeteriaOcrService.recognizeMenu(file);
        String rawText = ocrResult.rawText();
        List<String> lines = ocrResult.lines();

        List<String> detectedMenus = normalizeMenus(lines.isEmpty() ? List.of(rawText) : lines);
        Map<String, List<String>> dayMenus = splitMenusByDay(lines);

        List<String> unassignedMenus = List.of();
        if (shouldFallbackDistribution(dayMenus)) {
            dayMenus = distributeMenusAcrossWeek(detectedMenus);
        } else if (dayMenus.isEmpty()) {
            unassignedMenus = detectedMenus;
        }

        List<CafeteriaDayMenuDto> dayDtos = buildWeekDays(baseDate, dayMenus);

        return new CafeteriaOcrResponse(
            ocrResult.success(),
            uploadResponse.getFileUrl(),
            uploadResponse.getKey(),
            rawText,
            detectedMenus,
            unassignedMenus,
            dayDtos
        );
    }

    public CafeteriaMenuWeekResponse saveWeeklyMenus(CafeteriaMenuConfirmRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }

        List<CafeteriaDayMenuDto> days = request.getDays();
        if (days == null) {
            days = List.of();
        }

        for (CafeteriaDayMenuDto day : days) {
            LocalDate servedDate = LocalDate.parse(day.getDate(), DATE_FORMATTER);
            String menuJson = toJson(day.getMenus());
            CafeteriaMenu menu = cafeteriaMenuRepository
                .findByUserIdAndServedDate(request.getUserId(), servedDate)
                .orElseGet(() -> CafeteriaMenu.builder()
                    .userId(request.getUserId())
                    .servedDate(servedDate)
                    .build());

            menu.updateMenus(menuJson, request.getRawText(), request.getImageUrl());
            cafeteriaMenuRepository.save(menu);
        }

        return new CafeteriaMenuWeekResponse(request.getImageUrl(), days);
    }

    public CafeteriaMenuWeekResponse getWeeklyMenus(Long userId, LocalDate baseDate) {
        LocalDate start = weekStart(baseDate);
        LocalDate end = start.plusDays(4);
        List<CafeteriaMenu> menus = cafeteriaMenuRepository.findByUserIdAndServedDateBetween(userId, start, end);

        Map<LocalDate, CafeteriaMenu> menuByDate = new LinkedHashMap<>();
        for (CafeteriaMenu menu : menus) {
            menuByDate.put(menu.getServedDate(), menu);
        }

        String imageUrl = menus.stream()
            .map(CafeteriaMenu::getImageUrl)
            .filter(url -> url != null && !url.isBlank())
            .findFirst()
            .orElse(null);

        List<CafeteriaDayMenuDto> dayDtos = new ArrayList<>();
        LocalDate cursor = start;
        for (int i = 0; i < 5; i++) {
            String dayLabel = toKoreanDay(cursor.getDayOfWeek());
            CafeteriaMenu stored = menuByDate.get(cursor);
            List<String> menusForDay = fromJson(stored != null ? stored.getMainMenuNames() : null);
            dayDtos.add(new CafeteriaDayMenuDto(dayLabel, cursor.format(DATE_FORMATTER), menusForDay));
            cursor = cursor.plusDays(1);
        }

        return new CafeteriaMenuWeekResponse(imageUrl, dayDtos);
    }

    private List<String> normalizeMenus(List<String> rawLines) {
        Set<String> normalized = new LinkedHashSet<>();
        if (rawLines == null) {
            return List.of();
        }

        for (String line : rawLines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            List<String> parts = splitCandidates(line);
            for (String part : parts) {
                String cleaned = cleanText(part);
                if (cleaned.isBlank()) {
                    continue;
                }
                normalized.add(applySynonyms(cleaned));
            }
        }

        return new ArrayList<>(normalized);
    }

    private Map<String, List<String>> splitMenusByDay(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> result = new LinkedHashMap<>();
        String currentDay = null;

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            String trimmed = line.trim();
            Matcher matcher = KOREAN_DAY_PATTERN.matcher(trimmed);
            if (matcher.find()) {
                currentDay = matcher.group(1);
                String remainder = trimmed.replaceFirst("^(월|화|수|목|금)(요일)?[:\\s-]*", "");
                addMenus(result, currentDay, remainder);
                continue;
            }

            Matcher englishMatcher = ENGLISH_DAY_PATTERN.matcher(trimmed);
            if (englishMatcher.find()) {
                currentDay = normalizeEnglishDay(englishMatcher.group(1));
                String remainder = trimmed.replaceFirst(
                    "^(MON|TUE|WED|THU|FRI)(DAY)?[:\\s-]*",
                    ""
                );
                addMenus(result, currentDay, remainder);
                continue;
            }

            Matcher parenMatcher = PAREN_DAY_PATTERN.matcher(trimmed);
            if (parenMatcher.find()) {
                currentDay = parenMatcher.group(1);
                continue;
            }

            if (currentDay != null) {
                if (isDateLine(trimmed)) {
                    continue;
                }
                addMenus(result, currentDay, trimmed);
            }
        }

        return result;
    }

    private void addMenus(Map<String, List<String>> result, String day, String text) {
        if (day == null || text == null) {
            return;
        }
        List<String> items = normalizeMenus(List.of(text));
        if (items.isEmpty()) {
            return;
        }
        result.computeIfAbsent(day, key -> new ArrayList<>()).addAll(items);
    }

    private List<CafeteriaDayMenuDto> buildWeekDays(LocalDate baseDate, Map<String, List<String>> dayMenus) {
        LocalDate start = weekStart(baseDate);
        List<CafeteriaDayMenuDto> dayDtos = new ArrayList<>();
        LocalDate cursor = start;

        for (int i = 0; i < 5; i++) {
            String dayLabel = toKoreanDay(cursor.getDayOfWeek());
            List<String> menus = dayMenus.getOrDefault(dayLabel, List.of());
            dayDtos.add(new CafeteriaDayMenuDto(dayLabel, cursor.format(DATE_FORMATTER), menus));
            cursor = cursor.plusDays(1);
        }

        return dayDtos;
    }

    private LocalDate weekStart(LocalDate baseDate) {
        LocalDate target = resolveWeekBaseDate(baseDate);
        return target.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private LocalDate resolveWeekBaseDate(LocalDate baseDate) {
        LocalDate today = LocalDate.now(KST);
        LocalDate target = baseDate != null ? baseDate : today;
        if (!target.equals(today)) {
            return target;
        }
        LocalDateTime now = LocalDateTime.now(KST);
        if (now.getDayOfWeek() == DayOfWeek.FRIDAY && !now.toLocalTime().isBefore(FRIDAY_CUTOFF)) {
            return target.plusWeeks(1);
        }
        return target;
    }

    private String toKoreanDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }

    private List<String> splitCandidates(String text) {
        String sanitized = text.replace("·", ",");
        String[] parts = sanitized.split("[,/]");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (part != null) {
                result.add(part.trim());
            }
        }
        return result;
    }

    private String cleanText(String text) {
        String cleaned = text.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ");
        cleaned = cleaned.replaceAll("\\d+", "");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned.toLowerCase(Locale.ROOT);
    }

    private String applySynonyms(String text) {
        String normalized = text;
        for (Map.Entry<String, String> entry : MENU_SYNONYMS.entrySet()) {
            normalized = normalized.replace(entry.getKey(), entry.getValue());
        }
        return normalized;
    }

    private String toJson(List<String> menus) {
        try {
            return objectMapper.writeValueAsString(menus == null ? List.of() : menus);
        } catch (JsonProcessingException e) {
            log.error("메뉴 JSON 변환 실패", e);
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            log.error("메뉴 JSON 파싱 실패", e);
            return List.of();
        }
    }

    private String normalizeEnglishDay(String day) {
        if (day == null) {
            return null;
        }
        String upper = day.toUpperCase(Locale.ROOT);
        return ENGLISH_DAY_MAP.getOrDefault(upper, upper);
    }

    private boolean isDateLine(String text) {
        if (text == null) {
            return false;
        }
        return DATE_LINE_PATTERN.matcher(text).matches()
            || DAY_ONLY_LINE_PATTERN.matcher(text).matches();
    }

    private boolean shouldFallbackDistribution(Map<String, List<String>> dayMenus) {
        if (dayMenus == null || dayMenus.isEmpty()) {
            return true;
        }
        if (dayMenus.size() >= 2) {
            return false;
        }
        int totalMenus = dayMenus.values().stream().mapToInt(List::size).sum();
        return totalMenus >= 5;
    }

    private Map<String, List<String>> distributeMenusAcrossWeek(List<String> detectedMenus) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (String day : WEEK_DAYS) {
            result.put(day, new ArrayList<>());
        }

        List<String> filteredMenus = detectedMenus == null
            ? List.of()
            : detectedMenus.stream()
                .filter(menu -> menu != null && !menu.isBlank())
                .filter(menu -> !isNoiseMenu(menu))
                .toList();

        if (filteredMenus.isEmpty()) {
            return result;
        }

        int total = filteredMenus.size();
        int chunkSize = (int) Math.ceil(total / (double) WEEK_DAYS.size());
        int index = 0;
        for (String day : WEEK_DAYS) {
            List<String> bucket = result.get(day);
            for (int i = 0; i < chunkSize && index < total; i++) {
                bucket.add(filteredMenus.get(index++));
            }
        }
        return result;
    }

    private boolean isNoiseMenu(String menu) {
        String trimmed = menu.trim();
        if (trimmed.isBlank()) {
            return true;
        }
        if (WEEK_DAYS.contains(trimmed)) {
            return true;
        }
        if (trimmed.matches("(?i)^(mon|tue|wed|thu|fri|menu)$")) {
            return true;
        }
        return trimmed.matches("^[a-zA-Z]{1,4}$");
    }
}
