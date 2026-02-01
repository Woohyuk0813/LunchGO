<script setup>
import { RouterLink, useRoute } from 'vue-router';
import { computed } from 'vue';
import {
  CalendarCheck,
  ClipboardList,
  BarChart3,
  Bell,
  Store,
  Users,
  MessageSquare,
  Megaphone,
  UserCircle,
} from 'lucide-vue-next';

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
            <span class="nav-item-content">
              <CalendarCheck class="nav-icon" />
              오늘의 예약 현황
            </span>
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
            <span class="nav-item-content">
              <ClipboardList class="nav-icon" />
              전체 예약 관리
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/ai-insights')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'ai-insights'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <BarChart3 class="nav-icon" />
              AI 인사이트/예측
            </span>
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
            <span class="nav-item-content">
              <Bell class="nav-icon" />
              알림 내역
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/restaurant-info/resolve')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'restaurant-info'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <Store class="nav-icon" />
              식당 정보
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/staff')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'staff'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <Users class="nav-icon" />
              임직원 현황
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/reviews')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'reviews'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <MessageSquare class="nav-icon" />
              리뷰 관리
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/promotion')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'business-promotion'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <Megaphone class="nav-icon" />
              프로모션 관리
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
              :to="toWithRestaurantId('/business/mypage')"
              :class="[
              'block px-4 py-3 rounded-lg transition-colors',
              activeMenu === 'business-mypage'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <UserCircle class="nav-icon" />
              마이페이지
            </span>
          </RouterLink>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<style scoped>
.nav-item-content {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.nav-icon {
  width: 18px;
  height: 18px;
}
</style>
