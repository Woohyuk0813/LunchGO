<script setup lang="ts">
import { ref, watch, computed, onUnmounted, onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { ArrowLeft, User, Camera } from 'lucide-vue-next';
import BusinessHeader from '@/components/ui/BusinessHeader.vue';
import BusinessSideBar from '@/components/ui/BusinessSideBar.vue';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';

const router = useRouter();
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

// 이미지 상태 관리
const profileImage = ref<string | null>(null);
const selectedImageFile = ref<File | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);
const showSuccess = ref(false);

// 사업자 정보
const loginId = ref('');
const name = ref('');
const startAt = ref('');
const businessNum = ref('');

// 전화번호 관련 상태
const phoneNumber = ref('');
const isPhoneEditable = ref(false);
const showPhoneVerification = ref(false);
const isVerificationRequested = ref(false);

// 인증번호 관련 상태
const verificationCode = ref('');
const isTimeout = ref(false);
const isPhoneVerified = ref(false);

// 타이머 상태
const timer = ref(180);
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null);

// 휴대폰 번호 자동 포맷팅
watch(phoneNumber, (newVal) => {
  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  if (cleaned.length > 11) formatted = cleaned.slice(0, 11);

  if (cleaned.length > 3 && cleaned.length <= 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
  } else if (cleaned.length > 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(
      7
    )}`;
  }

  if (newVal !== formatted) phoneNumber.value = formatted;
});

// 타이머 포맷 (MM:SS)
const formattedTimer = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0');
  const s = (timer.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

// 타이머 시작
const startTimer = () => {
  if (timerInterval.value) clearInterval(timerInterval.value);
  timer.value = 180;
  isTimeout.value = false;

  timerInterval.value = setInterval(() => {
    if (timer.value > 0) {
      timer.value--;
    } else {
      if (timerInterval.value) clearInterval(timerInterval.value);
      isTimeout.value = true;
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
});

const fetchOwnerInfo = async () => {
  const resolvedOwnerId = ownerId.value;
  if (!resolvedOwnerId) {
    alert('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
    return;
  }

  try {
    const response = await httpRequest.get(`/api/info/business/${resolvedOwnerId}`);
    const data = response.data;

    loginId.value = data.loginId || '';
    name.value = data.name || '';
    phoneNumber.value = data.phone || '';
    businessNum.value = data.businessNum || '';
    startAt.value = data.startAt || '';

    if (data.image) {
      profileImage.value = data.image;
    }
  } catch (error) {
    const status = error?.response?.status;

    if (status === 404) {
      alert('[404 Not Found] 해당 사용자가 존재하지 않습니다.');
    } else {
      alert(`오류가 발생했습니다. (Code: ${status ?? 'unknown'})`);
    }
  }
};

onMounted(() => {
  fetchOwnerInfo();
});

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleImageUpload = (event: Event) => {
  const file = (event.target as HTMLInputElement).files?.[0];
  if (file) {
    selectedImageFile.value = file;
    const reader = new FileReader();
    reader.onload = (e) => {
      profileImage.value = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }
};

const handleVerifyCode = async () => {
  if (!verificationCode.value) return alert('인증번호를 입력해주세요.');

  try {
    const response = await httpRequest.post('/api/sms/verify', {
      phone: phoneNumber.value,
      verifyCode: verificationCode.value,
    });

    if (response.data === true) {
      alert('인증이 완료되었습니다.');

      // 인증 성공 처리
      isPhoneVerified.value = true;
      showPhoneVerification.value = false;
      isPhoneEditable.value = false;
      isVerificationRequested.value = false;

      if (timerInterval.value) clearInterval(timerInterval.value);
    } else {
      alert('인증번호가 일치하지 않습니다. 다시 확인해주세요.');
      isPhoneVerified.value = false;
    }
  } catch (error) {
    const status = error?.response?.status;
    if (status === 400)
      alert('[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.');
    else alert(`오류가 발생했습니다. (Code: ${status ?? 'unknown'})`);

    isPhoneVerified.value = false;
  }
};

const checkInputElement = () => {
  if (isPhoneEditable.value) {
    if (!isPhoneVerified.value)
      return alert('전화번호 수정 시 인증은 필수입니다.');
  }
  return null;
};

const handlePhoneBtn = async () => {
  if (!isPhoneEditable.value) {
    isPhoneVerified.value = false;
    isPhoneEditable.value = true;
    isVerificationRequested.value = false;
    showPhoneVerification.value = false;
  } else {
    if (phoneNumber.value.length < 12) {
      alert('올바른 전화번호를 입력해주세요.');
      return;
    }

    isPhoneVerified.value = false;
    verificationCode.value = '';

    alert(`인증번호를 발송했습니다: ${phoneNumber.value}`);
    try {
      await httpRequest.post('/api/sms/send', { phone: phoneNumber.value });

      showPhoneVerification.value = true;
      isVerificationRequested.value = true;
      startTimer();
    } catch (error) {
      const status = error?.response?.status;

      if (status === 400)
        alert('[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.');
      else
        alert(
          `메시지 전송에 오류가 발생했습니다. (Code: ${status ?? 'unknown'})`
        );
    }
  }
};

// 최종 수정
const handleSave = async () => {
  if (checkInputElement() !== null) return;

  const resolvedOwnerId = ownerId.value;
  if (!resolvedOwnerId) {
    alert('로그인 정보를 찾을 수 없습니다. 다시 로그인해주세요.');
    return;
  }

  try {
    const formData = new FormData();

    const infoData = {
      phone: phoneNumber.value,
      image: selectedImageFile.value ? null : profileImage.value,
    };

    const jsonBlob = new Blob([JSON.stringify(infoData)], {
      type: 'application/json',
    });
    formData.append('info', jsonBlob);

    if (selectedImageFile.value) {
      formData.append('image', selectedImageFile.value);
    }

    await httpRequest.put(`/api/info/business/${resolvedOwnerId}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    // 성공 처리
    showSuccess.value = true;
    isPhoneEditable.value = false;

    // 파일 객체 초기화
    selectedImageFile.value = null;

    setTimeout(() => (showSuccess.value = false), 3000);
  } catch (error) {
    const status = error?.response?.status;

    if (status === 404) {
      alert('[404 Not Found] 해당 사용자가 존재하지 않습니다.');
    } else {
      alert(`오류가 발생했습니다. (Code: ${status ?? 'unknown'})`);
    }
  }
};

