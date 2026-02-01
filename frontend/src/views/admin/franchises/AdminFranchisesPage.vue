<script setup>
import { ref, computed } from 'vue';
import AdminSidebar from '@/components/ui/AdminSideBar.vue';
import AdminHeader from '@/components/ui/AdminHeader.vue';
import Pagination from '@/components/ui/Pagination.vue';
import AdminSearchFilter from '@/components/ui/AdminSearchFilter.vue';

// 검색 및 필터
const searchQuery = ref('');
const selectedSettlementStatus = ref('all');
const selectedOperationStatus = ref('all');
const startDate = ref('');
const endDate = ref('');

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 10;

// 정산 상태 옵션
const settlementStatusOptions = [
  { value: 'all', label: '전체' },
  { value: 'scheduled', label: '정산 예정' },
  { value: 'processing', label: '처리중' },
  { value: 'completed', label: '정산 완료' },
  { value: 'failed', label: '정산 실패' },
  { value: 'pending', label: '정산 보류' },
  { value: 'overdue', label: '연체중' },
];

// 운영 상태 옵션
const operationStatusOptions = [
  { value: 'all', label: '전체' },
  { value: 'approval_pending', label: '승인 대기' },
  { value: 'operating', label: '운영 중' },
  { value: 'suspended', label: '임시 중단' },
  { value: 'closed', label: '폐업' },
];

// 정산 상태별 배지 색상
const getSettlementBadgeColor = (status) => {
  const colors = {
    scheduled: 'bg-blue-100 text-blue-800',
    processing: 'bg-yellow-100 text-yellow-800',
    completed: 'bg-green-100 text-green-800',
    failed: 'bg-red-100 text-red-800',
    pending: 'bg-orange-100 text-orange-800',
    overdue: 'bg-red-100 text-red-900',
  };
  return colors[status] || 'bg-gray-100 text-gray-800';
};

// 운영 상태별 배지 색상
const getOperationBadgeColor = (status) => {
  const colors = {
    approval_pending: 'bg-yellow-100 text-yellow-800',
    operating: 'bg-green-100 text-green-800',
    suspended: 'bg-orange-100 text-orange-800',
    closed: 'bg-gray-100 text-gray-800',
  };
  return colors[status] || 'bg-gray-100 text-gray-800';
};

// 정산 상태 라벨
const getSettlementLabel = (status) => {
  const labels = {
    scheduled: '정산 예정',
    processing: '처리중',
    completed: '정산 완료',
    failed: '정산 실패',
    pending: '정산 보류',
    overdue: '연체중',
  };
  return labels[status] || status;
};

// 운영 상태 라벨
const getOperationLabel = (status) => {
  const labels = {
    approval_pending: '승인 대기',
    operating: '운영 중',
    suspended: '임시 중단',
    closed: '폐업',
  };
  return labels[status] || status;
};

