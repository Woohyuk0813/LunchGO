<script setup>
import { computed } from "vue";
import Button from "@/components/ui/Button.vue";

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  message: {
    type: String,
    default: "",
  },
  confirmText: {
    type: String,
    default: "확인",
  },
  cancelText: {
    type: String,
    default: "취소",
  },
  showCancel: {
    type: Boolean,
    default: true,
  },
  anchorRect: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(["confirm", "close"]);

const modalStyle = computed(() => {
  if (!props.anchorRect || typeof window === "undefined") return {};
  const rect = props.anchorRect;
  const modalWidth = 260;
  const modalHeight = 150;
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
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[110] flex items-center justify-center">
      <div class="absolute inset-0 bg-black/30" @click.stop="$emit('close')" />
      <div
        class="relative z-10 w-[260px] rounded-xl bg-white px-4 py-4 shadow-xl"
        :style="modalStyle"
        @click.stop
      >
        <div class="flex items-start justify-between mb-3">
          <h3 class="text-sm font-semibold text-[#1e3a5f]">확인</h3>
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
        <div class="mt-4 flex justify-end gap-2">
          <Button
            v-if="showCancel"
            variant="outline"
            class="h-8 px-3 text-xs border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa]"
            @click="$emit('close')"
          >
            {{ cancelText }}
          </Button>
          <Button
            class="h-8 px-3 text-xs rounded-lg bg-[#ff6b4a] text-white hover:bg-[#FF8A6D]"
            @click="$emit('confirm')"
          >
            {{ confirmText }}
          </Button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
