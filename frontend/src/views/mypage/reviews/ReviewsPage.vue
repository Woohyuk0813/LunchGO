<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router';
import {
  ArrowLeft,
  Star,
  MoreVertical,
  Edit,
  Trash2,
  MapPin,
} from 'lucide-vue-next';
import Card from '@/components/ui/Card.vue';
import Pagination from "@/components/ui/Pagination.vue";
import httpRequest from "@/router/httpRequest";
import { useAccountStore } from "@/stores/account";

const route = useRoute();
const router = useRouter();
const accountStore = useAccountStore();

const getReviewRestaurantId = (review) =>
  review?.restaurant?.id ?? review?.restaurantId;

const getReviewId = (review) => {
  const raw = review?.id ?? review?.reviewId;
  if (typeof raw === "number") return raw;
  if (typeof raw === "string") {
    const match = raw.match(/\d+/);
    return match ? Number(match[0]) : null;
  }
  return null;
};

const myReviews = ref([]);
const searchQuery = ref("");
const minRating = ref("all");
const pageSize = ref(5);
const currentPage = ref(1);

const getStoredMember = () => {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("member");
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());
const memberId = computed(() => {
  const rawId = member.value?.id ?? member.value?.userId ?? member.value?.memberId;
  if (rawId === null || rawId === undefined) return null;
  const parsed = Number(rawId);
  return Number.isNaN(parsed) ? null : parsed;
});

