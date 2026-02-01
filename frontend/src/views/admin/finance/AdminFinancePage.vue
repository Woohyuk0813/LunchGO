<script setup>
import { ref, computed } from "vue";
import AdminSidebar from "@/components/ui/AdminSideBar.vue";
import AdminHeader from "@/components/ui/AdminHeader.vue";
import Pagination from "@/components/ui/Pagination.vue";
import AdminSearchFilter from "@/components/ui/AdminSearchFilter.vue";
import {
  DollarSign,
  TrendingUp,
  CheckCircle,
  Percent,
  ArrowUpRight,
  ArrowDownRight,
} from "lucide-vue-next";
import { Line, Pie } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

// 검색 및 필터
const searchQuery = ref("");
const selectedStatus = ref("all");
const startDate = ref("");
const endDate = ref("");

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 10;

// 매출 추이 기간 선택
const selectedPeriod = ref("6months"); // '8weeks', '6months', '2years'

// 정산 상태 옵션
const statusOptions = [
  { value: "all", label: "전체" },
  { value: "pending", label: "정산 예정" },
  { value: "processing", label: "처리중" },
  { value: "completed", label: "정산 완료" },
  { value: "failed", label: "정산 실패" },
  { value: "hold", label: "보류" },
];

// 정산 상태별 배지 색상
const getStatusBadgeColor = (status) => {
  const colors = {
    pending: "bg-yellow-100 text-yellow-800",
    processing: "bg-blue-100 text-blue-800",
    completed: "bg-green-100 text-green-800",
    failed: "bg-red-100 text-red-800",
    hold: "bg-gray-100 text-gray-800",
  };
  return colors[status] || "bg-gray-100 text-gray-800";
};

// 정산 상태 라벨
const getStatusLabel = (status) => {
  const labels = {
    pending: "정산 예정",
    processing: "처리중",
    completed: "정산 완료",
    failed: "정산 실패",
    hold: "보류",
  };
  return labels[status] || status;
};

// 숫자 포맷팅 함수
const formatNumber = (num) => {
  return num.toLocaleString();
};

// Mock 데이터 (총 58개)
const allSettlements = ref([
  {
    id: "ST001",
    restaurantName: "한신포차 강남점",
    settlementAmount: 3200000,
    settlementDate: "2024-12-15",
    status: "completed",
  },
  {
    id: "ST002",
    restaurantName: "본죽&비빔밥 서초점",
    settlementAmount: 850000,
    settlementDate: "2024-12-14",
    status: "completed",
  },
  {
    id: "ST003",
    restaurantName: "스시로 판교점",
    settlementAmount: 2100000,
    settlementDate: "2024-12-16",
    status: "pending",
  },
  {
    id: "ST004",
    restaurantName: "아웃백 스테이크하우스 홍대점",
    settlementAmount: 4500000,
    settlementDate: "2024-12-13",
    status: "completed",
  },
  {
    id: "ST005",
    restaurantName: "청년다방 신논현점",
    settlementAmount: 1200000,
    settlementDate: "2024-12-17",
    status: "processing",
  },
  {
    id: "ST006",
    restaurantName: "곱창이야기 강남점",
    settlementAmount: 2800000,
    settlementDate: "2024-12-12",
    status: "completed",
  },
  {
    id: "ST007",
    restaurantName: "교촌치킨 역삼점",
    settlementAmount: 1500000,
    settlementDate: "2024-12-18",
    status: "pending",
  },
  {
    id: "ST008",
    restaurantName: "도쿄스테이크 압구정점",
    settlementAmount: 3800000,
    settlementDate: "2024-12-11",
    status: "completed",
  },
  {
    id: "ST009",
    restaurantName: "한신포차 신촌점",
    settlementAmount: 2900000,
    settlementDate: "2024-12-19",
    status: "failed",
  },
  {
    id: "ST010",
    restaurantName: "본죽&비빔밥 잠실점",
    settlementAmount: 950000,
    settlementDate: "2024-12-10",
    status: "completed",
  },
  // 추가 데이터 생성 (ST011 ~ ST058)
  ...Array.from({ length: 48 }, (_, i) => {
    const idx = i + 11;
    const statusList = [
      "pending",
      "processing",
      "completed",
      "completed",
      "completed",
      "failed",
      "hold",
    ];
    const status = statusList[i % statusList.length];

    const monthOffset = Math.floor(i / 8);
    const now = new Date();
    const targetMonth = now.getMonth() - monthOffset;
    const targetYear = now.getFullYear() + Math.floor(targetMonth / 12);
    const finalMonth = (((targetMonth % 12) + 12) % 12) + 1;
    const day = 1 + (i % 28);

    return {
      id: `ST${String(idx).padStart(3, "0")}`,
      restaurantName:
        [
          "한신포차",
          "본죽&비빔밥",
          "스시로",
          "아웃백 스테이크하우스",
          "청년다방",
          "곱창이야기",
        ][i % 6] +
        " " +
        ["강남점", "서초점", "판교점", "홍대점"][i % 4],
      settlementAmount: 500000 + i * 100000,
      settlementDate: `${targetYear}-${String(finalMonth).padStart(
        2,
        "0"
      )}-${String(day).padStart(2, "0")}`,
      status: status,
    };
  }),
]);