const handleWithdraw = () => {
  if (confirm('정말로 탈퇴하시겠습니까?')) {
    alert('회원 탈퇴가 완료되었습니다.');
    router.push('/');
  }
};
</script>

<template>
  <div class="flex h-screen bg-[#f8f9fa]">
    <BusinessSideBar activeMenu="business-mypage" />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-y-auto">
      <BusinessHeader />

      <main class="flex-1 max-w-3xl mx-auto w-full px-4 lg:px-8 py-8 lg:py-10">
        <section class="flex flex-col items-center mb-8">
          <div class="relative">
            <div
              class="w-32 h-32 bg-[#F1F3F5] rounded-full flex items-center justify-center border-4 border-white shadow-sm overflow-hidden"
            >
              <img
                v-if="profileImage"
                :src="profileImage"
                class="w-full h-full object-cover"
              />
              <User v-else class="w-16 h-16 text-[#ADB5BD]" />
            </div>
            <input
              type="file"
              ref="fileInput"
              class="hidden"
              accept="image/*"
              @change="handleImageUpload"
            />
            <button
              @click="triggerFileInput"
              class="absolute bottom-1 right-1 bg-white border border-[#DEE2E6] p-2.5 rounded-full shadow-md hover:bg-[#F8F9FA] transition-all active:scale-95 group"
            >
              <Camera
                class="w-5 h-5 text-[#1E3A5F] group-hover:text-[#1E3A5F]"
              />
            </button>
          </div>
          <div class="mt-4 text-center">
            <h2 class="text-2xl font-bold text-[#1E3A5F]">{{ name }} 님</h2>
          </div>
        </section>

        <div class="space-y-6">
          <div class="info-card">
            <div class="card-title border-b border-[#F1F3F5]">기본 정보</div>
            <div class="p-6 lg:p-8 space-y-6">
              <div class="input-group">
                <label>아이디</label>
                <input
                  v-model="loginId"
                  type="text"
                  readonly
                  class="input-field"
                />
              </div>

              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div class="input-group">
                  <label>이름</label>
                  <input
                    v-model="name"
                    type="text"
                    readonly
                    class="input-field"
                  />
                </div>
              </div>

              <div class="input-group">
                <label>전화번호</label>
                <div class="flex gap-2">
                  <input
                    v-model="phoneNumber"
                    type="tel"
                    maxlength="13"
                    :readonly="!isPhoneEditable"
                    class="input-field flex-1"
                  />
                  <button
                    @click="handlePhoneBtn"
                    class="btn-outline-sm whitespace-nowrap px-4"
                  >
                    {{
                      !isPhoneEditable
                        ? '번호변경'
                        : isVerificationRequested
                        ? '재전송'
                        : '인증요청'
                    }}
                  </button>
                </div>

                <p
                  v-if="!isTimeout && isPhoneVerified"
                  class="text-xs text-blue-500 mt-2 pl-1 font-medium"
                >
                  인증되었습니다.
                </p>

                <div
                  v-if="showPhoneVerification"
                  class="mt-3 bg-[#F8F9FA] p-4 rounded-xl border border-[#F1F3F5]"
                >
                  <div class="flex gap-2 items-center">
                    <input
                      v-model="verificationCode"
                      type="text"
                      placeholder="인증번호 6자리"
                      class="input-field flex-1"
                      maxlength="6"
                    />
                    <button
                      class="h-[48px] px-6 bg-[#FF6B4A] hover:bg-[#fa5a36] text-white rounded-xl font-bold transition-colors shadow-sm whitespace-nowrap flex-shrink-0"
                      @click="handleVerifyCode"
                    >
                      확인
                    </button>
                  </div>
                  <div class="flex justify-between items-center mt-2 pl-1">
                    <p
                      v-if="!isTimeout"
                      class="text-[12px] text-[#FF6B4A] font-medium"
                    >
                      {{ `남은 시간: ${formattedTimer}` }}
                    </p>
                    <p v-else class="text-[12px] text-red-500 font-medium">
                      시간 초과 (재전송 필요)
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="info-card">
            <div class="card-title border-b border-[#F1F3F5]">사업자 정보</div>
            <div class="p-6 lg:p-8 space-y-6">
              <div class="input-group">
                <label>사업자등록번호</label>
                <input
                  v-model="businessNum"
                  type="text"
                  readonly
                  class="input-field"
                />
              </div>

              <div class="input-group">
                <label>개업일자</label>
                <input
                  v-model="startAt"
                  type="text"
                  readonly
                  class="input-field"
                />
              </div>
            </div>
          </div>

          <div class="pt-4 flex flex-col gap-4">
            <button
              @click="handleSave"
              class="btn-primary-gradient shadow-lg hover:shadow-xl transform transition-all"
            >
              수정 사항 저장하기
            </button>
            <div class="flex justify-center">
              <button
                @click="handleWithdraw"
                class="text-[#1E3A5F] text-sm underline underline-offset-4 hover:text-[#1E3A5F] transition-colors"
              >
                회원 탈퇴
              </button>
            </div>
          </div>
        </div>
      </main>

      <Transition name="fade">
        <div v-if="showSuccess" class="toast">
          <div class="flex items-center gap-3">
            <div class="bg-white rounded-full p-1 shadow-sm">
              <svg
                class="w-4 h-4 text-[#FF6B4A]"
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
            <span class="font-medium">성공적으로 저장되었습니다.</span>
          </div>
        </div>
      </Transition>
    </div>
  </div>
