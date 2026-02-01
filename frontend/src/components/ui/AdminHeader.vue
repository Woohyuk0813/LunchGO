<script setup>
import { computed, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { Bell, LogOut } from 'lucide-vue-next';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';
import ConfirmModal from '@/components/ui/ConfirmModal.vue';

const router = useRouter();
const accountStore = useAccountStore();

const getStoredMember = () => {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('member');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());
const userName = computed(()=> {return "관리자";});
const isLogoutConfirmOpen = ref(false);
const isLogoutAlertOpen = ref(false);
const isLogoutErrorOpen = ref(false);
const logoutButtonRef = ref(null);
const logoutAnchorRect = ref(null);

const openLogoutConfirm = () => {
  logoutAnchorRect.value =
    logoutButtonRef.value?.getBoundingClientRect() ?? null;
  isLogoutConfirmOpen.value = true;
};

const handleLogoutConfirm = async () => {
  isLogoutConfirmOpen.value = false;
  let logoutSuccess = false;
  try {
    await httpRequest.post('/api/logout', {});
    logoutSuccess = true;
  } catch (error) {
    console.warn('로그아웃 요청 실패:', error);
  } finally {
    accountStore.clearAccount();
    if (logoutSuccess) {
      isLogoutAlertOpen.value = true;
    } else {
      isLogoutErrorOpen.value = true;
    }
  }
};

const handleLogoutAlertConfirm = () => {
  isLogoutAlertOpen.value = false;
  router.push('/');
};

const handleLogoutErrorConfirm = () => {
  isLogoutErrorOpen.value = false;
};
</script>

<template>
  <header
    class="bg-white border-b border-[#e9ecef] px-8 py-5 flex items-center justify-between"
  >
    <RouterLink to="/admin/dashboard">
      <h1
        class="text-2xl font-bold text-[#1E3A5F] hover:text-[#FF6B4A] transition-colors cursor-pointer"
      >
        LunchGo 관리자
      </h1>
    </RouterLink>
    <div class="flex items-center gap-4">
      <button class="text-[#1e3a5f] hover:text-[#FF6B4A] transition-colors">
        <Bell class="w-6 h-6" />
      </button>
      <div class="flex items-center gap-4">
        <span class="text-sm text-gray-600">
          <span class="text-[#1e3a5f] font-bold">{{ userName }}</span>
          <span>님, 안녕하세요</span>
        </span>

        <button
          @click="openLogoutConfirm"
          ref="logoutButtonRef"
          class="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
        >
          <span class="text-sm">로그아웃</span>
          <LogOut class="w-4 h-4" />
        </button>
      </div>
    </div>
  </header>

  <ConfirmModal
    :is-open="isLogoutConfirmOpen"
    message="정말 로그아웃 하시겠습니까?"
    confirm-text="로그아웃"
    cancel-text="취소"
    :anchor-rect="logoutAnchorRect"
    @confirm="handleLogoutConfirm"
    @close="isLogoutConfirmOpen = false"
  />

  <ConfirmModal
    :is-open="isLogoutAlertOpen"
    message="로그아웃 되었습니다."
    :show-cancel="false"
    confirm-text="확인"
    :anchor-rect="logoutAnchorRect"
    @confirm="handleLogoutAlertConfirm"
    @close="handleLogoutAlertConfirm"
  />

  <ConfirmModal
    :is-open="isLogoutErrorOpen"
    message="로그아웃에 실패했습니다. 잠시 후 다시 시도해주세요."
    :show-cancel="false"
    confirm-text="확인"
    :anchor-rect="logoutAnchorRect"
    @confirm="handleLogoutErrorConfirm"
    @close="handleLogoutErrorConfirm"
  />
</template>