// 필터링된 정산 목록
const filteredSettlements = computed(() => {
  let filtered = allSettlements.value;

  // 검색어 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter((s) =>
      s.restaurantName.toLowerCase().includes(query)
    );
  }

  // 상태 필터
  if (selectedStatus.value !== "all") {
    filtered = filtered.filter((s) => s.status === selectedStatus.value);
  }

  // 날짜 범위 필터
  if (startDate.value || endDate.value) {
    filtered = filtered.filter((s) => {
      const settlementDate = new Date(s.settlementDate);

      if (startDate.value && endDate.value) {
        const start = new Date(startDate.value);
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return settlementDate >= start && settlementDate <= end;
      } else if (startDate.value) {
        const start = new Date(startDate.value);
        return settlementDate >= start;
      } else if (endDate.value) {
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return settlementDate <= end;
      }

      return true;
    });
  }

  return filtered;
});

// 통계 데이터
const stats = computed(() => {
  const now = new Date();
  const currentMonth = now.getMonth() + 1;
  const currentYear = now.getFullYear();
  const lastMonth = currentMonth === 1 ? 12 : currentMonth - 1;
  const lastMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;

  const allSettlementsList = allSettlements.value;

  // 이번 달 정산 내역
  const thisMonthSettlements = allSettlementsList.filter((s) => {
    const settlementDate = new Date(s.settlementDate);
    return (
      settlementDate.getMonth() + 1 === currentMonth &&
      settlementDate.getFullYear() === currentYear
    );
  });

  // 지난 달 정산 내역
  const lastMonthSettlements = allSettlementsList.filter((s) => {
    const settlementDate = new Date(s.settlementDate);
    return (
      settlementDate.getMonth() + 1 === lastMonth &&
      settlementDate.getFullYear() === lastMonthYear
    );
  });

  // 이번 달 총매출
  const totalSalesThisMonth = thisMonthSettlements.reduce(
    (sum, s) => sum + s.settlementAmount,
    0
  );
  const totalSalesLastMonth = lastMonthSettlements.reduce(
    (sum, s) => sum + s.settlementAmount,
    0
  );

  // 정산 예정액
  const pendingAmount = thisMonthSettlements
    .filter((s) => s.status === "pending" || s.status === "processing")
    .reduce((sum, s) => sum + s.settlementAmount, 0);
  const pendingAmountLastMonth = lastMonthSettlements
    .filter((s) => s.status === "pending" || s.status === "processing")
    .reduce((sum, s) => sum + s.settlementAmount, 0);

  // 정산 완료액
  const completedAmount = thisMonthSettlements
    .filter((s) => s.status === "completed")
    .reduce((sum, s) => sum + s.settlementAmount, 0);
  const completedAmountLastMonth = lastMonthSettlements
    .filter((s) => s.status === "completed")
    .reduce((sum, s) => sum + s.settlementAmount, 0);

  // 수수료 매출 (총매출의 10%로 가정)
  const commissionRevenue = Math.floor(totalSalesThisMonth * 0.1);
  const commissionRevenueLastMonth = Math.floor(totalSalesLastMonth * 0.1);

  // 전월 대비 증감
  const totalDiff = totalSalesThisMonth - totalSalesLastMonth;
  const pendingDiff = pendingAmount - pendingAmountLastMonth;
  const completedDiff = completedAmount - completedAmountLastMonth;
  const commissionDiff = commissionRevenue - commissionRevenueLastMonth;

  return {
    totalSales: totalSalesThisMonth,
    totalDiff,
    pending: pendingAmount,
    pendingDiff,
    completed: completedAmount,
    completedDiff,
    commission: commissionRevenue,
    commissionDiff,
  };
});

