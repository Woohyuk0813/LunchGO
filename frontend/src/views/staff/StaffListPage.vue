<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import StaffSideBar from '@/components/ui/StaffSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';

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
const staffId = computed(() => {
  const rawId = member.value?.staffId ?? member.value?.id ?? member.value?.memberId;
  if (rawId === null || rawId === undefined) return null;
  const parsed = Number(rawId);
  return Number.isNaN(parsed) ? null : parsed;
});

const currentPage = ref(1);
const itemsPerPage = 7;
const maxPageButtons = 5;

// 직원 목록
const staffList = ref([]); // id, name, email

const fetchStaffList = async () => {
  const resolvedStaffId = staffId.value;
  if (!resolvedStaffId) {
    alert('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
    return;
  }

  try {
    const response = await httpRequest.get(`/api/business/staff/${resolvedStaffId}`);
    if (response.data) {
      staffList.value = response.data.map((item) => ({
        id: item.staffId,
        name: item.name,
        email: item.email,
      }));
    }
  } catch (error) {
    alert(`오류가 발생했습니다. (Code: ${error?.response?.status ?? 'unknown'})`);
  }
};

onMounted(() => {
  fetchStaffList();
});

const totalPages = computed(() => {
  return Math.ceil(staffList.value.length / itemsPerPage);
});

const visiblePages = computed(() => {
  const current = currentPage.value;
  const total = totalPages.value;

  if (total <= maxPageButtons) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  let start = current - Math.floor(maxPageButtons / 2);
  let end = start + maxPageButtons - 1;

  if (start < 1) {
    start = 1;
    end = maxPageButtons;
  }

  if (end > total) {
    end = total;
    start = end - maxPageButtons + 1;
  }

  const pages = [];
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  return pages;
});

const paginatedStaffList = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return staffList.value.slice(start, end);
});

watch(totalPages, (newTotalPages) => {
  if (currentPage.value > newTotalPages && newTotalPages > 0) {
    currentPage.value = newTotalPages;
  }
});

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <StaffSideBar activeMenu="staff" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <main class="flex-1 overflow-auto p-8">
        <div class="max-w-6xl mx-auto">
          <h2 class="text-3xl font-bold text-[#1E3A5F] mb-8">직원 현황</h2>

          <div class="bg-white rounded-xl border border-gray-200 overflow-hidden shadow-sm">
            <table class="w-full table-fixed">
              <thead class="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th class="py-4 text-center text-base font-semibold text-gray-700 w-[15%]">
                    번호
                  </th>
                  <th class="py-4 text-center text-base font-semibold text-gray-700 w-[20%]">
                    이름
                  </th>
                  <th class="py-4 text-left pl-48 text-base font-semibold text-gray-700 w-[65%]">
                    이메일
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200">
                <tr
                  v-for="(staff, index) in paginatedStaffList"
                  :key="staff.id"
                  class="hover:bg-gray-50 transition-colors"
                >
                  <td class="py-4 text-base text-gray-500 text-center">
                    {{ (currentPage - 1) * itemsPerPage + index + 1 }}
                  </td>
                  <td class="py-4 text-base text-gray-900 text-center font-medium">
                    {{ staff.name }}
                  </td>
                  <td class="py-4 text-base text-gray-600 text-left pl-48">
                    {{ staff.email }}
                  </td>
                </tr>
              </tbody>
            </table>

            <div
              v-if="totalPages > 0"
              class="border-t border-gray-200 px-6 py-4 flex items-center justify-center gap-2"
            >
              <button
                @click="prevPage"
                :disabled="currentPage === 1"
                class="px-3 py-2 rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:hover:bg-transparent transition-colors"
              >
                <svg
                  class="w-5 h-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 19l-7-7 7-7"
                  />
                </svg>
              </button>

              <button
                v-for="page in visiblePages"
                :key="page"
                @click="currentPage = page"
                :class="`w-8 h-8 flex items-center justify-center rounded-lg text-sm font-medium transition-colors ${
                  currentPage === page
                    ? 'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] text-white shadow-sm'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                }`"
              >
                {{ page }}
              </button>

              <button
                @click="nextPage"
                :disabled="currentPage === totalPages"
                class="px-3 py-2 rounded-lg text-gray-500 hover:bg-gray-100 disabled:opacity-30 disabled:hover:bg-transparent transition-colors"
              >
                <svg
                  class="w-5 h-5"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 5l7 7-7 7"
                  />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