// Mock 데이터 (총 52개 - 페이지네이션 테스트용)
const allFranchises = ref([
  {
    id: 'F001',
    restaurantName: '한신포차 강남점',
    ownerName: '김철수',
    category: '한식',
    registrationDate: '2024-01-15',
    region: '서울 강남구',
    settlementStatus: 'completed',
    operationStatus: 'operating',
  },
  {
    id: 'F002',
    restaurantName: '본죽&비빔밥 서초점',
    ownerName: '이영희',
    category: '한식',
    registrationDate: '2024-02-20',
    region: '서울 서초구',
    settlementStatus: 'scheduled',
    operationStatus: 'operating',
  },
  {
    id: 'F003',
    restaurantName: '스시로 판교점',
    ownerName: '박민수',
    category: '일식',
    registrationDate: '2024-03-10',
    region: '경기 성남시',
    settlementStatus: 'processing',
    operationStatus: 'operating',
  },
  {
    id: 'F004',
    restaurantName: '아웃백 스테이크하우스 홍대점',
    ownerName: '정다은',
    category: '양식',
    registrationDate: '2024-01-25',
    region: '서울 마포구',
    settlementStatus: 'completed',
    operationStatus: 'operating',
  },
  {
    id: 'F005',
    restaurantName: '청년다방 신논현점',
    ownerName: '최지훈',
    category: '퓨전',
    registrationDate: '2024-04-05',
    region: '서울 강남구',
    settlementStatus: 'failed',
    operationStatus: 'suspended',
  },
  {
    id: 'F006',
    restaurantName: '곱창이야기 강남점',
    ownerName: '강서연',
    category: '한식',
    registrationDate: '2024-02-14',
    region: '서울 강남구',
    settlementStatus: 'completed',
    operationStatus: 'operating',
  },
  {
    id: 'F007',
    restaurantName: '교촌치킨 역삼점',
    ownerName: '윤준호',
    category: '한식',
    registrationDate: '2024-03-22',
    region: '서울 강남구',
    settlementStatus: 'pending',
    operationStatus: 'operating',
  },
  {
    id: 'F008',
    restaurantName: '도쿄스테이크 압구정점',
    ownerName: '임수진',
    category: '일식',
    registrationDate: '2024-01-30',
    region: '서울 강남구',
    settlementStatus: 'completed',
    operationStatus: 'operating',
  },
  {
    id: 'F009',
    restaurantName: '용궁각 신촌점',
    ownerName: '송민재',
    category: '중식',
    registrationDate: '2024-05-12',
    region: '서울 서대문구',
    settlementStatus: 'overdue',
    operationStatus: 'operating',
  },
  {
    id: 'F010',
    restaurantName: '미스터피자 잠실점',
    ownerName: '한지우',
    category: '양식',
    registrationDate: '2024-02-28',
    region: '서울 송파구',
    settlementStatus: 'scheduled',
    operationStatus: 'operating',
  },
  {
    id: 'F011',
    restaurantName: '스시쿠우 강남점',
    ownerName: '배수현',
    category: '일식',
    registrationDate: '2024-06-01',
    region: '서울 강남구',
    settlementStatus: 'completed',
    operationStatus: 'operating',
  },
  {
    id: 'F012',
    restaurantName: '타코벨 건대점',
    ownerName: '오태양',
    category: '퓨전',
    registrationDate: '2024-04-18',
    region: '서울 광진구',
    settlementStatus: 'processing',
    operationStatus: 'operating',
  },
  {
    id: 'F013',
    restaurantName: '빕스 여의도점',
    ownerName: '서은영',
    category: '양식',
    registrationDate: '2024-03-05',
    region: '서울 영등포구',
    settlementStatus: 'completed',
    operationStatus: 'operating',
  },
  {
    id: 'F014',
    restaurantName: '마포갈매기 홍대점',
    ownerName: '권민호',
    category: '한식',
    registrationDate: '2024-01-20',
    region: '서울 마포구',
    settlementStatus: 'completed',
    operationStatus: 'closed',
  },
  {
    id: 'F015',
    restaurantName: '진진짜장 강남점',
    ownerName: '조혜린',
    category: '중식',
    registrationDate: '2024-07-10',
    region: '서울 강남구',
    settlementStatus: 'scheduled',
    operationStatus: 'operating',
  },
  {
    id: 'F016',
    restaurantName: '스시효 신사점',
    ownerName: '남궁현',
    category: '일식',
    registrationDate: '2024-05-25',
    region: '서울 강남구',
    settlementStatus: 'processing',
    operationStatus: 'operating',
  },
  {
    id: 'F017',
    restaurantName: '대패삼겹 판교점',
    ownerName: '유채원',
    category: '한식',
    registrationDate: '2024-02-10',
    region: '경기 성남시',
    settlementStatus: 'completed',
    operationStatus: 'operating',
  },
  {
    id: 'F018',
    restaurantName: '파스타집 강남점',
    ownerName: '홍석진',
    category: '양식',
    registrationDate: '2024-08-15',
    region: '서울 강남구',
    settlementStatus: 'pending',
    operationStatus: 'approval_pending',
  },
  {
    id: 'F019',
    restaurantName: '초밥왕 서초점',
    ownerName: '안소희',
    category: '일식',
    registrationDate: '2024-04-22',
    region: '서울 서초구',
    settlementStatus: 'completed',
    operationStatus: 'operating',
  },
  {
    id: 'F020',
    restaurantName: '타이퐁 판교점',
    ownerName: '장우진',
    category: '퓨전',
    registrationDate: '2024-06-18',
    region: '경기 성남시',
    settlementStatus: 'scheduled',
    operationStatus: 'operating',
  },
  // 추가 데이터 (21-52)
  ...Array.from({ length: 32 }, (_, i) => ({
    id: `F${(21 + i).toString().padStart(3, '0')}`,
    restaurantName:
      [
        '한신포차',
        '본죽&비빔밥',
        '스시로',
        '아웃백',
        '청년다방',
        '곱창이야기',
        '교촌치킨',
        '도쿄스테이크',
      ][i % 8] +
      ' ' +
      [
        '강남점',
        '서초점',
        '판교점',
        '홍대점',
        '잠실점',
        '신촌점',
        '건대점',
        '여의도점',
      ][Math.floor(i / 4) % 8],
    ownerName:
      ['김', '이', '박', '정', '최', '강', '윤', '임'][i % 8] +
      ['철수', '영희', '민수', '지훈'][i % 4],
    category: ['한식', '중식', '일식', '양식', '퓨전'][i % 5],
    registrationDate: `2024-${String((i % 12) + 1).padStart(2, '0')}-${String(
      (i % 28) + 1
    ).padStart(2, '0')}`,
    region: [
      '서울 강남구',
      '서울 서초구',
      '경기 성남시',
      '서울 마포구',
      '서울 송파구',
    ][i % 5],
    settlementStatus: [
      'scheduled',
      'processing',
      'completed',
      'failed',
      'pending',
      'overdue',
    ][i % 6],
    operationStatus: ['approval_pending', 'operating', 'suspended', 'closed'][
      i % 4
    ],
  })),
]);

