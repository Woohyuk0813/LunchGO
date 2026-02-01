<script setup>
import { computed, ref, onMounted } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { ArrowLeft, Check } from "lucide-vue-next";
import httpRequest from "@/router/httpRequest";
import Button from "@/components/ui/Button.vue";
import Card from "@/components/ui/Card.vue";

const route = useRoute();
const router = useRouter();

const reservationId = computed(() => String(route.params.id));

const reasons = [
  "일정이 변경되었어요",
  "동행 인원이 바뀌었어요",
  "다른 식당으로 변경했어요",
  "결제/예약 과정에서 문제가 있었어요",
  "기타",
];

const reason = ref("");
const detail = ref("");

// (필수) 취소/환불 정책 동의
const agree = ref(false);

// (선택) 환불 신청
const requestRefund = ref(true);

const submitting = ref(false);
const errorMsg = ref("");

// 예약 상세 더미 (백엔드 연동 시 fetchReservationSummary만 교체)
const reservationSummary = ref({
  restaurantName: "",
  address: "",
  date: "",
  time: "",
  partySize: "",
  paymentType: "",
  paidAmount: "",
});

const isSummaryLoading = ref(false);

const fetchReservationSummary = async () => {
  isSummaryLoading.value = true;
  try {
    const response = await httpRequest.get(`/api/reservations/${reservationId.value}/summary`);
    const data = response?.data || {};

    reservationSummary.value = {
      restaurantName: data.restaurant?.name || "",
      address: data.restaurant?.address || "",
      date: data.booking?.date || "",
      time: data.booking?.time || "",
      partySize: String(data.booking?.partySize ?? ""),
      paymentType: data.payment?.type || "",
      paidAmount: String(data.payment?.amount ?? ""),
    };
  } finally {
    isSummaryLoading.value = false;
  }
};

const paidAmountNumber = computed(() => {
  const n = Number(reservationSummary.value.paidAmount);
  return Number.isFinite(n) ? n : 0;
});

// ===== 약관 모달 =====
const isTermsModalOpen = ref(false);
const modalTitle = ref("");
const modalContent = ref("");

