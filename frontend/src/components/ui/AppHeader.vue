<script setup>
import { ref, computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
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
const isLoggedIn = computed(() =>
  Boolean(accountStore.accessToken || localStorage.getItem('accessToken'))
);
const userProfileImage = computed(
  () =>
    member.value?.image ||
    'https://api.dicebear.com/9.x/avataaars/svg?seed=Felix'
);
const props = defineProps({
  searchQuery: {
    type: String,
    default: '',
  },
});
const emit = defineEmits(['update:searchQuery']);

const isMenuOpen = ref(false);
const isLogoutConfirmOpen = ref(false);
const isLogoutAlertOpen = ref(false);
const isLogoutErrorOpen = ref(false);
const logoutButtonRef = ref(null);
const logoutAnchorRect = ref(null);
const searchText = computed({
  get: () => props.searchQuery,
  set: (value) => emit('update:searchQuery', value),
});

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
};

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
    isMenuOpen.value = false;
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
  <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
    <div
      class="max-w-[500px] mx-auto px-4 h-14 flex items-center justify-between"
    >
      <div class="flex items-center gap-3">
        <img
          src="/images/lunch-go-whitebg.png"
          alt="런치고"
          width="56"
          height="56"
          class="w-14 h-14 object-contain -ml-2"
        />
      </div>
      <div class="flex-1 px-4">
        <input
          type="text"
          placeholder="검색"
          v-model="searchText"
          class="w-full h-9 px-3 rounded-full border border-[#ced4da] bg-[#e9ecef] text-sm text-[#495057] focus:outline-none focus:ring-2 focus:ring-[#ff6b4a]"
        />
      </div>

      <div class="flex items-center gap-4">
        <template v-if="isLoggedIn">
          <div class="relative">
            <button
              @click="toggleMenu"
              class="flex items-center justify-center w-9 h-9 rounded-full overflow-hidden border border-gray-200 hover:ring-2 hover:ring-[#ff6b4a] transition-all focus:outline-none"
            >
              <img
                :src="userProfileImage"
                alt="프로필"
                class="w-full h-full object-cover"
              />
            </button>

            <Transition name="dropdown">
              <div
                v-if="isMenuOpen"
                class="absolute right-0 top-full mt-2 w-max bg-white rounded-lg shadow-xl border border-gray-100 py-2 z-50"
              >
                <RouterLink
                  to="/mypage"
                  class="block px-4 py-2 text-sm text-center text-gray-700 hover:bg-gray-50 hover:text-[#ff6b4a] transition-colors"
                  @click="isMenuOpen = false"
                >
                  마이페이지
                </RouterLink>
                <RouterLink
                  to="/chatbot"
                  class="block px-4 py-2 text-sm text-center text-gray-700 hover:bg-gray-50 hover:text-[#ff6b4a] transition-colors"
                  @click="isMenuOpen = false"
                >
                  고객센터
                </RouterLink>

                <button
                  @click="openLogoutConfirm"
                  ref="logoutButtonRef"
                  class="w-full text-center block px-4 py-2 text-sm text-red-500 hover:bg-red-50 transition-colors"
                >
                  로그아웃
                </button>
              </div>
            </Transition>
          </div>
        </template>

        <template v-else>
          <RouterLink
            to="/login"
            class="text-sm text-[#495057] font-medium hover:text-[#ff6b4a] transition-colors"
          >
            로그인
          </RouterLink>
          <RouterLink
            to="/signup"
            class="text-sm text-[#495057] font-medium hover:text-[#ff6b4a] transition-colors"
          >
            회원가입
          </RouterLink>
        </template>
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

<style scoped>
/* Vue Transition Classes 
  name="dropdown"로 설정했으므로 모두 dropdown- 입니다.
*/

/* 애니메이션 동작 시간 및 가속도 설정 (Transition & 클래스명) */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease-out;
}

/* 시작 상태 (Enter From) & 종료 상태 (Leave To) 
  투명하고 위쪽으로 올라가 있음 
*/
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 종료 상태 (Enter To) & 시작 상태 (Leave From)
  완전 불투명하고 제자리에 있음 
*/
.dropdown-enter-to,
.dropdown-leave-from {
  opacity: 1;
  transform: translateY(0);
}
</style>
