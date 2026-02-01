<script setup>
import { ref, computed, watch, onMounted } from "vue";
import httpRequest from "@/router/httpRequest";
import AdminSidebar from "@/components/ui/AdminSideBar.vue";
import AdminHeader from "@/components/ui/AdminHeader.vue";
import Pagination from "@/components/ui/Pagination.vue";
import AdminSearchFilter from "@/components/ui/AdminSearchFilter.vue";
import Button from "@/components/ui/Button.vue";
import {
  MessageSquare,
  Star,
  Calendar,
  User,
  AlertTriangle,
  Clock,
  ThumbsUp,
  ArrowUpRight,
  ArrowDownRight,
} from "lucide-vue-next";

// 검색 및 필터
const searchQuery = ref("");
const selectedStatus = ref("all");
const startDate = ref("");
const endDate = ref("");

// 모달 상태
const isDetailModalOpen = ref(false);
const isReportModalOpen = ref(false);
const selectedReview = ref(null);
const reportReason = ref("");
const forbiddenWords = ref([]);
const forbiddenSearchQuery = ref("");
const forbiddenNewWord = ref("");
const showForbiddenList = ref(false);
const forbiddenCurrentPage = ref(1);
const forbiddenItemsPerPage = 20;
const editingWordId = ref(null);
const editingWordValue = ref("");

const adminReportTags = [
  { id: 21, name: "욕설/비속어 포함" },
  { id: 22, name: "인신공격/명예훼손" },
  { id: 23, name: "허위 사실 유포" },
  { id: 24, name: "도배/스팸/광고" },
  { id: 25, name: "경쟁 업체 비방" },
];

// 페이지네이션
const currentPage = ref(1);
const itemsPerPage = 10;

// 리뷰 상태 옵션
const statusOptions = [
  { value: "all", label: "전체" },
  { value: "pending", label: "답변 대기" },
  { value: "answered", label: "답변 완료" },
  { value: "PUBLIC", label: "공개" },
  { value: "BLIND_REQUEST", label: "블라인드 요청" },
  { value: "BLINDED", label: "블라인드" },
  { value: "BLIND_REJECTED", label: "블라인드 거부" },
];

// 리뷰 상태별 배지 색상
const getStatusBadgeColor = (status) => {
  const colors = {
    pending: "bg-yellow-100 text-yellow-800",
    answered: "bg-green-100 text-green-800",
    PUBLIC: "bg-blue-100 text-blue-800",
    BLIND_REQUEST: "bg-red-100 text-red-800",
    BLINDED: "bg-gray-100 text-gray-800",
    BLIND_REJECTED: "bg-orange-100 text-orange-800",
  };
  return colors[status] || "bg-gray-100 text-gray-800";
};

// 리뷰 상태 라벨
const getStatusLabel = (status) => {
  const labels = {
    pending: "답변 대기",
    answered: "답변 완료",
    PUBLIC: "공개",
    BLIND_REQUEST: "블라인드 요청",
    BLINDED: "블라인드",
    BLIND_REJECTED: "블라인드 거부",
  };
  return labels[status] || status;
};

