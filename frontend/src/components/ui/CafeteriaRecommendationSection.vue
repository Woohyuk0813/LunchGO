<script setup>
import Button from "@/components/ui/Button.vue";
import CafeteriaMenuUploadModal from "@/components/ui/CafeteriaMenuUploadModal.vue";
import RestaurantCardList from "@/components/ui/RestaurantCardList.vue";
import RestaurantCardSkeletonList from "@/components/ui/RestaurantCardSkeletonList.vue";

defineProps({
  recommendations: {
    type: Array,
    required: true,
  },
  recommendationButtons: {
    type: Array,
    default: () => [],
  },
  activeRecommendation: {
    type: String,
    default: null,
  },
  onSelectRecommendation: {
    type: Function,
    default: null,
  },
  showButtons: {
    type: Boolean,
    default: true,
  },
  onOpenSearch: {
    type: Function,
    required: true,
  },
  onClearRecommendations: {
    type: Function,
    required: true,
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
  stickyHeader: {
    type: Boolean,
    default: false,
  },
  hideHeader: {
    type: Boolean,
    default: false,
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  isModalOpen: {
    type: Boolean,
    required: true,
  },
  isProcessing: {
    type: Boolean,
    required: true,
  },
  initialImageUrl: {
    type: String,
    default: "",
  },
  ocrResult: {
    type: Object,
    default: null,
  },
  days: {
    type: Array,
    default: () => [],
  },
  errorMessage: {
    type: String,
    default: "",
  },
  onModalClose: {
    type: Function,
    required: true,
  },
  onFileChange: {
    type: Function,
    required: true,
  },
  onRunOcr: {
    type: Function,
    required: true,
  },
  onConfirm: {
    type: Function,
    required: true,
  },
});
</script>

<template>
  <div>
    <div
      v-if="!hideHeader && (recommendations.length || showButtons)"
      :class="stickyHeader
        ? 'sticky -top-px z-30 bg-[#f8f9fa] pt-2 pb-3 shadow-header-seam'
        : 'mb-3'"
      class="flex items-center justify-between"
    >
      <template v-if="recommendations.length">
        <h3 class="text-base font-semibold text-[#1e3a5f]">
          구내식당 대체 추천
        </h3>
        <Button
          @click="onClearRecommendations"
          variant="outline"
          size="sm"
          class="h-8 px-3 text-xs border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] hover:text-[#1e3a5f] rounded-lg flex items-center gap-1"
        >
          추천 해제
        </Button>
      </template>
      <template v-else-if="showButtons">
        <div class="flex items-center gap-2">
          <button
            v-for="option in recommendationButtons"
            :key="option.value"
            type="button"
            @click="onSelectRecommendation && onSelectRecommendation(option.value)"
            :class="`px-2.5 py-1 rounded-md text-[11px] font-medium whitespace-nowrap transition-colors ${
              activeRecommendation === option.value
                ? 'gradient-primary text-white border border-transparent'
                : 'bg-white text-[#495057] border border-[#dee2e6] hover:bg-[#f8f9fa]'
            }`"
          >
            <span class="inline-flex items-center gap-1">
              <span v-if="option.emoji">{{ option.emoji }}</span>
              <span>{{ option.label }}</span>
            </span>
          </button>
        </div>
      </template>
    </div>

    <RestaurantCardSkeletonList v-if="isLoading" :count="3" />

    <div v-else-if="recommendations.length" class="space-y-5">
      <div
        v-for="day in recommendations"
        :key="`${day.day}-${day.date}`"
        class="space-y-3"
      >
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-semibold text-[#1e3a5f]">
              {{ day.day }} · {{ day.date }}
            </p>
            <p class="text-xs text-[#495057]">
              기피 메뉴: {{ day.avoidMenu || "없음" }}
            </p>
          </div>
          <span
            class="text-xs font-semibold px-2 py-1 rounded-full bg-[#f8f9fa] text-[#495057]"
          >
            본인+팀 선호 반영
          </span>
        </div>
        <RestaurantCardList
          :restaurants="day.restaurants || []"
          :showRouteButton="showRouteButton"
          :onCheckRoute="onCheckRoute"
          :routeLoadingId="routeLoadingId"
          :routeInfo="routeInfo"
        />
      </div>
    </div>

    <CafeteriaMenuUploadModal
      v-if="isModalOpen"
      @close="onModalClose"
      @file-change="onFileChange"
      @run-ocr="onRunOcr"
      @confirm="onConfirm"
      :is-processing="isProcessing"
      :initial-image-url="initialImageUrl"
      :ocr-result="ocrResult"
      :days="days"
      :error-message="errorMessage"
    />
  </div>
</template>

<style scoped>
.shadow-header-seam {
  box-shadow: 0 1px 0 #f8f9fa;
}
</style>