// 네가 준 약관 전문 그대로 (효과/마크다운 없이)
const REFUND_POLICY_TEXT = `
취소 및 환불 약관

제 1 조 (환불의 기본 원칙)

① 환불은 예약 취소 시점, 예약 유형, 예약 인원, 카드 유형 등을 기준으로 산정되며, 본 약관에 따라 계산된 금액을 기준으로 처리됩니다.

② 환불은 결제대행사(PG)의 환불 절차를 통해 진행되며, 실제 환불 완료까지는 영업일 기준 일정 시간이 소요될 수 있습니다.

③ 예약 취소 후 환불이 진행되면 해당 예약의 상태는 “취소/환불 처리 중” 또는 “환불 완료” 상태로 변경됩니다.

제 2 조 (취소 시점별 환불 비율)

① 회원의 예약 취소 시점에 따른 환불 비율은 다음과 같습니다.

1. 예약 전일 취소: 결제 금액의 100% 환불
2. 예약 당일, 예약시간 기준 2시간 전까지 취소: 결제 금액의 50% 환불
3. 예약시간 2시간 이내 취소: 결제 금액의 20% 환불
4. 예약시간 2시간 이내 취소이면서 예약 인원 8인 이상인 경우: 결제 금액의 10% 환불
5. 노쇼(No-show): 환불 불가(0% 환불)

② 전일/당일, 2시간 전/2시간 이내의 판단 기준은 예약 확정 시 시스템에 기록된 예약일시(날짜 및 시간)를 기준으로 합니다.

제 3 조 (예약 유형별 환불 산정 방식)

① 예약금 결제 예약([예약하기])

1. 환불 기준 금액은 회원이 실제 결제한 예약금 전액입니다.
2. 최종 환불 금액은 다음과 같이 산정합니다.

최종 환불액 = 예약금 × 제2조의 환불 비율

3. 단, 제5조(예외 규정)에 해당하는 경우에는 해당 규정이 우선 적용됩니다.

② 선주문/선결제 예약([선주문/선결제])

1. 선주문/선결제 예약은 예약금이 별도로 결제되지 않으나, 취소 및 환불 산정을 위해 회사는 예약금 가정액을 적용합니다.
2. 예약금 가정액은 다음 기준에 따라 산정합니다.
1. 예약 인원 1~6인: 인당 5,000원
2. 예약 인원 7인 이상: 인당 10,000원
3. 예약금 가정액 = (인당 기준 금액 × 예약 인원 수)
4. 단, 예약금 가정액은 선결제된 음식값 총액을 초과할 수 없으며, 초과 시 음식값 총액을 한도로 합니다.
3. 선주문/선결제 예약의 환불은 다음과 같이 분리 산정됩니다.
1. 메뉴금액 잔액(선결제 금액 − 예약금 가정액): 취소 시점과 관계없이 100% 환불
2. 예약금 가정액: 제2조의 환불 비율을 적용하여 환불
4. 최종 환불 금액은 다음 공식에 따라 계산됩니다.

최종 환불액 = (메뉴금액 잔액 × 100%) + (예약금 가정액 × 제2조 환불 비율)

제 4 조 (환불 처리 절차)

① 회사는 본 약관에 따라 산정된 환불 금액을 기준으로 결제대행사(PG)를 통해 환불을 요청합니다.

② 환불 요청이 완료되면 결제 상태는 “환불 처리 중”으로 변경되며, PG 처리 완료 후 “환불 완료” 상태로 변경됩니다.

③ 회사는 환불 금액, 환불 비율, 결제 유형 등 환불 처리 결과를 플랫폼 화면 또는 알림을 통해 안내할 수 있습니다.

제 5 조 (예외 규정)

① 법인카드 결제 후 회원 취소

회원이 직접 예약을 취소하였으나, 결제 카드가 법인카드인 경우에는 위약금 성격의 금액을 면제하고 다음과 같이 환불합니다.

1. 예약금 결제 예약: 취소 시점과 관계없이 예약금 전액 환불
2. 선주문/선결제 예약:
- 메뉴금액 잔액 100% 환불
- 예약금 가정액 100% 환불
- 결과적으로 선결제 금액 전액 환불

② 점주에 의한 강제 취소

점주가 매장 사정 등으로 예약을 강제 취소하는 경우(취소 사유 입력 필수), 카드 유형과 관계없이 다음과 같이 환불합니다.

1. 예약금 결제 예약: 예약금 전액 환불
2. 선주문/선결제 예약: 선결제 금액 전액 환불

제 6 조 (자동 취소 및 환불 여부)

① 다음 각 호의 사유로 임시 예약이 자동 취소되는 경우, 결제가 완료되지 않은 상태이므로 환불 절차는 발생하지 않습니다.

1. 임시 예약 생성 후 7분 이내에 결제가 진행되지 않은 경우
2. 결제 요청 후 결제 가능 시간 10분 내 결제가 완료되지 않은 경우(타임아웃)

② 자동 취소 시 임시로 차감되었던 예약 인원은 즉시 복구됩니다.
`.trim();

const openRefundModal = () => {
  isTermsModalOpen.value = true;
  modalTitle.value = "취소 및 환불 약관";
  modalContent.value = REFUND_POLICY_TEXT;
};

const closeModal = () => {
  isTermsModalOpen.value = false;
};

// ===== 기타 사유: 50자 이내(최대 50자) =====
const isOther = computed(() => reason.value === "기타");
const otherLen = computed(() => detail.value.trim().length);

const canSubmit = computed(() => {
  if (!reason.value) return false;
  if (!agree.value) return false;

  if (reason.value === "기타") {
    const len = detail.value.trim().length;
    if (len === 0) return false;
    if (len > 50) return false;
  }
  return true;
});

async function cancelReservation(id, payload) {
  const response = await httpRequest.post(`/api/reservations/${id}/cancel`, payload);
  return response?.data;
}

const submitCancel = async () => {
  if (!canSubmit.value || submitting.value) return;

  try {
    submitting.value = true;
    errorMsg.value = "";

    const payload = {
      reason: reason.value,
      detail: detail.value.trim(),
      refund: {
        requested: requestRefund.value,
        amount: paidAmountNumber.value,
        paymentKey: null,
        transactionId: null,
        pgProvider: null,
      },
    };

    const res = await cancelReservation(reservationId.value, payload);
    if (!res?.ok) throw new Error("예약 취소에 실패했습니다.");

    router.replace({ path: "/my-reservations" });
  } catch (e) {
    errorMsg.value = e?.message ?? "오류가 발생했습니다.";
  } finally {
    submitting.value = false;
  }
};

const selectReason = (r) => {
  reason.value = r;
  if (r !== "기타") detail.value = "";
};

