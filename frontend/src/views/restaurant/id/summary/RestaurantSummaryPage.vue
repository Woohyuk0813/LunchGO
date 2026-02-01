<script setup>
import { computed } from 'vue';
import { RouterLink, useRoute } from 'vue-router'; // Import useRoute
import { ArrowLeft, MapPin, Calendar, Clock, Users, ShoppingBag } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';

const route = useRoute();
const restaurantId = route.params.id || '1'; // Default ID

const reservation = {
  restaurant: {
    name: '식당명',
    address: '서울시 강남구 테헤란로 123',
    phone: '02-1234-5678',
  },
  booking: {
    date: '12월 15일 (금)',
    time: '18:00',
    partySize: 4,
    requestNote: '유아용 의자 부탁드려요', //더미 (나중에 백엔드로 교체)
  },
  menu: [
    { name: 'A코스', quantity: 2, price: 35000 },
    { name: 'B코스', quantity: 2, price: 45000 },
  ],
};

const subtotal = computed(() =>
  reservation.menu.reduce((sum, item) => sum + item.price * item.quantity, 0),
);
const total = computed(() => subtotal.value); // 총 결제 = 주문금액
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink :to="`/restaurant/${restaurantId}/menu`" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">예약 확인</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-24">
      <!-- Restaurant Info -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <h2 class="text-lg font-semibold text-[#1e3a5f] mb-4">식당 정보</h2>
        <div class="space-y-3">
          <div>
            <p class="font-semibold text-[#1e3a5f] mb-1">{{ reservation.restaurant.name }}</p>
          </div>
          <div class="flex items-start gap-2 text-sm">
            <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{ reservation.restaurant.address }}</span>
          </div>
        </div>
      </div>

      <!-- Booking Details -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef] mt-2">
        <h2 class="text-lg font-semibold text-[#1e3a5f] mb-4">예약 정보</h2>
        <div class="space-y-3">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-[#fff5f3] flex items-center justify-center flex-shrink-0">
              <Calendar class="w-5 h-5 text-[#ff6b4a]" />
            </div>
            <div>
              <p class="text-xs text-[#6c757d] mb-0.5">예약 날짜</p>
              <p class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.date }}</p>
            </div>
          </div>

          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-[#fff5f3] flex items-center justify-center flex-shrink-0">
              <Clock class="w-5 h-5 text-[#ff6b4a]" />
            </div>
            <div>
              <p class="text-xs text-[#6c757d] mb-0.5">예약 시간</p>
              <p class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.time }}</p>
            </div>
          </div>

          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-lg bg-[#fff5f3] flex items-center justify-center flex-shrink-0">
              <Users class="w-5 h-5 text-[#ff6b4a]" />
            </div>
            <div>
              <p class="text-xs text-[#6c757d] mb-0.5">예약 인원</p>
              <p class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.partySize }}명</p>
            </div>
          </div>
        </div>
        <!-- Request Note (Read-only) : 예약 정보 밑에 따로 -->
        <div class="bg-white px-4 py-5">
          <h2 class="text-lg font-semibold text-[#1e3a5f] mb-4">요청사항</h2>

          <div class="rounded-2xl border border-[#e9ecef] bg-[#f8f9fa] px-4 py-4">
            <p class="text-base font-semibold text-[#6c757d] whitespace-pre-line">
              {{ reservation.booking.requestNote?.trim() ? reservation.booking.requestNote : '-' }}
            </p>
          </div>

          <p class="mt-3 text-sm text-[#6c757d] leading-relaxed">
            요청사항은 매장 참고용이며, 예약 변경은 매장으로 직접 문의해 주세요.
          </p>
        </div>
      </div>

      <!-- Menu Details -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef] mt-2">
        <div class="flex items-center gap-2 mb-4">
          <ShoppingBag class="w-5 h-5 text-[#ff6b4a]" />
          <h2 class="text-lg font-semibold text-[#1e3a5f]">주문 메뉴</h2>
        </div>
        <div class="space-y-3">
          <div v-for="(item, idx) in reservation.menu" :key="idx" class="flex items-center justify-between">
            <div class="flex-1">
              <p class="font-medium text-[#1e3a5f]">{{ item.name }}</p>
              <p class="text-sm text-[#6c757d]">{{ item.quantity }}개</p>
            </div>
            <p class="font-semibold text-[#1e3a5f]">{{ (item.price * item.quantity).toLocaleString() }}원</p>
          </div>
        </div>
      </div>

      <!-- Price Breakdown -->
      <div class="bg-white px-4 py-5 mt-2">
        <h2 class="text-lg font-semibold text-[#1e3a5f] mb-4">결제 금액</h2>
        <div class="space-y-2.5">
          <div class="flex items-center justify-between text-sm">
            <span class="text-[#6c757d]">주문 금액</span>
            <span class="font-medium text-[#495057]">{{ subtotal.toLocaleString() }}원</span>
          </div>
          <div class="h-px bg-[#e9ecef] my-3" />
          <div class="flex items-center justify-between">
            <span class="text-base font-semibold text-[#1e3a5f]">총 결제 금액</span>
            <span class="text-xl font-bold text-[#ff6b4a]">{{ total.toLocaleString() }}원</span>
          </div>
        </div>
      </div>

      <!-- Cancellation Policy -->
      <div class="mx-4 mt-4">
        <Card class="p-4 border-[#e9ecef] rounded-xl bg-[#f8f9fa]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">취소 및 환불 정책</h3>
          <ul class="space-y-1 text-xs text-[#6c757d] leading-relaxed">
            <li>• 예약일 1일 전까지 무료 취소 가능</li>
            <li>• 당일 취소 시 총 금액의 50% 위약금 발생</li>
            <li>• 노쇼 시 전액 위약금 발생 및 향후 예약 제한</li>
          </ul>
        </Card>
      </div>
    </main>

    <!-- Fixed Bottom Button -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="max-w-[500px] mx-auto px-4 py-3">
        <RouterLink :to="`/restaurant/${restaurantId}/payment`">
          <Button class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed">
            결제하기
          </Button>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