// 통계 데이터
const stats = computed(() => {
  return {
    total: allFranchises.value.length,
    approvalPending: allFranchises.value.filter(
      (f) => f.operationStatus === 'approval_pending'
    ).length,
    operating: allFranchises.value.filter(
      (f) => f.operationStatus === 'operating'
    ).length,
    overdue: allFranchises.value.filter((f) => f.settlementStatus === 'overdue')
      .length,
  };
});

// 필터링된 가맹점 목록
const filteredFranchises = computed(() => {
  let filtered = allFranchises.value;

  // 검색어 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (f) =>
        f.restaurantName.toLowerCase().includes(query) ||
        f.ownerName.toLowerCase().includes(query)
    );
  }

  // 정산 상태 필터
  if (selectedSettlementStatus.value !== 'all') {
    filtered = filtered.filter(
      (f) => f.settlementStatus === selectedSettlementStatus.value
    );
  }

  // 운영 상태 필터
  if (selectedOperationStatus.value !== 'all') {
    filtered = filtered.filter(
      (f) => f.operationStatus === selectedOperationStatus.value
    );
  }

  // 날짜 범위 필터 (가입일 기준)
  if (startDate.value || endDate.value) {
    filtered = filtered.filter((f) => {
      const registrationDate = new Date(f.registrationDate);

      if (startDate.value && endDate.value) {
        const start = new Date(startDate.value);
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return registrationDate >= start && registrationDate <= end;
      } else if (startDate.value) {
        const start = new Date(startDate.value);
        return registrationDate >= start;
      } else if (endDate.value) {
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return registrationDate <= end;
      }

      return true;
    });
  }

  return filtered;
});

// 페이지네이션된 가맹점 목록
const paginatedFranchises = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredFranchises.value.slice(start, end);
});

