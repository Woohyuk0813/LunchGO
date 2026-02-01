import { ref } from "vue";
import httpRequest from "@/router/httpRequest.js";
import { getRestaurantById } from "@/data/restaurants";
import { useAccountStore } from "@/stores/account";

export const useTagMappingRecommendation = () => {
  const isTagMappingLoading = ref(false);
  const tagMappingRecommendations = ref([]);
  const tagMappingError = ref(null);
  const tagMappingMessageCode = ref(null);
  const tagMappingRaw = ref([]);
  const accountStore = useAccountStore();

  const getStoredMember = () => {
    const raw = localStorage.getItem("member");
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  };

  const resolveUserId =() => {
    const member = accountStore.member || getStoredMember();
    return member?.id || null;
  };

  const mapToRestaurants = (mappings) =>
    mappings
      .map((mapping) => {
        const restaurant = getRestaurantById(mapping?.restaurantId);
        if (!restaurant) {
          return null;
        }
        return {
          ...restaurant,
          tagMapping: {
            restaurantId: mapping.restaurantId,
            likeScore: mapping.likeScore ?? 0,
            dislikePenalty: mapping.dislikePenalty ?? 0,
            finalScore: mapping.finalScore ?? 0,
          },
        };
      })
      .filter(Boolean);

  const fetchTagMappingRecommendations = async () => {
    const resolvedUserId = resolveUserId();
    if (!resolvedUserId) {
      tagMappingMessageCode.value = "LOGIN_REQUIRED"; //로그인 필요
      tagMappingRecommendations.value = [];
      tagMappingRaw.value = [];
      tagMappingError.value = null;
      return;
    }

    isTagMappingLoading.value = true;
    tagMappingError.value = null;
    tagMappingMessageCode.value = null;

    try {
      const response = await httpRequest.get(
        `/api/restaurants/mapping/${resolvedUserId}`
      );
      const data = response?.data;

      if (data && !Array.isArray(data) && data.code) {
        tagMappingMessageCode.value = data.code;
        tagMappingRecommendations.value = [];
        tagMappingRaw.value = [];
        return;
      }

      const mappings = Array.isArray(data) ? data : [];
      tagMappingRaw.value = mappings;
      tagMappingRecommendations.value = mapToRestaurants(mappings);
    } catch (error) {
      const status = error?.response?.status;
      console.error("Errors occurs: ", error);
      tagMappingRecommendations.value = [];
      tagMappingRaw.value = [];
      if (status === 404) {
        tagMappingMessageCode.value = null;
        tagMappingError.value = "추천 정보를 불러오지 못했습니다";
      } else {
        tagMappingError.value = "Failed to fetch tag mapping recommendations";
      }
    } finally {
      isTagMappingLoading.value = false;
    }
  };

  const clearTagMappingRecommendations = () => {
    tagMappingRecommendations.value = [];
    tagMappingRaw.value = [];
    tagMappingError.value = null;
    tagMappingMessageCode.value = null;
  };

  return {
    isTagMappingLoading,
    tagMappingRecommendations,
    tagMappingError,
    tagMappingMessageCode,
    tagMappingRaw,
    fetchTagMappingRecommendations,
    clearTagMappingRecommendations,
  };
};
