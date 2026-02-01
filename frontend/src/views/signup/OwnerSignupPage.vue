<script setup lang="ts">
import { ref, watch, computed, onUnmounted, onMounted } from 'vue';
import axios from 'axios';
import { marked } from 'marked';
import { RouterLink , useRouter} from 'vue-router';
import {
  ArrowLeft,
  ChevronRight,
  Check,
  X,
  TruckElectric,
} from 'lucide-vue-next'; // 아이콘 추가
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';
import ConfirmModal from '@/components/ui/ConfirmModal.vue';
import Input from '@/components/ui/Input.vue';
import PRIVACY_POLICY_TEXT from '@/content/privacyPolicy.md?raw';
import TERMS_POLICY_TEXT from '@/content/serviceTerms.md?raw';

const router = useRouter();

const isAlertOpen = ref(false);
const alertMessage = ref('');
const alertConfirmAction = ref<null | (() => void)>(null);

const openAlert = (message: string, onConfirm?: () => void) => {
  alertMessage.value = message;
  alertConfirmAction.value = onConfirm ?? null;
  isAlertOpen.value = true;
  return true;
};

const handleAlertConfirm = () => {
  isAlertOpen.value = false;
  const action = alertConfirmAction.value;
  alertConfirmAction.value = null;
  if (action) action();
};

const name = ref('');
const loginId = ref('');
const isLoginIdUnique = ref(false);
const phone = ref('');
const verificationCode = ref(''); //사용자가 입력한 인증번호
const password = ref('');
const passwordConfirm = ref('');
const businessNum = ref('');
const isBusinessNumAppropriate = ref(false);
const businessNumMsg = ref('');
const startAt = ref(''); //개업일자

//컴포넌트 ref 생성
const passwordConfirmRef = ref<any>(null);

// 인증번호 관련 상태
const isCodeSent = ref(false);
const isTimeout = ref(false);
const isPhoneVerified = ref(false);

// 타이머 상태
const timer = ref(180);
const timerInterval = ref<ReturnType<typeof setInterval> | null>(null);

// 약관 동의 상태
const agreeTerms = ref(false);
const agreePrivacy = ref(false);

// 전체 동의 체크박스 상태 (Computed 처럼 동작하게 하거나 watch로 제어)
const agreeAll = ref(false);

// 전체 동의 로직
watch(agreeAll, (newVal) => {
  if (newVal) {
    agreeTerms.value = true;
    agreePrivacy.value = true;
  } else {
    if (agreeTerms.value && agreePrivacy.value) {
      agreeTerms.value = false;
      agreePrivacy.value = false;
    }
  }
});

// 개별 체크박스가 변경될 때 '전체 동의' 체크박스 상태 업데이트
watch([agreeTerms, agreePrivacy], ([terms, privacy]) => {
  agreeAll.value = terms && privacy;
});

//개업일자 자동 포맷팅
watch(startAt, (newVal) => {
  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  if (cleaned.length > 8) {
    formatted = cleaned.slice(0, 8);
  }

  if (cleaned.length > 4 && cleaned.length <= 6) {
    formatted = `${cleaned.slice(0, 4)}-${cleaned.slice(4)}`;
  } else if (cleaned.length > 6) {
    formatted = `${cleaned.slice(0, 4)}-${cleaned.slice(4, 6)}-${cleaned.slice(
      6
    )}`;
  }

  if (newVal !== formatted) {
    startAt.value = formatted;
  }
});