// 작성자명 마스킹 함수
const maskName = (name) => {
  if (!name || name.length === 0) return "";
  if (name.length === 1) return name;
  if (name.length === 2) return name[0] + "*";
  return name[0] + "*".repeat(name.length - 2) + name[name.length - 1];
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

// 리뷰 내용 미리보기 (30자 제한)
const getContentPreview = (content) => {
  if (!content) return "";
  return content.length > 30 ? content.substring(0, 30) + "..." : content;
};

// 날짜 포맷팅
const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}.${month}.${day} ${hours}:${minutes}`;
};

const allReviews = ref([]);

const loadAdminReviews = async () => {
  try {
    const response = await httpRequest.get("/api/admin/reviews");
    const data = response.data?.data ?? response.data;
    const items = Array.isArray(data) ? data : [];
    allReviews.value = items.map((item) => ({
      id: item.reviewId,
      restaurantId: item.restaurantId,
      reviewerName: item.reviewerName || "",
      restaurantName: item.restaurantName || "",
      rating: item.rating ?? 0,
      content: item.content || "",
      totalAmount: item.totalAmount ?? 0,
      createdAt: item.createdAt,
      status: item.status,
      commentCount: item.commentCount ?? 0,
      blindRequestTagId: item.blindRequestTagId ?? null,
      blindRequestTagName: item.blindRequestTagName || "",
      blindRequestReason: item.blindRequestReason || "",
      blindRequestedAt: item.blindRequestedAt,
      tags: [],
      images: [],
      comments: [],
      isTempHidden: item.status === "BLINDED",
    }));
  } catch (error) {
    console.error("관리자 리뷰 데이터를 불러오지 못했습니다:", error);
    allReviews.value = [];
  }
};

const loadForbiddenWords = async () => {
  try {
    const response = await httpRequest.get("/api/admin/forbidden-words");
    const data = response.data?.data ?? response.data;
    const items = Array.isArray(data) ? data : [];
    forbiddenWords.value = items.map((item) => ({
      wordId: item.wordId,
      word: item.word ?? "",
      enabled: item.enabled ?? true,
    }));
  } catch (error) {
    console.error("금칙어 목록을 불러오지 못했습니다:", error);
    forbiddenWords.value = [];
  }
};

const filteredForbiddenWords = computed(() => {
  const query = forbiddenSearchQuery.value.trim().toLowerCase();
  if (!query) return forbiddenWords.value;
  return forbiddenWords.value.filter((item) =>
    (item.word || "").toLowerCase().includes(query)
  );
});

const paginatedForbiddenWords = computed(() => {
  const start = (forbiddenCurrentPage.value - 1) * forbiddenItemsPerPage;
  const end = start + forbiddenItemsPerPage;
  return filteredForbiddenWords.value.slice(start, end);
});

const forbiddenTotalPages = computed(() => {
  return Math.max(
    1,
    Math.ceil(filteredForbiddenWords.value.length / forbiddenItemsPerPage)
  );
});

const handleForbiddenPageChange = (page) => {
  forbiddenCurrentPage.value = page;
};

const handleCreateForbiddenWord = async () => {
  const word = forbiddenNewWord.value.trim();
  if (!word) {
    alert("금칙어를 입력해주세요.");
    return;
  }
  try {
    const response = await httpRequest.post("/api/admin/forbidden-words", {
      word,
    });
    const data = response.data?.data ?? response.data;
    if (data?.wordId) {
      forbiddenWords.value.unshift({
        wordId: data.wordId,
        word: data.word ?? word,
        enabled: data.enabled ?? true,
      });
      forbiddenCurrentPage.value = 1;
      forbiddenNewWord.value = "";
    }
  } catch (error) {
    console.error("금칙어 추가 실패:", error);
    alert(error?.response?.data?.message || "금칙어 추가에 실패했습니다.");
  }
};

const startEditForbiddenWord = (item) => {
  editingWordId.value = item.wordId;
  editingWordValue.value = item.word;
};

const cancelEditForbiddenWord = () => {
  editingWordId.value = null;
  editingWordValue.value = "";
};

const saveEditForbiddenWord = async (item) => {
  const word = editingWordValue.value.trim();
  if (!word) {
    alert("금칙어를 입력해주세요.");
    return;
  }
  try {
    const response = await httpRequest.put(
      `/api/admin/forbidden-words/${item.wordId}`,
      { word }
    );
    const data = response.data?.data ?? response.data;
    item.word = data?.word ?? word;
    cancelEditForbiddenWord();
  } catch (error) {
    console.error("금칙어 수정 실패:", error);
    alert(error?.response?.data?.message || "금칙어 수정에 실패했습니다.");
  }
};

const deleteForbiddenWord = async (item) => {
  if (!confirm(`금칙어를 삭제하시겠습니까?\n${item.word}`)) return;
  try {
    await httpRequest.delete(`/api/admin/forbidden-words/${item.wordId}`);
    forbiddenWords.value = forbiddenWords.value.filter(
      (word) => word.wordId !== item.wordId
    );
  } catch (error) {
    console.error("금칙어 삭제 실패:", error);
    alert(error?.response?.data?.message || "금칙어 삭제에 실패했습니다.");
  }
};

watch([forbiddenSearchQuery, showForbiddenList], () => {
  forbiddenCurrentPage.value = 1;
});

// 필터링된 리뷰 목록
const filteredReviews = computed(() => {
  let filtered = allReviews.value;

  // 검색어 필터
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    filtered = filtered.filter(
      (r) =>
        r.reviewerName.toLowerCase().includes(query) ||
        r.restaurantName.toLowerCase().includes(query) ||
        r.content.toLowerCase().includes(query)
    );
  }

  // 상태 필터
  if (selectedStatus.value !== "all") {
    if (selectedStatus.value === "pending") {
      filtered = filtered.filter((r) => (r.commentCount || 0) === 0);
    } else if (selectedStatus.value === "answered") {
      filtered = filtered.filter((r) => (r.commentCount || 0) > 0);
    } else {
      filtered = filtered.filter((r) => r.status === selectedStatus.value);
    }
  }

  // 날짜 범위 필터
  if (startDate.value || endDate.value) {
    filtered = filtered.filter((r) => {
      const reviewDate = new Date(r.createdAt);

      if (startDate.value && endDate.value) {
        const start = new Date(startDate.value);
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return reviewDate >= start && reviewDate <= end;
      } else if (startDate.value) {
        const start = new Date(startDate.value);
        return reviewDate >= start;
      } else if (endDate.value) {
        const end = new Date(endDate.value);
        end.setHours(23, 59, 59, 999);
        return reviewDate <= end;
      }

      return true;
    });
  }

  return filtered;
});

// 통계 데이터
const stats = computed(() => {
  const now = new Date();
  const currentMonth = now.getMonth() + 1;
  const currentYear = now.getFullYear();
  const lastMonth = currentMonth === 1 ? 12 : currentMonth - 1;
  const lastMonthYear = currentMonth === 1 ? currentYear - 1 : currentYear;

  const allReviewsList = allReviews.value;

  // 평균 평점 계산
  const totalRating = allReviewsList.reduce((sum, r) => sum + r.rating, 0);
  const averageRating =
    allReviewsList.length > 0
      ? (totalRating / allReviewsList.length).toFixed(1)
      : 0;

  // 신고 리뷰
  const reportedReviews = allReviewsList.filter(
    (r) => r.status === "BLIND_REQUEST"
  ).length;

  // 답변 대기 리뷰
  const pendingReviews = allReviewsList.filter(
    (r) => (r.commentCount || 0) === 0
  ).length;

  // 높은 평점 리뷰 (4점 이상)
  const highRatingReviews = allReviewsList.filter(
    (r) => r.rating >= 4.0
  ).length;

  // 이번 달 리뷰
  const thisMonthReviews = allReviewsList.filter((r) => {
    const reviewDate = new Date(r.createdAt);
    return (
      reviewDate.getMonth() + 1 === currentMonth &&
      reviewDate.getFullYear() === currentYear
    );
  }).length;

  // 지난 달 리뷰
  const lastMonthReviews = allReviewsList.filter((r) => {
    const reviewDate = new Date(r.createdAt);
    return (
      reviewDate.getMonth() + 1 === lastMonth &&
      reviewDate.getFullYear() === lastMonthYear
    );
  }).length;

  // 전월 대비 증감
  const totalDiff = thisMonthReviews - lastMonthReviews;
  const reportedDiff = Math.floor(reportedReviews * 0.15); // Mock data
  const pendingDiff = Math.floor(pendingReviews * -0.1); // Mock data
  const highRatingDiff = Math.floor(highRatingReviews * 0.08); // Mock data

  return {
    total: allReviewsList.length,
    totalDiff,
    averageRating,
    reported: reportedReviews,
    reportedDiff,
    pending: pendingReviews,
    pendingDiff,
    highRating: highRatingReviews,
    highRatingDiff,
  };
});

// 페이지네이션된 리뷰 목록
const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredReviews.value.slice(start, end);
});

// 총 페이지 수
const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredReviews.value.length / itemsPerPage));
});

// 페이지 변경 핸들러
const handlePageChange = (page) => {
  currentPage.value = page;
};

watch([searchQuery, selectedStatus, startDate, endDate], () => {
  currentPage.value = 1;
});

onMounted(() => {
  loadAdminReviews();
  loadForbiddenWords();
});

// 필터 값 업데이트 핸들러
const handleFilterUpdate = ({ model, value }) => {
  if (model === "selectedStatus") {
    selectedStatus.value = value;
  }
  currentPage.value = 1;
};

// 필터 초기화
const resetFilters = () => {
  searchQuery.value = "";
  selectedStatus.value = "all";
  startDate.value = "";
  endDate.value = "";
  currentPage.value = 1;
};

// 필터 설정
const filters = computed(() => [
  {
    model: "selectedStatus",
    label: "상태",
    value: selectedStatus.value,
    options: statusOptions,
  },
]);

const selectedReviewDetail = computed(() => {
  if (!selectedReview.value) return null;
  return {
    id: selectedReview.value.id,
    restaurantName: selectedReview.value.restaurantName,
    author: {
      name: selectedReview.value.reviewerName,
      company: "",
      isBlind: selectedReview.value.status === "BLINDED",
    },
    rating: selectedReview.value.rating,
    visitCount: selectedReview.value.visitCount,
    visitInfo: selectedReview.value.visitInfo || null,
    images: selectedReview.value.images || [],
    tags: selectedReview.value.tags || [],
    content:
      selectedReview.value.status === "BLINDED"
        ? ""
        : selectedReview.value.content,
    blindReason:
      selectedReview.value.status === "BLINDED"
        ? "관리자에 의해 숨김 처리된 리뷰입니다."
        : "",
    createdAt: selectedReview.value.createdAt,
    blindRequestTagName: selectedReview.value.blindRequestTagName,
    blindRequestReason: selectedReview.value.blindRequestReason,
    blindRequestedAt: selectedReview.value.blindRequestedAt,
    comments: selectedReview.value.comments || [],
  };
});

// 관리 버튼 핸들러
const handleViewDetail = (review) => {
  selectedReview.value = review;
  isDetailModalOpen.value = true;
  loadAdminReviewDetail(review);
};

const loadAdminReviewDetail = async (review) => {
  if (!review?.restaurantId || !review?.id) return;
  try {
    const response = await httpRequest.get(
      `/api/owners/restaurants/${review.restaurantId}/reviews/${review.id}`
    );
    const data = response.data?.data ?? response.data;
    if (!data || !selectedReview.value || selectedReview.value.id !== review.id) {
      return;
    }
    selectedReview.value = {
      ...selectedReview.value,
      rating: data.rating ?? selectedReview.value.rating,
      content: data.content ?? selectedReview.value.content,
      tags: (data.tags || []).map((tag) => tag.name ?? tag),
      images: data.images || [],
      comments: (data.comments || []).map(mapCommentResponse),
      visitInfo: mapVisitInfo(data.visitInfo),
      visitCount: data.visitCount ?? selectedReview.value.visitCount,
    };
  } catch (error) {
    console.error("리뷰 상세 조회 실패:", error);
  }
};

const handleProcessReport = (review) => {
  selectedReview.value = review;
  reportReason.value = "";
  isReportModalOpen.value = true;
};

const handleHideReview = (review) => {
  if (review.isTempHidden) {
    const confirmShow = confirm(
      `숨김 해제하시겠습니까?\n리뷰: ${review.id} (${review.restaurantName})`
    );
    if (!confirmShow) {
      alert("숨김 해제를 취소했습니다.");
      return;
    }
    toggleHideReview(review, false);
    return;
  }

  const confirmHide = confirm(
    `숨김 처리하시겠습니까?\n리뷰: ${review.id} (${review.restaurantName})`
  );
  if (!confirmHide) {
    alert("숨김 처리를 취소했습니다.");
    return;
  }
  toggleHideReview(review, true);
};

const toggleHideReview = async (review, hidden) => {
  try {
    const response = await httpRequest.patch(
      `/api/admin/reviews/${review.id}/hide`,
      { hidden }
    );
    const data = response.data?.data ?? response.data;
    review.status = data?.status || review.status;
    review.isTempHidden = hidden;
  } catch (error) {
    console.error("리뷰 숨김 처리 실패:", error);
    alert("리뷰 숨김 처리에 실패했습니다. 잠시 후 다시 시도해주세요.");
  }
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
  selectedReview.value = null;
};

const closeReportModal = () => {
  isReportModalOpen.value = false;
  selectedReview.value = null;
  reportReason.value = "";
};

const submitReportProcess = async (decision) => {
  if (!reportReason.value.trim()) {
    alert("신고 사유를 입력해주세요.");
    return;
  }
  if (!selectedReview.value?.blindRequestTagId) {
    alert("사업자 요청 태그가 없어 처리할 수 없습니다.");
    return;
  }

  if (!selectedReview.value) return;

  try {
    const response = await httpRequest.patch(
      `/api/admin/reviews/${selectedReview.value.id}/blind-requests`,
      {
        decision: decision === "approve" ? "APPROVE" : "REJECT",
        tagId: selectedReview.value?.blindRequestTagId,
        reason: reportReason.value.trim(),
      }
    );
    const data = response.data?.data ?? response.data;
    selectedReview.value.status =
      data?.status || (decision === "approve" ? "BLINDED" : "BLIND_REJECTED");
    selectedReview.value.isTempHidden =
      selectedReview.value.status === "BLINDED";

    alert(
      decision === "approve"
        ? "블라인드 승인이 완료되었습니다."
        : "블라인드 거부가 완료되었습니다."
    );
    closeReportModal();
  } catch (error) {
    console.error("블라인드 처리 실패:", error);
    alert("블라인드 처리에 실패했습니다. 잠시 후 다시 시도해주세요.");
  }
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <AdminSidebar activeMenu="reviews" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <AdminHeader />

      <!-- Scrollable Content Area -->
      <main class="flex-1 overflow-y-auto p-8">
        <div class="max-w-7xl mx-auto space-y-6">
          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <h2 class="text-3xl font-bold text-[#1e3a5f]">리뷰 관리</h2>
            <p class="text-sm text-[#6c757d]">
              총 {{ filteredReviews.length.toLocaleString() }}건
            </p>
          </div>

          <!-- 통계 카드 -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
            <!-- 전체 리뷰 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-purple-50 rounded-lg">
                  <MessageSquare class="w-6 h-6 text-purple-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">전체 리뷰</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.total.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.totalDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.totalDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 평균 평점 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-yellow-50 rounded-lg">
                  <Star class="w-6 h-6 text-yellow-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">평균 평점</p>
              <p class="text-2xl font-bold">
                {{ stats.averageRating }}
              </p>
              <p class="text-xs text-[#6c757d] mt-2">5.0 만점</p>
            </div>

            <!-- 신고 리뷰 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-red-50 rounded-lg">
                  <AlertTriangle class="w-6 h-6 text-red-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">블라인드 요청</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.reported.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.reportedDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.reportedDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 답변 대기 -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-orange-50 rounded-lg">
                  <Clock class="w-6 h-6 text-orange-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">답변 대기</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.pending.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="stats.pendingDiff >= 0 ? ArrowUpRight : ArrowDownRight"
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.pendingDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>

            <!-- 높은 평점 리뷰 (4점 이상) -->
            <div class="bg-white p-6 rounded-xl border border-[#e9ecef]">
              <div class="flex items-center justify-between mb-4">
                <div class="p-3 bg-green-50 rounded-lg">
                  <ThumbsUp class="w-6 h-6 text-green-600" />
                </div>
              </div>
              <p class="text-sm text-[#6c757d] mb-1">높은 평점 (4점↑)</p>
              <p class="text-2xl font-bold mb-2">
                {{ stats.highRating.toLocaleString() }}
              </p>
              <div class="flex items-center text-sm">
                <component
                  :is="
                    stats.highRatingDiff >= 0 ? ArrowUpRight : ArrowDownRight
                  "
                  class="w-4 h-4 mr-1"
                />
                <span class="">
                  {{ Math.abs(stats.highRatingDiff) }}
                </span>
                <span class="text-[#6c757d] ml-1">전월 대비</span>
              </div>
            </div>
          </div>

          <!-- 필터 및 검색 -->
          <AdminSearchFilter
            v-model:search-query="searchQuery"
            v-model:start-date="startDate"
            v-model:end-date="endDate"
            :filters="filters"
            :show-date-filter="true"
            search-label="검색"
            search-placeholder="작성자, 식당명, 리뷰 내용으로 검색"
            start-date-label="작성일 (시작)"
            end-date-label="작성일 (종료)"
            @update:filter-value="handleFilterUpdate"
            @reset-filters="resetFilters"
          />

          <!-- 금칙어 관리 -->
          <div class="bg-white rounded-xl border border-[#e9ecef] p-6 space-y-4">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-lg font-semibold text-[#1e3a5f]">금칙어 관리</h3>
                <p class="text-sm text-[#6c757d]">
                  총 {{ forbiddenWords.length.toLocaleString() }}건
                </p>
              </div>
            </div>

            <div class="flex flex-col gap-3 lg:flex-row lg:items-center">
              <div class="flex-1">
                <input
                  v-model="forbiddenSearchQuery"
                  type="text"
                  placeholder="금칙어 검색"
                  class="w-full px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
                />
              </div>
              <div class="flex flex-1 gap-2">
                <input
                  v-model="forbiddenNewWord"
                  type="text"
                  placeholder="새 금칙어 입력"
                  class="flex-1 px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
                />
                <Button variant="default" @click="handleCreateForbiddenWord">
                  추가
                </Button>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <p class="text-xs text-[#6c757d]">
                검색 결과 {{ filteredForbiddenWords.length.toLocaleString() }}건
              </p>
              <Button
                variant="outline"
                size="sm"
                @click="showForbiddenList = !showForbiddenList"
              >
                {{ showForbiddenList ? "리스트 숨기기" : "리스트 보기" }}
              </Button>
            </div>

            <div
              v-if="showForbiddenList || forbiddenSearchQuery.trim()"
              class="border border-[#e9ecef] rounded-lg overflow-hidden"
            >
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th
                      class="px-6 py-3 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      금칙어
                    </th>
                    <th
                      class="px-6 py-3 text-right text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      관리
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="item in paginatedForbiddenWords"
                    :key="item.wordId"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td class="px-6 py-3 text-sm text-[#1e3a5f]">
                      <div v-if="editingWordId === item.wordId" class="flex">
                        <input
                          v-model="editingWordValue"
                          type="text"
                          class="w-full px-3 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent"
                        />
                      </div>
                      <span v-else>{{ item.word }}</span>
                    </td>
                    <td class="px-6 py-3 text-right">
                      <div class="flex justify-end gap-2">
                        <template v-if="editingWordId === item.wordId">
                          <Button
                            variant="outline"
                            size="sm"
                            @click="saveEditForbiddenWord(item)"
                          >
                            저장
                          </Button>
                          <Button
                            variant="ghost"
                            size="sm"
                            @click="cancelEditForbiddenWord"
                          >
                            취소
                          </Button>
                        </template>
                        <template v-else>
                          <Button
                            variant="outline"
                            size="sm"
                            @click="startEditForbiddenWord(item)"
                          >
                            수정
                          </Button>
                          <Button
                            variant="destructive"
                            size="sm"
                            @click="deleteForbiddenWord(item)"
                          >
                            삭제
                          </Button>
                        </template>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>

              <div
                v-if="paginatedForbiddenWords.length === 0"
                class="text-center py-8 text-[#6c757d] text-sm"
              >
                금칙어가 없습니다.
              </div>

              <div
                v-if="forbiddenTotalPages > 1"
                class="px-6 py-4 border-t border-[#e9ecef] flex justify-center"
              >
                <Pagination
                  :current-page="forbiddenCurrentPage"
                  :total-pages="forbiddenTotalPages"
                  @change-page="handleForbiddenPageChange"
                />
              </div>
            </div>
          </div>

          <!-- 리뷰 목록 테이블 -->
          <div
            class="bg-white rounded-xl border border-[#e9ecef] overflow-hidden"
          >
            <div class="overflow-x-auto">
              <table class="w-full">
                <thead class="bg-[#f8f9fa] border-b border-[#e9ecef]">
                  <tr>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      작성자
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      식당명
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      평점
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      내용
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      총 금액
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      작성일
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      상태
                    </th>
                    <th
                      class="px-6 py-4 text-left text-xs font-semibold text-[#1e3a5f] uppercase tracking-wider"
                    >
                      관리
                    </th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-[#e9ecef]">
                  <tr
                    v-for="review in paginatedReviews"
                    :key="review.id"
                    class="hover:bg-[#f8f9fa] transition-colors"
                  >
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1e3a5f]"
                    >
                      {{ maskName(review.reviewerName) }}
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ review.restaurantName }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm">
                      <div class="flex items-center">
                        <Star
                          class="w-4 h-4 text-yellow-500 fill-yellow-500 mr-1"
                        />
                        <span class="font-semibold text-[#1e3a5f]">{{
                          review.rating
                        }}</span>
                        <span class="text-[#6c757d] ml-1">/ 5.0</span>
                      </div>
                    </td>
                    <td class="px-6 py-4 text-sm text-[#495057] max-w-xs">
                      <div class="truncate">
                        {{ getContentPreview(review.content) }}
                      </div>
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ review.totalAmount.toLocaleString() }}원
                    </td>
                    <td
                      class="px-6 py-4 whitespace-nowrap text-sm text-[#495057]"
                    >
                      {{ review.createdAt }}
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap">
                      <span
                        :class="[
                          'px-3 py-1 rounded-full text-xs font-medium',
                          getStatusBadgeColor(review.status),
                        ]"
                      >
                        {{ getStatusLabel(review.status) }}
                      </span>
                    </td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm">
                      <div class="flex gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          @click="handleViewDetail(review)"
                        >
                          상세 보기
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          @click="handleProcessReport(review)"
                          v-if="review.status === 'BLIND_REQUEST'"
                        >
                          요청 처리
                        </Button>
                        <Button
                          variant="secondary"
                          size="sm"
                          @click="handleHideReview(review)"
                        >
                          {{ review.isTempHidden ? "숨김 해제" : "숨김 처리" }}
                        </Button>
                      </div>
                      <div
                        v-if="review.isTempHidden"
                        class="mt-2 text-xs text-[#dc3545]"
                      >
                        숨김 처리됨
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>

              <!-- 데이터 없을 때 -->
              <div
                v-if="paginatedReviews.length === 0"
                class="text-center py-12 text-[#6c757d]"
              >
                <p class="text-lg">검색 결과가 없습니다.</p>
                <p class="text-sm mt-2">다른 조건으로 검색해보세요.</p>
              </div>
            </div>

            <!-- 페이지네이션 -->
            <div
              v-if="totalPages > 1"
              class="px-6 py-4 border-t border-[#e9ecef] flex justify-center"
            >
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
  </div>

  <!-- Detail Modal -->
  <Teleport to="body">
    <div
      v-if="isDetailModalOpen && selectedReviewDetail"
      class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4"
      @click="closeDetailModal"
    >
      <div
        class="bg-white rounded-2xl max-w-5xl w-full p-6 shadow-xl max-h-[90vh] overflow-y-auto"
        @click.stop
      >
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-xl font-bold text-[#1e3a5f]">리뷰 상세</h3>
            <p class="text-sm text-[#6c757d]">
              {{ selectedReviewDetail.restaurantName }}
            </p>
          </div>
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
                      selectedReviewDetail.author.isBlind
                        ? 'text-[#6c757d]'
                        : 'text-[#1e3a5f]',
                    ]"
                  >
                    {{ selectedReviewDetail.author.name }}
                  </span>
                  <span class="text-sm text-[#6c757d]">
                    {{ selectedReviewDetail.author.company }}
                  </span>
                  <span
                    v-if="selectedReviewDetail.visitCount"
                    class="text-xs px-2 py-0.5 rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
                  >
                    {{ selectedReviewDetail.visitCount }}번째 방문
                  </span>
                </div>
                <div class="flex items-center gap-3">
                  <div class="flex items-center gap-1">
                    <Star
                      v-for="i in 5"
                      :key="i"
                      :class="[
                        'w-4 h-4',
                        i <= selectedReviewDetail.rating
                          ? 'fill-[#ff6b4a] text-[#ff6b4a]'
                          : 'fill-[#dee2e6] text-[#dee2e6]',
                      ]"
                    />
                  </div>
                  <span class="text-sm text-[#6c757d]">
                    {{ formatDate(selectedReviewDetail.createdAt) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div
            v-if="selectedReviewDetail.author.isBlind"
            class="bg-[#f8f9fa] rounded-lg p-4 text-center opacity-60"
          >
            <p class="text-sm text-[#6c757d]">
              {{ selectedReviewDetail.blindReason }}
            </p>
          </div>

          <template v-else>
            <div v-if="selectedReviewDetail.visitInfo" class="mb-4">
              <div class="bg-[#f8f9fa] border border-[#e9ecef] rounded-xl p-4">
                <div class="flex items-center gap-6 text-sm mb-3">
                  <div class="flex items-center gap-2">
                    <Calendar class="w-4 h-4 text-[#6c757d]" />
                    <span class="text-[#1e3a5f]">{{
                      selectedReviewDetail.visitInfo.date
                    }}</span>
                  </div>
                  <div class="text-[#6c757d]">
                    {{ selectedReviewDetail.visitInfo.partySize }}명
                  </div>
                  <div class="font-semibold text-[#ff6b4a]">
                    {{
                      selectedReviewDetail.visitInfo.totalAmount.toLocaleString()
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
                        v-for="(item, idx) in selectedReviewDetail.visitInfo
                          .menuItems"
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
              v-if="selectedReviewDetail.images.length > 0"
              class="mb-4 flex gap-2 overflow-x-auto pb-2"
            >
              <div
                v-for="(image, idx) in selectedReviewDetail.images"
                :key="idx"
                class="relative flex-shrink-0 w-32 h-32 rounded-lg overflow-hidden"
              >
                <img
                  :src="image"
                  :alt="`리뷰 이미지 ${idx + 1}`"
                  class="w-full h-full object-cover"
                />
              </div>
            </div>

            <div
              v-if="selectedReviewDetail.tags.length > 0"
              class="mb-4 flex flex-wrap gap-2"
            >
              <span
                v-for="tag in selectedReviewDetail.tags"
                :key="tag"
                class="inline-flex items-center px-3 py-1 rounded-full text-sm bg-gradient-to-r from-[#ff6b4a] to-[#ff8e72] text-white font-semibold shadow-sm"
              >
                {{ tag }}
              </span>
            </div>

            <p class="text-[#1e3a5f] leading-relaxed mb-4">
              {{ selectedReviewDetail.content }}
            </p>

            <div
              v-if="selectedReviewDetail.comments.length > 0"
              class="border-t border-[#e9ecef] pt-4 space-y-3"
            >
              <div
                v-for="comment in selectedReviewDetail.comments"
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
                      {{ comment.authorType === "owner" ? "사장님" : "관리자" }}
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

  <!-- Report Process Modal -->
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
          <h3 class="text-xl font-bold text-[#1e3a5f] flex items-center gap-2">
            <AlertTriangle class="w-6 h-6 text-[#dc3545]" />
            블라인드 요청 처리
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
            블라인드 요청에 대한 처리 결과를 입력해주세요.
          </p>
        </div>

          <div
            v-if="selectedReview"
            class="mb-4 bg-[#f8f9fa] border border-[#e9ecef] rounded-lg p-4 text-sm"
          >
            <p class="font-semibold text-[#1e3a5f] mb-2">
              사업자 블라인드 요청 내용
            </p>
            <p class="text-[#6c757d]">
              태그:
              <span class="text-[#1e3a5f] font-medium">
              {{ selectedReview.blindRequestTagName || "미입력" }}
              </span>
            </p>
            <p class="text-[#6c757d] mt-1">
              사유:
              <span class="text-[#1e3a5f] font-medium">
              {{ selectedReview.blindRequestReason || "미입력" }}
              </span>
            </p>
            <p class="text-[#6c757d] mt-1">
              접수 시각:
              <span class="text-[#1e3a5f] font-medium">
              {{
                selectedReview.blindRequestedAt
                  ? formatDate(selectedReview.blindRequestedAt)
                  : "미입력"
              }}
              </span>
            </p>
          </div>

        <div class="mb-6">
          <label class="block text-sm font-semibold text-[#1e3a5f] mb-2">
            관리자 처리 사유 <span class="text-[#dc3545]">*</span>
          </label>
          <textarea
            v-model="reportReason"
            placeholder="신고 처리 사유를 입력해주세요."
            rows="4"
            class="w-full px-4 py-2 border border-[#dee2e6] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#ff6b4a] focus:border-transparent resize-none"
          ></textarea>
        </div>

        <div class="flex gap-3">
          <button
            @click="closeReportModal"
            class="flex-1 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
          >
            취소
          </button>
          <button
            @click="submitReportProcess('reject')"
            class="flex-1 px-4 py-2 border border-[#dee2e6] rounded-lg text-[#6c757d] hover:bg-[#f8f9fa] transition-colors"
          >
            거부
          </button>
          <button
            @click="submitReportProcess('approve')"
            class="flex-1 px-4 py-2 bg-gradient-to-r from-[#dc3545] to-[#c82333] text-white rounded-lg hover:opacity-90 transition-opacity"
          >
            승인
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* 스크롤바 스타일링 */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #ff6b4a;
  border-radius: 10px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #e55a39;
}
</style>
