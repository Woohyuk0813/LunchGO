<script setup>
import { ref, onMounted, computed } from 'vue';
import { RouterLink, useRouter } from 'vue-router'; // useRouter import 추가
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import { useRestaurantStore } from '@/stores/restaurant'; // Pinia 스토어 임포트

// Pinia 스토어 초기화
const store = useRestaurantStore();
const router = useRouter(); // router 인스턴스 생성

// Define props to receive the restaurant ID from the route
const props = defineProps({
  id: {
    type: String,
    required: true,
  },
});

// 2. 상태
// const restaurant = ref(null); // API로부터 받은 식당 정보를 저장할 ref -> Pinia 스토어 사용
const restaurant = computed(() => store.restaurantInfo); // Pinia 스토어의 restaurantInfo를 참조

// 3. 요일 매핑
const dayOfWeekMap = {
  1: '일',
  2: '월',
  3: '화',
  4: '수',
  5: '목',
  6: '금',
  7: '토',
};

// 4. 계산된 속성
// 정기 휴무일 숫자 배열을 문자 배열로 변환
const closedDaysAsStrings = computed(() => {
  if (!store.restaurantInfo || !store.restaurantInfo.regularHolidays) return [];
  return store.restaurantInfo.regularHolidays.map(
    holiday => dayOfWeekMap[holiday.dayOfWeek]
  );
});

// 대표 이미지 URL
const mainImageUrl = computed(() => {
  if (!store.restaurantInfo || !store.restaurantInfo.images || store.restaurantInfo.images.length === 0) {
    return '/placeholder.svg'; // 기본 이미지
  }
  return store.restaurantInfo.images[0].imageUrl;
});

