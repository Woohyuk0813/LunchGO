<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { Star, Bell, Link2, UserPlus, RefreshCw } from 'lucide-vue-next';
import FavoriteHeart from '@/components/ui/FavoriteHeart.vue';
import { useBookmarkShare } from '@/composables/useBookmarkShare';

// 타입 정의
interface Restaurant {
  id: number;
  name: string;
  description: string;
  avgMainPrice: number | null;
  reservationLimit: number | null;
  promotionAgree: boolean;
  isFavorite: boolean;
  isPublic: boolean;
  imageUrl: string | null;
  rating: number;
  reviewCount: number;
}

interface LinkItem {
  linkId: number;
  requesterId: number;
  receiverId: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
  respondedAt: string | null;
  counterpartId: number;
  counterpartEmail: string;
  counterpartNickname: string | null;
  counterpartName: string;
  counterpartImage?: string | null;
}

interface SharedBookmark {
  restaurantId: number;
  name: string;
  roadAddress: string;
  detailAddress: string;
  imageUrl: string | null;
  rating: number;
  reviewCount: number;
}

interface UserSearchResult {
  userId: number;
  email: string;
  nickname: string | null;
  name: string;
  image?: string | null;
}

const props = defineProps<{ userId: number | null }>();
const router = useRouter();

const {
  searchUserByEmail,
  searchUsersByEmail,
  requestLink,
  respondLink,
  getSentLinks,
  getReceivedLinks,
  deleteLink,
  toggleBookmarkVisibility,
  togglePromotionAgree,
  getSharedBookmarks,
  getMyBookmarks,
} = useBookmarkShare();

// 상태 관리
const favorites = ref<Restaurant[]>([]);
const isLoading = ref(true); // 로딩 상태
const linkSearchEmail = ref('');
const searchResult = ref<UserSearchResult | null>(null);
const searchResults = ref<UserSearchResult[]>([]);
const searchError = ref('');
const linkRequestError = ref('');
const sentLinks = ref<LinkItem[]>([]);
const receivedLinks = ref<LinkItem[]>([]);
const isLinkLoading = ref(false);
const selectedSharedUserId = ref<number | null>(null);
const sharedBookmarks = ref<SharedBookmark[]>([]);
const isSharedLoading = ref(false);
const searchTimer = ref<ReturnType<typeof setTimeout> | null>(null);
const fallbackProfileImage = '/placeholder-user.jpg';

const resolveProfileImage = (image?: string | null) => {
  if (!image || !image.trim()) return fallbackProfileImage;
  return image;
};

