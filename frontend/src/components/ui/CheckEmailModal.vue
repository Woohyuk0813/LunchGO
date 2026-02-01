<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue';
import { useAccountStore } from '@/stores/account';
import httpRequest from '@/router/httpRequest';

// 부모 컴포넌트에서 전달받은 email
const props = defineProps<{
  email: string;
}>();

const emit = defineEmits(['close', 'verified']);

const accountStore = useAccountStore();

// 상태 관리
const verificationCode = ref(''); // 입력한 인증코드
const isCodeSent = ref(false); // 인증번호 발송 여부
const isTimeout = ref(false); // 시간 초과 여부
const isLoading = ref(false); // 로딩 상태

const verifyCode = ref(''); //시스템 상의 보낸 인증코드

// 타이머 상태
const timer = ref(180);
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null);

// 타이머 포맷
const formattedTimer = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0');
  const s = (timer.value % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
});

// 타이머 시작 함수
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

// 컴포넌트 해제 시 타이머 정리
onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
});

//인증번호 이메일 발송
const handleSendEmailCode = async () => {
  alert(`'${props.email}'로 인증번호를 발송했습니다.\n메일함을 확인해주세요.`);

  if (!accountStore.accessToken) {
    return alert('로그인이 필요합니다.');
  }

  try{
    await httpRequest.post('/api/email/send', { mail: props.email });

    isCodeSent.value = true;
    verificationCode.value = ''; // 코드 입력 초기화
    startTimer();
  }catch(error){
    const status = error.response?.status;

    switch(status){
       case 400:
        alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
        break;
      case 404:
        alert("[404 Not Found] 해당 이메일은 존재하지 않습니다.");
        break;
      default:
        alert(`오류가 발생했습니다. (Code: ${status ?? 'unknown'})`);
    }
  }
};

// 인증번호 확인 (최종 완료)
const handleVerify = async () => {
  if (!isCodeSent.value) return alert('인증번호를 먼저 발송해주세요.');
  if (!verificationCode.value) return alert('인증번호를 입력해주세요.');
  if (isTimeout.value)
    return alert('입력 시간이 초과되었습니다. 재발송해주세요.');

  isLoading.value = true;

  if (!accountStore.accessToken) {
    return alert('로그인이 필요합니다.');
  }

  try {
    const response = await httpRequest.post(
      '/api/email/verify',
      { mail: props.email, verifyCode: verificationCode.value }
    );

    if(response.data === true){
      alert('인증이 완료되었습니다.');

      emit('verified'); // 부모에게 성공 알림
      emit('close'); // 모달 닫기

      // 타이머 정지
      if (timerInterval.value) clearInterval(timerInterval.value);
    } else{
      alert("인증번호가 일치하지 않습니다. 다시 확인해주세요.");
    }    
  } catch (error) {
    const status = error.response?.status;
    if(status === 400) alert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else alert(`오류가 발생했습니다. (Code: ${status ?? 'unknown'})`);
  } finally {
    isLoading.value = false;
  }
};
</script>

<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content">
        <div class="modal-header">
          <h3>이메일 인증</h3>
          <button class="close-btn" @click="$emit('close')">&times;</button>
        </div>

        <div class="modal-body">
          <div class="notice-box">
            <p>회원님의 안전한 정보 보호를 위해</p>
            <p><span class="highlight">이메일 인증</span>을 진행해주세요.</p>
          </div>

          <form @submit.prevent="handleVerify">
            <div class="input-group">
              <label>이메일 주소</label>
              <div class="input-with-button">
                <input
                  type="email"
                  :value="props.email"
                  readonly
                  class="flex-grow-input bg-gray-100 text-gray-500"
                />
                <button
                  type="button"
                  class="btn-secondary"
                  @click="handleSendEmailCode"
                  :disabled="isLoading"
                >
                  {{ isCodeSent ? '재전송' : '인증하기' }}
                </button>
              </div>
            </div>

            <div v-if="isCodeSent" class="input-group slide-in">
              <label>인증번호</label>
              <div class="relative">
                <input
                  v-model="verificationCode"
                  type="text"
                  placeholder="인증번호 6자리 입력"
                  class="input-field"
                  maxlength="6"
                  :disabled="isTimeout"
                />
                <p class="timer-text">{{ formattedTimer }}</p>
              </div>
              <p v-if="isTimeout" class="error-text">
                시간이 초과되었습니다. 재전송 버튼을 눌러주세요.
              </p>
            </div>

            <button
              type="submit"
              class="btn-confirm"
              :disabled="!isCodeSent || isTimeout || isLoading"
            >
              {{ isLoading ? '처리중...' : '인증 완료' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* 기존 스타일 재사용 + 일부 수정 */
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

.notice-box {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  text-align: center;
  font-size: 14px;
  color: #495057;
  line-height: 1.5;
}
.notice-box p {
  margin: 2px 0;
}
.highlight {
  color: #ff6b4a;
  font-weight: 700;
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

.input-field,
.flex-grow-input {
  width: 100%;
  height: 48px;
  padding: 0 16px;
  border: 1.5px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  color: #1e3a5f;
  box-sizing: border-box;
}

.input-field:focus {
  outline: none;
  border-color: #ff6b4a;
}
.bg-gray-100 {
  background-color: #f8f9fa;
}
.text-gray-500 {
  color: #6c757d;
}

.input-with-button {
  display: flex;
  gap: 8px;
}
.flex-grow-input {
  flex: 1;
}

.btn-secondary {
  height: 48px;
  padding: 0 16px;
  background-color: #ffffff;
  border: 1.5px solid #e9ecef;
  border-radius: 8px;
  color: #495057;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.btn-secondary:hover {
  background-color: #f1f3f5;
}

.timer-text {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 13px;
  color: #ff6b4a;
  font-weight: 500;
}

.error-text {
  color: #ff0000;
  font-size: 12px;
  margin-top: 4px;
}

.btn-confirm {
  width: 100%;
  height: 48px;
  background-color: #1e3a5f;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  margin-top: 8px;
  transition: background-color 0.2s;
}
.btn-confirm:disabled {
  background-color: #adb5bd;
  cursor: not-allowed;
}
.btn-confirm:hover:not(:disabled) {
  background-color: #162c46;
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
</style>
