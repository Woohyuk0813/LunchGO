<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { Upload, X } from 'lucide-vue-next';
import { useRouter, useRoute } from 'vue-router';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import { useRestaurantStore } from '@/stores/restaurant';
import httpRequest from "@/router/httpRequest.js";

// 1. 라우터, 라우트, 스토어
const router = useRouter();
const route = useRoute();
const store = useRestaurantStore();

// 2. 모드 및 페이지 제목
const isEditMode = computed(() => !!route.params.id);
const pageTitle = computed(() =>
  isEditMode.value ? '식당 메뉴 정보 편집' : '식당 메뉴 정보 추가'
);

// 3. 상태 관련 변수
const imageFile = ref(null);
const imageUrl = ref(null);
const fileInput = ref(null);
const menuData = reactive({
  id: null,
  name: '',
  category: '',
  price: 0,
  description: '', // 메뉴 설명 추가
  tags: [], // 재료 태그는 메뉴 객체에 포함하여 관리
  imageUrl: '', // 이미지 URL도 메뉴 객체에 포함
});

// 4. 데이터 변수
const allIngredientTags = ref([]);
const menuTypes = ref([
  { value: 'MAIN', text: '주메뉴' },
  { value: 'SUB', text: '서브메뉴' },
  { value: 'OTHER', text: '기타(디저트, 음료)' },
]);

// 5. 함수
const handleFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    imageFile.value = file;
    menuData.imageUrl = URL.createObjectURL(file);
    imageUrl.value = menuData.imageUrl;
  }
};

const triggerFileInput = () => {
  fileInput.value.click();
};

