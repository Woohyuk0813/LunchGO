<script setup>
import { ref, computed } from "vue";
import AdminSidebar from "@/components/ui/AdminSideBar.vue";
import AdminHeader from "@/components/ui/AdminHeader.vue";
import Pagination from "@/components/ui/Pagination.vue";
import AdminSearchFilter from "@/components/ui/AdminSearchFilter.vue";
import {
  Users,
  UserPlus,
  UserCheck,
  UserX,
  AlertTriangle,
  ArrowUpRight,
  ArrowDownRight,
} from "lucide-vue-next";
import { Line } from "vue-chartjs";
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

// 검색 및 필터
const searchQuery = ref("");
const selectedStatus = ref("all");
const startDate = ref("");
const endDate = ref("");

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 10;

// 회원 상태 옵션
const statusOptions = [
  { value: "all", label: "전체" },
  { value: "active", label: "활성" },
  { value: "dormant", label: "휴면" },
  { value: "withdrawn", label: "탈퇴" },
  { value: "blacklist", label: "블랙리스트" },
];

// 회원 상태별 배지 색상
const getStatusBadgeColor = (status) => {
  const colors = {
    active: "bg-green-100 text-green-800",
    dormant: "bg-yellow-100 text-yellow-800",
    withdrawn: "bg-gray-100 text-gray-800",
    blacklist: "bg-red-100 text-red-800",
  };
  return colors[status] || "bg-gray-100 text-gray-800";
};

// 회원 상태 라벨
const getStatusLabel = (status) => {
  const labels = {
    active: "활성",
    dormant: "휴면",
    withdrawn: "탈퇴",
    blacklist: "블랙리스트",
  };
  return labels[status] || status;
};

// 전화번호 마스킹 함수
const maskPhone = (phone) => {
  if (!phone) return "";
  return phone.replace(/(\d{3})-(\d{3,4})-(\d{4})/, "$1-***-****");
};

// 회원명 마스킹 함수
const maskName = (name) => {
  if (!name || name.length === 0) return "";
  if (name.length === 1) return name;
  if (name.length === 2) return name[0] + "*";
  return name[0] + "*".repeat(name.length - 2) + name[name.length - 1];
};