onMounted(() => {
  fetchReservationSummary();
});
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink to="/my-reservations" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">예약 취소</h1>
      </div>
    </header>

    <!-- Reservation Number -->
    <div class="bg-white px-4 py-6 border-b border-[#e9ecef]">
      <div class="text-sm text-[#6c757d]">
        예약번호: <span class="font-medium text-[#1e3a5f]">{{ reservationId }}</span>
      </div>
    </div>

    <main class="max-w-[500px] mx-auto pb-32">
      <div
        v-if="errorMsg"
        class="mx-4 mt-4 bg-white border border-[#f1aeb5] text-[#b02a37] rounded-xl p-4"
      >
        {{ errorMsg }}
      </div>

      <!-- Reservation Detail -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <div class="flex items-center justify-between mb-2">
          <h2 class="text-base font-semibold text-[#1e3a5f]">예약 상세</h2>
          <span v-if="isSummaryLoading" class="text-xs text-[#6c757d]">불러오는 중...</span>
        </div>

        <div class="rounded-xl border border-[#e9ecef] bg-[#f8f9fa] px-3 py-3">
          <div class="text-sm text-[#495057] space-y-1">
            <div class="font-semibold text-[#1e3a5f]">{{ reservationSummary.restaurantName }}</div>
            <div class="text-xs text-[#6c757d]">{{ reservationSummary.address }}</div>

            <div class="pt-2 space-y-1">
              <div>
                일시: <span class="font-medium">{{ reservationSummary.date }}</span>
                {{ reservationSummary.time }}
              </div>
              <div>인원: <span class="font-medium">{{ reservationSummary.partySize }}</span>명</div>
              <div>결제유형: <span class="font-medium">{{ reservationSummary.paymentType }}</span></div>
              <div>
                결제금액:
                <span class="font-medium">{{ paidAmountNumber.toLocaleString() }}</span>원
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Cancel Reason -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">취소 사유</h2>

        <div class="space-y-3">
          <button
            v-for="r in reasons"
            :key="r"
            type="button"
            @click="selectReason(r)"
            :class="`w-full text-left transition-all ${reason === r ? 'ring-2 ring-[#ff6b4a]' : ''}`"
          >
            <Card class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md">
              <div class="flex items-center gap-3">
                <div
                  :class="`w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 ${
                    reason === r ? 'gradient-primary' : 'bg-[#f8f9fa]'
                  }`"
                >
                  <div
                    v-if="reason === r"
                    class="w-6 h-6 rounded-full gradient-primary flex items-center justify-center"
                  >
                    <Check class="w-4 h-4 text-white" />
                  </div>
                </div>

                <div class="flex-1">
                  <p :class="`font-semibold mb-0.5 ${reason === r ? 'text-[#ff6b4a]' : 'text-[#1e3a5f]'}`">
                    {{ r }}
                  </p>
                  <p class="text-xs text-[#6c757d]">
                    <template v-if="r === '일정이 변경되었어요'">방문 일정이 바뀌어 예약을 취소합니다.</template>
                    <template v-else-if="r === '동행 인원이 바뀌었어요'">인원 변경으로 예약을 다시 잡을 예정입니다.</template>
                    <template v-else-if="r === '다른 식당으로 변경했어요'">다른 매장으로 예약을 변경합니다.</template>
                    <template v-else-if="r === '결제/예약 과정에서 문제가 있었어요'">예약 또는 결제 과정에서 오류가 발생했습니다.</template>
                    <template v-else>직접 사유를 입력합니다.</template>
                  </p>
                </div>
              </div>
            </Card>
          </button>
        </div>

        <!-- 기타: 50자 이내 입력 -->
        <div v-if="isOther" class="mt-3">
          <textarea
            v-model="detail"
            class="w-full border border-[#e9ecef] rounded-xl p-3 text-sm outline-none bg-white"
            rows="3"
            maxlength="50"
            placeholder="취소 사유를 50자 이내로 입력해 주세요(필수)"
          />
          <div class="mt-2 flex items-center justify-between text-xs">
            <span :class="otherLen === 0 ? 'text-[#ff6b4a]' : 'text-[#6c757d]'">
              {{ otherLen === 0 ? "사유를 입력해야 합니다." : "입력 완료" }}
            </span>
            <span class="text-[#6c757d]">{{ otherLen }}/50</span>
          </div>
        </div>
      </div>

      <!-- Refund Request -->
      <div class="bg-white px-4 py-5 border-b border-[#e9ecef]">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-2">환불 신청</h2>
        <p class="text-xs text-[#6c757d] leading-relaxed mb-4">
          환불은 결제대행사(PG) 승인 후 처리됩니다. 실제 환불 완료까지 시간이 걸릴 수 있습니다.
        </p>

        <label
          :class="`flex items-start gap-3 cursor-pointer group rounded-xl p-2 -m-2 transition-colors ${
            requestRefund ? 'bg-[#fff5f3]' : ''
          }`"
        >
          <div class="relative flex-shrink-0 mt-0.5">
            <input type="checkbox" v-model="requestRefund" class="sr-only" />
            <div
              :class="`w-5 h-5 rounded border-2 flex items-center justify-center transition-all ${
                requestRefund ? 'bg-[#ff6b4a] border-[#ff6b4a]' : 'bg-white border-[#dee2e6]'
              } group-hover:border-[#ff6b4a]`"
            >
              <Check v-if="requestRefund" class="w-3.5 h-3.5 text-white" />
            </div>
          </div>

          <div class="flex-1">
            <p class="text-sm text-[#1e3a5f] font-medium mb-0.5">환불 신청을 함께 진행합니다</p>
            <p class="text-xs text-[#6c757d]">예상 환불 금액: {{ paidAmountNumber.toLocaleString() }}원</p>
          </div>
        </label>
      </div>

      <!-- Policy Agreement -->
      <div class="px-4 py-5 bg-white border-t border-b border-[#e9ecef]">
        <h2 class="text-base font-semibold text-[#1e3a5f] mb-4">취소 및 환불 약관</h2>

        <label
          :class="`flex items-start gap-3 cursor-pointer group rounded-xl p-2 -m-2 transition-colors ${
            agree ? 'bg-[#fff5f3]' : ''
          }`"
        >
          <div class="relative flex-shrink-0 mt-0.5">
            <input type="checkbox" v-model="agree" class="sr-only" />
            <div
              :class="`w-5 h-5 rounded border-2 flex items-center justify-center transition-all ${
                agree ? 'bg-[#ff6b4a] border-[#ff6b4a]' : 'bg-white border-[#dee2e6]'
              } group-hover:border-[#ff6b4a]`"
            >
              <Check v-if="agree" class="w-3.5 h-3.5 text-white" />
            </div>
          </div>

          <div class="flex-1">
            <p class="text-sm text-[#1e3a5f] font-medium mb-0.5">(필수) 취소 및 환불 약관 동의</p>
            <button
              type="button"
              @click.stop="openRefundModal"
              class="text-xs text-[#6c757d] underline hover:text-[#ff6b4a]"
            >
              자세히 보기
            </button>
          </div>
        </label>

        <div class="mt-4">
          <Card class="p-4 border-[#e9ecef] rounded-xl bg-[#f8f9fa]">
            <h3 class="text-sm font-semibold text-[#1e3a5f] mb-2">안내</h3>
            <ul class="space-y-1 text-xs text-[#6c757d] leading-relaxed">
              <li>• 취소 요청이 완료되면 내 예약 목록에서 상태가 갱신됩니다.</li>
              <li>• 환불 신청을 선택한 경우, PG 처리 완료 후 환불이 반영됩니다.</li>
              <li>• 정책에 따라 환불 금액이 달라질 수 있습니다.</li>
            </ul>
          </Card>
        </div>
      </div>
    </main>

    <!-- Terms Modal -->
    <div v-if="isTermsModalOpen" class="fixed inset-0 z-[999]">
      <div class="absolute inset-0 bg-black/40" @click="closeModal"></div>

      <div class="absolute left-1/2 top-1/2 w-[calc(100%-32px)] max-w-[500px] -translate-x-1/2 -translate-y-1/2">
        <Card class="p-5 rounded-2xl bg-white border-[#e9ecef] shadow-lg">
          <div class="flex items-start justify-between gap-3">
            <h3 class="text-base font-semibold text-[#1e3a5f]">{{ modalTitle }}</h3>
            <button type="button" class="text-sm text-[#6c757d] hover:text-[#ff6b4a]" @click="closeModal">
              닫기
            </button>
          </div>

          <div class="mt-3 max-h-[60vh] overflow-auto text-sm text-[#495057] leading-relaxed whitespace-pre-line">
            {{ modalContent }}
          </div>

          <div class="mt-4">
            <Button
              type="button"
              class="w-full h-11 gradient-primary text-white font-semibold rounded-xl"
              @click="closeModal"
            >
              확인
            </Button>
          </div>
        </Card>
      </div>
    </div>

    <!-- Fixed Bottom Button -->
    <div class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="max-w-[500px] mx-auto px-4 py-3">
        <Button
          @click="submitCancel"
          :disabled="!canSubmit || submitting"
          class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {{ submitting ? "취소 처리 중..." : "예약 취소 확정" }}
        </Button>
      </div>
    </div>
  </div>
</template>
