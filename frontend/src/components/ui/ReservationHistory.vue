<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import {
  Calendar,
  Clock,
  Users,
  MapPin,
  CheckCircle2,
  ChevronDown,
} from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';

// 부모로부터 예약 데이터 받기
const props = defineProps<{ reservations: any[] }>();
const searchQuery = ref('');
const statusFilter = ref('all');
const dateRangeFilter = ref('3m');
const openDropdown = ref<null | 'status' | 'range'>(null);

const statusOptions = [
  { value: 'all', label: '전체 상태' },
  { value: 'confirmed', label: '예약확정' },
  { value: 'pending_payment', label: '결제대기' },
  { value: 'cancelled', label: '취소' },
  { value: 'restaurant_cancelled', label: '식당 취소' },
];

const dateRangeOptions = [
  { value: '3m', label: '앞으로 3개월' },
  { value: '6m', label: '앞으로 6개월' },
  { value: '12m', label: '앞으로 1년' },
  { value: 'all', label: '전체 기간' },
];

// 상태에 따른 뱃지 스타일 (컴포넌트 내부 로직)
const getStatusInfo = (reservationStatus) => {
  const statusMap = {
    pending_payment: {
      text: '결제대기',
      bgColor: 'bg-rose-50',
      textColor: 'text-rose-600',
      borderColor: 'border-rose-200',
      icon: Clock,
    },
    confirmed: {
      text: '예약확정',
      bgColor: 'bg-blue-50',
      textColor: 'text-blue-600',
      borderColor: 'border-blue-200',
      icon: CheckCircle2,
    },
    cancelled: {
      text: '취소',
      bgColor: 'bg-gray-50',
      textColor: 'text-gray-600',
      borderColor: 'border-gray-200',
    },
    restaurant_cancelled: {
      text: '식당 취소',
      bgColor: 'bg-gray-50',
      textColor: 'text-gray-600',
      borderColor: 'border-gray-200',
    },
  };
  return statusMap[reservationStatus] || statusMap.confirmed;
};

const router = useRouter();

const getReservationId = (reservation) =>
  reservation?.id ?? reservation?.reservationId ?? null;

const goCancel = (id) => {
  router.push({ name: "reservation-cancel", params: { id: String(id) } });
};

const shortConfirmationNumber = (value: unknown) => {
  const raw = String(value ?? '').trim();
  if (!raw) return '';
  const last = raw.split('-').pop() || raw;
  return String(last).slice(-4);
};

const getStatusTextClass = (value: string) => {
  if (value === 'all') {
    return 'text-[#495057]';
  }
  return getStatusInfo(value).textColor;
};

const getStatusIcon = (value: string) => {
  if (value === 'all') return null;
  return getStatusInfo(value).icon || null;
};

const selectedStatusLabel = computed(
  () => statusOptions.find((option) => option.value === statusFilter.value)?.label ?? '전체 상태'
);
const selectedDateRangeLabel = computed(
  () => dateRangeOptions.find((option) => option.value === dateRangeFilter.value)?.label ?? '앞으로 3개월'
);

const toggleDropdown = (key: 'status' | 'range') => {
  openDropdown.value = openDropdown.value === key ? null : key;
};

const closeDropdown = () => {
  openDropdown.value = null;
};

const selectStatus = (value: string) => {
  statusFilter.value = value;
  closeDropdown();
};

const selectDateRange = (value: string) => {
  dateRangeFilter.value = value;
  closeDropdown();
};

const parseReservationDate = (value: string) => {
  if (!value) return null;
  const normalized = String(value).replace(/\./g, '-');
  const date = new Date(normalized);
  if (!Number.isNaN(date.getTime())) return date;
  const parts = String(value).split(/[./-]/);
  if (parts.length >= 3) {
    const [year, month, day] = parts.map((part) => Number(part));
    if (!Number.isNaN(year) && !Number.isNaN(month) && !Number.isNaN(day)) {
      return new Date(year, month - 1, day);
    }
  }
  return null;
};