</template>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css');

.font-pretendard {
  font-family: 'Pretendard', sans-serif;
}

/* Card Styles */
.info-card {
  background: #ffffff;
  border-radius: 16px;
  border: 1px solid #e9ecef;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  transition: box-shadow 0.2s;
}

.info-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-title {
  padding: 20px 24px;
  font-size: 16px;
  font-weight: 700;
  color: #1e3a5f;
  background-color: #fff;
}

/* Input Styles */
.input-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #1e3a5f;
  margin-bottom: 8px;
}

.input-field {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  font-size: 15px;
  border: 1px solid #dee2e6;
  border-radius: 12px;
  transition: all 0.2s ease-in-out;
  color: #1e3a5f;
  background-color: #fff;
}

/* Readonly 상태 */
.input-field:read-only {
  background-color: #f8f9fa;
  color: #1e3a5f;
  cursor: default;
  opacity: 1;
  -webkit-text-fill-color: #1e3a5f;
}

.input-field:read-only:focus {
  border-color: #dee2e6;
  box-shadow: none;
  outline: none;
}

.input-field:not(:read-only):focus {
  outline: none;
  border-color: #ff6b4a;
  box-shadow: 0 0 0 3px rgba(255, 107, 74, 0.1);
}

.input-field::placeholder {
  color: #1e3a5f;
}

/* Button Styles */
.btn-outline-sm {
  height: 48px;
  padding: 0 20px;
  font-size: 14px;
  font-weight: 600;
  color: #1e3a5f;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-outline-sm:hover {
  background-color: #f8f9fa;
  border-color: #ced4da;
}

.btn-primary-gradient {
  width: 100%;
  height: 56px;
  background: linear-gradient(135deg, #ff6b4a 0%, #ff8e72 100%);
  color: white;
  font-size: 16px;
  font-weight: 700;
  border: none;
  border-radius: 16px;
  cursor: pointer;
  transition: transform 0.2s, opacity 0.2s;
}

.btn-primary-gradient:hover {
  opacity: 0.95;
}

.btn-primary-gradient:active {
  transform: scale(0.99);
}

/* Toast */
.toast {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  background: #343a40;
  color: white;
  padding: 14px 28px;
  border-radius: 50px;
  font-size: 15px;
  z-index: 100;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  white-space: nowrap;
  width: max-content;
  max-width: 90vw;
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
