import { defineStore } from "pinia";
import httpRequest from "@/router/httpRequest";

const normalizeIdList = (ids = []) => {
  const normalized = ids
    .map((id) => Number(id))
    .filter((id) => Number.isFinite(id));
  return Array.from(new Set(normalized));
};

export const useFavoritesStore = defineStore("favorites", {
  state: () => ({
    favoriteRestaurantIds: [],
    isLoading: false,
    hasLoaded: false,
    error: null,
    loginPromptOpen: false,
    loginRedirectPath: "",
  }),
  getters: {
    isFavorite: (state) => (restaurantId) => {
      const id = Number(restaurantId);
      if (!Number.isFinite(id)) return false;
      return state.favoriteRestaurantIds.includes(id);
    },
  },
  actions: {
    setFavorites(ids) {
      this.favoriteRestaurantIds = normalizeIdList(ids);
    },
    clearFavorites() {
      this.favoriteRestaurantIds = [];
      this.error = null;
      this.isLoading = false;
      this.hasLoaded = false;
    },
    openLoginPrompt(redirectPath = "") {
      this.loginPromptOpen = true;
      this.loginRedirectPath = redirectPath || "";
    },
    closeLoginPrompt() {
      this.loginPromptOpen = false;
      this.loginRedirectPath = "";
    },
    async fetchFavorites(userId) {
      if (!userId) {
        this.clearFavorites();
        return;
      }

      this.isLoading = true;
      this.error = null;

      try {
        const response = await httpRequest.get("/api/bookmark/list", {
          userId,
        });
        const data = Array.isArray(response.data) ? response.data : [];
        this.setFavorites(data.map((item) => item.restaurantId));
      } catch (error) {
        this.error = "즐겨찾기 목록 조회 실패";
        this.favoriteRestaurantIds = [];
      } finally {
        this.isLoading = false;
        this.hasLoaded = true;
      }
    },
    async addFavorite(userId, restaurantId) {
      if (!userId) return false;
      const id = Number(restaurantId);
      if (!Number.isFinite(id)) return false;
      if (this.favoriteRestaurantIds.includes(id)) return true;
      this.hasLoaded = true;

      const previous = [...this.favoriteRestaurantIds];
      this.favoriteRestaurantIds = [...previous, id];

      try {
        await httpRequest.post("/api/bookmark", {
          userId,
          restaurantId: id,
        });
        return true;
      } catch (error) {
        this.favoriteRestaurantIds = previous;
        throw error;
      }
    },
    async removeFavorite(userId, restaurantId) {
      if (!userId) return false;
      const id = Number(restaurantId);
      if (!Number.isFinite(id)) return false;
      this.hasLoaded = true;
      const previous = [...this.favoriteRestaurantIds];
      this.favoriteRestaurantIds = previous.filter((item) => item !== id);

      try {
        await httpRequest.delete("/api/bookmark", {
          data: { userId, restaurantId: id },
        });
        return true;
      } catch (error) {
        this.favoriteRestaurantIds = previous;
        throw error;
      }
    },
    async toggleFavorite(userId, restaurantId) {
      if (this.isFavorite(restaurantId)) {
        return this.removeFavorite(userId, restaurantId);
      }
      return this.addFavorite(userId, restaurantId);
    },
  },
});
