<script setup>
import {computed, onMounted, reactive, ref, watch} from 'vue';
import {Upload, X} from 'lucide-vue-next';
import {RouterLink, useRoute, useRouter} from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import Pagination from '@/components/ui/Pagination.vue';
import {useRestaurantStore} from '@/stores/restaurant';
import httpRequest from "@/router/httpRequest.js";

const router = useRouter();
const route = useRoute();
const store = useRestaurantStore();

// 메뉴 검색 및 필터
const menuSearchKeyword = ref('');
const menuTypes = ['전체', '주메뉴', '서브메뉴', '기타(디저트, 음료)'];
const selectedMenuType = ref('전체');
const sortOptions = ['기본', '이름순', '가격 높은순', '가격 낮은순'];
const selectedSort = ref('기본');

// 필터링 및 정렬된 메뉴
const filteredMenus = computed(() => {
  let menus = store.menus;

  // 메뉴 타입으로 필터링
  if (selectedMenuType.value !== '전체') {
    menus = menus.filter(
      (menu) => menu.category && menu.category.value === selectedMenuType.value
    );
  }

  // 메뉴 이름 키워드로 필터링
  if (menuSearchKeyword.value) {
    menus = menus.filter((menu) => menu.name.includes(menuSearchKeyword.value));
  }

  // 정렬
  const sortedMenus = [...menus]; // 원본 배열 수정을 피하기 위해 복사
  switch (selectedSort.value) {
    case '이름순':
      sortedMenus.sort((a, b) => a.name.localeCompare(b.name));
      break;
    case '가격 높은순':
      sortedMenus.sort((a, b) => b.price - a.price);
      break;
    case '가격 낮은순':
      sortedMenus.sort((a, b) => a.price - b.price);
      break;
  }

  return sortedMenus;
});

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 5; // 한 페이지에 5개씩 보여주기

const totalPages = computed(() => {
  return Math.ceil(filteredMenus.value.length / itemsPerPage);
});

