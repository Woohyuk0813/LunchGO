<script setup>
import { ref, computed, onMounted } from 'vue';
import AdminSidebar from '@/components/ui/AdminSideBar.vue';
import AdminHeader from '@/components/ui/AdminHeader.vue';
import Pagination from '@/components/ui/Pagination.vue';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';

// 검색 및 필터 상태
const searchQuery = ref('');
const selectedStatus = ref('all');
const selectedDate = ref('');

// 페이지네이션 설정
const currentPage = ref(1);
const itemsPerPage = 10;

// 상태 옵션
const statusOptions = [
  { value: 'all', label: '전체' },
  { value: 'pending', label: '승인 대기' },
  { value: 'approval', label: '승인 완료' },
  { value: 'rejected', label: '거절됨' },
];

// 배지 색상 함수
const getBadgeColor = (status) => {
  const colors = {
    pending: 'bg-yellow-100 text-yellow-800',
    approval: 'bg-green-100 text-green-800',
    rejected: 'bg-red-100 text-red-800', 
  };
  return colors[status] || 'bg-gray-100 text-gray-800';
};

// 상태 라벨 함수
const getStatusLabel = (status) => {
  const labels = {
    pending: '승인 대기',
    approval: '승인 완료',
    rejected: '거절됨',
  };
  return labels[status] || status;
};

const accountStore = useAccountStore();

const getStoredMember = () => {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('member');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());
const adminDisplayName = computed(() => {
  const data = member.value || {};
  return data.name || data.loginId || data.email || 'Unknown';
});

const allOwners = ref([]);

const loadOwners = async () => {
  try {
    const response = await httpRequest.get('/api/admin/list/owner');
    if (!Array.isArray(response.data)) return alert("정보 불러오는 중 오류가 발생했습니다.");

    allOwners.value = response.data.map((row, index) => {
      const rawStatus = row.status || '';
      const normalized = typeof rawStatus === 'string' ? rawStatus.toUpperCase() : rawStatus;
      const statusMap = {
        PENDING: 'pending',
        ACTIVE: 'approval',
        WITHDRAWAL: 'rejected',
      };

      return {
        id: index + 1,
        name: row.name ?? '',
        businessNumber: row.businessNum ?? '',
        openingDate: row.startAt ?? '',
        username: row.loginId ?? '',
        status: statusMap[normalized],
      };
    });
  } catch (error) {
    console.error('Owner list load failed:', error);
  }
};

onMounted(() => {
  loadOwners();
});

// 통계 데이터 계산 (rejected 추가)
const stats = computed(() => {
  return {
    total: allOwners.value.length,
    pending: allOwners.value.filter((o) => o.status === 'pending').length,
    approval: allOwners.value.filter((o) => o.status === 'approval').length,
    rejected: allOwners.value.filter((o) => o.status === 'rejected').length, // 거절된 건수 추가
  };
});

// 필터링 및 정렬 로직
const filteredOwners = computed(() => {
  let filtered = allOwners.value;

  // 1. 검색어
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (o) =>
        o.name.toLowerCase().includes(query) ||
        o.businessNumber.includes(query) ||
        o.username.toLowerCase().includes(query)
    );
  }

  // 2. 상태
  if (selectedStatus.value !== 'all') {
    filtered = filtered.filter((o) => o.status === selectedStatus.value);
  }

  // 3. 날짜
  if (selectedDate.value) {
    filtered = filtered.filter((o) => o.openingDate === selectedDate.value);
  }

  // 4. 정렬 (pending 우선)
  filtered.sort((a, b) => {
    if (a.status === 'pending' && b.status !== 'pending') return -1;
    if (a.status !== 'pending' && b.status === 'pending') return 1;
    return b.id - a.id;
  });

  return filtered;
});

// 페이지네이션
const paginatedOwners = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredOwners.value.slice(start, end);
});

const totalPages = computed(() => {
  return Math.ceil(filteredOwners.value.length / itemsPerPage);
});

// 핸들러
const handlePageChange = (page) => {
  currentPage.value = page;
};

const resetFilters = () => {
  searchQuery.value = '';
  selectedStatus.value = 'all';
  selectedDate.value = '';
  currentPage.value = 1;
};

// 승인 처리
const approveOwner = async (id) => {
  if (confirm('해당 사업자를 승인하시겠습니까?')) {
    const index = allOwners.value.findIndex(o => o.id === id);
    if (index !== -1) {
      allOwners.value[index].status = 'approval';
    }
  }
};

