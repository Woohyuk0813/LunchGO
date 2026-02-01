<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { Filter, Bell, CheckCircle2, XCircle, Clock, Search } from 'lucide-vue-next';

import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import StaffSideBar from '@/components/ui/StaffSideBar.vue';
import { useAccountStore } from '@/stores/account';
import httpRequest from '@/router/httpRequest';

const router = useRouter();
const route = useRoute();
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

// const activeMenu = computed(() =>
//   route.query.from === 'notifications' ? 'notifications' : 'reservations'
// );

const goReservationDetail = (reservationId) => {
  router.push({
    name: 'reservation-detail',
    params: { id: String(reservationId) },
    query: { from: 'notifications' },
  });
};

const handleWillVisitAction = async (notification) => {
  try {
    await httpRequest.patch(`/api/reservations/${notification.reservationId}/complete`);

    alert("방문 확정 처리 되었습니다.");

    notifications.value = notifications.value.filter((n) => n.id !== notification.id); //방문 확정되면 해당 row 제거
  } catch (error) {
    const status = error?.response?.status;
    if (status === 404) {
      alert('[404 Not Found] 예약 정보를 찾을 수 없습니다.');
    }
    console.log("error: "+ error);
  }
};

const notifications = ref([]);

// ---- UI state ----
const filterOpen = ref(false);
const statusFilter = ref('all'); // all | pending | will_visit | cancel_visit
const keyword = ref(''); // 이름/전화/예약ID 검색

const filterWrap = ref(null);
const handleOutsideClick = (e) => {
  if (!filterOpen.value) return;
  if (filterWrap.value && !filterWrap.value.contains(e.target)) {
    filterOpen.value = false;
  }
};

onMounted(async () => {
  document.addEventListener('click', handleOutsideClick);

  try {
    const res = await httpRequest.get('/api/business/notifications');
    notifications.value = res.data || [];
  } catch (e) {
      console.error('알림 내역을 불러오는 데 실패했습니다:', e);
      alert('알림 내역을 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }
  });

onBeforeUnmount(() => document.removeEventListener('click', handleOutsideClick));

// ---- computed ----
const stats = computed(() => ({
  total: notifications.value.length,
  pending: notifications.value.filter((n) => n.responseStatus === 'pending').length,
  willVisit: notifications.value.filter((n) => n.responseStatus === 'will_visit').length,
  cancelVisit: notifications.value.filter((n) => n.responseStatus === 'cancel_visit').length,
  unread: notifications.value.filter((n) => !n.isRead).length,
}));

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase();

  return notifications.value.filter((n) => {
    const byStatus =
      statusFilter.value === 'all' ? true : n.responseStatus === statusFilter.value;

    const byKeyword = !k
      ? true
      : String(n.reservationId).includes(k) ||
        n.customerName.toLowerCase().includes(k) ||
        n.phone.replaceAll('-', '').includes(k.replaceAll('-', ''));

    return byStatus && byKeyword;
  });
});

const statusLabel = (s) => {
  if (s === 'pending') return '응답 대기';
  if (s === 'will_visit') return '방문 예정';
  if (s === 'cancel_visit') return '방문 취소';
  return '알 수 없음';
};

const statusBadgeClass = (s) => {
  if (s === 'pending') return 'bg-[#fff3cd] text-[#856404]';
  if (s === 'will_visit') return 'bg-[#d4edda] text-[#155724]';
  if (s === 'cancel_visit') return 'bg-[#f8d7da] text-[#721c24]';
  return 'bg-[#e9ecef] text-[#495057]';
};

