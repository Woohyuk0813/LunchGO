<script setup>
import {
  ref,
  computed,
  watch,
  onMounted,
  onBeforeUnmount,
  reactive,
  nextTick,
} from "vue";
import {
  MapPin,
  Calendar,
  Star,
  X,
  Minus,
  Plus,
  ChevronLeft,
  ChevronRight,
} from "lucide-vue-next"; // Import Lucide icons for Vue
import Button from "@/components/ui/Button.vue"; // Import our custom Button component
import AppFooter from "@/components/ui/AppFooter.vue";
import BottomNav from "@/components/ui/BottomNav.vue";
import { RouterLink, useRouter } from "vue-router"; // Import Vue RouterLink
import { geocodeAddress } from "@/utils/kakao";
import { restaurants as restaurantData, loadRestaurants } from "@/data/restaurants";
import AppHeader from "@/components/ui/AppHeader.vue";
import HomeSearchBar from "@/components/ui/HomeSearchBar.vue";
import HomeRecommendationContent from "@/components/ui/HomeRecommendationContent.vue";
import HomeRecommendationHeader from "@/components/ui/HomeRecommendationHeader.vue";
import HomePagination from "@/components/ui/HomePagination.vue";
import MapMarkerLegendIcon from "@/components/ui/MapMarkerLegendIcon.vue";
import { useCafeteriaRecommendation } from "@/composables/useCafeteriaRecommendation";
import { useTrendingRestaurants } from "@/composables/useTrendingRestaurants";
import { useBudgetRecommendation, extractPriceValue } from "@/composables/useBudgetRecommendation";
import { useTagMappingRecommendation } from "@/composables/useTagMappingRecommendation";
import { useWeatherRecommendation } from "@/composables/useWeatherRecommendation";
import { useHomeRecommendations } from "@/composables/useHomeRecommendations";
import { useHomePersistence } from "@/composables/useHomePersistence";
import { useHomeMap } from "@/composables/useHomeMap";
import { useAccountStore } from "@/stores/account";
import { useFavorites } from "@/composables/useFavorites";
import { useBookmarkShare } from "@/composables/useBookmarkShare";
import {
  formatRouteDistance,
  formatRouteDurationDetailed,
} from "@/utils/formatters";
import axios from "axios";
import httpRequest from "@/router/httpRequest.js";

const accountStore = useAccountStore();
const router = useRouter();
const isLoggedIn = computed(() =>
    Boolean(accountStore.accessToken || localStorage.getItem("accessToken"))
);
const { fetchFavorites, clearFavorites, userId, favoriteRestaurantIds } = useFavorites();
const { getSentLinks, getReceivedLinks, getSharedBookmarks } = useBookmarkShare();

const getStoredMember = () => {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("member");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());
const memberId = computed(() => {
  const rawId = member.value?.id ?? member.value?.userId ?? member.value?.memberId;
  if (rawId === null || rawId === undefined) return null;
  const parsed = Number(rawId);
  return Number.isNaN(parsed) ? null : parsed;
});

const favoriteIdSet = computed(
  () => new Set(favoriteRestaurantIds.value.map((id) => Number(id)))
);
const sharedFavoriteRestaurantIds = ref([]);
const sharedFavoriteNameMap = ref({});
const sharedFavoriteIdSet = computed(
  () => new Set(sharedFavoriteRestaurantIds.value.map((id) => Number(id)))
);
const getSharedFavoriteLabel = (restaurantId) => {
  const names = sharedFavoriteNameMap.value?.[Number(restaurantId)];
  if (!names || !names.length) return "";
  if (names.length === 1) return names[0];
  return `${names[0]} 외 ${names.length - 1}명`;
};

// State management (React's useState -> Vue's ref)
const isFilterOpen = ref(false);
const DEFAULT_SORT = "가나다순";
const selectedSort = ref(DEFAULT_SORT);
const selectedPriceRange = ref(null);
const selectedRecommendation = ref(null);
const {
  cafeteriaRecommendations,
  cafeteriaOcrResult,
  cafeteriaOcrError,
  cafeteriaDaysDraft,
  cafeteriaImageUrl: cafeteriaImageUrlRef,
  isCafeteriaOcrLoading,
  isCafeteriaModalOpen,
  hasConfirmedMenus,
  isCheckingMenus,
  loadRecommendationsFromStorage,
  clearCafeteriaRecommendations,
  handleCafeteriaFileChange,
  handleCafeteriaOcr,
  handleCafeteriaConfirm,
  checkCafeteriaMenuStatus,
  openCafeteriaModal,
  openCafeteriaModalWithExisting,
  requestCafeteriaRecommendations,
} = useCafeteriaRecommendation({ userId: memberId });
const cafeteriaImageUrl = computed(() => cafeteriaImageUrlRef.value);
const searchStateStorageKey = "homeSearchState";
const searchQuery = ref("");
const filterForm = reactive({
  sort: selectedSort.value,
  priceRange: selectedPriceRange.value,
  recommendation: null,
});
const filterBudget = ref(60000);
const filterPartySize = ref(4);
const filterBudgetMin = 0;
const filterBudgetMax = 500000;
const filterBudgetStep = 10000;
const { loadHomeListState, saveHomeListState, clearHomeListState } =
  useHomePersistence();
const filterBudgetPercent = computed(() => {
  if (filterBudgetMax <= filterBudgetMin) return 0;
  const clamped = Math.min(
      Math.max(filterBudget.value, filterBudgetMin),
      filterBudgetMax
  );
  return ((clamped - filterBudgetMin) / (filterBudgetMax - filterBudgetMin)) * 100;
});
const filterBudgetDisplay = computed(() => {
  if (filterBudget.value >= filterBudgetMax) {
    return "50만원 이상";
  }
  if (filterBudget.value <= 0) {
    return "0원";
  }
  return `${Math.round(filterBudget.value / 10000)}만원`;
});
const filterPerPersonBudget = computed(() => {
  if (!filterPartySize.value || filterPartySize.value <= 0) {
    return 0;
  }
  return Math.floor(filterBudget.value / filterPartySize.value);
});
const filterPerPersonBudgetDisplay = computed(() => {
  if (!filterPerPersonBudget.value) {
    return "0원";
  }
  return `${filterPerPersonBudget.value.toLocaleString()}원`;
});

const isSearchOpen = ref(false);
const searchModalScrollRef = ref(null);
const searchResultIds = ref(null);
const searchDate = ref("");
const searchTime = ref("");
const searchCategories = ref([]);
const searchPartySize = ref(4);
const searchPreorder = ref(false);
const searchTags = ref([]);
const avoidIngredients = ref([]);
const searchDistance = ref("");
const searchRestaurantIds = ref(null);
const isSearchLoading = ref(false);
const searchRestaurantError = ref("");
const categories = ref([]);
const restaurantTags = ref([]);
const ingredients = ref([]);
const hasAutoRefreshedCafeteria = ref(false);
const isBudgetLoading = ref(false);
const isCafeteriaLoading = ref(false);
const ingredientEmojiMap = {
  계란: "🥚",
  우유: "🥛",
  유제품: "🥛",
  치즈: "🧀",
  땅콩: "🥜",
  견과류: "🥜",
  아몬드: "🥜",
  호두: "🥜",
  대두: "🫘",
  콩: "🫘",
  밀: "🌾",
  메밀: "🌾",
  새우: "🦐",
  게: "🦀",
  갑각류: "🦐",
  오징어: "🦑",
  생선: "🐟",
  조개: "🐚",
  굴: "🦪",
  토마토: "🍅",
  복숭아: "🍑",
  닭: "🍗",
  돼지: "🐷",
  소고기: "🥩",
  고수: "🌿",
  오이: "🥒",
  비건: "🥗",
  매운: "🌶️",
  매운맛: "🌶️",
};

const getIngredientEmoji = (ingredient) => {
  if (!ingredient) return "";
  const match = Object.entries(ingredientEmojiMap).find(([key]) =>
    ingredient.includes(key)
  );
  return match ? match[1] : "";
};

const restaurantTagEmojiMap = {
  단체: "👥",
  회식: "🍻",
  모임: "🧑‍🤝‍🧑",
  혼밥: "🍽️",
  가족: "👨‍👩‍👧‍👦",
  데이트: "💕",
  뷰: "🌆",
  경치: "🌆",
  분위기: "🕯️",
  조용: "🤫",
  활기: "🎉",
  룸: "🚪",
  프라이빗: "🔒",
  와인: "🍷",
  주류: "🍺",
  가성비: "💸",
  예약: "📌",
  주차: "🅿️",
  포장: "🥡",
  배달: "🛵",
  반려동물: "🐾",
  키즈: "🧒",
  휠체어: "♿",
  장애인: "♿",
  콜키지: "🍷",
  와이파이: "📶",
  WiFi: "📶",
  와이파이존: "📶",
  "24시간": "🕒",
};

const getRestaurantTagEmoji = (tag) => {
  if (!tag) return "";
  const match = Object.entries(restaurantTagEmojiMap).find(([key]) =>
    tag.includes(key)
  );
  return match ? match[1] : "";
};

const fetchSearchTags = async () => {
  try {
    const response = await httpRequest.get("/api/tags/search?categories=MENUTYPE,TABLETYPE,ATMOSPHERE,FACILITY,INGREDIENT");
    const data = response.data;
    if (data && data.MENUTYPE) {
      categories.value = data.MENUTYPE.map((tag) => tag.content);
    }
    const newRestaurantTags = [];
    if (data && data.TABLETYPE) {
      newRestaurantTags.push(...data.TABLETYPE.map((tag) => tag.content));
    }
    if (data && data.ATMOSPHERE) {
      newRestaurantTags.push(...data.ATMOSPHERE.map((tag) => tag.content));
    }
    if (data && data.FACILITY) {
      newRestaurantTags.push(...data.FACILITY.map((tag) => tag.content));
    }
    restaurantTags.value = newRestaurantTags;
    if (data && data.INGREDIENT) {
      ingredients.value = data.INGREDIENT.map((tag) => tag.content);
    }
  } catch (error) {
    console.error("태그를 불러오는 데 실패했습니다:", error);
  }
};

const fetchUserAddress = async () => {
  const userId = memberId.value;
  if (!userId) return null;
  try {
    const response = await httpRequest.get(`/api/info/user/${userId}`);
    const data = response.data;
    return data?.companyAddress || null;
  } catch (error) {
    return null;
  }
};

const {
  isTrendingLoading,
  trendingRestaurants,
  trendingError,
  fetchTrendingRestaurants,
  clearTrendingRestaurants,
} = useTrendingRestaurants();
const { budgetRecommendations, fetchBudgetRecommendations, clearBudgetRecommendations } =
    useBudgetRecommendation();
const {
  isTagMappingLoading,
  tagMappingRecommendations,
  tagMappingError,
  tagMappingMessageCode,
  fetchTagMappingRecommendations,
  clearTagMappingRecommendations,
} = useTagMappingRecommendation();
const {
  isWeatherLoading,
  weatherRecommendations,
  weatherError,
  weatherSummary,
  fetchWeatherRecommendations,
  clearWeatherRecommendations,
} = useWeatherRecommendation();
const isGeocodeExportMode = ref(false);
const isGeocodeExporting = ref(false);
const geocodeExportProgress = ref({ done: 0, total: 0 });
const geocodeExportMissing = ref([]);

const selectedDistanceKm = computed(() => {
  if (!searchDistance.value) return null;
  const kmMatch = searchDistance.value.match(/([\d.]+)\s*km/);
  if (kmMatch) return Number(kmMatch[1]);
  return null;
});