const paginatedMenus = computed(() => {
  const startIndex = (currentPage.value - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  return filteredMenus.value.slice(startIndex, endIndex);
});

// 검색어, 메뉴 타입 또는 정렬 변경 시 첫 페이지로 이동
watch([menuSearchKeyword, selectedMenuType, selectedSort], () => {
  currentPage.value = 1;
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const isEditMode = computed(() => !!route.params.id);
const pageTitle = computed(() =>
  isEditMode.value ? '식당 정보 수정' : '식당 정보 등록'
);
const submitButtonText = computed(() =>
  isEditMode.value ? '수정하기' : '등록하기'
);

const avgMainPrice = computed(() => {
  const mainMenus = store.menus.filter(
    (menu) => menu.category && menu.category.code === 'MAIN'
  );
  if (mainMenus.length === 0) {
    return 0;
  }
  if (mainMenus.length < 3) {
    const totalPrice = mainMenus.reduce((sum, menu) => sum + menu.price, 0);
    return Math.round(totalPrice / mainMenus.length);
  }
  const prices = mainMenus.map((menu) => menu.price);
  const minPrice = Math.min(...prices);
  const maxPrice = Math.max(...prices);
  const totalPrice = prices.reduce((sum, price) => sum + price, 0);
  const adjustedTotal = totalPrice - minPrice - maxPrice;
  return Math.round(adjustedTotal / (prices.length - 2));
});

const formData = reactive({
  name: '',
  phone: '',
  roadAddress: '',
  detailAddress: '',
  description: '',
  avgMainPrice: 0,
  reservationLimit: '',
  holidayAvailable: false,
  preorderAvailable: false,
  openTime: '',
  closeTime: '',
  openDate: '',
});

const restaurantImageFile = ref(null);
const restaurantFileInput = ref(null);

// restaurantImageUrl을 store와 연동된 computed 속성으로 변경
const restaurantImageUrl = computed(
  () => store.restaurantInfo?.images?.[0]?.imageUrl || null
);

const selectedClosedDays = ref([]);
const selectedTags = ref([]);

const dayOfWeekInverseMap = {
  일: 1,
  월: 2,
  화: 3,
  수: 4,
  목: 5,
  금: 6,
  토: 7,
};

const tagCategoryDisplayNames = {
  MENUTYPE: '식당 종류',
  TABLETYPE: '테이블 옵션',
  ATMOSPHERE: '식당 분위기',
  FACILITY: '편의사항',
};

const handleRestaurantFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    restaurantImageFile.value = file;
    // 로컬 URL을 생성하고 store 상태를 업데이트
    const localUrl = URL.createObjectURL(file);
    store.setDraftImageUrl(localUrl);
  }
};

const triggerRestaurantFileInput = () => {
  console.log('triggerRestaurantFileInput called.');
  console.log('restaurantFileInput ref value:', restaurantFileInput.value);
  if (restaurantFileInput.value) {
    restaurantFileInput.value.click();
  } else {
    console.error('restaurantFileInput ref is null. Cannot trigger click.');
  }
};

const clearRestaurantImage = () => {
  restaurantImageFile.value = null;
  // store의 이미지 URL 상태를 업데이트
  store.setDraftImageUrl(null);
  if (restaurantFileInput.value) {
    restaurantFileInput.value.value = '';
  }
};

const openPostcodeSearch = () => {
  new window.daum.Postcode({
    oncomplete: (data) => {
      formData.roadAddress = data.roadAddress;
    },
  }).open();
};

const toggleClosedDay = (dayString) => {
  const dayNumber = dayOfWeekInverseMap[dayString];
  if (dayNumber === undefined) return;

  const index = selectedClosedDays.value.indexOf(dayNumber);
  if (index > -1) {
    selectedClosedDays.value.splice(index, 1);
  } else {
    selectedClosedDays.value.push(dayNumber);
  }
};

const isDaySelected = (dayString) => {
  const dayNumber = dayOfWeekInverseMap[dayString];
  return selectedClosedDays.value.includes(dayNumber);
};

const allTags = ref([]);
const tagCategories = ref({});

const toggleTag = (tag) => {
  const isRestaurantTypeTag = tag.category === 'MENUTYPE';
  const index = selectedTags.value.findIndex((st) => st.tagId === tag.tagId);
  const isCurrentlySelected = index > -1;

  if (isRestaurantTypeTag) {
    if (isCurrentlySelected) {
      // 이미 선택된 MENUTYPE 태그를 클릭 -> 선택 해제
      selectedTags.value = selectedTags.value.filter(
        (st) => st.tagId !== tag.tagId
      );
    } else {
      // 새로운 MENUTYPE 태그를 클릭 -> 기존 MENUTYPE 태그를 모두 제거하고 새 태그만 추가
      const otherTags = selectedTags.value.filter(
        (st) => st.category !== 'MENUTYPE'
      );
      selectedTags.value = [...otherTags, tag];
    }
  } else {
    // 다른 카테고리의 태그 (다중 선택)
    if (isCurrentlySelected) {
      // 선택 해제
      selectedTags.value = selectedTags.value.filter(
        (st) => st.tagId !== tag.tagId
      );
    } else {
      // 선택
      selectedTags.value = [...selectedTags.value, tag];
    }
  }
};

const isTagSelected = (tag) => {
  return selectedTags.value.some((st) => st.tagId === tag.tagId);
};

// API: GET /api/tags/search
const fetchTags = async () => {
  try {
    const response = await httpRequest.get('/api/tags/search');
    return response.data; // axios는 응답 데이터를 response.data로 제공합니다.
  } catch (error) {
    console.error('Failed to fetch tags:', error);
    // 에러 발생 시 빈 객체 반환 또는 에러 다시 던지기
    return {};
  }
};

onMounted(async () => {
  tagCategories.value = await fetchTags();

  if (isEditMode.value) {
    const restaurantId = Number(route.params.id);
    // 스토어에 정보가 없거나 다른 식당 정보가 들어있으면 새로 API 호출
    if (!store.restaurantInfo || store.restaurantInfo.restaurantId !== restaurantId) {
      try {
        await store.fetchRestaurantDetail(restaurantId);
      } catch (error) {
        if (error.response && (error.response.status === 403 || error.response.status === 404)) {
          alert('해당 식당 정보에 접근할 권한이 없습니다.');
          router.push('/business/dashboard');
        } else {
          alert('식당 정보를 불러오는 중 오류가 발생했습니다.');
          router.push('/business/dashboard');
        }
        return; // Exit onMounted if error, to prevent further rendering with invalid data
      }
    }
  }

  // '등록' 및 '수정' 모드 모두, 라우터 가드에서 스토어 상태를 이미 올바르게 설정했으므로,
  // 컴포넌트는 스토어의 데이터를 로컬 상태와 동기화하기만 하면 됩니다.
  if (store.restaurantInfo) {
    Object.assign(formData, store.restaurantInfo);
    selectedClosedDays.value = store.restaurantInfo.regularHolidays?.map(h => h.dayOfWeek) ?? [];
    selectedTags.value = store.restaurantInfo.tags ?? [];
    if (store.restaurantInfo.images?.length > 0) {
      restaurantImageFile.value = new File([], 'mock-restaurant-image.png');
    } else {
      restaurantImageFile.value = null;
    }
  }
});

const uploadRestaurantImage = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await httpRequest.post(
    '/api/v1/images/upload/restaurants',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  );
  const data = response.data?.data ?? response.data;
  return data?.fileUrl || data?.key || null;
};

