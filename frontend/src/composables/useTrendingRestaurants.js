import { ref } from "vue";
import axios from "axios";

export const useTrendingRestaurants = () => {
  const isTrendingLoading = ref(false);
  const trendingRestaurants = ref([]);
  const trendingError = ref(null);

  const fetchTrendingRestaurants = async ({ days = 7, limit = 10 } = {}) => {
    isTrendingLoading.value = true;
    trendingError.value = null;
    try {
      const response = await axios.get("/api/restaurants/trending", {
        params: { days, limit },
      });
      trendingRestaurants.value = Array.isArray(response.data)
        ? response.data
        : [];
    } catch (error) {
      console.error("트렌딩 식당 로드 실패:", error);
      trendingError.value = "트렌딩 식당을 불러오지 못했어요.";
      trendingRestaurants.value = [];
    } finally {
      isTrendingLoading.value = false;
    }
  };

  const clearTrendingRestaurants = () => {
    trendingRestaurants.value = [];
    trendingError.value = null;
  };

  return {
    isTrendingLoading,
    trendingRestaurants,
    trendingError,
    fetchTrendingRestaurants,
    clearTrendingRestaurants,
  };
};
