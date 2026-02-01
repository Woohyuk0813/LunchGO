<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import { ArrowLeft } from "lucide-vue-next";
import httpRequest from "@/router/httpRequest";
import { useFavorites } from "@/composables/useFavorites";
import { useAccountStore } from "@/stores/account";
import Pagination from "@/components/ui/Pagination.vue";

// 분리한 자식 컴포넌트 임포트
import ReservationHistory from "@/components/ui/ReservationHistory.vue"; // 예정된 예약 목록
import UsageHistory from "@/components/ui/UsageHistory.vue"; // 지난 예약(이용완료/환불) 목록

const route = useRoute();
const { fetchFavorites, favoriteRestaurantIds, clearFavorites } = useFavorites();
const accountStore = useAccountStore();

const devUserId = import.meta.env.DEV ? 1 : 0;
const userId = computed(() => Number(route.query.userId || devUserId || 0));
const loadError = ref("");

// 탭 상태 관리 ('upcoming' | 'past')
const activeTab = ref("upcoming");
const upcomingReservations = ref([]);
const pastReservations = ref([]);
const pastSearchQuery = ref("");
const pastStatusFilter = ref("all");
const pastPageSize = ref(5);
const pastCurrentPage = ref(1);

const getStoredMember = () => {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("member");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());
const memberId = computed(() => {
  const rawId = member.value?.id ?? member.value?.userId ?? member.value?.memberId;
  if (rawId === null || rawId === undefined) return null;
  const parsed = Number(rawId);
  return Number.isNaN(parsed) ? null : parsed;
});

// URL 쿼리에 따라 초기 탭 설정 (예: ?tab=past)
onMounted(() => {
  if (route.query.tab === "past") {
    activeTab.value = "past";
  }
  loadFavorites();
  loadReservations("upcoming");
  loadReservations("past");
});

const loadFavorites = async () => {
  if (!memberId.value) {
    clearFavorites();
    return;
  }
  await fetchFavorites(memberId.value);
};

const statusMap = {
  TEMPORARY: "pending_payment",
  CONFIRMED: "confirmed",
  PREPAID_CONFIRMED: "confirmed",
  COMPLETED: "completed",
  REFUND_PENDING: "refund_pending",
  REFUNDED: "refunded",
  CANCELLED: "cancelled",
  NOSHOW: "refunded",
  NO_SHOW: "refunded",
};

const statusPriority = {
  confirmed: 1,
  pending_payment: 2,
  cancelled: 3,
  restaurant_cancelled: 3,
};

