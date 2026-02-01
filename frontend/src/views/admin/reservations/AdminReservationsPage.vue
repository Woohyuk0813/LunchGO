<script setup>
import { ref, computed, onMounted } from "vue";
import AdminSideBar from "@/components/ui/AdminSideBar.vue";
import AdminHeader from "@/components/ui/AdminHeader.vue";
import Pagination from "@/components/ui/Pagination.vue";
import AdminSearchFilter from "@/components/ui/AdminSearchFilter.vue";
import { Line } from "vue-chartjs";
import httpRequest from "@/router/httpRequest";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";
import {
  Calendar,
  CheckCircle,
  Clock,
  CreditCard,
  XCircle,
  ArrowUpRight,
  ArrowDownRight,
  Activity,
  AlertCircle,
} from "lucide-vue-next";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

// --- 1. 상태 관리 (기간 선택) ---
const chartPeriod = ref("8weeks"); // '8weeks' | '6months' | '2years'

// --- 2. 통계 데이터 (Mock Data Map) ---
const periodStatsMap = {
  "8weeks": {
    label: "최근 8주",
    comparisonText: "지난주 대비",
    total: 1450,
    totalDiff: 125,
    confirmed: 1120,
    confirmedDiff: 98,
    temp: 180,
    tempDiff: -15,
    refundPending: 95,
    refundPendingDiff: 12,
    refunded: 55,
    refundedDiff: -5,
  },
  "6months": {
    label: "최근 6개월",
    comparisonText: "전월 대비",
    total: 5840,
    totalDiff: -320,
    confirmed: 4950,
    confirmedDiff: -150,
    temp: 450,
    tempDiff: 45,
    refundPending: 280,
    refundPendingDiff: 20,
    refunded: 160,
    refundedDiff: 15,
  },
  "2years": {
    label: "최근 2년",
    comparisonText: "전년 동기 대비",
    total: 24500,
    totalDiff: 5600,
    confirmed: 21000,
    confirmedDiff: 5100,
    temp: 2000,
    tempDiff: -300,
    refundPending: 900,
    refundPendingDiff: -50,
    refunded: 600,
    refundedDiff: -120,
  },
};

// --- 3. 차트 데이터 로직 ---
const getWeekLabels = () => {
  const labels = [];
  const today = new Date();
  for (let i = 7; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i * 7);
    labels.push(`${d.getMonth() + 1}월 ${Math.ceil(d.getDate() / 7)}주`);
  }
  return labels;
};

const getMonthLabels = (count) => {
  const labels = [];
  const today = new Date();
  for (let i = count - 1; i >= 0; i--) {
    const d = new Date(today);
    d.setMonth(today.getMonth() - i);
    labels.push(
      `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}`
    );
  }
  return labels;
};

const chartDataMap = {
  "8weeks": {
    labels: getWeekLabels(),
    success: [150, 165, 160, 178, 190, 185, 210, 225],
    completed: [130, 145, 148, 165, 175, 178, 195, 208],
    cancel: [25, 28, 30, 22, 18, 15, 12, 10],
  },
  "6months": {
    labels: getMonthLabels(6),
    success: [650, 720, 680, 850, 920, 1050],
    completed: [600, 680, 640, 800, 880, 1010],
    cancel: [120, 110, 95, 80, 75, 60],
  },
  "2years": {
    labels: getMonthLabels(24),
    success: Array.from(
      { length: 24 },
      (_, i) => 500 + i * 30 + Math.random() * 50
    ),
    completed: Array.from(
      { length: 24 },
      (_, i) => 450 + i * 28 + Math.random() * 50
    ),
    cancel: Array.from(
      { length: 24 },
      (_, i) => 100 - i * 2 + Math.random() * 20
    ),
  },
};

const reservationSuccessData = computed(() => {
  const data = chartDataMap[chartPeriod.value];
  return {
    labels: data.labels,
    datasets: [
      {
        label: "신규 예약",
        data: data.success,
        borderColor: "#4A90E2",
        backgroundColor: "rgba(74, 144, 226, 0.1)",
        tension: 0.4,
        fill: true,
      },
      {
        label: "이용 완료",
        data: data.completed,
        borderColor: "#10B981",
        backgroundColor: "rgba(16, 185, 129, 0.1)",
        tension: 0.4,
        fill: true,
      },
    ],
  };
});

const cancellationTrendData = computed(() => {
  const data = chartDataMap[chartPeriod.value];
  return {
    labels: data.labels,
    datasets: [
      {
        label: "취소/환불",
        data: data.cancel,
        borderColor: "#EF4444",
        backgroundColor: "rgba(239, 68, 68, 0.1)",
        tension: 0.4,
        fill: true,
      },
    ],
  };
});

