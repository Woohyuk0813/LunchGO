<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { CheckCircle2, MapPin, Calendar, Clock, Users } from 'lucide-vue-next';
import { RouterLink, useRoute, useRouter } from 'vue-router'; // Import Vue RouterLink
import httpRequest from '@/router/httpRequest';
import Button from '@/components/ui/Button.vue'; // Import custom Button
import Card from '@/components/ui/Card.vue';

const route = useRoute();
const router = useRouter();
const reservationId = route.query.reservationId || null;
const isLoading = ref(false);
const errorMessage = ref('');
const completionAttempted = ref(false);
const paymentStatus = ref({ paid: false, paidAt: '' });
const bookingSummary = ref({ paymentDeadlineAt: null });
const nowTick = ref(Date.now());
let countdownTimer = null;

const normalizeDeadline = (raw) => {
  if (!raw) return null;
  if (typeof raw !== "string") return raw;
  if (raw.includes("T")) return raw;
  return raw.replace(" ", "T");
};

const countdownText = computed(() => {
  const deadlineRaw = normalizeDeadline(bookingSummary.value.paymentDeadlineAt);
  if (!deadlineRaw) return "";
  const deadline = new Date(deadlineRaw);
  if (Number.isNaN(deadline.getTime())) return "";
  const diffMs = deadline.getTime() - nowTick.value;
  if (diffMs <= 0) {
    return "결제 시간이 초과되었습니다";
  }
  const totalSec = Math.floor(diffMs / 1000);
  const minutes = Math.floor(totalSec / 60);
  const seconds = totalSec % 60;
  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;
});

const isPendingPayment = computed(
  () => reservation.value.status !== "CANCELLED" && !paymentStatus.value.paid
);

const shortConfirmationNumber = (value) => {
  const raw = String(value ?? '').trim();
  if (!raw) return '';
  const last = raw.split('-').pop() || raw;
  return String(last).slice(-4);
};

const reservation = ref({
  confirmationNumber: '',
  status: '',
  cancelledBy: '',
  restaurant: {
    name: '',
    address: '',
    phone: '',
  },
  booking: {
    date: '',
    time: '',
    partySize: null,
    requestNote: '',
  },
  payment: {
    amount: 0,
    method: '',
    paidAt: '',
  },
  menuItems: [],
});

const fetchPaymentStatus = async () => {
  if (!reservationId) return;
  try {
    const response = await httpRequest.get(
      `/api/reservations/${reservationId}/confirmation/status`
    );
    const paidAt = response?.data?.paidAt || '';
    paymentStatus.value = {
      paid: Boolean(response?.data?.paid || paidAt),
      paidAt,
    };
  } catch (error) {
    paymentStatus.value = { paid: false, paidAt: '' };
  }
};

const fetchReservationDetail = async () => {
  if (!reservationId) return;
  isLoading.value = true;
  errorMessage.value = '';
  try {
    // TODO: 백엔드 스펙 확정 후 엔드포인트/필드 매핑 조정
    const response = await httpRequest.get(`/api/reservations/${reservationId}/confirmation`);
    const data = response?.data || {};

    reservation.value = {
      confirmationNumber: data.reservationCode || reservation.value.confirmationNumber,
      status: data.reservationStatus || data.status || reservation.value.status,
      cancelledBy: data.cancelledBy ? String(data.cancelledBy).toUpperCase() : reservation.value.cancelledBy,
      restaurant: {
        name: data.restaurant?.name || reservation.value.restaurant.name,
        address: data.restaurant?.address || reservation.value.restaurant.address,
        phone: data.restaurant?.phone || reservation.value.restaurant.phone,
      },
      booking: {
        date: data.booking?.date || reservation.value.booking.date,
        time: data.booking?.time || reservation.value.booking.time,
        partySize: data.booking?.partySize ?? reservation.value.booking.partySize,
        requestNote: data.booking?.requestNote || '',
      },
      payment: {
        amount: data.payment?.amount ?? reservation.value.payment.amount,
        method: data.payment?.method || reservation.value.payment.method,
        paidAt: data.payment?.paidAt || reservation.value.payment.paidAt,
      },
      menuItems: Array.isArray(data.menuItems) ? data.menuItems : [],
    };
  } catch (error) {
    errorMessage.value = error?.message || '예약 정보를 불러오지 못했습니다.';
  } finally {
    isLoading.value = false;
  }
};

