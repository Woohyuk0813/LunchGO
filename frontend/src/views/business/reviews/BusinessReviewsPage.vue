<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import httpRequest from "@/router/httpRequest";
import {
  Star,
  MessageSquare,
  Calendar,
  User,
  Image as ImageIcon,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  Search,
  Filter,
  AlertTriangle,
  CheckCircle,
  Clock,
  XCircle,
} from "lucide-vue-next";
import BusinessSidebar from "@/components/ui/BusinessSideBar.vue";
import BusinessHeader from "@/components/ui/BusinessHeader.vue";
import Pagination from "@/components/ui/Pagination.vue";

// 필터 상태
const selectedRating = ref("all"); // 'all', '5', '4', '3', '2', '1'
const searchQuery = ref("");
const selectedSort = ref("latest"); // 'latest', 'most-commented'
const selectedResponseStatus = ref("all"); // 'all', 'need-response', 'responded'
const selectedReportStatus = ref("all"); // 'all', 'none', 'pending', 'approved', 'rejected'
const openFilterKey = ref(null);

const ratingOptions = [
  { value: "all", label: "전체 평점" },
  { value: "5", label: "⭐ 5점" },
  { value: "4", label: "⭐ 4점" },
  { value: "3", label: "⭐ 3점" },
  { value: "2", label: "⭐ 2점" },
  { value: "1", label: "⭐ 1점" },
];

const responseStatusOptions = [
  { value: "all", label: "전체 답변 상태" },
  { value: "need-response", label: "답변 필요" },
  { value: "responded", label: "답변 완료" },
];

const reportStatusOptions = [
  { value: "all", label: "전체 신고 상태" },
  { value: "none", label: "신고 안됨" },
  { value: "pending", label: "검토 중" },
  { value: "approved", label: "승인됨" },
  { value: "rejected", label: "거부됨" },
];

const sortOptions = [
  { value: "latest", label: "최신순" },
  { value: "most-commented", label: "댓글 많은순" },
];

const toggleFilterDropdown = (key) => {
  openFilterKey.value = openFilterKey.value === key ? null : key;
};

const filterModelMap = {
  rating: selectedRating,
  response: selectedResponseStatus,
  report: selectedReportStatus,
  sort: selectedSort,
};

const selectFilterOption = (modelKey, value) => {
  const modelRef = filterModelMap[modelKey];
  if (!modelRef) return;
  modelRef.value = value;
  openFilterKey.value = null;
};

const getOptionLabel = (options, value) => {
  return options.find((option) => option.value === value)?.label ?? "";
};

// 댓글 입력 상태
const commentInputs = ref({});
const showCommentInput = ref({});

// 페이지네이션
const pageSize = 10;
const currentPage = ref(1);
const totalReviews = ref(0);
const reviewSummary = ref(null);

// 모달 상태
const isImageModalOpen = ref(false);
const modalImages = ref([]);
const modalImageIndex = ref(0);

// 리뷰 상세 모달
const isDetailModalOpen = ref(false);
const selectedReview = ref(null);

// 블라인드 요청 모달
const isReportModalOpen = ref(false);
const reportReviewId = ref(null);
const reportTag = ref("");
const reportReason = ref("");
const isReportTagOpen = ref(false);
const reportTagDropdownRef = ref(null);

const router = useRouter();
const route = useRoute();
const restaurantId = ref(Number(route.query.restaurantId || 0));
const reviews = ref([]);

const reportTagOptions = [
  { id: 21, name: "욕설/비속어 포함" },
  { id: 22, name: "인신공격/명예훼손" },
  { id: 23, name: "허위 사실 유포" },
  { id: 24, name: "도배/스팸/광고" },
  { id: 25, name: "경쟁 업체 비방" },
];

const getReportStatusFromReviewStatus = (status) => {
  if (status === "BLIND_REQUEST") return "pending";
  if (status === "BLIND_REJECTED") return "rejected";
  if (status === "BLINDED") return "approved";
  return "none";
};

// 통계 계산
const stats = computed(() => {
  const validReviews = reviews.value.filter((r) => !r.author.isBlind);
  const summary = reviewSummary.value;
  const totalReviewCount =
    summary?.reviewCount ?? totalReviews.value ?? validReviews.length;
  const avgValue =
    summary?.avgRating ??
    (validReviews.length > 0
      ? validReviews.reduce((sum, r) => sum + r.rating, 0) /
        validReviews.length
      : 0);
  const avgRating = Number(avgValue).toFixed(1);
  const totalComments = reviews.value.filter((r) => (r.commentCount ?? 0) > 0).length;
  const needsResponse = reviews.value.filter(
    (r) => !r.author.isBlind && (r.commentCount ?? 0) === 0
  ).length;
  const reportedReviews = reviews.value.filter(
    (r) => r.reportStatus === "pending"
  ).length;

  return {
    totalReviews: totalReviewCount,
    avgRating,
    totalComments,
    needsResponse,
    reportedReviews,
  };
});

