<script setup lang="ts">
import { ref, watch, computed, onUnmounted } from 'vue';
import httpRequest from '@/router/httpRequest';

const props = defineProps<{
  isVisible: boolean;
  userType: string;
}>();

const emit = defineEmits(['close']);

// 도달 번호 관리 (1: 본인인증, 2: 비밀번호 재설정)
const step = ref(1);

//본인인증 관련 상태
const name = ref('');
// email 변수는 userType에 따라 '이메일' 또는 '아이디' 값을 담음.
const email = ref('');
const phone = ref('');
const isCodeSent = ref(false);
const verifyCode = ref('');
const isTimeout = ref(false);
const isPhoneVerified  = ref(false);

// 두번째 모달: 비밀번호 재설정 관련 상태
const newPassword = ref('');
const confirmPassword = ref('');

// owner 여부 판단
const isOwner = computed(() => props.userType === 'owner');

// 모달이 닫힐 때 모든 상태 초기화
watch(
  () => props.isVisible,
  (newVal) => {
    if (!newVal) {
      if (timerInterval.value) clearInterval(timerInterval.value);

      setTimeout(() => {
        step.value = 1;
        name.value = '';
        email.value = '';
        phone.value = '';
        verifyCode.value = '';
        isCodeSent.value = false;
        isTimeout.value = false;
        newPassword.value = '';
        confirmPassword.value = '';
        isPhoneVerified.value = false;
        timer.value = 180;
      }, 300);
    }
  }
);

