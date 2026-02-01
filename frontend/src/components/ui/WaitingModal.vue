<script setup>
import { Loader2, AlertCircle } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';

defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  type: {
    type: String,
    default: 'waiting', // 'waiting' | 'error'
  },
  message: {
    type: String,
    default: '예약 요청이 많아 대기 중입니다.\n잠시만 기다려주세요...',
  },
  waitingCount: {
    type: Number,
    default: 0
  },
  estimatedTime: {
    type: String,
    default: '약 5초'
  }
});

defineEmits(['close']);
</script>

<template>
  <Teleport to="body">
    <div v-if="isOpen" class="fixed inset-0 z-[100] flex items-center justify-center">
      <!-- Backdrop -->
      <div class="absolute inset-0 bg-black/40 backdrop-blur-[2px]" />
      
      <!-- Content -->
      <div class="relative z-10 w-[280px] bg-white rounded-2xl p-6 shadow-2xl flex flex-col items-center text-center">
        <!-- Waiting State -->
        <template v-if="type === 'waiting'">
          <div class="w-12 h-12 bg-[#fff5f3] rounded-full flex items-center justify-center mb-4">
            <Loader2 class="w-7 h-7 text-[#ff6b4a] animate-spin" />
          </div>
          
          <h3 class="text-base font-bold text-[#1e3a5f] mb-2">접속 대기 중</h3>
          
          <p class="text-sm text-[#495057] leading-relaxed whitespace-pre-line mb-4">
            {{ message }}
          </p>

          <div class="flex flex-col gap-2 mb-4 w-full">
            <div class="flex justify-between items-center bg-[#f8f9fa] rounded-lg px-4 py-2 border border-[#e9ecef]">
              <span class="text-xs text-[#6c757d]">현재 접속 시도</span>
              <span class="text-sm font-bold text-[#ff6b4a]">{{ waitingCount }}명</span>
            </div>
            <div class="flex justify-between items-center bg-[#f8f9fa] rounded-lg px-4 py-2 border border-[#e9ecef]">
              <span class="text-xs text-[#6c757d]">예상 대기 시간</span>
              <span class="text-sm font-bold text-[#1e3a5f]">{{ estimatedTime }}</span>
            </div>
          </div>
          
          <div class="w-full bg-[#f8f9fa] rounded-lg p-3 text-left">
            <p class="text-[11px] text-[#868e96] leading-relaxed">
              • 페이지를 새로고침하거나 닫지 마세요.<br>
              • 순서가 되면 자동으로 넘어갑니다.
            </p>
          </div>
        </template>

        <!-- Error State -->
        <template v-else-if="type === 'error'">
          <div class="w-12 h-12 bg-[#fff5f5] rounded-full flex items-center justify-center mb-4">
            <AlertCircle class="w-7 h-7 text-[#fa5252]" />
          </div>
          
          <h3 class="text-base font-bold text-[#1e3a5f] mb-2">알림</h3>
          
          <p class="text-sm text-[#495057] leading-relaxed whitespace-pre-line mb-6">
            {{ message }}
          </p>
          
          <Button 
            class="w-full h-10 gradient-primary text-white font-semibold text-sm rounded-xl"
            @click="$emit('close')"
          >
            확인
          </Button>
        </template>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
