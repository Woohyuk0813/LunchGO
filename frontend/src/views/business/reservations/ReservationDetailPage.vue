<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import httpRequest from '@/router/httpRequest';

const router = useRouter();
const route = useRoute();

const props = defineProps({
  id: { type: [String, Number], required: true },
});

const reservationId = computed(() => Number(props.id));

//확정 버튼 삭제
// // ---- 확정 (pending만) ----
// const confirmReservation = () => {
//   const target = reservation.value;
//   if (!target) return;
//   if (target.status !== 'pending') return;
//   target.status = 'confirmed';
//   target.confirmedAt = new Date().toISOString();
// };

// ---- 취소 모달 상태 ----
const cancelModalOpen = ref(false);
const cancelReason = ref('');
const cancelError = ref('');
const cancelling = ref(false);

const MAX_CANCEL_REASON = 50;

// confirmed/pending만 취소 가능
const openCancelModal = () => {
  const target = reservation.value;
  if (!target) return;
  if (target.status === 'cancelled') return;
  cancelReason.value = '';
  cancelError.value = '';
  cancelModalOpen.value = true;
};

const closeCancelModal = () => {
  cancelModalOpen.value = false;
  cancelReason.value = '';
  cancelError.value = '';
};

const submitCancel = async () => {
  const target = reservation.value;
  if (!target) return;
  if (target.status === 'cancelled') return;

  const reason = cancelReason.value.trim();
  if (!reason) {
    cancelError.value = '취소 사유를 입력해주세요.';
    return;
  }

  if (reason.length > MAX_CANCEL_REASON) {
    cancelError.value = `취소 사유는 ${MAX_CANCEL_REASON}자 이내로 입력해주세요.`;
    return;
  }

  try {
    cancelling.value = true;
    cancelError.value = '';
    await httpRequest.post(`/api/business/reservations/${reservationId.value}/cancel`, {
      reason,
      detail: '',
    });
    target.status = 'cancelled';
    target.cancelReason = reason;
    target.cancelledAt = new Date().toISOString();
    closeCancelModal();
    window.alert('취소 되었습니다.');
  } catch (error) {
    cancelError.value = error?.message || '예약 취소에 실패했습니다.';
  } finally {
    cancelling.value = false;
  }
};

// // 버튼 노출 조건
// const canConfirm = computed(() => reservation.value?.status === 'pending');
const canCancel = computed(() => {
  const s = reservation.value?.status;
  return s === 'pending' || s === 'confirmed';
});

const reservation = ref(null);
const restaurantId = computed(() => Number(route.query.restaurantId || 0));

const loadReservation = async () => {
  try {
    const response = await httpRequest.get(
      `/api/business/reservations/${reservationId.value}`,
      restaurantId.value ? { restaurantId: restaurantId.value } : {}
    );
    const data = response.data ?? response;
    reservation.value = data;
  } catch (error) {
    console.error('예약 상세 조회 실패:', error);
  }
};

