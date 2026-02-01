<script setup>
import { ref, computed } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { Calendar, Home, User } from "lucide-vue-next";
import { useAccountStore } from "@/stores/account";
import LoginRequiredModal from "@/components/ui/LoginRequiredModal.vue";

const router = useRouter();
const accountStore = useAccountStore();
const isLoggedIn = computed(() =>
  Boolean(accountStore.accessToken || localStorage.getItem("accessToken"))
);
const isLoginModalOpen = ref(false);
const modalAnchor = ref(null);
const reservationsButtonRef = ref(null);
const myPageButtonRef = ref(null);
const modalMessage = ref("");
const redirectPath = ref("");

const handleMyReservationsClick = () => {
  if (isLoggedIn.value) {
    router.push("/my-reservations");
    return;
  }
  modalMessage.value = "나의 예약을 보기 위해 로그인이 필요합니다";
  redirectPath.value = "/my-reservations";
  modalAnchor.value = reservationsButtonRef.value?.getBoundingClientRect() ?? null;
  isLoginModalOpen.value = true;
};

const handleMyPageClick = () => {
  if (isLoggedIn.value) {
    router.push("/mypage");
    return;
  }
  modalMessage.value = "마이 페이지는 로그인이 필요합니다";
  redirectPath.value = "/mypage";
  modalAnchor.value = myPageButtonRef.value?.getBoundingClientRect() ?? null;
  isLoginModalOpen.value = true;
};
</script>

<template>
  <nav class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
    <div class="max-w-[500px] mx-auto flex items-center justify-around h-16 px-4">
      <button
        ref="reservationsButtonRef"
        type="button"
        class="flex items-center justify-center -mt-4 text-[#ff6b4a] hover:text-[#FF8A6D] transition-colors min-w-[60px]"
        @click="handleMyReservationsClick"
        aria-label="예약 내역"
      >
        <Calendar class="w-6 h-6" />
      </button>

      <button
        type="button"
        class="flex items-center justify-center -mt-4"
        @click="$emit('home')"
        aria-label="홈으로 이동"
      >
        <div
          class="w-12 h-12 rounded-full gradient-primary flex items-center justify-center shadow-button-hover border-4 border-white"
        >
          <Home class="w-5 h-5 text-white" />
          <span class="sr-only">홈으로 이동</span>
        </div>
      </button>

      <button
        ref="myPageButtonRef"
        type="button"
        class="flex items-center justify-center -mt-4 text-[#ff6b4a] hover:text-[#FF8A6D] transition-colors min-w-[60px]"
        @click="handleMyPageClick"
        aria-label="마이페이지"
      >
        <User class="w-6 h-6" />
      </button>
    </div>
    <LoginRequiredModal
      :is-open="isLoginModalOpen"
      :redirect-path="redirectPath"
      :message="modalMessage"
      :anchor-rect="modalAnchor"
      @close="
        isLoginModalOpen = false;
        modalAnchor = null;
        modalMessage = '';
        redirectPath = '';
      "
    />
  </nav>
</template>

<style scoped></style>
