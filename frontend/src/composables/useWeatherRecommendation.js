import { ref } from "vue";
import httpRequest from "@/router/httpRequest.js";
import { restaurants as restaurantData } from "@/data/restaurants";

const normalizeTag = (tag) => {
  if (!tag) return "";
  if (typeof tag === "string") return tag;
  if (tag.name) return tag.name;
  if (tag.content) return tag.content;
  return String(tag);
};

const extractRestaurantTags = (restaurant) => {
  const sources = [
    restaurant?.restaurantTags,
    restaurant?.tags,
    restaurant?.topTags,
  ];
  return sources
    .flatMap((tags) => tags || [])
    .map(normalizeTag)
    .map((value) => value.trim())
    .filter(Boolean);
};

const normalizeWeatherPayload = (payload) => {
  if (!payload) return null;
  const data = payload.data ?? payload;
  if (!data || typeof data !== "object") return null;
  return {
    temp: typeof data.temp === "number" ? data.temp : Number(data.temp),
    feelsLike:
      typeof data.feelsLike === "number" ? data.feelsLike : Number(data.feelsLike),
    condition: data.condition || "",
    description: data.description || "",
  };
};

const resolveWeatherProfile = (weather) => {
  if (!weather) return null;
  const condition = String(weather.condition || "").toLowerCase();
  const temp = Number.isFinite(weather.temp)
    ? weather.temp
    : weather.feelsLike;

  if (["rain", "drizzle", "thunderstorm"].includes(condition)) {
    return "rain";
  }
  if (condition === "snow") {
    return "snow";
  }
  if (Number.isFinite(temp) && temp >= 28) return "hot";
  if (Number.isFinite(temp) && temp <= 7) return "cold";
  return "mild";
};

const weatherKeywords = {
  rain: ["국물", "찌개", "전골", "칼국수", "우동", "짬뽕", "수제비"],
  snow: ["국물", "탕", "전골", "보양", "뜨끈", "곰탕", "설렁탕", "갈비탕"],
  hot: ["냉면", "소바", "빙수", "샐러드", "회", "초밥", "시원", "냉"],
  cold: ["국", "탕", "찌개", "전골", "칼국수", "우동", "라면", "뜨끈", "따뜻"],
  mild: ["가벼운", "정식", "한식", "분식", "파스타", "덮밥", "샌드위치"],
};

const buildRestaurantText = (restaurant) => {
  const menus = restaurant?.menus || [];
  const menuText = menus
    .map((menu) => [menu?.name, menu?.description].filter(Boolean).join(" "))
    .join(" ");
  return [
    restaurant?.name,
    restaurant?.category,
    restaurant?.tagline,
    restaurant?.description,
    menuText,
  ]
    .filter(Boolean)
    .join(" ")
    .toLowerCase();
};

const scoreRestaurant = (restaurant, keywords) => {
  const tagList = extractRestaurantTags(restaurant).map((tag) =>
    tag.toLowerCase()
  );
  const text = buildRestaurantText(restaurant);

  let score = 0;
  keywords.forEach((keyword) => {
    const lowered = keyword.toLowerCase();
    if (tagList.some((tag) => tag.includes(lowered))) {
      score += 2;
    }
    if (text.includes(lowered)) {
      score += 1;
    }
  });
  return score;
};

export const useWeatherRecommendation = () => {
  const isWeatherLoading = ref(false);
  const weatherRecommendations = ref([]);
  const weatherError = ref(null);
  const weatherSummary = ref(null);

  const fetchWeatherRecommendations = async (coords) => {
    if (!coords || !Number.isFinite(coords.lat) || !Number.isFinite(coords.lng)) {
      weatherRecommendations.value = [];
      weatherSummary.value = null;
      weatherError.value = "위치 정보가 유효하지 않습니다.";
      return;
    }

    isWeatherLoading.value = true;
    weatherError.value = null;

    try {
      const response = await httpRequest.get("/api/weather/current", {
        lat: coords.lat,
        lon: coords.lng,
      });
      const weather = normalizeWeatherPayload(response?.data);
      if (!weather) {
        throw new Error("invalid-weather-response");
      }

      weatherSummary.value = weather;
      const profile = resolveWeatherProfile(weather);
      const keywords = weatherKeywords[profile] || [];
      const scored = restaurantData
        .map((restaurant) => ({
          restaurant,
          score: scoreRestaurant(restaurant, keywords),
        }))
        .filter((item) => item.score > 0)
        .sort((a, b) => b.score - a.score)
        .map((item) => item.restaurant);

      weatherRecommendations.value = scored.length ? scored : restaurantData;
    } catch (error) {
      console.error("날씨 추천 실패:", error);
      weatherRecommendations.value = [];
      weatherSummary.value = null;
      weatherError.value = "날씨 정보를 가져오지 못했습니다.";
    } finally {
      isWeatherLoading.value = false;
    }
  };

  const clearWeatherRecommendations = () => {
    weatherRecommendations.value = [];
    weatherSummary.value = null;
    weatherError.value = null;
  };

  return {
    isWeatherLoading,
    weatherRecommendations,
    weatherError,
    weatherSummary,
    fetchWeatherRecommendations,
    clearWeatherRecommendations,
  };
};