const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}.${month}.${day}`;
};

const mapReview = (item) => ({
  id: item.reviewId ?? item.id,
  reservationId: item.reservationId ?? null,
  restaurant: {
    id: item.restaurant?.id ?? item.restaurantId,
    name: item.restaurant?.name ?? item.restaurantName ?? "",
    address: item.restaurant?.address ?? item.restaurantAddress ?? "",
  },
  visitCount: item.visitCount ?? 1,
  rating: item.rating ?? 0,
  date: formatDate(item.createdAt ?? item.date),
  visitDate: formatDate(item.visitDate),
  content: item.content ?? "",
  status: item.status ?? "PUBLIC",
  isBlinded: item.status === "BLINDED",
  tags: Array.isArray(item.tags) ? item.tags : [],
  images: Array.isArray(item.images) ? item.images : [],
});

const loadMyReviews = async () => {
  if (!memberId.value) return;
  try {
    const response = await httpRequest.get("/api/reviews/my", {
      userId: memberId.value,
    });
    const data = Array.isArray(response.data) ? response.data : [];
    myReviews.value = data.map(mapReview);
    scrollToHighlightedReview();
  } catch (error) {
    console.error("내 리뷰 목록 조회 실패:", error);
    myReviews.value = [];
  }
};

// 리뷰 메뉴 드롭다운 상태
const activeReviewMenu = ref(null);

// 리뷰 메뉴 토글
const toggleReviewMenu = (reviewId) => {
  if (activeReviewMenu.value === reviewId) {
    activeReviewMenu.value = null;
  } else {
    activeReviewMenu.value = reviewId;
  }
};

// 리뷰 수정
const handleEditReview = (review) => {
  console.log('리뷰 수정:', review.id);
  activeReviewMenu.value = null;
  // 리뷰 수정 페이지로 이동
  const restaurantId = getReviewRestaurantId(review);
  const reviewId = getReviewId(review);
  if (!restaurantId || !reviewId) {
    alert('리뷰 상세 정보가 없습니다.');
    return;
  }
  router.push(`/restaurant/${restaurantId}/reviews/${reviewId}/edit`);
};

// 리뷰 삭제
const handleDeleteReview = (review) => {
  if (confirm('리뷰를 삭제하시겠습니까?')) {
    const restaurantId = getReviewRestaurantId(review);
    const reviewId = getReviewId(review);
    if (!restaurantId || !reviewId) {
      alert('리뷰 상세 정보가 없습니다.');
      return;
    }
    httpRequest
      .delete(`/api/restaurants/${restaurantId}/reviews/${reviewId}`)
      .then(() => {
        myReviews.value = myReviews.value.filter((r) => r.id !== reviewId);
      })
      .catch((error) => {
        console.error('리뷰 삭제 실패:', error);
        alert('리뷰 삭제에 실패했습니다.');
      });
  }
  activeReviewMenu.value = null;
};

// 리뷰 텍스트 더보기/접기 상태
const expandedReviews = ref(new Set());

// 리뷰 확장/축소 토글
const toggleReviewExpand = (reviewId) => {
  if (expandedReviews.value.has(reviewId)) {
    expandedReviews.value.delete(reviewId);
  } else {
    expandedReviews.value.add(reviewId);
  }
};

// 리뷰 텍스트 길이 체크 (70자 이상이면 더보기 버튼 표시)
const shouldShowExpandButton = (content) => {
  return content.length > 70;
};

// 리뷰 텍스트 자르기
const truncateText = (content, reviewId) => {
  if (expandedReviews.value.has(reviewId) || content.length <= 70) {
    return content;
  }
  return content.substring(0, 70) + '...';
};

const filteredReviews = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  const min = minRating.value === "all" ? 0 : Number(minRating.value);

  return myReviews.value.filter((review) => {
    if (min > 0 && (review.rating ?? 0) < min) return false;
    if (!query) return true;
    const restaurantName = review.restaurant?.name || "";
    const content = review.content || "";
    return (
      restaurantName.toLowerCase().includes(query) ||
      content.toLowerCase().includes(query)
    );
  });
});

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredReviews.value.length / pageSize.value))
);

const pagedReviews = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredReviews.value.slice(start, start + pageSize.value);
});

const goToPage = (page) => {
  const next = Math.min(Math.max(page, 1), totalPages.value);
  currentPage.value = next;
};

// URL에서 highlight 쿼리 파라미터 가져오기
const highlightedReviewId = computed(() => route.query.highlight);

// 하이라이트된 리뷰로 스크롤
const scrollToHighlightedReview = () => {
  if (highlightedReviewId.value) {
    const targetIndex = filteredReviews.value.findIndex(
      (review) => String(review.id) === String(highlightedReviewId.value)
    );
    if (targetIndex >= 0) {
      currentPage.value = Math.floor(targetIndex / pageSize.value) + 1;
    }
    nextTick(() => {
      const element = document.getElementById(
        `review-${highlightedReviewId.value}`
      );
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center' });
        // 하이라이트 효과를 위해 잠시 후 클래스 추가
        setTimeout(() => {
          element.classList.add('highlight-pulse');
          setTimeout(() => {
            element.classList.remove('highlight-pulse');
          }, 2000);
        }, 500);
      }
    });
  }
};

onMounted(() => {
  loadMyReviews();
});

watch(
  () => memberId.value,
  (next) => {
    if (!next) return;
    loadMyReviews();
  }
);

watch(
  () => [searchQuery.value, minRating.value, pageSize.value],
  () => {
    currentPage.value = 1;
  }
);
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <button class="mr-3" type="button" @click="router.back()">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </button>
        <h1 class="font-semibold text-[#1e3a5f] text-base">내가 쓴 리뷰</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-8">
      <!-- 리뷰 개수 표시 -->
      <div class="px-4 py-4 bg-white border-b border-[#e9ecef]">
        <p class="text-sm text-[#6c757d]">
          총
          <span class="font-semibold text-[#1e3a5f]">{{
            filteredReviews.length
          }}</span
          >개의 리뷰
        </p>
      </div>

      <!-- 필터 & 검색 -->
      <div class="px-4 py-3 bg-white border-b border-[#e9ecef] space-y-2">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="식당명 또는 리뷰 내용 검색"
          class="w-full h-10 px-3 border border-[#dee2e6] rounded-lg text-sm focus:outline-none focus:border-[#ff6b4a]"
        />
        <div class="flex items-center gap-2">
          <select
            v-model="minRating"
            class="h-9 px-3 border border-[#dee2e6] rounded-lg text-xs bg-white"
          >
            <option value="all">전체 별점</option>
            <option value="5">5점 이상</option>
            <option value="4">4점 이상</option>
            <option value="3">3점 이상</option>
            <option value="2">2점 이상</option>
            <option value="1">1점 이상</option>
          </select>
          <select
            v-model="pageSize"
            class="h-9 px-3 border border-[#dee2e6] rounded-lg text-xs bg-white"
          >
            <option :value="3">3개씩</option>
            <option :value="5">5개씩</option>
            <option :value="10">10개씩</option>
          </select>
        </div>
      </div>

      <!-- 리뷰가 없는 경우 -->
      <div
        v-if="filteredReviews.length === 0"
        class="flex flex-col items-center justify-center py-16 px-4"
      >
        <div
          class="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mb-4"
        >
          <Star class="w-10 h-10 text-gray-300" />
        </div>
        <p class="text-[#6c757d] text-sm mb-6">아직 작성한 리뷰가 없습니다.</p>
        <RouterLink to="/my-reservations?tab=past">
          <button
            class="px-6 py-3 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
          >
            지난 예약 보기
          </button>
        </RouterLink>
      </div>

      <!-- 리뷰 목록 -->
      <div v-else class="px-4 pt-4 space-y-3">
        <Card
          v-for="review in pagedReviews"
          :key="review.id"
          :id="`review-${review.id}`"
          :class="`p-4 border-[#e9ecef] rounded-2xl bg-white shadow-card hover:shadow-md transition-all ${
            highlightedReviewId === review.id ? 'ring-2 ring-orange-400' : ''
          }`"
        >
          <!-- 식당 정보 -->
          <div class="flex items-start justify-between mb-3">
            <div class="flex-1">
              <RouterLink
                :to="`/restaurant/${review.restaurant.id}`"
                class="block hover:text-blue-600 transition-colors"
              >
                <h3 class="font-semibold text-[#1e3a5f] text-base mb-1">
                  {{ review.restaurant.name }}
                </h3>
              </RouterLink>
              <div class="flex items-start gap-1 text-xs text-[#6c757d]">
                <MapPin class="w-3 h-3 mt-0.5 flex-shrink-0" />
                <span>{{ review.restaurant.address }}</span>
              </div>
            </div>

            <!-- 햄버거 메뉴 버튼 -->
            <div v-if="!review.isBlinded" class="relative ml-2">
              <button
                @click="toggleReviewMenu(review.id)"
                class="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors"
              >
                <MoreVertical class="w-4 h-4 text-gray-600" />
              </button>

              <!-- 드롭다운 메뉴 -->
              <div
                v-if="activeReviewMenu === review.id"
                class="absolute top-10 right-0 bg-white border border-gray-200 rounded-lg shadow-lg py-1 min-w-[120px] z-10"
              >
                <button
                  @click="handleEditReview(review)"
                  class="w-full flex items-center gap-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                >
                  <Edit class="w-4 h-4" />
                  수정
                </button>
                <button
                  @click="handleDeleteReview(review)"
                  class="w-full flex items-center gap-2 px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                >
                  <Trash2 class="w-4 h-4" />
                  삭제
                </button>
              </div>
            </div>
          </div>

          <!-- 별점 & 방문 정보 -->
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-2">
              <div class="flex items-center gap-1">
                <Star
                  v-for="(_, i) in Array.from({ length: review.rating })"
                  :key="i"
                  class="w-4 h-4 fill-[#ffc107] text-[#ffc107]"
                />
              </div>
              <span class="text-xs text-[#6c757d]">
                {{ review.visitCount }}번째 방문
              </span>
            </div>
            <div class="text-xs text-[#6c757d]">
              <span>{{ review.date }}</span>
            </div>
          </div>

          <!-- 리뷰 이미지 (있는 경우) -->
          <div
            v-if="!review.isBlinded && review.images && review.images.length > 0"
            class="mb-3 -mx-4"
          >
            <div class="flex gap-2 overflow-x-auto px-4 scrollbar-hide">
              <RouterLink
                v-for="(image, idx) in review.images"
                :key="idx"
                :to="`/restaurant/${getReviewRestaurantId(review)}/reviews/${getReviewId(review)}`"
                class="flex-shrink-0 w-32 h-32"
              >
                <div
                  class="relative rounded-lg overflow-hidden bg-[#f8f9fa] h-full hover:opacity-95 transition-opacity"
                >
                  <img
                    :src="image || '/placeholder.svg'"
                    :alt="`리뷰 이미지 ${idx + 1}`"
                    class="w-full h-full object-cover"
                  />
                </div>
              </RouterLink>
            </div>
          </div>

          <!-- 리뷰 내용 -->
          <div class="mb-3">
            <div
              v-if="review.isBlinded"
              class="rounded-lg border border-[#e9ecef] bg-[#f8f9fa] px-3 py-2"
            >
              <p class="text-xs font-semibold text-[#dc3545] mb-1">
                숨김 처리됨
              </p>
              <p class="text-sm text-[#6c757d]">
                런치고 리뷰 정책에 의해 블라인드 처리되었습니다.
              </p>
            </div>
            <template v-else>
              <p class="text-sm text-[#495057] leading-relaxed">
                {{ truncateText(review.content, review.id) }}
              </p>
              <!-- 더보기/접기 버튼 -->
              <button
                v-if="shouldShowExpandButton(review.content)"
                @click="toggleReviewExpand(review.id)"
                class="text-xs text-[#6c757d] hover:text-[#ff6b4a] font-medium mt-1 transition-colors"
              >
                {{ expandedReviews.has(review.id) ? '접기' : '더보기' }}
              </button>
            </template>
          </div>

          <!-- 태그 -->
          <RouterLink
            v-if="!review.isBlinded"
            :to="`/restaurant/${getReviewRestaurantId(review)}/reviews/${getReviewId(review)}`"
            class="block"
          >
            <div
              v-if="review.tags && review.tags.length > 0"
              class="flex flex-wrap gap-1.5"
            >
              <span
                v-for="(tag, idx) in review.tags"
                :key="idx"
                class="inline-flex items-center px-2.5 py-1 text-xs rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white font-semibold shadow-sm"
              >
                {{ tag }}
              </span>
            </div>
          </RouterLink>

          <!-- 리뷰 상세 보기 버튼 -->
          <div
            v-if="!review.isBlinded"
            class="mt-3 pt-3 border-t border-[#e9ecef]"
          >
            <RouterLink
              :to="`/restaurant/${getReviewRestaurantId(review)}/reviews/${getReviewId(review)}`"
              class="text-sm text-blue-600 hover:text-blue-700 font-medium transition-colors"
            >
              리뷰 상세 보기 →
            </RouterLink>
          </div>
        </Card>

        <!-- 페이지네이션 -->
        <div class="flex items-center justify-center py-4">
          <Pagination
            :current-page="currentPage"
            :total-pages="totalPages"
            @change-page="goToPage"
          />
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
/* 스크롤바 숨기기 */
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.scrollbar-hide::-webkit-scrollbar {
  display: none;
}

/* 하이라이트 애니메이션 */
@keyframes highlight-pulse {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(251, 146, 60, 0);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(251, 146, 60, 0.3);
  }
}

.highlight-pulse {
  animation: highlight-pulse 2s ease-in-out;
}
</style>