// 5. 라이프사이클 훅
// API로부터 데이터를 받아오는 로직을 처리할 함수
onMounted(async () => {
  try {
    await store.fetchRestaurantDetail(props.id);
  } catch (error) {
    if (error.response && (error.response.status === 403 || error.response.status === 404)) {
      alert('해당 식당 정보에 접근할 권한이 없습니다.');
      router.push('/business/dashboard');
    } else {
      alert('식당 정보를 불러오는 중 오류가 발생했습니다.');
      router.push('/business/dashboard');
    }
  }
});
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="restaurant-info" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <!-- v-if를 추가하여 restaurant 데이터가 로드된 후에만 렌더링 -->
        <div v-if="store.restaurantInfo" class="max-w-4xl mx-auto space-y-8">
          <!-- Page Title -->
          <h2 class="text-3xl font-bold text-[#1e3a5f]">식당 정보 조회</h2>

          <!-- Restaurant Basic Info Card -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              식당 기본 정보
            </h3>

            <!-- Restaurant Image -->
            <div
              class="mb-8 border-2 border-[#e9ecef] rounded-xl overflow-hidden bg-[#f8f9fa]"
            >
              <div class="aspect-[2/1] flex items-center justify-center">
                <img
                  :src="mainImageUrl"
                  :alt="restaurant.name"
                  class="w-full h-full object-cover"
                />
              </div>
            </div>

            <!-- Form Fields -->
            <div class="space-y-6">
              <!-- Restaurant Name & Phone -->
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >식당명</label
                  >
                  <input
                    type="text"
                    :value="restaurant.name"
                    readonly
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >식당전화번호</label
                  >
                  <input
                    type="text"
                    :value="restaurant.phone"
                    readonly
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                  />
                </div>
              </div>

              <!-- Opening Date -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >개업일</label
                >
                <input
                  type="text"
                  :value="restaurant.openDate"
                  readonly
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                />
              </div>

              <!-- Business Hours -->
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업시작시간</label
                  >
                  <input
                    type="text"
                    :value="restaurant.openTime"
                    readonly
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                  />
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업종료시간</label
                  >
                  <input
                    type="text"
                    :value="restaurant.closeTime"
                    readonly
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                  />
                </div>
              </div>

              <!-- Reservation Capacity -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >예약인원 상한</label
                >
                <input
                  type="number"
                  :value="restaurant.reservationLimit"
                  readonly
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                />
              </div>

              <!-- Address -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >도로명주소</label
                >
                <input
                  type="text"
                  :value="restaurant.roadAddress"
                  readonly
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                />
              </div>

              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >상세주소</label
                >
                <input
                  type="text"
                  :value="restaurant.detailAddress"
                  readonly
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg bg-[#f8f9fa] text-[#6c757d]"
                />
              </div>

              <!-- Checkboxes -->
              <div class="grid grid-cols-2 gap-6">
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="holiday"
                    :checked="restaurant.holidayAvailable"
                    disabled
                    class="custom-checkbox w-5 h-5 rounded appearance-none cursor-not-allowed"
                  />
                  <label for="holiday" class="text-sm text-[#1e3a5f]">
                    공휴일 운영
                  </label>
                </div>
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="preorder"
                    :checked="restaurant.preorderAvailable"
                    disabled
                    class="custom-checkbox w-5 h-5 rounded appearance-none cursor-not-allowed"
                  />
                  <label for="preorder" class="text-sm text-[#1e3a5f]">
                    선주문/선결제 가능
                  </label>
                </div>
              </div>

              <!-- Regular Closing Days -->
              <div v-if="closedDaysAsStrings.length > 0">
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
                  정기휴무일
                </label>
                <div class="flex flex-wrap gap-3">
                  <button
                    v-for="day in ['일', '월', '화', '수', '목', '금', '토']"
                    :key="day"
                    :class="`px-4 py-2 rounded-lg border transition-colors ${
                      closedDaysAsStrings.includes(day)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] bg-[#f8f9fa]'
                    }`"
                    disabled
                  >
                    {{ day }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Restaurant Introduction -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-4">식당 소개</h3>
            <div
              class="border-2 border-[#e9ecef] rounded-xl p-6 bg-[#f8f9fa] min-h-[120px]"
            >
              <p class="text-sm text-[#6c757d]">{{ restaurant.description }}</p>
            </div>
          </div>

          <!-- Restaurant Tags -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-4">식당 태그</h3>
            <div class="flex flex-wrap gap-3">
              <div
                v-for="tag in restaurant.tags"
                :key="tag.tagId"
                class="px-4 py-2 border-2 border-[#dee2e6] rounded-lg bg-white"
              >
                <span class="text-sm text-[#1e3a5f]">#{{ tag.content }}</span>
              </div>
            </div>
          </div>

          <!-- View All Menus Button -->
          <RouterLink
            :to="{
              name: 'business-restaurant-menus',
              params: { id: props.id },
            }"
            class="block w-full text-center gradient-primary text-white py-4 rounded-xl text-lg font-semibold hover:opacity-90 transition-opacity"
          >
            식당메뉴 전체보기
          </RouterLink>

          <!-- Edit Button -->
          <div class="flex justify-end">
            <RouterLink :to="`/business/restaurant-info/edit/${props.id}`">
              <button
                class="px-8 py-3 border-2 border-[#FF6B4A] text-[#FF6B4A] rounded-xl font-semibold hover:bg-[#fff5f2] transition-colors"
              >
                식당정보 편집
              </button>
            </RouterLink>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.custom-checkbox {
  border-width: 1px;
  border-color: #dee2e6; /* Unchecked border color */
  background-color: #fff; /* Unchecked background */
}
.custom-checkbox:checked {
  border-color: rgba(255, 107, 74, 0.5); /* Primary color with opacity */
  background-color: rgba(255, 107, 74, 0.5); /* Primary color with opacity */
  background-image: url("data:image/svg+xml,%3csvg viewBox='0 0 16 16' fill='white' xmlns='http://www.w3.org/2000/svg'%3e%3cpath d='M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z'/%3e%3c/svg%3e");
  background-size: 100% 100%;
  background-position: center;
  background-repeat: no-repeat;
}
</style>