// 페이지네이션된 정산 목록
const paginatedSettlements = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredSettlements.value.slice(start, end);
});

// 총 페이지 수
const totalPages = computed(() => {
  return Math.ceil(filteredSettlements.value.length / itemsPerPage);
});

// 페이지 변경 핸들러
const handlePageChange = (page) => {
  currentPage.value = page;
};

// 필터 값 업데이트 핸들러
const handleFilterUpdate = ({ model, value }) => {
  if (model === "selectedStatus") {
    selectedStatus.value = value;
  }
  currentPage.value = 1;
};

// 필터 초기화
const resetFilters = () => {
  searchQuery.value = "";
  selectedStatus.value = "all";
  startDate.value = "";
  endDate.value = "";
  currentPage.value = 1;
};

// 필터 설정
const filters = computed(() => [
  {
    model: "selectedStatus",
    label: "정산 상태",
    value: selectedStatus.value,
    options: statusOptions,
  },
]);

// 매출 추이 차트 데이터
const salesTrendData = computed(() => {
  const labels = [];
  const salesData = [];
  const now = new Date();

  if (selectedPeriod.value === "8weeks") {
    // 최근 8주
    for (let i = 7; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i * 7);
      const month = date.getMonth() + 1;
      const day = date.getDate();
      labels.push(`${month}/${day}`);

      // 해당 주의 매출 계산 (Mock)
      const weekSales = 2000000 + Math.floor(Math.random() * 1000000);
      salesData.push(weekSales);
    }
  } else if (selectedPeriod.value === "6months") {
    // 최근 6개월
    for (let i = 5; i >= 0; i--) {
      const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const month = date.getMonth() + 1;
      labels.push(`${month}월`);

      const settlements = allSettlements.value.filter((s) => {
        const settlementDate = new Date(s.settlementDate);
        return (
          settlementDate.getMonth() + 1 === month &&
          settlementDate.getFullYear() === date.getFullYear()
        );
      });

      const monthSales = settlements.reduce(
        (sum, s) => sum + s.settlementAmount,
        0
      );
      salesData.push(monthSales);
    }
  } else if (selectedPeriod.value === "2years") {
    // 최근 2년 (분기별)
    for (let i = 7; i >= 0; i--) {
      const quarterOffset = i;
      const year = now.getFullYear() - Math.floor(quarterOffset / 4);
      const quarter = 4 - (quarterOffset % 4);
      labels.push(`${year}Q${quarter}`);

      // 해당 분기 매출 계산 (Mock)
      const quarterSales = 15000000 + Math.floor(Math.random() * 5000000);
      salesData.push(quarterSales);
    }
  }

  return {
    labels,
    datasets: [
      {
        label: "매출액",
        data: salesData,
        borderColor: "#3b82f6",
        backgroundColor: "rgba(59, 130, 246, 0.1)",
        tension: 0.4,
        fill: true,
        pointRadius: 4,
        pointHoverRadius: 6,
      },
    ],
  };
});

const salesChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false,
    },
    tooltip: {
      backgroundColor: "rgba(0, 0, 0, 0.8)",
      padding: 12,
      titleColor: "#fff",
      bodyColor: "#fff",
      borderColor: "#3b82f6",
      borderWidth: 1,
      callbacks: {
        label: function (context) {
          return "매출: " + context.parsed.y.toLocaleString() + "원";
        },
      },
    },
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: {
        color: "rgba(0, 0, 0, 0.05)",
      },
      ticks: {
        color: "#6c757d",
        callback: function (value) {
          return (value / 1000000).toFixed(0) + "M";
        },
      },
    },
    x: {
      grid: {
        display: false,
      },
      ticks: {
        color: "#6c757d",
      },
    },
  },
};

// 카테고리별 매출 비율 파이 차트 데이터
const categoryRevenueData = computed(() => {
  // Mock 데이터
  const prepaidCommission = 12500000; // 선결제 중개 수수료
  const matchingCommission = 8200000; // 매칭 중개 수수료
  const penaltyFee = 3800000; // 위약금

  return {
    labels: ["선결제 중개 수수료", "매칭 중개 수수료", "위약금"],
    datasets: [
      {
        data: [prepaidCommission, matchingCommission, penaltyFee],
        backgroundColor: [
          "#fbbf24", // 노란색
          "#60a5fa", // 파란색
          "#f87171", // 빨간색
        ],
        borderColor: "#fff",
        borderWidth: 2,
      },
    ],
  };
});

const pieChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: "bottom",
      labels: {
        color: "#1e3a5f",
        padding: 15,
        font: {
          size: 12,
        },
      },
    },
    tooltip: {
      backgroundColor: "rgba(0, 0, 0, 0.8)",
      padding: 12,
      titleColor: "#fff",
      bodyColor: "#fff",
      callbacks: {
        label: function (context) {
          const label = context.label || "";
          const value = context.parsed;
          const total = context.dataset.data.reduce((a, b) => a + b, 0);
          const percentage = ((value / total) * 100).toFixed(1);
          return (
            label + ": " + value.toLocaleString() + "원 (" + percentage + "%)"
          );
        },
      },
    },
  },
};