const fetchBookingSummary = async () => {
  if (!reservationId) return;
  try {
    const response = await httpRequest.get(`/api/reservations/${reservationId}/summary`);
    bookingSummary.value.paymentDeadlineAt =
      response.data?.paymentDeadlineAt ?? response.data?.holdExpiresAt ?? null;
  } catch (error) {
    bookingSummary.value.paymentDeadlineAt = null;
  }
};

const isRedirectFailure = () => {
  const code = route.query.code ? String(route.query.code) : '';
  const pgCode = route.query.pgCode ? String(route.query.pgCode) : '';
  const pgMessage = route.query.pgMessage ? String(route.query.pgMessage) : '';
  const message = route.query.message ? String(route.query.message) : '';

  if (code.startsWith('FAILURE') || code.startsWith('ERROR')) {
    return true;
  }
  if (pgCode || pgMessage || message) {
    return true;
  }
  return false;
};

const redirectToPayment = () => {
  router.replace({
    path: `/restaurant/${route.params.id}/payment`,
    query: {
      type: route.query.type || 'deposit',
      totalAmount: route.query.totalAmount,
      partySize: route.query.partySize,
      requestNote: route.query.requestNote,
      dateIndex: route.query.dateIndex,
      time: route.query.time,
      reservationId: route.query.reservationId,
      paymentError: route.query.message || route.query.pgMessage || '결제가 취소되었습니다. 다시 시도해 주세요.',
    },
  });
};

const completePaymentFromRedirect = async () => {
  if (completionAttempted.value) return;
  completionAttempted.value = true;

  console.info("[PortOne] confirmation route.query", { ...route.query });
  const isRedirectFlow =
    route.query.redirected === "1" || route.query.redirected === 1;
  if (!isRedirectFlow) {
    return;
  }
  if (isRedirectFailure()) {
    redirectToPayment();
    return true;
  }
  const merchantUid =
    route.query.paymentId ||
    route.query.merchantUid ||
    route.query.merchant_uid ||
    null;
  const paidAmount = Number(route.query.totalAmount || route.query.amount || 0);
  const impUid =
    route.query.imp_uid ||
    route.query.impUid ||
    route.query.txId ||
    route.query.transactionId ||
    null;

  if (!merchantUid || !Number.isFinite(paidAmount) || paidAmount <= 0) {
    console.warn("[PortOne] redirect params missing/invalid", {
      merchantUid,
      paidAmount,
      impUid,
    });
    return;
  }

  try {
    await httpRequest.post('/api/payments/portone/complete', {
      merchantUid: String(merchantUid),
      impUid: impUid ? String(impUid) : null,
      paidAmount,
    });
    console.info("[PortOne] complete from redirect success", {
      merchantUid,
      paidAmount,
    });
  } catch (error) {
    errorMessage.value =
      error?.message || '결제 완료 처리에 실패했습니다. 다시 확인해 주세요.';
    console.error("[PortOne] complete from redirect failed", error);
  }
  return false;
};