const statusIcon = (s) => {
  if (s === 'pending') return Clock;
  if (s === 'will_visit') return CheckCircle2;
  if (s === 'cancel_visit') return XCircle;
  return Bell;
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar 
      v-if="userRole === 'owner'" 
      activeMenu="notifications" 
    />
    <StaffSideBar 
      v-else-if="userRole === 'staff'" 
      activeMenu="notifications" 
    />

    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-8">
          <!-- Title -->
          <div class="flex items-start justify-between gap-4">
            <div>
              <h2 class="text-3xl font-bold text-[#1e3a5f]">알림 내역</h2>
              <p class="mt-1 text-sm text-[#6c757d]">
                예약 1시간 전 확인 메시지에 대한 고객 응답(방문 예정/방문 취소)을 확인합니다.
              </p>
            </div>
          </div>

          <!-- Stats -->
          <div class="grid grid-cols-4 gap-6">
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center">
              <p class="text-sm text-[#6c757d] mb-2">전체</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.total }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center">
              <p class="text-sm text-[#6c757d] mb-2">응답 대기</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.pending }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center">
              <p class="text-sm text-[#6c757d] mb-2">방문 예정</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.willVisit }}건</p>
            </div>
            <div class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center">
              <p class="text-sm text-[#6c757d] mb-2">방문 취소</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">{{ stats.cancelVisit }}건</p>
            </div>
          </div>

          <!-- List -->
          <div class="bg-white rounded-xl border border-[#e9ecef] overflow-visible">
            <div class="border-b border-[#e9ecef] p-6 flex items-center justify-between bg-[#f8f9fa] gap-3">
              <h3 class="text-lg font-bold text-[#1e3a5f]">알림 목록</h3>

              <div class="flex items-center gap-3">
                <!-- Search -->
                <div class="relative">
                  <Search class="w-4 h-4 text-[#6c757d] absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    v-model="keyword"
                    class="pl-9 pr-3 py-2 text-sm rounded-lg border border-[#dee2e6] outline-none focus:ring-2 focus:ring-[#ff6b4a]/20 bg-white"
                    placeholder="이름/전화/예약ID 검색"
                  />
                </div>

                <!-- Filter -->
                <div class="relative" ref="filterWrap">
                  <button
                    @click.stop="filterOpen = !filterOpen"
                    class="flex items-center gap-2 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-white transition-colors"
                  >
                    <Filter class="w-4 h-4" />
                    <span class="text-sm">필터</span>
                  </button>

                  <div
                    v-if="filterOpen"
                    class="absolute right-0 mt-2 w-44 bg-white border border-[#e9ecef] rounded-lg shadow-md z-20 overflow-hidden"
                  >
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='pending'; filterOpen=false">응답 대기</button>
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='will_visit'; filterOpen=false">방문 예정</button>
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='cancel_visit'; filterOpen=false">방문 취소</button>
                    <button class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]" @click="statusFilter='all'; filterOpen=false">전체</button>
                  </div>
                </div>
              </div>
            </div>

            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">고객</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">연락처</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">예약시간</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">발송시간</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">응답</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">응답시간</th>
                    <th class="px-6 py-4 text-left text-sm font-semibold text-[#1e3a5f]">작업</th>
                  </tr>
                </thead>

                <tbody class="divide-y divide-[#e9ecef]">
                  <tr v-if="!filtered.length">
                    <td colspan="7" class="px-6 py-10 text-center text-sm text-[#6c757d]">
                      조건에 맞는 알림이 없습니다.
                    </td>
                  </tr>

                  <tr
                    v-for="n in filtered"
                    :key="n.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td class="px-6 py-4">
                      <div class="flex items-center gap-2">
                        <div class="text-sm text-[#1e3a5f] font-semibold">{{ n.customerName }}</div>
                        <div class="text-xs text-[#6c757d]">#{{ n.reservationId }}</div>
                      </div>
                    </td>

                    <td class="px-6 py-4 text-sm text-[#6c757d]">{{ n.phone }}</td>
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ n.reservationDatetime }}</td>
                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">{{ n.messageSentAt }}</td>

                    <td class="px-6 py-4">
                      <span
                        class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-semibold"
                        :class="statusBadgeClass(n.responseStatus)"
                      >
                        <component :is="statusIcon(n.responseStatus)" class="w-3.5 h-3.5" />
                        {{ statusLabel(n.responseStatus) }}
                      </span>
                    </td>

                    <td class="px-6 py-4 text-sm text-[#1e3a5f]">
                      {{ n.responseAt || '-' }}
                    </td>

                    <td class="px-6 py-4">
                      <div class="flex items-center gap-2">
                        <button
                          @click="goReservationDetail(n.reservationId)"
                          class="bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white px-3 py-2 rounded-lg text-xs hover:opacity-90"
                        >
                          예약 상세
                        </button>
                        <button
                          v-if="n.responseStatus === 'will_visit'"
                          @click="handleWillVisitAction(n)"
                          class="bg-white text-[#FF6B4A] border border-[#FF6B4A] px-3 py-2 rounded-lg text-xs font-semibold hover:bg-[#FFF0EC] cursor-pointer transition-colors"
                        >
                          방문 확정
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Footer hint -->
            <div class="px-6 py-4 text-xs text-[#6c757d] border-t border-[#e9ecef] bg-white">
              고객이 응답하지 않은 경우(응답 대기)는 메뉴 준비가 지연될 수 있도록 운영 정책을 안내할 수 있습니다.
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
