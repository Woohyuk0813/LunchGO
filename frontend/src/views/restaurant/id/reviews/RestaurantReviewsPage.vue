<script setup>
import { ref, computed, onMounted } from "vue";
import { RouterLink, useRoute } from "vue-router";
import {
  ArrowLeft,
  Star,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  X,
} from "lucide-vue-next";
import Card from "@/components/ui/Card.vue";
import axios from "axios";
import { formatReviewTag } from "@/utils/reviewTagEmojis";

const route = useRoute();
const restaurantId = route.params.id || "1"; // Default ID

const sortOrder = ref("추천순");
const isDropdownOpen = ref(false);

const reviews = ref([]);
const reviewSummary = ref(null);

const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}.${month}.${day}`;
};

const mapReviewItem = (item) => ({
  id: item.reviewId,
  author: item.author || "익명",
  company: item.company || null,
  visitCount: item.visitCount ?? null,
  rating: item.rating ?? 0,
  date: formatDate(item.createdAt),
  content: item.isBlinded
    ? "관리자에 의해 블라인드 처리된 리뷰입니다."
    : item.content || "",
  tags: (item.tags || []).map((tag) => tag.name || tag),
  images: item.images || [],
  isExpanded: false,
  isBlinded: Boolean(item.isBlinded),
  blindReason: item.blindReason || "관리자에 의해 블라인드 처리된 리뷰입니다.",
});

const averageRating = computed(() => {
  if (reviewSummary.value?.avgRating != null) {
    return Number(reviewSummary.value.avgRating).toFixed(1);
  }
  const totalRating = reviews.value.reduce((sum, r) => sum + r.rating, 0);
  return reviews.value.length > 0
    ? (totalRating / reviews.value.length).toFixed(1)
    : "0.0";
});

const topTags = computed(() => {
  if (reviewSummary.value?.topTags?.length > 0) {
    return reviewSummary.value.topTags.map((tag) => [tag.name, tag.count]);
  }
  return [];
});

const sortOptions = ref(["추천순", "최신순", "별점 높은순", "별점 낮은순"]);

const setSortOrder = (option) => {
  sortOrder.value = option;
  isDropdownOpen.value = false;
};

// 정렬된 리뷰 목록
const sortedReviews = computed(() => {
  const reviewsCopy = [...reviews.value];

  switch (sortOrder.value) {
    case "최신순":
      return reviewsCopy.sort((a, b) => new Date(b.date) - new Date(a.date));
    case "별점 높은순":
      return reviewsCopy.sort((a, b) => {
        // 1. 블라인드 리뷰는 무조건 맨 아래로
        if (a.isBlinded && !b.isBlinded) return 1;
        if (!a.isBlinded && b.isBlinded) return -1;

        // 2. 그 외에는 별점 내림차순 (5 -> 1)
        return b.rating - a.rating;
      });

    case "별점 낮은순":
      return reviewsCopy.sort((a, b) => {
        // 1. 블라인드 리뷰는 무조건 맨 아래로
        if (a.isBlinded && !b.isBlinded) return 1;
        if (!a.isBlinded && b.isBlinded) return -1;

        // 2. 그 외에는 별점 오름차순 (1 -> 5)
        return a.rating - b.rating;
      });
    case "추천순":
    default:
      // 추천순: 날짜 스코어, 방문 횟수 스코어, 별점 스코어의 가중 평균
      return reviewsCopy.sort((a, b) => {
        // 블라인드 리뷰는 맨 뒤로
        if (a.isBlinded && !b.isBlinded) return 1;
        if (!a.isBlinded && b.isBlinded) return -1;

        // 가중치 설정
        const WEIGHT_DATE = 0.3; // 날짜 가중치 30%
        const WEIGHT_VISIT = 0.4; // 방문 횟수 가중치 40%
        const WEIGHT_RATING = 0.3; // 별점 가중치 30%

        // 1. 날짜 스코어 계산 (0-100점, 최신일수록 높음)
        const today = new Date();
        const dateA = new Date(a.date.replace(/\./g, "-"));
        const dateB = new Date(b.date.replace(/\./g, "-"));
        const maxDaysDiff = 365; // 1년 기준

        const daysAgoA = Math.max(0, (today - dateA) / (1000 * 60 * 60 * 24));
        const daysAgoB = Math.max(0, (today - dateB) / (1000 * 60 * 60 * 24));

        const dateScoreA = Math.max(0, 100 - (daysAgoA / maxDaysDiff) * 100);
        const dateScoreB = Math.max(0, 100 - (daysAgoB / maxDaysDiff) * 100);

        // 2. 방문 횟수 스코어 계산 (0-100점, 방문 횟수 많을수록 높음)
        const maxVisits = 10; // 최대 방문 횟수 기준
        const visitScoreA = Math.min(
          100,
          ((a.visitCount || 0) / maxVisits) * 100
        );
        const visitScoreB = Math.min(
          100,
          ((b.visitCount || 0) / maxVisits) * 100
        );

        // 3. 별점 스코어 계산 (0-100점)
        const maxRating = 5;
        const ratingScoreA = (a.rating / maxRating) * 100;
        const ratingScoreB = (b.rating / maxRating) * 100;

        // 가중 평균 계산
        const totalScoreA =
          dateScoreA * WEIGHT_DATE +
          visitScoreA * WEIGHT_VISIT +
          ratingScoreA * WEIGHT_RATING;

        const totalScoreB =
          dateScoreB * WEIGHT_DATE +
          visitScoreB * WEIGHT_VISIT +
          ratingScoreB * WEIGHT_RATING;

        // 높은 점수가 먼저 오도록 정렬
        return totalScoreB - totalScoreA;
      });
  }
});

// 이미지 확대 모달 상태
const isImageModalOpen = ref(false);
const modalImageUrl = ref("");
const modalImageIndex = ref(0);
const modalImages = ref([]);

// 이미지 클릭 시 모달 열기
const openImageModal = (images, index) => {
  modalImages.value = images;
  modalImageIndex.value = index;
  modalImageUrl.value = images[index];
  isImageModalOpen.value = true;
};

// 모달 닫기
const closeImageModal = () => {
  isImageModalOpen.value = false;
};

// 모달에서 이전/다음 이미지
const handleModalPrevImage = () => {
  modalImageIndex.value =
    modalImageIndex.value === 0
      ? modalImages.value.length - 1
      : modalImageIndex.value - 1;
  modalImageUrl.value = modalImages.value[modalImageIndex.value];
};

const handleModalNextImage = () => {
  modalImageIndex.value =
    modalImageIndex.value === modalImages.value.length - 1
      ? 0
      : modalImageIndex.value + 1;
  modalImageUrl.value = modalImages.value[modalImageIndex.value];
};

// 리뷰 텍스트 더보기/접기 토글 함수
const toggleReviewExpand = (review) => {
  review.isExpanded = !review.isExpanded;
};

// 리뷰 텍스트 길이 체크 (70자 이상이면 더보기 버튼 표시)
const shouldShowExpandButton = (content) => {
  return content.length > 70;
};

// 리뷰 텍스트 자르기 (접혀있을 때)
const truncateText = (content, isExpanded) => {
  if (isExpanded || content.length <= 70) {
    return content;
  }
  return content.substring(0, 70) + "...";
};

// 마우스 드래그 스크롤 기능
const setupDragScroll = (element) => {
  if (!element) return;

  let isDown = false;
  let startX;
  let scrollLeft;

  element.addEventListener("mousedown", (e) => {
    isDown = true;
    element.style.cursor = "grabbing";
    startX = e.pageX - element.offsetLeft;
    scrollLeft = element.scrollLeft;
  });

  element.addEventListener("mouseleave", () => {
    isDown = false;
    element.style.cursor = "grab";
  });

  element.addEventListener("mouseup", () => {
    isDown = false;
    element.style.cursor = "grab";
  });

  element.addEventListener("mousemove", (e) => {
    if (!isDown) return;
    e.preventDefault();
    const x = e.pageX - element.offsetLeft;
    const walk = (x - startX) * 2;
    element.scrollLeft = scrollLeft - walk;
  });
};

// 컴포넌트 마운트 후 드래그 스크롤 설정
onMounted(() => {
  const loadReviews = async () => {
    try {
      const response = await axios.get(
        `/api/restaurants/${restaurantId}/reviews`,
        {
          params: { page: 1, size: 200, sort: "LATEST" },
        }
      );
      const data = response.data?.data ?? response.data;
      reviewSummary.value = data?.summary ?? null;
      reviews.value = (data?.items || []).map(mapReviewItem);
    } catch (error) {
      console.error("리뷰 데이터를 불러오지 못했습니다:", error);
      reviews.value = [];
    }

    const scrollContainers = document.querySelectorAll(".review-image-scroll");
    scrollContainers.forEach((container) => {
      setupDragScroll(container);
    });
  };

  loadReviews();
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink :to="`/restaurant/${restaurantId}`" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">리뷰</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-8">
      <!-- Review Summary -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <div class="text-center">
          <div class="flex items-start gap-6 mb-4">
            <div class="flex flex-col items-center">
              <div class="text-4xl font-bold text-[#1e3a5f] mb-1">
                {{ averageRating }}
              </div>
              <div class="flex items-center gap-0.5 mb-1">
                <Star
                  v-for="(_, i) in Array.from({ length: 5 })"
                  :key="i"
                  :class="`w-4 h-4 ${
                    i < Math.round(Number(averageRating))
                      ? 'fill-[#ffc107] text-[#ffc107]'
                      : 'text-[#e9ecef]'
                  }`"
                />
              </div>
              <div class="text-xs text-[#6c757d]">
                {{ reviewSummary?.reviewCount ?? reviews.length }}개 리뷰
              </div>
            </div>

            <div class="flex-1 text-left">
              <p class="text-xs text-[#6c757d] leading-relaxed mb-3">
                고객님들이 남겨주신 소중한 리뷰입니다. 실제 방문 후 작성된
                리뷰만 표시됩니다.
              </p>
            </div>
          </div>

          <div class="space-y-2">
            <div
              v-for="([tag, count], idx) in topTags"
              :key="idx"
              class="flex items-center justify-between"
            >
              <span
                class="px-4 py-2 text-sm rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white font-semibold shadow-sm flex-1 text-center"
              >
                {{ formatReviewTag(tag) }}
              </span>
              <span class="text-base font-semibold text-[#1e3a5f] ml-3">{{
                count
              }}</span>
            </div>
            <p
              v-if="topTags.length > 0"
              class="text-xs text-[#6c757d] text-right mt-2"
            >
              상위 {{ topTags.length }}개 태그만 표시
            </p>
          </div>
        </div>

        <div class="px-4 pt-4 pb-2">
          <div class="relative inline-block">
            <button
              @click="isDropdownOpen = !isDropdownOpen"
              class="flex items-center gap-2 px-4 py-2 bg-[#6c757d] text-white rounded-lg text-sm font-medium hover:bg-[#5a6268] transition-colors"
            >
              {{ sortOrder }}
              <ChevronDown class="w-4 h-4" />
            </button>
            <div
              v-if="isDropdownOpen"
              class="absolute top-full mt-1 left-0 bg-white border border-[#e9ecef] rounded-lg shadow-lg py-1 min-w-[140px] z-10"
            >
              <button
                v-for="option in sortOptions"
                :key="option"
                @click="setSortOrder(option)"
                :class="`block w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa] transition-colors ${
                  sortOrder === option
                    ? 'text-[#ff6b4a] font-semibold'
                    : 'text-[#495057]'
                }`"
              >
                {{ option }}
              </button>
            </div>
          </div>
        </div>

        <!-- Reviews List -->
        <div class="py-3 space-y-3">
          <div v-for="review in sortedReviews" :key="review.id">
            <Card
              :class="`p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md transition-shadow ${
                review.isBlinded ? 'opacity-60' : ''
              }`"
            >
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-2">
                  <div class="flex flex-col">
                    <div class="flex items-center gap-2">
                      <span class="font-semibold text-[#1e3a5f] text-sm">
                        {{ review.author }}
                        <span
                          v-if="review.company"
                          class="text-[#6c757d] font-normal"
                        >
                          ({{ review.company }})</span
                        >
                      </span>
                      <div
                        v-if="!review.isBlinded"
                        class="flex items-center gap-1"
                      >
                        <Star
                          v-for="(_, i) in Array.from({
                            length: review.rating,
                          })"
                          :key="i"
                          class="w-3.5 h-3.5 fill-[#ffc107] text-[#ffc107]"
                        />
                      </div>
                    </div>
                    <span
                      v-if="!review.isBlinded && review.visitCount"
                      class="text-xs text-[#6c757d] mt-0.5"
                    >
                      {{ review.visitCount }}번째 방문
                    </span>
                  </div>
                </div>
                <span class="text-xs text-[#6c757d]">{{ review.date }}</span>
              </div>

              <!-- 리뷰 이미지 갤러리 (스크롤 방식) -->
              <div
                v-if="
                  !review.isBlinded && review.images && review.images.length > 0
                "
                class="mb-3 -mx-4"
              >
                <div
                  class="flex gap-2 overflow-x-auto px-4 snap-x snap-mandatory scrollbar-hide review-image-scroll cursor-grab active:cursor-grabbing"
                >
                  <div
                    v-for="(image, idx) in review.images"
                    :key="idx"
                    class="flex-shrink-0 snap-start"
                    :class="
                      idx === 0 ? 'w-[calc(100%-3rem)]' : 'w-[calc(100%-6rem)]'
                    "
                  >
                    <div
                      class="relative rounded-lg overflow-hidden bg-[#f8f9fa] h-48 cursor-pointer hover:opacity-95 transition-opacity"
                      @click="openImageModal(review.images, idx)"
                    >
                      <img
                        :src="image || '/placeholder.svg'"
                        :alt="`리뷰 이미지 ${idx + 1}`"
                        class="w-full h-full object-cover pointer-events-none"
                      />
                      <!-- 이미지 카운터 -->
                      <div
                        class="absolute top-2 right-2 bg-black/60 text-white text-xs px-2 py-1 rounded-full"
                      >
                        {{ idx + 1 }} / {{ review.images.length }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 리뷰 내용 -->
              <div v-if="!review.isBlinded">
                <p class="text-sm text-[#495057] leading-relaxed mb-2">
                  {{ truncateText(review.content, review.isExpanded) }}
                </p>

                <!-- 더보기/접기 버튼 -->
                <button
                  v-if="shouldShowExpandButton(review.content)"
                  @click.prevent="toggleReviewExpand(review)"
                  class="text-xs text-[#6c757d] hover:text-[#ff6b4a] font-medium mb-3 transition-colors"
                >
                  {{ review.isExpanded ? "접기" : "더보기" }}
                </button>

                <!-- 태그 -->
                <RouterLink
                  :to="`/restaurant/${restaurantId}/reviews/${review.id}`"
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
                      {{ formatReviewTag(tag) }}
                    </span>
                  </div>
                </RouterLink>
              </div>

              <!-- 블라인드 리뷰 -->
              <div v-else>
                <p class="text-sm text-[#495057] leading-relaxed mb-2">
                  {{ review.content }}
                </p>
                <span
                  class="inline-block px-2.5 py-1 text-xs rounded-full bg-[#6c757d] text-white"
                >
                  사유: {{ review.blindReason }}
                </span>
              </div>
            </Card>
          </div>
        </div>
      </div>
    </main>

    <!-- 이미지 확대 모달 -->
    <div
      v-if="isImageModalOpen"
      class="fixed inset-0 z-[100] bg-black/95 flex items-center justify-center"
      @click="closeImageModal"
    >
      <div class="relative w-full h-full flex items-center justify-center">
        <!-- 닫기 버튼 -->
        <button
          @click.stop="closeImageModal"
          class="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/20 hover:bg-white/30 flex items-center justify-center transition-colors z-10"
          aria-label="닫기"
        >
          <X class="w-6 h-6 text-white" />
        </button>

        <!-- 이미지 -->
        <div class="relative max-w-[90vw] max-h-[90vh]" @click.stop>
          <img
            :src="modalImageUrl || '/placeholder.svg'"
            :alt="`확대 이미지 ${modalImageIndex + 1}`"
            class="max-w-full max-h-[90vh] object-contain rounded-lg"
          />

          <!-- 이미지가 2개 이상일 때만 화살표 표시 -->
          <template v-if="modalImages.length > 1">
            <!-- 왼쪽 화살표 -->
            <button
              @click.stop="handleModalPrevImage"
              class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/30 hover:bg-white/50 flex items-center justify-center transition-colors"
              aria-label="이전 이미지"
            >
              <ChevronLeft class="w-6 h-6 text-white" />
            </button>

            <!-- 오른쪽 화살표 -->
            <button
              @click.stop="handleModalNextImage"
              class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 rounded-full bg-white/30 hover:bg-white/50 flex items-center justify-center transition-colors"
              aria-label="다음 이미지"
            >
              <ChevronRight class="w-6 h-6 text-white" />
            </button>

            <!-- 이미지 카운터 -->
            <div
              class="absolute bottom-4 left-1/2 -translate-x-1/2 bg-black/60 text-white text-sm px-4 py-2 rounded-full"
            >
              {{ modalImageIndex + 1 }} / {{ modalImages.length }}
            </div>
          </template>
        </div>
      </div>
    </div>
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

/* 스크롤 스냅 부드럽게 */
.snap-x {
  scroll-behavior: smooth;
}

/* 마우스 드래그 시 텍스트 선택 방지 */
.review-image-scroll {
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

/* 이미지 드래그 방지 */
.review-image-scroll img {
  pointer-events: none;
  -webkit-user-drag: none;
  user-select: none;
}
</style>
