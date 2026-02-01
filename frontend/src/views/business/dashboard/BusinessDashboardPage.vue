<script setup>
import { ref, computed, onMounted } from 'vue';
import { Filter } from 'lucide-vue-next'; // Bell, User는 Header로 이동
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import StaffSideBar from '@/components/ui/StaffSideBar.vue';
import { useRouter, useRoute } from 'vue-router';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';
import { Line } from 'vue-chartjs';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend } from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend);


const router = useRouter();
const route = useRoute();
const restaurantId = computed(() => Number(route.query.restaurantId || 0));

/* ====== (추가) 권한 확인: Pinia 우선, 없으면 localStorage ====== */
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

const userRole = computed(() => {
  if (member.value?.role === 'ROLE_OWNER') return 'owner';
  if (member.value?.role === 'ROLE_STAFF') return 'staff';
  return '';
});
/* ============================================================= */

const goDetail = (id) => {
  router.push({
    name: 'reservation-detail',
    params: { id: String(id) },
    query: { ...route.query },
  });
};

// 필터 상태
const filterOpen = ref(false);
const statusFilter = ref('all'); // all | confirmed | pending | cancelled | refunded

// 취소 모달 상태
const cancelModalOpen = ref(false);
const cancelTargetId = ref(null);
const cancelReason = ref('');
const cancelError = ref('');
const cancelling = ref(false);
const MAX_CANCEL_REASON = 50;

const openCancelModal = (id) => {
  cancelTargetId.value = id;
  cancelReason.value = '';
  cancelError.value = '';
  cancelModalOpen.value = true;
};

const closeCancelModal = () => {
  cancelModalOpen.value = false;
  cancelTargetId.value = null;
  cancelReason.value = '';
  cancelError.value = '';
};

const submitCancel = async () => {
  const reason = cancelReason.value.trim();
  if (!reason) {
    cancelError.value = '취소 사유를 입력해주세요.';
    return;
  }

  if (reason.length > MAX_CANCEL_REASON) {
    cancelError.value = `취소 사유는 ${MAX_CANCEL_REASON}자 이내로 입력해주세요.`;
    return;
  }

  const target = reservations.value.find((r) => r.id === cancelTargetId.value);
  if (!target) return;

  try {
    cancelling.value = true;
    cancelError.value = '';
    await httpRequest.post(`/api/business/reservations/${cancelTargetId.value}/cancel`, {
      reason,
      detail: '',
    });
    target.status = 'cancelled';
    target.cancelReason = reason;
    target.cancelledAt = new Date().toISOString();
    closeCancelModal();
    window.alert('취소 되었습니다.');
  } catch (error) {
    cancelError.value = error?.message || '예약 취소에 실패했습니다.';
  } finally {
    cancelling.value = false;
  }
};

const selectedDate = ref(new Date());
const selectedDateStr = computed(() => {
  const d = selectedDate.value;
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
});

const reservations = ref([]);

const mapStatus = (status) => {
  if (status === '대기') return 'pending';
  if (status === '확정') return 'confirmed';
  if (status === '취소') return 'cancelled';
  if (status === '환불') return 'refunded';
  return 'confirmed';
};

const splitDateTime = (datetime) => {
  if (!datetime) return { date: '', time: '' };
  const parts = String(datetime).split(' ');
  return { date: parts[0] || '', time: parts[1] || '' };
};

const ensureRestaurantId = async () => {
  if (restaurantId.value) return restaurantId.value;

  // 사업자 로그인 상태면 백엔드에서 owner의 식당 id를 받아서 쿼리에 박아넣음
  try {
    const res = await httpRequest.get('/api/business/me/restaurant');
    const rid = res?.data?.restaurantId;
    if (rid) {
      await router.replace({
        path: route.path,
        query: { ...route.query, restaurantId: String(rid) },
      });
      return Number(rid);
    }
  } catch (e) {
    console.error('restaurantId 자동 조회 실패:', e);
  }
  return 0;
};

const loadReservations = async (rid) => {
  if (!rid) return;
  try {
    const response = await httpRequest.get('/api/business/reservations', {
      params: { restaurantId: rid },
    });
    if (Array.isArray(response.data)) {
      reservations.value = response.data.map((row) => {
        const { date, time } = splitDateTime(row.datetime);
        return {
          id: row.id,
          name: row.name,
          phone: row.phone,
          date,
          time,
          guests: row.guests,
          amount: row.amount || 0,
          status: mapStatus(row.status),
        };
      });
    }
  } catch (error) {
    console.error('예약 조회 실패:', error);
  }
};

onMounted(async () => {
  selectedDate.value = new Date();
  const rid = await ensureRestaurantId();
  if (rid) {
    await loadReservations(rid);
  }
});

const reservationsForDate = computed(() => {
  return reservations.value.filter((r) => r.date === selectedDateStr.value);
});

//필터 적용된 예약 리스트
const filteredReservations = computed(() => {
  const base = reservationsForDate.value;
  if (statusFilter.value === 'all') return base;
  return base.filter((r) => r.status === statusFilter.value);
});

