<script setup>
import { Search, Filter } from 'lucide-vue-next';
import Input from '@/components/ui/Input.vue';
import Button from '@/components/ui/Button.vue';

const props = defineProps({
  // 검색 관련
  searchQuery: {
    type: String,
    default: '',
  },
  searchPlaceholder: {
    type: String,
    default: '검색어를 입력하세요',
  },
  searchLabel: {
    type: String,
    default: '검색',
  },
  searchColSpan: {
    type: Number,
    default: 2,
  },

  // 필터 옵션들
  filters: {
    type: Array,
    default: () => [],
    // filters 구조:
    // [
    //   {
    //     model: 'selectedStatus',
    //     label: '상태',
    //     options: [{ value: 'all', label: '전체' }, ...]
    //   }
    // ]
  },

  // 날짜 필터
  showDateFilter: {
    type: Boolean,
    default: false,
  },
  startDate: {
    type: String,
    default: '',
  },
  endDate: {
    type: String,
    default: '',
  },
  startDateLabel: {
    type: String,
    default: '시작 날짜',
  },
  endDateLabel: {
    type: String,
    default: '종료 날짜',
  },
});

const emit = defineEmits([
  'update:searchQuery',
  'update:startDate',
  'update:endDate',
  'update:filterValue',
  'reset-filters',
]);

const handleSearchInput = (value) => {
  emit('update:searchQuery', value);
};

const handleFilterChange = (filterModel, value) => {
  emit('update:filterValue', { model: filterModel, value });
};

const handleStartDateChange = (event) => {
  emit('update:startDate', event.target.value);
};

const handleEndDateChange = (event) => {
  emit('update:endDate', event.target.value);
};

const handleReset = () => {
  emit('reset-filters');
};
</script>

<template>
  <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
    <!-- 첫 번째 행: 검색 및 필터 -->
    <div class="grid grid-cols-4 gap-4" :class="{ 'mb-4': showDateFilter }">
      <!-- 검색 -->
      <div :class="`col-span-${searchColSpan}`">
        <label class="block text-sm font-medium text-[#1e3a5f] mb-2">
          <Search class="w-4 h-4 inline mr-1" />
          {{ searchLabel }}
        </label>
        <Input
          :model-value="searchQuery"
          @update:model-value="handleSearchInput"
          type="text"
          :placeholder="searchPlaceholder"
          class="w-full"
        />
      </div>

      <!-- 동적 필터들 -->
      <div
        v-for="(filter, index) in filters"
        :key="index"
        :class="filter.colSpan ? `col-span-${filter.colSpan}` : ''"
      >
        <label class="block text-sm font-medium text-[#1e3a5f] mb-2">
          <Filter class="w-4 h-4 inline mr-1" />
          {{ filter.label }}
        </label>
        <select
          :value="filter.value"
          @change="handleFilterChange(filter.model, $event.target.value)"
          class="w-full h-9 px-3 py-1 text-sm border border-[#e9ecef] rounded-md focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
        >
          <option
            v-for="option in filter.options"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </div>
    </div>

    <!-- 두 번째 행: 날짜 범위 (옵셔널) -->
    <div v-if="showDateFilter" class="grid grid-cols-4 gap-4">
      <!-- 시작 날짜 -->
      <div>
        <label class="block text-sm font-medium text-[#1e3a5f] mb-2">
          {{ startDateLabel }}
        </label>
        <input
          :value="startDate"
          @input="handleStartDateChange"
          type="date"
          class="w-full h-9 px-3 py-1 text-sm border border-[#e9ecef] rounded-md focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
        />
      </div>

      <!-- 종료 날짜 -->
      <div>
        <label class="block text-sm font-medium text-[#1e3a5f] mb-2">
          {{ endDateLabel }}
        </label>
        <input
          :value="endDate"
          @input="handleEndDateChange"
          type="date"
          class="w-full h-9 px-3 py-1 text-sm border border-[#e9ecef] rounded-md focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
        />
      </div>

      <!-- 빈 공간 및 필터 초기화 버튼 -->
      <div class="col-span-2 flex items-end justify-end">
        <Button variant="outline" size="sm" @click="handleReset">
          필터 초기화
        </Button>
      </div>
    </div>

    <!-- 날짜 필터가 없는 경우 필터 초기화 버튼 -->
    <div v-if="!showDateFilter" class="mt-4 flex justify-end">
      <Button variant="outline" size="sm" @click="handleReset">
        필터 초기화
      </Button>
    </div>
  </div>
</template>