const isWithinRange = (dateValue: string) => {
  if (dateRangeFilter.value === 'all') return true;
  const date = parseReservationDate(dateValue);
  if (!date) return true;
  const now = new Date();
  now.setHours(0, 0, 0, 0);
  const months = Number(dateRangeFilter.value.replace('m', ''));
  const end = new Date(now);
  end.setMonth(end.getMonth() + months);
  return date >= now && date <= end;
};

const filteredReservations = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  return props.reservations.filter((reservation) => {
    const statusMatch =
      statusFilter.value === 'all' ||
      reservation.reservationStatus === statusFilter.value;
    if (!statusMatch) return false;

    const dateValue = reservation.booking?.date || '';
    if (!isWithinRange(dateValue)) return false;
    if (!query) return true;

    const restaurantName = reservation.restaurant?.name || '';
    const address = reservation.restaurant?.address || '';
    const date = reservation.booking?.date || '';
    const time = reservation.booking?.time || '';
    const confirmation = reservation.confirmationNumber || '';
    const haystack = `${restaurantName} ${address} ${date} ${time} ${confirmation}`
      .toLowerCase()
      .trim();
    return haystack.includes(query);
  });
});

onMounted(() => {
  document.addEventListener('click', closeDropdown);
});

onUnmounted(() => {
  document.removeEventListener('click', closeDropdown);
});
</script>

