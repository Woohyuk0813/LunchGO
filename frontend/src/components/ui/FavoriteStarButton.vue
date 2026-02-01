<script setup>
import { computed, ref } from "vue";
import { useRoute } from "vue-router";
import { Star } from "lucide-vue-next";
import LoginRequiredModal from "@/components/ui/LoginRequiredModal.vue";
import ConfirmModal from "@/components/ui/ConfirmModal.vue";
import { useFavorites } from "@/composables/useFavorites";

const props = defineProps({
  restaurantId: {
    type: [Number, String],
    required: true,
  },
  buttonClass: {
    type: String,
    default:
      "absolute top-3 right-3 z-10 p-2 rounded-full bg-white/95 border border-[#e9ecef] shadow-card text-[#adb5bd] hover:text-[#ff6b4a] hover:bg-[#f8f9fa] transition-colors",
  },
  initialFavorite: {
    type: Boolean,
    default: undefined,
  },
});

const route = useRoute();
const { addFavorite, removeFavorite, hasLoaded, favoriteRestaurantIds, userId } =
  useFavorites();
const isLoginModalOpen = ref(false);
const modalAnchor = ref(null);
const buttonRef = ref(null);
const isConfirmOpen = ref(false);
const confirmAnchor = ref(null);
const pendingAction = ref(null);
const restaurantIdNumber = computed(() => {
  const parsed = Number(props.restaurantId);
  return Number.isNaN(parsed) ? null : parsed;
});

const isActive = computed(() => {
  if (!restaurantIdNumber.value) return false;
  if (hasLoaded.value) {
    return favoriteRestaurantIds.value.includes(restaurantIdNumber.value);
  }
  if (props.initialFavorite !== undefined) {
    return Boolean(props.initialFavorite);
  }
  return favoriteRestaurantIds.value.includes(restaurantIdNumber.value);
});

const ariaPressed = computed(() => isActive.value);

const toggleFavorite = async () => {
  if (!userId.value) {
    modalAnchor.value = buttonRef.value?.getBoundingClientRect() ?? null;
    isLoginModalOpen.value = true;
    return;
  }
  if (!restaurantIdNumber.value) {
    alert("즐겨찾기 처리에 실패했습니다.");
    return;
  }

  if (isActive.value) {
    pendingAction.value = async () => {
      try {
        await removeFavorite(userId.value, restaurantIdNumber.value);
      } catch (error) {
        alert("즐겨찾기 해제에 실패했습니다.");
      }
    };
    confirmAnchor.value = buttonRef.value?.getBoundingClientRect() ?? null;
    isConfirmOpen.value = true;
    return;
  }

  try {
    await addFavorite(userId.value, restaurantIdNumber.value);
  } catch (error) {
    alert("즐겨찾기 추가에 실패했습니다.");
  }
};
</script>

<template>
  <button
    type="button"
    ref="buttonRef"
    :class="[buttonClass, isActive ? 'border-transparent' : '']"
    :aria-pressed="ariaPressed"
    @click.stop.prevent="toggleFavorite"
  >
    <Star
      class="w-4 h-4"
      :class="
        isActive ? 'fill-current text-[#ff6b4a]' : 'text-[#adb5bd] fill-white'
      "
    />
    <span class="sr-only">
      {{ isActive ? "즐겨찾기 해제" : "즐겨찾기에 추가" }}
    </span>
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