// 기간 선택 변경 핸들러
const handlePeriodChange = (period) => {
  selectedPeriod.value = period;
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="finance" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-6">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">재무 관리</h2>
            <p class="text-sm text-[#6c757d]">
              총 {{ filteredSettlements.length.toLocaleString() }}건
            </p>
          </div>

          <!-- 통계 카드 -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <!-- 이번 달 총매출 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-blue-50 rounded-lg">
                  <DollarSign class="w-6 h-6 text-blue-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">이번 달 총매출</p>
              <p class="text-2xl font-bold mb-2">
                {{ formatNumber(stats.totalSales) }}원
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.totalDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ formatNumber(Math.abs(stats.totalDiff)) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 정산 예정액 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-yellow-50 rounded-lg">
                  <TrendingUp class="w-6 h-6 text-yellow-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">정산 예정액</p>
              <p class="text-2xl font-bold mb-2">
                {{ formatNumber(stats.pending) }}원
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.pendingDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ formatNumber(Math.abs(stats.pendingDiff)) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 정산 완료액 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-green-50 rounded-lg">
                  <CheckCircle class="w-6 h-6 text-green-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">정산 완료</p>
              <p class="text-2xl font-bold mb-2">
                {{ formatNumber(stats.completed) }}원
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.completedDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ formatNumber(Math.abs(stats.completedDiff)) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 수수료 수익 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-purple-50 rounded-lg">
                  <Percent class="w-6 h-6 text-purple-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">수수료 수익</p>
              <p class="text-2xl font-bold mb-2">
                {{ formatNumber(stats.commission) }}원
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    stats.commissionDiff >= 0 ? ArrowUpRight : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ formatNumber(Math.abs(stats.commissionDiff)) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>
          </div>

          <!-- 차트 영역 -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- 매출 추이 차트 -->
            <div
              class="lg:col-span-2 bg-white p-6 rounded-xl border border-[#e9ecef]"
            >
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-lg font-semibold text-[#1e3a5f]">매출 추이</h3>
                <div class="flex gap-2">
                  <button
                    @click="handlePeriodChange('8weeks')"
                    :class="[
                      'px-3 py-1 rounded-lg text-sm font-medium transition-colors',
                      selectedPeriod === '8weeks'
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
                    ]"
                  >
                    최근 8주
                  </button>
                  <button
                    @click="handlePeriodChange('6months')"
                    :class="[
                      'px-3 py-1 rounded-lg text-sm font-medium transition-colors',
                      selectedPeriod === '6months'
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
                    ]"
                  >
                    6개월
                  </button>
                  <button
                    @click="handlePeriodChange('2years')"
                    :class="[
                      'px-3 py-1 rounded-lg text-sm font-medium transition-colors',
                      selectedPeriod === '2years'
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-100 text-gray-600 hover:bg-gray-200',
                    ]"
                  >
                    2년
                  </button>
                </div>
              </div>
              <div class="h-80">
                <Line :data="salesTrendData" :options="salesChartOptions" />
              </div>
            </div>

            <!-- 카테고리별 매출 비율 파이 차트 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <h3 class="text-lg font-semibold text-[#1e3a5f] mb-4">
                카테고리별 매출 비율
              </h3>
              <div class="h-80">
                <Pie :data="categoryRevenueData" :options="pieChartOptions" />
              </div>
            </div>
          </div>

          <!-- 필터 및 검색 -->
          <AdminSearchFilter
            v-model:search-query="searchQuery"
            v-model:start-date="startDate"
            v-model:end-date="endDate"
            :filters="filters"
            :show-date-filter="true"
            search-label="검색"
            search-placeholder="식당명으로 검색"
            start-date-label="정산일 (시작)"
            end-date-label="정산일 (종료)"
            @update:filter-value="handleFilterUpdate"
            @reset-filters="resetFilters"
          />

          <!-- 식당별 정산 내역 테이블 -->
          <div
            class="bg-white rounded-xl border border-[#e9ecef] overflow-hidden"
          >
            <div class="px-6 py-4 border-b border-[#e9ecef]">
              <h3 class="text-lg font-semibold text-[#1e3a5f]">
                식당별 정산 내역
              </h3>
            </div>
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      식당명
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      정산 금액
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      정산일
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      정산 상태
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="settlement in paginatedSettlements"
                    :key="settlement.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1e3a5f]"
                    >
                      {{ settlement.restaurantName }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057] font-semibold"
                    >
                      {{ formatNumber(settlement.settlementAmount) }}원
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ settlement.settlementDate }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getStatusBadgeColor(settlement.status),
                        ]"
                      >
                        {{ getStatusLabel(settlement.status) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- 데이터 없을 때 -->
              <div
                v-if="paginatedSettlements.length === 0"
                class="text-center py-12 text-[#6c757d]"
              >
                <p class="text-lg">검색 결과가 없습니다.</p>
                <p class="text-sm mt-2">다른 조건으로 검색해보세요.</p>
              </div>
            </div>

            <!-- 페이지네이션 -->
            <div
              v-if="totalPages > 1"
              class="px-6 py-4 border-t border-[#e9ecef] flex justify-center"
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
/* 스크롤바 스타일링 */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #ff6b4a;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #e55a39;
}
</style>
