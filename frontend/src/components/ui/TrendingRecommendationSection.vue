<script setup>
import HomeRecommendationHeader from "@/components/ui/HomeRecommendationHeader.vue";
import RestaurantCardList from "@/components/ui/RestaurantCardList.vue";
import RestaurantCardSkeletonList from "@/components/ui/RestaurantCardSkeletonList.vue";

defineProps({
  isActive: {
    type: Boolean,
    default: false,
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: "",
  },
  cards: {
    type: Array,
    default: () => [],
  },
  favoriteRestaurantIds: {
    type: Array,
    default: () => [],
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
  onToggleFavorite: {
    type: Function,
    default: () => {},
  },
  onClear: {
    type: Function,
    required: true,
  },
  stickyHeader: {
    type: Boolean,
    default: false,
  },
  hideHeader: {
    type: Boolean,
    default: false,
  },
});
</script>

<template>
  <section v-if="isActive" class="space-y-3">
    <div
      v-if="!hideHeader"
      :class="stickyHeader
        ? 'sticky -top-px z-30 bg-[#f8f9fa] pt-2 pb-3 shadow-header-seam'
        : 'mb-3'"
    >
      <HomeRecommendationHeader
        title="이달의 회식 맛집 추천"
        :isLoading="isLoading"
        :onClear="onClear"
      />
    </div>
    <p v-if="error" class="text-xs text-[#e03131]">
      {{ error }}
    </p>
    <p v-else class="text-xs text-[#6c757d]">
      최근 7일 조회/예약 기반 인기순 상위 {{ cards.length }}곳이에요.
    </p>
    <RestaurantCardSkeletonList v-if="isLoading" :count="3" />
    <RestaurantCardList
      v-if="!isLoading && cards.length"
      :restaurants="cards"
      :showRouteButton="showRouteButton"
      :onCheckRoute="onCheckRoute"
      :routeLoadingId="routeLoadingId"
      :routeInfo="routeInfo"
    />
  </section>
</template>

<style scoped>
.shadow-header-seam {
  box-shadow: 0 1px 0 #f8f9fa;
}
</style>