const fetchRestaurantImages = async (restaurantId) => {
  const response = await httpRequest.get(
    `/api/business/restaurants/${restaurantId}/images`
  );
  return response.data ?? [];
};

const deleteRestaurantImages = async (restaurantId, images) => {
  if (!images.length) return;
  await Promise.all(
    images.map((image) =>
      httpRequest.delete(
        `/api/business/restaurants/${restaurantId}/images/${image.restaurantImageId}`
      )
    )
  );
};

const saveRestaurantImage = async (restaurantId, imageUrl) => {
  await httpRequest.post(
    `/api/business/restaurants/${restaurantId}/images`,
    { imageUrl }
  );
};

const syncRestaurantImage = async (restaurantId, { isNewRestaurant } = {}) => {
  const imageFile = restaurantImageFile.value;
  const hasNewImageFile = Boolean(imageFile && imageFile.size > 0);
  const hasImageUrl = Boolean(restaurantImageUrl.value);

  if (!hasNewImageFile && !hasImageUrl) {
    if (!isNewRestaurant) {
      const existingImages = await fetchRestaurantImages(restaurantId);
      await deleteRestaurantImages(restaurantId, existingImages);
    }
    return;
  }

  if (!hasNewImageFile) {
    return;
  }

  const uploadedUrl = await uploadRestaurantImage(imageFile);
  if (!uploadedUrl) {
    throw new Error('이미지 업로드 실패');
  }

  if (!isNewRestaurant) {
    const existingImages = await fetchRestaurantImages(restaurantId);
    await deleteRestaurantImages(restaurantId, existingImages);
  }

  await saveRestaurantImage(restaurantId, uploadedUrl);
  store.setDraftImageUrl(uploadedUrl);
  restaurantImageFile.value = new File([], 'mock-restaurant-image.png');
};