onMounted(() => {
  loadReservation();
});
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="reservations" />
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-5xl mx-auto space-y-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <button @click="router.back()" class="px-4 py-2 border rounded-lg">뒤로</button>
              <h2 class="text-3xl font-bold text-[#1e3a5f]">예약 상세</h2>
            </div>
            <!-- 액션 버튼: pending=취소 / confirmed=취소 / cancelled=없음 -->
            <div v-if="reservation" class="flex items-center gap-2">
            <!-- <button v-if="canConfirm" @click="confirmReservation" class="bg-[#28a745] text-white px-4 py-2 rounded-lg text-sm hover:opacity-90 transition-opacity">확정</button> -->
            <button v-if="canCancel" @click="openCancelModal" class="bg-[#dc3545] text-white px-4 py-2 rounded-lg text-sm hover:opacity-90 transition-opacity">취소</button></div>
          </div>
            <div v-if="!reservation" class="bg-white rounded-xl border p-10 text-center text-[#6c757d]">해당 예약을 찾을 수 없습니다.
            </div>

          <div v-else class="bg-white rounded-xl border p-6">
            <p class="text-sm text-[#6c757d] mb-2">예약자</p>
            <p class="text-2xl font-bold text-[#1e3a5f]">{{ reservation.name }}</p>
            <p class="text-sm text-[#6c757d]">{{ reservation.phone }}</p>

            <div class="mt-6 grid grid-cols-2 gap-6">
              <div>
                <p class="text-sm text-[#6c757d]">예약시간</p>
                <p class="font-semibold">{{ reservation.date }} {{ reservation.time }}</p>
              </div>
              <div>
                <p class="text-sm text-[#6c757d]">결제금액</p>
                <p class="font-semibold">{{ reservation.amount.toLocaleString() }}원</p>
              </div>
            </div>
            <!-- 상태 -->
            <div class="mt-6">
            <p class="text-sm text-[#6c757d] mb-2">예약 상태</p>
            <span
                class="inline-flex px-3 py-1 rounded-full text-xs font-semibold"
                :class="
                reservation.status === 'confirmed'
                    ? 'bg-[#d4edda] text-[#155724]'
                    : reservation.status === 'pending'
                    ? 'bg-[#fff3cd] text-[#856404]'
                    : reservation.status === 'cancelled'
                    ? 'bg-[#f8d7da] text-[#721c24]'
                    : 'bg-[#d1ecf1] text-[#0c5460]'
                "
            >
                {{
                reservation.status === 'confirmed'
                    ? '확정'
                    : reservation.status === 'pending'
                    ? '대기'
                    : reservation.status === 'cancelled'
                    ? '취소'
                    : '환불'
                }}
            </span>
            <div v-if="reservation.status === 'cancelled'" class="mt-3 bg-[#f8f9fa] border border-[#e9ecef] rounded-lg p-4 text-sm">
              <p class="text-[#6c757d] mb-1">취소 사유</p>
              <p class="text-[#1e3a5f]">{{ reservation.cancelReason || '사유 없음' }}</p>
            </div>
          </div>

            <!-- 요청사항 -->
            <div class="mt-6">
            <p class="text-sm text-[#6c757d] mb-2">요청사항</p>
            <div class="bg-[#f8f9fa] border border-[#e9ecef] rounded-lg p-4 text-sm text-[#1e3a5f]">
                {{ reservation.requestNote || '요청사항 없음' }}
            </div>
            </div>

            <!-- 선주문/선결제 메뉴 -->
            <div class="mt-6">
            <p class="text-sm text-[#6c757d] mb-2">선주문 메뉴</p>

            <div
                v-if="reservation.preorderItems && reservation.preorderItems.length"
                class="border border-[#e9ecef] rounded-lg overflow-hidden"
            >
                <div class="bg-[#f8f9fa] px-4 py-3 text-sm font-semibold text-[#1e3a5f]">
                {{ reservation.paymentType === 'prepaid' ? '선결제' : '현장결제' }}
                </div>

                <div class="divide-y divide-[#e9ecef]">
                <div
                    v-for="(item, idx) in reservation.preorderItems"
                    :key="idx"
                    class="px-4 py-3 flex items-center justify-between text-sm"
                >
                    <div class="text-[#1e3a5f]">
                    {{ item.name }} <span class="text-[#6c757d]">x{{ item.qty }}</span>
                    </div>
                    <div class="text-[#1e3a5f] font-semibold">
                    {{ (item.price * item.qty).toLocaleString() }}원
                    </div>
                </div>
                </div>

                <div class="bg-white px-4 py-3 flex items-center justify-between text-sm">
                <span class="text-[#6c757d]">합계</span>
                <span class="font-bold text-[#1e3a5f]">
                    {{
                    reservation.preorderItems
                        .reduce((sum, i) => sum + i.price * i.qty, 0)
                        .toLocaleString()
                    }}원
                </span>
                </div>
            </div>

            <div v-else class="bg-[#f8f9fa] border border-[#e9ecef] rounded-lg p-4 text-sm text-[#6c757d]">
                선주문 내역 없음
            </div>
            </div>
          </div>
        </div>
        <!-- 취소 사유 모달 -->
        <div v-if="cancelModalOpen" class="fixed inset-0 z-50 flex items-center justify-center">
          <div class="absolute inset-0 bg-black/30" @click="closeCancelModal"></div>

          <div class="relative w-full max-w-lg bg-white rounded-xl border border-[#e9ecef] shadow-lg p-6">
            <h3 class="text-lg font-bold text-[#1e3a5f]">예약 취소</h3>
            <p class="text-sm text-[#6c757d] mt-1">취소 사유를 입력해주세요.</p>

            <div class="mt-4">
              <textarea
                v-model="cancelReason"
                rows="4"
                maxlength="50"
                class="w-full border border-[#dee2e6] rounded-lg p-3 text-sm outline-none focus:ring-2 focus:ring-[#ff6b4a]/30"
                placeholder="취소 사유를 50자 이내로 입력해주세요. (예: 고객 요청 / 매장 사정 / 노쇼 등)"
              />
              <p class="mt-2 text-xs text-[#6c757d] text-right">
                {{ cancelReason.trim().length }}/50
              </p>
              <p v-if="cancelError" class="mt-2 text-sm text-[#dc3545]">{{ cancelError }}</p>
            </div>

            <div class="mt-5 flex justify-end gap-2">
              <button
                @click="closeCancelModal"
                class="px-4 py-2 border border-[#dee2e6] rounded-lg text-sm text-[#1e3a5f] hover:bg-[#f8f9fa]"
              >
                닫기
              </button>
              <button @click="submitCancel" :disabled="cancelReason.trim().length > 50 || cancelling" class="px-4 py-2 rounded-lg text-sm text-white bg-[#dc3545] hover:opacity-90">
                취소 확정
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