//사업자 등록번호 자동 포맷팅
watch(businessNum, (newVal) => {
  if (businessNumMsg.value) {
    businessNumMsg.value = '';
  }

  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  // 최대 10자리(숫자 기준)까지만 처리
  if (cleaned.length > 10) {
    formatted = cleaned.slice(0, 10);
  }

  if (cleaned.length > 3 && cleaned.length <= 5) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3)}`;
  } else if (cleaned.length > 5) {
    formatted = `${cleaned.slice(0, 3)}-${cleaned.slice(3, 5)}-${cleaned.slice(
      5
    )}`;
  }

  // 무한 루프 방지
  if (newVal !== formatted) {
    businessNum.value = formatted;
  }
});

//휴대폰 번호 자동 포맷팅
watch(phone, (newVal) => {
  // 숫자만 남기고 제거
  const cleaned = newVal.replace(/[^0-9]/g, '');
  let formatted = cleaned;

  // 11자리까지만 입력 가능하도록 제한
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
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
});

const checkInputElement = () => {
  if (!loginId.value) return openAlert('아이디를 입력해주세요.');
  if (!isLoginIdUnique.value) return openAlert('아이디 중복 확인이 필요합니다.');
  if (!password.value) return openAlert('비밀번호를 입력해주세요.');
  if (!passwordConfirm.value) return openAlert('비밀번호 재입력이 필요합니다.');
  if (!name.value) return openAlert('이름(대표자명)을 입력해주세요.');
  //전화번호도 인증, 사업자 등록번호도 인증해야하므로 별도로 입력 관리
  return false;
};

//아이디 중복체크 버튼
const handleLoginIdDuplicateCheck = async () => {
  const loginIdRegex = /^(?=.*[a-z])(?=.*\d)[a-z\d]{7,15}$/;
  if (!loginId.value) return openAlert('아이디를 먼저 입력해주세요.');
  if (!loginIdRegex.test(loginId.value))
    return openAlert(
      '아이디는 7~15자이어야 하며, 영문 소문자, 숫자만 포함해야합니다.'
    );

  try{
    await axios.post('/api/auth/loginId', {loginId: loginId.value});

    openAlert('사용 가능한 아이디입니다.');
    isLoginIdUnique.value = true;

  }catch(error){
    const status = error.response.status;

    switch(status){
      case 400:
        openAlert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
        break;
      case 409:
        openAlert("이미 사용중인 아이디입니다.");
        loginId.value = '';
        break;
      default:
        openAlert(`오류가 발생했습니다. (Code: ${status})`);
    }
  }
};

//사업자등록번호 인증 api 연동 버튼
const handleBusinessNumCheck = async () => {
  if (!name.value) return openAlert('이름(대표자명)을 먼저 입력해주세요.');
  if (!startAt.value || startAt.value.length < 10)
    return openAlert('개업일자를 올바르게 입력해주세요.');
  if (!businessNum.value) return openAlert('사업자 등록번호를 먼저 입력해주세요.');

  //api 호출해서 확인하려면 데이터 정제 필수
  const cleanBusinessNum = businessNum.value.replace(/[^0-9]/g, '');
  const cleanStartDt = startAt.value.replace(/[^0-9]/g, ''); // YYYYMMDD 형태
  const cleanName = name.value;

  //api 연동
  const SERVICE_KEY =
    '71e991b467c362f2d2661868fbc38193524993405b29e8b95253665610407c50';

  try {
    const response = await axios.post(
      `https://api.odcloud.kr/api/nts-businessman/v1/validate?serviceKey=${SERVICE_KEY}`,
      {
        businesses: [
          {
            b_no: cleanBusinessNum,
            start_dt: cleanStartDt,
            p_nm: cleanName,
          },
        ],
      },
      {
        headers: {
          'Content-Type': 'application/json',
          Accept: 'application/json',
        },
      }
    );

    const data = response.data;

    if (data.data && data.data.length > 0) {
      const result = data.data[0]; //어짜피 정보 1개

      // valid: '01'(일치), '02'(불일치)
      if (result.valid === '01') {
        // 일치하더라도 폐업자일 수 있으므로 상태코드(b_stt_cd)도 확인 권장
        // status가 없을 수도 있으니 안전하게 접근
        const status = result.status?.b_stt_cd || '';

        if (status === '01') {
          // 01: 계속사업자
          businessNumMsg.value = '사업자 정보가 확인되었습니다.';
        } else if (status === '02') {
          businessNumMsg.value = '휴업 상태인 사업자입니다.';
        } else if (status === '03') {
          businessNumMsg.value = '폐업한 사업자입니다.';
          isBusinessNumAppropriate.value = false;
          return;
        } else {
          // 상태 정보가 없지만 valid가 01인 경우 (드물지만 통과 처리 or 경고)
          businessNumMsg.value = '사업자 정보가 없습니다.';
          isBusinessNumAppropriate.value = false;
          return;
        }
      } else {
        // valid: '02' 인 경우 (정보 불일치)
        businessNumMsg.value =
          '사업자 정보가 국세청 등록 정보와 일치하지 않습니다.\n대표자명, 개업일자, 사업자번호를 다시 확인해주세요.';
        isBusinessNumAppropriate.value = false;
        return;
      }
    } else {
      return openAlert('검증 오류가 발생했습니다.');
    }
  } catch (e) {
    console.error('API Error: ', e);
  }

  //사업자 등록번호가 이미 있는 경우 (pinia 이용해서 회원정보는 업데이트 할거임)

  isBusinessNumAppropriate.value = true;
};