// 1. [API 로직] 즐겨찾기 목록 불러오기
const fetchFavorites = async () => {
  try {
    isLoading.value = true;
    if (!props.userId) {
      favorites.value = [];
      return;
    }

    const response = await getMyBookmarks(props.userId);
    const data = Array.isArray(response.data) ? response.data : [];
    favorites.value = data.map((item) => ({
      id: item.restaurantId,
      name: item.name,
      description: item.description || '설명이 없습니다.',
      avgMainPrice: item.avgMainPrice ?? null,
      reservationLimit: item.reservationLimit ?? null,
      promotionAgree: Boolean(item.promotionAgree),
      isFavorite: true,
      isPublic: Boolean(item.isPublic),
      imageUrl: item.imageUrl || null,
      rating: Number(item.rating || 0),
      reviewCount: Number(item.reviewCount || 0),
    }));
  } catch (error) {
    console.error('데이터 로드 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

const fetchLinkLists = async () => {
  if (!props.userId) return;
  isLinkLoading.value = true;
  try {
    const [sentResponse, receivedResponse] = await Promise.all([
      getSentLinks(props.userId),
      getReceivedLinks(props.userId),
    ]);
    sentLinks.value = Array.isArray(sentResponse.data)
      ? sentResponse.data
      : [];
    receivedLinks.value = Array.isArray(receivedResponse.data)
      ? receivedResponse.data
      : [];
  } catch (error) {
    console.error('링크 목록 로드 실패:', error);
  } finally {
    isLinkLoading.value = false;
  }
};

// 컴포넌트 마운트 시 데이터 호출
onMounted(() => {
  fetchFavorites();
  fetchLinkLists();
});


//찜 해제
const handleRemoveFromList = (id:number) => {
  favorites.value = favorites.value.filter(item => item.id !== id);
};

// [프로모션 토글 핸들러]
const handlePromotionToggle = async (restaurant: Restaurant) => {
  const previousState = restaurant.promotionAgree;
  restaurant.promotionAgree = !previousState;
  
  const actionName = restaurant.promotionAgree ? '설정' : '해제';

  try {
    if (!props.userId) {
      throw new Error('사용자 정보가 없습니다.');
    }

    await togglePromotionAgree(props.userId, restaurant.id, restaurant.promotionAgree);
    
  } catch (error) {
    // 실패 시 롤백
    restaurant.promotionAgree = previousState;
    alert(`프로모션 ${actionName} 중 오류가 발생했습니다.`);
  }
};

const handleSearchUser = async () => {
  searchError.value = '';
  linkRequestError.value = '';
  searchResult.value = null;
  searchResults.value = [];

  const query = linkSearchEmail.value.trim();
  if (!query) {
    searchError.value = '이메일을 입력해주세요.';
    return;
  }

  try {
    const response = await searchUsersByEmail(query);
    const data = Array.isArray(response.data) ? response.data : [];
    searchResults.value = data;
    if (data.length === 1) {
      searchResult.value = data[0];
    }
  } catch (error) {
    searchError.value = '해당 이메일을 찾을 수 없습니다.';
  }
};

const clearSearchResults = () => {
  searchError.value = '';
  searchResult.value = null;
  searchResults.value = [];
};

const scheduleSearch = () => {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value);
  }

  const query = linkSearchEmail.value.trim();
  if (query.length < 3) {
    clearSearchResults();
    return;
  }

  searchTimer.value = setTimeout(() => {
    handleSearchUser();
  }, 300);
};

watch(linkSearchEmail, scheduleSearch);

const handleRequestLink = async (targetUser?: UserSearchResult) => {
  const target = targetUser || searchResult.value;
  if (!props.userId || !target) return;
  const alreadyLinked = [...sentLinks.value, ...receivedLinks.value].some(
    (link) => link.counterpartId === target.userId
  );
  if (alreadyLinked) {
    linkRequestError.value = '이미 링크 등록한 이메일입니다.';
    return;
  }
  if (props.userId === target.userId) {
    linkRequestError.value = '자기 자신에게 링크 요청을 보낼 수 없습니다.';
    return;
  }
  try {
    linkRequestError.value = '';
    await requestLink(props.userId, target.userId);
    await fetchLinkLists();
  } catch (error) {
    if (error?.response?.status === 409) {
      linkRequestError.value = '이미 링크 등록한 이메일입니다.';
      return;
    }
    alert('링크 요청에 실패했습니다.');
  }
};

const handleRespondLink = async (linkId: number, status: LinkItem['status']) => {
  try {
    await respondLink(linkId, status);
    await fetchLinkLists();
  } catch (error) {
    alert('링크 처리에 실패했습니다.');
  }
};

const handleDeleteLink = async (counterpartId: number) => {
  if (!props.userId) return;
  try {
    await deleteLink(props.userId, counterpartId);
    await fetchLinkLists();
  } catch (error) {
    alert('링크 해제에 실패했습니다.');
  }
};

const handleTogglePublic = async (restaurant: Restaurant) => {
  if (!props.userId) return;
  const nextState = !restaurant.isPublic;
  const previousState = restaurant.isPublic;
  restaurant.isPublic = nextState;
  try {
    await toggleBookmarkVisibility(props.userId, restaurant.id, nextState);
  } catch (error) {
    restaurant.isPublic = previousState;
    alert('공개 설정 변경에 실패했습니다.');
  }
};

const handleOpenRestaurant = (restaurantId: number) => {
  router.push(`/restaurant/${restaurantId}`);
};

const handleOpenSharedFolder = async (counterpartId: number) => {
  if (!props.userId) return;
  selectedSharedUserId.value = counterpartId;
  isSharedLoading.value = true;
  try {
    const response = await getSharedBookmarks(props.userId, counterpartId);
    const data = Array.isArray(response.data) ? response.data : [];
    sharedBookmarks.value = data.map((item) => ({
      restaurantId: item.restaurantId,
      name: item.name,
      roadAddress: item.roadAddress,
      detailAddress: item.detailAddress,
      imageUrl: item.imageUrl || null,
      rating: Number(item.rating || 0),
      reviewCount: Number(item.reviewCount || 0),
    }));
  } catch (error) {
    sharedBookmarks.value = [];
  } finally {
    isSharedLoading.value = false;
  }
};

const sharedFolders = computed(() => {
  const approvedSent = sentLinks.value.filter(
    (item) => item.status === 'APPROVED'
  );
  const approvedReceived = receivedLinks.value.filter(
    (item) => item.status === 'APPROVED'
  );
  return [...approvedSent, ...approvedReceived];
});

const pendingSentLinks = computed(() =>
  sentLinks.value.filter((item) => item.status === 'PENDING')
);
const pendingReceivedLinks = computed(() =>
  receivedLinks.value.filter((item) => item.status === 'PENDING')
);
const selectedSharedUser = computed(() => {
  if (selectedSharedUserId.value === null) return null;
  return (
    sharedFolders.value.find(
      (link) => link.counterpartId === selectedSharedUserId.value
    ) || null
  );
});

const statusLabel = (status: LinkItem['status']) => {
  if (status === 'APPROVED') return '승인됨';
  if (status === 'REJECTED') return '거절됨';
  return '대기 중';
};
</script>

<template>
  <div class="h-full">
    <div v-if="isLoading" class="py-10 space-y-4">
      <div
        v-for="index in 3"
        :key="index"
        class="bg-white border border-[#e9ecef] rounded-xl p-4 shadow-sm"
      >
        <div class="flex gap-3">
          <div class="w-24 h-24 rounded-lg skeleton"></div>
          <div class="flex-1 space-y-2">
            <div class="h-4 w-2/3 rounded skeleton"></div>
            <div class="h-3 w-1/3 rounded skeleton"></div>
            <div class="h-3 w-4/5 rounded skeleton"></div>
            <div class="h-3 w-2/5 rounded skeleton"></div>
          </div>
        </div>
      </div>
    </div>

    <template v-else>
      <div class="mb-6 space-y-4">
        <div class="bg-white border border-[#e9ecef] rounded-xl p-4 shadow-sm">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-semibold text-[#1e3a5f] text-sm flex items-center gap-2">
              <Link2 class="w-4 h-4" />
              즐겨찾기 공유
            </h3>
            <button
              class="text-xs text-[#1e3a5f] flex items-center gap-1"
              @click="fetchLinkLists"
            >
              <RefreshCw class="w-3 h-3" />
              새로고침
            </button>
          </div>

          <div class="flex gap-2">
            <input
              v-model="linkSearchEmail"
              type="email"
              placeholder="공유할 사용자 이메일"
              class="input-field flex-1"
            />
            <button class="btn-outline-sm whitespace-nowrap" @click="handleSearchUser">
              검색
            </button>
          </div>
          <p v-if="searchError" class="text-xs text-red-500 mt-2">{{ searchError }}</p>
          <p v-if="linkRequestError" class="text-xs text-red-500 mt-2">{{ linkRequestError }}</p>

          <div v-if="searchResults.length > 0" class="mt-3 space-y-2">
            <div
              v-for="user in searchResults"
              :key="user.userId"
              class="p-3 border border-[#e9ecef] rounded-lg bg-[#f8f9fa]"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <img
                    :src="resolveProfileImage(user.image)"
                    :alt="user.nickname || user.name"
                    class="w-10 h-10 rounded-full object-cover border border-[#e9ecef] bg-white"
                  />
                  <div>
                    <p class="text-sm font-semibold text-[#1e3a5f]">
                      {{ user.nickname || user.name }}
                    </p>
                    <p class="text-xs text-[#1e3a5f]">{{ user.email }}</p>
                  </div>
                </div>
                <button class="btn-outline-sm" @click="handleRequestLink(user)">
                  <UserPlus class="w-4 h-4 mr-1" />
                  링크 요청
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white border border-[#e9ecef] rounded-xl p-4 shadow-sm">
          <h4 class="font-semibold text-[#1e3a5f] text-sm mb-3">링크 요청함</h4>
          <div v-if="isLinkLoading" class="text-xs text-[#1e3a5f]">불러오는 중...</div>
          <div v-else-if="pendingSentLinks.length === 0" class="text-xs text-[#1e3a5f]">
            요청한 링크가 없습니다.
          </div>
          <div v-else class="space-y-2">
            <div
              v-for="link in pendingSentLinks"
              :key="link.linkId"
              class="flex items-center justify-between text-xs border border-[#e9ecef] rounded-lg px-3 py-2"
            >
              <div class="flex items-center gap-3">
                <img
                  :src="resolveProfileImage(link.counterpartImage)"
                  :alt="link.counterpartNickname || link.counterpartName"
                  class="w-9 h-9 rounded-full object-cover border border-[#e9ecef] bg-white"
                />
                <div>
                  <p class="font-semibold text-[#1e3a5f]">
                    {{ link.counterpartNickname || link.counterpartName }}
                  </p>
                  <p class="text-[#1e3a5f]">{{ link.counterpartEmail }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <span
                  class="text-[11px] px-2 py-0.5 rounded-full border bg-amber-50 text-amber-700 border-amber-200"
                >
                  {{ statusLabel(link.status) }}
                </span>
                <button
                  class="text-xs text-[#ff6b4a] font-semibold"
                  @click="handleDeleteLink(link.counterpartId)"
                >
                  삭제
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white border border-[#e9ecef] rounded-xl p-4 shadow-sm">
          <h4 class="font-semibold text-[#1e3a5f] text-sm mb-3">링크 수신함</h4>
          <div v-if="isLinkLoading" class="text-xs text-[#1e3a5f]">불러오는 중...</div>
          <div v-else-if="pendingReceivedLinks.length === 0" class="text-xs text-[#1e3a5f]">
            수신된 링크가 없습니다.
          </div>
          <div v-else class="space-y-2">
            <div
              v-for="link in pendingReceivedLinks"
              :key="link.linkId"
              class="flex items-center justify-between text-xs border border-[#e9ecef] rounded-lg px-3 py-2"
            >
              <div class="flex items-center gap-3">
                <img
                  :src="resolveProfileImage(link.counterpartImage)"
                  :alt="link.counterpartNickname || link.counterpartName"
                  class="w-9 h-9 rounded-full object-cover border border-[#e9ecef] bg-white"
                />
                <div>
                  <p class="font-semibold text-[#1e3a5f]">
                    {{ link.counterpartNickname || link.counterpartName }}
                  </p>
                  <p class="text-[#1e3a5f]">{{ link.counterpartEmail }}</p>
                </div>
              </div>
              <div class="flex items-center gap-2">
                <span
                  class="text-[11px] px-2 py-0.5 rounded-full border bg-amber-50 text-amber-700 border-amber-200"
                >
                  {{ statusLabel(link.status) }}
                </span>
                <div class="flex gap-2">
                  <button
                    class="text-xs text-[#1e3a5f] font-semibold"
                    @click="handleRespondLink(link.linkId, 'APPROVED')"
                  >
                    수락
                  </button>
                  <button
                    class="text-xs text-[#ff6b4a] font-semibold"
                    @click="handleRespondLink(link.linkId, 'REJECTED')"
                  >
                    삭제
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="bg-white border border-[#e9ecef] rounded-xl p-4 shadow-sm">
          <h4 class="font-semibold text-[#1e3a5f] text-sm mb-3">공유 폴더</h4>
          <div v-if="sharedFolders.length === 0" class="text-xs text-[#1e3a5f]">
            승인된 링크가 없습니다.
          </div>
          <div v-else class="space-y-2">
            <button
              v-for="link in sharedFolders"
              :key="link.linkId"
              :class="[
                'w-full text-left border rounded-lg px-3 py-2 text-xs transition-colors',
                selectedSharedUserId === link.counterpartId
                  ? 'border-[#ff6b4a] bg-[#fff5f3]'
                  : 'border-[#e9ecef] hover:bg-[#f8f9fa]',
              ]"
              @click="handleOpenSharedFolder(link.counterpartId)"
            >
              <div class="flex items-center gap-3">
                <img
                  :src="resolveProfileImage(link.counterpartImage)"
                  :alt="link.counterpartNickname || link.counterpartName"
                  class="w-9 h-9 rounded-full object-cover border border-[#e9ecef] bg-white"
                />
                <div>
                  <p class="font-semibold text-[#1e3a5f]">
                    {{ link.counterpartNickname || link.counterpartName }}
                  </p>
                  <p class="text-[#1e3a5f]">{{ link.counterpartEmail }}</p>
                </div>
              </div>
            </button>
          </div>

          <div v-if="selectedSharedUserId !== null" class="mt-3">
            <div class="flex items-center justify-between mb-2">
              <div class="flex items-center gap-2">
                <img
                  v-if="selectedSharedUser"
                  :src="resolveProfileImage(selectedSharedUser.counterpartImage)"
                  :alt="selectedSharedUser.counterpartNickname || selectedSharedUser.counterpartName"
                  class="w-6 h-6 rounded-full object-cover border border-[#e9ecef] bg-white"
                />
                <div>
                  <p class="text-xs font-semibold text-[#1e3a5f]">
                    {{ selectedSharedUser
                      ? `${selectedSharedUser.counterpartNickname || selectedSharedUser.counterpartName}님의 공유 폴더`
                      : '공유된 즐겨찾기' }}
                  </p>
                  <p v-if="selectedSharedUser" class="text-[10px] text-[#1e3a5f]">
                    {{ selectedSharedUser.counterpartEmail }}
                  </p>
                </div>
              </div>
              <button
                class="text-xs text-[#1e3a5f]"
                @click="handleOpenSharedFolder(selectedSharedUserId)"
              >
                새로고침
              </button>
            </div>
            <div v-if="isSharedLoading" class="space-y-2">
              <div
                v-for="index in 2"
                :key="index"
                class="border border-[#e9ecef] rounded-lg px-3 py-2 flex gap-3"
              >
                <div class="w-16 h-16 rounded-lg skeleton"></div>
                <div class="flex-1 space-y-2">
                  <div class="h-3 w-1/2 rounded skeleton"></div>
                  <div class="h-3 w-1/3 rounded skeleton"></div>
                  <div class="h-3 w-4/5 rounded skeleton"></div>
                </div>
              </div>
            </div>
            <div v-else-if="sharedBookmarks.length === 0" class="text-xs text-[#1e3a5f]">
              공유된 즐겨찾기가 없습니다.
            </div>
            <div v-else class="space-y-2">
              <RouterLink
                v-for="restaurant in sharedBookmarks"
                :key="restaurant.restaurantId"
                :to="`/restaurant/${restaurant.restaurantId}`"
                class="group border border-[#e9ecef] rounded-lg px-3 py-2 text-xs flex gap-3 hover:bg-[#f8f9fa] transition-all hover:border-[#ffd5cb] hover:shadow-sm"
              >
                <div class="w-16 h-16 rounded-lg overflow-hidden bg-gray-100 flex-shrink-0">
                  <img
                    v-if="restaurant.imageUrl"
                    :src="restaurant.imageUrl"
                    :alt="restaurant.name"
                    class="w-full h-full object-cover"
                  />
                  <div
                    v-else
                    class="w-full h-full flex items-center justify-center text-[10px] text-gray-400"
                  >
                    이미지
                  </div>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="font-semibold text-[#1e3a5f] truncate group-hover:text-[#ff6b4a] transition-colors">
                    {{ restaurant.name }}
                  </p>
                  <div class="flex items-center gap-1 mt-1">
                    <Star class="w-3 h-3 fill-[#ffc107] text-[#ffc107]" />
                    <span class="text-[11px] font-semibold text-[#1e3a5f]">
                      {{ restaurant.reviewCount > 0 ? restaurant.rating.toFixed(1) : '리뷰 없음' }}
                    </span>
                    <span class="text-[10px] text-[#1e3a5f]">({{ restaurant.reviewCount }})</span>
                  </div>
                  <p class="text-[10px] text-[#1e3a5f] mt-1 truncate">
                    {{ restaurant.roadAddress }} {{ restaurant.detailAddress }}
                  </p>
                </div>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>

      <div v-if="favorites.length > 0">
        <div class="px-2 py-4 border-b border-[#e9ecef] mb-2">
          <p class="text-sm text-[#1e3a5f]">
            총 <span class="font-semibold text-[#ff6b4a]">{{ favorites.length }}개</span>의 즐겨찾기
          </p>
        </div>

        <div class="space-y-4 pb-10">
          <div
            v-for="restaurant in favorites"
            :key="restaurant.id"
            class="group bg-white border border-[#e9ecef] rounded-xl overflow-hidden shadow-sm hover:shadow-md transition-all hover:border-[#ffd5cb]"
          >
            <button
              type="button"
              class="w-full text-left"
              @click="handleOpenRestaurant(restaurant.id)"
            >
              <div class="flex gap-3 p-3">
                <div class="relative w-24 h-24 flex-shrink-0 rounded-lg overflow-hidden bg-gray-200">
                  <img
                    v-if="restaurant.imageUrl"
                  :src="restaurant.imageUrl"
                  :alt="restaurant.name"
                  class="absolute inset-0 w-full h-full object-cover"
                />
                <div
                  v-else
                  class="absolute inset-0 flex items-center justify-center text-gray-400 text-xs font-medium bg-gray-100"
                >
                  이미지
                  </div>
                </div>

                <div class="flex-1 min-w-0 flex flex-col justify-center">
                  <div class="flex items-start justify-between mb-1">
                    <h4 class="font-bold text-[#1e3a5f] text-sm truncate pr-2 group-hover:text-[#ff6b4a] transition-colors">
                      {{ restaurant.name }}
                    </h4>
                    <div class="-mt-1 -mr-1">
                      <FavoriteHeart
                        :restaurant-id="restaurant.id"
                        :user-id="props.userId"
                        :initial-favorite="restaurant.isFavorite"
                        @remove="handleRemoveFromList(restaurant.id)" 
                      />
                    </div>
                  </div>
                
                <div class="flex items-center gap-1 mb-1.5">
                  <Star class="w-3 h-3 fill-[#ffc107] text-[#ffc107]" />
                  <span class="text-sm font-bold text-[#1e3a5f]">
                    {{ restaurant.reviewCount > 0 ? restaurant.rating.toFixed(1) : '리뷰 없음' }}
                  </span>
                  <span class="text-xs text-[#1e3a5f]">({{ restaurant.reviewCount }})</span>
                </div>

                <p class="text-xs text-[#1e3a5f] mb-1.5 truncate">{{ restaurant.description }}</p>
                <p class="text-sm font-bold text-[#1e3a5f]">
                  {{ restaurant.avgMainPrice ? `${restaurant.avgMainPrice.toLocaleString()}원` : '가격 미정' }}
                  <span v-if="restaurant.reservationLimit" class="text-xs text-[#1e3a5f]">
                    · 최대 {{ restaurant.reservationLimit }}인
                  </span>
                  </p>
                </div>
              </div>
            </button>

            <div class="flex gap-2 px-3 pb-3">
              <button
                class="flex-1 h-9 text-xs font-semibold border rounded-lg transition-all"
                :class="restaurant.isPublic
                  ? 'bg-[#fff9f8] border-[#ff6b4a] text-[#ff6b4a]'
                  : 'bg-white border-[#dee2e6] text-[#1e3a5f]'"
                @click.stop="handleTogglePublic(restaurant)"
              >
                {{ restaurant.isPublic ? '공개중' : '비공개' }}
              </button>
              <button 
                @click.stop="handlePromotionToggle(restaurant)"
                :class="[
                  'flex-1 h-9 flex items-center justify-center gap-1.5 text-xs font-semibold border rounded-lg transition-all',
                  restaurant.promotionAgree
                    ? 'bg-[#fff9f8] border-[#ff6b4a] text-[#ff6b4a] hover:bg-[#fff0ed]' 
                    : 'bg-white border-[#dee2e6] text-[#1e3a5f] hover:bg-[#f8f9fa] group'
                ]"
              >
                <Bell 
                  :class="[
                    'w-3.5 h-3.5 transition-colors',
                    restaurant.promotionAgree 
                      ? 'fill-[#ff6b4a] text-[#ff6b4a]' 
                      : 'text-[#1e3a5f] group-hover:text-[#ff6b4a]'
                  ]" 
                />
                {{ restaurant.promotionAgree ? '알림 받는 중' : '프로모션 받기' }}
              </button>

              <RouterLink :to="`/restaurant/${restaurant.id}?type=preorder`" class="flex-1">
                <button class="w-full h-9 text-xs font-bold text-white rounded-lg shadow-sm btn-gradient hover:opacity-90 transition-opacity">
                  예약하기
                </button>
              </RouterLink>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="py-20 flex flex-col items-center justify-center text-[#1e3a5f]">
        <Star class="w-12 h-12 mb-3 opacity-20" />
        <p class="text-sm">즐겨찾기한 항목이 없습니다.</p>
      </div>
    </template>
  </div>
</template>

<style scoped>
.btn-gradient {
  background: linear-gradient(135deg, #ff6b4a 0%, #ff8e72 100%);
}

.input-field {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  font-size: 13px;
  border: 1.5px solid #e9ecef;
  border-radius: 10px;
  color: #1e3a5f;
}

.input-field:focus {
  outline: none;
  border-color: #ff6b4a;
  box-shadow: 0 0 0 3px rgba(255, 107, 74, 0.1);
}

.btn-outline-sm {
  height: 40px;
  padding: 0 14px;
  font-size: 12px;
  font-weight: 600;
  color: #1e3a5f;
  background: white;
  border: 1.5px solid #e9ecef;
  border-radius: 10px;
  cursor: pointer;
}

.skeleton {
  background: linear-gradient(120deg, #f8f9fa 0%, #f1f3f5 50%, #f8f9fa 100%);
  background-size: 200% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
