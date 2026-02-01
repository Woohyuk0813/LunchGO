<script setup>
import { computed } from "vue";
import { useRouter } from "vue-router";
import Button from "@/components/ui/Button.vue";

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  redirectPath: {
    type: String,
    default: "",
  },
  message: {
    type: String,
    default: "즐겨찾기 추가를 위해서는 로그인이 필요합니다.",
  },
  anchorRect: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(["close"]);
const router = useRouter();

const modalStyle = computed(() => {
  if (!props.anchorRect || typeof window === "undefined") return {};
  const rect = props.anchorRect;
  const modalWidth = 280;
  const modalHeight = 180;
  const offset = 8;
  const viewportWidth = window.innerWidth;
  const viewportHeight = window.innerHeight;
  const left = Math.max(
    12,
    Math.min(rect.left, viewportWidth - modalWidth - 12)
  );
  const maxTop = viewportHeight - modalHeight - 12;
  const preferredTop = rect.bottom + offset;
  const top =
    preferredTop > maxTop ? Math.max(12, rect.top - modalHeight - offset) : preferredTop;
  return {
    position: "fixed",
    top: `${top}px`,
    left: `${left}px`,
  };
});

const handleLogin = () => {
  const query = props.redirectPath ? { redirect: props.redirectPath } : {};
  router.push({ path: "/login", query });
  emit("close");
};
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center">
      <div class="absolute inset-0 bg-black/30" @click.stop="$emit('close')" />
      <div
        class="relative z-10 w-[280px] rounded-xl bg-white px-4 py-5 shadow-xl"
        :style="modalStyle"
        @click.stop
      >
        <div class="flex items-start justify-between mb-3">
          <h3 class="text-sm font-semibold text-[#1e3a5f]">로그인 필요</h3>
          <button
            type="button"
            class="text-xs text-[#adb5bd] hover:text-[#6c757d]"
            @click="$emit('close')"
          >
            닫기
          </button>
        </div>
        <p class="text-xs text-[#495057] leading-relaxed">
          {{ message }}
        </p>
        <div class="mt-4 flex justify-end">
          <Button
            class="h-9 px-4 text-sm rounded-lg bg-[#ff6b4a] text-white hover:bg-[#FF8A6D]"
            @click="handleLogin"
          >
            로그인
          </Button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
