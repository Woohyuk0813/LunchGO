<script setup>
import { RouterLink } from 'vue-router';
import {
  BarChart3,
  ClipboardList,
  Store,
  MessageSquare,
  UserCircle,
  Users,
  DollarSign,
} from 'lucide-vue-next';

// 'activeMenu' 라는 이름의 prop을 정의합니다.
defineProps({
  activeMenu: {
    type: String,
    required: true,
  },
});
</script>

<template>
  <aside class="w-56 bg-white border-r border-[#e9ecef] flex flex-col">
    <div class="px-4 py-5 border-b border-[#e9ecef]">
      <RouterLink to="/admin/dashboard">
        <img
          src="/images/lunch-go-whitebg.png"
          alt="LunchGo"
          class="w-auto h-10"
          width="130"
          height="50"
        />
      </RouterLink>
    </div>

    <nav class="flex-1 p-3">
      <ul class="space-y-1.5">
        <li>
          <RouterLink
            to="/admin/dashboard"
            :class="[
              'block px-3 py-2.5 rounded-lg transition-colors text-sm',
              activeMenu === 'dashboard'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <BarChart3 class="nav-icon" />
              대시보드
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/reservations"
            :class="[
              'block px-3 py-2.5 rounded-lg transition-colors text-sm',
              activeMenu === 'reservations'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <ClipboardList class="nav-icon" />
              예약 등록 현황
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/franchises"
            :class="[
              'block px-3 py-2.5 rounded-lg transition-colors text-sm',
              activeMenu === 'franchises'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <Store class="nav-icon" />
              가맹점 관리
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/reviews"
            :class="[
              'block px-3 py-2.5 rounded-lg transition-colors text-sm',
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
            to="/admin/owners"
            :class="[
              'block px-3 py-2.5 rounded-lg transition-colors text-sm',
              activeMenu === 'owners'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <UserCircle class="nav-icon" />
              사업자 현황
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/members"
            :class="[
              'block px-3 py-2.5 rounded-lg transition-colors text-sm',
              activeMenu === 'members'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <Users class="nav-icon" />
              회원 관리
            </span>
          </RouterLink>
        </li>
        <li>
          <RouterLink
            to="/admin/finance"
            :class="[
              'block px-3 py-2.5 rounded-lg transition-colors text-sm',
              activeMenu === 'finance'
                ? 'text-white bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] font-semibold'
                : 'text-[#1e3a5f] hover:bg-[#f8f9fa]',
            ]"
          >
            <span class="nav-item-content">
              <DollarSign class="nav-icon" />
              재무 관리
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
