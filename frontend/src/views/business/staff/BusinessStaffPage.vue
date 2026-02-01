<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
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
const ownerId = computed(() => {
  const rawId = member.value?.id;
  if (rawId === null || rawId === undefined) return null;
  const parsed = Number(rawId);
  return Number.isNaN(parsed) ? null : parsed;
});

const emailInput = ref('');
const emailFormatMsg = ref('');
const showSuccessMessage = ref(false);
const currentPage = ref(1);
const itemsPerPage = 7;
const maxPageButtons = 5; // 최대 보여줄 페이지 번호 개수

//중복 클릭 방지를 위한 로딩 상태
const isRegistering = ref(false);

// 데이터 목록
const staffList = ref([]);

const fetchStaffInfo = async () => {
  const resolvedOwnerId = ownerId.value;
  if (!resolvedOwnerId) {
    return alert('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
  }

  try {
    const response = await httpRequest.get(
      `/api/business/staff/${resolvedOwnerId}`
    );

    if (response.data) {
      staffList.value = response.data.map((item) => ({
        id: item.staffId,   // 백엔드 staffId -> 프론트 id
        name: item.name,    // 이름
        email: item.email   // 이메일
      }));
    }
  } catch (error) {
    console.error('에러 상세 내용:', error);
    alert(`오류가 발생했습니다. (Code: ${error?.response?.status ?? 'unknown'})`);
  }
};

onMounted(() => {
  fetchStaffInfo();
});

const totalPages = computed(() => {
  return Math.ceil(staffList.value.length / itemsPerPage);
});

// 화면에 보여줄 페이지 번호 배열 계산 (최대 5개)
const visiblePages = computed(() => {
  const current = currentPage.value;
  const total = totalPages.value;

  if (total <= maxPageButtons) {
    // 전체 페이지가 5개 이하이면 전체 표시
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  //보여줄 페이지 번호 계산
  let start = current - Math.floor(maxPageButtons / 2);
  let end = start + maxPageButtons - 1;

  //시작값이 1보다 작을 경우
  if (start < 1) {
    start = 1;
    end = maxPageButtons;
  }

  //끝값이 전체 페이지를 넘을 경우
  if (end > total) {
    end = total;
    start = end - maxPageButtons + 1;
  }

  // start부터 end까지의 숫자 배열 생성
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

const handleInputChange = () => {
  if (emailFormatMsg.value) {
    emailFormatMsg.value = '';
  }
};

const handleAddStaff = async () => {
  if (isRegistering.value) return;

  if (!emailInput.value) {
    emailFormatMsg.value = '이메일을 입력해주세요.';
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(emailInput.value)) {
    emailFormatMsg.value = '올바른 이메일 형식이 아닙니다.';
    return;
  }

  isRegistering.value = true;

  const resolvedOwnerId = ownerId.value;
  if (!resolvedOwnerId) {
    alert('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
    isRegistering.value = false;
    return;
  }

  try {
    await httpRequest.post('/api/business/staff', {
      ownerId: resolvedOwnerId,
      email: emailInput.value
    });

    fetchStaffInfo();
    emailFormatMsg.value = '';
    emailInput.value = '';
    showSuccessMessage.value = true;
    setTimeout(() => (showSuccessMessage.value = false), 3000);
  } catch (error) {
    const status = error?.response?.status;

    switch (status) {
      case 400:
        alert('[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.');
        break;
      case 404:
        alert('[404 Not Found] 해당 사용자는 존재하지 않습니다.');
        break;
      case 409:
        alert('[409 Conflict] 이미 등록된 직원입니다.');
        break;
      default:
        alert(`오류가 발생했습니다. (Code: ${status ?? 'unknown'})`);
    }
  } finally {
    isRegistering.value = false;
  }
};

const handleDeleteStaff = async (targetId) => {
  try {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    const resolvedOwnerId = ownerId.value;
    if (!resolvedOwnerId) {
      alert('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
      return;
    }

    await httpRequest.delete('/api/business/staff', {
      data: {
        staffId: targetId,
        ownerId: resolvedOwnerId
      }
    });

    staffList.value = staffList.value.filter((staff) => staff.id !== targetId);
  } catch (error) {
    const status = error?.response?.status;

    switch (status) {
      case 400:
        alert('[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.');
        break;
      case 404:
        alert('[404 Not Found] 해당 사업자의 직원이 존재하지 않습니다.');
        break;
      default:
        alert(`오류가 발생했습니다. (Code: ${status ?? 'unknown'})`);
    }
  }
};

// [추가] 이전, 다음 페이지 이동 함수
const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--;
};

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++;
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="staff" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <main class="flex-1 overflow-auto p-8">
        <div class="max-w-6xl mx-auto">
          <h2 class="text-3xl font-bold text-[#1E3A5F] mb-8">직원 관리</h2>

          <div class="bg-white rounded-xl border border-gray-200 p-6 pb-9 mb-6">
            <div class="flex items-start gap-4">
              <div class="flex-1 relative">
                <input
                  type="email"
                  v-model="emailInput"
                  @input="handleInputChange"
                  placeholder="직원 이메일을 입력하세요"
                  class="w-full px-4 py-3 border rounded-lg focus:outline-none focus:ring-2 transition-colors"
                  :class="[
                    emailFormatMsg
                      ? 'border-red-500 focus:border-red-500 focus:ring-red-200'
                      : 'border-gray-300 focus:ring-[#FF6B4A] focus:border-transparent',
                  ]"
                />

                <p
                  v-if="emailFormatMsg"
                  class="absolute left-1 top-full mt-2 text-xs text-red-500 font-medium"
                >
                  {{ emailFormatMsg }}
                </p>
              </div>

              <button
                @click="handleAddStaff"
                :disabled="isRegistering"
                class="px-6 py-3 text-white font-semibold rounded-lg transition-all whitespace-nowrap flex items-center justify-center min-w-[120px]"
                :class="isRegistering 
                    ? 'bg-gray-400 cursor-not-allowed' 
                    : 'bg-gradient-to-r from-[#FF6B4A] to-[#FFC4B8] hover:opacity-90'"
            >
                <span v-if="isRegistering">등록 중...</span>
                <span v-else>직원 추가</span>
            </button>
            </div>
          </div>

          <div
            class="bg-white rounded-xl border border-gray-200 overflow-hidden"
          >
            <table class="w-full">
              <thead class="bg-gray-50 border-b border-gray-200">
                <tr>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700 w-24"
                  >
                    번호
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    이름
                  </th>
                  <th
                    class="px-6 py-4 text-left text-sm font-semibold text-gray-700"
                  >
                    이메일
                  </th>
                  <th
                    class="px-6 py-4 text-center text-sm font-semibold text-gray-700 w-32"
                  ></th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200">
                <tr
                  v-for="(staff, index) in paginatedStaffList"
                  :key="staff.id"
                  class="hover:bg-gray-50"
                >
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ (currentPage - 1) * itemsPerPage + index + 1 }}
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ staff.name }}
                  </td>
                  <td class="px-6 py-4 text-sm text-gray-900">
                    {{ staff.email }}
                  </td>
                  <td class="px-6 py-4 text-center">
                    <button
                      @click="handleDeleteStaff(staff.id)"
                      class="px-4 py-2 bg-[#ffcdd2] text-[#c62828] rounded-lg text-sm font-medium hover:bg-[#ef9a9a] transition-colors"
                    >
                      삭제
                    </button>
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

          <div
            v-if="showSuccessMessage"
            class="fixed bottom-8 right-8 bg-blue-50 border border-blue-200 rounded-lg px-6 py-4 shadow-lg flex items-center gap-3 animate-fade-in"
          >
            <div
              class="w-6 h-6 bg-blue-500 rounded-full flex items-center justify-center"
            >
              <svg
                class="w-4 h-4 text-white"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
            <span class="text-sm font-medium text-blue-900"
              >직원이 등록되었습니다.</span
            >
          </div>
        </div>
      </main>
    </div>
  </div>
</template>