<template>
  <div class="space-y-3">
    <div class="bg-white border border-[#e9ecef] rounded-xl p-3 flex flex-col gap-2">
      <input
        v-model="searchQuery"
        type="text"
        placeholder="식당명, 주소, 날짜로 검색"
        class="h-9 px-3 rounded-lg border border-[#dee2e6] text-sm text-[#495057] focus:outline-none focus:ring-2 focus:ring-[#ff6b4a]"
      />
      <div class="grid grid-cols-2 gap-2">
        <div class="relative" @click.stop>
          <button
            type="button"
            class="w-full h-9 px-3 border border-[#dee2e6] rounded-lg text-left text-sm text-[#1e3a5f] flex items-center justify-between hover:bg-white transition-colors"
            @click.stop="toggleDropdown('status')"
          >
            <span class="inline-flex items-center gap-2 min-w-0">
              <span
                :class="[
                  'inline-flex items-center gap-1 text-sm font-medium whitespace-nowrap',
                  getStatusTextClass(statusFilter),
                ]"
              >
                <component
                  v-if="getStatusIcon(statusFilter)"
                  :is="getStatusIcon(statusFilter)"
                  class="w-3.5 h-3.5 inline-block"
                />
                {{ selectedStatusLabel }}
              </span>
            </span>
            <ChevronDown class="w-4 h-4 text-[#1E3A5F]" />
          </button>
          <div
            v-if="openDropdown === 'status'"
            class="absolute left-0 right-0 mt-2 bg-white border border-[#e9ecef] rounded-lg shadow-md z-30 overflow-y-auto max-h-56"
          >
            <button
              v-for="option in statusOptions"
              :key="option.value"
              class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]"
              @click.stop="selectStatus(option.value)"
            >
              <span
                :class="[
                  'inline-flex items-center gap-1 text-sm font-medium whitespace-nowrap',
                  getStatusTextClass(option.value),
                ]"
              >
                <component
                  v-if="getStatusIcon(option.value)"
                  :is="getStatusIcon(option.value)"
                  class="w-3.5 h-3.5 inline-block"
                />
                {{ option.label }}
              </span>
            </button>
          </div>
        </div>
        <div class="relative" @click.stop>
          <button
            type="button"
            class="w-full h-9 px-3 border border-[#dee2e6] rounded-lg text-left text-sm text-[#1e3a5f] flex items-center justify-between hover:bg-white transition-colors"
            @click.stop="toggleDropdown('range')"
          >
            <span class="truncate">{{ selectedDateRangeLabel }}</span>
            <ChevronDown class="w-4 h-4 text-[#1E3A5F]" />
          </button>
          <div
            v-if="openDropdown === 'range'"
            class="absolute left-0 right-0 mt-2 bg-white border border-[#e9ecef] rounded-lg shadow-md z-30 overflow-y-auto max-h-56"
          >
            <button
              v-for="option in dateRangeOptions"
              :key="option.value"
              class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]"
              @click.stop="selectDateRange(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="props.reservations.length === 0" class="text-center py-12">
      <p class="text-[#6c757d] text-sm">예정된 예약이 없습니다.</p>
    </div>

    <Card
      v-else-if="filteredReservations.length > 0"
      v-for="reservation in filteredReservations"
      :key="reservation.reservationId || reservation.id"
      class="overflow-hidden border-[#e9ecef] rounded-xl bg-white shadow-sm"
    >
      <div class="p-4">
        <div class="flex items-start justify-between mb-3">
          <div>
            <h3 class="font-semibold text-[#1e3a5f] text-base mb-1">
              {{ reservation.restaurant.name }}
            </h3>
            <p class="text-xs text-[#6c757d]">
              예약번호: {{ shortConfirmationNumber(reservation.confirmationNumber) }}
            </p>
          </div>
          <span
            :class="[
              'px-2.5 py-1 rounded-full border text-xs font-medium whitespace-nowrap',
              getStatusInfo(reservation.reservationStatus).bgColor,
              getStatusInfo(reservation.reservationStatus).textColor,
              getStatusInfo(reservation.reservationStatus).borderColor,
            ]"
          >
            <component
              v-if="getStatusInfo(reservation.reservationStatus).icon"
              :is="getStatusInfo(reservation.reservationStatus).icon"
              class="w-3.5 h-3.5 inline-block mr-1 -mt-0.5"
            />
            {{ getStatusInfo(reservation.reservationStatus).text }}
          </span>
        </div>

        <div class="space-y-2 mb-4">
          <div class="flex items-center gap-2 text-sm">
            <Calendar class="w-4 h-4 text-[#6c757d]" />
            <span class="text-[#495057]">{{ reservation.booking.date }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm">
            <Clock class="w-4 h-4 text-[#6c757d]" />
            <span class="text-[#495057]">{{ reservation.booking.time }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm">
            <Users class="w-4 h-4 text-[#6c757d]" />
            <span class="text-[#495057]"
              >{{ reservation.booking.partySize }}명</span
            >
          </div>
          <div class="flex items-start gap-2 text-sm">
            <MapPin class="w-4 h-4 text-[#6c757d] mt-0.5 flex-shrink-0" />
            <span class="text-[#495057] leading-relaxed">{{
              reservation.restaurant.address
            }}</span>
          </div>
        </div>

        <div
          v-if="
            reservation.cancelledReason &&
            (reservation.reservationStatus === 'cancelled' ||
              reservation.reservationStatus === 'restaurant_cancelled')
          "
          class="text-sm text-[#6c757d] bg-[#f8f9fa] border border-[#e9ecef] rounded-lg px-3 py-2 mb-4"
        >
          취소 사유: {{ reservation.cancelledReason }}
        </div>

        <div class="flex gap-2">
          <RouterLink
            :to="{
              path: `/restaurant/${reservation.restaurant.id}/confirmation`,
              query: { reservationId: getReservationId(reservation) },
            }"
            class="flex-1"
          >
            <Button
              variant="outline"
              class="w-full h-10 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg text-sm font-medium"
            >
              예약 상세
            </Button>
          </RouterLink>
          <Button
            variant="outline"
            class="flex-1 h-10 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg text-sm font-medium"
            @click="goCancel(reservation.id)"
          >
            예약 취소
          </Button>
        </div>
      </div>
    </Card>

    <div v-else class="text-center py-12">
      <p class="text-[#6c757d] text-sm">검색 결과가 없습니다.</p>
    </div>
  </div>
</template>
