<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import { ArrowLeft, CalendarIcon, Clock, Users } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';
import WaitingModal from '@/components/ui/WaitingModal.vue';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';
import { useReservationQueue } from '@/composables/useReservationQueue';

const route = useRoute();
const router = useRouter();
const selectedBookingType = computed(() => {
  const rawType = route.query.type;
  if (typeof rawType === 'string' && rawType) return rawType;
  const selectFlag = route.query.select;
  if (selectFlag === '1' || selectFlag === 'true') return null;
  return 'reservation';
});
const isSelectionMode = computed(() => selectedBookingType.value === null);
const isPreorder = computed(() => selectedBookingType.value === 'preorder');
const restaurantId = route.params.id;

const accountStore = useAccountStore();

const resolveUserId = () => {
  const storeMemberId = accountStore.member?.id;
  if (storeMemberId) return storeMemberId;
  const rawMember = localStorage.getItem('member');
  if (!rawMember) return null;
  try {
    return JSON.parse(rawMember)?.id ?? null;
  } catch (e) {
    return null;
  }
};

const restaurantName = ref('');
const preorderAvailable = ref(false);

const fetchRestaurantName = async () => {
  try {
    const res = await httpRequest.get(`/api/restaurants/${restaurantId}`);
    const data = res?.data || {};
    restaurantName.value = data?.name || '';
    preorderAvailable.value = Boolean(data?.preorderAvailable);
  } catch (e) {
    restaurantName.value = '';
    preorderAvailable.value = false;
  }
};

onMounted(() => {
  fetchRestaurantName();
});

const selectedDateIndex = ref(null);
const selectedTime = ref(null);
const partySize = ref(4);
const requestNote = ref('');

const onInputRequestNote = (e) => {
  const v = e.target.value ?? '';
  if (v.length > 50) {
    requestNote.value = v.slice(0, 50);
  }
};

const dates = computed(() => {
  return Array.from({ length: 14 }, (_, i) => {
    const date = new Date();
    date.setDate(date.getDate() + i);
    return {
      day: date.getDate(),
      month: date.getMonth() + 1,
      weekday: ['일', '월', '화', '수', '목', '금', '토'][date.getDay()],
      isToday: i === 0,
    };
  });
});

const reservationDateRangeText = computed(() => {
  const start = new Date();
  const end = new Date();
  end.setDate(start.getDate() + dates.value.length - 1);
  const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
  const formatShortDate = (date) => {
    const year = String(date.getFullYear()).slice(-2);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const weekday = weekdays[date.getDay()];
    return `${year}.${month}.${day}.${weekday}`;
  };
  return `예약은 오늘(${formatShortDate(start)}) 부터 최대 2주 후(${formatShortDate(end)}) 까지 가능합니다`;
});

const timeSlots = ref(['11:00', '12:00', '13:00', '14:00']);

const canProceed = computed(() => selectedDateIndex.value !== null && selectedTime.value !== null);

const isCreatingReservation = ref(false);
const createErrorMessage = ref('');

const { 
  isWaiting, 
  modalType, 
  modalMessage, 
  queueErrorMessage, 
  currentWaitingCount,
  formattedWaitTime,
  processQueue, 
  handleQueueModalClose 
} = useReservationQueue();

const handleBookingTypeSelect = (type) => {
  router.replace({
    path: `/restaurant/${restaurantId}/booking`,
    query: { type },
  });
};

// queueErrorMessage 변경 시 로컬 에러 메시지 업데이트 (필요 시)
watch(queueErrorMessage, (newVal) => {
  if (newVal) createErrorMessage.value = newVal;
});

const selectedSlotDate = computed(() => {
  if (selectedDateIndex.value === null) return null;
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() + Number(selectedDateIndex.value));
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
});

const selectedSlotTime = computed(() => {
  if (!selectedTime.value) return null;
  return String(selectedTime.value);
});

const reservationTypeValue = computed(() => (isPreorder.value ? 'PREORDER_PREPAY' : 'RESERVATION_DEPOSIT'));

const createReservation = async () => {
  if (!selectedSlotDate.value || !selectedSlotTime.value) return null;

  const normalizeTime = (t) => (t && t.length === 5 ? `${t}:00` : t);
  const userId = resolveUserId();
  if (!userId) {
    throw new Error('로그인이 필요합니다.');
  }

  const payload = {
    userId,
    restaurantId: Number(restaurantId),
    slotDate: selectedSlotDate.value,      // "YYYY-MM-DD"
    slotTime: normalizeTime(selectedTime.value), // "11:00:00"
    partySize: Number(partySize.value),
    reservationType: reservationTypeValue.value, // "RESERVATION_DEPOSIT" or "PREORDER_PREPAY"
    requestMessage: requestNote.value?.trim() || null,
  };

  const res = await httpRequest.post('/api/reservations', payload);
  return res.data;
};