// 필터링된 리뷰
const filteredReviews = computed(() => {
  let result = [...reviews.value];

  // 평점 필터
  if (selectedRating.value !== "all") {
    const rating = parseInt(selectedRating.value);
    result = result.filter((r) => r.rating === rating);
  }

  // 답변 상태 필터
  if (selectedResponseStatus.value === "need-response") {
    result = result.filter(
      (r) => !r.author.isBlind && (r.commentCount ?? 0) === 0
    );
  } else if (selectedResponseStatus.value === "responded") {
    result = result.filter((r) => (r.commentCount ?? 0) > 0);
  }

  // 신고 상태 필터
  if (selectedReportStatus.value === "none") {
    result = result.filter((r) => r.reportStatus === "none");
  } else if (selectedReportStatus.value === "pending") {
    result = result.filter((r) => r.reportStatus === "pending");
  } else if (selectedReportStatus.value === "approved") {
    result = result.filter((r) => r.reportStatus === "approved");
  } else if (selectedReportStatus.value === "rejected") {
    result = result.filter((r) => r.reportStatus === "rejected");
  }

  // 검색어 필터
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(
      (r) =>
        (r.content || "").toLowerCase().includes(query) ||
        (r.author?.name || "").toLowerCase().includes(query) ||
        (r.author?.company || "").toLowerCase().includes(query)
    );
  }

  // 정렬
  if (selectedSort.value === "latest") {
    result.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } else if (selectedSort.value === "most-commented") {
    result.sort((a, b) => b.comments.length - a.comments.length);
  }

  return result;
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(totalReviews.value / pageSize));
});

