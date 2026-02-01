<script setup>
defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  pageNumbers: {
    type: Array,
    default: () => [],
  },
  currentPage: {
    type: Number,
    required: true,
  },
  canGoPrevious: {
    type: Boolean,
    default: false,
  },
  canGoNext: {
    type: Boolean,
    default: false,
  },
  onGoPrevious: {
    type: Function,
    required: true,
  },
  onGoNext: {
    type: Function,
    required: true,
  },
  onGoToPage: {
    type: Function,
    required: true,
  },
});
</script>

<template>
  <div v-if="show" class="mt-6">
    <nav
      class="flex flex-wrap items-center justify-center gap-2 text-sm"
      aria-label="페이지네이션"
    >
      <button
        type="button"
        class="min-w-[56px] h-9 px-4 rounded-2xl border border-[#e9ecef] bg-white text-gray-700 font-medium transition-colors disabled:text-[#c7cdd3] disabled:border-[#f1f3f5] disabled:cursor-not-allowed"
        :disabled="!canGoPrevious"
        @click="onGoPrevious"
      >
        이전
      </button>

      <button
        v-for="page in pageNumbers"
        :key="page"
        type="button"
        @click="onGoToPage(page)"
        :aria-current="page === currentPage ? 'page' : undefined"
        :class="[
          'min-w-[36px] h-9 px-3 rounded-2xl border font-medium transition-colors',
          page === currentPage
            ? 'bg-gradient-to-r from-[#ff6b4a] via-[#ff805f] to-[#ff987d] text-white border-transparent shadow-button-hover'
            : 'bg-white text-gray-700 border-[#e9ecef] hover:text-[#ff6b4a] hover:border-[#ff6b4a]',
        ]"
      >
        {{ page }}
      </button>

      <button
        type="button"
        class="min-w-[56px] h-9 px-4 rounded-2xl border border-[#e9ecef] bg-white text-gray-700 font-medium transition-colors disabled:text-[#c7cdd3] disabled:border-[#f1f3f5] disabled:cursor-not-allowed"
        :disabled="!canGoNext"
        @click="onGoNext"
      >
        다음
      </button>
    </nav>
  </div>
</template>