const handleProceed = async () => {
  if (!canProceed.value) return;

  isCreatingReservation.value = true;
  createErrorMessage.value = '';

  // 선주문인 경우 바로 이동 (기존 로직 유지)
  if (isPreorder.value) {
    router.push({
      path: `/restaurant/${restaurantId}/menu`,
      query: {
        type: 'preorder',
        partySize: String(partySize.value),
        requestNote: String(requestNote.value || ''),
        dateIndex: String(selectedDateIndex.value),
        time: String(selectedTime.value),
      },
    });
    return;
  }

  // 일반 예약: 대기열 처리 로직 사용
  await processQueue(
    createReservation, 
    (created) => {
      const reservationId = created?.reservationId;
      if (!reservationId) throw new Error('예약 생성 실패');
      
      router.push({
        path: `/restaurant/${restaurantId}/payment`,
        query: {
          type: 'deposit',
          partySize: String(partySize.value),
          requestNote: String(requestNote.value || ''),
          dateIndex: String(selectedDateIndex.value),
          time: String(selectedTime.value),
          reservationId: String(reservationId),
        },
      });
    },
    isCreatingReservation
  );
};

const selectDate = (idx) => {
  selectedDateIndex.value = idx;
};
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink :to="`/restaurant/${restaurantId}`" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">
          {{
            isSelectionMode
              ? '예약 방식 선택'
              : isPreorder
                ? '선주문/선결제'
                : '예약 정보 입력'
          }}
        </h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-24">
      <template v-if="isSelectionMode">
        <div class="bg-white px-4 py-8 border-b border-[#e9ecef]">
          <h2 class="text-base font-semibold text-[#1e3a5f] mb-2">
            예약 방식 선택
          </h2>
          <p class="text-xs text-[#6c757d]">
            원하는 예약 유형을 선택해주세요.
          </p>
          <div class="mt-5 flex gap-3">
            <Button
              class="flex-1 h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed"
              @click="handleBookingTypeSelect('reservation')"
            >
              예약하기
            </Button>
            <Button
              v-if="preorderAvailable"
              class="flex-1 h-12 bg-white border-2 border-[#ff6b4a] text-[#ff6b4a] font-semibold text-base rounded-xl hover:bg-[#fff5f3] transition-colors"
              @click="handleBookingTypeSelect('preorder')"
            >
              선주문/선결제
            </Button>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
          <div class="flex items-center gap-2 mb-4">
            <CalendarIcon class="w-5 h-5 text-[#ff6b4a]" />
            <h2 class="text-base font-semibold text-[#1e3a5f]">날짜 선택</h2>
          </div>
          <p class="text-xs text-[#6c757d] mb-4">
            {{ reservationDateRangeText }}
          </p>

          <div class="overflow-x-auto -mx-4 px-4">
            <div class="flex gap-2 pb-2">
              <button
                  v-for="(date, idx) in dates"
                  :key="idx"
                  @click="selectDate(idx)"
                  :class="`flex-shrink-0 w-16 h-20 rounded-lg border-2 flex flex-col items-center justify-center gap-1 transition-all ${
                  selectedDateIndex === idx
                    ? 'border-[#ff6b4a] bg-[#fff5f3]'
                    : 'border-[#dee2e6] bg-white hover:border-[#ffc4b8]'
                }`"
              >
                <span :class="`text-xs ${selectedDateIndex === idx ? 'text-[#ff6b4a] font-semibold' : 'text-[#6c757d]'}`">
                  {{ date.weekday }}
                </span>
                <span :class="`text-lg font-semibold ${selectedDateIndex === idx ? 'text-[#ff6b4a]' : 'text-[#1e3a5f]'}`">
                  {{ date.day }}
                </span>
                <span :class="`text-xs ${selectedDateIndex === idx ? 'text-[#ff6b4a]' : 'text-[#6c757d]'}`">
                  {{ date.month }}월
                </span>
              </button>
            </div>
          </div>
        </div>

        <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
          <div class="flex items-center gap-2 mb-4">
            <Clock class="w-5 h-5 text-[#ff6b4a]" />
            <h2 class="text-base font-semibold text-[#1e3a5f]">시간 선택</h2>
          </div>

          <div class="grid grid-cols-4 gap-2">
            <button
                v-for="time in timeSlots"
                :key="time"
                @click="selectedTime = time"
                :class="`h-11 rounded-lg border-2 text-sm font-medium transition-all ${
                selectedTime === time
                  ? 'border-[#ff6b4a] bg-[#fff5f3] text-[#ff6b4a]'
                  : 'border-[#dee2e6] bg-white text-[#495057] hover:border-[#ffc4b8]'
              }`"
            >
              {{ time }}
            </button>
          </div>
        </div>

        <div class="bg-white px-4 py-5">
          <div class="flex items-center gap-2 mb-4">
            <Users class="w-5 h-5 text-[#ff6b4a]" />
            <h2 class="text-base font-semibold text-[#1e3a5f]">인원 선택</h2>
          </div>

          <div class="flex items-center justify-between max-w-xs mx-auto">
            <button
                @click="partySize = Math.max(4, partySize - 1)"
                :disabled="partySize <= 4"
                class="w-12 h-12 rounded-full border-2 border-[#dee2e6] bg-white text-[#1e3a5f] font-bold text-xl disabled:opacity-30 disabled:cursor-not-allowed hover:border-[#ff6b4a] hover:text-[#ff6b4a] transition-colors"
            >
              -
            </button>

            <div class="text-center">
              <p class="text-3xl font-bold text-[#1e3a5f]">{{ partySize }}</p>
              <p class="text-sm text-[#6c757d] mt-1">명</p>
            </div>

            <button
                @click="partySize = Math.min(12, partySize + 1)"
                :disabled="partySize >= 12"
                class="w-12 h-12 rounded-full border-2 border-[#dee2e6] bg-white text-[#1e3a5f] font-bold text-xl disabled:opacity-30 disabled:cursor-not-allowed hover:border-[#ff6b4a] hover:text-[#ff6b4a] transition-colors"
            >
              +
            </button>
          </div>

          <p class="text-xs text-[#6c757d] text-center mt-4 leading-relaxed">
            최소 4인 ~ 최대 12인까지 예약 가능합니다
          </p>
        </div>

        <div v-if="canProceed" class="bg-white px-4 py-5 border-b border-[#e9ecef]">
          <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">요청사항 (선택)</h2>

          <textarea
              v-model="requestNote"
              :maxlength="50"
              @input="onInputRequestNote"
              placeholder="요청사항이 있다면 입력해주세요 (최대 50자)"
              class="w-full border-2 border-[#dee2e6] rounded-lg p-3 text-sm text-[#495057] resize-none bg-white
                  focus:outline-none focus:border-[#ffc4b8] transition-all"
              rows="3"
          ></textarea>

          <div class="mt-2 text-xs text-right text-[#6c757d]">
            {{ requestNote.length }}/50
          </div>
        </div>

        <div v-if="canProceed" class="mx-4 mt-4">
          <Card class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card">
            <h3 class="font-semibold text-[#1e3a5f] mb-3">{{ isPreorder ? '선주문 정보' : '예약 정보' }}</h3>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-[#6c757d]">식당명</span>
                <span class="text-[#1e3a5f] font-medium">{{ restaurantName || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-[#6c757d]">요청사항</span>
                <span class="text-[#1e3a5f] font-medium">{{ requestNote?.trim() || '-' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-[#6c757d]">예약 날짜</span>
                <span class="text-[#1e3a5f] font-medium">
                  {{ dates[selectedDateIndex]?.month }}월 {{ dates[selectedDateIndex]?.day }}일 ({{
                    dates[selectedDateIndex]?.weekday
                  }})
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-[#6c757d]">예약 시간</span>
                <span class="text-[#1e3a5f] font-medium">{{ selectedTime }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-[#6c757d]">인원</span>
                <span class="text-[#1e3a5f] font-medium">{{ partySize }}명</span>
              </div>
            </div>
          </Card>
        </div>

        <div v-if="createErrorMessage" class="mx-4 mt-3">
          <div class="text-sm text-[#e03131]">{{ createErrorMessage }}</div>
        </div>
      </template>
    </main>

    <div
      v-if="!isSelectionMode"
      class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg"
    >
      <div class="max-w-[500px] mx-auto px-4 py-3">
        <Button
            :disabled="!canProceed || isCreatingReservation"
            @click="handleProceed"
            class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ isCreatingReservation ? '처리 중...' : isPreorder ? '메뉴 선택하기' : '예약금 결제하기' }}
        </Button>
      </div>
    </div>

    <WaitingModal 
      :isOpen="isWaiting" 
      :type="modalType"
      :message="modalMessage || undefined"
      :waitingCount="currentWaitingCount"
      :estimatedTime="formattedWaitTime"
      @close="() => {
        isCreatingReservation = false;
        handleQueueModalClose();
      }"
    />
  </div>
</template>

<style scoped></style>