const saveRestaurant = async () => {
  // 1. Reset previous errors
  for (const key in validationErrors) {
    validationErrors[key] = '';
  }

  // 2. Validate fields
  let isValid = true;
  // // 식당이미지 유효성 검사 (백엔드 로직 미구현으로 임시 주석 처리)
  // if (!restaurantImageFile.value && !restaurantImageUrl.value) { // Ensure image exists for both new and existing
  //   validationErrors.image = '식당 이미지를 등록해주세요.';
  //   isValid = false;
  // }
  if (!formData.name.trim()) {
    validationErrors.name = '식당 이름을 입력해주세요.';
    isValid = false;
  }
  if (!formData.phone.trim()) {
    validationErrors.phone = '식당 전화번호를 입력해주세요.';
    isValid = false;
  } else {
    // 유효한 접두사와 전화번호 형식을 모두 검사하는 정규식
    const phoneRegex =
      /^(02|010|011|01[6-9]|03[1-3]|04[1-4]|05[1-5]|06[1-4]|070|080|050[0-9]|050)-\d{3,4}-\d{4}$/;
    if (!phoneRegex.test(formData.phone)) {
      validationErrors.phone =
        '유효하지 않은 전화번호 형식 또는 지역번호입니다.';
      isValid = false;
    }
  }
  if (!formData.openDate) {
    validationErrors.openDate = '개업일을 입력해주세요.';
    isValid = false;
  }
  if (!formData.openTime) {
    validationErrors.openTime = '영업시작시간을 입력해주세요.';
    isValid = false;
  }
  if (!formData.closeTime) {
    validationErrors.closeTime = '영업종료시간을 입력해주세요.';
    isValid = false;
  }
  if (!formData.reservationLimit || formData.reservationLimit < 4) {
    validationErrors.reservationLimit = '예약가능인원을 4인 이상 입력해주세요.';
    isValid = false;
  }
  if (!formData.roadAddress.trim()) {
    validationErrors.roadAddress = '도로명주소를 입력해주세요.';
    isValid = false;
  }
  if (!formData.detailAddress.trim()) {
    validationErrors.detailAddress = '상세주소를 입력해주세요.';
    isValid = false;
  }
  if (!formData.description.trim()) {
    validationErrors.description = '식당 소개를 입력해주세요.';
    isValid = false;
  }

  const hasMainMenu = store.menus.some(
    (menu) => menu.category && menu.category.code === 'MAIN'
  );
  if (!hasMainMenu) {
    validationErrors.menus = '주메뉴를 1개 이상 등록해주세요.';
    isValid = false;
  }

  if (!isValid) {
    alert('필수 입력 항목을 모두 채워주세요.');
    return;
  }

  // 3. Prepare data for submission
  const dataToSubmit = {
    ...formData,
    regularHolidayNumbers: selectedClosedDays.value,
    selectedTagIds: selectedTags.value.map((tag) => tag.tagId),
    menus: store.menus, // Include menus from the store
    avgMainPrice: avgMainPrice.value, // Include calculated average main price
    imageUrls: restaurantImageUrl.value ? [restaurantImageUrl.value] : [], // Pass current image URL as a list
  };

  // API를 통해 등록/수정 요청을 보낼 부분
  try {
    const apiUrl = '/api/business/restaurants';
    let response;
    let restaurantId;

    if (isEditMode.value) {
      // 수정 모드일 경우: PUT 요청
      response = await httpRequest.put(`${apiUrl}/${route.params.id}`, dataToSubmit);
      restaurantId = Number(route.params.id);
    } else {
      // 등록 모드일 경우: POST 요청
      response = await httpRequest.post(apiUrl, dataToSubmit);
      restaurantId = response.data; // 백엔드에서 생성된 ID를 응답으로 받음
    }

    try {
      await syncRestaurantImage(restaurantId, { isNewRestaurant: !isEditMode.value });
    } catch (error) {
      console.error('이미지 저장 실패:', error);
      alert('이미지 저장에 실패했습니다.');
      return;
    }

    if (isEditMode.value) {
      alert('식당 정보가 성공적으로 수정되었습니다.');
      router.push(`/business/restaurant-info/${restaurantId}`);
    } else {
      alert('식당 정보가 성공적으로 등록되었습니다.');
      router.push(`/business/restaurant-info/${restaurantId}`);
    }
  } catch (error) {
    console.error('저장 실패:', error);
    alert('저장에 실패했습니다.');
    // 에러 상세 메시지가 있다면 사용자에게 보여줄 수 있음
    if (error.response && error.response.data && error.response.data.message) {
      alert(`에러: ${error.response.data.message}`);
    }
  }
};

const handleBulkDelete = () => {
  if (filteredMenus.value.length === 0) {
    alert('삭제할 메뉴가 없습니다. 먼저 메뉴를 필터링해주세요.');
    return;
  }

  const confirmDelete = confirm(
    `필터링된 ${filteredMenus.value.length}개의 식당메뉴를 정말로 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`
  );

  if (confirmDelete) {
    const idsToDelete = filteredMenus.value.map((menu) => menu.id);
    const restaurantId = store.restaurantInfo?.restaurantId;

    if (restaurantId) {
      Promise.all(
        idsToDelete.map((menuId) =>
          store.deleteMenuForRestaurant(restaurantId, menuId)
        )
      ).catch((error) => {
        console.error('메뉴 삭제 실패:', error);
        alert('메뉴 삭제에 실패했습니다.');
      });
    } else {
      store.deleteMenus(idsToDelete); // 스토어 액션 호출
    }
  }
};

const deleteMenuItem = async (menuId) => {
  const restaurantId = store.restaurantInfo?.restaurantId;
  if (restaurantId) {
    try {
      await store.deleteMenuForRestaurant(restaurantId, menuId);
    } catch (error) {
      console.error('메뉴 삭제 실패:', error);
      alert('메뉴 삭제에 실패했습니다.');
    }
  } else {
    store.deleteMenu(menuId);
  }
};

