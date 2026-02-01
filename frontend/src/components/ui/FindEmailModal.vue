<script setup lang="ts">
import { ref, watch, computed, onUnmounted } from 'vue';
import httpRequest from '@/router/httpRequest';

const props = defineProps<{
  isVisible: boolean;
  userType: string;
}>();

const emit = defineEmits(['close']);

// 단계 관리 (1: 정보 입력, 2: 이메일 확인 결과)
const step = ref(1);

const name = ref('');
const phone = ref('');

// 인증번호 관련 상태
const isCodeSent = ref(false);
const verificationCode = ref('');
const isTimeout = ref(false);
const foundEmail = ref(''); // 찾은 이메일
const isPhoneVerified = ref(false);

// 타이머 상태
const timer = ref(180);
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null);

// 모달이 닫힐 때 상태 초기화
watch(
  () => props.isVisible,
  (newVal) => {
    if (!newVal) {
      if (timerInterval.value) clearInterval(timerInterval.value);
      // 애니메이션 시간을 고려해 약간 뒤에 초기화
      setTimeout(() => {
        step.value = 1;
        name.value = '';
        phone.value = '';
        verificationCode.value = '';
        isCodeSent.value = false;
        isTimeout.value = false;
        foundEmail.value = '';
        isPhoneVerified.value = false;
        timer.value = 180;
      }, 300);
    }
  }
);