// 예약 상태가 바뀌면 자동 갱신
const stats = computed(() => ({
  total: reservationsForDate.value.length,
  confirmed: reservationsForDate.value.filter((r) => r.status === 'confirmed').length,
  pending: reservationsForDate.value.filter((r) => r.status === 'pending').length,
  cancelled: reservationsForDate.value.filter((r) => r.status === 'cancelled').length,
}));

const salesStats = computed(() => {
  const confirmedList = reservationsForDate.value.filter((r) => r.status === 'confirmed');
  return {
    totalSales: confirmedList.reduce((sum, r) => sum + r.amount, 0),
    totalReservations: confirmedList.length,
    averagePerPerson: 18000,
    totalGuests: confirmedList.reduce((sum, r) => sum + r.guests, 0),
  };
});

// 오늘(선택 날짜) 시간대별 예약 건수 그래프: 확정/대기/취소 분리
const todayHourlyChartData = computed(() => {
  const labels = Array.from({ length: 24 }, (_, i) => `${i}시`);

  const confirmed = Array(24).fill(0);
  const pending = Array(24).fill(0);
  const cancelled = Array(24).fill(0);

  reservationsForDate.value.forEach(r => {
    const hour = Number(String(r.time).split(':')[0]);
    if (Number.isNaN(hour)) return;

    if (r.status === 'confirmed') confirmed[hour]++;
    else if (r.status === 'pending') pending[hour]++;
    else if (r.status === 'cancelled') cancelled[hour]++;
  });

  return {
    labels,
    datasets: [
      {
        label: '확정',
        data: confirmed,
        borderColor: '#FF6B4A',
        backgroundColor: 'rgba(255,107,74,0.12)',
        pointBackgroundColor: '#FF6B4A',
        pointBorderColor: '#FF6B4A',
        tension: 0.35,
      },
      {
        label: '대기',
        data: pending,
        borderColor: '#FF9A76',
        backgroundColor: 'rgba(255,154,118,0.12)',
        pointBackgroundColor: '#FF9A76',
        pointBorderColor: '#FF9A76',
        tension: 0.35,
      },
      {
        label: '취소',
        data: cancelled,
        borderColor: '#FFC4B8',
        backgroundColor: 'rgba(255,196,184,0.12)',
        pointBackgroundColor: '#FFC4B8',
        pointBorderColor: '#FFC4B8',
        borderDash: [6, 4],
        tension: 0.35,
      },
    ],
  };
});


const todayHourlyChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    y: { beginAtZero: true },
  },
};