const validationErrors = reactive({
  image: '',
  name: '',
  phone: '',
  openDate: '',
  openTime: '',
  closeTime: '',
  reservationLimit: '',
  roadAddress: '',
  detailAddress: '',
  description: '',
  menus: '',
});

watch(formData, (newState) => {
  if (newState.name) validationErrors.name = '';
  if (newState.phone) validationErrors.phone = '';
  if (newState.openDate) validationErrors.openDate = '';
  if (newState.openTime) validationErrors.openTime = '';
  if (newState.closeTime) validationErrors.closeTime = '';
  if (newState.reservationLimit >= 4) validationErrors.reservationLimit = '';
  if (newState.roadAddress) validationErrors.roadAddress = '';
  if (newState.detailAddress) validationErrors.detailAddress = '';
  if (newState.description) validationErrors.description = '';
});

// formData의 변경을 감지하여 Pinia store에 동기화
watch(
  formData,
  (newData) => {
    // store.restaurantInfo가 존재할 때만 동기화
    if (store.restaurantInfo) {
      Object.assign(store.restaurantInfo, newData);
    }
  },
  { deep: true }
);

watch(restaurantImageFile, (newFile) => {
  if (newFile) validationErrors.image = '';
});

watch(
  () => store.menus,
  (newMenus) => {
    if (newMenus.some((menu) => menu.category && menu.category.code === 'MAIN')) {
      validationErrors.menus = '';
    }
  },
  { deep: true }
);

// avgMainPrice computed 속성의 변화를 감지하여 store에 동기화
watch(avgMainPrice, (newAvg) => {
  if(store.restaurantInfo) {
    store.restaurantInfo.avgMainPrice = newAvg;
  }
});