const restaurantGeocodeCache = new Map();

const handleMapMarkerClick = (restaurants = []) => {
  if (!restaurants.length) return;
  restaurants.forEach((restaurant) => {
    fetchRestaurantImage(restaurant.id);
  });
  updateSelectedMapRestaurants(restaurants);
  restaurants.forEach((restaurant) => {
    ensureReviewSummary(restaurant);
  });
};

async function resolveRestaurantCoords(restaurant) {
  if (restaurant.coords?.lat && restaurant.coords?.lng) {
    return restaurant.coords;
  }

  const cacheKey = restaurant.address?.trim() || restaurant.name;
  if (!cacheKey) {
    return null;
  }

  if (restaurantGeocodeCache.has(cacheKey)) {
    return restaurantGeocodeCache.get(cacheKey);
  }

  const rawAddress = restaurant.address?.trim();
  const candidates = [];
  if (rawAddress) {
    candidates.push(rawAddress);
    const withoutParens = rawAddress.split(" (")[0].trim();
    if (withoutParens && withoutParens !== rawAddress) {
      candidates.push(withoutParens);
    }
    const withoutSemicolon = rawAddress.split(";")[0].trim();
    if (withoutSemicolon && !candidates.includes(withoutSemicolon)) {
      candidates.push(withoutSemicolon);
    }
  }

  try {
    for (const candidate of candidates) {
      try {
        const coords = await geocodeAddress(candidate);
        restaurantGeocodeCache.set(cacheKey, coords);
        restaurant.coords = coords;
        return coords;
      } catch (candidateError) {
        continue;
      }
    }
    throw new Error("geocode-failed");
  } catch (error) {
    console.error("주소 지오코딩 실패:", cacheKey, error);
    return null;
  }
}

const {
  mapContainer,
  mapCenter,
  currentLocation,
  currentDistanceLabel,
  distanceSliderFill,
  changeMapDistance,
  resetMapToHome,
  initializeMap,
  applyUserMapCenter,
  isWithinDistance,
  calculateDistanceKm,
  drawRoute,
  clearRoute,
  setRouteFocus,
  clearRouteFocus,
} = useHomeMap({
  isLoggedIn,
  fetchUserAddress,
  geocodeAddress,
  isSearchOpen,
  selectedDistanceKm,
  resolveRestaurantCoords,
  onMarkerClick: handleMapMarkerClick,
  favoriteIdSet,
  sharedFavoriteIdSet,
  sharedFavoriteNameMap,
  restaurants: restaurantData,
});

const RECOMMEND_CAFETERIA = "\uAD6C\uB0B4\uC2DD\uB2F9 \uB300\uCCB4 \uD83C\uDF71";
const RECOMMEND_BUDGET = "\uC608\uC0B0 \uB9DE\uCDA4 \uD83D\uDCB0";
const RECOMMEND_TASTE = "\uCDE8\uD5A5 \uB9DE\uCDA4 \uD83D\uDE0B";
const RECOMMEND_TRENDING = "\uC778\uAE30\uC21C \uD83D\uDD25";
const RECOMMEND_WEATHER = "\uB0A0\uC528 \uCD94\uCC9C \u2600\uFE0F";
const TAG_MESSAGE_LOGIN = "\uB85C\uADF8\uC778\uC774 \uD544\uC694\uD569\uB2C8\uB2E4";
const TAG_MESSAGE_SPECIALITY = "\uD2B9\uC774\uC0AC\uD56D\uC744 \uBA3C\uC800 \uB4F1\uB85D\uD574\uC8FC\uC138\uC694";
const TAG_MESSAGE_LOADING = "\uCD94\uCC9C \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uB294 \uC911\uC785\uB2C8\uB2E4";
const TAG_MESSAGE_ERROR = "\uCD94\uCC9C \uC815\uBCF4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4";

const restaurants = restaurantData;
const activeRestaurantSource = computed(() => {
  if (selectedRecommendation.value === RECOMMEND_BUDGET) {
    return budgetRecommendations.value;
  }
  if (selectedRecommendation.value === RECOMMEND_WEATHER) {
    return weatherRecommendations.value;
  }
  if (selectedRecommendation.value === RECOMMEND_TASTE) {
    return tagMappingRecommendations.value;
  }
  return restaurants;
});
const tagMappingNotice = computed(() => {
  if (selectedRecommendation.value !== RECOMMEND_TASTE) {
    return "";
  }
  if (isTagMappingLoading.value) {
    return TAG_MESSAGE_LOADING;
  }
  if (tagMappingMessageCode.value === "LOGIN_REQUIRED") {
    return TAG_MESSAGE_LOGIN;
  }
  if (tagMappingMessageCode.value === "SPECIALITY_REQUIRED") {
    return "";
  }
  if (tagMappingError.value) {
    return "";
  }
  return "";
});

const goToSpeciality = () => {
  router.push({ path: "/mypage", hash: "#speciality-section" });
};


const tasteRecommendationSummary = computed(() => {
  if (selectedRecommendation.value !== RECOMMEND_TASTE) {
    return "";
  }
  const count = tagMappingRecommendations.value.length;
  if (!count) {
    return "";
  }
  return `상위 ${count}곳`;
});
const weatherThemeMap = {
  hot: {
    wrapperClass:
      "bg-gradient-to-br from-[#ffd6a5] via-[#ffb56b] to-[#ff8c5a] border border-[#ff9a76]",
    accentClass: "text-[#9a3412]",
    emoji: "🔥",
    label: "더운 날씨",
  },
  cold: {
    wrapperClass:
      "bg-gradient-to-br from-[#dbeafe] via-[#c7d2fe] to-[#b8c7ff] border border-[#a5b4fc]",
    accentClass: "text-[#1e40af]",
    emoji: "🥶",
    label: "추운 날씨",
  },
  rain: {
    wrapperClass:
      "bg-gradient-to-br from-[#dbeafe] via-[#c7d2fe] to-[#b8c7ff] border border-[#a5b4fc]",
    accentClass: "text-[#1e3a8a]",
    emoji: "🌧️",
    label: "비 오는 날씨",
  },
  snow: {
    wrapperClass:
      "bg-gradient-to-br from-[#eef2ff] via-[#e0f2fe] to-[#cfe7ff] border border-[#c7d2fe]",
    accentClass: "text-[#1e40af]",
    emoji: "❄️",
    label: "눈 오는 날씨",
  },
};

const loadSharedFavoriteRestaurants = async () => {
  if (!userId.value) {
    sharedFavoriteRestaurantIds.value = [];
    return;
  }
  try {
    const [sentResponse, receivedResponse] = await Promise.all([
      getSentLinks(userId.value, "APPROVED"),
      getReceivedLinks(userId.value, "APPROVED"),
    ]);
    const sentLinks = Array.isArray(sentResponse.data) ? sentResponse.data : [];
    const receivedLinks = Array.isArray(receivedResponse.data)
      ? receivedResponse.data
      : [];
    const counterpartMap = new Map(
      [...sentLinks, ...receivedLinks]
        .map((link) => [
          link.counterpartId,
          link.counterpartNickname || link.counterpartName || link.counterpartEmail,
        ])
        .filter(([id, name]) => id !== null && id !== undefined && name)
    );
    const counterpartIds = Array.from(
      new Set(
        [...sentLinks, ...receivedLinks]
          .map((link) => link.counterpartId)
          .filter((id) => id !== null && id !== undefined)
      )
    );

    if (!counterpartIds.length) {
      sharedFavoriteRestaurantIds.value = [];
      return;
    }

    const sharedResponses = await Promise.all(
      counterpartIds.map((counterpartId) =>
        getSharedBookmarks(userId.value, counterpartId).catch(() => ({ data: [] }))
      )
    );
    const nameMap = {};
    const sharedIds = sharedResponses.flatMap((response, index) => {
      const data = Array.isArray(response.data) ? response.data : [];
      const counterpartId = counterpartIds[index];
      const counterpartName = counterpartMap.get(counterpartId);
      data.forEach((item) => {
        const id = Number(item.restaurantId);
        if (!Number.isFinite(id) || !counterpartName) return;
        if (!nameMap[id]) {
          nameMap[id] = [counterpartName];
          return;
        }
        if (!nameMap[id].includes(counterpartName)) {
          nameMap[id].push(counterpartName);
        }
      });
      return data.map((item) => Number(item.restaurantId)).filter(Number.isFinite);
    });
    sharedFavoriteRestaurantIds.value = Array.from(new Set(sharedIds));
    sharedFavoriteNameMap.value = nameMap;
  } catch (error) {
    sharedFavoriteRestaurantIds.value = [];
    sharedFavoriteNameMap.value = {};
  }
};
const weatherTheme = computed(() => {
  if (selectedRecommendation.value !== RECOMMEND_WEATHER) return null;
  const summary = weatherSummary.value;
  if (!summary) return null;
  const condition = String(summary.condition || "").toLowerCase();
  if (["rain", "drizzle", "thunderstorm"].includes(condition)) return "rain";
  if (condition === "snow") return "snow";
  const temp = Number.isFinite(summary.temp)
    ? summary.temp
    : summary.feelsLike;
  if (Number.isFinite(temp) && temp <= 7) return "cold";
  if (Number.isFinite(temp) && temp >= 28) return "hot";
  return null;
});
const weatherThemeStyle = computed(() => {
  if (!weatherTheme.value) return null;
  return weatherThemeMap[weatherTheme.value] || weatherThemeMap.cold;
});
const formatTemp = (value) =>
  Number.isFinite(value) ? `${Math.round(value)}°` : "--°";
const weatherDisplayLabel = computed(() => weatherThemeStyle.value?.label || "");
const formatRouteDuration = formatRouteDurationDetailed;
const isRecommendationLoading = computed(() => {
  if (isHomeListRestoring.value) {
    return isScoreSort(selectedSort.value);
  }
  if (isScoreSort(selectedSort.value) && isRatingSortPending.value) {
    return true;
  }
  if (selectedRecommendation.value === RECOMMEND_WEATHER) {
    return isWeatherLoading.value;
  }
  if (selectedRecommendation.value === RECOMMEND_TASTE) {
    return isTagMappingLoading.value;
  }
  if (selectedRecommendation.value === RECOMMEND_BUDGET) {
    return isBudgetLoading.value;
  }
  if (!selectedRecommendation.value) {
    return isRatingSortPending.value;
  }
  return false;
});
const activeRecommendationHeader = computed(() => {
  if (
    selectedRecommendation.value === RECOMMEND_CAFETERIA ||
    cafeteriaRecommendations.value.length ||
    isCafeteriaLoading.value
  ) {
    return {
      title: "구내식당 대체 추천",
      onClear: () => clearRecommendation(RECOMMEND_CAFETERIA),
    };
  }
  if (isTrendingSort.value) {
    return {
      title: "이달의 회식 맛집 추천",
      onClear: clearTrendingRecommendation,
      isLoading: isTrendingLoading.value,
    };
  }
  if (selectedRecommendation.value === RECOMMEND_WEATHER) {
    return {
      title: "날씨 추천",
      onClear: () => clearRecommendation(RECOMMEND_WEATHER),
    };
  }
  if (selectedRecommendation.value === RECOMMEND_TASTE) {
    return {
      title: "취향 맞춤 추천",
      subtitle: tasteRecommendationSummary.value,
      description:
        "나와 팀원의 특이사항 태그를 기반으로 매칭 점수가 높은 식당을 골랐어요.",
      onClear: () => clearRecommendation(RECOMMEND_TASTE),
    };
  }
  if (selectedRecommendation.value === RECOMMEND_BUDGET) {
    return {
      title: "예산 맞춤 추천",
      subtitle: `1인당 ${filterPerPersonBudgetDisplay.value}`,
      onClear: () => clearRecommendation(RECOMMEND_BUDGET),
    };
  }
  return null;
});

