import { computed, reactive } from "vue";
import { useAccountStore } from "@/stores/account";
import { useFavoritesStore } from "@/stores/favorites";
import httpRequest from "@/router/httpRequest";

const fallbackState = reactive({
  favoriteRestaurantIds: [],
  isLoading: false,
  hasLoaded: false,
  error: null,
  loginPromptOpen: false,
  loginRedirectPath: "",
});

const normalizeIdList = (ids = []) => {
  const normalized = ids
    .map((id) => Number(id))
    .filter((id) => Number.isFinite(id));
  return Array.from(new Set(normalized));
};

const resolveUserId = (accountStore) => {
  const member = accountStore?.member;
  const rawId = member?.id ?? member?.userId ?? member?.memberId;
  if (rawId === null || rawId === undefined) return null;
  const parsed = Number(rawId);
  return Number.isNaN(parsed) ? null : parsed;
};

const buildFallbackActions = () => ({
  setFavorites(ids) {
    fallbackState.favoriteRestaurantIds = normalizeIdList(ids);
  },
  clearFavorites() {
    fallbackState.favoriteRestaurantIds = [];
    fallbackState.error = null;
    fallbackState.isLoading = false;
    fallbackState.hasLoaded = false;
  },
  openLoginPrompt(redirectPath = "") {
    fallbackState.loginPromptOpen = true;
    fallbackState.loginRedirectPath = redirectPath || "";
  },
  closeLoginPrompt() {
    fallbackState.loginPromptOpen = false;
    fallbackState.loginRedirectPath = "";
  },
  async fetchFavorites(userId) {
    if (!userId) {
      this.clearFavorites();
      return;
    }
    fallbackState.isLoading = true;
    fallbackState.error = null;
    try {
      const response = await httpRequest.get("/api/bookmark/list", { userId });
      const data = Array.isArray(response.data) ? response.data : [];
      fallbackState.favoriteRestaurantIds = normalizeIdList(
        data.map((item) => item.restaurantId)
      );
    } catch (error) {
      fallbackState.error = "즐겨찾기 목록 조회 실패";
      fallbackState.favoriteRestaurantIds = [];
    } finally {
      fallbackState.isLoading = false;
      fallbackState.hasLoaded = true;
    }
  },
  async addFavorite(userId, restaurantId) {
    if (!userId) return false;
    const id = Number(restaurantId);
    if (!Number.isFinite(id)) return false;
    if (fallbackState.favoriteRestaurantIds.includes(id)) return true;
    fallbackState.hasLoaded = true;
    const previous = [...fallbackState.favoriteRestaurantIds];
    fallbackState.favoriteRestaurantIds = [...previous, id];
    try {
      await httpRequest.post("/api/bookmark", { userId, restaurantId: id });
      return true;
    } catch (error) {
      fallbackState.favoriteRestaurantIds = previous;
      throw error;
    }
  },
  async removeFavorite(userId, restaurantId) {
    if (!userId) return false;
    const id = Number(restaurantId);
    if (!Number.isFinite(id)) return false;
    fallbackState.hasLoaded = true;
    const previous = [...fallbackState.favoriteRestaurantIds];
    fallbackState.favoriteRestaurantIds = previous.filter((item) => item !== id);
    try {
      await httpRequest.delete("/api/bookmark", {
        data: { userId, restaurantId: id },
      });
      return true;
    } catch (error) {
      fallbackState.favoriteRestaurantIds = previous;
      throw error;
    }
  },
  async toggleFavorite(userId, restaurantId) {
    const id = Number(restaurantId);
    if (fallbackState.favoriteRestaurantIds.includes(id)) {
      return this.removeFavorite(userId, id);
    }
    return this.addFavorite(userId, id);
  },
  isFavorite(restaurantId) {
    const id = Number(restaurantId);
    if (!Number.isFinite(id)) return false;
    return fallbackState.favoriteRestaurantIds.includes(id);
  },
});

export const useFavorites = () => {
  let store = null;

  try {
    store = useFavoritesStore();
  } catch (error) {
    store = null;
  }

  const accountStore = useAccountStore?.();
  const fallbackActions = buildFallbackActions();

  const isFavorite = (restaurantId) =>
    store ? store.isFavorite(restaurantId) : fallbackActions.isFavorite(restaurantId);

  const openLoginPrompt = (redirectPath = "") =>
    store
      ? store.openLoginPrompt(redirectPath)
      : fallbackActions.openLoginPrompt(redirectPath);

  const closeLoginPrompt = () =>
    store ? store.closeLoginPrompt() : fallbackActions.closeLoginPrompt();

  const ensureUserId = () => resolveUserId(accountStore);

  const toggleFavoriteWithPrompt = async (restaurantId, options = {}) => {
    const { confirmOnRemove = true, redirectPath = "" } = options;
    const userId = ensureUserId();
    if (!userId) {
      openLoginPrompt(redirectPath);
      return { status: "login_required" };
    }
    if (confirmOnRemove && isFavorite(restaurantId)) {
      if (!confirm("즐겨찾기를 해제하시겠습니까?")) {
        return { status: "cancelled" };
      }
    }
    try {
      await (store
        ? store.toggleFavorite(userId, restaurantId)
        : fallbackActions.toggleFavorite(userId, restaurantId));
      return { status: "success" };
    } catch (error) {
      return { status: "error", error };
    }
  };

  const fetchFavorites = (userId = ensureUserId()) =>
    store ? store.fetchFavorites(userId) : fallbackActions.fetchFavorites(userId);

  return {
    favoriteRestaurantIds: computed(() =>
      store ? store.favoriteRestaurantIds : fallbackState.favoriteRestaurantIds
    ),
    isLoading: computed(() => (store ? store.isLoading : fallbackState.isLoading)),
    hasLoaded: computed(() => (store ? store.hasLoaded : fallbackState.hasLoaded)),
    error: computed(() => (store ? store.error : fallbackState.error)),
    loginPromptOpen: computed(() =>
      store ? store.loginPromptOpen : fallbackState.loginPromptOpen
    ),
    loginRedirectPath: computed(() =>
      store ? store.loginRedirectPath : fallbackState.loginRedirectPath
    ),
    userId: computed(() => ensureUserId()),
    isLoggedIn: computed(() => Boolean(ensureUserId())),
    isFavorite,
    setFavorites: (ids) =>
      store ? store.setFavorites(ids) : fallbackActions.setFavorites(ids),
    clearFavorites: () =>
      store ? store.clearFavorites() : fallbackActions.clearFavorites(),
    openLoginPrompt,
    closeLoginPrompt,
    fetchFavorites,
    addFavorite: (userId, restaurantId) =>
      store
        ? store.addFavorite(userId, restaurantId)
        : fallbackActions.addFavorite(userId, restaurantId),
    removeFavorite: (userId, restaurantId) =>
      store
        ? store.removeFavorite(userId, restaurantId)
        : fallbackActions.removeFavorite(userId, restaurantId),
    toggleFavorite: (userId, restaurantId) =>
      store
        ? store.toggleFavorite(userId, restaurantId)
        : fallbackActions.toggleFavorite(userId, restaurantId),
    toggleFavoriteWithPrompt,
  };
};
