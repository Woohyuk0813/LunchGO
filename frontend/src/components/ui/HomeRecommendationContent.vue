<script setup>
import CafeteriaRecommendationSection from "@/components/ui/CafeteriaRecommendationSection.vue";
import TrendingRecommendationSection from "@/components/ui/TrendingRecommendationSection.vue";
import HomeRecommendationHeader from "@/components/ui/HomeRecommendationHeader.vue";
import RestaurantCardList from "@/components/ui/RestaurantCardList.vue";
import RestaurantCardSkeletonList from "@/components/ui/RestaurantCardSkeletonList.vue";

const emit = defineEmits(["goToSpeciality"]);

defineProps({
  isLoggedIn: {
    type: Boolean,
    default: false,
  },
  cafeteriaRecommendations: {
    type: Array,
    default: () => [],
  },
  recommendationButtons: {
    type: Array,
    default: () => [],
  },
  selectedRecommendation: {
    type: String,
    default: null,
  },
  recommendWeatherKey: {
    type: String,
    required: true,
  },
  recommendTasteKey: {
    type: String,
    required: true,
  },
  recommendBudgetKey: {
    type: String,
    required: true,
  },
  isTrendingSort: {
    type: Boolean,
    default: false,
  },
  isTrendingLoading: {
    type: Boolean,
    default: false,
  },
  isRecommendationLoading: {
    type: Boolean,
    default: false,
  },
  isCafeteriaLoading: {
    type: Boolean,
    default: false,
  },
  trendingError: {
    type: String,
    default: "",
  },
  trendingCards: {
    type: Array,
    default: () => [],
  },
  tagMappingNotice: {
    type: String,
    default: "",
  },
  tagMappingError: {
    type: String,
    default: "",
  },
  tasteRecommendationSummary: {
    type: String,
    default: "",
  },
  filterPerPersonBudgetDisplay: {
    type: String,
    default: "",
  },
  paginatedRestaurants: {
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
  onSelectRecommendation: {
    type: Function,
    required: true,
  },
  onOpenSearch: {
    type: Function,
    required: true,
  },
  onClearCafeteria: {
    type: Function,
    required: true,
  },
  onClearTrending: {
    type: Function,
    required: true,
  },
  onClearWeather: {
    type: Function,
    required: true,
  },
  onClearTaste: {
    type: Function,
    required: true,
  },
  onClearBudget: {
    type: Function,
    required: true,
  },
  isCafeteriaModalOpen: {
    type: Boolean,
    default: false,
  },
  stickyHeaders: {
    type: Boolean,
    default: false,
  },
  hideHeaders: {
    type: Boolean,
    default: false,
  },
  isCafeteriaOcrLoading: {
    type: Boolean,
    default: false,
  },
  cafeteriaOcrResult: {
    type: Object,
    default: null,
  },
  cafeteriaDaysDraft: {
    type: Array,
    default: () => [],
  },
  cafeteriaOcrError: {
    type: String,
    default: "",
  },
  cafeteriaImageUrl: {
    type: String,
    default: "",
  },
  onCafeteriaModalClose: {
    type: Function,
    required: true,
  },
  onCafeteriaFileChange: {
    type: Function,
    required: true,
  },
  onCafeteriaOcr: {
    type: Function,
    required: true,
  },
  onCafeteriaConfirm: {
    type: Function,
    required: true,
  },
});
</script>

<template>
  <div :class="stickyHeaders ? 'space-y-0' : 'space-y-4'">
    <CafeteriaRecommendationSection
      v-if="isLoggedIn"
      :recommendations="cafeteriaRecommendations"
      :recommendation-buttons="recommendationButtons"
      :active-recommendation="selectedRecommendation"
      :onSelectRecommendation="onSelectRecommendation"
      :show-buttons="false"
      :onOpenSearch="onOpenSearch"
      :onClearRecommendations="onClearCafeteria"
      :showRouteButton="showRouteButton"
      :onCheckRoute="onCheckRoute"
      :routeLoadingId="routeLoadingId"
      :routeInfo="routeInfo"
      :stickyHeader="stickyHeaders"
      :hideHeader="hideHeaders"
      :isLoading="isCafeteriaLoading"
      :isModalOpen="isCafeteriaModalOpen"
      :isProcessing="isCafeteriaOcrLoading"
      :ocrResult="cafeteriaOcrResult"
      :days="cafeteriaDaysDraft"
      :errorMessage="cafeteriaOcrError"
      :initialImageUrl="cafeteriaImageUrl"
      :onModalClose="onCafeteriaModalClose"
      :onFileChange="onCafeteriaFileChange"
      :onRunOcr="onCafeteriaOcr"
      :onConfirm="onCafeteriaConfirm"
    />
    <div
      v-if="!cafeteriaRecommendations.length && tagMappingNotice"
      class="rounded-2xl border border-[#e9ecef] bg-white px-4 py-3 text-sm text-gray-700"
    >
      {{ tagMappingNotice }}
    </div>

    <TrendingRecommendationSection
      :isActive="!cafeteriaRecommendations.length && isTrendingSort"
      :isLoading="isTrendingLoading"
      :error="trendingError"
      :cards="trendingCards"
      :onClear="onClearTrending"
      :showRouteButton="showRouteButton"
      :onCheckRoute="onCheckRoute"
      :routeLoadingId="routeLoadingId"
      :routeInfo="routeInfo"
      :stickyHeader="stickyHeaders"
      :hideHeader="hideHeaders"
    />

    <div
      v-if="!hideHeaders && !cafeteriaRecommendations.length && selectedRecommendation === recommendWeatherKey"
      :class="stickyHeaders
        ? 'sticky -top-px z-30 bg-[#f8f9fa] pt-2 pb-3 shadow-header-seam'
        : 'mb-3'"
    >
      <HomeRecommendationHeader
        title="날씨 추천"
        :onClear="onClearWeather"
      />
    </div>

    <div
      v-if="!hideHeaders && !cafeteriaRecommendations.length && selectedRecommendation === recommendTasteKey"
      :class="stickyHeaders
        ? 'sticky -top-px z-30 bg-[#f8f9fa] pt-2 pb-3 shadow-header-seam'
        : 'mb-3'"
    >
      <HomeRecommendationHeader
        title="취향 맞춤 추천"
        :subtitle="tasteRecommendationSummary"
        description="나와 팀원의 특이사항 태그를 기반으로 매칭 점수가 높은 식당을 골랐어요."
        :onClear="onClearTaste"
      />
    </div>

    <div
      v-if="!hideHeaders && !cafeteriaRecommendations.length && selectedRecommendation === recommendBudgetKey"
      :class="stickyHeaders
        ? 'sticky -top-px z-30 bg-[#f8f9fa] pt-2 pb-3 shadow-header-seam'
        : 'mb-3'"
    >
      <HomeRecommendationHeader
        title="예산 맞춤 추천"
        :subtitle="`1인당 ${filterPerPersonBudgetDisplay}`"
        :onClear="onClearBudget"
      />
    </div>

    <RestaurantCardSkeletonList
      v-if="!cafeteriaRecommendations.length && !isTrendingSort && isRecommendationLoading"
      :count="3"
    />

    <div
      v-else-if="!cafeteriaRecommendations.length && !isTrendingSort && !paginatedRestaurants.length"
      class="w-full px-4 py-10 text-center text-sm text-gray-700"
    >
      <button
        v-if="selectedRecommendation === recommendTasteKey && tagMappingError"
        type="button"
        class="cursor-pointer rounded-md border border-[#d9e2ef] bg-[#f4f7fb] px-8 py-4 text-sm font-semibold text-[#1e3a5f] shadow-sm transition hover:border-[#c2d1e6] hover:bg-[#edf3fa]"
        @click="emit('goToSpeciality')"
      >
        특이사항을 먼저 입력해주세요
      </button>
      <span v-else>해당 검색 결과가 없습니다.</span>
    </div>
    <RestaurantCardList
      v-else-if="!cafeteriaRecommendations.length && !isTrendingSort"
      :restaurants="paginatedRestaurants"
      :showRouteButton="showRouteButton"
      :onCheckRoute="onCheckRoute"
      :routeLoadingId="routeLoadingId"
      :routeInfo="routeInfo"
    />
  </div>
</template>

<style scoped>
.shadow-header-seam {
  box-shadow: 0 1px 0 #f8f9fa;
}
</style>