const restaurantsPerPage = 10;
const currentPage = ref(1);
const routeInfo = ref(null);
const routeError = ref("");
const routeLoadingId = ref(null);
const isTrendingSort = computed(() => selectedRecommendation.value === RECOMMEND_TRENDING);
const restaurantIndexById = new Map(
    restaurants.map((restaurant) => [String(restaurant.id), restaurant])
);
const restaurantImageCache = new Map();
const restaurantImageOverrides = ref({});
const reviewSummaryCache = ref({});
const reviewSummarySortCache = ref({});
const reviewSummaryInFlight = new Set();
const isRatingSortPending = ref(false);
const ratingSortPendingIds = new Set();
const isScoreSort = (value) => value === "평점순" || value === "추천순";
const searchRestaurantIdSet = computed(() => {
  if (!Array.isArray(searchRestaurantIds.value)) return null;
  return new Set(searchRestaurantIds.value.map((id) => String(id)));
});

const applyReviewSummary = (restaurant) => {
  const summary = reviewSummaryCache.value[String(restaurant.id)];
  if (!summary) return restaurant;
  return {
    ...restaurant,
    rating: summary.rating ?? restaurant.rating,
    reviews: summary.reviews ?? restaurant.reviews,
  };
};
const getSortRating = (restaurant) => {
  const summary = reviewSummaryCache.value[String(restaurant?.id)];
  if (summary && summary.rating != null) {
    return summary.rating;
  }
  return restaurant?.rating ?? 0;
};
const getSortRatingSnapshot = (restaurant) => {
  const summary = reviewSummarySortCache.value[String(restaurant?.id)];
  if (summary && summary.rating != null) {
    return summary.rating;
  }
  return restaurant?.rating ?? 0;
};
const getSortReviewCount = (restaurant) => {
  const summary = reviewSummaryCache.value[String(restaurant?.id)];
  if (summary && summary.reviews != null) {
    return summary.reviews;
  }
  return restaurant?.reviews ?? 0;
};
const getSortReviewCountSnapshot = (restaurant) => {
  const summary = reviewSummarySortCache.value[String(restaurant?.id)];
  if (summary && summary.reviews != null) {
    return summary.reviews;
  }
  return restaurant?.reviews ?? 0;
};
const getSortRecommendScore = (restaurant) => {
  const rating = getSortRating(restaurant);
  const reviews = getSortReviewCount(restaurant);
  return rating * 20 + Math.log10(reviews + 1) * 10;
};
const getSortRecommendScoreSnapshot = (restaurant) => {
  const rating = getSortRatingSnapshot(restaurant);
  const reviews = getSortReviewCountSnapshot(restaurant);
  return rating * 20 + Math.log10(reviews + 1) * 10;
};
const getSortId = (restaurant) => {
  const value = Number(restaurant?.id);
  return Number.isFinite(value) ? value : 0;
};
const syncReviewSummarySortCache = () => {
  reviewSummarySortCache.value = { ...reviewSummaryCache.value };
};
let reviewSummarySortSyncTimer = null;
const scheduleReviewSummarySortSync = () => {
  if (reviewSummarySortSyncTimer) {
    clearTimeout(reviewSummarySortSyncTimer);
  }
  reviewSummarySortSyncTimer = setTimeout(() => {
    syncReviewSummarySortCache();
    reviewSummarySortSyncTimer = null;
  }, 200);
};
const clearReviewSummarySortSyncTimer = () => {
  if (!reviewSummarySortSyncTimer) return;
  clearTimeout(reviewSummarySortSyncTimer);
  reviewSummarySortSyncTimer = null;
};
const finishRatingSortPending = () => {
  clearReviewSummarySortSyncTimer();
  isRatingSortPending.value = false;
};
const resetRatingSortPendingIds = (restaurants) => {
  ratingSortPendingIds.clear();
  restaurants.forEach((restaurant) => {
    const id = String(restaurant.id);
    if (!reviewSummaryCache.value[id]) {
      ratingSortPendingIds.add(id);
    }
  });
};
const markRatingSortReady = (restaurantId) => {
  if (!isRatingSortPending.value) return;
  ratingSortPendingIds.delete(String(restaurantId));
  if (ratingSortPendingIds.size === 0) {
    syncReviewSummarySortCache();
    finishRatingSortPending();
  }
};
const startRatingSortPending = (restaurants) => {
  isRatingSortPending.value = true;
  setTimeout(() => {
    resetRatingSortPendingIds(restaurants);
    if (ratingSortPendingIds.size === 0) {
      finishRatingSortPending();
    }
  }, 0);
};
const finalizeRatingSortPending = () => {
  if (!isRatingSortPending.value) return;
  if (ratingSortPendingIds.size === 0) {
    syncReviewSummarySortCache();
    finishRatingSortPending();
  }
};
const handleCheckRoute = async (restaurant) => {
  if (!restaurant) return;
  routeError.value = "";
  routeInfo.value = null;
  routeLoadingId.value = restaurant.id ?? restaurant.restaurantId ?? null;

  try {
    const coords = await resolveRestaurantCoords(restaurant);
    if (!coords) {
      routeError.value = "경로를 확인할 수 없습니다.";
      return;
    }
    setRouteFocus(coords, restaurant.name);
    if (
      !Number.isFinite(mapCenter.value?.lat) ||
      !Number.isFinite(mapCenter.value?.lng)
    ) {
      routeError.value = "회사 위치를 확인할 수 없습니다.";
      return;
    }
    const response = await httpRequest.post("/api/map/route", {
      origin: { lat: mapCenter.value.lat, lng: mapCenter.value.lng },
      destination: { lat: coords.lat, lng: coords.lng },
    });
    const data = response?.data || {};
    if (Array.isArray(data.path) && data.path.length) {
      drawRoute(data.path);
    }
    setRouteFocus(coords, restaurant.name);
    routeInfo.value = {
      restaurantId: restaurant.id ?? restaurant.restaurantId ?? null,
      restaurantName: restaurant.name,
      distanceMeters: data.distanceMeters ?? null,
      durationSeconds: data.durationSeconds ?? null,
    };
  } catch (error) {
    routeError.value = "경로를 불러오지 못했습니다.";
  } finally {
    routeLoadingId.value = null;
  }
};

const clearRouteInfo = () => {
  routeInfo.value = null;
  routeError.value = "";
  routeLoadingId.value = null;
  clearRoute();
  clearRouteFocus();
};

watch(selectedRecommendation, () => {
  clearRouteInfo();
});
const processedRestaurants = computed(() => {
  let result = activeRestaurantSource.value.slice();

  if (searchResultIds.value !== null) {
    const validIds = new Set(searchResultIds.value.map(String));
    result = result.filter((r) => validIds.has(String(r.id)));
  }

  const normalizedQuery = searchQuery.value.trim().toLowerCase();
  if (normalizedQuery) {
    result = result.filter((restaurant) =>
      String(restaurant.name || "").toLowerCase().includes(normalizedQuery)
    );
  }
  const idSet = searchRestaurantIdSet.value;
  if (idSet) {
    result = result.filter((restaurant) =>
      idSet.has(String(restaurant.id ?? restaurant.restaurantId))
    );
  }
  const distanceLimit = selectedDistanceKm.value;
  if (distanceLimit) {
    result = result.filter((restaurant) =>
      isWithinDistance(restaurant.coords, distanceLimit)
    );
  }

  const activeRange = selectedPriceRange.value;
  if (activeRange && activeRange !== "전체") {
    const range = priceRangeMap[activeRange];
    if (range) {
      result = result.filter((restaurant) => {
        const priceValue = resolveRestaurantPriceValue(restaurant);
        if (priceValue == null) return false;
        return priceValue >= range.min && priceValue <= range.max;
      });
    }
  }

  const center = mapCenter.value;
  const getDistance = (restaurant) =>
      calculateDistanceKm(restaurant.coords, center);

  const sorters = {
    가나다순: (a, b) => {
      const nameA = String(a?.name || "");
      const nameB = String(b?.name || "");
      const nameDiff = nameA.localeCompare(nameB, "ko");
      if (nameDiff !== 0) return nameDiff;
      return getSortId(a) - getSortId(b);
    },
    추천순: (a, b) => {
      const scoreDiff =
        getSortRecommendScoreSnapshot(b) - getSortRecommendScoreSnapshot(a);
      if (scoreDiff !== 0) return scoreDiff;
      return getSortId(a) - getSortId(b);
    },
    거리순: (a, b) => {
      const distanceDiff = getDistance(a) - getDistance(b);
      if (distanceDiff !== 0) return distanceDiff;
      return getSortId(a) - getSortId(b);
    },
    평점순: (a, b) => {
      const ratingDiff = getSortRatingSnapshot(b) - getSortRatingSnapshot(a);
      if (ratingDiff !== 0) return ratingDiff;
      return getSortId(a) - getSortId(b);
    },
    가격순: (a, b) => {
      const priceA =
          resolveRestaurantPriceValue(a) ?? Number.POSITIVE_INFINITY;
      const priceB =
          resolveRestaurantPriceValue(b) ?? Number.POSITIVE_INFINITY;
      const priceDiff = priceA - priceB;
      if (priceDiff !== 0) return priceDiff;
      return getSortId(a) - getSortId(b);
    },
    "낮은 가격순": (a, b) => {
      const priceA =
          resolveRestaurantPriceValue(a) ?? Number.POSITIVE_INFINITY;
      const priceB =
          resolveRestaurantPriceValue(b) ?? Number.POSITIVE_INFINITY;
      const priceDiff = priceA - priceB;
      if (priceDiff !== 0) return priceDiff;
      return getSortId(a) - getSortId(b);
    },
  };

  if (
    selectedRecommendation.value !== RECOMMEND_TASTE ||
    selectedSort.value === "평점순"
  ) {
    const sorter = sorters[selectedSort.value] || sorters[Object.keys(sorters)[0]];
    result.sort(sorter);
  }

  const overrides = restaurantImageOverrides.value;
  return result.map((restaurant) => ({
    ...restaurant,
    image: overrides[restaurant.id] ?? restaurant.image,
  }));
});
const trendingCards = computed(() => {
  return trendingRestaurants.value.map((restaurant) => {
    const fallback = restaurantIndexById.get(String(restaurant.restaurantId));
    const addressParts = [restaurant.roadAddress, restaurant.detailAddress]
        .filter(Boolean)
        .join(" ");
    const apiTags = Array.isArray(restaurant.tags)
        ? restaurant.tags.map((tag) => ({ name: tag.content }))
        : [];
    return {
      id: String(restaurant.restaurantId),
      name: restaurant.name || fallback?.name || "",
      address: addressParts || fallback?.address || "",
      image: restaurant.imageUrl || fallback?.image || "/placeholder.svg",
      rating: restaurant.rating ?? fallback?.rating ?? null,
      reviews: restaurant.reviewCount ?? restaurant.reviews ?? fallback?.reviews ?? 0,
      price: restaurant.price ?? fallback?.price ?? "가격 정보 없음",
      reviewTags: restaurant.reviewTags || [],
      restaurantTags: apiTags.length ? apiTags : fallback?.topTags || [],
    };
  });
});
const trendingRestaurantIdSet = computed(
    () =>
        new Set(
            trendingRestaurants.value.map((restaurant) =>
                String(restaurant.restaurantId)
            )
        )
);
const availableRestaurants = computed(() => {
  if (!isTrendingSort.value) {
    return processedRestaurants.value;
  }
  return processedRestaurants.value.filter(
      (restaurant) => !trendingRestaurantIdSet.value.has(String(restaurant.id))
  );
});
const displayRestaurants = computed(() => availableRestaurants.value);
const totalPages = computed(() =>
    Math.max(1, Math.ceil(displayRestaurants.value.length / restaurantsPerPage))
);
const paginatedRestaurantsRaw = computed(() => {
  const start = (currentPage.value - 1) * restaurantsPerPage;
  return displayRestaurants.value.slice(start, start + restaurantsPerPage);
});
const paginatedRestaurants = computed(() =>
    paginatedRestaurantsRaw.value.map((restaurant) =>
        applyReviewSummary(restaurant)
    )
);
const pageGroupSize = 5;
const currentPageGroupStart = computed(
    () => Math.floor((currentPage.value - 1) / pageGroupSize) * pageGroupSize + 1
);
const currentPageGroupEnd = computed(() =>
    Math.min(totalPages.value, currentPageGroupStart.value + pageGroupSize - 1)
);
const pageNumbers = computed(() => {
  const length = currentPageGroupEnd.value - currentPageGroupStart.value + 1;
  return Array.from({ length }, (_, index) => currentPageGroupStart.value + index);
});
const canGoPrevious = computed(() => currentPageGroupStart.value > 1);
const canGoNext = computed(() => currentPageGroupEnd.value < totalPages.value);
const isCalendarOpen = ref(false);
const calendarMonth = ref(new Date());
const weekdayLabels = ["일", "월", "화", "수", "목", "금", "토"];
const selectedMapRestaurants = ref([]);

