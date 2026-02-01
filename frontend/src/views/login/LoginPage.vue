<script setup lang="ts">
import { ref, computed } from 'vue';
import { RouterLink, useRouter, useRoute } from 'vue-router';
import httpRequest from '@/router/httpRequest';
import { ArrowLeft } from 'lucide-vue-next';
import FindIdModal from '@/components/ui/FindIdModal.vue';
import FindEmailModal from '@/components/ui/FindEmailModal.vue';
import FindPwdModal from '@/components/ui/FindPwdModal.vue';
import UserDormantModal from '@/components/ui/UserDormantModal.vue';
import WaitingModal from '@/components/ui/WaitingModal.vue';
import { useAccountStore } from '@/stores/account';
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

type UserType = 'user' | 'staff' | 'owner';

const showFindIdModal = ref(false);
const showFindEmailModal = ref(false);
const showFindPwdModal = ref(false);
const showDormantModal = ref(false);

const currentTab = ref<UserType>('user');
const email = ref('');
const username = ref('');
const password = ref('');
const isLoading = ref(false);
const errorMessage = ref('');

const tabs = [
  { id: 'user', label: '사용자' },
  { id: 'staff', label: '임직원' },
  { id: 'owner', label: '사업자' },
] as const;

const isEmailLogin = computed(() => {
  return currentTab.value === 'user' || currentTab.value === 'staff';
});

const clearError = () => {
  if (errorMessage.value) {
    errorMessage.value = '';
  }
};

const checkLoginElement = () => {
  if (isEmailLogin.value) {
    if (!email.value) return (errorMessage.value = '이메일은 필수 입력입니다.');

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email.value))
      return (errorMessage.value = '이메일 형식이 맞지 않습니다.');
  } else {
    if (!username.value)
      return (errorMessage.value = '아이디는 필수 입력입니다.');
  }

  if (!password.value)
    return (errorMessage.value = '비밀번호는 필수 입력입니다.');

  return null;
};

const selectTab = (tabId: UserType) => {
  currentTab.value = tabId;
  clearError();
};

const openFindModal = () => {
  if (isEmailLogin.value) {
    showFindEmailModal.value = true;
  } else {
    showFindIdModal.value = true;
  }
};

const handleLogin = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  if (checkLoginElement() !== null) {
    isLoading.value = false;
    return;
  }

  try {
    const queueToken = await waitForTurn();
    const userTypeMap: Record<UserType, string> = {
      user: 'USER',
      staff: 'STAFF',
      owner: 'OWNER',
    };

    const payload = {
      email: isEmailLogin.value ? email.value : username.value,
      password: password.value,
      userType: userTypeMap[currentTab.value],
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

    const targetPath = (() => {
      switch (response.data?.role) {
        case 'ROLE_OWNER':
          return '/business/dashboard';
        case 'ROLE_ADMIN':
          return '/admin/dashboard';
        case 'ROLE_STAFF':
          return '/staff/list';
        case 'ROLE_USER':
        default:
          return '/';
      }
    })();

    router.push(targetPath);
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

const handleDormantUnlocked = () => {
  password.value = '';
  errorMessage.value = '';
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
        <h1 class="text-2xl font-bold text-[#1E3A5F] mb-2">로그인</h1>
        <p class="text-sm text-[#6C757D]">
          런치고에 오신 것을 환영합니다.
        </p>
      </div>
      <div class="login-card">
        <div class="tabs-nav">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            type="button"
            class="tab-btn"
            :class="{ active: currentTab === tab.id }"
            @click="selectTab(tab.id)"
          >
            {{ tab.label }}
            <div class="active-bar" v-if="currentTab === tab.id"></div>
          </button>
        </div>
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="input-group">
            <label :for="isEmailLogin ? 'email' : 'username'">
              {{ isEmailLogin ? '이메일' : '아이디' }}
            </label>
            <input
              v-if="isEmailLogin"
              id="email"
              v-model="email"
              type="email"
              class="input-field"
              placeholder="이메일을 입력하세요"
              @input="clearError"
              required
            />
            <input
              v-else
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
          <div class="find-links-container">
            <a href="#" class="find-link" @click.prevent="openFindModal">
              {{ isEmailLogin ? '이메일 찾기' : '아이디 찾기' }}
            </a>
            <span class="separator">|</span>
            <a
              href="#"
              class="find-link"
              @click.prevent="showFindPwdModal = true"
            >
              비밀번호 찾기
            </a>
          </div>
        </form>
      </div>
      <div class="mt-6 text-center">
        <p class="text-sm text-[#6C757D]">
          아직 회원이 아니신가요?
          <RouterLink
            to="/signup"
            class="text-[#FF6B4A] font-semibold hover:underline ml-1"
          >
            회원가입
          </RouterLink>
        </p>
      </div>
    </main>
    <FindIdModal
      :is-visible="showFindIdModal"
      :user-type="currentTab"
      @close="showFindIdModal = false"
    />
    <FindEmailModal
      :is-visible="showFindEmailModal"
      :user-type="currentTab"
      @close="showFindEmailModal = false"
    />
    <FindPwdModal
      :is-visible="showFindPwdModal"
      :user-type="currentTab"
      @close="showFindPwdModal = false"
    />

    <UserDormantModal
      :is-visible="showDormantModal"
      @close="showDormantModal = false"
      :user-email="email"
      @unlocked="handleDormantUnlocked"
    />
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
.tabs-nav {
  display: flex;
  border-bottom: 1px solid #e9ecef;
  background-color: #fff;
}
.tab-btn {
  flex: 1;
  padding: 16px 0;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 600;
  color: #adb5bd;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}
.tab-btn:hover {
  color: #495057;
  background-color: #fafafa;
}
.tab-btn.active {
  color: #ff6b4a;
  font-weight: 700;
}
.active-bar {
  position: absolute;
  bottom: -1px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #ff6b4a;
  border-radius: 3px 3px 0 0;
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
.find-links-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  font-size: 13px;
  color: #adb5bd;
}
.find-link {
  color: #6c757d;
  text-decoration: none;
  transition: color 0.2s;
}
.find-link:hover {
  color: #ff6b4a;
  text-decoration: underline;
}
.separator {
  margin: 0 12px;
  color: #e9ecef;
  font-size: 12px;
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