const formatDate = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}.${month}.${day}`;
};

const formatTime = (value) => {
  if (!value) return "";
  return String(value).slice(0, 5);
};

const mapReservation = (item) => {
  const rawStatus = item.reservationStatus || item.status;
  let reservationStatus = statusMap[rawStatus] || "confirmed";
  const cancelledBy = String(item.cancelledBy || "").toUpperCase();
  if (rawStatus === "CANCELLED" && cancelledBy === "OWNER") {
    reservationStatus = "restaurant_cancelled";
  }
  const fallbackVisitCount = reservationStatus === "completed" ? 1 : 0;

  return {
    id: item.id,
    confirmationNumber: item.confirmationNumber,
    restaurant: {
      id: item.restaurant?.id,
      name: item.restaurant?.name,
      address: item.restaurant?.address,
    },
    booking: {
      date: formatDate(item.booking?.date),
      time: formatTime(item.booking?.time),
      partySize: item.booking?.partySize,
    },
    visitCount: item.visitCount ?? fallbackVisitCount,
    daysSinceLastVisit: item.daysSinceLastVisit ?? null,
    payment: item.payment?.amount ? { amount: item.payment.amount } : null,
    reservationStatus,
    cancelledBy: item.cancelledBy,
    cancelledReason: item.cancelledReason || null,
    cancelledAt: item.cancelledAt || null,
    review: item.review
      ? {
          id: item.review.id,
          rating: item.review.rating,
          content: item.review.content || "",
          tags: Array.isArray(item.review.tags) ? item.review.tags : [],
          createdAt: formatDate(item.review.createdAt),
        }
      : null,
  };
};

const isCancelledStatus = (reservation) =>
  reservation?.reservationStatus === "cancelled" ||
  reservation?.reservationStatus === "restaurant_cancelled";

const getBookingTimestamp = (reservation) => {
  const date = reservation?.booking?.date;
  const time = reservation?.booking?.time;
  if (!date) return Number.NEGATIVE_INFINITY;
  const normalizedDate = String(date).replace(/\./g, "-");
  const normalizedTime = time ? String(time) : "00:00";
  const parsed = new Date(`${normalizedDate}T${normalizedTime}`);
  return Number.isNaN(parsed.getTime()) ? Number.NEGATIVE_INFINITY : parsed.getTime();
};

const getCancelledTimestamp = (reservation) => {
  const value = reservation?.cancelledAt;
  if (!value) return Number.NEGATIVE_INFINITY;
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? Number.NEGATIVE_INFINITY : parsed.getTime();
};

const getActivityTimestamp = (reservation) =>
  isCancelledStatus(reservation)
    ? Math.max(getCancelledTimestamp(reservation), getBookingTimestamp(reservation))
    : getBookingTimestamp(reservation);

const sortUpcomingByPriority = (reservations) =>
  [...reservations].sort((a, b) => {
    const priorityA = statusPriority[a.reservationStatus] ?? 99;
    const priorityB = statusPriority[b.reservationStatus] ?? 99;
    if (priorityA !== priorityB) return priorityA - priorityB;
    return getActivityTimestamp(b) - getActivityTimestamp(a);
  });

const filteredPastReservations = computed(() => {
  const query = pastSearchQuery.value.trim().toLowerCase();
  const status = pastStatusFilter.value;

  const filtered = pastReservations.value.filter((reservation) => {
    if (status !== "all" && reservation.reservationStatus !== status) {
      return false;
    }
    if (!query) return true;
    const name = reservation.restaurant?.name || "";
    const address = reservation.restaurant?.address || "";
    return (
      name.toLowerCase().includes(query) ||
      address.toLowerCase().includes(query)
    );
  });
  return [...filtered].sort((a, b) => {
    const priorityA = statusPriority[a.reservationStatus] ?? 99;
    const priorityB = statusPriority[b.reservationStatus] ?? 99;
    if (priorityA !== priorityB) return priorityA - priorityB;
    return getActivityTimestamp(b) - getActivityTimestamp(a);
  });
});

const pastTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredPastReservations.value.length / pastPageSize.value))
);

const pagedPastReservations = computed(() => {
  const start = (pastCurrentPage.value - 1) * pastPageSize.value;
  return filteredPastReservations.value.slice(start, start + pastPageSize.value);
});

const goToPastPage = (page) => {
  const next = Math.min(Math.max(page, 1), pastTotalPages.value);
  pastCurrentPage.value = next;
};

const loadReservations = async (type) => {
  if (!memberId.value) return;
  try {
    const response = await httpRequest.get("/api/reservations/history", {
      userId: memberId.value,
      type,
    });
    const data = Array.isArray(response.data) ? response.data : [];
    const mapped = data.map(mapReservation);
    if (type === "past") {
      pastReservations.value = mapped;
    } else {
      upcomingReservations.value = sortUpcomingByPriority(mapped);
    }
  } catch (error) {
    console.error("예약 내역 조회 실패:", error);
    if (type === "past") {
      pastReservations.value = [];
    } else {
      upcomingReservations.value = [];
    }
  }
};

watch(
  () => [pastSearchQuery.value, pastStatusFilter.value, pastPageSize.value],
  () => {
    pastCurrentPage.value = 1;
  }
);

watch(
  () => memberId.value,
  (next) => {
    if (!next) return;
    loadFavorites();
    loadReservations("upcoming");
    loadReservations("past");
  }
);
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink to="/" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">예약 내역</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-5">
      <div class="bg-white border-b border-[#e9ecef] sticky top-14 z-40">
        <div class="flex">
          <button
              @click="activeTab = 'upcoming'"
              :class="[
              'flex-1 py-3 text-sm font-medium transition-colors relative',
              activeTab === 'upcoming'
                ? 'text-[#1e3a5f] font-semibold'
                : 'text-[#6c757d]',
            ]"
          >
            예약 내역
            <div
                v-if="activeTab === 'upcoming'"
                class="absolute bottom-0 left-0 right-0 h-0.5 bg-[#1e3a5f]"
            ></div>
          </button>
          <button
              @click="activeTab = 'past'"
              :class="[
              'flex-1 py-3 text-sm font-medium transition-colors relative',
              activeTab === 'past'
                ? 'text-[#1e3a5f] font-semibold'
                : 'text-[#6c757d]',
            ]"
          >
            지난 예약
            <div
                v-if="activeTab === 'past'"
                class="absolute bottom-0 left-0 right-0 h-0.5 bg-[#1e3a5f]"
            ></div>
          </button>
        </div>
      </div>

      <div class="px-4 pt-5">
        <ReservationHistory
            v-show="activeTab === 'upcoming'"
            :reservations="upcomingReservations"
        />

        <div v-show="activeTab === 'past'">
          <div class="py-3 border-b border-[#e9ecef]">
            <p class="text-sm text-[#6c757d]">
              총
              <span class="font-semibold text-[#1e3a5f]">{{
                filteredPastReservations.length
              }}</span
              >건의 지난 예약
            </p>
          </div>

          <div class="py-3 border-b border-[#e9ecef] space-y-2">
            <input
              v-model="pastSearchQuery"
              type="text"
              placeholder="식당명 또는 주소 검색"
              class="w-full h-10 px-3 border border-[#dee2e6] rounded-lg text-sm focus:outline-none focus:border-[#ff6b4a]"
            />
            <div class="flex items-center gap-2">
              <select
                v-model="pastStatusFilter"
                class="h-9 px-3 border border-[#dee2e6] rounded-lg text-xs bg-white"
              >
                <option value="all">전체 상태</option>
                <option value="completed">이용완료</option>
              <option value="refund_pending">환불대기</option>
              <option value="refunded">환불완료</option>
              <option value="cancelled">취소</option>
              </select>
              <select
                v-model="pastPageSize"
                class="h-9 px-3 border border-[#dee2e6] rounded-lg text-xs bg-white"
              >
                <option :value="3">3개씩</option>
                <option :value="5">5개씩</option>
                <option :value="10">10개씩</option>
              </select>
            </div>
          </div>

          <UsageHistory
            :reservations="pagedPastReservations"
            :user-id="memberId"
            :favorites="favoriteRestaurantIds"
          />

          <div class="flex items-center justify-center py-4">
            <Pagination
              :current-page="pastCurrentPage"
              :total-pages="pastTotalPages"
              @change-page="goToPastPage"
            />
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