const formattedSearchDate = computed(() => {
  if (!searchDate.value) {
    return "날짜를 선택하세요";
  }
  const date = new Date(searchDate.value);
  if (Number.isNaN(date.getTime())) {
    return "날짜를 선택하세요";
  }
  return date.toLocaleDateString("ko-KR", {
    month: "long",
    day: "numeric",
    weekday: "short",
  });
});

const formattedCalendarMonth = computed(() => {
  return calendarMonth.value.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
  });
});

const calendarDays = computed(() => {
  const year = calendarMonth.value.getFullYear();
  const month = calendarMonth.value.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const totalCells = Math.ceil((firstDay + daysInMonth) / 7) * 7;
  const days = [];

  for (let i = 0; i < firstDay; i++) {
    days.push(null);
  }

  for (let day = 1; day <= daysInMonth; day++) {
    days.push(day);
  }

  while (days.length < totalCells) {
    days.push(null);
  }

  return days;
});

const formatDateValue = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const toggleCalendar = () => {
  if (!isCalendarOpen.value) {
    calendarMonth.value = searchDate.value
        ? new Date(searchDate.value)
        : new Date();
  }
  isCalendarOpen.value = !isCalendarOpen.value;
};
const handleSearchModalScroll = () => {
  if (isCalendarOpen.value) {
    isCalendarOpen.value = false;
  }
};

const isDateDisabled = (day) => {
  if (!day) return true;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(
    calendarMonth.value.getFullYear(),
    calendarMonth.value.getMonth(),
    day
  );
  return target < today;
};

const selectCalendarDay = (day) => {
  if (!day || isDateDisabled(day)) return;

  const selected = new Date(
      calendarMonth.value.getFullYear(),
      calendarMonth.value.getMonth(),
      day
  );
  searchDate.value = formatDateValue(selected);
  isCalendarOpen.value = false;
};

const previousCalendarMonth = () => {
  calendarMonth.value = new Date(
      calendarMonth.value.getFullYear(),
      calendarMonth.value.getMonth() - 1,
      1
  );
};

const nextCalendarMonth = () => {
  calendarMonth.value = new Date(
      calendarMonth.value.getFullYear(),
      calendarMonth.value.getMonth() + 1,
      1
  );
};

const isCalendarSelectedDay = (day) => {
  if (!day || !searchDate.value) return false;
  const selectedDate = new Date(searchDate.value);
  return (
      selectedDate.getFullYear() === calendarMonth.value.getFullYear() &&
      selectedDate.getMonth() === calendarMonth.value.getMonth() &&
      selectedDate.getDate() === day
  );
};

watch(searchDate, (value) => {
  if (!value) return;
  const parsed = new Date(value);
  if (!Number.isNaN(parsed.getTime())) {
    calendarMonth.value = new Date(parsed.getFullYear(), parsed.getMonth(), 1);
  }
});

watch(totalPages, (newTotal) => {
  if (currentPage.value > newTotal) {
    currentPage.value = newTotal;
  }
});

watch(selectedSort, () => {
  if (isHomeListRestoring.value) return;
  currentPage.value = 1;
});

watch(
    [selectedSort, availableRestaurants],
    ([sortValue, list]) => {
      if (!isScoreSort(sortValue)) return;
      startRatingSortPending(list);
      setTimeout(() => {
        list.forEach((restaurant) => {
          fetchReviewSummary(restaurant.id, restaurant);
        });
        finalizeRatingSortPending();
      }, 0);
    },
    { immediate: true }
);

watch(selectedPriceRange, () => {
  if (isHomeListRestoring.value) return;
  currentPage.value = 1;
});

watch(selectedSort, (nextSort) => {
  if (isScoreSort(nextSort)) {
    return;
  }
  finishRatingSortPending();
});

watch(reviewSummaryCache, () => {
  if (!isScoreSort(selectedSort.value)) return;
  if (!isRatingSortPending.value) return;
  scheduleReviewSummarySortSync();
  finalizeRatingSortPending();
});

watch(paginatedRestaurantsRaw, () => {
  if (!isScoreSort(selectedSort.value)) return;
  if (!isRatingSortPending.value) return;
  finalizeRatingSortPending();
});

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
};

const goToPreviousPage = () => {
  if (!canGoPrevious.value) return;
  goToPage(Math.max(1, currentPageGroupStart.value - pageGroupSize));
};

const goToNextPage = () => {
  if (!canGoNext.value) return;
  goToPage(currentPageGroupStart.value + pageGroupSize);
};

const fetchReviewSummary = async (restaurantId, fallback = null) => {
  const key = String(restaurantId);
  if (reviewSummaryCache.value[key] || reviewSummaryInFlight.has(key)) return;
  reviewSummaryInFlight.add(key);
  try {
    const response = await axios.get(`/api/restaurants/${restaurantId}/reviews`, {
      params: { page: 1, size: 1, sort: "RECOMMEND" },
    });
    const data = response.data?.data ?? response.data;
    const summary = data?.summary;
    if (summary) {
      reviewSummaryCache.value = {
        ...reviewSummaryCache.value,
        [key]: {
          rating: summary.avgRating ?? 0,
          reviews: summary.reviewCount ?? 0,
        },
      };
    } else if (fallback) {
      reviewSummaryCache.value = {
        ...reviewSummaryCache.value,
        [key]: {
          rating: fallback.rating ?? 0,
          reviews: fallback.reviews ?? 0,
        },
      };
    }
  } catch (error) {
    if (fallback) {
      reviewSummaryCache.value = {
        ...reviewSummaryCache.value,
        [key]: {
          rating: fallback.rating ?? 0,
          reviews: fallback.reviews ?? 0,
        },
      };
    }
  } finally {
    reviewSummaryInFlight.delete(key);
    markRatingSortReady(restaurantId);
  }
};

const normalizeMapRestaurant = (restaurant) => {
  const key = String(restaurant.id);
  const summary = reviewSummaryCache.value[key];
  return {
    ...restaurant,
    image:
        restaurantImageOverrides.value[String(restaurant.id)] ?? restaurant.image,
    rating: summary?.rating ?? restaurant.rating,
    reviews: summary?.reviews ?? restaurant.reviews,
  };
};

const updateSelectedMapRestaurants = (restaurants) => {
  selectedMapRestaurants.value = restaurants.map(normalizeMapRestaurant);
};

const refreshSelectedMapRestaurants = () => {
  if (!selectedMapRestaurants.value.length) return;
  selectedMapRestaurants.value = selectedMapRestaurants.value.map(normalizeMapRestaurant);
};
const singleMapRestaurant = computed(() => {
  if (selectedMapRestaurants.value.length !== 1) return null;
  return selectedMapRestaurants.value[0];
});

const formatRating = (value) => {
  if (value == null) return "0.0";
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return "0.0";
  return parsed.toFixed(1);
};

const ensureReviewSummary = async (restaurant) => {
  const key = String(restaurant.id);
  if (!reviewSummaryCache.value[key]) {
    await fetchReviewSummary(restaurant.id, restaurant);
  }
  if (selectedMapRestaurants.value.some((item) => item.id === restaurant.id)) {
    refreshSelectedMapRestaurants();
  }
};

watch(
    paginatedRestaurantsRaw,
    (restaurants) => {
      restaurants.forEach((restaurant) => {
        fetchReviewSummary(restaurant.id, restaurant);
      });
    },
    { immediate: true }
);

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const downloadJsonFile = (data, filename) => {
  if (typeof window === "undefined") return;
  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: "application/json",
  });
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
};

const startGeocodeExport = async () => {
  if (isGeocodeExporting.value) return;
  isGeocodeExporting.value = true;
  geocodeExportMissing.value = [];
  geocodeExportProgress.value = {
    done: 0,
    total: restaurants.length,
  };

  for (const restaurant of restaurants) {
    try {
      const coords = await resolveRestaurantCoords(restaurant);
      if (!coords) {
        geocodeExportMissing.value.push({
          id: restaurant.id,
          name: restaurant.name,
          address: restaurant.address,
        });
      }
    } catch (error) {
      geocodeExportMissing.value.push({
        id: restaurant.id,
        name: restaurant.name,
        address: restaurant.address,
      });
    } finally {
      geocodeExportProgress.value.done += 1;
      await delay(200);
    }
  }

  const exportPayload = restaurants.map((restaurant) => ({
    id: restaurant.id,
    name: restaurant.name,
    address: restaurant.address,
    coords: restaurant.coords ?? null,
  }));

  downloadJsonFile(exportPayload, "restaurant_coords.json");
  if (geocodeExportMissing.value.length) {
    console.warn("좌표 변환 실패 목록:", geocodeExportMissing.value);
  }
  isGeocodeExporting.value = false;
};

const fetchRestaurantImage = async (restaurantId) => {
  const key = String(restaurantId);
  if (restaurantImageCache.has(key)) return;
  restaurantImageCache.set(key, null);
  try {
    const response = await axios.get(
        `/api/business/restaurants/${restaurantId}/images`
    );
    const imageUrl = response.data?.[0]?.imageUrl;
    if (imageUrl) {
      restaurantImageCache.set(key, imageUrl);
      restaurantImageOverrides.value = {
        ...restaurantImageOverrides.value,
        [key]: imageUrl,
      };
    }
  } catch (error) {
    restaurantImageCache.set(key, null);
  }
};

