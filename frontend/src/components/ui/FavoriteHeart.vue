<script setup lang="ts">
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { Heart } from "lucide-vue-next";
import LoginRequiredModal from "@/components/ui/LoginRequiredModal.vue";
import ConfirmModal from "@/components/ui/ConfirmModal.vue";
import { useFavorites } from "@/composables/useFavorites";

const props = defineProps<{
  restaurantId: number;
  userId?: number | null;
  initialFavorite?: boolean; // 초기 즐겨찾기 상태
}>();

// 부모에게 상태 변경을 알리기 위한 이벤트 정의
const emit = defineEmits<{
  (e: 'update:favorite', status: boolean): void; // 상태가 바뀔 때
  (e: 'remove'): void; // 해제 시 목록에서 제거
}>();

const route = useRoute();
const {
  isFavorite,
  addFavorite,
  removeFavorite,
  hasLoaded,
  favoriteRestaurantIds,
  userId: storeUserId,
} = useFavorites();
const isLoginModalOpen = ref(false);
const modalAnchor = ref(null);
const buttonRef = ref(null);
const isConfirmOpen = ref(false);
const confirmAnchor = ref(null);
const pendingAction = ref(null);

const resolvedUserId = computed(() => props.userId ?? storeUserId.value);

const isActive = computed(() => {
  if (hasLoaded.value) {
    return favoriteRestaurantIds.value.includes(props.restaurantId);
  }
  if (props.initialFavorite !== undefined) {
    return Boolean(props.initialFavorite);
  }
  return isFavorite(props.restaurantId);
});

const toggleFavorite = async () => {
  const userId = resolvedUserId.value;
  if (!userId) {
    modalAnchor.value = buttonRef.value?.getBoundingClientRect() ?? null;
    isLoginModalOpen.value = true;
    return;
  }

  if (isActive.value) {
    pendingAction.value = async () => {
      try {
        await removeFavorite(userId, props.restaurantId);
        emit("update:favorite", false);
        emit("remove");
      } catch (error: any) {
        const status = error?.response?.status;
        switch (status) {
          case 400:
            alert("[400 Bad Request] 잘못된 요청입니다 입력값을 확인해주세요.");
            break;
          case 404:
            alert("[404 Not Found] 이미 삭제되었거나 즐겨찾기에 없는 맛집입니다.");
            break;
          default:
            alert(`오류가 발생했습니다. (Code: ${status || "unknown"})`);
        }
      }
    };
    confirmAnchor.value = buttonRef.value?.getBoundingClientRect() ?? null;
    isConfirmOpen.value = true;
    return;
  }

  try {
    await addFavorite(userId, props.restaurantId);
    emit("update:favorite", true);
  } catch (error: any) {
    const status = error?.response?.status;
    switch (status) {
      case 400:
        alert("[400 Bad Request] 잘못된 요청입니다 입력값을 확인해주세요.");
        break;
      case 409:
        alert("[409 Conflict] 이미 즐겨찾기에 추가되어 있습니다.");
        isActive.value = true;
        break;
      default:
        alert(`오류가 발생했습니다. (Code: ${status || "unknown"})`);
    }
  }
};
</script>

<template>
  <button
    ref="buttonRef"
    @click.prevent="toggleFavorite"
    class="w-6 h-6 flex items-center justify-center rounded-full transition-colors focus:outline-none"
    :class="isActive
      ? 'bg-white hover:bg-gray-50'
      : 'bg-white border border-[#e9ecef] shadow-sm hover:bg-[#f8f9fa]'"
    :title="isActive ? '즐겨찾기 해제' : '즐겨찾기 등록'"
  >
    <Heart 
      class="w-4 h-4 transition-all duration-200" 
      :class="isActive 
        ? 'fill-[#ff6b4a] text-[#ff6b4a]' 
        : 'fill-none text-[#adb5bd]'"
    />
  </button>
  <LoginRequiredModal
    :is-open="isLoginModalOpen"
    :redirect-path="route.fullPath"
    :anchor-rect="modalAnchor"
    @close="
      isLoginModalOpen = false;
      modalAnchor = null;
    "
  />
  <ConfirmModal
    :is-open="isConfirmOpen"
    message="즐겨찾기를 해제하시겠습니까?"
    :anchor-rect="confirmAnchor"
    @close="
      isConfirmOpen = false;
      confirmAnchor = null;
      pendingAction = null;
    "
    @confirm="
      isConfirmOpen = false;
      pendingAction && pendingAction();
      confirmAnchor = null;
      pendingAction = null;
    "
  />
</template>