// 총 페이지 수
const totalPages = computed(() => {
  return Math.ceil(filteredFranchises.value.length / itemsPerPage);
});

// 페이지 변경 핸들러
const handlePageChange = (page) => {
  currentPage.value = page;
};

// 필터 값 업데이트 핸들러
const handleFilterUpdate = ({ model, value }) => {
  if (model === 'selectedSettlementStatus') {
    selectedSettlementStatus.value = value;
  } else if (model === 'selectedOperationStatus') {
    selectedOperationStatus.value = value;
  }
  currentPage.value = 1;
};

// 필터 초기화
const resetFilters = () => {
  searchQuery.value = '';
  selectedSettlementStatus.value = 'all';
  selectedOperationStatus.value = 'all';
  startDate.value = '';
  endDate.value = '';
  currentPage.value = 1;
};

// 필터 설정
const filters = computed(() => [
  {
    model: 'selectedSettlementStatus',
    label: '정산 상태',
    value: selectedSettlementStatus.value,
    options: settlementStatusOptions,
  },
  {
    model: 'selectedOperationStatus',
    label: '운영 상태',
    value: selectedOperationStatus.value,
    options: operationStatusOptions,
  },
]);
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="franchises" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-6">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">가맹점 관리</h2>
          </div>

          <!-- 통계 카드 -->
          <div class="grid grid-cols-4 gap-6">
            <div
              class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 text-center hover:shadow-lg transition-shadow"
            >
              <p class="text-sm text-[#6c757d] mb-2">전체 식당</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.total }}<span class="text-lg ml-1">개</span>
              </p>
            </div>
            <div
              class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 text-center hover:shadow-lg transition-shadow"
            >
              <p class="text-sm text-[#6c757d] mb-2">승인 대기</p>
              <p class="text-4xl font-bold text-yellow-600">
                {{ stats.approvalPending }}<span class="text-lg ml-1">개</span>
              </p>
            </div>
            <div
              class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 text-center hover:shadow-lg transition-shadow"
            >
              <p class="text-sm text-[#6c757d] mb-2">운영 중</p>
              <p class="text-4xl font-bold text-green-600">
                {{ stats.operating }}<span class="text-lg ml-1">개</span>
              </p>
            </div>
            <div
              class="bg-white rounded-xl border-2 border-[#e9ecef] p-6 text-center hover:shadow-lg transition-shadow"
            >
              <p class="text-sm text-[#6c757d] mb-2">미수금 연체</p>
              <p class="text-4xl font-bold text-red-600">
                {{ stats.overdue }}<span class="text-lg ml-1">개</span>
              </p>
            </div>
          </div>

          <!-- 필터 및 검색 -->
          <AdminSearchFilter
            v-model:search-query="searchQuery"
            v-model:start-date="startDate"
            v-model:end-date="endDate"
            :filters="filters"
            :show-date-filter="true"
            :search-col-span="1"
            search-label="식당명/대표명"
            search-placeholder="식당명 또는 대표자명으로 검색"
            start-date-label="가입일 (시작)"
            end-date-label="가입일 (종료)"
            @update:filter-value="handleFilterUpdate"
            @reset-filters="resetFilters"
          />

          <!-- 가맹점 목록 테이블 -->
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
                      식당명
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      대표자명
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      카테고리
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      등록일
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      지역
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      정산 상태
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      운영 상태
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="franchise in paginatedFranchises"
                    :key="franchise.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1e3a5f]"
                    >
                      {{ franchise.restaurantName }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ franchise.ownerName }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ franchise.category }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ franchise.registrationDate }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ franchise.region }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getSettlementBadgeColor(franchise.settlementStatus),
                        ]"
                      >
                        {{ getSettlementLabel(franchise.settlementStatus) }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getOperationBadgeColor(franchise.operationStatus),
                        ]"
                      >
                        {{ getOperationLabel(franchise.operationStatus) }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- 데이터 없을 때 -->
              <div
                v-if="paginatedFranchises.length === 0"
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
