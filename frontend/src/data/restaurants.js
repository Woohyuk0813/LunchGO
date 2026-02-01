import { reactive } from "vue";
import { fetchRestaurants } from "@/services/restaurantService";

// 반응형 배열 선언
export const restaurants = reactive([]);

// 비동기 데이터 로딩 함수 (외부에서 명시적 호출)
export const loadRestaurants = async () => {
  if (restaurants.length > 0) return; // 이미 데이터가 있으면 스킵 (선택 사항)
  
  const data = await fetchRestaurants();
  restaurants.splice(0, restaurants.length, ...data);
};

export const getRestaurantById = (id) =>
  restaurants.find((restaurant) => restaurant.id === String(id));