// Mock 데이터 (총 62개)
const allMembers = ref([
  {
    id: "M001",
    name: "김민수",
    email: "minsu.kim@gmail.com",
    phone: "010-1234-5678",
    joinDate: "2024-11-15",
    reservationCount: 12,
    lastAccess: "2024-12-17",
    status: "active",
  },
  {
    id: "M002",
    name: "이영희",
    email: "younghee.lee@naver.com",
    phone: "010-2345-6789",
    joinDate: "2024-10-20",
    reservationCount: 8,
    lastAccess: "2024-12-16",
    status: "active",
  },
  {
    id: "M003",
    name: "박철수",
    email: "chulsoo.park@daum.net",
    phone: "010-3456-7890",
    joinDate: "2024-12-01",
    reservationCount: 3,
    lastAccess: "2024-12-15",
    status: "active",
  },
  {
    id: "M004",
    name: "정다은",
    email: "daeun.jung@gmail.com",
    phone: "010-4567-8901",
    joinDate: "2024-09-10",
    reservationCount: 15,
    lastAccess: "2024-08-20",
    status: "dormant",
  },
  {
    id: "M005",
    name: "최지훈",
    email: "jihoon.choi@naver.com",
    phone: "010-5678-9012",
    joinDate: "2024-08-05",
    reservationCount: 20,
    lastAccess: "2024-12-10",
    status: "active",
  },
  {
    id: "M006",
    name: "강서연",
    email: "seoyeon.kang@gmail.com",
    phone: "010-6789-0123",
    joinDate: "2024-12-10",
    reservationCount: 2,
    lastAccess: "2024-12-17",
    status: "active",
  },
  {
    id: "M007",
    name: "윤준호",
    email: "junho.yoon@daum.net",
    phone: "010-7890-1234",
    joinDate: "2024-07-15",
    reservationCount: 5,
    lastAccess: "2024-12-14",
    status: "active",
  },
  {
    id: "M008",
    name: "임수진",
    email: "sujin.lim@gmail.com",
    phone: "010-8901-2345",
    joinDate: "2024-11-25",
    reservationCount: 7,
    lastAccess: "2024-12-16",
    status: "active",
  },
  {
    id: "M009",
    name: "송민재",
    email: "minjae.song@naver.com",
    phone: "010-9012-3456",
    joinDate: "2024-06-20",
    reservationCount: 0,
    lastAccess: "2024-11-30",
    status: "withdrawn",
  },
  {
    id: "M010",
    name: "한지우",
    email: "jiwoo.han@gmail.com",
    phone: "010-0123-4567",
    joinDate: "2024-12-05",
    reservationCount: 4,
    lastAccess: "2024-12-17",
    status: "active",
  },
  // 추가 데이터 생성 (M011 ~ M062)
  ...Array.from({ length: 52 }, (_, i) => {
    const idx = i + 11;
    // 최근 6개월 데이터가 골고루 분포되도록 수정
    const monthOffset = Math.floor(i / 9); // 0~5 사이 값
    const now = new Date();
    const targetMonth = now.getMonth() - monthOffset; // 현재 월부터 5개월 전까지
    const targetYear = now.getFullYear() + Math.floor(targetMonth / 12);
    const finalMonth = (((targetMonth % 12) + 12) % 12) + 1;
    const day = 1 + (i % 28);

    const statusList = [
      "active",
      "active",
      "active",
      "active",
      "dormant",
      "withdrawn",
      "blacklist",
    ];
    const status = statusList[i % statusList.length];

    return {
      id: `M${String(idx).padStart(3, "0")}`,
      name:
        ["김", "이", "박", "정", "최", "강", "윤", "임"][i % 8] +
        ["민수", "영희", "철수", "다은", "지훈", "서연"][i % 6],
      email: `user${idx}@${["gmail.com", "naver.com", "daum.net"][i % 3]}`,
      phone: `010-${String(1000 + idx).slice(-4)}-${String(5000 + idx).slice(
        -4
      )}`,
      joinDate: `${targetYear}-${String(finalMonth).padStart(2, "0")}-${String(
        day
      ).padStart(2, "0")}`,
      reservationCount: Math.floor(Math.random() * 25),
      lastAccess: `2024-${String(10 + (i % 3)).padStart(2, "0")}-${String(
        15 + (i % 15)
      ).padStart(2, "0")}`,
      status: status,
    };
  }),
]);

// 필터링된 회원 목록
const filteredMembers = computed(() => {
  let filtered = allMembers.value;

  // 검색어 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (m) =>
        m.name.toLowerCase().includes(query) ||
        m.email.toLowerCase().includes(query) ||
        m.phone.includes(query)
    );
  }

  // 상태 필터
  if (selectedStatus.value !== "all") {
    filtered = filtered.filter((m) => m.status === selectedStatus.value);
  }

  // 날짜 범위 필터
  if (startDate.value || endDate.value) {
    filtered = filtered.filter((m) => {
      const joinDate = new Date(m.joinDate);

      if (startDate.value && endDate.value) {
        const start = new Date(startDate.value);
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return joinDate >= start && joinDate <= end;
      } else if (startDate.value) {
        const start = new Date(startDate.value);
        return joinDate >= start;
      } else if (endDate.value) {
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return joinDate <= end;
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

  const allMembersList = allMembers.value;

  // 이번 달 신규 가입자
  const newMembersThisMonth = allMembersList.filter((m) => {
    const joinDate = new Date(m.joinDate);
    return (
      joinDate.getMonth() + 1 === currentMonth &&
      joinDate.getFullYear() === currentYear
    );
  }).length;

  // 지난 달 신규 가입자
  const newMembersLastMonth = allMembersList.filter((m) => {
    const joinDate = new Date(m.joinDate);
    return (
      joinDate.getMonth() + 1 === lastMonth &&
      joinDate.getFullYear() === lastMonthYear
    );
  }).length;

  // 활성 회원
  const activeMembers = allMembersList.filter(
    (m) => m.status === "active"
  ).length;

  // 휴면 회원
  const dormantMembers = allMembersList.filter(
    (m) => m.status === "dormant"
  ).length;

  // 블랙리스트 회원
  const blacklistMembers = allMembersList.filter(
    (m) => m.status === "blacklist"
  ).length;

  // 전월 대비 증감
  const totalDiff = newMembersThisMonth - newMembersLastMonth;
  const activeDiff = Math.floor(activeMembers * 0.05); // Mock data
  const dormantDiff = Math.floor(dormantMembers * -0.03); // Mock data
  const blacklistDiff = Math.floor(blacklistMembers * 0.1); // Mock data

  return {
    total: allMembersList.length,
    totalDiff,
    newMembers: newMembersThisMonth,
    newMembersDiff: totalDiff,
    active: activeMembers,
    activeDiff,
    dormant: dormantMembers,
    dormantDiff,
    blacklist: blacklistMembers,
    blacklistDiff,
  };
});

// 페이지네이션된 회원 목록
const paginatedMembers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredMembers.value.slice(start, end);
});

