<script setup lang="ts">
import { ref, watch, computed, onUnmounted } from 'vue';

const props = defineProps<{
  isVisible: boolean;
  userEmail: string;
}>();

const emit = defineEmits(['close', 'unlocked']);
const phone = ref('');

//인증번호 및 결과 상태
const isCodeSent = ref(false);
const verifyCode = ref('');
const isTimeout = ref(false);
const isLoading = ref(false);

// 타이머 상태
const timer = ref(180);
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null);

watch(
  () => props.isVisible,
  (newVal) => {
    if (!newVal) {
      if (timerInterval.value) clearInterval(timerInterval.value);
      setTimeout(() => {
        phone.value = '';
        verifyCode.value = '';
        isCodeSent.value = false;
        isTimeout.value = false;
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
      //시간 초과 난 경우
      alert('인증번호 입력 시간이 초과되었습니다. 재발송이 필요합니다.');
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
});

// 인증번호 발송
const handleSendVerifyCode = async () => {
  if (!phone.value) return alert('휴대폰 번호를 입력해주세요.');

  // API 호출 시뮬레이션
  // await api.sendCode(...)
  alert(`인증번호를 발송했습니다: ${phone.value}`);

  isCodeSent.value = true;
  verifyCode.value = '';
  startTimer();
};

//인증하기 (폼 제출)
const handleUnlockDormant = async () => {
  if (!phone.value) return alert('휴대폰 번호를 입력해주세요.');
  if (!isCodeSent.value) return alert('인증번호를 먼저 발송해주세요.');
  if (!verifyCode.value) return alert('인증번호를 입력해주세요.');

  isLoading.value = true;

  // 실제 백엔드 API 호출로 회원정보 조회 및 인증번호 일치 유무
  // const response = await api.findUser({email: email.value, phone: phone.value, 인증번호});

  //전화번호가 맞지 않는 경우
  //인증번호가 틀린 경우

  //일치한 경우
  //휴면 해제 및 다시 로그인 유도
  try {
    // 1. 백엔드 API 호출 시뮬레이션
    // 실제로는 axios.post('/api/auth/unlock-dormant', { id: props.userEmail, phone: ... }) 형태
    console.log(
      `휴면 해지 요청: User=${props.userEmail}, Phone=${phone.value}, Code=${verifyCode.value}`
    );

    // await new Promise((resolve) => setTimeout(resolve, 1000)); // 1초 대기 (API 연동 흉내)

    // 2. (가정) 백엔드에서 성공 응답(200 OK)이 왔다고 가정
    alert('휴면 상태가 정상적으로 해지되었습니다.\n다시 로그인해주세요.');

    // 3. 부모 컴포넌트(로그인 페이지)에 "해지 완료됨" 알림
    emit('unlocked');

    // 4. 모달 닫기
    emit('close');
  } catch (error) {
    // API 에러 처리
    console.error(error);
    alert('인증번호가 올바르지 않거나 시스템 오류가 발생했습니다.');
  } finally {
    isLoading.value = false;
  }
  emit('close');
};
</script>

<template>
  <Teleport to="body">
    <div v-if="isVisible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content">
        <div class="modal-header">
          <h3>휴면 상태 안내</h3>
          <button class="close-btn" @click="$emit('close')">&times;</button>
        </div>

        <div class="modal-body">
          <div class="notice-box">
            <p>
              <strong
                >고객님은 현재
                <span class="highlight">휴면회원</span>입니다.</strong
              >
            </p>
            <p>휴면을 해지하시려면,<br />전화번호 인증이 필요합니다.</p>
          </div>

          <form @submit.prevent="handleUnlockDormant">
            <div class="input-group">
              <label for="dormant-phone">휴대폰 번호</label>
              <div class="input-with-button">
                <input
                  id="dormant-phone"
                  v-model="phone"
                  type="tel"
                  placeholder="010-0000-0000"
                  maxlength="13"
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
              <input
                v-model="verifyCode"
                type="text"
                placeholder="인증번호 6자리를 입력하세요"
                class="input-field"
                maxlength="6"
              />
              <p class="timer-text">{{ formattedTimer }}</p>
            </div>

            <button type="submit" class="btn-confirm">휴면 해지</button>

            <div v-if="isCodeSent" class="resend-link-container">
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
/* 기존 모달 스타일 상속 및 유지 */
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
  margin-bottom: 20px;
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

/* 안내 문구 스타일 */
.notice-box {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  text-align: center;
  font-size: 14px;
  color: #495057;
  line-height: 1.6;
}
.notice-box p {
  margin: 4px 0;
}
.highlight {
  color: #ff6b4a;
  font-weight: 700;
}

/* 입력 필드 공통 */
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
  background-color: #f8f9fa;
  border: 1.5px solid #e9ecef;
  border-radius: 8px;
  color: #495057;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.btn-secondary:hover {
  background-color: #e9ecef;
  color: #1e3a5f;
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

/* 타이머 */
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

/* 확인 버튼 */
.btn-confirm {
  width: 100%;
  height: 48px;
  background-color: #1e3a5f; /* 휴면 해지는 중요한 액션이므로 진한 색 */
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  margin-top: 12px;
  transition: background-color 0.2s;
}
.btn-confirm:hover {
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
