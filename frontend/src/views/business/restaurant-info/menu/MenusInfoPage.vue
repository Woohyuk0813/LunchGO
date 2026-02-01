<script setup>
import { ref, computed, onMounted } from 'vue';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import { ArrowLeft } from 'lucide-vue-next';
import { useRouter } from 'vue-router';
import RestaurantMenuList from '@/components/ui/RestaurantMenuList.vue';
import httpRequest from "@/router/httpRequest.js";

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
});

const router = useRouter();

const goBack = () => {
  router.back();
};

const fetchedMenus = ref([]); // 백엔드에서 가져온 메뉴 데이터를 저장할 ref

const menuCategories = computed(() => {
  const categoriesMap = {
    MAIN: { id: 'MAIN', name: '주메뉴', items: [] },
    SUB: { id: 'SUB', name: '서브메뉴', items: [] },
    OTHER: { id: 'OTHER', name: '기타(디저트, 음료)', items: [] },
  };

  if (fetchedMenus.value && fetchedMenus.value.length > 0) {
    fetchedMenus.value.forEach(menu => {
      const categoryCode = menu.category?.code;
      if (categoryCode && categoriesMap[categoryCode]) {
        categoriesMap[categoryCode].items.push({
          id: menu.id,
          name: menu.name,
          price: menu.price ? `${menu.price.toLocaleString()}원` : '가격정보 없음',
          description: menu.description,
          image: menu.imageUrl || '/placeholder.svg',
        });
      }
    });
  }
  return Object.values(categoriesMap).filter(cat => cat.items.length > 0);
});

onMounted(async () => {
  try {
    const response = await httpRequest.get(`/api/restaurants/${props.id}/menus`);
    fetchedMenus.value = response.data;
  } catch (error) {
    console.error('Failed to fetch menus:', error);
    fetchedMenus.value = [];
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
          <!-- Page Title and Back Button -->
          <div class="flex items-center">
            <button @click="goBack" class="mr-4 p-2 rounded-full hover:bg-gray-200">
              <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
            </button>
            <h2 class="text-3xl font-bold text-[#1e3a5f]">전체 메뉴 보기</h2>
          </div>

          <!-- Menu List Section -->
          <section>
            <RestaurantMenuList :menu-categories="menuCategories" />
          </section>
        </div>
      </main>
    </div>
  </div>
</template>