// Static data (constants)
const timeSlots = ["11:00", "12:00", "13:00", "14:00"];
const priceRanges = [
  "전체",
  "1만원 이하",
  "1만원~1.5만원",
  "1.5만원~2만원",
  "2만원~3만원",
  "3만원 이상",
];
const priceRangeMap = Object.freeze({
  전체: null,
  "1만원 이하": { min: 0, max: 10000 },
  "1만원~1.5만원": { min: 10000, max: 15000 },
  "1.5만원~2만원": { min: 15000, max: 20000 },
  "2만원~3만원": { min: 20000, max: 30000 },
  "3만원 이상": { min: 30000, max: Number.POSITIVE_INFINITY },
});
const recommendationOptions = [
  RECOMMEND_CAFETERIA,
  RECOMMEND_BUDGET,
  RECOMMEND_TASTE,
  RECOMMEND_TRENDING,
  RECOMMEND_WEATHER,
];
const recommendationButtons = computed(() => [
  { value: RECOMMEND_CAFETERIA, label: "\uAD6C\uB0B4\uC2DD\uB2F9 \uB300\uCCB4", emoji: "\uD83C\uDF71" },
  { value: RECOMMEND_BUDGET, label: "\uC608\uC0B0", emoji: "\uD83D\uDCB0" },
  { value: RECOMMEND_TASTE, label: "\uCDE8\uD5A5", emoji: "\uD83D\uDE0B" },
  { value: RECOMMEND_TRENDING, label: "\uC778\uAE30", emoji: "\uD83D\uDD25" },
  { value: RECOMMEND_WEATHER, label: "\uB0A0\uC528", emoji: "\u2600\uFE0F" },
]);
const distances = ["1km 이내", "2km 이내", "3km 이내"];
const sortOptions = [DEFAULT_SORT, "추천순", "거리순", "평점순", "낮은 가격순"];
const storedHomeState = loadHomeListState();
const isHomeListRestoring = ref(Boolean(storedHomeState));

const resolveStoredHomeSelection = (state) => {
  const restoredSort = sortOptions.includes(state.selectedSort)
    ? state.selectedSort
    : selectedSort.value;
  const restoredPriceRange = priceRanges.includes(state.selectedPriceRange)
    ? state.selectedPriceRange
    : selectedPriceRange.value;
  const restoredRecommendation = recommendationOptions.includes(
    state.selectedRecommendation
  )
    ? state.selectedRecommendation
    : selectedRecommendation.value;
  return {
    restoredSort,
    restoredPriceRange,
    restoredRecommendation,
  };
};

if (storedHomeState) {
  const { restoredSort, restoredPriceRange, restoredRecommendation } =
    resolveStoredHomeSelection(storedHomeState);
  selectedSort.value = restoredSort;
  selectedPriceRange.value = restoredPriceRange;
  selectedRecommendation.value = restoredRecommendation;
  filterForm.sort = selectedSort.value;
  filterForm.priceRange = selectedPriceRange.value;
  filterForm.recommendation = selectedRecommendation.value;
  if (Number.isFinite(Number(storedHomeState.filterBudget))) {
    filterBudget.value = Number(storedHomeState.filterBudget);
  }
  if (Number.isFinite(Number(storedHomeState.filterPartySize))) {
    filterPartySize.value = Number(storedHomeState.filterPartySize);
  }
  currentPage.value = storedHomeState.currentPage ?? currentPage.value;
}


const resolveRestaurantPriceValue = (restaurant) => {
  const directPrice = extractPriceValue(restaurant?.price ?? "");
  if (directPrice != null) return directPrice;
  const menuPrices = (restaurant?.menus || [])
      .map((menu) => extractPriceValue(menu?.price ?? ""))
      .filter((value) => Number.isFinite(value));
  if (!menuPrices.length) return null;
  const total = menuPrices.reduce((sum, value) => sum + value, 0);
  return Math.round(total / menuPrices.length);
};

// Event handlers
const openFilterModal = () => {
  filterForm.sort = selectedSort.value;
  filterForm.priceRange = selectedPriceRange.value;
  filterForm.recommendation = selectedRecommendation.value;
  isFilterOpen.value = true;
};

const toggleFilterPriceRange = (range) => {
  if (filterForm.priceRange === range) {
    filterForm.priceRange = null;
    return;
  }
  filterForm.priceRange = range;
};

const selectSortOption = (option) => {
  filterForm.sort = option;
};

const openSearchModal = () => {
  isSearchOpen.value = true;
};
watch(
  () => userId.value,
  (nextUserId) => {
    if (!nextUserId) {
      clearFavorites();
      return;
    }
    fetchFavorites(nextUserId);
  },
  { immediate: true }
);

const resetFilters = () => {
  filterForm.sort = sortOptions[0];
  filterForm.priceRange = null;
  filterForm.recommendation = null;
  filterBudget.value = 60000;
  filterPartySize.value = 4;
  clearBudgetRecommendations();
  clearWeatherRecommendations();
  clearTagMappingRecommendations();
};

const runBudgetRecommendations = (budget) => {
  isBudgetLoading.value = true;
  setTimeout(() => {
    fetchBudgetRecommendations(budget);
    isBudgetLoading.value = false;
  }, 0);
};

const runCafeteriaRecommendations = async (baseDate) => {
  isCafeteriaLoading.value = true;
  try {
    await requestCafeteriaRecommendations(baseDate);
  } finally {
    isCafeteriaLoading.value = false;
  }
};

const closeFilterModal = () => {
  filterForm.sort = selectedSort.value;
  filterForm.priceRange = selectedPriceRange.value;
  filterForm.recommendation = selectedRecommendation.value;
  isFilterOpen.value = false;
};

const handleCafeteriaMenuEdit = () => {
  if (hasConfirmedMenus.value) {
    openCafeteriaModalWithExisting(resolveCafeteriaBaseDate());
    return;
  }
  openCafeteriaModal();
};

const handleCafeteriaRecommendNow = async () => {
  await runCafeteriaRecommendations(resolveCafeteriaBaseDate());
  isFilterOpen.value = false;
};

const handleCafeteriaConfirmAndClose = async (days) => {
  const saved = await handleCafeteriaConfirm(
      days,
      resolveCafeteriaBaseDate()
  );
  if (saved) {
    isFilterOpen.value = false;
  }
};

const toggleSearchCategory = (category) => {
  if (searchCategories.value.includes(category)) {
    searchCategories.value = [];
  } else {
    searchCategories.value = [category];
  }
};

const toggleSearchTag = (tag) => {
  const index = searchTags.value.indexOf(tag);
  if (index > -1) {
    searchTags.value.splice(index, 1);
  } else {
    searchTags.value.push(tag);
  }
};

const toggleAvoidIngredient = (ingredient) => {
  const index = avoidIngredients.value.indexOf(ingredient);
  if (index > -1) {
    avoidIngredients.value.splice(index, 1);
  } else {
    avoidIngredients.value.push(ingredient);
  }
};

const resetSearch = () => {
  searchDate.value = "";
  searchTime.value = "";
  searchCategories.value = [];
  searchPartySize.value = 4;
  searchTags.value = [];
  avoidIngredients.value = [];
  searchDistance.value = "";
  searchRestaurantIds.value = null;
  searchRestaurantError.value = "";
  persistSearchState();
  isCalendarOpen.value = false;
  calendarMonth.value = new Date();
};

const buildSearchParams = () => {
  const hasDate = Boolean(searchDate.value);
  const hasTime = Boolean(searchTime.value);
  const hasMenuTypes = searchCategories.value.length > 0;
  const hasRestaurantTags = searchTags.value.length > 0;
  const hasAvoidIngredients = avoidIngredients.value.length > 0;
  const shouldSearch =
    hasDate ||
    hasTime ||
    hasMenuTypes ||
    hasRestaurantTags ||
    hasAvoidIngredients ||
    searchPreorder.value;
  const params = new URLSearchParams();
  if (hasDate) params.set("date", searchDate.value);
  if (hasTime) params.set("time", searchTime.value);
  if (Number.isFinite(Number(searchPartySize.value))) {
    params.set("partySize", String(searchPartySize.value));
  }
  if (hasMenuTypes) {
    searchCategories.value.forEach((menuType) =>
      params.append("menuTypes", menuType)
    );
  }
  if (hasRestaurantTags) {
    searchTags.value.forEach((tag) => params.append("restaurantTags", tag));
  }
  if (hasAvoidIngredients) {
    avoidIngredients.value.forEach((ingredient) =>
      params.append("avoidIngredients", ingredient)
    );
  }
  if (searchPreorder.value) {
    params.set("preorderAvailable", "true");
  }
  return { shouldSearch, params };
};

const executeSearch = async ({ closeModal = true, silent = false } = {}) => {
  if (closeModal) {
    isSearchOpen.value = false;
    isCalendarOpen.value = false;
  }
  const { shouldSearch, params } = buildSearchParams();
  if (!shouldSearch) {
    searchRestaurantIds.value = null;
    searchRestaurantError.value = "";
    persistSearchState();
    return;
  }

  if (!silent) {
    isSearchLoading.value = true;
  }
  searchRestaurantError.value = "";
  try {
    const response = await httpRequest.get("/api/restaurants/search", params);
    const ids = Array.isArray(response.data) ? response.data : [];
    searchRestaurantIds.value = ids;
    currentPage.value = 1;
    persistSearchState();
  } catch (error) {
    if (error?.response?.status === 404) {
      searchRestaurantIds.value = [];
      persistSearchState();
      return;
    }
    searchRestaurantError.value = "검색 결과를 불러오지 못했어요.";
    searchRestaurantIds.value = [];
    persistSearchState();
  } finally {
    if (!silent) {
      isSearchLoading.value = false;
    }
  }
};

const applySearch = () => executeSearch({ closeModal: true });

const persistSearchState = () => {
  const state = {
    searchDate: searchDate.value,
    searchTime: searchTime.value,
    searchCategories: searchCategories.value,
    searchPartySize: searchPartySize.value,
    searchTags: searchTags.value,
    avoidIngredients: avoidIngredients.value,
    searchDistance: searchDistance.value,
    searchRestaurantIds: searchRestaurantIds.value,
  };
  sessionStorage.setItem(searchStateStorageKey, JSON.stringify(state));
};

const restoreSearchState = async () => {
  const storedSearchState = sessionStorage.getItem(searchStateStorageKey);
  if (!storedSearchState) return;
  try {
    const parsed = JSON.parse(storedSearchState);
    if (typeof parsed.searchDate === "string") {
      searchDate.value = parsed.searchDate;
    }
    if (typeof parsed.searchTime === "string") {
      searchTime.value = parsed.searchTime;
    }
    if (Array.isArray(parsed.searchCategories)) {
      searchCategories.value = parsed.searchCategories;
    }
    if (Number.isFinite(Number(parsed.searchPartySize))) {
      searchPartySize.value = Number(parsed.searchPartySize);
    }
    if (Array.isArray(parsed.searchTags)) {
      searchTags.value = parsed.searchTags;
    }
    if (Array.isArray(parsed.avoidIngredients)) {
      avoidIngredients.value = parsed.avoidIngredients;
    }
    if (typeof parsed.searchDistance === "string") {
      searchDistance.value = parsed.searchDistance;
    }
    if (Array.isArray(parsed.searchRestaurantIds)) {
      searchRestaurantIds.value = parsed.searchRestaurantIds;
    }
  } catch (error) {
    console.error("검색 상태 복원 실패:", error);
    sessionStorage.removeItem(searchStateStorageKey);
    return;
  }

  const { shouldSearch } = buildSearchParams();
  if (shouldSearch) {
    await executeSearch({ closeModal: false, silent: true });
  }
};

const closeMapRestaurantModal = () => {
  selectedMapRestaurants.value = [];
};

