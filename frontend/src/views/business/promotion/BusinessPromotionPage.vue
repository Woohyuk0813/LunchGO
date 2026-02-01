<script setup>
import { ref, computed } from 'vue';
import BusinessSidebar from '@/components/ui/BusinessSideBar.vue';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';

const accountStore = useAccountStore();

const getStoredMember = () => {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('member');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
};

const member = computed(() => accountStore.member || getStoredMember());
const ownerId = computed(() => {
  const rawId = member.value?.id;
  if (rawId === null || rawId === undefined) return null;
  const parsed = Number(rawId);
  return Number.isNaN(parsed) ? null : parsed;
});

// --- 상태 관리 변수 ---
const isPromotionOn = ref(false); // 프로모션 여부 (기본값 끄기)
const promotionTitle = ref(''); // 제목
const promotionContent = ref(''); // 내용
const showSuccessMessage = ref(false); // 성공 메시지 표시 여부
const errorMsg = ref(''); // 에러 메시지

// 프로모션 상태 변경
const setPromotionStatus = (status) => {
  isPromotionOn.value = status;

  if (!status) errorMsg.value = '';
};

// 등록하기 버튼
const handleRegister = async () => {
  if (!promotionTitle.value.trim()) {
    errorMsg.value = '제목을 입력해주세요.';
    return;
  }
  if (!promotionContent.value.trim()) {
    errorMsg.value = '프로모션 내용을 입력해주세요.';
    return;
  }

  const resolvedOwnerId = ownerId.value;
  if (!resolvedOwnerId) {
    alert('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
    return;
  }

  //백엔드 전송 로직
  try {
    await httpRequest.post('/api/business/promotion', {
      ownerId: resolvedOwnerId,
      title: promotionTitle.value,
      content: promotionContent.value
    });

    // 성공 처리
    errorMsg.value = '';
    showSuccessMessage.value = true;
    setTimeout(() => (showSuccessMessage.value = false), 3000);
  } catch (error) {
    const status = error?.response?.status;

    switch(status){
      case 400:
        alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
        break;
      case 404:
        alert("[404 Not Found] 해당 사업자가 존재하지 않습니다.");
        break;
      case 429:
        alert("[429 Too Many Requests] 프로모션은 6시간 간격으로 전송가능합니다.");
        break;
      default:
        alert(`오류가 발생했습니다. (Code: ${status})`);
    }
  }
};

// 입력 시 에러 메시지 초기화
const handleInput = () => {
  if (errorMsg.value) errorMsg.value = '';
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSidebar activeMenu="business-promotion" />

    <div class="flex-1 flex flex-col overflow-hidden">
      <BusinessHeader />

      <main class="flex-1 overflow-auto p-8">
        <div class="max-w-6xl mx-auto">
          <h2 class="text-3xl font-bold text-[#1E3A5F] mb-8">프로모션 관리</h2>

          <div class="bg-white rounded-xl border border-gray-200 p-8 shadow-sm">
            <div class="flex items-center gap-6 mb-8">
              <span class="text-lg font-bold text-gray-800">프로모션 여부</span>
              <div class="flex items-center gap-6">
                <label class="flex items-center gap-2 cursor-pointer group">
                  <div
                    class="w-5 h-5 border-2 rounded flex items-center justify-center transition-colors"
                    :class="
                      isPromotionOn
                        ? 'bg-[#1E3A5F] border-[#1E3A5F]'
                        : 'border-gray-400 group-hover:border-[#1E3A5F]'
                    "
                    @click.prevent="setPromotionStatus(true)"
                  >
                    <svg
                      v-if="isPromotionOn"
                      class="w-3.5 h-3.5 text-white"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="3"
                        d="M5 13l4 4L19 7"
                      />
                    </svg>
                  </div>
                  <span
                    class="text-gray-700 font-medium group-hover:text-[#1E3A5F]"
                    >켜기</span
                  >
                </label>

                <label class="flex items-center gap-2 cursor-pointer group">
                  <div
                    class="w-5 h-5 border-2 rounded flex items-center justify-center transition-colors"
                    :class="
                      !isPromotionOn
                        ? 'bg-[#1E3A5F] border-[#1E3A5F]'
                        : 'border-gray-400 group-hover:border-[#1E3A5F]'
                    "
                    @click.prevent="setPromotionStatus(false)"
                  >
                    <svg
                      v-if="!isPromotionOn"
                      class="w-3.5 h-3.5 text-white"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="3"
                        d="M5 13l4 4L19 7"
                      />
                    </svg>
                  </div>
                  <span
                    class="text-gray-700 font-medium group-hover:text-[#1E3A5F]"
                    >끄기</span
                  >
                </label>
              </div>
            </div>

            <div v-if="isPromotionOn" class="animate-fade-in">
              <div class="mb-4">
                <input
                  type="text"
                  v-model="promotionTitle"
                  @input="handleInput"
                  placeholder="제목을 입력하세요. (최대 60자)"
                  maxlength="60"
                  class="w-full px-5 py-4 text-lg border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] focus:border-transparent transition-all placeholder-gray-400"
                />
              </div>

              <div class="mb-2 relative">
                <textarea
                  v-model="promotionContent"
                  @input="handleInput"
                  rows="12"
                  maxlength="1500"
                  placeholder="프로모션 내용을 입력하세요. (최대 1,500자)"
                  class="w-full px-5 py-4 text-base border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#FF6B4A] focus:border-transparent transition-all placeholder-gray-400 resize-none"
                ></textarea>
                <div class="absolute bottom-4 right-4 text-sm text-gray-400">
                  {{ promotionContent.length }} / 1,500
                </div>
              </div>

              <p
                v-if="errorMsg"
                class="text-red-500 text-sm font-medium mb-4 pl-1"
              >
                {{ errorMsg }}
              </p>

              <div class="text-xs text-gray-500 space-y-1 mb-8 pl-1">
                <p>
                  * 부적절한 내용이나 프로모션에 적절하지 않은 내용은 자동으로
                  차단될 수 있습니다.
                </p>
                <p>* 차단된 내용은 사용자에게 이메일로 전송되지 않습니다.</p>
              </div>

              <div class="flex justify-end">
                <button
                  @click="handleRegister"
                  class="px-10 py-3 bg-[#2f3e46] hover:bg-[#1E3A5F] text-white text-lg font-semibold rounded hover:opacity-90 transition-all shadow-md"
                >
                  등록하기
                </button>
              </div>
            </div>
          </div>

          <Transition name="fade">
            <div v-if="showSuccessMessage" class="toast">
              <div class="flex items-center gap-2">
                <div class="bg-white rounded-full p-1">
                  <svg
                    class="w-3 h-3 text-[#FF6B4A]"
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path
                      fill-rule="evenodd"
                      d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </div>
                <span>성공적으로 등록되었습니다.</span>
              </div>
            </div>
          </Transition>
        </div>
      </main>
    </div>
  </div>
</template>

<style>
/* Toast */
.toast {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: #343a40;
  color: white;
  padding: 12px 24px;
  border-radius: 50px;
  font-size: 14px;
  z-index: 100;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);

  white-space: nowrap; /* 텍스트가 절대 줄바꿈되지 않게 강제함 */
  width: max-content; /* 내용물(텍스트) 길이만큼 가로 사이즈를 확보함 */
  max-width: 90vw; /* 화면이 너무 작을 경우를 대비해 화면 너비의 90%까지만 커지게 제한 */
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translate(-50%, 20px);
}
</style>