// 총 페이지 수
const totalPages = computed(() => {
  return Math.ceil(filteredMembers.value.length / itemsPerPage);
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
    label: "상태",
    value: selectedStatus.value,
    options: statusOptions,
  },
]);

// 월별 가입자 추이 차트 데이터
const monthlyTrendData = computed(() => {
  const months = [];
  const memberCounts = [];
  const now = new Date();

  // 최근 6개월 데이터
  for (let i = 5; i >= 0; i--) {
    const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const month = date.getMonth() + 1;
    const year = date.getFullYear();

    months.push(`${month}월`);

    const count = allMembers.value.filter((m) => {
      const joinDate = new Date(m.joinDate);
      return (
        joinDate.getMonth() + 1 === month && joinDate.getFullYear() === year
      );
    }).length;

    memberCounts.push(count);
  }

  return {
    labels: months,
    datasets: [
      {
        label: "월별 가입자 수",
        data: memberCounts,
        borderColor: "#8b5cf6",
        backgroundColor: "rgba(139, 92, 246, 0.1)",
        tension: 0.4,
        fill: true,
        pointRadius: 4,
        pointHoverRadius: 6,
      },
    ],
  };
});

const chartOptions = {
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
      borderColor: "#8b5cf6",
      borderWidth: 1,
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
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="members" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-6">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">회원 관리</h2>
            <p class="text-sm text-[#6c757d]">
              총 {{ filteredMembers.length.toLocaleString() }}명
            </p>
          </div>

          <!-- 통계 카드 -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <!-- 전체 회원 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-purple-50 rounded-lg">
                  <Users class="w-6 h-6 text-purple-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">전체 회원</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.total.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.totalDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.totalDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 신규 가입 (이번달) -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-blue-50 rounded-lg">
                  <UserPlus class="w-6 h-6 text-blue-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">신규 가입 (이번달)</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.newMembers.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    stats.newMembersDiff >= 0 ? ArrowUpRight : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.newMembersDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 활성 회원 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-green-50 rounded-lg">
                  <UserCheck class="w-6 h-6 text-green-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">활성 회원</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.active.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.activeDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.activeDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 휴면 회원 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-yellow-50 rounded-lg">
                  <UserX class="w-6 h-6 text-yellow-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">휴면 회원</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.dormant.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.dormantDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.dormantDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 블랙리스트 회원 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-red-50 rounded-lg">
                  <AlertTriangle class="w-6 h-6 text-red-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">블랙리스트</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.blacklist.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.blacklistDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.blacklistDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>
          </div>

          <!-- 월별 가입자 추이 차트 -->
          <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
            <h3 class="text-lg font-semibold text-[#1e3a5f] mb-4">
              월별 가입자 추이
            </h3>
            <div class="h-64">
              <Line :data="monthlyTrendData" :options="chartOptions" />
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
            search-placeholder="회원명, 이메일, 휴대폰으로 검색"
            start-date-label="가입일 (시작)"
            end-date-label="가입일 (종료)"
            @update:filter-value="handleFilterUpdate"
            @reset-filters="resetFilters"
          />

          <!-- 회원 목록 테이블 -->
          <div
            class="bg-white rounded-xl border border-[#e9ecef] overflow-hidden"
          >
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      회원명
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      이메일
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      휴대폰
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      가입일
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      예약 횟수
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      마지막 접속
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
                    v-for="member in paginatedMembers"
                    :key="member.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1e3a5f]"
                    >
                      {{ maskName(member.name) }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ member.email }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ maskPhone(member.phone) }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ member.joinDate }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ member.reservationCount }}회
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ member.lastAccess }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getStatusBadgeColor(member.status),
                        ]"
                      >
                        {{ getStatusLabel(member.status) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- 데이터 없을 때 -->
              <div
                v-if="paginatedMembers.length === 0"
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
