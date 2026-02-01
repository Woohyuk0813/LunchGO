<script setup>
import { RouterLink } from "vue-router";
import { Star } from "lucide-vue-next";
import Card from "@/components/ui/Card.vue";
import FavoriteStarButton from "@/components/ui/FavoriteStarButton.vue";
import {
  formatRouteDistance,
  formatRouteDurationMinutes,
} from "@/utils/formatters";

const props = defineProps({
  restaurants: {
    type: Array,
    default: () => [],
  },
  pageKey: {
    type: [String, Number],
    default: null,
  },
  showRouteButton: {
    type: Boolean,
    default: false,
  },
  onCheckRoute: {
    type: Function,
    default: () => {},
  },
  routeLoadingId: {
    type: [Number, String],
    default: null,
  },
  routeInfo: {
    type: Object,
    default: null,
  },
  favoriteRestaurantIds: {
    type: Array,
    default: () => [],
  },
  onToggleFavorite: {
    type: Function,
    default: () => {},
  },
});

const normalizeTag = (tag) => {
  if (!tag) return null;
  if (typeof tag === "string") {
    return { name: tag };
  }
  const name = tag.name ?? tag.content;
  if (!name) return null;
  return { name, count: tag.count };
};

const buildTagList = (tags) => (tags || []).map(normalizeTag).filter(Boolean);

const resolveDisplayTags = (restaurant) => {
  const reviewTags = buildTagList(restaurant.reviewTags || []).filter(
    (tag) => (tag.count ?? 0) > 0
  );
  const restaurantTags = buildTagList(
    restaurant.restaurantTags || restaurant.tags || restaurant.topTags || []
  );

  if (reviewTags.length >= 3) {
    return reviewTags.slice(0, 3);
  }
  if (restaurantTags.length) {
    return restaurantTags.slice(0, 3);
  }
  return reviewTags.slice(0, 3);
};

const hasDisplayTags = (restaurant) => resolveDisplayTags(restaurant).length > 0;

const formatTagLabel = (tag) => {
  if (!tag) return "";
  if (tag.count == null || tag.count === "") {
    return tag.name ?? "";
  }
  return `${tag.name} ${tag.count}`;
};

const formatRating = (rating) => {
  const value = Number(rating);
  return Number.isFinite(value) ? value.toFixed(1) : "-";
};

const getRestaurantId = (restaurant) =>
  restaurant?.id ?? restaurant?.restaurantId ?? restaurant?.restaurant_id ?? null;

const isRouteActive = (restaurant) => {
  const currentId = getRestaurantId(restaurant);
  const activeId = props.routeInfo?.restaurantId;
  if (currentId == null || activeId == null) return false;
  return String(currentId) === String(activeId);
};

const formatRouteDuration = formatRouteDurationMinutes;
</script>

<template>
  <div class="space-y-3">
    <RouterLink
      v-for="(restaurant, index) in restaurants"
      :key="`${props.pageKey ?? 'list'}-${restaurant.id ?? restaurant.restaurantId ?? 'row'}-${index}`"
      :to="`/restaurant/${restaurant.id ?? restaurant.restaurantId}`"
    >
      <Card
        class="relative overflow-hidden border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-lg transition-shadow cursor-pointer"
      >
        <FavoriteStarButton :restaurant-id="restaurant.id" />
        <div class="flex gap-3 p-2">
          <div
            class="relative w-24 h-24 flex-shrink-0 rounded-lg overflow-hidden"
          >
            <img
              :src="restaurant.image || '/placeholder.svg'"
              :alt="restaurant.name"
              class="w-full h-full object-cover"
            />
          </div>

          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <h4 class="font-semibold text-[#1e3a5f] text-sm">
                {{ restaurant.name }}
              </h4>
            </div>
            <p class="text-xs text-gray-700 mb-1 truncate">
              {{ restaurant.address }}
            </p>
            <div class="flex items-center gap-1 mb-1.5">
              <Star class="w-3.5 h-3.5 fill-[#ffc107] text-[#ffc107]" />
              <span class="text-sm font-medium text-[#1e3a5f]">
                {{ formatRating(restaurant.rating) }}
              </span>
              <span class="text-xs text-gray-700"
                >(리뷰수 : {{ restaurant.reviews ?? 0 }})</span
              >
            </div>
            <div
              v-if="hasDisplayTags(restaurant)"
              class="flex flex-wrap gap-1 mb-2"
            >
              <span
                v-for="(tag, index) in resolveDisplayTags(restaurant)"
                :key="index"
                class="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
              >
                {{ formatTagLabel(tag) }}
              </span>
            </div>
            <div class="flex items-center justify-between gap-2">
              <p class="text-sm font-semibold text-[#1e3a5f]">
                {{ restaurant.price || "가격 정보 없음" }}
              </p>
              <div v-if="showRouteButton" class="flex flex-col items-end gap-1">
                <span
                  v-if="isRouteActive(restaurant)"
                  class="text-xs font-semibold text-[#1e3a5f] whitespace-nowrap"
                >
                  {{ formatRouteDistance(routeInfo?.distanceMeters) }} · 예상
                  {{ formatRouteDuration(routeInfo?.durationSeconds) }}
                </span>
                <button
                  type="button"
                  class="text-[11px] px-2.5 py-1 rounded-full border transition-colors whitespace-nowrap"
                  :class="isRouteActive(restaurant)
                    ? 'bg-[#ff6b4a] border-[#ff6b4a] text-white hover:bg-[#ff7d61]'
                    : 'border-[#e9ecef] text-[#1e3a5f] bg-white hover:bg-[#f8f9fa]'"
                  :disabled="routeLoadingId === (restaurant.id ?? restaurant.restaurantId)"
                  @click.stop.prevent="onCheckRoute && onCheckRoute(restaurant)"
                >
                  {{
                    routeLoadingId === (restaurant.id ?? restaurant.restaurantId)
                      ? "경로 확인 중"
                      : "경로 및 거리 확인"
                  }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </Card>
    </RouterLink>
  </div>
</template>
