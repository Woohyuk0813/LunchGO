<script setup>
import { RouterLink, useRoute } from 'vue-router';
import { computed } from 'vue';

// 'activeMenu' 라는 이름의 prop을 정의합니다.
defineProps({
  activeMenu: {
    type: String,
    required: true,
  },
});

const route = useRoute();
const restaurantId = computed(() => String(route.query.restaurantId || ''));

const toWithRestaurantId = (path) => {
  return restaurantId.value
      ? { path, query: { restaurantId: restaurantId.value } }
      : { path };
};
</script>

<template>
  <aside class="w-64 bg-white border-r border-[#e9ecef] flex flex-col">
    <div class="px-6 py-5 border-b border-[#e9ecef]">
      <RouterLink :to="toWithRestaurantId('/business/dashboard')">
        <img
            src="/images/lunch-go-whitebg.png"
            alt="LunchGo"
            class="w-auto h-12"
            width="150"
            height="60"
        />
      </RouterLink>
    </div>

    <nav class="flex-1 p-4">
      <ul class="space-y-2">
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/dashboard')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'dashboard'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            오늘의 예약 현황
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/reservations')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'reservations'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            전체 예약 관리
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/notifications')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'notifications'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            알림 내역
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/staff/list')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'staff'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            임직원 현황
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>