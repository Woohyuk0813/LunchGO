import { ref } from "vue";
import httpRequest from "@/router/httpRequest";

const defaultStorageKey = "cafeteriaRecommendations";

export const useCafeteriaRecommendation = ({ userId, storageKey } = {}) => {
  const cafeteriaRecommendations = ref([]);
  const cafeteriaFile = ref(null);
  const cafeteriaOcrResult = ref(null);
  const cafeteriaOcrError = ref("");
  const cafeteriaDaysDraft = ref([]);
  const cafeteriaImageUrl = ref("");
  const cafeteriaRawText = ref("");
  const isCafeteriaOcrLoading = ref(false);
  const isCafeteriaModalOpen = ref(false);
  const hasConfirmedMenus = ref(false);
  const isCheckingMenus = ref(false);

  const resolvedStorageKey = storageKey || defaultStorageKey;

  const resetCafeteriaModalState = () => {
    cafeteriaFile.value = null;
    cafeteriaOcrResult.value = null;
    cafeteriaOcrError.value = "";
    cafeteriaDaysDraft.value = [];
    cafeteriaImageUrl.value = "";
    cafeteriaRawText.value = "";
  };

  const storeRecommendations = (recommendations) => {
    cafeteriaRecommendations.value = recommendations;
    sessionStorage.setItem(
      resolvedStorageKey,
      JSON.stringify(cafeteriaRecommendations.value)
    );
  };

  const loadRecommendationsFromStorage = () => {
    const storedRecommendations = sessionStorage.getItem(resolvedStorageKey);
    if (!storedRecommendations) {
      return;
    }
    try {
      cafeteriaRecommendations.value = JSON.parse(storedRecommendations);
    } catch (error) {
      console.error("추천 데이터 복원 실패:", error);
      sessionStorage.removeItem(resolvedStorageKey);
    }
  };

  const clearCafeteriaRecommendations = () => {
    cafeteriaRecommendations.value = [];
    sessionStorage.removeItem(resolvedStorageKey);
  };

  const resolveUserId = () => {
    if (typeof userId === "function") return userId();
    if (userId && typeof userId === "object" && "value" in userId) {
      return userId.value;
    }
    return userId ?? null;
  };

  const fetchCafeteriaWeekMenus = async (baseDate) => {
    const response = await httpRequest.get("/api/cafeteria/menus/week", {
      userId: resolveUserId(),
      baseDate,
    });
    return response.data;
  };

  const fetchCafeteriaRecommendations = async (baseDate, limit = 2) => {
    const response = await httpRequest.get("/api/cafeteria/recommendations", {
      userId: resolveUserId(),
      baseDate,
      limit,
    });
    storeRecommendations(response.data?.recommendations ?? []);
  };

  const handleCafeteriaFileChange = (file) => {
    cafeteriaFile.value = file;
    cafeteriaOcrResult.value = null;
    cafeteriaDaysDraft.value = [];
    cafeteriaOcrError.value = "";
  };

  const handleCafeteriaOcr = async (baseDate) => {
    if (!cafeteriaFile.value) {
      cafeteriaOcrError.value = "먼저 메뉴 사진을 선택해주세요.";
      return;
    }

    const formData = new FormData();
    formData.append("file", cafeteriaFile.value);

    isCafeteriaOcrLoading.value = true;
    cafeteriaOcrError.value = "";

    try {
      const response = await httpRequest.post("/api/cafeteria/menus/ocr", formData, {
        params: { userId: resolveUserId(), baseDate },
        headers: { "Content-Type": "multipart/form-data" },
      });
      cafeteriaOcrResult.value = response.data;
      cafeteriaDaysDraft.value = response.data?.days ?? [];
      cafeteriaImageUrl.value = response.data?.imageUrl ?? "";
      cafeteriaRawText.value = response.data?.rawText ?? "";
    } catch (error) {
      console.error("구내식당 OCR 실패:", error);
      cafeteriaOcrError.value = "OCR 인식에 실패했습니다. 다시 시도해주세요.";
    } finally {
      isCafeteriaOcrLoading.value = false;
    }
  };

  const handleCafeteriaConfirm = async (days, baseDate) => {
    if (!days || !days.length) {
      cafeteriaOcrError.value = "날짜별 메뉴를 입력해주세요.";
      return false;
    }

    try {
      await httpRequest.post("/api/cafeteria/menus/confirm", {
        userId: resolveUserId(),
        imageUrl: cafeteriaImageUrl.value,
        rawText: cafeteriaRawText.value,
        days,
      });
      await fetchCafeteriaRecommendations(baseDate);
      isCafeteriaModalOpen.value = false;
      return true;
    } catch (error) {
      console.error("구내식당 메뉴 확정 실패:", error);
      cafeteriaOcrError.value = "메뉴 저장에 실패했습니다. 다시 시도해주세요.";
      return false;
    }
  };

  const checkCafeteriaMenuStatus = async (baseDate) => {
    isCheckingMenus.value = true;
    try {
      const response = await fetchCafeteriaWeekMenus(baseDate);
      const hasMenus =
        response?.days?.some((day) => day.menus && day.menus.length) ||
        Boolean(response?.imageUrl);
      hasConfirmedMenus.value = hasMenus;
      return hasMenus;
    } catch (error) {
      console.error("구내식당 메뉴 조회 실패:", error);
      hasConfirmedMenus.value = false;
      return false;
    } finally {
      isCheckingMenus.value = false;
    }
  };

  const openCafeteriaModal = () => {
    resetCafeteriaModalState();
    isCafeteriaModalOpen.value = true;
  };

  const openCafeteriaModalWithExisting = async (baseDate) => {
    resetCafeteriaModalState();
    try {
      const response = await fetchCafeteriaWeekMenus(baseDate);
      cafeteriaDaysDraft.value = response?.days ?? [];
      cafeteriaImageUrl.value = response?.imageUrl ?? "";
      cafeteriaOcrResult.value = cafeteriaDaysDraft.value.length
        ? { ocrSuccess: true, detectedMenus: [] }
        : null;
    } catch (error) {
      console.error("구내식당 메뉴 조회 실패:", error);
    } finally {
      isCafeteriaModalOpen.value = true;
    }
  };

  const requestCafeteriaRecommendations = async (baseDate) => {
    await fetchCafeteriaRecommendations(baseDate);
  };

  return {
    cafeteriaRecommendations,
    cafeteriaOcrResult,
    cafeteriaOcrError,
    cafeteriaDaysDraft,
    cafeteriaImageUrl,
    cafeteriaRawText,
    cafeteriaFile,
    isCafeteriaOcrLoading,
    isCafeteriaModalOpen,
    hasConfirmedMenus,
    isCheckingMenus,
    resetCafeteriaModalState,
    loadRecommendationsFromStorage,
    clearCafeteriaRecommendations,
    handleCafeteriaFileChange,
    handleCafeteriaOcr,
    handleCafeteriaConfirm,
    checkCafeteriaMenuStatus,
    openCafeteriaModal,
    openCafeteriaModalWithExisting,
    requestCafeteriaRecommendations,
  };
};