const currentStats = computed(() => {
  return periodStatsMap[chartPeriod.value];
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "top",
      align: "end",
      labels: { boxWidth: 10, usePointStyle: true },
    },
  },
  scales: {
    y: { beginAtZero: true, grid: { borderDash: [2, 2] } },
    x: { grid: { display: false } },
  },
  interaction: {
    mode: "index",
    intersect: false,
  },
};

// --- 4. 테이블 관련 로직 ---
const searchQuery = ref("");
const selectedStatus = ref("all");
const selectedType = ref("all");
const startDate = ref("");
const endDate = ref("");
const currentPage = ref(1);
const itemsPerPage = 10;

const statusOptions = [
  { value: "all", label: "전체" },
  { value: "temp", label: "임시 예약" },
  { value: "confirmed", label: "예약 확정" },
  { value: "completed", label: "이용 완료" },
  { value: "refund_pending", label: "환불 대기" },
  { value: "refunded", label: "환불 완료" },
];

const typeOptions = [
  { value: "all", label: "전체" },
  { value: "prepaid", label: "선결제" },
  { value: "deposit", label: "예약금" },
];

const getStatusBadgeColor = (status) => {
  const colors = {
    temp: "bg-yellow-100 text-yellow-800",
    confirmed: "bg-blue-100 text-blue-800",
    completed: "bg-green-100 text-green-800",
    refund_pending: "bg-orange-100 text-orange-800",
    refunded: "bg-gray-100 text-gray-800",
  };
  return colors[status] || "bg-gray-100 text-gray-800";
};

const getStatusLabel = (status) => {
  const labels = {
    temp: "임시 예약",
    confirmed: "예약 확정",
    completed: "이용 완료",
    refund_pending: "환불 대기",
    refunded: "환불 완료",
  };
  return labels[status] || status;
};

const getTypeLabel = (type) => (type === "prepaid" ? "선결제" : "예약금");
const maskName = (name) =>
  name.length <= 1 ? name : name[0] + "*".repeat(name.length - 1);

const allReservations = ref([]);

const loadReservations = async () => {
  try {
    const response = await httpRequest.get("/api/admin/reservations");
    if (Array.isArray(response.data)) {
      allReservations.value = response.data;
    }
  } catch (error) {
    console.error("예약 조회 실패:", error);
  }
};

onMounted(() => {
  loadReservations();
});

const filteredReservations = computed(() => {
  let filtered = allReservations.value;
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (r) =>
        r.id.toLowerCase().includes(query) ||
        r.restaurantName.toLowerCase().includes(query) ||
        r.customerName.toLowerCase().includes(query)
    );
  }
  if (selectedStatus.value !== "all")
    filtered = filtered.filter((r) => r.status === selectedStatus.value);
  if (selectedType.value !== "all")
    filtered = filtered.filter((r) => r.type === selectedType.value);
  if (startDate.value || endDate.value) {
    filtered = filtered.filter((r) => {
      const resDate = new Date(r.reservationDateTime);
      if (startDate.value && endDate.value) {
        const start = new Date(startDate.value);
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return resDate >= start && resDate <= end;
      } else if (startDate.value) {
        return resDate >= new Date(startDate.value);
      } else if (endDate.value) {
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return resDate <= end;
      }
      return true;
    });
  }
  return filtered;
});

const paginatedReservations = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredReservations.value.slice(start, end);
});

const totalPages = computed(() =>
  Math.ceil(filteredReservations.value.length / itemsPerPage)
);

const handlePageChange = (page) => (currentPage.value = page);
const handleFilterUpdate = ({ model, value }) => {
  if (model === "selectedStatus") selectedStatus.value = value;
  else if (model === "selectedType") selectedType.value = value;
  currentPage.value = 1;
};
const resetFilters = () => {
  searchQuery.value = "";
  selectedStatus.value = "all";
  selectedType.value = "all";
  startDate.value = "";
  endDate.value = "";
  currentPage.value = 1;
};

const filters = computed(() => [
  {
    model: "selectedStatus",
    label: "예약 상태",
    value: selectedStatus.value,
    options: statusOptions,
  },
  {
    model: "selectedType",
    label: "예약 타입",
    value: selectedType.value,
    options: typeOptions,
  },
]);
</script>