const clearImage = () => {
  imageFile.value = null;
  imageUrl.value = null;
  menuData.imageUrl = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const toggleIngredientTag = (tag) => {
  const index = menuData.tags.findIndex((t) => t.tagId === tag.tagId);
  if (index > -1) {
    menuData.tags.splice(index, 1);
  } else {
    menuData.tags.push(tag);
  }
};

const isTagSelected = (tag) => {
  return menuData.tags.some((t) => t.tagId === tag.tagId);
};

const uploadMenuImage = async (file) => {
  const formData = new FormData();
  formData.append('file', file);
  const response = await httpRequest.post(
    '/api/v1/images/upload/menus',
    formData,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  );
  const data = response.data?.data ?? response.data;
  return data?.fileUrl || data?.key || null;
};

const fetchMenuImages = async (restaurantId, menuId) => {
  const response = await httpRequest.get(
    `/api/business/restaurants/${restaurantId}/menus/${menuId}/images`
  );
  return response.data ?? [];
};

const deleteMenuImages = async (restaurantId, menuId, images) => {
  if (!images.length) return;
  await Promise.all(
    images.map((image) =>
      httpRequest.delete(
        `/api/business/restaurants/${restaurantId}/menus/${menuId}/images/${image.menuImageId}`
      )
    )
  );
};

const saveMenuImage = async (restaurantId, menuId, imageUrl) => {
  await httpRequest.post(
    `/api/business/restaurants/${restaurantId}/menus/${menuId}/images`,
    { imageUrl }
  );
};

const syncMenuImage = async (restaurantId, menuId) => {
  const hasNewImageFile = Boolean(imageFile.value && imageFile.value.size > 0);
  const hasImageUrl = Boolean(menuData.imageUrl);

  if (!hasNewImageFile && !hasImageUrl) {
    const existingImages = await fetchMenuImages(restaurantId, menuId);
    await deleteMenuImages(restaurantId, menuId, existingImages);
    return null;
  }

  if (!hasNewImageFile) {
    return menuData.imageUrl || null;
  }

  const uploadedUrl = await uploadMenuImage(imageFile.value);
  if (!uploadedUrl) {
    throw new Error('이미지 업로드 실패');
  }

  const existingImages = await fetchMenuImages(restaurantId, menuId);
  await deleteMenuImages(restaurantId, menuId, existingImages);
  await saveMenuImage(restaurantId, menuId, uploadedUrl);
  return uploadedUrl;
};

// 6. 라이프사이클 훅
const fetchIngredientTags = async () => {
  try {
    const response = await httpRequest.get('/api/tags/search', {
      params: { categories: 'INGREDIENT' },
    });
    // API는 { "INGREDIENT": [...] } 와 같은 객체를 반환하므로 배열을 추출합니다.
    return response.data.INGREDIENT || [];
  } catch (error) {
    console.error('Failed to fetch ingredient tags:', error);
    return [];
  }
};

// 추후 API로부터 데이터를 받아오는 로직을 처리할 함수
onMounted(async () => {
  // 1. (모든 메뉴 공통) 재료 태그 목록 가져오기
  allIngredientTags.value = await fetchIngredientTags();

  if (isEditMode.value) {
    // 2. (수정 모드일 경우) 기존 메뉴 정보 가져오기
    const menuId = route.params.id;
    const existingMenu = store.getMenuById(menuId);
    if (existingMenu) {
      // 스토어에서 찾은 메뉴로 데이터 채우기
      menuData.id = existingMenu.id;
      menuData.name = existingMenu.name;

      // 'category' 객체에서 'code'를 사용해 v-model과 바인딩
      menuData.category = existingMenu.category?.code || '';
      menuData.price = existingMenu.price;
      menuData.description = existingMenu.description || ''; // description 추가
      menuData.tags = existingMenu.tags || []; // 태그가 없을 수 있으므로 기본값 설정
      menuData.imageUrl = existingMenu.imageUrl || ''; // 이미지 URL이 없을 수 있음
      imageUrl.value = menuData.imageUrl;
    }
  }
});

const validationErrors = reactive({
  image: '',
  name: '',
  category: '',
  price: '',
  description: '', // 메뉴 설명 유효성 검사 에러 필드 추가
});

// 7. 저장 함수
const saveMenu = async () => {
  // 에러 초기화
  for (const key in validationErrors) {
    validationErrors[key] = '';
  }

  let isValid = true;
  // // 메뉴 이미지 유효성 검사 (백엔드 로직 미구현으로 임시 주석 처리)
  // // menuData.imageUrl이 이미 존재하면 이미지 파일 필수 아님 (수정 모드)
  // if (!imageFile.value && !menuData.imageUrl) {
  //   validationErrors.image = '메뉴 사진을 등록해주세요.';
  //   isValid = false;
  // }
  if (!menuData.name.trim()) {
    validationErrors.name = '메뉴 이름을 입력해주세요.';
    isValid = false;
  }
  if (!menuData.category) {
    validationErrors.category = '메뉴 타입을 선택해주세요.';
    isValid = false;
  }
  if (
    menuData.price === null ||
    menuData.price === undefined ||
    menuData.price <= 0
  ) {
    validationErrors.price = '가격을 1 이상 입력해주세요.';
    isValid = false;
  }

  if (!isValid) {
    alert('필수 입력 항목을 모두 채워주세요.');
    return;
  }

  const selectedCategoryObject =
    menuTypes.value.find((mt) => mt.value === menuData.category) || {};
  const menuToSave = {
    id: menuData.id,
    name: menuData.name,
    category: {
      code: selectedCategoryObject.value,
      value: selectedCategoryObject.text,
    },
    price: menuData.price,
    description: menuData.description, // 메뉴 설명 추가
    tags: menuData.tags,
    imageUrl: menuData.imageUrl,
  };

  const restaurantId = store.restaurantInfo?.restaurantId;

  try {
    if (restaurantId) {
      let savedMenu = null;
      if (isEditMode.value) {
        savedMenu = await store.updateMenuForRestaurant(
          restaurantId,
          menuData.id,
          menuToSave
        );
        alert('메뉴가 수정되었습니다.');
      } else {
        menuToSave.id = null;
        savedMenu = await store.createMenuForRestaurant(restaurantId, menuToSave);
        alert('메뉴가 추가되었습니다.');
      }
      const menuId = savedMenu?.id ?? menuData.id;
      const shouldSyncImage = Boolean(
        menuId && (imageFile.value || (isEditMode.value && !menuData.imageUrl))
      );
      if (shouldSyncImage) {
        const syncedImageUrl = await syncMenuImage(restaurantId, menuId);
        menuData.imageUrl = syncedImageUrl || '';
        if (savedMenu) {
          store.updateMenu({ ...savedMenu, imageUrl: menuData.imageUrl });
        }
      }
    } else {
      if (isEditMode.value) {
        store.updateMenu(menuToSave);
        alert('메뉴가 수정되었습니다.');
      } else {
        menuToSave.id = store.getNextId();
        store.addMenu(menuToSave);
        alert('메뉴가 추가되었습니다.');
      }
    }
    router.back();
  } catch (error) {
    console.error('메뉴 저장 실패:', error);
    alert('메뉴 저장에 실패했습니다.');
  }
};

// Watchers for clearing errors
watch(
  () => menuData.name,
  (newValue) => {
    if (newValue.trim()) validationErrors.name = '';
  }
);
watch(
  () => menuData.category,
  (newValue) => {
    if (newValue) validationErrors.category = '';
  }
);
watch(
  () => menuData.price,
  (newValue) => {
    if (newValue > 0) validationErrors.price = '';
  }
);
watch(imageFile, (newFile) => {
  if (newFile) validationErrors.image = '';
});
watch(
  () => menuData.imageUrl,
  (newUrl) => {
    if (newUrl) validationErrors.image = '';
  }
);
watch(
  () => menuData.description, // description 필드에 대한 watch 추가
  (newValue) => {
    if (newValue.trim()) validationErrors.description = '';
  }
);
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="restaurant-info" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-2xl mx-auto">
          <!-- Page Title with Close Button -->
          <div class="flex items-center justify-between mb-8">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">
              {{ pageTitle }}
            </h2>
            <button
              @click="router.back()"
              class="p-2 hover:bg-[#f8f9fa] rounded-lg transition-colors"
            >
              <X class="w-6 h-6 text-[#6c757d]" />
            </button>
          </div>

          <!-- Menu Form Card -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-8">
            <div class="space-y-6">
              <!-- Menu Image Upload -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴사진</label
                >
                <div
                  @click="triggerFileInput"
                  class="border-2 border-[#e9ecef] rounded-xl overflow-hidden bg-[#f8f9fa] relative cursor-pointer"
                >
                  <div class="aspect-[2/1] flex items-center justify-center">
                    <div v-if="imageUrl" class="w-full h-full">
                      <img
                        :src="imageUrl"
                        alt="메뉴 이미지 미리보기"
                        class="w-full h-full object-cover rounded-lg"
                      />
                      <button
                        @click.stop="clearImage"
                        class="absolute top-2 right-2 p-1 bg-red-500 text-white rounded-full hover:bg-red-600 transition-colors"
                      >
                        <X class="w-4 h-4" />
                      </button>
                    </div>
                    <div v-else class="text-center">
                      <Upload class="w-12 h-12 text-[#6c757d] mx-auto mb-3" />
                      <p class="text-sm text-[#6c757d]">
                        이미지를 업로드하세요
                      </p>
                    </div>
                  </div>
                </div>
                <input
                  type="file"
                  ref="fileInput"
                  @change="handleFileChange"
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

              <!-- Menu Name -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴이름</label
                >
                <input
                  type="text"
                  placeholder="메뉴이름 입력"
                  v-model="menuData.name"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
                <p
                  v-if="validationErrors.name"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.name }}
                </p>
              </div>

              <!-- Menu Type -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴타입</label
                >
                <select
                  v-model="menuData.category"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                >
                  <option disabled value="">메뉴 타입 선택</option>
                  <option
                    v-for="typeOption in menuTypes"
                    :key="typeOption.value"
                    :value="typeOption.value"
                  >
                    {{ typeOption.text }}
                  </option>
                </select>
                <p
                  v-if="validationErrors.category"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.category }}
                </p>
              </div>

              <!-- Menu Price -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >가격</label
                >
                <input
                  type="number"
                  placeholder="가격 입력"
                  v-model="menuData.price"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A]"
                />
                <p
                  v-if="validationErrors.price"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.price }}
                </p>
              </div>

              <!-- Menu Description -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-2"
                  >메뉴 설명</label
                >
                <textarea
                  placeholder="메뉴 설명을 입력하세요"
                  v-model="menuData.description"
                  rows="4"
                  class="w-full px-4 py-3 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] resize-none"
                ></textarea>
                <p
                  v-if="validationErrors.description"
                  class="text-red-500 text-sm mt-1"
                >
                  {{ validationErrors.description }}
                </p>
              </div>

              <!-- Ingredient Tags -->
              <div>
                <label class="block text-sm font-semibold text-[#1e3a5f] mb-3">
                  재료 특이사항
                </label>
                <div class="flex flex-wrap gap-3">
                  <button
                    v-for="tag in allIngredientTags"
                    :key="tag.tagId"
                    @click="toggleIngredientTag(tag)"
                    :class="`px-4 py-2 rounded-lg border transition-colors ${
                      isTagSelected(tag)
                        ? 'gradient-primary text-white border-transparent'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa]'
                    }`"
                  >
                    {{ tag.content }}
                  </button>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex justify-end gap-3 mt-8">
              <button
                @click="router.back()"
                class="px-8 py-3 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
              >
                취소
              </button>
              <button
                @click="saveMenu"
                class="px-8 py-3 gradient-primary text-white rounded-lg font-semibold hover:opacity-90 transition-opacity"
              >
                저장
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
select {
  -webkit-appearance: none; /* Safari and Chrome */
  -moz-appearance: none; /* Firefox */
  appearance: none; /* Other modern browsers */
  background-image: url("data:image/svg+xml;charset=UTF-8,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 20 20' fill='none' stroke='%236c757d' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 1.25em auto;
  padding-right: 2.5rem; /* 화살표 공간 확보 */
}
</style>
