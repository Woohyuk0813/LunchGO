package com.example.LunchGo.cafeteria.service;

import com.example.LunchGo.cafeteria.dto.CafeteriaDayMenuDto;
import com.example.LunchGo.cafeteria.dto.CafeteriaDayRecommendationDto;
import com.example.LunchGo.cafeteria.dto.CafeteriaMenuWeekResponse;
import com.example.LunchGo.cafeteria.dto.CafeteriaRecommendationResponse;
import com.example.LunchGo.cafeteria.dto.CafeteriaRestaurantRecommendationDto;
import com.example.LunchGo.cafeteria.dto.CafeteriaTagCountDto;
import com.example.LunchGo.cafeteria.repository.CafeteriaRestaurantProjection;
import com.example.LunchGo.cafeteria.repository.CafeteriaRestaurantRepository;
import com.example.LunchGo.member.dto.MemberInfo;
import com.example.LunchGo.member.dto.Speciality;
import com.example.LunchGo.member.mapper.MemberMapper;
import com.example.LunchGo.restaurant.repository.MenuRepository;
import com.example.LunchGo.tag.domain.TagCategory;
import com.example.LunchGo.tag.repository.SearchTagRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CafeteriaRecommendationService {

    private static final int DEFAULT_RECOMMENDATION_LIMIT = 2;
    private static final String NO_AVOID_MENU_MESSAGE =
        "기피 메뉴가 없으시군요? 그럼 이런 곳은 어때요?";
    private static final String NO_CAFETERIA_MENU_MESSAGE =
        "구내식당 운영을 안하니 여긴 어때요?";
    private static final List<String> PORK_INGREDIENTS = List.of("돼지고기");
    private static final List<String> BEEF_INGREDIENTS = List.of("소고기");
    private static final List<String> CHICKEN_INGREDIENTS = List.of("닭고기");
    private static final List<String> SEAFOOD_INGREDIENTS = List.of("해물", "해산물");
    private static final List<String> SHELLFISH_INGREDIENTS = List.of("조개류");
    private static final List<String> CRUSTACEAN_INGREDIENTS = List.of("갑각류");
    private static final List<String> FISH_INGREDIENTS = List.of("생선");
    private static final List<String> BUCKWHEAT_INGREDIENTS = List.of("메밀");
    private static final List<String> DAIRY_INGREDIENTS = List.of(
        "치즈",
        "유제품",
        "우유",
        "크림",
        "버터",
        "느끼한",
        "느끼한 음식"
    );
    private static final List<String> EGG_INGREDIENTS = List.of("계란");
    private static final List<String> FLOUR_INGREDIENTS = List.of("밀가루");
    private static final List<String> PEANUT_INGREDIENTS = List.of("땅콩", "견과류");
    private static final List<String> NUT_INGREDIENTS = List.of("견과류", "땅콩");
    private static final List<String> HERB_INGREDIENTS = List.of("고수");
    private static final List<String> CUCUMBER_INGREDIENTS = List.of("오이");
    private static final List<String> KIMCHI_INGREDIENTS = List.of("김치");
    private static final List<String> VEGAN_INGREDIENTS = List.of("비건");
    private static final List<String> SPICY_INGREDIENTS = List.of("매운맛", "매운", "매운 음식");
    private static final Map<String, List<String>> MENU_INGREDIENT_MAP = Map.ofEntries(
        Map.entry("제육", PORK_INGREDIENTS),
        Map.entry("돈가스", PORK_INGREDIENTS),
        Map.entry("삼겹", PORK_INGREDIENTS),
        Map.entry("목살", PORK_INGREDIENTS),
        Map.entry("보쌈", PORK_INGREDIENTS),
        Map.entry("돼지", PORK_INGREDIENTS),
        Map.entry("수육", PORK_INGREDIENTS),
        Map.entry("소고기", BEEF_INGREDIENTS),
        Map.entry("갈비", BEEF_INGREDIENTS),
        Map.entry("불고기", BEEF_INGREDIENTS),
        Map.entry("스테이크", BEEF_INGREDIENTS),
        Map.entry("닭", CHICKEN_INGREDIENTS),
        Map.entry("치킨", CHICKEN_INGREDIENTS),
        Map.entry("닭갈비", CHICKEN_INGREDIENTS),
        Map.entry("해물", SEAFOOD_INGREDIENTS),
        Map.entry("해산물", SEAFOOD_INGREDIENTS),
        Map.entry("새우", CRUSTACEAN_INGREDIENTS),
        Map.entry("게", CRUSTACEAN_INGREDIENTS),
        Map.entry("조개", SHELLFISH_INGREDIENTS),
        Map.entry("바지락", SHELLFISH_INGREDIENTS),
        Map.entry("굴", SHELLFISH_INGREDIENTS),
        Map.entry("오징어", SEAFOOD_INGREDIENTS),
        Map.entry("낙지", SEAFOOD_INGREDIENTS),
        Map.entry("생선", FISH_INGREDIENTS),
        Map.entry("고등어", FISH_INGREDIENTS),
        Map.entry("연어", FISH_INGREDIENTS),
        Map.entry("메밀", BUCKWHEAT_INGREDIENTS),
        Map.entry("치즈", DAIRY_INGREDIENTS),
        Map.entry("크림", DAIRY_INGREDIENTS),
        Map.entry("우유", DAIRY_INGREDIENTS),
        Map.entry("버터", DAIRY_INGREDIENTS),
        Map.entry("마요", DAIRY_INGREDIENTS),
        Map.entry("계란", EGG_INGREDIENTS),
        Map.entry("달걀", EGG_INGREDIENTS),
        Map.entry("밀", FLOUR_INGREDIENTS),
        Map.entry("빵", FLOUR_INGREDIENTS),
        Map.entry("면", FLOUR_INGREDIENTS),
        Map.entry("만두", FLOUR_INGREDIENTS),
        Map.entry("땅콩", PEANUT_INGREDIENTS),
        Map.entry("견과", NUT_INGREDIENTS),
        Map.entry("고수", HERB_INGREDIENTS),
        Map.entry("오이", CUCUMBER_INGREDIENTS),
        Map.entry("김치", KIMCHI_INGREDIENTS),
        Map.entry("비건", VEGAN_INGREDIENTS),
        Map.entry("샐러드", VEGAN_INGREDIENTS),
        Map.entry("매운", SPICY_INGREDIENTS),
        Map.entry("매운맛", SPICY_INGREDIENTS)
    );

    private final CafeteriaMenuService cafeteriaMenuService;
    private final CafeteriaRestaurantRepository cafeteriaRestaurantRepository;
    private final MenuRepository menuRepository;
    private final SearchTagRepository searchTagRepository;
    private final MemberMapper memberMapper;

    public CafeteriaRecommendationResponse recommend(Long userId, LocalDate baseDate) {
        return recommend(userId, baseDate, DEFAULT_RECOMMENDATION_LIMIT);
    }

    public CafeteriaRecommendationResponse recommend(Long userId, LocalDate baseDate, int limitPerDay) {
        CafeteriaMenuWeekResponse weekResponse = cafeteriaMenuService.getWeeklyMenus(userId, baseDate);
        List<CafeteriaDayMenuDto> days = weekResponse.getDays();

        List<String> dislikedKeywords = loadDislikedKeywords(userId);
        Set<Long> excludedRestaurantIds = loadExcludedRestaurantIds(dislikedKeywords);

        int candidateLimit = Math.max(limitPerDay * 10, 10);
        List<CafeteriaRestaurantProjection> allCandidateProjections = cafeteriaRestaurantRepository
            .findCandidateRestaurants(candidateLimit);
        List<CafeteriaRestaurantProjection> filteredProjections = allCandidateProjections
            .stream()
            .filter(projection -> !excludedRestaurantIds.contains(projection.getRestaurantId()))
            .toList();
        List<CafeteriaRestaurantProjection> candidateProjections = filteredProjections.isEmpty()
            ? allCandidateProjections
            : filteredProjections;
        List<CafeteriaRestaurantRecommendationDto> candidates = candidateProjections
            .stream()
            .map(this::toRecommendation)
            .toList();

        List<CafeteriaDayRecommendationDto> recommendations = new ArrayList<>();
        for (int index = 0; index < days.size(); index++) {
            CafeteriaDayMenuDto day = days.get(index);
            if (day.getMenus() == null || day.getMenus().isEmpty()) {
                List<CafeteriaRestaurantRecommendationDto> selected =
                    pickRandomCheapestCandidates(candidateProjections, limitPerDay);
                recommendations.add(new CafeteriaDayRecommendationDto(
                    day.getDay(),
                    day.getDate(),
                    NO_CAFETERIA_MENU_MESSAGE,
                    selected
                ));
                continue;
            }
            String avoidMenu = buildAvoidMenu(day.getMenus(), dislikedKeywords);
            List<CafeteriaRestaurantRecommendationDto> selected;
            if ("-".equals(avoidMenu)) {
                selected = pickRandomCheapestCandidates(candidateProjections, limitPerDay);
                avoidMenu = NO_AVOID_MENU_MESSAGE;
            } else {
                selected = pickRecommendations(candidates, limitPerDay, index);
            }
            recommendations.add(new CafeteriaDayRecommendationDto(day.getDay(), day.getDate(), avoidMenu, selected));
        }

        return new CafeteriaRecommendationResponse(recommendations);
    }

    private List<String> loadDislikedKeywords(Long userId) {
        MemberInfo memberInfo = memberMapper.selectUser(userId);
        if (memberInfo == null || memberInfo.getSpecialities() == null) {
            return List.of();
        }
        return memberInfo.getSpecialities().stream()
            .filter(speciality -> !speciality.isLiked())
            .map(Speciality::getKeyword)
            .filter(keyword -> keyword != null && !keyword.isBlank())
            .map(String::trim)
            .distinct()
            .toList();
    }

    private Set<Long> loadExcludedRestaurantIds(List<String> dislikedKeywords) {
        if (dislikedKeywords == null || dislikedKeywords.isEmpty()) {
            return Set.of();
        }

        Set<Long> excluded = new HashSet<>();
        for (String keyword : dislikedKeywords) {
            excluded.addAll(menuRepository.findRestaurantIdsByMenuNameContaining(keyword));
        }

        List<Long> tagIds = searchTagRepository
            .findByContentInAndCategory(dislikedKeywords, TagCategory.INGREDIENT)
            .stream()
            .map(tag -> tag.getTagId())
            .toList();
        if (!tagIds.isEmpty()) {
            excluded.addAll(menuRepository.findRestaurantIdsByMenuTagIds(tagIds));
        }

        return excluded;
    }

    private CafeteriaRestaurantRecommendationDto toRecommendation(CafeteriaRestaurantProjection projection) {
        String address = joinAddress(projection.getRoadAddress(), projection.getDetailAddress());
        String price = projection.getAvgMainPrice() != null
            ? String.format("1인 %,d원대", projection.getAvgMainPrice())
            : "가격 정보 없음";
        List<CafeteriaTagCountDto> tags = parseReviewTags(
            projection.getReviewTagContents(),
            projection.getReviewTagCounts(),
            projection.getTagContents()
        );

        return new CafeteriaRestaurantRecommendationDto(
            String.valueOf(projection.getRestaurantId()),
            projection.getName(),
            address,
            price,
            projection.getRating(),
            projection.getReviewCount(),
            projection.getImageUrl(),
            tags
        );
    }

    private String joinAddress(String road, String detail) {
        if (detail == null || detail.isBlank()) {
            return road;
        }
        return road + " " + detail;
    }

    private List<CafeteriaTagCountDto> parseReviewTags(
        String reviewTagContents,
        String reviewTagCounts,
        String fallbackTagContents
    ) {
        if (reviewTagContents != null && !reviewTagContents.isBlank()) {
            String[] names = reviewTagContents.split(",");
            String[] counts = reviewTagCounts != null ? reviewTagCounts.split(",") : new String[0];
            List<CafeteriaTagCountDto> tags = new ArrayList<>();
            for (int i = 0; i < names.length && i < 3; i++) {
                String name = names[i].trim();
                if (name.isBlank()) {
                    continue;
                }
                int count = 1;
                if (i < counts.length) {
                    try {
                        count = Integer.parseInt(counts[i].trim());
                    } catch (NumberFormatException ignored) {
                        count = 1;
                    }
                }
                tags.add(new CafeteriaTagCountDto(name, count));
            }
            return tags;
        }
        return parseFallbackTags(fallbackTagContents);
    }

    private List<CafeteriaTagCountDto> parseFallbackTags(String tagContents) {
        if (tagContents == null || tagContents.isBlank()) {
            return List.of();
        }
        String[] parts = tagContents.split(",");
        List<CafeteriaTagCountDto> tags = new ArrayList<>();
        for (int i = 0; i < parts.length && i < 3; i++) {
            String name = parts[i].trim();
            if (!name.isBlank()) {
                tags.add(new CafeteriaTagCountDto(name, 1));
            }
        }
        return tags;
    }

    private String buildAvoidMenu(List<String> menus, List<String> dislikedKeywords) {
        if (menus == null || menus.isEmpty()) {
            return "-";
        }
        if (dislikedKeywords == null || dislikedKeywords.isEmpty()) {
            return "-";
        }

        String matched = menus.stream()
            .filter(menu -> menu != null && !menu.isBlank())
            .filter(menu -> containsAnyKeyword(menu, dislikedKeywords))
            .collect(Collectors.joining(", "));
        return matched.isBlank() ? "-" : matched;
    }

    private List<CafeteriaRestaurantRecommendationDto> pickRandomCheapestCandidates(
        List<CafeteriaRestaurantProjection> candidates,
        int limit
    ) {
        if (limit <= 0 || candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        List<CafeteriaRestaurantProjection> priced = candidates.stream()
            .filter(candidate -> candidate.getAvgMainPrice() != null)
            .sorted((a, b) -> comparePrice(a.getAvgMainPrice(), b.getAvgMainPrice()))
            .toList();
        List<CafeteriaRestaurantProjection> pool = priced.isEmpty()
            ? new ArrayList<>(candidates)
            : new ArrayList<>(priced);
        int poolSize = Math.min(pool.size(), Math.max(limit * 5, 5));
        pool = new ArrayList<>(pool.subList(0, poolSize));
        java.util.Collections.shuffle(pool);
        return pool.stream()
            .limit(limit)
            .map(this::toRecommendation)
            .toList();
    }

    private int comparePrice(Integer left, Integer right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return Integer.compare(left, right);
    }

    private boolean containsAnyKeyword(String menu, List<String> keywords) {
        String lowered = menu.toLowerCase();
        for (String keyword : keywords) {
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            String keywordLower = keyword.toLowerCase();
            if (lowered.contains(keywordLower)) {
                return true;
            }
            if (isIngredientMatch(lowered, keywordLower)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIngredientMatch(String menuLower, String keywordLower) {
        for (Map.Entry<String, List<String>> entry : MENU_INGREDIENT_MAP.entrySet()) {
            String menuKey = entry.getKey().toLowerCase();
            if (!menuLower.contains(menuKey)) {
                continue;
            }
            for (String ingredient : entry.getValue()) {
                String ingredientLower = ingredient.toLowerCase();
                if (keywordLower.contains(ingredientLower) || ingredientLower.contains(keywordLower)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<CafeteriaRestaurantRecommendationDto> pickRecommendations(
        List<CafeteriaRestaurantRecommendationDto> candidates,
        int limit,
        int dayIndex
    ) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        if (limit <= 0) {
            return List.of();
        }

        List<CafeteriaRestaurantRecommendationDto> result = new ArrayList<>();
        int startIndex = (dayIndex * limit) % candidates.size();
        for (int i = 0; i < limit; i++) {
            int index = (startIndex + i) % candidates.size();
            result.add(candidates.get(index));
        }
        return result;
    }
}
