import { defineStore } from 'pinia';
import { ref } from 'vue';
import httpRequest from "@/router/httpRequest.js"; // axios 임포트

// 새 식당 정보의 기본 구조
const defaultRestaurant = () => ({
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
  images: [],
  regularHolidays: [],
  tags: [],
});

export const useRestaurantStore = defineStore('restaurant', () => {
  // State
  const restaurantInfo = ref(null); // 현재 편집 중인 식당 정보 전체
  const menus = ref([]);

  // Actions
  /**
   * 새 식당 등록을 위해 스토어를 초기화합니다.
   */
  function initializeNewRestaurant() {
    restaurantInfo.value = defaultRestaurant();
    menus.value = [];
  }

  /**
   * 수정 모드에서 식당 데이터를 로드합니다.
   * 스토어에 이미 같은 식당 데이터가 로드되어 있다면 아무것도 하지 않습니다.
   * @param {object} data - API로부터 받은 식당 데이터
   */
  function loadRestaurant(data) {
    if (restaurantInfo.value && restaurantInfo.value.restaurantId === data.restaurantId) {
      return;
    }
    // 새로운 식당 데이터를 로드합니다.
    restaurantInfo.value = data;
    menus.value = [...(data.menus || [])]; // data.menus가 없는 경우를 대비하여 || [] 추가
  }

  /**
   * 백엔드 API에서 식당 상세 정보를 가져와 스토어에 로드합니다.
   * @param {number} restaurantId - 조회할 식당의 ID
   */
  async function fetchRestaurantDetail(restaurantId) {
    try {
      const response = await httpRequest.get(`/api/business/restaurants/${restaurantId}`);
      loadRestaurant(response.data);
    } catch (error) {
      console.error('Failed to fetch restaurant details:', error);
      // 에러 처리 로직 추가 (예: 사용자에게 알림)
      throw error; // 에러를 호출자에게 다시 던져 페이지에서 처리하도록 함
    }
  }

  /**
   * 스토어를 초기 상태로 리셋합니다.
   */
  function clearRestaurant() {
    restaurantInfo.value = null;
    menus.value = [];
  }

  function addMenu(menu) {
    menus.value.push(menu);
  }

  function updateMenu(updatedMenu) {
    const index = menus.value.findIndex(menu => menu.id === updatedMenu.id);
    if (index !== -1) {
      menus.value[index] = updatedMenu;
    }
  }

  function deleteMenu(menuId) {
    menus.value = menus.value.filter(menu => menu.id !== menuId);
  }

  function deleteMenus(menuIds) {
    menus.value = menus.value.filter(menu => !menuIds.includes(menu.id));
  }

  function getMenuById(menuId) {
    const id = Number(menuId);
    return menus.value.find(menu => menu.id === id);
  }
  
  /**
   * 임시 클라이언트 측 ID를 생성합니다.
   * @returns {number} 새로운 메뉴 ID
   */
  function getNextId() {
    return Date.now();
  }

  function setDraftImageUrl(url) {
    if (!restaurantInfo.value) {
      restaurantInfo.value = { images: [] };
    }
    if (!restaurantInfo.value.images || restaurantInfo.value.images.length === 0) {
      restaurantInfo.value.images = [{ imageUrl: null }];
    }
    restaurantInfo.value.images[0].imageUrl = url;
  }

  async function createMenuForRestaurant(restaurantId, menu) {
    try {
      const response = await httpRequest.post(
        `/api/business/restaurants/${restaurantId}/menus`,
        menu
      );
      addMenu(response.data);
      return response.data;
    } catch (error) {
      console.error('Failed to create menu:', error);
      throw error;
    }
  }

  async function updateMenuForRestaurant(restaurantId, menuId, menu) {
    try {
      const response = await httpRequest.put(
        `/api/business/restaurants/${restaurantId}/menus/${menuId}`,
        menu
      );
      updateMenu(response.data);
      return response.data;
    } catch (error) {
      console.error('Failed to update menu:', error);
      throw error;
    }
  }

  async function deleteMenuForRestaurant(restaurantId, menuId) {
    try {
      await httpRequest.delete(
        `/api/business/restaurants/${restaurantId}/menus/${menuId}`
      );
      deleteMenu(menuId);
    } catch (error) {
      console.error('Failed to delete menu:', error);
      throw error;
    }
  }

  return { 
    restaurantInfo,
    menus, 
    initializeNewRestaurant,
    loadRestaurant,
    fetchRestaurantDetail, // 새로운 액션 추가
    clearRestaurant,
    setDraftImageUrl,
    addMenu, 
    updateMenu, 
    deleteMenu,
    createMenuForRestaurant,
    updateMenuForRestaurant,
    deleteMenuForRestaurant,
    deleteMenus,
    getMenuById,
    getNextId,
  };
});