const resolveCafeteriaBaseDate = () => {
  if (searchDate.value) {
    return searchDate.value;
  }
  const now = new Date();
  const base = new Date(now);
  const isFriday = base.getDay() === 5;
  const isAfterFridayNoon =
      isFriday && (base.getHours() > 12 || (base.getHours() === 12 && base.getMinutes() >= 0));
  if (isAfterFridayNoon) {
    base.setDate(base.getDate() + 7);
  }
  const year = base.getFullYear();
  const month = String(base.getMonth() + 1).padStart(2, "0");
  const day = String(base.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const refreshCafeteriaRecommendationsIfNeeded = async () => {
  if (hasAutoRefreshedCafeteria.value) {
    return;
  }
  if (!isLoggedIn.value) {
    return;
  }
  const shouldRefresh =
    selectedRecommendation.value === RECOMMEND_CAFETERIA ||
    cafeteriaRecommendations.value.length > 0;
  if (!shouldRefresh) {
    return;
  }
  hasAutoRefreshedCafeteria.value = true;
  const baseDate = resolveCafeteriaBaseDate();
  const hasMenus = await checkCafeteriaMenuStatus(baseDate);
  if (hasMenus) {
    await requestCafeteriaRecommendations(baseDate);
  } else {
    clearCafeteriaRecommendations();
  }
};

onMounted(async () => {
  await loadRestaurants(); // Load data from API
  void restoreSearchState();
  await applyUserMapCenter();
  await initializeMap();
  if (typeof window !== "undefined") {
    const params = new URLSearchParams(window.location.search);
    if (params.get("geocode") === "1") {
      isGeocodeExportMode.value = true;
      startGeocodeExport();
    }
  }
  fetchSearchTags();
  loadRecommendationsFromStorage();

  if (storedHomeState) {
    try {
      const parsed = storedHomeState;
      const { restoredSort, restoredPriceRange, restoredRecommendation } =
        resolveStoredHomeSelection(parsed);
      selectedSort.value = restoredSort;
      selectedPriceRange.value = restoredPriceRange;
      selectedRecommendation.value = restoredRecommendation;
      filterForm.sort = selectedSort.value;
      filterForm.priceRange = selectedPriceRange.value;
      filterForm.recommendation = selectedRecommendation.value;
      if (selectedRecommendation.value === RECOMMEND_TASTE) {
        if (!isLoggedIn.value) {
          selectedRecommendation.value = null;
          filterForm.recommendation = null;
          persistHomeListState();
        } else {
          fetchTagMappingRecommendations();
        }
      }
      if (selectedRecommendation.value === RECOMMEND_BUDGET) {
        runBudgetRecommendations(filterPerPersonBudget.value);
      }
      if (selectedRecommendation.value === RECOMMEND_WEATHER) {
        fetchWeatherRecommendationsForCenter();
      }
      if (
          selectedRecommendation.value === RECOMMEND_CAFETERIA &&
          !cafeteriaRecommendations.value.length
      ) {
        selectedRecommendation.value = null;
        filterForm.recommendation = null;
        persistHomeListState();
      }
      await refreshCafeteriaRecommendationsIfNeeded();
      nextTick(() => {
        if (Number.isFinite(parsed.scrollY)) {
          window.scrollTo(0, parsed.scrollY);
        }
      });
    } catch (error) {
      console.error("홈 리스트 상태 복원 실패:", error);
      clearHomeListState();
    }
  } else {
    await refreshCafeteriaRecommendationsIfNeeded();
  }
  isHomeListRestoring.value = false;
});

watch(isTrendingSort, (isActive) => {
  if (isActive) {
    fetchTrendingRestaurants({ days: 7, limit: restaurantsPerPage });
    return;
  }
  clearTrendingRestaurants();
});

const persistHomeListState = () => {
  saveHomeListState({
    selectedSort: selectedSort.value,
    selectedPriceRange: selectedPriceRange.value,
    selectedRecommendation: selectedRecommendation.value,
    filterBudget: filterBudget.value,
    filterPartySize: filterPartySize.value,
    currentPage: currentPage.value,
    scrollY: window.scrollY,
  });
};

const fetchWeatherRecommendationsForCenter = () =>
    fetchWeatherRecommendations(mapCenter.value);

const {
  applyFilters,
  clearTrendingRecommendation,
  toggleRecommendationOption,
  handleRecommendationQuickSelect,
  clearRecommendation,
} = useHomeRecommendations({
  selectedSort,
  selectedPriceRange,
  selectedRecommendation,
  filterForm,
  sortOptions,
  filterPerPersonBudget,
  fetchBudgetRecommendations: runBudgetRecommendations,
  clearBudgetRecommendations,
  fetchWeatherRecommendations: fetchWeatherRecommendationsForCenter,
  clearWeatherRecommendations,
  fetchTagMappingRecommendations,
  clearTagMappingRecommendations,
  clearTrendingRestaurants,
  cafeteriaRecommendations,
  clearCafeteriaRecommendations,
  resolveCafeteriaBaseDate,
  checkCafeteriaMenuStatus,
  openCafeteriaModal,
  requestCafeteriaRecommendations: runCafeteriaRecommendations,
  hasConfirmedMenus,
  currentPage,
  isFilterOpen,
  persistHomeListState,
  RECOMMEND_CAFETERIA,
  RECOMMEND_BUDGET,
  RECOMMEND_TASTE,
  RECOMMEND_WEATHER,
  RECOMMEND_TRENDING,
});

watch([selectedSort, selectedPriceRange, selectedRecommendation], () => {
  persistHomeListState();
});

watch(currentPage, () => {
  persistHomeListState();
});

watch(userId, () => {
  loadSharedFavoriteRestaurants();
}, { immediate: true });

watch(
    paginatedRestaurants,
    (list) => {
      list.forEach((restaurant) => fetchRestaurantImage(restaurant.id));
    },
    { immediate: true }
);

onBeforeUnmount(() => {
  persistHomeListState();
  clearReviewSummarySortSyncTimer();
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <AppHeader v-model:searchQuery="searchQuery" />
    <div
        v-if="isGeocodeExportMode"
        class="bg-white border-b border-[#e9ecef]"
    >
      <div class="max-w-[500px] mx-auto px-4 py-3 text-xs text-gray-700">
        <p class="font-semibold text-[#1e3a5f]">주소 좌표 생성 모드</p>
        <p v-if="isGeocodeExporting">
          진행 중: {{ geocodeExportProgress.done }} /
          {{ geocodeExportProgress.total }}
        </p>
        <p v-else>완료되었습니다. 다운로드된 파일을 확인하세요.</p>
        <p v-if="geocodeExportMissing.length">
          실패 {{ geocodeExportMissing.length }}건 (콘솔 확인)
        </p>
      </div>

    </div>

    <main class="max-w-[500px] mx-auto pb-20">
      <div class="bg-white px-3 py-2 border-b border-[#e9ecef]">
        <div class="flex items-center justify-between gap-3">
          <div class="flex items-center gap-2">
            <MapPin class="w-5 h-5 text-[#ff6b4a]" />
            <div>
              <h2 class="text-sm font-semibold text-[#1e3a5f]">
                {{ currentLocation }}
              </h2>
              <p class="text-xs text-gray-700">현재 위치 기준</p>
            </div>
          </div>
          <div
              v-if="isLoggedIn"
              class="flex flex-col items-start gap-0 text-[11px] text-gray-600"
          >
            <span
                class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-white"
            >
              <MapMarkerLegendIcon fill="#007bff" star-fill="white" />
              나의 즐겨찾기
            </span>
            <span
                class="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-white"
            >
              <MapMarkerLegendIcon fill="#ffc107" star-fill="white" />
              공유 즐겨찾기
            </span>
          </div>
        </div>
      </div>

      <div class="relative h-64">
        <div ref="mapContainer" class="w-full h-full" />
        <div
            class="absolute inset-0 pointer-events-none bg-gradient-to-t from-black/20 via-transparent to-transparent z-0"
        />
        <div
            class="absolute top-4 right-4 z-20 pointer-events-auto flex flex-col items-center gap-2"
        >
          <button
              @click="changeMapDistance(-1)"
              class="w-8 h-8 rounded-sm bg-white shadow-card flex items-center justify-center text-[#1e3a5f] hover:bg-[#f8f9fa]"
          >
            <Plus class="w-4 h-4" />
          </button>
          <div
              class="h-20 w-[6px] bg-white/80 rounded relative shadow-card overflow-hidden"
          >
            <div
                class="absolute top-0 left-0 right-0 bg-[#ff6b4a] transition-all"
                :style="{ height: `${distanceSliderFill}%` }"
            ></div>
          </div>
          <button
              @click="changeMapDistance(1)"
              class="w-8 h-8 rounded-sm bg-white shadow-card flex items-center justify-center text-[#1e3a5f] hover:bg-[#f8f9fa]"
          >
            <Minus class="w-4 h-4" />
          </button>
          <div
              class="mt-2 px-3 py-1 rounded-full bg-white text-xs font-semibold text-[#1e3a5f] shadow-card"
          >
            반경 {{ currentDistanceLabel }}
          </div>
        </div>
        <div
          v-if="routeInfo || routeError"
          class="absolute bottom-4 right-4 z-20 max-w-[220px] bg-white/95 backdrop-blur px-3 py-2 rounded-xl shadow-card text-xs text-[#1e3a5f]"
        >
          <div class="flex items-start justify-between gap-2">
            <div class="min-w-0">
              <p v-if="routeInfo" class="font-semibold truncate">
                {{ routeInfo.restaurantName }}
              </p>
              <p v-if="routeError" class="text-red-500">
                {{ routeError }}
              </p>
              <p v-else class="text-[11px] text-[#6c757d] mt-1">
                거리 {{ formatRouteDistance(routeInfo?.distanceMeters) }} ·
                예상 {{ formatRouteDuration(routeInfo?.durationSeconds) }}
              </p>
            </div>
            <button
              type="button"
              class="text-[#adb5bd] hover:text-[#6c757d]"
              @click="clearRouteInfo"
            >
              <X class="w-3 h-3" />
            </button>
          </div>
        </div>

        <div
            v-if="selectedMapRestaurants.length"
            class="absolute left-1/2 top-full z-30 w-[calc(100%-2rem)] max-w-[500px] -translate-x-1/2 mt-3 pointer-events-auto"
        >
          <div v-if="singleMapRestaurant" class="relative">
            <button
                @click="closeMapRestaurantModal"
                class="absolute right-2 top-2 z-10 rounded-full bg-white/90 border border-[#e9ecef] p-1.5 text-gray-700 shadow-card hover:bg-[#f8f9fa]"
            >
              <X class="w-4 h-4" />
            </button>
            <RouterLink
                :to="`/restaurant/${singleMapRestaurant.id}`"
                class="block border border-[#e9ecef] rounded-xl p-3 bg-white shadow-card hover:bg-[#f8f9fa]"
                @click="closeMapRestaurantModal"
            >
              <div class="flex gap-3">
                <img
                    :src="singleMapRestaurant.image || '/placeholder.svg'"
                    :alt="singleMapRestaurant.name"
                    class="w-16 h-16 rounded-lg object-cover flex-shrink-0"
                />
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-[#1e3a5f]">
                    {{ singleMapRestaurant.name }}
                  </p>
                  <p class="text-xs text-gray-700 truncate">
                    {{ singleMapRestaurant.address }}
                  </p>
                  <div class="flex items-center gap-2 text-xs text-gray-700 mt-1">
                    <span
                        class="flex items-center gap-1 text-[#1e3a5f] font-semibold"
                    >
                      <Star class="w-3.5 h-3.5 fill-[#ffc107] text-[#ffc107]" />
                      {{ formatRating(singleMapRestaurant.rating) }}
                    </span>
                  <span
                      v-if="favoriteIdSet.has(Number(singleMapRestaurant.id))"
                      class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-blue-50 text-blue-700 border border-blue-100"
                  >
                    즐겨찾기
                  </span>
                  <span
                      v-if="getSharedFavoriteLabel(singleMapRestaurant.id)"
                      class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-[#fff3cd] text-[#6b4e00] border border-[#ffe8a1]"
                  >
                    {{ getSharedFavoriteLabel(singleMapRestaurant.id) }}
                  </span>
                  <span>리뷰 {{ singleMapRestaurant.reviews }}개</span>
                  <span>{{ singleMapRestaurant.category }}</span>
                </div>
                  <p class="text-sm font-semibold text-[#1e3a5f] mt-1">
                    {{ singleMapRestaurant.price }}
                  </p>
                </div>
              </div>
            </RouterLink>
          </div>
          <div v-else class="bg-white border border-[#e9ecef] shadow-card rounded-2xl p-4">
            <div class="flex items-center justify-between mb-3">
              <p class="text-sm font-semibold text-[#1e3a5f]">
                같은 건물 식당 {{ selectedMapRestaurants.length }}곳
              </p>
              <button
                  @click="closeMapRestaurantModal"
                  class="text-gray-700 hover:text-gray-700"
              >
                <X class="w-4 h-4" />
              </button>
            </div>
            <div class="space-y-3 max-h-[40vh] overflow-y-auto pr-1">
              <RouterLink
                  v-for="restaurant in selectedMapRestaurants"
                  :key="restaurant.id"
                  :to="`/restaurant/${restaurant.id}`"
                  class="block border border-[#e9ecef] rounded-xl p-3 hover:bg-[#f8f9fa]"
                  @click="closeMapRestaurantModal"
              >
                <div class="flex gap-3">
                  <img
                      :src="restaurant.image || '/placeholder.svg'"
                      :alt="restaurant.name"
                      class="w-16 h-16 rounded-lg object-cover flex-shrink-0"
                  />
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-semibold text-[#1e3a5f]">
                      {{ restaurant.name }}
                    </p>
                    <p class="text-xs text-gray-700 truncate">
                      {{ restaurant.address }}
                    </p>
                    <div class="flex items-center gap-2 text-xs text-gray-700 mt-1">
                      <span
                          class="flex items-center gap-1 text-[#1e3a5f] font-semibold"
                      >
                        <Star class="w-3.5 h-3.5 fill-[#ffc107] text-[#ffc107]" />
                        {{ formatRating(restaurant.rating) }}
                      </span>
                    <span
                        v-if="favoriteIdSet.has(Number(restaurant.id))"
                        class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-blue-50 text-blue-700 border border-blue-100"
                    >
                      즐겨찾기
                    </span>
                    <span
                        v-if="getSharedFavoriteLabel(restaurant.id)"
                        class="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-[#fff3cd] text-[#6b4e00] border border-[#ffe8a1]"
                    >
                      {{ getSharedFavoriteLabel(restaurant.id) }}
                    </span>
                    <span>리뷰 {{ restaurant.reviews }}개</span>
                    <span>{{ restaurant.category }}</span>
                  </div>
                    <p class="text-sm font-semibold text-[#1e3a5f] mt-1">
                      {{ restaurant.price }}
                    </p>
                  </div>
                </div>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>

      <div class="relative overflow-hidden rounded-3xl">
        <div class="px-4 pt-5 pb-0 shrink-0 bg-[#f8f9fa] -mb-px">
          <div
              v-if="!isLoggedIn"
              class="mb-3 rounded-2xl border border-[#e9ecef] bg-white py-2 px-4 text-[13px] text-gray-700 whitespace-nowrap text-center"
          >
            로그인하면 취향/예산/구내식당 등 다양한 추천을 받을 수 있어요.
          </div>

          <div v-if="isLoggedIn" class="mb-3">
            <div class="flex items-center gap-2">
              <button
                v-for="option in recommendationButtons"
                :key="option.value"
                type="button"
                @click="handleRecommendationQuickSelect(option.value)"
                :class="`px-2.5 py-1 rounded-md text-[11px] font-semibold whitespace-nowrap transition-colors ${
                  selectedRecommendation === option.value
                    ? 'gradient-primary text-white border border-transparent'
                    : 'bg-white text-gray-700 border border-[#dee2e6] hover:bg-[#f8f9fa]'
                }`"
              >
                <span class="inline-flex items-center gap-1">
                  <span v-if="option.emoji">{{ option.emoji }}</span>
                  <span>{{ option.label }}</span>
                </span>
              </button>
            </div>
          </div>

          <div
            v-if="weatherThemeStyle && selectedRecommendation === RECOMMEND_WEATHER && weatherSummary"
            class="mb-3 rounded-2xl border border-white/60 bg-white/70 backdrop-blur px-4 py-3"
          >
            <div class="flex items-center justify-between gap-4">
              <div class="flex items-center gap-3">
                <span class="text-2xl">{{ weatherThemeStyle.emoji }}</span>
                <div>
                  <p class="text-sm font-semibold" :class="weatherThemeStyle.accentClass">
                    오늘 날씨
                  </p>
                  <p class="text-xs text-gray-700">
                    {{ weatherDisplayLabel }}
                  </p>
                </div>
              </div>
              <div class="text-right">
                <p class="text-lg font-semibold text-[#1e3a5f]">
                  {{ formatTemp(weatherSummary.temp) }}
                </p>
                <p class="text-xs text-gray-600">
                  체감 {{ formatTemp(weatherSummary.feelsLike) }}
                </p>
              </div>
            </div>
          </div>
          <div
            v-else-if="selectedRecommendation === RECOMMEND_WEATHER && weatherError"
            class="mb-3 rounded-2xl border border-[#e9ecef] bg-white px-4 py-3 text-sm text-gray-700"
          >
            {{ weatherError }}
          </div>

          <HomeSearchBar
              :onOpenFilter="openFilterModal"
              :onOpenSearch="openSearchModal"
          />
        </div>

        <div v-if="activeRecommendationHeader" class="mt-3 px-4">
          <HomeRecommendationHeader
              :title="activeRecommendationHeader.title"
              :subtitle="activeRecommendationHeader.subtitle"
              :description="activeRecommendationHeader.description"
              :isLoading="activeRecommendationHeader.isLoading"
              :onClear="activeRecommendationHeader.onClear"
          />
        </div>

        <div class="mt-4 max-h-[60vh] overflow-y-auto px-4 pb-6">
          <HomeRecommendationContent
              :isLoggedIn="isLoggedIn"
              :cafeteriaRecommendations="cafeteriaRecommendations"
              :recommendationButtons="recommendationButtons"
              :selectedRecommendation="selectedRecommendation"
              :recommendWeatherKey="RECOMMEND_WEATHER"
              :recommendTasteKey="RECOMMEND_TASTE"
              :recommendBudgetKey="RECOMMEND_BUDGET"
              :isTrendingSort="isTrendingSort"
              :isTrendingLoading="isTrendingLoading"
              :trendingError="trendingError"
              :trendingCards="trendingCards"
              :isRecommendationLoading="isRecommendationLoading"
              :isCafeteriaLoading="isCafeteriaLoading"
              :tagMappingNotice="tagMappingNotice"
              :tasteRecommendationSummary="tasteRecommendationSummary"
              :filterPerPersonBudgetDisplay="filterPerPersonBudgetDisplay"
              :paginatedRestaurants="paginatedRestaurants"
              :showRouteButton="Boolean(selectedRecommendation)"
              :onCheckRoute="handleCheckRoute"
              :routeLoadingId="routeLoadingId"
              :routeInfo="routeInfo"
              :stickyHeaders="false"
              :hideHeaders="true"
              :onSelectRecommendation="handleRecommendationQuickSelect"
              :onOpenSearch="() => (isSearchOpen = true)"
              :onClearCafeteria="() => clearRecommendation(RECOMMEND_CAFETERIA)"
              :onClearTrending="clearTrendingRecommendation"
              :onClearWeather="() => clearRecommendation(RECOMMEND_WEATHER)"
              :onClearTaste="() => clearRecommendation(RECOMMEND_TASTE)"
              :onClearBudget="() => clearRecommendation(RECOMMEND_BUDGET)"
              :isCafeteriaModalOpen="isCafeteriaModalOpen"
              :isCafeteriaOcrLoading="isCafeteriaOcrLoading"
              :cafeteriaOcrResult="cafeteriaOcrResult"
              :cafeteriaDaysDraft="cafeteriaDaysDraft"
              :cafeteriaOcrError="cafeteriaOcrError"
              :cafeteriaImageUrl="cafeteriaImageUrl"
              :onCafeteriaModalClose="() => (isCafeteriaModalOpen = false)"
              :onCafeteriaFileChange="handleCafeteriaFileChange"
              :onCafeteriaOcr="() => handleCafeteriaOcr(resolveCafeteriaBaseDate())"
              :onCafeteriaConfirm="handleCafeteriaConfirmAndClose"
          />

          <HomePagination
              :show="!cafeteriaRecommendations.length && !isTrendingSort && (selectedRecommendation === RECOMMEND_WEATHER || totalPages > 1)"
              :pageNumbers="pageNumbers"
              :currentPage="currentPage"
              :canGoPrevious="canGoPrevious"
              :canGoNext="canGoNext"
              :onGoPrevious="goToPreviousPage"
              :onGoNext="goToNextPage"
              :onGoToPage="goToPage"
          />
        </div>

        <AppFooter />
      </div>

    </main>

    <BottomNav @home="resetMapToHome" />

    <!-- Filter Modal -->
    <div
        v-if="isFilterOpen"
        class="fixed inset-0 z-[100] bg-black/50 flex items-end"
    >
      <div
          class="w-full max-w-[500px] mx-auto bg-white rounded-t-2xl max-h-[85vh] overflow-y-auto animate-in slide-in-from-bottom duration-300"
      >
        <div
            class="sticky top-0 bg-white border-b border-[#e9ecef] px-4 py-4 flex items-center justify-between"
        >
          <h3 class="text-lg font-semibold text-[#1e3a5f]">필터 및 정렬</h3>
          <button
              @click="closeFilterModal"
              class="text-gray-700 hover:text-[#1e3a5f]"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <div class="p-4 space-y-6">
          <!-- Sort Options -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">정렬</h4>
            <div class="flex flex-wrap gap-2">
              <button
                  v-for="option in sortOptions"
                  :key="option"
                  @click="selectSortOption(option)"
                  :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  filterForm.sort === option
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                {{ option }}
              </button>
            </div>
          </div>

          <!-- Price Range Filter -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
              1인당 가격대
            </h4>
            <div class="flex flex-wrap gap-2">
              <button
                  v-for="range in priceRanges"
                  :key="range"
                  @click="toggleFilterPriceRange(range)"
                  :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  filterForm.priceRange === range
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                {{ range }}
              </button>
            </div>
          </div>

          <!-- 로그인 사용자만 노출 -->
          <div v-if="isLoggedIn">
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">추천옵션</h4>
            <div class="flex flex-wrap gap-2">
              <button
                  v-for="option in recommendationOptions"
                  :key="option"
                  type="button"
                  @click="toggleRecommendationOption(option)"
                  :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  filterForm.recommendation === option
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                {{ option }}
              </button>
            </div>
            <div
                v-if="filterForm.recommendation === RECOMMEND_BUDGET"
                class="mt-4 rounded-xl border border-[#e9ecef] bg-[#f8f9fa] p-4 space-y-4"
            >
              <div class="flex items-center justify-between">
                <p class="text-sm font-semibold text-[#1e3a5f]">총 예산</p>
                <p class="text-sm font-semibold text-[#ff6b4a]">
                  {{ filterBudgetDisplay }}
                </p>
              </div>
              <div class="space-y-2">
                <input
                    type="range"
                    :min="filterBudgetMin"
                    :max="filterBudgetMax"
                    :step="filterBudgetStep"
                    v-model.number="filterBudget"
                    :style="{
                    background: `linear-gradient(to right, #ff6b4a 0%, #ff6b4a ${filterBudgetPercent}%, #e9ecef ${filterBudgetPercent}%, #e9ecef 100%)`,
                  }"
                    class="w-full h-2 rounded-lg appearance-none cursor-pointer [&::-webkit-slider-thumb]:appearance-none [&::-webkit-slider-thumb]:w-5 [&::-webkit-slider-thumb]:h-5 [&::-webkit-slider-thumb]:rounded-full [&::-webkit-slider-thumb]:bg-[#ff6b4a] [&::-webkit-slider-thumb]:cursor-pointer"
                />
                <div class="flex justify-between text-xs text-gray-700">
                  <span>0원</span>
                  <span>50만원 이상</span>
                </div>
              </div>
              <div class="flex items-center justify-between gap-3">
                <label class="text-sm font-semibold text-[#1e3a5f]">
                  인원수
                </label>
                <div class="flex items-center gap-2">
                  <input
                      type="number"
                      min="4"
                      max="12"
                      v-model.number="filterPartySize"
                      class="w-20 px-3 py-2 rounded-lg border border-[#dee2e6] text-sm text-[#1e3a5f] bg-white focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
                  />
                  <span class="text-sm text-gray-700">명</span>
                </div>
              </div>
              <div class="text-xs text-gray-700 text-right">
                1인당 {{ filterPerPersonBudgetDisplay }}
              </div>
            </div>
            <div
                v-if="filterForm.recommendation === RECOMMEND_CAFETERIA"
                class="mt-3 flex items-center justify-end gap-2"
            >
              <div class="flex items-center gap-2">
                <button
                    type="button"
                    class="px-3 py-2 rounded-lg text-xs font-semibold border border-[#dee2e6] text-gray-700 bg-white hover:bg-[#f8f9fa] disabled:opacity-60 disabled:cursor-not-allowed"
                    :disabled="isCheckingMenus"
                    @click="handleCafeteriaMenuEdit"
                >
                  {{ hasConfirmedMenus ? '구내식당 메뉴 수정' : '구내식당 메뉴 입력' }}
                </button>
                <button
                    v-if="hasConfirmedMenus"
                    type="button"
                    class="px-3 py-2 rounded-lg text-xs font-semibold gradient-primary text-white disabled:opacity-60 disabled:cursor-not-allowed"
                    :disabled="isCheckingMenus"
                    @click="handleCafeteriaRecommendNow"
                >
                  추천받기
                </button>
              </div>
            </div>
          </div>
          <div v-else class="mt-2 text-xs text-gray-700">
            로그인 후 추천 옵션을 사용할 수 있습니다.
          </div>
        </div>

        <div
            class="sticky bottom-0 bg-white border-t border-[#e9ecef] p-4 flex gap-3"
        >
          <Button
              @click="resetFilters"
              variant="outline"
              class="flex-1 h-12 text-gray-700 border-[#dee2e6] hover:bg-[#f8f9fa] rounded-xl bg-transparent"
          >
            초기화
          </Button>
          <Button
              @click="applyFilters"
              class="flex-1 h-12 gradient-primary text-white rounded-xl"
          >
            적용하기
          </Button>
        </div>
      </div>
    </div>

    <!-- Search Modal -->
    <div
        v-if="isSearchOpen"
        class="fixed inset-0 z-[100] bg-black/50 flex items-end"
    >
      <div
          class="w-full max-w-[500px] mx-auto bg-white rounded-t-2xl max-h-[85vh] animate-in slide-in-from-bottom duration-300 flex flex-col"
      >
        <div
            class="sticky top-0 z-10 bg-white border-b border-[#e9ecef] px-4 py-4 flex items-center justify-between"
        >
          <h3 class="text-lg font-semibold text-[#1e3a5f]">검색 필터</h3>
          <button
              @click="isSearchOpen = false"
              class="text-gray-700 hover:text-[#1e3a5f]"
          >
            <X class="w-6 h-6" />
          </button>
        </div>

        <div
            ref="searchModalScrollRef"
            class="flex-1 overflow-y-auto"
            @scroll="handleSearchModalScroll"
        >
          <div class="p-4 space-y-6">
          <!-- Reservation Date -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">예약 날짜</h4>
            <div class="relative">
              <button
                  type="button"
                  @click="toggleCalendar"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg text-sm flex items-center justify-between text-left text-gray-700 focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
              >
                <span
                    :class="searchDate ? 'text-[#1e3a5f]' : 'text-gray-700'"
                >{{ formattedSearchDate }}</span
                >
                <Calendar class="w-5 h-5 text-[#ff6b4a]" />
              </button>

              <div
                  v-if="isCalendarOpen"
                  class="absolute right-0 top-full mt-2 w-72 bg-white border border-[#e9ecef] rounded-xl shadow-card z-20"
              >
                <div
                    class="flex items-center justify-between px-4 py-3 border-b border-[#e9ecef]"
                >
                  <button
                      @click.stop="previousCalendarMonth"
                      class="p-1 rounded-full hover:bg-[#f8f9fa] transition-colors"
                  >
                    <ChevronLeft class="w-4 h-4 text-gray-700" />
                  </button>
                  <span class="text-sm font-semibold text-[#1e3a5f]">{{
                      formattedCalendarMonth
                    }}</span>
                  <button
                      @click.stop="nextCalendarMonth"
                      class="p-1 rounded-full hover:bg-[#f8f9fa] transition-colors"
                  >
                    <ChevronRight class="w-4 h-4 text-gray-700" />
                  </button>
                </div>
                <div class="px-4 py-3">
                  <div
                      class="grid grid-cols-7 text-center text-xs text-gray-700 mb-2"
                  >
                    <span
                        v-for="dayName in weekdayLabels"
                        :key="dayName"
                        class="py-1"
                    >{{ dayName }}</span
                    >
                  </div>
                  <div class="grid grid-cols-7 gap-1">
                    <button
                        v-for="(day, index) in calendarDays"
                        :key="index"
                        :disabled="!day || isDateDisabled(day)"
                        @click.stop="selectCalendarDay(day)"
                        :class="[
                        'w-9 h-9 rounded-full text-sm transition-colors',
                        !day ? 'cursor-default opacity-0' : '',
                        day && isDateDisabled(day) ? 'text-[#c7cdd3] cursor-not-allowed' : '',
                        day && !isDateDisabled(day) && isCalendarSelectedDay(day)
                          ? 'bg-[#ff6b4a] text-white font-semibold'
                          : day && !isDateDisabled(day) ? 'text-gray-700 hover:bg-[#f1f3f5]' : '',
                      ]"
                    >
                      {{ day }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Time Slot Selection -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">시간대</h4>
            <div class="grid grid-cols-4 gap-2">
              <button
                  v-for="time in timeSlots"
                  :key="time"
                  @click="searchTime = time"
                  :class="`px-4 py-3 rounded-lg text-sm font-medium transition-colors ${
                  searchTime === time
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                {{ time }}
              </button>
            </div>
          </div>

          <!-- Category -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">음식 종류</h4>
            <div class="flex flex-wrap gap-2">
              <button
                  v-for="category in categories"
                  :key="category"
                  @click="toggleSearchCategory(category)"
                  :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  searchCategories.includes(category)
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                {{ category }}
              </button>
            </div>
          </div>

          <!-- Party Size -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">인원 수</h4>
            <div class="flex items-center gap-4">
              <button
                  @click="searchPartySize = Math.max(4, searchPartySize - 1)"
                  class="w-10 h-10 rounded-lg border border-[#dee2e6] flex items-center justify-center hover:bg-[#f8f9fa] transition-colors disabled:opacity-50"
                  :disabled="searchPartySize <= 4"
              >
                <Minus class="w-4 h-4 text-gray-700" />
              </button>
              <div class="flex-1 text-center">
                <span class="text-2xl font-semibold text-[#1e3a5f]">{{
                    searchPartySize
                  }}</span>
                <span class="text-sm text-gray-700 ml-1">명</span>
              </div>
              <button
                  @click="searchPartySize = Math.min(12, searchPartySize + 1)"
                  class="w-10 h-10 rounded-lg border border-[#dee2e6] flex items-center justify-center hover:bg-[#f8f9fa] transition-colors disabled:opacity-50"
                  :disabled="searchPartySize >= 12"
              >
                <Plus class="w-4 h-4 text-gray-700" />
              </button>
            </div>
          </div>

          <!-- Pre-order/Pre-payment -->
          <div class="flex items-center justify-between">
            <h4 class="text-sm font-semibold text-[#1e3a5f]">선주문/선결제 가능 식당</h4>
            <button
                @click="searchPreorder = !searchPreorder"
                :class="[
                'relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:ring-offset-2',
                searchPreorder ? 'bg-[#ff6b4a]' : 'bg-gray-200',
              ]"
            >
              <span
                  :class="[
                  'inline-block h-4 w-4 transform rounded-full bg-white transition-transform',
                  searchPreorder ? 'translate-x-6' : 'translate-x-1',
                ]"
              />
            </button>
          </div>

          <!-- Distance Filter -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">거리</h4>
            <div class="flex flex-wrap gap-2">
              <button
                  v-for="distance in distances"
                  :key="distance"
                  @click="searchDistance = distance"
                  :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  searchDistance === distance
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                {{ distance }}
              </button>
            </div>
          </div>

          <!-- Restaurant Tags -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">식당 태그</h4>
            <div class="flex flex-wrap gap-2">
              <button
                  v-for="tag in restaurantTags"
                  :key="tag"
                  @click="toggleSearchTag(tag)"
                  :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  searchTags.includes(tag)
                    ? 'gradient-primary text-white'
                    : 'bg-[#f8f9fa] text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                <span class="inline-flex items-center gap-2">
                  <span v-if="getRestaurantTagEmoji(tag)" class="text-base">
                    {{ getRestaurantTagEmoji(tag) }}
                  </span>
                  <span>{{ tag }}</span>
                </span>
              </button>
            </div>
          </div>

          <!-- Avoid Ingredients -->
          <div>
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">기피 재료</h4>
            <div class="flex flex-wrap gap-2">
              <button
                  v-for="ingredient in ingredients"
                  :key="ingredient"
                  @click="toggleAvoidIngredient(ingredient)"
                  :class="`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                  avoidIngredients.includes(ingredient)
                    ? 'bg-red-500 text-white'
                    : 'bg-[#f8f9fa] text-gray-700 hover:bg-[#e9ecef]'
                }`"
              >
                <span class="inline-flex items-center gap-2">
                  <span v-if="getIngredientEmoji(ingredient)" class="text-base">
                    {{ getIngredientEmoji(ingredient) }}
                  </span>
                  <span>{{ ingredient }}</span>
                </span>
              </button>
            </div>
          </div>
        </div>

          <div
              class="sticky bottom-0 bg-white border-t border-[#e9ecef] p-4 flex gap-3"
          >
            <Button
                @click="resetSearch"
                variant="outline"
                class="flex-1 h-12 text-gray-700 border-[#dee2e6] hover:bg-[#f8f9fa] rounded-xl bg-transparent"
            >
              초기화
            </Button>
            <Button
                @click="applySearch"
                class="flex-1 h-12 gradient-primary text-white rounded-xl"
            >
              검색하기
            </Button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.wrap_btn_zoom,
.wrap_scale {
  display: none !important;
}
</style>
