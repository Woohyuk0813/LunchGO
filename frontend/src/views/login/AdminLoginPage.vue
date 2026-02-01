<script setup lang="ts">
import { ref } from 'vue';
import { RouterLink, useRouter, useRoute } from 'vue-router';
import httpRequest from '@/router/httpRequest';
import { ArrowLeft } from 'lucide-vue-next';
import { useAccountStore } from '@/stores/account';
import WaitingModal from '@/components/ui/WaitingModal.vue';
import { useLoginQueue } from '@/composables/useLoginQueue';

const router = useRouter();
const route = useRoute();
const accountStore = useAccountStore();
const {
  isWaiting: isQueueWaiting,
  modalType: queueModalType,
  modalMessage: queueModalMessage,
  waitingCount: queueWaitingCount,
  formattedWaitTime: queueWaitTime,
  waitForTurn,
  handleQueueModalClose,
  resetQueueState,
} = useLoginQueue();

const username = ref('');
const password = ref('');
const isLoading = ref(false);
const errorMessage = ref('');

const clearError = () => {
  if (errorMessage.value) {
    errorMessage.value = '';
  }
};

const handleLogin = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  if (!username.value) {
    errorMessage.value = '아이디는 필수 입력입니다.';
    isLoading.value = false;
    return;
  }

  if (!password.value) {
    errorMessage.value = '비밀번호는 필수 입력입니다.';
    isLoading.value = false;
    return;
  }

  try {
    const queueToken = await waitForTurn();
    const payload = {
      email: username.value,
      password: password.value,
      userType: 'MANAGER',
      queueToken,
    };

    const response = await httpRequest.post('/api/login', payload, { skipAuth: true });
    const { accessToken } = response.data || {};
    if (!accessToken) {
      throw new Error('accessToken not found');
    }

    localStorage.setItem('accessToken', accessToken);
    if (response.data?.member) {
      localStorage.setItem('member', JSON.stringify(response.data.member));
    }

    accountStore.setLoggedIn(true, response.data);
    accountStore.setAccessToken(accessToken);

    const nextPath = route.query.next;
    if (typeof nextPath === 'string' && nextPath) {
      router.push(nextPath);
      return;
    }

    router.push('/admin/dashboard');
  } catch (error) {
    if (queueModalType.value === 'error') {
      return;
    }
    const statusCode = error?.response?.status;
    password.value = '';
    if (statusCode === 401) {
      errorMessage.value = '아이디 또는 비밀번호가 올바르지 않습니다.';
    } else if (statusCode === 400) {
      errorMessage.value = '로그인 정보가 올바르지 않습니다.';
    } else if (statusCode === 429) {
      errorMessage.value = '로그인 대기열에 진입했습니다. 잠시만 기다려주세요.';
    } else {
      errorMessage.value = '로그인 중 오류가 발생했습니다.';
    }
  } finally {
    isLoading.value = false;
    if (queueModalType.value !== 'error' && !isQueueWaiting.value) {
      resetQueueState();
    }
  }
};
</script>

<template>
  <div class="min-h-screen bg-[#F8F9FA] flex flex-col font-pretendard">
    <header class="bg-white border-b border-[#E9ECEF]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink
          to="/"
          class="flex items-center gap-2 text-[#495057] hover:text-[#1E3A5F]"
        >
          <ArrowLeft class="w-5 h-5" />
          <span class="text-sm font-medium">돌아가기</span>
        </RouterLink>
      </div>
    </header>
    <main class="flex-1 max-w-[500px] mx-auto w-full px-4 py-8">
      <div class="flex flex-col items-center mb-8">
        <img
          src="/images/lunch-go-whitebg.png"
          alt="런치고"
          width="80"
          height="80"
          class="w-20 h-20 mb-4 object-contain"
        />
        <h1 class="text-2xl font-bold text-[#1E3A5F] mb-2">관리자 로그인</h1>
        <p class="text-sm text-[#6C757D]">
          관리자 전용 로그인 페이지입니다.
        </p>
      </div>
      <div class="login-card">
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="input-group">
            <label for="username">아이디</label>
            <input
              id="username"
              v-model="username"
              type="text"
              class="input-field"
              placeholder="아이디를 입력하세요"
              @input="clearError"
              required
            />
          </div>
          <div class="input-group">
            <label for="password">비밀번호</label>
            <input
              id="password"
              v-model="password"
              type="password"
              class="input-field"
              placeholder="비밀번호를 입력하세요"
              maxlength="20"
              @input="clearError"
              required
            />
          </div>
          <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>
          <button type="submit" :disabled="isLoading" class="btn-primary">
            <span v-if="isLoading" class="spinner"></span>
            <span v-else>로그인</span>
          </button>
        </form>
      </div>
    </main>
    <WaitingModal
      :is-open="isQueueWaiting"
      :type="queueModalType"
      :message="queueModalMessage"
      :waiting-count="queueWaitingCount"
      :estimated-time="queueWaitTime"
      @close="handleQueueModalClose"
    />
  </div>
</template>

<style scoped>
@import url('https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css');
.font-pretendard {
  font-family: 'Pretendard', sans-serif;
}
.login-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #e9ecef;
  overflow: hidden;
}
.login-form {
  padding: 24px;
}
.input-group {
  margin-bottom: 20px;
}
.input-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #1e3a5f;
  margin-bottom: 6px;
}
.input-field {
  display: block;
  width: 100%;
  height: 48px;
  padding: 12px 16px;
  font-size: 14px;
  color: #1e3a5f;
  background: #ffffff;
  border: 1.5px solid #e9ecef;
  border-radius: 8px;
  box-sizing: border-box;
  transition: all 0.2s ease;
}
.input-field::placeholder {
  color: #adb5bd;
}
.input-field:focus {
  outline: none;
  border-color: #ff6b4a;
  box-shadow: 0 0 0 3px rgba(255, 107, 74, 0.12);
}
.error-msg {
  color: #f44336;
  font-size: 12px;
  text-align: center;
  margin-bottom: 16px;
  background: #ffebee;
  padding: 8px;
  border-radius: 4px;
}
.btn-primary {
  width: 100%;
  height: 52px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #ff6b4a;
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(255, 107, 74, 0.2);
  transition: all 0.2s ease;
}
.btn-primary:hover:not(:disabled) {
  background-color: #e5553a;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(229, 85, 58, 0.3);
}
.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}
.btn-primary:disabled {
  background-color: #ced4da;
  cursor: not-allowed;
  box-shadow: none;
}
.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #ffffff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