// 날짜 포맷팅
const formatDate = (dateString) => {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}.${month}.${day} ${hours}:${minutes}`;
};

// 이미지 모달 열기
const openImageModal = (images, index) => {
  modalImages.value = images;
  modalImageIndex.value = index;
  isImageModalOpen.value = true;
};

const openDetailModal = async (review) => {
  selectedReview.value = review;
  isDetailModalOpen.value = true;
  if (!review?.id || review.detailLoaded) return;
  if (!restaurantId.value) return;
  try {
    const detail = await httpRequest.get(
      `/api/owners/restaurants/${restaurantId.value}/reviews/${review.id}`
    );
    const mapped = mapReviewDetail(detail.data);
    review.detailLoaded = true;
    Object.assign(review, mapped);
  } catch (error) {
    console.error("리뷰 상세 조회 실패:", error);
  }
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
  selectedReview.value = null;
};

// 이미지 모달 닫기
const closeImageModal = () => {
  isImageModalOpen.value = false;
  modalImages.value = [];
  modalImageIndex.value = 0;
};

// 이미지 모달 네비게이션
const nextImage = () => {
  modalImageIndex.value =
    (modalImageIndex.value + 1) % modalImages.value.length;
};

const prevImage = () => {
  modalImageIndex.value =
    (modalImageIndex.value - 1 + modalImages.value.length) %
    modalImages.value.length;
};

// 댓글 입력 토글
const toggleCommentInput = (reviewId) => {
  showCommentInput.value[reviewId] = !showCommentInput.value[reviewId];
  if (!commentInputs.value[reviewId]) {
    commentInputs.value[reviewId] = "";
  }
};

const mapCommentResponse = (comment) => ({
  id: comment.commentId,
  authorType: comment.writerType === "OWNER" ? "owner" : "admin",
  authorName: comment.writerName,
  content: comment.content,
  createdAt: comment.createdAt,
});

const mapVisitInfo = (visitInfo) => {
  if (!visitInfo) return null;
  return {
    date: visitInfo.date,
    partySize: visitInfo.partySize ?? 0,
    totalAmount: visitInfo.totalAmount ?? 0,
    menuItems: (visitInfo.menuItems || []).map((item) => ({
      name: item.name,
      quantity: item.qty ?? item.quantity ?? 0,
      price: item.unitPrice ?? item.price ?? 0,
    })),
  };
};

const mapReviewDetail = (detail) => ({
  id: detail.reviewId,
  restaurantId: restaurantId.value,
  author: {
    name: detail.author || "익명",
    company: detail.company || "",
    isBlind: Boolean(detail.isBlinded),
  },
  rating: detail.rating ?? 0,
  visitCount: detail.visitCount ?? null,
  visitInfo: mapVisitInfo(detail.visitInfo),
  images: detail.images || [],
  tags: (detail.tags || []).map((tag) => tag.name),
  content: detail.isBlinded ? "" : detail.content || "",
  blindReason: detail.blindReason || "",
  createdAt: detail.createdAt,
  status: detail.status || "PUBLIC",
  reportStatus: getReportStatusFromReviewStatus(detail.status),
  reportTag: "",
  reportReason: "",
  reportedAt: null,
  comments: (detail.comments || []).map(mapCommentResponse),
  commentCount: (detail.comments || []).length,
  detailLoaded: true,
});

const mapReviewItem = (item) => ({
  id: item.reviewId,
  restaurantId: restaurantId.value,
  author: {
    name: item.author || "익명",
    company: item.company || "",
    isBlind: Boolean(item.isBlinded),
  },
  rating: item.rating ?? 0,
  visitCount: item.visitCount ?? null,
  visitInfo:
    item.visitDate || item.visitPartySize || item.visitTotalAmount
      ? {
          date: item.visitDate,
          partySize: item.visitPartySize ?? 0,
          totalAmount: item.visitTotalAmount ?? 0,
          menuItems: [],
        }
      : null,
  images: item.images || [],
  tags: (item.tags || []).map((tag) => tag.name),
  content: item.isBlinded ? "" : item.content || "",
  blindReason: item.blindReason || "",
  createdAt: item.createdAt,
  status: item.status || "PUBLIC",
  reportStatus: getReportStatusFromReviewStatus(item.status),
  reportTag: "",
  reportReason: "",
  reportedAt: item.blindRequestedAt || null,
  comments: [],
  commentCount: item.commentCount ?? 0,
  detailLoaded: false,
});

const ensureRestaurantId = async () => {
  if (restaurantId.value) return restaurantId.value;

  try {
    const res = await httpRequest.get("/api/business/owner/restaurant");
    const rid = res?.data?.restaurantId;
    if (rid) {
      restaurantId.value = Number(rid);
      await router.replace({
        path: route.path,
        query: { ...route.query, restaurantId: String(rid) },
      });
      return restaurantId.value;
    }
  } catch (error) {
    console.error("사업자 restaurantId 조회 실패:", error);
  }

  return 0;
};

const loadReviews = async () => {
  if (!restaurantId.value) return;
  const response = await httpRequest.get(
    `/api/owners/restaurants/${restaurantId.value}/reviews`,
    {
      page: currentPage.value,
      size: pageSize,
      sort: "LATEST",
    }
  );
  const data = response.data?.data ?? response.data;
  const items = data?.items || [];
  reviewSummary.value = data?.summary ?? null;
  totalReviews.value = data?.page?.total ?? items.length;
  reviews.value = items.map(mapReviewItem);
};


const handlePageChange = async (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  await loadReviews();
};

// 댓글 추가
const addComment = async (reviewId) => {
  const content = commentInputs.value[reviewId]?.trim();
  if (!content) return;
  if (!restaurantId.value) {
    alert("식당 정보를 불러온 후 다시 시도해주세요.");
    return;
  }

  const review = reviews.value.find((r) => r.id === reviewId);
  if (!review) return;

  try {
    const response = await httpRequest.post(
      `/api/owners/restaurants/${restaurantId.value}/reviews/${reviewId}/comments`,
      { content }
    );
    const newComment = mapCommentResponse(response.data);
    review.comments.push(newComment);
    review.commentCount = (review.commentCount ?? 0) + 1;

    // 입력 초기화
    commentInputs.value[reviewId] = "";
    showCommentInput.value[reviewId] = false;
  } catch (error) {
    console.error("댓글 등록 실패:", error);
    alert("댓글 등록에 실패했습니다. 잠시 후 다시 시도해주세요.");
  }
};

// 댓글 삭제
const deleteComment = async (reviewId, commentId) => {
  if (!restaurantId.value) {
    alert("식당 정보를 불러온 후 다시 시도해주세요.");
    return;
  }
  const review = reviews.value.find((r) => r.id === reviewId);
  if (!review) return;

  try {
    await httpRequest.delete(
      `/api/owners/restaurants/${restaurantId.value}/reviews/${reviewId}/comments/${commentId}`
    );
    review.comments = review.comments.filter((c) => c.id !== commentId);
    review.commentCount = Math.max(0, (review.commentCount ?? 0) - 1);
  } catch (error) {
    console.error("댓글 삭제 실패:", error);
    alert("댓글 삭제에 실패했습니다. 잠시 후 다시 시도해주세요.");
  }
};

// 블라인드 요청 모달 열기
const openReportModal = (reviewId) => {
  reportReviewId.value = reviewId;
  reportTag.value = "";
  reportReason.value = "";
  isReportModalOpen.value = true;
};

// 블라인드 요청 모달 닫기
const closeReportModal = () => {
  isReportModalOpen.value = false;
  reportReviewId.value = null;
  reportTag.value = "";
  reportReason.value = "";
  isReportTagOpen.value = false;
};

// 블라인드 요청 제출
const submitReport = async () => {
  if (!reportTag.value) {
    alert("신고 태그를 선택해주세요.");
    return;
  }
  if (!reportReason.value.trim()) {
    alert("신고 사유를 입력해주세요.");
    return;
  }

  const review = reviews.value.find((r) => r.id === reportReviewId.value);
  if (!review || !restaurantId.value) return;

  try {
    const tag = reportTagOptions.find((item) => item.name === reportTag.value);
    if (!tag) {
      alert("신고 태그를 다시 선택해주세요.");
      return;
    }
    const response = await httpRequest.post(
      `/api/owners/restaurants/${restaurantId.value}/reviews/${reportReviewId.value}/blind-requests`,
      {
        tagId: tag.id,
        reason: reportReason.value.trim(),
      }
    );
    const data = response.data?.data ?? response.data;
    review.status = data?.status || "BLIND_REQUEST";
    review.reportStatus = "pending";
    review.reportTag = reportTag.value;
    review.reportReason = reportReason.value;
    review.reportedAt = data?.blindRequestedAt || new Date().toISOString();

    closeReportModal();
    alert("블라인드 요청이 접수되었습니다. 관리자 검토 후 처리됩니다.");
  } catch (error) {
    console.error("블라인드 요청 실패:", error);
    alert("블라인드 요청에 실패했습니다. 잠시 후 다시 시도해주세요.");
  }
};

// 신고 상태 텍스트 및 스타일
const getReportStatusInfo = (status) => {
  const statusMap = {
    none: { text: "", color: "", icon: null },
    pending: {
      text: "검토 중",
      color: "text-yellow-600 bg-yellow-50",
      icon: Clock,
    },
    approved: {
      text: "승인됨",
      color: "text-green-600 bg-green-50",
      icon: CheckCircle,
    },
    rejected: {
      text: "거부됨",
      color: "text-red-600 bg-red-50",
      icon: XCircle,
    },
  };
  return statusMap[status] || statusMap.none;
};

// 키보드 이벤트 (모달)
onMounted(() => {
  const handleKeydown = (e) => {
    if (isDetailModalOpen.value && e.key === "Escape") {
      closeDetailModal();
      return;
    }
    if (!isImageModalOpen.value) return;
    if (e.key === "ArrowLeft") prevImage();
    if (e.key === "ArrowRight") nextImage();
    if (e.key === "Escape") closeImageModal();
  };
  window.addEventListener("keydown", handleKeydown);
  const handleClickOutside = (e) => {
    const target = e.target;
    if (isReportTagOpen.value) {
      if (
        reportTagDropdownRef.value &&
        !reportTagDropdownRef.value.contains(target)
      ) {
        isReportTagOpen.value = false;
      }
    }
    if (
      openFilterKey.value !== null &&
      !target.closest(".review-filter-dropdown")
    ) {
      openFilterKey.value = null;
    }
  };
  window.addEventListener("click", handleClickOutside);
  return () => {
    window.removeEventListener("keydown", handleKeydown);
    window.removeEventListener("click", handleClickOutside);
  };
});

onMounted(async () => {
  try {
    const rid = await ensureRestaurantId();
    if (!rid) return;
    await loadReviews();
  } catch (error) {
    console.error("리뷰 데이터를 불러오지 못했습니다:", error);
  }
});
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="reviews" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-8">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">리뷰 관리</h2>
          </div>

          <!-- Stats Cards -->
          <div class="grid grid-cols-5 gap-6">
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">전체 리뷰</p>
              <p class="text-4xl font-bold text-[#1e3a5f]">
                {{ stats.totalReviews }}개
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">평균 평점</p>
              <p class="text-4xl font-bold gradient-primary-text">
                {{ stats.avgRating }}
                <span class="text-base">/ 5.0</span>
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">답변 완료</p>
              <p class="text-4xl font-bold text-[#28a745]">
                {{ stats.totalComments }}개
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">답변 필요</p>
              <p class="text-4xl font-bold text-[#ff6b4a]">
                {{ stats.needsResponse }}건
              </p>
            </div>
            <div
              class="bg-white rounded-xl border border-[#e9ecef] p-6 text-center"
            >
              <p class="text-sm text-[#6c757d] mb-2">신고 대기</p>
              <p class="text-4xl font-bold text-[#ffc107]">
                {{ stats.reportedReviews }}건
              </p>
            </div>
          </div>

          <!-- Filters -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-6">
            <div class="flex flex-col md:flex-row gap-4">
              <!-- 검색 -->
              <div class="flex-1 relative">
                <Search
                  class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-[#6c757d]"
                />
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="리뷰 내용, 작성자 검색..."
                  class="w-full pl-10 pr-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
                />
              </div>

              <!-- 평점 필터 -->
              <div class="flex items-center gap-2">
                <Filter class="w-5 h-5 text-[#6c757d]" />
                <div class="review-filter-dropdown">
                  <button
                    type="button"
                    class="filter-dropdown-trigger"
                    @click.stop="toggleFilterDropdown('rating')"
                  >
                    <span class="truncate">
                      {{ getOptionLabel(ratingOptions, selectedRating) }}
                    </span>
                    <ChevronDown class="w-4 h-4 text-[#1E3A5F]" />
                  </button>
                  <div
                    v-if="openFilterKey === 'rating'"
                    class="filter-dropdown-menu"
                  >
                    <button
                      v-for="option in ratingOptions"
                      :key="option.value"
                      :class="[
                        'filter-dropdown-option',
                        option.value === selectedRating ? 'is-active' : '',
                      ]"
                      @click.stop="selectFilterOption('rating', option.value)"
                    >
                      {{ option.label }}
                    </button>
                  </div>
                </div>
              </div>

              <!-- 답변 상태 필터 -->
              <div class="review-filter-dropdown">
                <button
                  type="button"
                  class="filter-dropdown-trigger"
                  @click.stop="toggleFilterDropdown('response')"
                >
                  <span class="truncate">
                    {{
                      getOptionLabel(
                        responseStatusOptions,
                        selectedResponseStatus
                      )
                    }}
                  </span>
                  <ChevronDown class="w-4 h-4 text-[#1E3A5F]" />
                </button>
                <div
                  v-if="openFilterKey === 'response'"
                  class="filter-dropdown-menu"
                >
                  <button
                    v-for="option in responseStatusOptions"
                    :key="option.value"
                    :class="[
                      'filter-dropdown-option',
                      option.value === selectedResponseStatus
                        ? 'is-active'
                        : '',
                    ]"
                    @click.stop="
                      selectFilterOption('response', option.value)
                    "
                  >
                    {{ option.label }}
                  </button>
                </div>
              </div>

              <!-- 신고 상태 필터 -->
              <div class="review-filter-dropdown">
                <button
                  type="button"
                  class="filter-dropdown-trigger"
                  @click.stop="toggleFilterDropdown('report')"
                >
                  <span class="truncate">
                    {{
                      getOptionLabel(reportStatusOptions, selectedReportStatus)
                    }}
                  </span>
                  <ChevronDown class="w-4 h-4 text-[#1E3A5F]" />
                </button>
                <div
                  v-if="openFilterKey === 'report'"
                  class="filter-dropdown-menu"
                >
                  <button
                    v-for="option in reportStatusOptions"
                    :key="option.value"
                    :class="[
                      'filter-dropdown-option',
                      option.value === selectedReportStatus ? 'is-active' : '',
                    ]"
                    @click.stop="
                      selectFilterOption('report', option.value)
                    "
                  >
                    {{ option.label }}
                  </button>
                </div>
              </div>

              <!-- 정렬 -->
              <div class="review-filter-dropdown">
                <button
                  type="button"
                  class="filter-dropdown-trigger"
                  @click.stop="toggleFilterDropdown('sort')"
                >
                  <span class="truncate">
                    {{ getOptionLabel(sortOptions, selectedSort) }}
                  </span>
                  <ChevronDown class="w-4 h-4 text-[#1E3A5F]" />
                </button>
                <div
                  v-if="openFilterKey === 'sort'"
                  class="filter-dropdown-menu"
                >
                  <button
                    v-for="option in sortOptions"
                    :key="option.value"
                    :class="[
                      'filter-dropdown-option',
                      option.value === selectedSort ? 'is-active' : '',
                    ]"
                    @click.stop="selectFilterOption('sort', option.value)"
                  >
                    {{ option.label }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Reviews List -->
          <div class="space-y-6">
            <div
              v-for="review in filteredReviews"
              :key="review.id"
              class="bg-white rounded-2xl border border-[#e9ecef] p-6 shadow-sm hover:shadow-md transition-shadow"
            >
              <!-- Review Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-start gap-4">
                  <div
                    class="w-12 h-12 rounded-full bg-gradient-to-br from-[#ff6b4a] to-[#ffc4b8] flex items-center justify-center text-white font-semibold"
                  >
                    <User class="w-6 h-6" />
                  </div>
                  <div>
                    <div class="flex items-center gap-2 mb-1">
                      <span
                        :class="[
                          'font-semibold',
                          review.author.isBlind
                            ? 'text-[#6c757d]'
                            : 'text-[#1e3a5f]',
                        ]"
                      >
                        {{ review.author.name }}
                      </span>
                      <span class="text-sm text-[#6c757d]">
                        {{ review.author.company }}
                      </span>
                      <span
                        v-if="!review.author.isBlind && review.visitCount"
                        class="text-xs px-2 py-0.5 rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
                      >
                        {{ review.visitCount }}번째 방문
                      </span>
                    </div>
                    <div class="flex items-center gap-3">
                      <div class="flex items-center gap-1">
                        <Star
                          v-for="i in 5"
                          :key="i"
                          :class="[
                            'w-4 h-4',
                            i <= review.rating
                              ? 'fill-[#ff6b4a] text-[#ff6b4a]'
                              : 'fill-[#dee2e6] text-[#dee2e6]',
                          ]"
                        />
                      </div>
                      <span class="text-sm text-[#6c757d]">
                        {{ formatDate(review.createdAt) }}
                      </span>
                      <!-- 신고 상태 뱃지 -->
                      <span
                        v-if="review.reportStatus !== 'none'"
                        :class="[
                          'flex items-center gap-1 text-xs px-2 py-1 rounded-full font-semibold',
                          getReportStatusInfo(review.reportStatus).color,
                        ]"
                      >
                        <component
                          :is="getReportStatusInfo(review.reportStatus).icon"
                          class="w-3 h-3"
                        />
                        {{ getReportStatusInfo(review.reportStatus).text }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Actions -->
                <div class="flex items-center gap-2">
                  <button
                    @click="openDetailModal(review)"
                    class="text-sm text-[#ff6b4a] hover:underline"
                  >
                    상세보기
                  </button>
                  <!-- 블라인드 요청 버튼 (아직 신고하지 않은 경우만) -->
                  <button
                    v-if="
                      !review.author.isBlind && review.reportStatus === 'none'
                    "
                    @click="openReportModal(review.id)"
                    class="flex items-center gap-1 text-sm text-[#dc3545] hover:underline"
                  >
                    <AlertTriangle class="w-4 h-4" />
                    블라인드 요청
                  </button>
                </div>
              </div>

              <!-- Blind Review -->
              <div
                v-if="review.author.isBlind"
                class="bg-[#f8f9fa] rounded-lg p-4 text-center opacity-60"
              >
                <p class="text-sm text-[#6c757d]">{{ review.blindReason }}</p>
              </div>

              <!-- Normal Review Content -->
              <template v-else>
                <!-- Visit Info (if available) -->
                <div v-if="review.visitInfo" class="mb-4">
                  <div
                    class="bg-[#f8f9fa] border border-[#e9ecef] rounded-xl p-4"
                  >
                    <div class="flex items-center gap-6 text-sm mb-3">
                      <div class="flex items-center gap-2">
                        <Calendar class="w-4 h-4 text-[#6c757d]" />
                        <span class="text-[#1e3a5f]">{{
                          review.visitInfo.date
                        }}</span>
                      </div>
                      <div class="text-[#6c757d]">
                        {{ review.visitInfo.partySize }}명
                      </div>
                      <div class="font-semibold text-[#ff6b4a]">
                        {{ review.visitInfo.totalAmount.toLocaleString() }}원
                      </div>
                    </div>
                    <div
                      v-if="review.visitInfo.menuItems.length > 0"
                      class="border-t border-[#dee2e6] pt-3"
                    >
                      <table class="w-full text-sm">
                        <thead class="text-[#6c757d] text-xs">
                          <tr>
                            <th class="text-left pb-2">메뉴명</th>
                            <th class="text-center pb-2">수량</th>
                            <th class="text-right pb-2">단가</th>
                          </tr>
                        </thead>
                        <tbody class="text-[#1e3a5f]">
                          <tr
                            v-for="(item, idx) in review.visitInfo.menuItems"
                            :key="idx"
                            class="border-t border-[#e9ecef]"
                          >
                            <td class="py-1">{{ item.name }}</td>
                            <td class="text-center">{{ item.quantity }}개</td>
                            <td class="text-right">
                              {{ item.price.toLocaleString() }}원
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>

                <!-- Tags -->
                <div
                  v-if="review.tags.length > 0"
                  class="mb-4 flex flex-wrap gap-2"
                >
                  <span
                    v-for="tag in review.tags"
                    :key="tag"
                    class="inline-flex items-center px-3 py-1 rounded-full text-sm bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white font-semibold shadow-sm"
                  >
                    {{ tag }}
                  </span>
                </div>

                <!-- Content -->
                <p class="text-[#1e3a5f] leading-relaxed mb-4">
                  {{ review.content }}
                </p>

                <!-- Comments Section -->
                <div
                  v-if="review.comments.length > 0"
                  class="border-t border-[#e9ecef] pt-4 space-y-3"
                >
                  <div
                    v-for="comment in review.comments"
                    :key="comment.id"
                    class="bg-[#f8f9fa] rounded-lg p-4"
                  >
                    <div class="flex items-start justify-between mb-2">
                      <div class="flex items-center gap-2">
                        <span
                          :class="[
                            'text-xs px-2 py-1 rounded font-semibold',
                            comment.authorType === 'owner'
                              ? 'bg-[#007bff] text-white'
                              : 'bg-[#6f42c1] text-white',
                          ]"
                        >
                          {{
                            comment.authorType === "owner" ? "사장님" : "관리자"
                          }}
                        </span>
                        <span class="font-semibold text-[#1e3a5f]">
                          {{ comment.authorName }}
                        </span>
                        <span class="text-xs text-[#6c757d]">
                          {{ formatDate(comment.createdAt) }}
                        </span>
                      </div>
                      <button
                        @click="deleteComment(review.id, comment.id)"
                        class="text-xs text-[#dc3545] hover:underline"
                      >
                        삭제
                      </button>
                    </div>
                    <p class="text-sm text-[#1e3a5f]">{{ comment.content }}</p>
                  </div>
                </div>

                <!-- Comment Input Toggle -->
                <div class="border-t border-[#e9ecef] pt-4 mt-4">
                  <button
                    v-if="!showCommentInput[review.id]"
                    @click="toggleCommentInput(review.id)"
                    class="flex items-center gap-2 text-sm text-[#ff6b4a] hover:underline"
                  >
                    <MessageSquare class="w-4 h-4" />
                    댓글 작성하기
                  </button>

                  <!-- Comment Input Form -->
                  <div v-else class="space-y-2">
                    <textarea
                      v-model="commentInputs[review.id]"
                      placeholder="고객에게 답변을 남겨주세요..."
                      rows="3"
                      class="w-full px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent resize-none"
                    ></textarea>
                    <div class="flex gap-2 justify-end">
                      <button
                        @click="toggleCommentInput(review.id)"
                        class="px-4 py-2 border border-[#dee2e6] rounded-lg text-sm text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
                      >
                        취소
                      </button>
                      <button
                        @click="addComment(review.id)"
                        class="px-4 py-2 bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white rounded-lg text-sm hover:opacity-90 transition-opacity"
                      >
                        댓글 등록
                      </button>
                    </div>
                  </div>
                </div>
              </template>
            </div>

            <!-- Empty State -->
            <div
              v-if="filteredReviews.length === 0"
              class="bg-white rounded-2xl border border-[#e9ecef] p-12 text-center"
            >
              <MessageSquare class="w-16 h-16 text-[#dee2e6] mx-auto mb-4" />
              <p class="text-lg font-semibold text-[#1e3a5f] mb-2">
                조회된 리뷰가 없습니다
              </p>
              <p class="text-sm text-[#6c757d]">
                필터 조건을 변경하거나 검색어를 확인해주세요.
              </p>
            </div>

            <div v-if="totalPages > 1" class="flex justify-center pt-2">
              <Pagination
                :current-page="currentPage"
                :total-pages="totalPages"
                @change-page="handlePageChange"
              />
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- Image Modal -->
    <Teleport to="body">
      <div
        v-if="isImageModalOpen"
        class="fixed inset-0 bg-black/90 z-50 flex items-center justify-center p-4"
        @click="closeImageModal"
      >
        <button
          @click.stop="closeImageModal"
          class="absolute top-4 right-4 text-white hover:text-[#ff6b4a] transition-colors z-10"
        >
          <svg
            class="w-8 h-8"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>

        <button
          v-if="modalImages.length > 1"
          @click.stop="prevImage"
          class="absolute left-4 text-white hover:text-[#ff6b4a] transition-colors"
        >
          <ChevronLeft class="w-12 h-12" />
        </button>

        <div class="max-w-4xl max-h-[90vh] relative" @click.stop>
          <img
            :src="modalImages[modalImageIndex]"
            alt="리뷰 이미지"
            class="max-w-full max-h-[90vh] object-contain rounded-lg"
          />
          <div
            class="absolute bottom-4 left-1/2 -translate-x-1/2 bg-black/60 text-white px-4 py-2 rounded-full text-sm"
          >
            {{ modalImageIndex + 1 }} / {{ modalImages.length }}
          </div>
        </div>

        <button
          v-if="modalImages.length > 1"
          @click.stop="nextImage"
          class="absolute right-4 text-white hover:text-[#ff6b4a] transition-colors"
        >
          <ChevronRight class="w-12 h-12" />
        </button>
      </div>
    </Teleport>

    <!-- Review Detail Modal -->
    <Teleport to="body">
      <div
        v-if="isDetailModalOpen && selectedReview"
        class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        @click="closeDetailModal"
      >
        <div
          class="bg-white rounded-2xl max-w-5xl w-full p-6 shadow-xl max-h-[90vh] overflow-y-auto"
          @click.stop
        >
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-xl font-bold text-[#1e3a5f]">리뷰 상세</h3>
            <button
              @click="closeDetailModal"
              class="text-[#6c757d] hover:text-[#1e3a5f] transition-colors"
            >
              <svg
                class="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <div class="bg-white rounded-2xl border border-[#e9ecef] p-6">
            <div class="flex items-start justify-between mb-4">
              <div class="flex items-start gap-4">
                <div
                  class="w-12 h-12 rounded-full bg-gradient-to-br from-[#ff6b4a] to-[#ffc4b8] flex items-center justify-center text-white font-semibold"
                >
                  <User class="w-6 h-6" />
                </div>
                <div>
                  <div class="flex items-center gap-2 mb-1">
                    <span
                      :class="[
                        'font-semibold',
                        selectedReview.author.isBlind
                          ? 'text-[#6c757d]'
                          : 'text-[#1e3a5f]',
                      ]"
                    >
                      {{ selectedReview.author.name }}
                    </span>
                    <span class="text-sm text-[#6c757d]">
                      {{ selectedReview.author.company }}
                    </span>
                    <span
                      v-if="!selectedReview.author.isBlind && selectedReview.visitCount"
                      class="text-xs px-2 py-0.5 rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
                    >
                      {{ selectedReview.visitCount }}번째 방문
                    </span>
                  </div>
                  <div class="flex items-center gap-3">
                    <div class="flex items-center gap-1">
                      <Star
                        v-for="i in 5"
                        :key="i"
                        :class="[
                          'w-4 h-4',
                          i <= selectedReview.rating
                            ? 'fill-[#ff6b4a] text-[#ff6b4a]'
                            : 'fill-[#dee2e6] text-[#dee2e6]',
                        ]"
                      />
                    </div>
                    <span class="text-sm text-[#6c757d]">
                      {{ formatDate(selectedReview.createdAt) }}
                    </span>
                    <span
                      v-if="selectedReview.reportStatus !== 'none'"
                      :class="[
                        'flex items-center gap-1 text-xs px-2 py-1 rounded-full font-semibold',
                        getReportStatusInfo(selectedReview.reportStatus).color,
                      ]"
                    >
                      <component
                        :is="
                          getReportStatusInfo(selectedReview.reportStatus).icon
                        "
                        class="w-3 h-3"
                      />
                      {{ getReportStatusInfo(selectedReview.reportStatus).text }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div
              v-if="selectedReview.author.isBlind"
              class="bg-[#f8f9fa] rounded-lg p-4 text-center opacity-60"
            >
              <p class="text-sm text-[#6c757d]">
                {{ selectedReview.blindReason }}
              </p>
            </div>

            <template v-else>
              <div v-if="selectedReview.visitInfo" class="mb-4">
                <div class="bg-[#f8f9fa] border border-[#e9ecef] rounded-xl p-4">
                  <div class="flex items-center gap-6 text-sm mb-3">
                    <div class="flex items-center gap-2">
                      <Calendar class="w-4 h-4 text-[#6c757d]" />
                      <span class="text-[#1e3a5f]">{{
                        selectedReview.visitInfo.date
                      }}</span>
                    </div>
                    <div class="text-[#6c757d]">
                      {{ selectedReview.visitInfo.partySize }}명
                    </div>
                    <div class="font-semibold text-[#ff6b4a]">
                      {{
                        selectedReview.visitInfo.totalAmount.toLocaleString()
                      }}원
                    </div>
                  </div>
                  <div class="border-t border-[#dee2e6] pt-3">
                    <table class="w-full text-sm">
                      <thead class="text-[#6c757d] text-xs">
                        <tr>
                          <th class="text-left pb-2">메뉴명</th>
                          <th class="text-center pb-2">수량</th>
                          <th class="text-right pb-2">단가</th>
                        </tr>
                      </thead>
                      <tbody class="text-[#1e3a5f]">
                        <tr
                          v-for="(item, idx) in selectedReview.visitInfo.menuItems"
                          :key="idx"
                          class="border-t border-[#e9ecef]"
                        >
                          <td class="py-1">{{ item.name }}</td>
                          <td class="text-center">{{ item.quantity }}개</td>
                          <td class="text-right">
                            {{ item.price.toLocaleString() }}원
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              <div
                v-if="selectedReview.images.length > 0"
                class="mb-4 flex gap-2 overflow-x-auto pb-2"
              >
                <div
                  v-for="(image, idx) in selectedReview.images"
                  :key="idx"
                  class="relative flex-shrink-0 w-32 h-32 rounded-lg overflow-hidden cursor-pointer hover:opacity-90 transition-opacity"
                  @click="openImageModal(selectedReview.images, idx)"
                >
                  <img
                    :src="image"
                    :alt="`리뷰 이미지 ${idx + 1}`"
                    class="w-full h-full object-cover"
                  />
                </div>
              </div>

              <div
                v-if="selectedReview.tags.length > 0"
                class="mb-4 flex flex-wrap gap-2"
              >
                <span
                  v-for="tag in selectedReview.tags"
                  :key="tag"
                  class="inline-flex items-center px-3 py-1 rounded-full text-sm bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white font-semibold shadow-sm"
                >
                  {{ tag }}
                </span>
              </div>

              <p class="text-[#1e3a5f] leading-relaxed mb-4">
                {{ selectedReview.content }}
              </p>

              <div
                v-if="selectedReview.comments.length > 0"
                class="border-t border-[#e9ecef] pt-4 space-y-3"
              >
                <div
                  v-for="comment in selectedReview.comments"
                  :key="comment.id"
                  class="bg-[#f8f9fa] rounded-lg p-4"
                >
                  <div class="flex items-start justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <span
                        :class="[
                          'text-xs px-2 py-1 rounded font-semibold',
                          comment.authorType === 'owner'
                            ? 'bg-[#007bff] text-white'
                            : 'bg-[#6f42c1] text-white',
                        ]"
                      >
                        {{
                          comment.authorType === "owner" ? "사장님" : "관리자"
                        }}
                      </span>
                      <span class="font-semibold text-[#1e3a5f]">
                        {{ comment.authorName }}
                      </span>
                      <span class="text-xs text-[#6c757d]">
                        {{ formatDate(comment.createdAt) }}
                      </span>
                    </div>
                  </div>
                  <p class="text-sm text-[#1e3a5f]">{{ comment.content }}</p>
                </div>
              </div>
              <div
                v-else
                class="border-t border-[#e9ecef] pt-4 text-sm text-[#6c757d] flex items-center gap-2"
              >
                <MessageSquare class="w-4 h-4" />
                아직 등록된 댓글이 없습니다.
              </div>
            </template>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Report Modal (블라인드 요청 모달) -->
    <Teleport to="body">
      <div
        v-if="isReportModalOpen"
        class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
        @click="closeReportModal"
      >
        <div
          class="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl"
          @click.stop
        >
          <div class="flex items-center justify-between mb-4">
            <h3
              class="text-xl font-bold text-[#1e3a5f] flex items-center gap-2"
            >
              <AlertTriangle class="w-6 h-6 text-[#dc3545]" />
              블라인드 요청
            </h3>
            <button
              @click="closeReportModal"
              class="text-[#6c757d] hover:text-[#1e3a5f] transition-colors"
            >
              <svg
                class="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <div class="mb-4">
            <p class="text-sm text-[#6c757d] mb-2">
              부적절한 리뷰를 관리자에게 신고하여 블라인드 처리를 요청할 수
              있습니다.
            </p>
            <p class="text-xs text-[#dc3545]">
              ※ 허위 신고는 제재 대상이 될 수 있습니다.
            </p>
          </div>

          <div class="mb-6">
            <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
              신고 태그 <span class="text-[#dc3545]">*</span>
            </label>
            <div class="relative" ref="reportTagDropdownRef">
              <button
                type="button"
                class="w-full h-11 px-4 border border-[#dee2e6] rounded-lg text-left text-sm text-[#1e3a5f] bg-white flex items-center justify-between hover:bg-white transition-colors"
                @click.stop="isReportTagOpen = !isReportTagOpen"
              >
                <span :class="reportTag ? 'text-[#1e3a5f]' : 'text-[#6c757d]'">
                  {{ reportTag || "신고 태그를 선택해주세요" }}
                </span>
                <ChevronDown class="w-4 h-4 text-[#1e3a5f]" />
              </button>

              <div
                v-if="isReportTagOpen"
                class="absolute left-0 right-0 mt-2 bg-white border border-[#e9ecef] rounded-lg shadow-md z-30 max-h-56 overflow-y-auto"
              >
                <button
                  v-for="tag in reportTagOptions"
                  :key="tag.id"
                  type="button"
                  class="w-full text-left px-4 py-2 text-sm text-[#1e3a5f] hover:bg-[#f8f9fa]"
                  @click.stop="
                    reportTag = tag.name;
                    isReportTagOpen = false;
                  "
                >
                  {{ tag.name }}
                </button>
              </div>
            </div>
          </div>

          <div class="mb-6">
            <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
              신고 사유 <span class="text-[#dc3545]">*</span>
            </label>
            <textarea
              v-model="reportReason"
              placeholder="신고 사유를 상세히 작성해주세요&#10;예) 허위 사실 유포, 욕설 포함, 명예훼손 등"
              rows="4"
              class="w-full px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent resize-none"
            ></textarea>
            <p class="text-xs text-[#6c757d] mt-1">
              최소 10자 이상 작성해주세요
            </p>
          </div>

          <div class="flex gap-3">
            <button
              @click="closeReportModal"
              class="flex-1 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
            >
              취소
            </button>
            <button
              @click="submitReport"
              class="flex-1 px-4 py-2 bg-gradient-to-r from-[#dc3545] to-[#c82333] text-white rounded-lg hover:opacity-90 transition-opacity"
            >
              신고하기
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.gradient-primary-text {
  background: linear-gradient(135deg, #ff6b4a 0%, #ffaa8d 50%, #ffc4b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}

/* 스크롤바 스타일링 */
.overflow-x-auto::-webkit-scrollbar {
  height: 6px;
}

.overflow-x-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.overflow-x-auto::-webkit-scrollbar-thumb {
  background: #ff6b4a;
  border-radius: 10px;
}

.overflow-x-auto::-webkit-scrollbar-thumb:hover {
  background: #e55a39;
}

.review-filter-dropdown {
  position: relative;
  min-width: 160px;
}

.filter-dropdown-trigger {
  width: 100%;
  height: 40px;
  padding: 0 14px;
  border: 1px solid #dee2e6;
  border-radius: 10px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  color: #1e3a5f;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.filter-dropdown-trigger:hover {
  background: #f8f9fa;
}

.filter-dropdown-trigger:focus {
  outline: none;
  border-color: #ff6b4a;
  box-shadow: 0 0 0 2px rgba(255, 107, 74, 0.25);
}

.filter-dropdown-menu {
  position: absolute;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: #ffffff;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(30, 58, 95, 0.08);
  z-index: 20;
  max-height: 240px;
  overflow-y: auto;
}

.filter-dropdown-option {
  width: 100%;
  text-align: left;
  padding: 10px 14px;
  font-size: 13px;
  color: #1e3a5f;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.filter-dropdown-option:hover {
  background: #f8f9fa;
}

.filter-dropdown-option.is-active {
  background: #fff4f1;
  color: #ff6b4a;
  font-weight: 600;
}
</style>