// 인증번호 발송
const handleSendVerifyCode = async () => {
  if (checkInputElement()) {
    return;
  }

  openAlert(`인증번호를 발송했습니다: ${phone.value}`);

  try{
    await axios.post('/api/sms/send', {phone: phone.value});

    isCodeSent.value = true;
    startTimer();

  }catch(error){
    const status = error.response.status;

    if (status === 400) openAlert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else openAlert(`메시지 전송에 오류가 발생했습니다. (Code: ${status})`);
  }
};

//인증번호 확인
const handleVerifyCode = async() => {
  if (!verificationCode.value) return openAlert('인증번호를 입력해주세요.');
  if (isTimeout.value) return openAlert('입력 시간이 초과되었습니다. 재발송해주세요.');

  try {
    const response = await axios.post('/api/sms/verify', {
      phone: phone.value,
      verifyCode: verificationCode.value
    });

    if(response.data === true){
      openAlert('인증이 완료되었습니다.');
      isPhoneVerified.value = true; // 인증 완료 상태로 변경

      // 타이머 정지
      if (timerInterval.value) clearInterval(timerInterval.value);
    } else{
      openAlert("인증번호가 일치하지 않습니다. 다시 확인해주세요.");
      
      isPhoneVerified.value = false;
    }
    
  } catch (error) {
    // 에러 처리
    const status = error.response.status;
    if (status === 400) openAlert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else openAlert(`오류가 발생했습니다. (Code: ${status})`);
    
    isPhoneVerified.value = false;
  }
}

// 모달 관련 상태
const isTermsModalOpen = ref(false);
const modalTitle = ref('');
const modalContent = ref('');
const modalContentHtml = computed(() => marked.parse(modalContent.value, { breaks: true }));

// 이용약관 모달 열기 -> 내용 수정 필수!!
const openModal = (type) => {
  isTermsModalOpen.value = true;
  if (type === 'terms') {
    modalTitle.value = '서비스 이용약관';
    modalContent.value = TERMS_POLICY_TEXT;
  } else if (type === 'privacy') {
    modalTitle.value = '개인정보 처리방침';
    modalContent.value = PRIVACY_POLICY_TEXT;
  }
};

// 모달 닫기
const closeModal = () => {
  isTermsModalOpen.value = false;
};

