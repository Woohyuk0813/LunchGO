<script setup>
import { computed } from 'vue';

const props = defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(['change-page']);

const visiblePageNumbers = computed(() => {
  const maxVisiblePages = 5;
  let startPage = Math.max(
    1,
    props.currentPage - Math.floor(maxVisiblePages / 2)
  );
  let endPage = startPage + maxVisiblePages - 1;

  if (endPage > props.totalPages) {
    endPage = props.totalPages;
    startPage = Math.max(1, endPage - maxVisiblePages + 1);
  }

  const pages = [];
  for (let i = startPage; i <= endPage; i++) {
    pages.push(i);
  }
  return pages;
});

const changePage = (page) => {
  if (page < 1 || page > props.totalPages) return;
  emit('change-page', page);
};
</script>

<template>
  <div class="flex items-center gap-2">
    <button
      @click="changePage(currentPage - 1)"
      :disabled="currentPage === 1"
      class="px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors disabled:opacity-50"
    >
      이전
    </button>
    <button
      v-for="page in visiblePageNumbers"
      :key="page"
      @click="changePage(page)"
      :class="[
        'px-4 py-2 border border-[#dee2e6] rounded-lg transition-colors',
        currentPage === page
          ? 'gradient-primary text-white border-transparent'
          : 'text-[#6c757d] hover:bg-[#f8f9fa]',
      ]"
    >
      {{ page }}
    </button>
    <button
      @click="changePage(currentPage + 1)"
      :disabled="currentPage === totalPages"
      class="px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors disabled:opacity-50"
    >
      다음
    </button>
  </div>
</template>
