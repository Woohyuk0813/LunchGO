<script setup>
import Button from "@/components/ui/Button.vue";
import { SlidersHorizontal, Search } from "lucide-vue-next";

defineProps({
  onOpenFilter: {
    type: Function,
    required: true,
  },
  onOpenSearch: {
    type: Function,
    required: true,
  },
});
</script>

<template>
  <div class="flex items-center gap-2 mb-2">
    <button
      @click="onOpenFilter"
      class="flex items-center gap-1.5 text-sm text-gray-700 hover:text-[#ff6b4a] transition-colors"
    >
      <SlidersHorizontal class="w-4 h-4" />
      <span>필터</span>
    </button>
    <Button
      variant="outline"
      size="sm"
      class="ml-auto h-8 px-3 text-xs border-[#dee2e6] text-gray-700 bg-white hover:bg-[#f8f9fa] hover:text-[#1e3a5f] rounded-lg flex items-center gap-1"
      @click="onOpenSearch"
    >
      <Search class="w-3.5 h-3.5" />
      검색
    </Button>
  </div>
</template>