const handleSignup = async () => {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,20}$/;

  if (checkInputElement()) return;
  if (!isPhoneVerified.value) return openAlert('휴대전화 인증은 필수입니다.');

  if (!passwordRegex.test(password.value))
    return openAlert(
      '비밀번호는 8~20자이어야 하며, 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.'
    );

  if (password.value !== passwordConfirm.value) {
    //비밀번호 확인 입력창 비우고 포커스 이동
    passwordConfirm.value = '';
    passwordConfirmRef.value?.focus(); // 포커스 함수 실행
    openAlert('비밀번호가 일치하지 않습니다.');
    return;
  }
  //사업자등록번호 인증여부 확인
  if (!isBusinessNumAppropriate.value) return openAlert("사업자 등록번호 인증은 필수입니다.");

  if (!agreeTerms.value || !agreePrivacy.value) {
    openAlert('필수 약관에 동의해주세요.');
    return;
  }

  try {
    await axios.post('/api/join/owner', {
      loginId: loginId.value,
      password: password.value,
      name: name.value,
      phone: phone.value,
      businessNum: businessNum.value,
      startAt: startAt.value
    });

    //타이머 실행되고 있으면 정지
    if (timerInterval.value) clearInterval(timerInterval.value);
    openAlert('회원가입 완료!', () => router.push('/'));
  }catch(error){
    const status = error.response.status;

    if (status === 400) openAlert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else openAlert(`오류가 발생했습니다. (Code: ${status})`);
  }
};
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] flex flex-col">
    <header class="bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink
          to="/signup"
          class="flex items-center gap-2 text-[#495057] hover:text-[#1e3a5f]"
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
          class="w-20 h-20 mb-4"
        />
        <h1 class="text-2xl font-bold text-[#1e3a5f] mb-2">사업자 회원가입</h1>
        <p class="text-sm text-[#6c757d]">
          런치고 파트너가 되어 더 많은 고객을 만나보세요
        </p>
      </div>

      <Card class="border-[#e9ecef] rounded-xl bg-white shadow-card p-6">
        <form @submit.prevent="handleSignup" class="space-y-4">
          <div>
            <label
              for="loginId"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >아이디</label
            >
            <div class="flex gap-2">
              <Input
                id="loginId"
                type="text"
                placeholder="아이디를 입력하세요"
                v-model="loginId"
                minlength="7"
                maxlength="15"
                :readonly="isLoginIdUnique"
                :class="`flex-1 h-12 px-4 border-[#dee2e6] rounded-lg transition-colors text-sm ${
                  isLoginIdUnique
                    ? 'bg-gray-100 text-[#1e3a5f] cursor-not-allowed text-sm'
                    : 'bg-white text-[#1e3a5f] focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a]'
                }`"
              />
              <Button
                type="button"
                @click="handleLoginIdDuplicateCheck"
                variant="outline"
                class="h-12 px-4 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg whitespace-nowrap"
              >
                중복확인
              </Button>
            </div>
          </div>

          <div>
            <label
              for="password"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >비밀번호</label
            >
            <Input
              id="password"
              type="password"
              placeholder="비밀번호를 입력하세요 (8자 이상)"
              minlength="8"
              maxlength="20"
              v-model="password"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] text-sm"
            />
          </div>

          <div>
            <label
              for="password-confirm"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >비밀번호 확인</label
            >
            <Input
              ref="passwordConfirmRef"
              id="password-confirm"
              type="password"
              placeholder="비밀번호를 다시 입력하세요"
              minlength="8"
              maxlength="20"
              v-model="passwordConfirm"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] text-sm"
            />
          </div>

          <div>
            <label
              for="name"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >이름</label
            >
            <Input
              id="name"
              type="text"
              placeholder="이름(대표자명)을 입력하세요"
              maxlength="10"
              v-model="name"
              :readonly="isBusinessNumAppropriate"
              :class="`w-full h-12 px-4 border-[#dee2e6] rounded-lg transition-colors text-sm ${
                isBusinessNumAppropriate
                  ? 'bg-gray-100 text-gray-500 cursor-not-allowed'
                  : 'bg-white text-[#1e3a5f] focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a]'
              }`"
            />
          </div>

          <div>
            <label
              for="startDt"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >개업일자</label
            >
            <Input
              id="startDt"
              type="text"
              placeholder="YYYY-mm-dd(숫자 8자리 입력)"
              maxlength="10"
              v-model="startAt"
              :readonly="isBusinessNumAppropriate"
              :class="`w-full h-12 px-4 border-[#dee2e6] rounded-lg transition-colors text-sm ${
                isBusinessNumAppropriate
                  ? 'bg-gray-100 text-gray-500 cursor-not-allowed'
                  : 'bg-white text-[#1e3a5f] focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a]'
              }`"
            />
          </div>

          <div>
            <label
              for="businessNum"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >사업자등록번호</label
            >
            <div class="flex gap-2">
              <Input
                id="businessNum"
                type="text"
                placeholder="사업자등록번호를 입력하세요"
                maxlength="12"
                v-model="businessNum"
                :readonly="isBusinessNumAppropriate"
                :class="`flex-1 h-12 px-4 border-[#dee2e6] rounded-lg transition-colors text-sm ${
                  isBusinessNumAppropriate
                    ? 'bg-gray-100 text-gray-500 cursor-not-allowed'
                    : 'bg-white text-[#1e3a5f] focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a]'
                }`"
              />

              <Button
                type="button"
                @click="handleBusinessNumCheck"
                :disabled="isBusinessNumAppropriate"
                variant="outline"
                class="h-12 px-4 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg whitespace-nowrap"
              >
                {{ isBusinessNumAppropriate ? '인증완료' : '인증' }}
              </Button>
            </div>
            <p
              v-if="businessNumMsg"
              class="text-xs text-red-500 mt-1 pl-1 whitespace-pre-line"
            >
              {{ businessNumMsg }}
            </p>
          </div>

          <div>
            <label
              for="phone"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >휴대폰 번호</label
            >
            <div class="flex gap-2">
              <Input
                id="phone"
                type="tel"
                placeholder="010-0000-0000"
                maxlength="13"
                v-model="phone"
                class="flex-1 h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] text-sm"
              />
              <Button
                type="button"
                @click="handleSendVerifyCode"
                variant="outline"
                class="h-12 px-4 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg whitespace-nowrap"
              >
                {{ isCodeSent ? '재발송' : '인증번호 발송' }}
              </Button>
            </div>
          </div>

          <div
            v-if="isCodeSent"
            class="animate-in fade-in slide-in-from-top-2 duration-300"
          >
            <label
              for="verify"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
            >
              인증번호
            </label>
            
            <div class="flex gap-2">
              <div class="relative flex-1">
                <Input
                  id="verify"
                  type="text"
                  placeholder="인증번호 6자리"
                  v-model="verificationCode"
                  :disabled="isPhoneVerified || isTimeout"
                  @keydown.enter.prevent="handleVerifyCode"
                  class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] pr-16 disabled:bg-gray-100 disabled:text-gray-500"
                />
                <span
                  class="absolute right-4 top-1/2 -translate-y-1/2 text-sm font-medium"
                  :class="{
                    'text-red-500': isTimeout,
                    'text-[#20c997]': isPhoneVerified, 
                    'text-[#ff6b4a]': !isTimeout && !isPhoneVerified
                  }"
                >
                  {{ isPhoneVerified ? '인증되었습니다.' : formattedTimer }}
                </span>
              </div>

              <Button
                type="button"
                @click="handleVerifyCode"
                :disabled="isPhoneVerified || isTimeout"
                variant="outline"
                class="h-12 px-4 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg whitespace-nowrap disabled:bg-gray-50 disabled:text-gray-400"
              >
                확인
              </Button>
            </div>

            <p v-if="isTimeout && !isPhoneVerified" class="text-xs text-red-500 mt-1 pl-1">
              입력 시간이 초과되었습니다. 재발송 버튼을 눌러주세요.
            </p>
          </div>

          <div class="pt-4">
            <label
              class="flex items-center gap-3 p-4 bg-[#f8f9fa] rounded-xl cursor-pointer hover:bg-[#f1f3f5] transition-colors mb-3 border border-transparent hover:border-[#dee2e6]"
            >
              <div class="relative flex items-center justify-center w-5 h-5">
                <input
                  type="checkbox"
                  v-model="agreeAll"
                  class="peer appearance-none w-5 h-5 border-2 border-[#adb5bd] rounded checked:bg-[#ff6b4a] checked:border-[#ff6b4a] transition-colors"
                />
                <Check
                  class="absolute w-3.5 h-3.5 text-white pointer-events-none opacity-0 peer-checked:opacity-100"
                />
              </div>
              <span class="text-sm font-bold text-[#1e3a5f]"
                >약관 전체 동의</span
              >
            </label>

            <div class="flex flex-col gap-1 pl-1">
              <div class="flex items-center justify-between py-2 group">
                <label class="flex items-center gap-3 cursor-pointer flex-1">
                  <div
                    class="relative flex items-center justify-center w-5 h-5"
                  >
                    <input
                      type="checkbox"
                      v-model="agreeTerms"
                      class="peer appearance-none w-5 h-5 border border-[#dee2e6] rounded checked:bg-[#ff6b4a] checked:border-[#ff6b4a] transition-colors"
                    />
                    <Check
                      class="absolute w-3.5 h-3.5 text-white pointer-events-none opacity-0 peer-checked:opacity-100"
                    />
                  </div>
                  <span
                    class="text-sm text-[#495057] group-hover:text-[#1e3a5f] transition-colors"
                  >
                    <span class="text-[#ff6b4a] mr-1">(필수)</span>서비스
                    이용약관 동의
                  </span>
                </label>
                <button
                  type="button"
                  @click.stop="openModal('terms')"
                  class="p-1 text-[#adb5bd] hover:text-[#495057] hover:bg-[#f8f9fa] rounded-full transition-colors"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>

              <div class="flex items-center justify-between py-2 group">
                <label class="flex items-center gap-3 cursor-pointer flex-1">
                  <div
                    class="relative flex items-center justify-center w-5 h-5"
                  >
                    <input
                      type="checkbox"
                      v-model="agreePrivacy"
                      class="peer appearance-none w-5 h-5 border border-[#dee2e6] rounded checked:bg-[#ff6b4a] checked:border-[#ff6b4a] transition-colors"
                    />
                    <Check
                      class="absolute w-3.5 h-3.5 text-white pointer-events-none opacity-0 peer-checked:opacity-100"
                    />
                  </div>
                  <span
                    class="text-sm text-[#495057] group-hover:text-[#1e3a5f] transition-colors"
                  >
                    <span class="text-[#ff6b4a] mr-1">(필수)</span>개인정보
                    처리방침 동의
                  </span>
                </label>
                <button
                  type="button"
                  @click.stop="openModal('privacy')"
                  class="p-1 text-[#adb5bd] hover:text-[#495057] hover:bg-[#f8f9fa] rounded-full transition-colors"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          <Button
            type="submit"
            class="w-full h-12 gradient-primary text-white font-semibold rounded-xl hover:opacity-90 transition-opacity shadow-button mt-6"
          >
            회원가입
          </Button>
        </form>

        <div class="mt-6 text-center">
          <p class="text-sm text-[#6c757d]">
            이미 회원이신가요?
            <RouterLink
              to="/login"
              class="text-[#ff6b4a] font-semibold hover:underline"
            >
              로그인
            </RouterLink>
          </p>
        </div>
      </Card>
    </main>
  </div>

  <Transition
    enter-active-class="transition duration-200 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition duration-150 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <div v-if="isTermsModalOpen" class="fixed inset-0 z-[999]">
      <div
        class="absolute inset-0 bg-black/40 backdrop-blur-sm"
        @click="closeModal"
      ></div>

      <div
        class="absolute left-1/2 top-1/2 w-[calc(100%-32px)] max-w-[400px] -translate-x-1/2 -translate-y-1/2"
      >
        <Card
          class="flex flex-col max-h-[70vh] rounded-2xl bg-white shadow-2xl overflow-hidden animate-in fade-in zoom-in-95 duration-200"
        >
          <div
            class="px-5 py-4 border-b border-[#f1f3f5] flex items-center justify-between bg-white"
          >
            <h3 class="text-base font-bold text-[#1e3a5f]">
              {{ modalTitle }}
            </h3>
            <button
              type="button"
              class="text-[#adb5bd] hover:text-[#495057] transition-colors p-1"
              @click="closeModal"
            >
              <X class="w-5 h-5" />
            </button>
          </div>

          <div
            class="p-5 overflow-y-auto text-sm text-[#495057] leading-relaxed whitespace-pre-line bg-[#f8f9fa]"
          >
            <div v-html="modalContentHtml"></div>
          </div>

          <div class="p-4 bg-white border-t border-[#f1f3f5]">
            <Button
              type="button"
              class="w-full h-11 bg-[#1e3a5f] text-white font-semibold rounded-xl hover:bg-[#162c4b] transition-colors"
              @click="closeModal"
            >
              확인
            </Button>
          </div>
        </Card>
      </div>
    </div>
  </Transition>

  <ConfirmModal
    :is-open="isAlertOpen"
    :message="alertMessage"
    :show-cancel="false"
    confirm-text="확인"
    @confirm="handleAlertConfirm"
    @close="handleAlertConfirm"
  />
</template>

<style scoped>
/* 커스텀 체크박스 스타일링을 위해 appearance-none을 사용했으므로 별도 CSS 불필요 */
/* 모달 애니메이션을 위한 Transition 클래스는 Tailwind로 대체하거나 필요시 추가 */
</style>