// 거절 처리
const rejectOwner = async (id) => {
  if (confirm('해당 사업자 승인을 거절하시겠습니까?')) {
    const index = allOwners.value.findIndex(o => o.id === id);
    if (index !== -1) {
      allOwners.value[index].status = 'rejected'; 
    }
  }
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="owners" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-6">
          
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">사업자 승인 관리</h2>
          </div>

          <div class="grid grid-cols-4 gap-6">
            <div class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 text-center hover:shadow-lg transition-shadow">
              <p class="text-sm text-[#6c757d] mb-2">전체 신청</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.total }}<span class="text-lg ml-1">명</span>
              </p>
            </div>
            <div class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 text-center hover:shadow-lg transition-shadow">
              <p class="text-sm text-[#6c757d] mb-2">승인 대기</p>
              <p class="text-4xl font-bold text-yellow-600">
                {{ stats.pending }}<span class="text-lg ml-1">명</span>
              </p>
            </div>
            <div class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 text-center hover:shadow-lg transition-shadow">
              <p class="text-sm text-[#6c757d] mb-2">승인 완료</p>
              <p class="text-4xl font-bold text-green-600">
                {{ stats.approval }}<span class="text-lg ml-1">명</span>
              </p>
            </div>
            <div class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 text-center hover:shadow-lg transition-shadow">
              <p class="text-sm text-[#6c757d] mb-2">거절됨</p>
              <p class="text-4xl font-bold text-red-600">
                {{ stats.rejected }}<span class="text-lg ml-1">명</span>
              </p>
            </div>
          </div>

          <div class="bg-white p-4 rounded-xl border border-[#e9ecef] shadow-sm">
            <div class="flex items-center gap-4">
              <div class="flex-1 relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                </div>
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="이름, 아이디, 사업자번호 검색"
                  class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#1e3a5f] focus:border-transparent outline-none transition-all"
                />
              </div>

              <div class="w-40">
                <select
                  v-model="selectedStatus"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#1e3a5f] focus:border-transparent outline-none bg-white cursor-pointer"
                >
                  <option v-for="option in statusOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </div>

              <div class="w-48 relative">
                <input
                  v-model="selectedDate"
                  type="date"
                  class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#1e3a5f] focus:border-transparent outline-none text-gray-600 cursor-pointer"
                />
                <span 
                  v-if="!selectedDate" 
                  class="absolute left-4 top-2.5 text-gray-400 text-sm pointer-events-none bg-white pr-2"
                >
                  개업일자 선택
                </span>
              </div>

              <button
                @click="resetFilters"
                class="px-4 py-2 text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors flex items-center gap-2 whitespace-nowrap"
              >
                <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                </svg>
                초기화
              </button>
            </div>
          </div>

          <div class="bg-white rounded-xl border border-[#e9ecef] overflow-hidden">
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider">No.</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider">이름</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider">사업자등록번호</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider">개업일자</th>
                    <th class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider">아이디</th>
                    <th class="px-6 py-4 text-center text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider">상태</th>
                    <th class="px-6 py-4 text-center text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider">관리</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="(owner, index) in paginatedOwners"
                    :key="owner.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]">{{ owner.id }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1e3a5f]">{{ owner.name }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]">{{ owner.businessNumber }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]">{{ owner.openingDate }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]">{{ owner.username }}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-center">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getBadgeColor(owner.status),
                        ]"
                      >
                        {{ getStatusLabel(owner.status) }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-center">
                      <div v-if="owner.status === 'pending'" class="flex items-center justify-center gap-2">
                        <button
                          @click="approveOwner(owner.id)"
                          class="px-3 py-1.5 bg-[#1e3a5f] text-white text-xs rounded hover:bg-[#162c46] transition-colors"
                        >
                          승인
                        </button>
                        <button
                          @click="rejectOwner(owner.id)"
                          class="px-3 py-1.5 bg-red-500 text-white text-xs rounded hover:bg-red-600 transition-colors"
                        >
                          거절
                        </button>
                      </div>
                      <span v-else class="text-xs text-gray-400">-</span>
                    </td>
                  </tr>
                </tbody>
              </table>

              <div v-if="paginatedOwners.length === 0" class="text-center py-12 text-[#6c757d]">
                <p class="text-lg">데이터가 없습니다.</p>
                <p class="text-sm mt-2">검색 조건을 변경해보세요.</p>
              </div>
            </div>

            <div v-if="totalPages > 1" class="px-6 py-4 border-t border-[#e9ecef] flex justify-center">
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
