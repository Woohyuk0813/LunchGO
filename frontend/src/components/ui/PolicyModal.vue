<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  contentHtml: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['close']);
const close = () => emit('close');
</script>

<template>
  <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
  >
    <div class="fixed inset-0 z-[999]">
      <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="close"></div>

      <div
          class="absolute left-1/2 top-1/2 w-[calc(100%-32px)] max-w-[600px] -translate-x-1/2 -translate-y-1/2"
      >
        <div
            class="flex flex-col max-h-[70vh] rounded-2xl bg-white shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200"
        >
          <div
              class="px-5 py-4 border-b border-[#f1f3f5] flex items-center justify-between bg-white"
          >
            <h3 class="text-base font-bold text-[#1e3a5f]">{{ title }}</h3>
            <button
                type="button"
                class="text-[#adb5bd] hover:text-[#495057] transition-colors p-1"
                @click="close"
            >
              닫기
            </button>
          </div>

          <div
              class="p-5 overflow-y-auto text-sm text-[#495057] leading-relaxed whitespace-pre-line bg-[#f8f9fa]"
          >
            <div v-html="contentHtml"></div>
          </div>

          <div class="p-4 bg-white border-t border-[#f1f3f5]">
            <button
                type="button"
                class="w-full h-11 bg-[#1e3a5f] text-white font-semibold rounded-xl hover:bg-[#162c4b] transition-colors"
                @click="close"
            >
              확인
            </button>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped></style>