// 휴대폰 번호 자동 포맷팅
watch(phone, (newVal) => {
  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  if (cleaned.length > 11) {
    formatted = cleaned.slice(0, 11);
  }

  if (cleaned.length > 3 && cleaned.length <= 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
  } else if (cleaned.length > 7) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(
      7
    )}`;
  }

  if (newVal !== formatted) {
    phone.value = formatted;
  }
});

// 타이머 관련 상태
const timer = ref(180);
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null);

const formattedTimer = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0');
  const s = (timer.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

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

// 인증번호 발송 핸들러
const handleSendVerifyCode = async () => {
  // 입력값 검증
  // 🚨 수정: isOwner.value 사용
  if (!email.value) {
    const label = isOwner.value ? '아이디' : '이메일';
     return alert(`${label}을(를) 입력해주세요.`); 
  }
  if (!name.value) return alert('이름을 입력해주세요.');
  if (!phone.value) return alert('휴대폰 번호를 입력해주세요.');

  isPhoneVerified.value = false;
  verifyCode.value = '';

  alert(`인증번호를 발송했습니다: ${phone.value}`);
  try {
    await httpRequest.post('/api/sms/send', {phone: phone.value});

    isCodeSent.value = true;
    startTimer(); 
  }catch(error: any){
    const status = error.response?.status;

    if(status === 400) alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else alert(`메시지 전송에 오류가 발생했습니다. (Code: ${status})`);
  }  
};

// 인증번호 확인
const handleVerifyCode = async () => {
  if (!verifyCode.value) return alert('인증번호를 입력해주세요.');
  if (isTimeout.value) return alert('입력 시간이 초과되었습니다. 재발송해주세요.');

  try {
    const response = await httpRequest.post('/api/sms/verify', {
      phone: phone.value,
      verifyCode: verifyCode.value
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
  } catch (error: any) {
    const status = error.response?.status;
    if (status === 400) alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else alert(`오류가 발생했습니다. (Code: ${status})`);
    
    isPhoneVerified.value = false;
  }
};

// 통합 폼 제출 핸들러
const handleSubmit = () => {
  if (step.value === 1) {
    handleVerifyUser();
  } else {
    handleResetPassword();
  }
};

// 1단계: 인증 확인
const handleVerifyUser = async () => {
  // 🚨 수정: isOwner.value 사용
  if (!email.value) {
    const label = isOwner.value ? '아이디' : '이메일';
    alert(`${label}을(를) 입력해주세요.`);
    return;
  }
  if (!name.value) {
    alert('이름을 입력해주세요.');
    return;
  }
  if (!isCodeSent.value) {
    alert('인증번호를 먼저 발송해주세요.');
    return;
  }
  if (!verifyCode.value) {
    alert('인증번호를 입력해주세요.');
    return;
  }
  if(!isPhoneVerified.value) return alert("인증번호 확인은 필수입니다.");

  let body;
  
  if(isOwner.value){
    body = {
      loginId: email.value,
      phone: phone.value,
      name: name.value
    };
  }else{
    body = {
      email: email.value,
      phone: phone.value,
      name: name.value
    };
  }

  try{
    const response = await httpRequest.post('/api/auth/search/pwd', body);

    step.value = 2;
    // 타이머 정지
    if (timerInterval.value) clearInterval(timerInterval.value);
  }catch(error: any){
    const status = error.response?.status;

    switch(status){
      case 400:
        alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
        break;
      case 404:
        alert("[404 Not Found] 해당 사용자/사업자는 존재하지 않습니다.");
        handleGoToLogin(); //모달 닫기
        break;
      default:
        alert(`오류가 발생했습니다. (Code: ${status})`);
    }
  }
};

// 로그인하러 가기 버튼
const handleGoToLogin = () => {
  emit('close'); // 모달 닫기
};

// 2단계: 비밀번호 변경 요청
const handleResetPassword = async () => {
  if (!newPassword.value) {
    alert('새로운 비밀번호를 입력해주세요.');
    return;
  }
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,20}$/;

  if (!passwordRegex.test(newPassword.value)) {
    return alert(
      '비밀번호는 8~20자이어야 하며, 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.'
    );
  }

  if (newPassword.value !== confirmPassword.value) {
    alert('비밀번호가 일치하지 않습니다.');
    return;
  }

  let body;
  // 🚨 수정: isOwner.value 사용
  if(isOwner.value){
    body = {
      loginId: email.value,
      password: newPassword.value
    };
  }else{
    body = {
      email: email.value,
      password: newPassword.value
    };
  }

  try{
    await httpRequest.put('/api/auth/pwd', body);

    alert('비밀번호가 성공적으로 변경되었습니다.');
    emit('close');
  }catch(error: any){
    const status = error.response?.status;

    switch(status){
      case 400:
        alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
        break;
      case 404:
        alert("[404 Not Found] 해당 아이디/이메일은 존재하지 않습니다.");
        handleGoToLogin(); //모달 닫기
        break;
      default:
        alert(`오류가 발생했습니다. (Code: ${status})`);
    }
  }
};

const submitButtonText = computed(() => {
  return step.value === 1 ? '인증하기' : '비밀번호 변경하기';
});

const modalTitle = computed(() => {
  return step.value === 1 ? '비밀번호 찾기' : '비밀번호 재설정';
});
</script>

<template>
  <Teleport to="body">
    <div v-if="isVisible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="close-btn" @click="$emit('close')">&times;</button>
        </div>

        <div class="modal-body">
          <p class="guide-text" v-if="step === 1">
            가입 시 등록한 정보를 입력해주세요.
          </p>
          <p class="guide-text" v-else>
            새롭게 사용할 비밀번호를 입력해주세요.
          </p>

          <form @submit.prevent="handleSubmit">
            <template v-if="step === 1">
              <div v-if="isOwner" class="input-group">
                <label for="find-id">아이디</label>
                <input
                  id="find-id"
                  v-model="email"
                  type="text"
                  placeholder="아이디를 입력하세요."
                  maxlength="15"
                  required
                />
              </div>

              <div v-else class="input-group">
                <label for="find-email">이메일</label>
                <input
                  id="find-email"
                  v-model="email"
                  type="email"
                  placeholder="이메일을 입력하세요."
                  required
                />
              </div>

              <div class="input-group">
                <label for="find-name">이름</label>
                <input
                  id="find-name"
                  v-model="name"
                  type="text"
                  placeholder="이름을 입력하세요."
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
                    placeholder="전화번호를 입력하세요."
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
                      v-model="verifyCode"
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
            </template>

            <template v-else>
              <div class="input-group slide-in">
                <label for="new-password">새로운 비밀번호</label>
                <input
                  id="new-password"
                  v-model="newPassword"
                  type="password"
                  placeholder="새로운 비밀번호를 입력하세요."
                  required
                />
              </div>

              <div class="input-group slide-in">
                <label for="confirm-password">비밀번호 재입력</label>
                <input
                  id="confirm-password"
                  v-model="confirmPassword"
                  type="password"
                  placeholder="비밀번호를 재입력하세요."
                  required
                />
              </div>
            </template>

            <button type="submit" class="btn-confirm">
              {{ submitButtonText }}
            </button>

            <div v-if="step === 1 && isCodeSent" class="resend-link-container">
              <button
                type="button"
                class="btn-text-link"
                @click="handleSendVerifyCode"
              >
                인증번호가 오지 않나요? <span>재전송</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* 기존 스타일 그대로 유지 */
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

/* 🚨 수정: 타이머 위치 CSS 정리 */
.timer-text {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%); /* 정확히 수직 중앙 */
  font-size: 13px;
  color: #ff6b4a;
  margin: 0; /* 불필요한 마진 제거 */
  pointer-events: none;
  z-index: 10;
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
</style>