</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar
        v-if="userRole === 'owner'"
        activeMenu="dashboard"
    />
    <StaffSideBar
        v-else-if="userRole === 'staff'"
        activeMenu="dashboard"
    />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-8">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">오늘의 예약 현황</h2>
            <div class="flex items-center gap-2">
              <span class="text-sm text-[#6c757d]">{{ selectedDateStr }}</span>
            </div>
          </div>

          <!-- Stats Cards -->
          <div class="grid grid-cols-4 gap-6">
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center">
              <p class="text-sm text-[#6c757d] mb-2">전체 예약</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.total }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center">
              <p class="text-sm text-[#6c757d] mb-2">확정</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.confirmed }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center">
              <p class="text-sm text-[#6c757d] mb-2">대기</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.pending }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center">
              <p class="text-sm text-[#6c757d] mb-2">취소</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.cancelled }}건</p>
            </div>
          </div>

          <!-- 오늘 시간대별 예약 그래프 -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-bold text-[#1e3a5f]">오늘 시간대별 예약</h3>
            </div>

            <div class="h-56">
              <Line
                  v-if="todayHourlyChartData"
                  :data="todayHourlyChartData"
                  :options="todayHourlyChartOptions"
              />
            </div>
          </div>


          <!-- Reservations Table -->
          <div class="bg-white rounded-xl border border-[#e9ecef] overflow-visible">
            <div class="border-b border-[#e9ecef] p-6 flex items-center justify-between bg-[#f8f9fa]">
              <h3 class="text-lg font-bold text-[#1e3a5f]">예약 목록</h3>

              <!-- 기존 버튼 구조 유지 + 클릭만 추가 -->
              <div class="relative">
                <button
                    @click="filterOpen = !filterOpen"
                    class="flex items-center gap-2 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-white transition-colors"
                >
                  <Filter class="w-4 h-4" />
                  <span class="text-sm">필터</span>
                </button>

                <!-- 드롭다운 (한 번만) -->
                <div
                    v-if="filterOpen"
                    class="absolute right-0 mt-2 w-40 bg-white border border-[#e9ecef] rounded-lg shadow-md z-20 overflow-hidden"
                >
                  <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='confirmed'; filterOpen=false">확정</button>
                  <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='pending'; filterOpen=false">대기</button>
                  <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='cancelled'; filterOpen=false">취소</button>
                  <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='refunded'; filterOpen=false">환불</button>
                  <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='all'; filterOpen=false">전체</button>
                </div>
              </div>
            </div>

            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                <tr>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">예약자</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">연락처</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">예약시간</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">인원</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">결제금액</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">상태</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">작업</th>
                </tr>
                </thead>

                <tbody class="divide-y divide-[#e9ecef]">
                <template v-if="filteredReservations.length">
                  <tr
                      v-for="reservation in filteredReservations"
                      :key="reservation.id"
                      class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ reservation.name }}</td>
                    <td class="px-6 py-4 text-sm text-[#6c757d]">{{ reservation.phone }}</td>
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ reservation.date }} {{ reservation.time }}</td>
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ reservation.guests }}명</td>
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ reservation.amount.toLocaleString() }}원</td>

                    <td class="px-6 py-4">
                        <span
                            :class="`inline-flex px-3 py-1 rounded-full text-xs font-semibold ${
                            reservation.status === 'confirmed'
                              ? 'bg-[#d4edda] text-[#155724]'
                              : reservation.status === 'pending'
                              ? 'bg-[#fff3cd] text-[#856404]'
                              : 'bg-[#f8d7da] text-[#721c24]'
                          }`"
                        >
                          {{
                            reservation.status === 'confirmed'
                                ? '확정'
                                : reservation.status === 'pending'
                                    ? '대기'
                                    : '취소'
                          }}
                        </span>
                    </td>

                    <td class="px-6 py-4">
                      <div class="flex items-center gap-2">
                        <button
                            @click="goDetail(reservation.id)"
                            class="gradient-primary text-white px-3 py-2 rounded-lg text-xs hover:opacity-90 transition-opacity"
                        >
                          상세보기
                        </button>

                        <!-- 확정/대기 둘 다 취소 가능 -->
                        <button
                            v-if="reservation.status !== 'cancelled'"
                            @click="openCancelModal(reservation.id)"
                            class="bg-[#dc3545] text-white px-3 py-2 rounded-lg text-xs hover:opacity-90 transition-opacity"
                        >
                          취소
                        </button>
                      </div>
                    </td>
                  </tr>
                </template>

                <tr v-else>
                  <td colspan="7" class="px-6 py-10 text-center text-sm text-[#6c757d]">
                    해당 상태의 예약이 없습니다.
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Daily Sales Stats -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">금일 매출 통계</h3>
            <div class="grid grid-cols-4 gap-6">
              <div class="text-center">
                <p class="text-sm text-[#6c757d] mb-2">총 매출</p>
                <p class="text-2xl font-bold gradient-primary-text">{{ salesStats.totalSales.toLocaleString() }}원</p>
              </div>
              <div class="text-center">
                <p class="text-sm text-[#6c757d] mb-2">예약 건수</p>
                <p class="text-2xl font-bold text-[#1e3a5f]">{{ salesStats.totalReservations }}건</p>
              </div>
              <div class="text-center">
                <p class="text-sm text-[#6c757d] mb-2">1인당 평균</p>
                <p class="text-2xl font-bold text-[#1e3a5f]">{{ salesStats.averagePerPerson.toLocaleString() }}원</p>
              </div>
              <div class="text-center">
                <p class="text-sm text-[#6c757d] mb-2">총 방문 인원</p>
                <p class="text-2xl font-bold text-[#1e3a5f]">{{ salesStats.totalGuests }}명</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>

  <!--취소 사유 모달 (테이블 밖에 1개만 유지) -->
  <div v-if="cancelModalOpen" class="fixed inset-0 z-50 flex items-center justify-center">
    <div class="absolute inset-0 bg-black/30" @click="closeCancelModal"></div>

    <div class="relative w-full max-w-lg bg-white rounded-xl border border-[#e9ecef] shadow-lg p-6">
      <h3 class="text-lg font-bold text-[#1e3a5f]">예약 취소</h3>
      <p class="text-sm text-[#6c757d] mt-1">취소 사유를 입력해주세요.</p>

      <div class="mt-4">
        <textarea
            v-model="cancelReason"
            rows="4"
            maxlength="50"
            class="w-full border border-[#dee2e6] rounded-lg p-3 text-sm outline-none focus:ring-2 focus:ring-[#ff6b4a]/30"
            placeholder="취소 사유를 50자 이내로 입력해주세요. (예: 고객 요청 / 매장 사정 / 노쇼 등)"
        />
        <p class="mt-2 text-xs text-[#6c757d] text-right">
          {{ cancelReason.trim().length }}/50
        </p>
        <p v-if="cancelError" class="mt-2 text-sm text-[#dc3545]">{{ cancelError }}</p>
      </div>

      <div class="mt-5 flex justify-end gap-2">
        <button
            @click="closeCancelModal"
            class="px-4 py-2 border border-[#dee2e6] rounded-lg text-sm text-[#1e3a5f] hover:bg-[#f8f9fa]"
        >
          닫기
        </button>
        <button
            @click="submitCancel"
            :disabled="cancelling || !cancelReason.trim() || cancelReason.trim().length > 50"
            class="px-4 py-2 rounded-lg text-sm text-white bg-[#dc3545] hover:opacity-90"
        >
          취소 확정
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gradient-primary-text {
  background: linear-gradient(135deg, #ff6b4a 0%, #ffaa8d 50%, #ffc4b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}
</style>