onMounted(async () => {
  const handledRedirect = await completePaymentFromRedirect();
  if (handledRedirect) {
    return;
  }
  await fetchPaymentStatus();
  await fetchBookingSummary();
  await fetchReservationDetail();
  countdownTimer = setInterval(() => {
    nowTick.value = Date.now();
  }, 1000);
});

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
  }
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <main class="max-w-[500px] mx-auto pb-24">
      <!-- Success Header -->
      <div class="bg-white px-4 pt-12 pb-8 text-center border-b border-[#e9ecef]">
        <div class="w-16 h-16 rounded-full gradient-primary flex items-center justify-center mx-auto mb-4 shadow-button-hover">
          <component :is="isPendingPayment ? Clock : CheckCircle2" class="w-10 h-10 text-white" />
        </div>
        <h1 class="text-2xl font-bold text-[#1e3a5f] mb-2">
          <template v-if="reservation.status === 'CANCELLED'">
            {{ reservation.cancelledBy === 'OWNER' ? "식당 취소되었습니다" : "예약이 취소되었습니다" }}
          </template>
          <template v-else>
            {{ paymentStatus.paid ? "예약이 완료되었습니다" : "결제 대기 중입니다" }}
          </template>
        </h1>
        <p class="text-sm text-[#6c757d] leading-relaxed">
          <template v-if="reservation.status === 'CANCELLED'">
            {{ reservation.cancelledBy === 'OWNER' ? "식당 사정으로 취소되었습니다." : "해당 예약은 취소 처리되었습니다." }}
          </template>
          <template v-else-if="paymentStatus.paid">
            예약 정보를 확인하시고
            <br />
            방문 전 예약 내역을 확인해 주세요
          </template>
          <template v-else>
            결제가 완료되면 예약이 확정됩니다.
            <br />
            결제 후 다시 확인해 주세요
          </template>
        </p>
        <p
          v-if="isPendingPayment && countdownText"
          class="mt-3 text-sm text-red-500"
        >
          결제 남은 시간 : {{ countdownText }}
        </p>
        <p v-if="isLoading" class="mt-3 text-xs text-[#6c757d]">예약 정보를 불러오는 중...</p>
        <p v-if="errorMessage" class="mt-3 text-xs text-red-500">{{ errorMessage }}</p>
      </div>

      <!-- Confirmation Number -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <div class="text-center">
          <p class="text-lg font-bold text-[#1e3a5f] tracking-wide">
            예약 번호 : {{ shortConfirmationNumber(reservation.confirmationNumber) }}
          </p>
        </div>
      </div>

      <!-- Restaurant Info -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef] mt-2">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">식당 정보</h2>
        <div class="space-y-3">
          <div>
            <p class="font-semibold text-[#1e3a5f] text-lg mb-1">{{ reservation.restaurant.name }}</p>
          </div>
          <div class="flex items-start gap-2 text-sm">
            <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{ reservation.restaurant.address }}</span>
          </div>
        </div>
      </div>

      <!-- Booking Details -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">예약 정보</h2>
        <div class="space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Calendar class="w-4 h-4 text-[#6c757d]" />
              <span class="text-sm text-[#6c757d]">예약 날짜</span>
            </div>
            <span class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.date }}</span>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Clock class="w-4 h-4 text-[#6c757d]" />
              <span class="text-sm text-[#6c757d]">예약 시간</span>
            </div>
            <span class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.time }}</span>
          </div>

          <div class="flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Users class="w-4 h-4 text-[#6c757d]" />
              <span class="text-sm text-[#6c757d]">예약 인원</span>
            </div>
            <span class="text-sm font-semibold text-[#1e3a5f]">{{ reservation.booking.partySize }}명</span>
          </div>
        </div>

        <!-- Request Note (Read-only) -->
        <div class="mt-4 pt-4 border-t border-[#e9ecef]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">요청사항</h3>

          <div class="rounded-xl border border-[#e9ecef] bg-[#f8f9fa] px-3 py-3">
            <p class="text-sm text-[#495057] whitespace-pre-line">
              {{ reservation.booking.requestNote?.trim() ? reservation.booking.requestNote : '-' }}
            </p>
          </div>

          <p class="mt-2 text-xs text-[#6c757d] leading-relaxed">
            요청사항은 매장 참고용이며, 예약 변경은 매장으로 직접 문의해 주세요.
          </p>
        </div>
      </div>

      <!-- Order Info -->
      <div
          v-if="reservation.menuItems && reservation.menuItems.length"
          class="bg-white px-4 py-5 border-b border-[#e9ecef] mt-2"
      >
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">주문 내역</h2>

        <div class="divide-y divide-[#e9ecef]">
          <div
              v-for="(item, idx) in reservation.menuItems"
              :key="idx"
              class="py-4 flex items-center justify-between"
          >
            <div class="text-base text-[#495057]">
              <span class="font-semibold">{{ item.name }}</span>
              <span class="text-[#6c757d] font-normal"> · {{ item.quantity }}개</span>
            </div>

            <div class="text-base font-semibold text-[#1e3a5f]">
              {{ (item.lineAmount ?? (item.unitPrice * item.quantity)).toLocaleString() }}원
            </div>
          </div>
        </div>

        <div class="pt-4 mt-1 border-t border-[#e9ecef] flex items-center justify-between">
          <span class="text-sm text-[#6c757d]">주문 합계</span>

          <span class="text-lg font-bold text-[#1e3a5f]">
      {{
              reservation.menuItems
                  .reduce((sum, it) => sum + (it.lineAmount ?? (it.unitPrice * it.quantity)), 0)
                  .toLocaleString()
            }}원
    </span>
        </div>
      </div>

      <!-- Payment Info -->
      <div v-if="reservation.status !== 'CANCELLED'" class="bg-white px-4 py-5 mt-2">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">결제 정보</h2>
        <div v-if="paymentStatus.paid" class="space-y-2.5">
          <div class="flex items-center justify-between text-sm">
            <span class="text-[#6c757d]">결제 금액</span>
            <span class="font-semibold text-[#1e3a5f]">{{ reservation.payment.amount.toLocaleString() }}원</span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span class="text-[#6c757d]">결제 수단</span>
            <span class="font-medium text-[#495057]">{{ reservation.payment.method }}</span>
          </div>
          <div class="flex items-center justify-between text-sm">
            <span class="text-[#6c757d]">결제 일시</span>
            <span class="font-medium text-[#495057]">{{ reservation.payment.paidAt }}</span>
          </div>
        </div>
        <div v-else class="text-sm text-[#6c757d] leading-relaxed">
          결제 대기 중입니다. 결제 완료 후 결제 정보가 표시됩니다.
        </div>
      </div>

      <!-- Notice -->
      <div class="mx-4 mt-4">
        <Card class="p-4 border-[#e9ecef] rounded-xl bg-[#f8f9fa]">
          <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">예약 안내</h3>
          <ul class="space-y-1 text-xs text-[#6c757d] leading-relaxed">
            <li>• 예약 시간 10분 전까지 도착해 주세요</li>
            <li>• 예약 시간이 지나면 자동으로 취소될 수 있습니다</li>
            <li>• 예약 취소는 마이페이지에서 가능합니다</li>
            <li>• 문의사항은 식당으로 직접 연락 주시기 바랍니다</li>
          </ul>
        </Card>
      </div>
    </main>

    <!-- Fixed Bottom Buttons -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="max-w-[500px] mx-auto px-4 py-3 flex gap-2">
        <RouterLink
          v-if="!paymentStatus.paid"
          :to="{
            path: `/restaurant/${route.params.id}/payment`,
            query: {
              type: route.query.type || 'deposit',
              totalAmount: route.query.totalAmount,
              partySize: route.query.partySize,
              requestNote: route.query.requestNote,
              dateIndex: route.query.dateIndex,
              time: route.query.time,
              reservationId: route.query.reservationId,
            },
          }"
          class="flex-1"
        >
          <Button
            class="w-full h-12 bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white rounded-xl font-semibold shadow-button-hover hover:shadow-button-pressed"
          >
            결제 페이지로
          </Button>
        </RouterLink>
        <RouterLink to="/" class="flex-1">
          <Button
            variant="outline"
            class="w-full h-12 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-xl font-semibold"
          >
            홈으로
          </Button>
        </RouterLink>
        <RouterLink to="/my-reservations" class="flex-1">
          <Button class="w-full h-12 gradient-primary text-white font-semibold rounded-xl shadow-button-hover hover:shadow-button-pressed">
            예약 내역
          </Button>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
