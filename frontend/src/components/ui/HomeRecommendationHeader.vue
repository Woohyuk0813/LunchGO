<script setup>
import Button from "@/components/ui/Button.vue";

defineProps({
  title: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    default: "",
  },
  description: {
    type: String,
    default: "",
  },
  isLoading: {
    type: Boolean,
    default: false,
  },
  loadingText: {
    type: String,
    default: "불러오는 중...",
  },
  onClear: {
    type: Function,
    default: null,
  },
});
</script>

<template>
  <div class="space-y-2">
    <div class="flex items-center justify-between">
      <h4 class="text-base font-semibold text-[#1e3a5f]">
        {{ title }}
        <span v-if="subtitle" class="text-xs text-gray-700 font-normal ml-2">
          {{ subtitle }}
        </span>
      </h4>
      <div class="flex items-center gap-2">
        <span v-if="isLoading" class="text-xs text-[#6c757d]">
          {{ loadingText }}
        </span>
        <Button
          v-if="onClear"
          variant="outline"
          size="sm"
          class="h-8 px-3 text-xs border-[#dee2e6] text-gray-700 bg-white hover:bg-[#f8f9fa] hover:text-[#1e3a5f] rounded-lg"
          @click="onClear"
        >
          추천 해제
        </Button>
      </div>
    </div>
    <p v-if="description" class="text-xs text-gray-700">
      {{ description }}
    </p>
  </div>
</template>