// 현재 페이지의 메뉴가 모두 삭제되었을 때 이전 페이지로 이동
watch(paginatedMenus, (newPaginatedMenus) => {
  if (newPaginatedMenus.length === 0 && currentPage.value > 1) {
    currentPage.value--;
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
        <div class="max-w-4xl mx-auto space-y-8">
          <!-- Page Title -->
          <h2 class="text-3xl font-bold text-[#1e3a5f]">{{ pageTitle }}</h2>

          <!-- Restaurant Basic Info Card -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              식당 기본 정보
            </h3>

            <!-- Restaurant Image Upload -->
            <div class="mb-8">
              <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                >식당이미지</label
              >
              <div
                @click="triggerRestaurantFileInput"
                class="border-2 border-[#e9ecef] rounded-xl overflow-hidden bg-[#f8f9fa] relative cursor-pointer"
              >
                <div class="aspect-[2/1] flex items-center justify-center">
                  <div v-if="restaurantImageUrl" class="w-full h-full">
                    <img
                      :src="restaurantImageUrl"
                      alt="식당 이미지 미리보기"
                      class="w-full h-full object-cover rounded-lg"
                    />
                    <button
                      @click.stop="clearRestaurantImage"
                      class="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
                    >
                      <X class="w-4 h-4" />
                    </button>
                  </div>
                  <div v-else class="text-center">
                    <Upload class="w-12 h-12 text-[#6c757d] mx-auto mb-3" />
                    <p class="text-sm text-[#6c757d]">이미지를 업로드하세요</p>
                  </div>
                </div>
              </div>
              <input
                type="file"
                ref="restaurantFileInput"
                @change="handleRestaurantFileChange"
                class="hidden"
                accept="image/*"
              />
              <p
                v-if="validationErrors.image"
                class="text-red-500 text-sm mt-1"
              >
                {{ validationErrors.image }}
              </p>
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
                    placeholder="식당 이름을 입력하세요"
                    v-model="formData.name"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                  <p
                    v-if="validationErrors.name"
                    class="text-red-500 text-sm mt-1"
                  >
                    {{ validationErrors.name }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >식당전화번호</label
                  >
                  <input
                    type="tel"
                    placeholder="하이픈(-)을 포함하여 입력하세요"
                    v-model="formData.phone"
                    maxlength="15"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                  <p
                    v-if="validationErrors.phone"
                    class="text-red-500 text-sm mt-1"
                  >
                    {{ validationErrors.phone }}
                  </p>
                </div>
              </div>

              <!-- Opening Date -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >개업일</label
                >
                <input
                  type="date"
                  v-model="formData.openDate"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
                <p
                  v-if="validationErrors.openDate"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.openDate }}
                </p>
              </div>

              <!-- Business Hours -->
              <div class="grid grid-cols-2 gap-6">
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업시작시간</label
                  >
                  <input
                    type="time"
                    v-model="formData.openTime"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                  <p
                    v-if="validationErrors.openTime"
                    class="text-red-500 text-sm mt-1"
                  >
                    {{ validationErrors.openTime }}
                  </p>
                </div>
                <div>
                  <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                    >영업종료시간</label
                  >
                  <input
                    type="time"
                    v-model="formData.closeTime"
                    class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                  />
                  <p
                    v-if="validationErrors.closeTime"
                    class="text-red-500 text-sm mt-1"
                  >
                    {{ validationErrors.closeTime }}
                  </p>
                </div>
              </div>

              <!-- Reservation Limit -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >예약가능인원 상한</label
                >
                <input
                  type="number"
                  placeholder="숫자를 입력하세요"
                  v-model.number="formData.reservationLimit"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
                <p
                  v-if="validationErrors.reservationLimit"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.reservationLimit }}
                </p>
              </div>

              <!-- Address -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >도로명주소</label
                >
                <div class="flex gap-3">
                  <input
                    type="text"
                    placeholder="우측 '주소검색' 버튼을 클릭하세요"
                    v-model="formData.roadAddress"
                    readonly
                    class="flex-1 px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] bg-[#f8f9fa]"
                  />
                  <button
                    @click="openPostcodeSearch"
                    type="button"
                    class="px-6 py-3 border border-[#dee2e6] rounded-lg text-[#1e3a5f] hover:bg-[#f8f9fa] transition-colors"
                  >
                    주소검색
                  </button>
                </div>
                <p
                  v-if="validationErrors.roadAddress"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.roadAddress }}
                </p>
              </div>

              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >상세주소</label
                >
                <input
                  type="text"
                  placeholder="상세주소를 입력하세요"
                  v-model="formData.detailAddress"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
                <p
                  v-if="validationErrors.detailAddress"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.detailAddress }}
                </p>
              </div>

              <!-- Checkboxes -->
              <div class="grid grid-cols-2 gap-6">
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="holiday"
                    v-model="formData.holidayAvailable"
                    class="w-5 h-5 rounded border-[#dee2e6]"
                  />
                  <label for="holiday" class="text-sm text-[#1e3a5f]">
                    공휴일 운영
                  </label>
                </div>
                <div class="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="preorder"
                    v-model="formData.preorderAvailable"
                    class="w-5 h-5 rounded border-[#dee2e6]"
                  />
                  <label for="preorder" class="text-sm text-[#1e3a5f]">
                    선주문/선결제 가능
                  </label>
                </div>
              </div>

              <!-- Regular Closed Days -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
                  정기휴무일
                </label>
                <div class="flex flex-wrap gap-3">
                  <button
                    v-for="dayString in [
                      '일',
                      '월',
                      '화',
                      '수',
                      '목',
                      '금',
                      '토'
                    ]"
                    :key="dayString"
                    @click="toggleClosedDay(dayString)"
                    :class="`px-4 py-2 rounded-lg border transition-colors ${
                      isDaySelected(dayString)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ dayString }}
                  </button>
                </div>
              </div>

              <!-- Restaurant Description -->
              <div>
                <label
                  class="block text-sm font-semibold text-[#1e3a5f] mb-2 mt-4"
                  >식당 소개</label
                >
                <textarea
                  placeholder="식당 소개글 입력창"
                  v-model="formData.description"
                  rows="5"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] resize-none"
                ></textarea>
                <p
                  v-if="validationErrors.description"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.description }}
                </p>
              </div>
            </div>
          </div>

          <!-- Restaurant Tags -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <h3 class="text-xl font-bold text-[#1e3a5f] mb-6">
              식당 태그 선택
            </h3>

            <!-- Characteristics Tags -->
            <div
              v-for="(displayName, categoryName) in tagCategoryDisplayNames"
              :key="categoryName"
              class="mb-6"
            >
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-3">
                {{ displayName }}
              </h4>
              <div class="flex flex-wrap gap-3">
                <button
                  v-for="tag in tagCategories[categoryName]"
                  :key="tag.tagId"
                  @click="toggleTag(tag)"
                  :class="`px-4 py-2 rounded-lg border transition-colors ${
                    isTagSelected(tag)
                      ? 'gradient-primary text-white border-transparent'
                      : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                  }`"
                >
                  #{{ tag.content }}
                </button>
              </div>
            </div>
          </div>

          <!-- Menu Management Section -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <div class="flex items-center justify-between mb-6">
              <h3 class="text-xl font-bold text-[#1e3a5f]">식당메뉴</h3>
              <div class="flex items-center gap-4">
                <!-- 메뉴 타입 필터 드롭다운 -->
                <select
                  v-model="selectedMenuType"
                  class="px-3 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] text-sm"
                >
                  <option v-for="type in menuTypes" :key="type" :value="type">
                    {{ type }}
                  </option>
                </select>

                <!-- 메뉴 이름 검색창 -->
                <input
                  type="text"
                  v-model="menuSearchKeyword"
                  placeholder="메뉴 이름으로 검색"
                  class="w-48 px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] text-sm"
                />

                <!-- 정렬 드롭다운 -->
                <select
                  v-model="selectedSort"
                  class="px-3 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] text-sm"
                >
                  <option v-for="opt in sortOptions" :key="opt" :value="opt">
                    {{ opt }}
                  </option>
                </select>
              </div>
              <span
                class="text-base font-semibold text-[#FF6B4A] whitespace-nowrap"
              >
                주메뉴 평균가: {{ avgMainPrice.toLocaleString() }}원
              </span>
            </div>
            <p
              v-if="validationErrors.menus"
              class="text-red-500 text-sm mb-4 text-center"
            >
              {{ validationErrors.menus }}
            </p>

            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#dee2e6]">
                  <tr>
                    <th
                      class="px-12 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      메뉴이름
                    </th>
                    <th
                      class="px-12 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      메뉴타입
                    </th>
                    <th
                      class="px-12 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      가격
                    </th>
                    <th
                      class="px-4 py-3 text-left text-sm font-semibold text-[#1e3a5f]"
                    >
                      수정/삭제
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="menu in paginatedMenus"
                    :key="menu.id"
                    class="border-b border-[#e9ecef]"
                  >
                    <td class="px-12 py-4 text-sm text-[#1e3a5f]">
                      {{ menu.name }}
                    </td>
                    <td class="px-12 py-4 text-sm text-[#6c757d]">
                      {{ menu.category.value }}
                    </td>
                    <td class="px-12 py-4 text-sm text-[#6c757d]">
                      {{ menu.price.toLocaleString() }}원
                    </td>
                    <td class="px-4 py-4 text-left">
                      <div class="flex justify-start gap-2">
                        <RouterLink
                          :to="`/business/restaurant-info/menu/edit/${menu.id}`"
                          class="px-3 py-2 border border-[#dee2e6] rounded-lg text-[#1e3a5f] text-sm hover:bg-[#f8f9fa] transition-colors"
                        >
                          수정
                        </RouterLink>
                        <button
                          @click="deleteMenuItem(menu.id)"
                          class="px-3 py-2 border border-[#dc3545] text-[#dc3545] rounded-lg text-sm hover:bg-[#fff5f5] transition-colors"
                        >
                          삭제
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 페이지네이션 및 버튼 그룹 -->
            <div class="flex items-center justify-between mt-6">
              <Pagination
                :current-page="currentPage"
                :total-pages="totalPages"
                @change-page="changePage"
              />
              <div class="flex items-center gap-3">
                <button
                  @click="handleBulkDelete"
                  class="px-6 py-3 border border-[#dc3545] text-[#dc3545] rounded-lg font-semibold hover:bg-[#fff5f5] transition-colors"
                >
                  일괄 삭제
                </button>
                <RouterLink
                  to="/business/restaurant-info/menu/add"
                  class="px-6 py-3 gradient-primary text-white rounded-lg font-semibold hover:opacity-90 transition-opacity"
                >
                  메뉴 추가
                </RouterLink>
              </div>
            </div>
          </div>

          <!-- Save Button -->
          <div class="flex justify-end">
            <button
              @click="saveRestaurant"
              class="px-8 py-3 gradient-primary text-white rounded-xl font-semibold hover:opacity-90 transition-opacity"
            >
              {{ submitButtonText }}
            </button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
