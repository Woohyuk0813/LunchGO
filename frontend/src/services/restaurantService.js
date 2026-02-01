import httpRequest from "@/router/httpRequest";

/**
 * 백엔드 API로부터 식당 목록을 조회하고,
 * 프론트엔드 컴포넌트(HomeView 등)에서 사용하는 포맷으로 변환하여 반환합니다.
 */
export const fetchRestaurants = async () => {
  try {
    const response = await httpRequest.get("/api/restaurants");
    const data = response.data;

    // 백엔드 DTO(RestaurantSummaryResponse)를 프론트엔드 포맷으로 변환
    return data.map(transformRestaurantData);
  } catch (error) {
    console.error("식당 목록을 불러오는 중 오류 발생:", error);
    return []; // 오류 발생 시 빈 배열 반환하여 UI 충돌 방지
  }
};

/**
 * 백엔드 데이터(RestaurantSummaryResponse)를 프론트엔드(restaurants.js 구조)에 맞게 변환합니다.
 * HomeView.vue의 로직을 그대로 사용하기 위한 어댑터 함수입니다.
 */
const transformRestaurantData = (backendData) => {
  return {
    id: String(backendData.id), // 프론트엔드에서는 id를 문자열로 다루는 경우가 많음
    name: backendData.name,
    category: backendData.category || "기타",
    rating: backendData.rating || 0,
    reviews: backendData.reviews || 0,
    
    // [중요] 백엔드에는 없는 필드이지만, 프론트엔드 로직 충돌 방지를 위해 기본값 설정
    topTags: [], 
    
    // [중요] 가격 포맷 변환 (Integer -> "XX,XXX원")
    price: backendData.price 
      ? `${backendData.price.toLocaleString()}원` 
      : "가격 정보 없음",
      
    // [중요] 배지 색상은 정적 데이터에만 있었으므로 기본값 처리
    badgeColor: "bg-slate-500", 
    
    image: backendData.image || "/placeholder.svg",
    
    // [중요] 필드명 매핑 (roadAddress + detailAddress -> address)
    address: [backendData.roadAddress, backendData.detailAddress]
      .filter(Boolean)
      .join(" "),
    
    // [중요] 좌표 구조 변환 (latitude, longitude -> coords: { lat, lng })
    coords: {
      lat: backendData.latitude ?? null,
      lng: backendData.longitude ?? null,
    },
    
    // 상세 정보 필드들은 목록 조회 시에는 없으므로 기본값 처리
    phone: "",
    hours: "",
    capacity: "",
    tagline: "",
    description: "",
    gallery: [],
    menus: [], // HomeView에서 가격 계산 시 menus를 참조하지만, 위에서 price를 이미 변환했으므로 빈 배열이어도 무방할 것으로 예상됨
  };
};