//휴대폰 번호 자동 포맷팅
watch(phone, (newVal) => {
  // 숫자만 남기고 제거
  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  // 11자리까지만 입력 가능하도록 제한
  if (cleaned.length > 11) {
    formatted = cleaned.slice(0, 11);
  }

  // 포맷팅 로직 apply
  if (cleaned.length > 3 && cleaned.length <= 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
  } else if (cleaned.length > 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(
      7
    )}`;
  }

  // 값이 변경되었을 때만 업데이트
  if (newVal !== formatted) {
    phone.value = formatted;
  }
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
      alert('인증번호 입력 시간이 초과되었습니다. 재발송이 필요합니다.');
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
});

// 인증번호 발송
const handleSendVerifyCode = async () => {
  if (!name.value) return alert('이름을 입력해주세요.');
  if (!phone.value) return alert('휴대폰 번호를 입력해주세요.');

  isPhoneVerified.value = false;
  verificationCode.value = '';

  alert(`인증번호를 발송했습니다: ${phone.value}`);
  try {
    await httpRequest.post('/api/sms/send', {phone: phone.value});

    isCodeSent.value = true;
    startTimer(); 
  }catch(error){
    const status = error.response.status;

    if(status === 400) alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else alert(`메시지 전송에 오류가 발생했습니다. (Code: ${status})`);
  }  
};

// 인증번호 확인
const handleVerifyCode = async () => {
  if (!verificationCode.value) return alert('인증번호를 입력해주세요.');
  if (isTimeout.value) return alert('입력 시간이 초과되었습니다. 재발송해주세요.');

  try {
    const response = await httpRequest.post('/api/sms/verify', {
      phone: phone.value,
      verifyCode: verificationCode.value
    });

    if(response.data === true){
      alert('인증이 완료되었습니다.');
      isPhoneVerified.value = true; // 인증 완료 상태로 변경

      // 타이머 정지
      if (timerInterval.value) clearInterval(timerInterval.value);
    } else{
      alert("인증번호가 일치하지 않습니다. 다시 확인해주세요.");

      isPhoneVerified.value = false;
    }
  } catch (error) {
    // 에러 처리
    const status = error.response.status;
    if (status === 400) alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else alert(`오류가 발생했습니다. (Code: ${status})`);
    
    isPhoneVerified.value = false;
  }
};

// 이메일 찾기 (최종 제출)
const handleFindEmail = async () => {
  if (!name.value) return alert('이름을 입력해주세요.');
  if (!phone.value) return alert('휴대폰 번호를 입력해주세요.');
  if (!isCodeSent.value) return alert('인증번호를 먼저 발송해주세요.');
  if (!verificationCode.value) return alert('인증번호를 입력해주세요.');
  if(!isPhoneVerified.value) return alert("인증번호 확인은 필수입니다.");

  try {
    const response = await httpRequest.post('/api/auth/search/email',{
      name: name.value,
      phone: phone.value
    });

    const realEmail = response.data.email;

    // 1. 이메일을 @ 기준으로 분리
    const [localPart, domain] = realEmail.split('@');

    // 2. 앞부분(아이디) 길이 계산
    const totalLen = localPart.length;

    // 3. 보여줄 길이 계산 (절반 올림)
    const visibleLen = Math.ceil(totalLen / 2);

    // 4. 마스킹 처리: (보여줄 부분) + (나머지 길이만큼 *) + @도메인
    const maskedEmail =
      localPart.slice(0, visibleLen) +
      '*'.repeat(totalLen - visibleLen) +
      '@' +
      domain;

    foundEmail.value = maskedEmail;

    // 성공 시 단계 변경 (결과 화면으로 이동)
    step.value = 2;
    // 타이머 정지
    if (timerInterval.value) clearInterval(timerInterval.value);
  }catch(error){
    const status = error.response.status;

    switch(status){
      case 400:
        alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
        break;
      case 404:
        alert("[404 Not Found] 해당 이메일은 존재하지 않습니다.");
        handleGoToLogin(); //모달 닫기
        break;
      default:
        alert(`오류가 발생했습니다. (Code: ${status})`);
    }
  }
};

// 로그인하러 가기 버튼 (모달 닫기)
const handleGoToLogin = () => {
  emit('close');
};
</script>

<template>
  <Teleport to="body">
    <div v-if="isVisible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content">
        <div class="modal-header">
          <h3>이메일 찾기</h3>
          <button class="close-btn" @click="$emit('close')">&times;</button>
        </div>

        <div class="modal-body">
          <template v-if="step === 1">
            <p class="guide-text">가입 시 등록한 정보를 입력해주세요.</p>

            <form @submit.prevent="handleFindEmail">
              <div class="input-group">
                <label for="find-name">이름</label>
                <input
                  id="find-name"
                  v-model="name"
                  type="text"
                  placeholder="이름을 입력하세요"
                  maxlength="10"
                  required
                />
              </div>

              <div class="input-group">
                <label for="find-phone">휴대폰 번호</label>
                <div class="input-with-button">
                  <input
                    id="find-phone"
                    v-model="phone"
                    type="tel"
                    placeholder="010-0000-0000"
                    required
                    class="flex-grow-input"
                    :disabled="isCodeSent"
                  />
                  <button
                    type="button"
                    class="btn-secondary"
                    @click="handleSendVerifyCode"
                  >
                    {{ isCodeSent ? '재전송' : '인증번호 발송' }}
                  </button>
                </div>
              </div>

              <div v-if="isCodeSent" class="input-group slide-in">
                <label for="verify-code">인증번호</label>
                <div class="input-with-button">
                  <div style="position: relative; flex: 1;">
                    <input
                      id="verify-code"
                      v-model="verificationCode"
                      type="text"
                      placeholder="인증번호 6자리"
                      class="input-field"
                      maxlength="6"
                      style="width: 100%;" 
                      :disabled="isPhoneVerified"
                    />
                    <p class="timer-text">{{ formattedTimer }}</p>
                  </div>
                  
                  <button
                    type="button"
                    class="btn-secondary"
                    @click="handleVerifyCode"
                    :disabled="isPhoneVerified || isTimeout"
                    :style="isPhoneVerified ? 'color: #20c997; border-color: #20c997;' : ''"
                  >
                    {{ isPhoneVerified ? '인증완료' : '확인' }}
                  </button>
                </div>
                 <p v-if="isTimeout" style="color: red; font-size: 12px; margin-top: 4px;">
                    입력 시간이 초과되었습니다. 재전송해주세요.
                 </p>
              </div>

              <button type="submit" class="btn-confirm" :disabled="!isPhoneVerified">
                이메일 찾기
              </button>

              <div v-if="isCodeSent && !isPhoneVerified" class="resend-link-container">
                <button
                  type="button"
                  class="btn-text-link"
                  @click="handleSendVerifyCode"
                >
                  인증번호가 오지 않나요? <span>재전송</span>
                </button>
              </div>
            </form>
          </template>

          <template v-else>
            <div class="result-container slide-in">
              <p class="result-text-top">현재 가입하신 이메일은</p>
              <h2 class="result-email">{{ foundEmail }}</h2>
              <p class="result-text-bottom">입니다.</p>

              <button class="btn-confirm btn-login" @click="handleGoToLogin">
                로그인하기
              </button>
            </div>
          </template>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* 기존 스타일 유지 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
  backdrop-filter: blur(2px);
}
.modal-content {
  background: white;
  width: 90%;
  max-width: 400px;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  animation: slideUp 0.3s ease-out;
}
@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1e3a5f;
  font-weight: 700;
}
.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #adb5bd;
  padding: 0;
  line-height: 1;
}
.guide-text {
  font-size: 14px;
  color: #6c757d;
  margin-bottom: 24px;
  line-height: 1.5;
}
.input-group {
  margin-bottom: 16px;
  position: relative;
}
.input-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #1e3a5f;
  margin-bottom: 6px;
}
.input-group input {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  border: 1.5px solid #e9ecef;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 14px;
  color: #1e3a5f;
  transition: border-color 0.2s;
}
.input-group input:focus {
  outline: none;
  border-color: #ff6b4a;
}
.input-group input:disabled {
  background-color: #f8f9fa;
  color: #adb5bd;
}
.input-with-button {
  display: flex;
  gap: 8px;
  width: 100%;
}
.input-with-button input.flex-grow-input {
  flex: 1;
}
.btn-secondary {
  height: 48px;
  padding: 0 16px;
  background-color: #f8f9fa;
  border: 1.5px solid #e9ecef;
  border-radius: 8px;
  color: #495057;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}
.btn-secondary:hover {
  background-color: #e9ecef;
  border-color: #dee2e6;
  color: #1e3a5f;
}
.slide-in {
  animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.timer-text {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  color: #ff6b4a;
  margin: 0;
  pointer-events: none;
}
.input-group:has(label) .timer-text {
  top: auto;
  bottom: 16px;
  transform: none;
}
.btn-confirm {
  width: 100%;
  height: 48px;
  background-color: #ff6b4a;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  margin-top: 8px;
  transition: background-color 0.2s;
}
.btn-confirm:hover {
  background-color: #e5553a;
}
.resend-link-container {
  margin-top: 12px;
  text-align: center;
  animation: fadeIn 0.5s ease-out;
}
.btn-text-link {
  background: none;
  border: none;
  color: #adb5bd;
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  font-family: inherit;
}
.btn-text-link span {
  color: #6c757d;
  text-decoration: underline;
  margin-left: 4px;
  font-weight: 600;
  transition: color 0.2s;
}
.btn-text-link:hover span {
  color: #1e3a5f;
}

/* --- Step 2 결과 화면 스타일 추가 --- */
.result-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  text-align: center;
}

.result-text-top,
.result-text-bottom {
  font-size: 16px;
  color: #495057;
  margin: 0;
}

.result-email {
  font-size: 20px; /* 이메일은 길이가 길 수 있어 아이디보다 약간 작게 */
  color: #3b82f6; /* 강조 색상 (파란색) */
  font-weight: 800;
  margin: 16px 0;
  word-break: break-all; /* 이메일이 너무 길 경우 줄바꿈 허용 */
}

.btn-login {
  margin-top: 32px;
  background-color: #1e3a5f; /* 로그인 버튼은 짙은 남색 */
}
.btn-login:hover {
  background-color: #162c46;
}
</style>