<template>
  <!-- AdminLayout 대신 직접 Flexbox 레이아웃 구성 -->
  <div class="flex h-screen bg-[#f8f9fa] overflow-hidden">
    <!-- 사이드바: 왼쪽 고정 -->
    <AdminSideBar active-menu="reservations" class="flex-shrink-0" />

    <!-- 우측 메인 영역 -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- 헤더: 상단 고정 -->
      <AdminHeader />

      <!-- 컨텐츠 영역: 스크롤 가능 -->
      <main class="flex-1 overflow-y-auto p-8 custom-scrollbar">
        <div class="max-w-7xl mx-auto space-y-6">
          <!-- 1. Header & Period Selector -->
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-3xl font-bold text-[#1e3a5f]">예약 등록 현황</h2>
              <p class="text-sm text-[#6c757d] mt-1">
                전체 식당의 예약 흐름과 취소 현황을 모니터링합니다.
              </p>
            </div>

            <div
              class="flex items-center gap-2 bg-white rounded-lg p-1 border border-[#e9ecef] shadow-sm"
            >
              <button
                v-for="period in [
                  { key: '8weeks', label: '최근 8주' },
                  { key: '6months', label: '최근 6개월' },
                  { key: '2years', label: '최근 2년' },
                ]"
                :key="period.key"
                @click="chartPeriod = period.key"
                :class="[
                  'px-4 py-2 text-sm font-medium rounded-md transition-all',
                  chartPeriod === period.key
                    ? 'bg-[#1e3a5f] text-white shadow-md'
                    : 'text-gray-600 hover:bg-gray-100',
                ]"
              >
                {{ period.label }}
              </button>
            </div>
          </div>

          <!-- 2. Dynamic Stats Cards -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <!-- 총 예약 -->
            <div
              class="bg-white p-6 rounded-xl border border-[#e9ecef] shadow-sm hover:shadow-md transition-shadow"
            >
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-purple-50 rounded-lg">
                  <Calendar class="w-6 h-6 text-purple-600" />
                </div>
                <span
                  class="text-xs font-medium text-gray-500 bg-gray-100 px-2 py-1 rounded"
                  >{{ currentStats.label }}</span
                >
              </div>
              <p class="text-sm text-[#6c757d] mb-1">총 예약 수</p>
              <p class="text-2xl font-bold mb-2">
                {{ currentStats.total.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    currentStats.totalDiff >= 0 ? ArrowUpRight : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1"
                />
                <span>{{
                  Math.abs(currentStats.totalDiff).toLocaleString()
                }}</span>
                <span class="text-[#6c757d] ml-1">{{
                  currentStats.comparisonText
                }}</span>
              </div>
            </div>

            <!-- 예약 확정 -->
            <div
              class="bg-white p-6 rounded-xl border border-[#e9ecef] shadow-sm hover:shadow-md transition-shadow"
            >
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-blue-50 rounded-lg">
                  <CheckCircle class="w-6 h-6 text-blue-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">예약 확정</p>
              <p class="text-2xl font-bold mb-2">
                {{ currentStats.confirmed.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    currentStats.confirmedDiff >= 0
                      ? ArrowUpRight
                      : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1"
                />
                <span>{{
                  Math.abs(currentStats.confirmedDiff).toLocaleString()
                }}</span>
                <span class="text-[#6c757d] ml-1">{{
                  currentStats.comparisonText
                }}</span>
              </div>
            </div>

            <!-- 임시 예약 -->
            <div
              class="bg-white p-6 rounded-xl border border-[#e9ecef] shadow-sm hover:shadow-md transition-shadow"
            >
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-yellow-50 rounded-lg">
                  <Clock class="w-6 h-6 text-yellow-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">임시 예약</p>
              <p class="text-2xl font-bold mb-2">
                {{ currentStats.temp.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    currentStats.tempDiff >= 0 ? ArrowUpRight : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1"
                />
                <span>{{
                  Math.abs(currentStats.tempDiff).toLocaleString()
                }}</span>
                <span class="text-[#6c757d] ml-1">{{
                  currentStats.comparisonText
                }}</span>
              </div>
            </div>

            <!-- 환불 대기 -->
            <div
              class="bg-white p-6 rounded-xl border border-[#e9ecef] shadow-sm hover:shadow-md transition-shadow"
            >
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-orange-50 rounded-lg">
                  <CreditCard class="w-6 h-6 text-orange-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">환불 대기</p>
              <p class="text-2xl font-bold mb-2">
                {{ currentStats.refundPending.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    currentStats.refundPendingDiff >= 0
                      ? ArrowUpRight
                      : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1"
                />
                <span>{{
                  Math.abs(currentStats.refundPendingDiff).toLocaleString()
                }}</span>
                <span class="text-[#6c757d] ml-1">{{
                  currentStats.comparisonText
                }}</span>
              </div>
            </div>

            <!-- 환불 완료 -->
            <div
              class="bg-white p-6 rounded-xl border border-[#e9ecef] shadow-sm hover:shadow-md transition-shadow"
            >
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-gray-50 rounded-lg">
                  <XCircle class="w-6 h-6 text-gray-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">환불 완료</p>
              <p class="text-2xl font-bold mb-2">
                {{ currentStats.refunded.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    currentStats.refundedDiff >= 0
                      ? ArrowUpRight
                      : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1 text-gray-600"
                />
                <span class="text-gray-600">{{
                  Math.abs(currentStats.refundedDiff).toLocaleString()
                }}</span>
                <span class="text-[#6c757d] ml-1">{{
                  currentStats.comparisonText
                }}</span>
              </div>
            </div>
          </div>

          <!-- 3. Chart Section -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div
              class="bg-white p-6 rounded-xl border border-[#e9ecef] shadow-sm"
            >
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h3
                    class="text-lg font-bold text-[#1e3a5f] flex items-center gap-2"
                  >
                    <Activity class="w-5 h-5 text-[#FF6B4A]" />
                    예약 및 이용 완료 추이
                  </h3>
                  <p class="text-sm text-[#6c757d] mt-1">
                    {{ currentStats.label }}간의 예약 흐름
                  </p>
                </div>
              </div>
              <div class="h-64">
                <Line :data="reservationSuccessData" :options="chartOptions" />
              </div>
            </div>

            <div
              class="bg-white p-6 rounded-xl border border-[#e9ecef] shadow-sm"
            >
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h3
                    class="text-lg font-bold text-[#1e3a5f] flex items-center gap-2"
                  >
                    <AlertCircle class="w-5 h-5 text-red-500" />
                    취소/환불 추이
                  </h3>
                  <p class="text-sm text-[#6c757d] mt-1">
                    {{ currentStats.label }}간의 취소 발생 건수
                  </p>
                </div>
              </div>
              <div class="h-64">
                <Line :data="cancellationTrendData" :options="chartOptions" />
              </div>
            </div>
          </div>

          <!-- 4. Table Section -->
          <AdminSearchFilter
            v-model:search-query="searchQuery"
            v-model:start-date="startDate"
            v-model:end-date="endDate"
            :filters="filters"
            :show-date-filter="true"
            search-label="검색"
            search-placeholder="예약번호, 식당명, 예약자명으로 검색"
            start-date-label="시작 날짜"
            end-date-label="종료 날짜"
            @update:filter-value="handleFilterUpdate"
            @reset-filters="resetFilters"
          />

          <div
            class="bg-white rounded-xl border border-[#e9ecef] overflow-hidden shadow-sm"
          >
            <div
              class="px-6 py-4 border-b border-[#e9ecef] flex justify-between items-center bg-gray-50"
            >
              <h3 class="font-bold text-[#1e3a5f]">예약 상세 리스트</h3>
              <span class="text-sm text-gray-500"
                >총 {{ filteredReservations.length }}건</span
              >
            </div>
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-white border-b border-[#e9ecef]">
                  <tr>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약 번호
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      식당명
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약자
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약 일시
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      인원
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      타입
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      상태
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="reservation in paginatedReservations"
                    :key="reservation.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1e3a5f]"
                    >
                      {{ reservation.id }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ reservation.restaurantName }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ maskName(reservation.customerName) }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ reservation.reservationDateTime }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ reservation.partySize }}명
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ getTypeLabel(reservation.type) }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getStatusBadgeColor(reservation.status),
                        ]"
                      >
                        {{ getStatusLabel(reservation.status) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div
                v-if="paginatedReservations.length === 0"
                class="text-center py-12 text-[#6c757d]"
              >
                <p class="text-lg">검색 결과가 없습니다.</p>
              </div>
            </div>
            <div
              v-if="totalPages > 1"
              class="px-6 py-4 border-t border-[#e9ecef] flex justify-center bg-white"
            >
              <Pagination
                :current-page="currentPage"
                :total-pages="totalPages"
                @change-page="handlePageChange"
              />
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* 스크롤바 스타일링 (AdminLayout을 쓰지 않으므로 여기에 직접 포함) */
.custom-scrollbar::-webkit-scrollbar {
  width: 8px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #ff6b4a;
  border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #e55a39;
}
</style>
