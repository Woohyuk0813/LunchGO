<script setup>
import { ref, computed } from "vue";
import { Line, Bar } from "vue-chartjs";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";
import {
  Calendar,
  Store,
  Users,
  TrendingUp,
  ArrowUpRight,
  ArrowDownRight,
  Minus,
} from "lucide-vue-next";

import AdminSidebar from "@/components/ui/AdminSideBar.vue";
import AdminHeader from "@/components/ui/AdminHeader.vue";

// Chart.js 등록
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

// --- 1. 통계 데이터 (전월 대비 증감률 등) ---
const stats = ref({
  totalReservations: { value: 1247, change: 15.3, diff: 166 },
  totalRestaurants: { value: 149, change: 8.2, diff: 11 },
  totalMembers: { value: 5832, newThisMonth: 342, change: 12.7 },
  monthlyRevenue: { value: 24500000, change: -3.4, diff: -860000 },
});

const getChangeIcon = (change) => {
  if (change > 0) return ArrowUpRight;
  if (change < 0) return ArrowDownRight;
  return Minus;
};

const formatNumber = (num) => num.toLocaleString("ko-KR");

// --- 2. 차트 라벨 생성 유틸리티 ---
// 주간 라벨 (8주)
const generateWeekLabels = () => {
  const labels = [];
  const today = new Date();
  for (let i = 7; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(today.getDate() - i * 7);
    labels.push(`${d.getMonth() + 1}월 ${d.getDate()}일`);
  }
  return labels;
};

// 월간 라벨 (6개월, 2년)
const generateMonthLabels = (count) => {
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

// --- 3. 차트 데이터 관리 (Mock Data) ---

// 선택 옵션 정의
const periodOptions = [
  { value: "8weeks", label: "최근 8주" },
  { value: "6months", label: "최근 6개월" },
  { value: "2years", label: "최근 2년" },
];

// 각 차트별 선택된 기간 상태
const signupPeriod = ref("8weeks");
const revenuePeriod = ref("8weeks");
const reservationPeriod = ref("8weeks");

// 데이터 맵 (기간별 데이터 정의)
const chartDataMap = {
  signups: {
    "8weeks": {
      labels: generateWeekLabels(),
      data: [65, 78, 85, 92, 105, 98, 112, 125],
    },
    "6months": {
      labels: generateMonthLabels(6),
      data: [320, 350, 410, 380, 450, 490],
    },
    "2years": {
      labels: generateMonthLabels(24),
      data: Array.from(
        { length: 24 },
        (_, i) => 200 + i * 15 + Math.random() * 50
      ),
    },
  },
  revenue: {
    "8weeks": {
      labels: generateWeekLabels(),
      data: [520, 580, 610, 590, 640, 615, 670, 695],
    },
    "6months": {
      labels: generateMonthLabels(6),
      data: [2200, 2400, 2350, 2600, 2800, 3100],
    },
    "2years": {
      labels: generateMonthLabels(24),
      data: Array.from(
        { length: 24 },
        (_, i) => 1500 + i * 80 + Math.random() * 200
      ),
    },
  },
  reservations: {
    "8weeks": {
      labels: generateWeekLabels(),
      data: [145, 168, 172, 185, 195, 188, 202, 215],
    },
    "6months": {
      labels: generateMonthLabels(6),
      data: [600, 650, 700, 680, 750, 820],
    },
    "2years": {
      labels: generateMonthLabels(24),
      data: Array.from(
        { length: 24 },
        (_, i) => 400 + i * 20 + Math.random() * 60
      ),
    },
  },
};

// Computed Properties: 선택된 기간에 따른 차트 데이터 반환

// 1) 가입자 추세
const signupChartData = computed(() => {
  const current = chartDataMap.signups[signupPeriod.value];
  return {
    labels: current.labels,
    datasets: [
      {
        label: "신규 가입자",
        data: current.data,
        borderColor: "#4A90E2",
        backgroundColor: "rgba(74, 144, 226, 0.1)",
        fill: true,
        tension: 0.4,
      },
    ],
  };
});

// 2) 매출 추세
const revenueChartData = computed(() => {
  const current = chartDataMap.revenue[revenuePeriod.value];
  return {
    labels: current.labels,
    datasets: [
      {
        label: "매출 (만원)",
        data: current.data,
        borderColor: "#FF6B4A",
        backgroundColor: "rgba(255, 107, 74, 0.1)",
        fill: true,
        tension: 0.4,
      },
    ],
  };
});

// 3) 예약 추세
const reservationChartData = computed(() => {
  const current = chartDataMap.reservations[reservationPeriod.value];
  return {
    labels: current.labels,
    datasets: [
      {
        label: "예약 건수",
        data: current.data,
        borderColor: "#9333EA",
        backgroundColor: "rgba(147, 51, 234, 0.1)",
        fill: true,
        tension: 0.4,
      },
    ],
  };
});

// 식당별 매출 데이터 (Top 8 - 기존 유지)
const restaurantSales = ref({
  labels: [
    "한신포차",
    "본죽&비빔밥",
    "스시로",
    "아웃백 스테이크하우스",
    "청년다방",
    "곱창이야기",
    "교촌치킨",
    "도쿄스테이크",
  ],
  datasets: [
    {
      label: "매출 (만원)",
      data: [350, 320, 280, 265, 240, 225, 210, 195],
      backgroundColor: [
        "rgba(255, 107, 74, 0.8)",
        "rgba(74, 144, 226, 0.8)",
        "rgba(147, 51, 234, 0.8)",
        "rgba(16, 185, 129, 0.8)",
        "rgba(245, 158, 11, 0.8)",
        "rgba(239, 68, 68, 0.8)",
        "rgba(99, 102, 241, 0.8)",
        "rgba(236, 72, 153, 0.8)",
      ],
    },
  ],
});

// 차트 옵션
const lineChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: true, position: "top" },
    tooltip: { mode: "index", intersect: false },
  },
  scales: {
    y: { beginAtZero: true },
    x: { grid: { display: false } },
  },
};

const barChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: { label: (context) => context.parsed.y + "만원" },
    },
  },
  scales: { y: { beginAtZero: true } },
};

// 최근 활동 로그 (기존 유지)
const recentActivities = ref([
  {
    id: 1,
    type: "partner",
    title: '새로운 식당 "판교 소반"이 가맹점 등록을 요청했습니다.',
    time: "5분 전",
    badge: "승인 대기",
    badgeColor: "bg-yellow-100 text-yellow-800",
  },
  {
    id: 2,
    type: "payment",
    title: "강남 본점의 12월 정산이 완료되었습니다 (3,500,000원)",
    time: "15분 전",
    badge: "정산 완료",
    badgeColor: "bg-green-100 text-green-800",
  },
  {
    id: 3,
    type: "member",
    title: "김철수 님이 회원가입을 완료했습니다.",
    time: "32분 전",
    badge: "신규 회원",
    badgeColor: "bg-blue-100 text-blue-800",
  },
  {
    id: 4,
    type: "reservation",
    title: '이영희 님이 "서초 지점"에 점심 예약을 완료했습니다 (52,000원)',
    time: "1시간 전",
    badge: "예약 완료",
    badgeColor: "bg-purple-100 text-purple-800",
  },
  {
    id: 5,
    type: "review",
    title: '"홍대 본점"에 새로운 리뷰가 등록되었습니다 (★★★★★)',
    time: "2시간 전",
    badge: "신규 리뷰",
    badgeColor: "bg-pink-100 text-pink-800",
  },
]);
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="dashboard" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-8">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">대시보드</h2>
            <p class="text-sm text-[#6c757d]">
              {{ new Date().toLocaleDateString("ko-KR") }} 기준
            </p>
          </div>

          <!-- Stats Cards (기존 코드 유지) -->
          <div class="grid grid-cols-4 gap-6">
            <div
              class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 hover:shadow-xl transition-all hover:-translate-y-1"
            >
              <div class="flex items-start justify-between mb-4">
                <div class="p-3 bg-purple-50 rounded-lg">
                  <Calendar class="w-6 h-6 text-purple-600" />
                </div>
                <div class="flex items-center gap-1 text-sm text-purple-600">
                  <component
                    :is="getChangeIcon(stats.totalReservations.change)"
                    class="w-4 h-4"
                  />
                  <span>{{ Math.abs(stats.totalReservations.change) }}%</span>
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">총 예약 건수</p>
              <p class="text-3xl font-bold">
                {{ formatNumber(stats.totalReservations.value)
                }}<span class="text-lg ml-1">건</span>
              </p>
              <p class="text-xs mt-2">
                전월 대비 {{ stats.totalReservations.diff > 0 ? "+" : ""
                }}{{ formatNumber(stats.totalReservations.diff) }}건
              </p>
            </div>

            <div
              class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 hover:shadow-xl transition-all hover:-translate-y-1"
            >
              <div class="flex items-start justify-between mb-4">
                <div class="p-3 bg-blue-50 rounded-lg">
                  <Store class="w-6 h-6 text-blue-600" />
                </div>
                <div class="flex items-center gap-1 text-sm text-blue-600">
                  <component
                    :is="getChangeIcon(stats.totalRestaurants.change)"
                    class="w-4 h-4"
                  />
                  <span>{{ Math.abs(stats.totalRestaurants.change) }}%</span>
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">총 등록 식당</p>
              <p class="text-3xl font-bold">
                {{ formatNumber(stats.totalRestaurants.value)
                }}<span class="text-lg ml-1">개</span>
              </p>
              <p class="text-xs mt-2">
                전월 대비 {{ stats.totalRestaurants.diff > 0 ? "+" : ""
                }}{{ formatNumber(stats.totalRestaurants.diff) }}개
              </p>
            </div>

            <div
              class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 hover:shadow-xl transition-all hover:-translate-y-1"
            >
              <div class="flex items-start justify-between mb-4">
                <div class="p-3 bg-green-50 rounded-lg">
                  <Users class="w-6 h-6 text-green-600" />
                </div>
                <div class="flex items-center gap-1 text-sm text-green-600">
                  <component
                    :is="getChangeIcon(stats.totalMembers.change)"
                    class="w-4 h-4"
                  />
                  <span>{{ Math.abs(stats.totalMembers.change) }}%</span>
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">총 회원 수</p>
              <p class="text-3xl font-bold">
                {{ formatNumber(stats.totalMembers.value)
                }}<span class="text-lg ml-1">명</span>
              </p>
              <p class="text-xs mt-2">
                이번 달 +{{ formatNumber(stats.totalMembers.newThisMonth) }}명
              </p>
            </div>

            <div
              class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 hover:shadow-xl transition-all hover:-translate-y-1"
            >
              <div class="flex items-start justify-between mb-4">
                <div class="p-3 bg-orange-50 rounded-lg">
                  <TrendingUp class="w-6 h-6 text-orange-600" />
                </div>
                <div class="flex items-center gap-1 text-sm text-orange-600">
                  <component
                    :is="getChangeIcon(stats.monthlyRevenue.change)"
                    class="w-4 h-4"
                  />
                  <span>{{ Math.abs(stats.monthlyRevenue.change) }}%</span>
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">이번 달 매출</p>
              <p class="text-3xl font-bold">
                {{ formatNumber(stats.monthlyRevenue.value)
                }}<span class="text-lg ml-1">원</span>
              </p>
              <p class="text-xs mt-2">
                전월 대비 {{ stats.monthlyRevenue.diff > 0 ? "+" : ""
                }}{{ formatNumber(stats.monthlyRevenue.diff) }}원
              </p>
            </div>
          </div>

          <!-- Charts Section - 3개 라인 차트 (필터 추가됨) -->
          <div class="grid grid-cols-3 gap-6">
            <!-- 1. 가입자 추세 -->
            <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h3 class="text-xl font-bold text-[#1e3a5f]">가입자 추세</h3>
                  <p class="text-sm text-[#6c757d] mt-1">
                    {{
                      periodOptions.find((o) => o.value === signupPeriod).label
                    }}
                  </p>
                </div>
                <!-- 개별 필터 -->
                <select
                  v-model="signupPeriod"
                  class="text-sm border border-gray-300 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                >
                  <option
                    v-for="opt in periodOptions"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <div class="h-64">
                <Line :data="signupChartData" :options="lineChartOptions" />
              </div>
            </div>

            <!-- 2. 매출 추세 -->
            <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h3 class="text-xl font-bold text-[#1e3a5f]">매출 추세</h3>
                  <p class="text-sm text-[#6c757d] mt-1">
                    {{
                      periodOptions.find((o) => o.value === revenuePeriod).label
                    }}
                  </p>
                </div>
                <!-- 개별 필터 -->
                <select
                  v-model="revenuePeriod"
                  class="text-sm border border-gray-300 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                >
                  <option
                    v-for="opt in periodOptions"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <div class="h-64">
                <Line :data="revenueChartData" :options="lineChartOptions" />
              </div>
            </div>

            <!-- 3. 예약 추세 -->
            <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h3 class="text-xl font-bold text-[#1e3a5f]">예약 추세</h3>
                  <p class="text-sm text-[#6c757d] mt-1">
                    {{
                      periodOptions.find((o) => o.value === reservationPeriod)
                        .label
                    }}
                  </p>
                </div>
                <!-- 개별 필터 -->
                <select
                  v-model="reservationPeriod"
                  class="text-sm border border-gray-300 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                >
                  <option
                    v-for="opt in periodOptions"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </option>
                </select>
              </div>
              <div class="h-64">
                <Line
                  :data="reservationChartData"
                  :options="lineChartOptions"
                />
              </div>
            </div>
          </div>

          <!-- 식당별 매출 바 차트 -->
          <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
            <div class="mb-6">
              <h3 class="text-xl font-bold text-[#1e3a5f]">
                식당별 매출 Top 8
              </h3>
              <p class="text-sm text-[#6c757d] mt-1">이번 달 기준</p>
            </div>
            <div class="h-80">
              <Bar :data="restaurantSales" :options="barChartOptions" />
            </div>
          </div>

          <!-- 최근 활동 -->
          <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">최근 활동</h3>
            <div class="space-y-3">
              <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="flex items-start justify-between p-4 rounded-lg hover:bg-[#f8f9fa] transition-colors"
              >
                <div class="flex-1">
                  <div class="flex items-center gap-3 mb-1">
                    <span
                      :class="[
                        'px-2.5 py-1 rounded-full text-xs font-medium',
                        activity.badgeColor,
                      ]"
                    >
                      {{ activity.badge }}
                    </span>
                    <span class="text-xs text-[#6c757d]">{{
                      activity.time
                    }}</span>
                  </div>
                  <p class="text-[#1e3a5f] leading-relaxed">
                    {{ activity.title }}
                  </p>
                </div>
              </div>
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
