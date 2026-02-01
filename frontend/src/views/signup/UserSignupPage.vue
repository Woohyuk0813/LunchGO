<script setup lang="ts">
import { ref, watch, computed, onUnmounted, onMounted } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { ArrowLeft, ChevronRight, ChevronDown, Check, X } from 'lucide-vue-next'; // 아이콘 추가
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';
import ConfirmModal from '@/components/ui/ConfirmModal.vue';
import Input from '@/components/ui/Input.vue';
import TERMS_POLICY_TEXT from '@/content/serviceTerms.md?raw';
import PRIVACY_POLICY_TEXT from '@/content/privacyPolicy.md?raw';
import MARKETING_POLICY_TEXT from '@/content/marketingPolicy.md?raw';
import { marked } from 'marked';
import httpRequest from '@/router/httpRequest';

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
const emailLocal = ref('');
const emailDomain = ref('');
const emailDomains = [
  'gmail.com',
  'naver.com',
  'daum.net',
  'kakao.com',
  'outlook.com',
  'hotmail.com',
  'yahoo.com',
];
const email = computed(() => {
  if (!emailLocal.value || !emailDomain.value) return '';
  return `${emailLocal.value}@${emailDomain.value}`;
});
const isEmailUnique = ref(false);
const isEmailDomainOpen = ref(false);
const emailDomainDropdownRef = ref<HTMLElement | null>(null);
const phone = ref('');
const verificationCode = ref(''); //사용자가 입력한 인증번호
const password = ref('');
const passwordConfirm = ref('');
const companyName = ref('');
const companyFrontAddress = ref('');
const companyBackAddress = ref('');

//컴포넌트 ref 생성
const passwordConfirmRef = ref<any>(null);
//상세주소 focus를 위한 ref 생성
const companyBackAddressRef = ref<any>(null);

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
const agreeMarketing = ref(false);

// 전체 동의 체크박스 상태 (Computed 처럼 동작하게 하거나 watch로 제어)
const agreeAll = ref(false);

watch([emailLocal, emailDomain], () => {
  isEmailUnique.value = false;
});

//도로명주소 api
const loadDaumPostcodeScript = () => {
  const scriptId = 'daum-postcode-script';
  const existingScript = document.getElementById(scriptId);

  if (!existingScript) {
    const script = document.createElement('script');
    script.src =
      '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
    script.id = scriptId;
    document.body.appendChild(script);
  }
};

//컴포넌트 마운트 시 스크립트 로드
onMounted(() => {
  loadDaumPostcodeScript();
});

//주소 검색 핸들러
const handleAddressSearch = () => {
  //TypeScript에서 window객체의 daum 프로퍼티 접근을 위해 any 타입 단언 사용
  if (!(window as any).daum || !(window as any).daum.Postcode) {
    openAlert('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
    loadDaumPostcodeScript();
    return;
  }

  new (window as any).daum.Postcode({
    oncomplete: function (data: any) {
      let addr = '';
      if (data.userSelectedType === 'R') {
        addr = data.roadAddress;
      } else {
        addr = data.jibunAddress;
      }

      companyFrontAddress.value = addr;

      // (Vue의 ref를 통해 포커스)
      if (companyBackAddressRef.value) {
        // Input 컴포넌트 내부의 실제 input 요소에 접근하거나,
        // 컴포넌트가 expose한 focus 메서드를 호출해야 함.
        // 일반적인 HTML input ref라면 .focus() 바로 사용 가능
        companyBackAddressRef.value.$el.querySelector('input')?.focus() ||
          companyBackAddressRef.value.focus?.();
      }
    },
  }).open();
};

// 전체 동의 로직
watch(agreeAll, (newVal) => {
  if (newVal) {
    agreeTerms.value = true;
    agreePrivacy.value = true;
    agreeMarketing.value = true;
  } else {
    // 개별 해제가 아니라 사용자가 직접 '전체 해제'를 눌렀을 때만 동작하도록
    // (여기서는 단순화를 위해 전체 토글로 구현, 필요 시 로직 세분화 가능)
    if (agreeTerms.value && agreePrivacy.value && agreeMarketing.value) {
      agreeTerms.value = false;
      agreePrivacy.value = false;
      agreeMarketing.value = false;
    }
  }
});

// 개별 체크박스가 변경될 때 '전체 동의' 체크박스 상태 업데이트
watch(
  [agreeTerms, agreePrivacy, agreeMarketing],
  ([terms, privacy, marketing]) => {
    agreeAll.value = terms && privacy && marketing;
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
    }
  }, 1000);
};

onUnmounted(() => {
  if (timerInterval.value) clearInterval(timerInterval.value);
  document.removeEventListener('click', handleEmailDomainClickOutside);
});

const handleEmailDomainClickOutside = (event: MouseEvent) => {
  if (!emailDomainDropdownRef.value) return;
  if (!emailDomainDropdownRef.value.contains(event.target as Node)) {
    isEmailDomainOpen.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleEmailDomainClickOutside);
});

const toggleEmailDomainDropdown = () => {
  if (isEmailUnique.value) return;
  isEmailDomainOpen.value = !isEmailDomainOpen.value;
};

const selectEmailDomain = (domain: string) => {
  emailDomain.value = domain;
  isEmailDomainOpen.value = false;
};

const checkInputElement = () => {
  if (!emailLocal.value || !emailDomain.value) return openAlert('이메일을 입력해주세요.');
  if (emailLocal.value.length < 5)
    return openAlert('이메일 아이디는 5자 이상이어야 합니다.');

  if (!isEmailUnique.value) return openAlert('이메일 중복확인이 필요합니다.');
  if (!password.value) return openAlert('비밀번호를 입력해주세요.');

  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,20}$/;

  if (!passwordRegex.test(password.value))
    return openAlert(
      '비밀번호는 8~20자이어야 하며, 영문 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.'
    );
  if (!passwordConfirm.value) return openAlert('비밀번호 재입력이 필요합니다.');
  if (!name.value) return openAlert('이름을 입력해주세요.');
  if (!companyName.value) return openAlert('회사명을 입력해주세요.');
  if (!companyFrontAddress.value) return openAlert('도로명주소를 입력해주세요.');
  return false;
};

// 인증번호 발송
const handleSendVerifyCode = async () => {
  if (checkInputElement()) {
    return;
  }

  isPhoneVerified.value = false;
  verificationCode.value = '';

  openAlert(`인증번호를 발송했습니다: ${phone.value}`);
  try {
    await httpRequest.post('/api/sms/send', { phone: phone.value }, { skipAuth: true });

    isCodeSent.value = true;
    startTimer(); 
  }catch(error){
    const status = error.response.status;

    if (status === 400) openAlert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
    else openAlert(`메시지 전송에 오류가 발생했습니다. (Code: ${status})`);
  }  
};

// 인증번호 확인
const handleVerifyCode = async () => {
  if (!verificationCode.value) return openAlert('인증번호를 입력해주세요.');
  if (isTimeout.value) return openAlert('입력 시간이 초과되었습니다. 재발송해주세요.');

  try {
    const response = await httpRequest.post('/api/sms/verify', {
      phone: phone.value,
      verifyCode: verificationCode.value
    }, { skipAuth: true });

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
};

// 모달 관련 상태
const isTermsModalOpen = ref(false);
const modalTitle = ref('');
const modalContent = ref('');
const modalContentHtml = computed(() => marked.parse(modalContent.value, { breaks: true }));

const handleEmailDuplicateCheck = async () => {
  if (isEmailUnique.value) {
    isEmailUnique.value = false;
    return;
  }
  if (!emailLocal.value || !emailDomain.value) return openAlert('이메일을 입력해주세요.');

  //백엔드 연동해서 이메일 unique한지 check
  try{
    await httpRequest.post('/api/auth/email', {
      email: email.value
    }, { skipAuth: true });

    openAlert('사용 가능한 이메일입니다.');
    isEmailUnique.value = true;
  }catch(error){
    const status = error.response.status;

    switch(status){
      case 400:
        openAlert("[400 Bad Request] 잘못된 요청입니다. 입력값을 확인해주세요.");
        break;
      case 409:
        openAlert("이미 사용중인 이메일입니다.");
        emailLocal.value = '';
        emailDomain.value = '';
        break;
      default:
        openAlert(`오류가 발생했습니다. (Code: ${status})`);
    }
  }
};

// 이용약관 모달 열기 -> 내용 수정 필수!!
const openModal = (type) => {
  isTermsModalOpen.value = true;
  if (type === 'terms') {
    modalTitle.value = '서비스 이용약관';
    modalContent.value = TERMS_POLICY_TEXT;
  } else if (type === 'privacy') {
    modalTitle.value = '개인정보 처리방침';
    modalContent.value = PRIVACY_POLICY_TEXT;
  } else if (type === 'marketing') {
    modalTitle.value = '마케팅 정보 수신 동의';
    modalContent.value = MARKETING_POLICY_TEXT;
  } else {
    modalTitle.value = '취소 및 환불 정책';
    modalContent.value = '예약 시간 1시간 전까지 취소 가능하며...';
  }
};

// 모달 닫기
const closeModal = () => {
  isTermsModalOpen.value = false;
};

const handleSignup = async () => {
  if (checkInputElement()) return;
  if (!isCodeSent.value) return openAlert('휴대전화 인증은 필수입니다.');

  if (password.value !== passwordConfirm.value) {
    //비밀번호 확인 입력창 비우고 포커스 이동
    passwordConfirm.value = '';
    passwordConfirmRef.value?.focus(); 
    openAlert('비밀번호가 일치하지 않습니다.');
    return;
  }

  // 인증번호 체크
  if (!isPhoneVerified.value) return openAlert('휴대전화 인증은 필수입니다.');

  if (!agreeTerms.value || !agreePrivacy.value) {
    openAlert('필수 약관에 동의해주세요.');
    return;
  }

  //회원가입 시 도로명주소와 상세주소를 합쳐서 백엔드로 전달
  let totalAddress = companyFrontAddress.value + ' ' + companyBackAddress.value;

  try {
    await httpRequest.post('/api/join/user', {
      email: email.value, password: password.value, name: name.value, 
      phone: phone.value, companyName: companyName.value,
      companyAddress: totalAddress, marketingAgree: agreeMarketing.value
    }, { skipAuth: true });

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
        <h1 class="text-2xl font-bold text-[#1e3a5f] mb-2">회원가입</h1>
        <p class="text-sm text-[#6c757d]">
          런치고 회원이 되어 편리한 회식 예약을 경험하세요
        </p>
      </div>

      <Card class="border-[#e9ecef] rounded-xl bg-white shadow-card p-6">
        <form @submit.prevent="handleSignup" class="space-y-4">
          <div>
            <label
              for="email"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >이메일</label
            >
            <div class="flex gap-2">
              <div class="flex flex-1 items-center gap-2">
                <Input
                  id="email"
                  type="text"
                  placeholder="아이디"
                  minlength="5"
                  v-model="emailLocal"
                  :readonly="isEmailUnique"
                  class="flex-1 h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
                />
                <span class="text-[#6c757d]">@</span>
                <div ref="emailDomainDropdownRef" class="relative">
                  <button
                    type="button"
                    :class="[
                      'h-12 min-w-[140px] px-3 border rounded-lg bg-white text-sm flex items-center justify-between gap-2 transition-colors',
                      isEmailUnique
                        ? 'bg-gray-100 text-gray-500 cursor-not-allowed border-[#dee2e6]'
                        : 'border-[#dee2e6] text-[#1e3a5f] hover:bg-white focus:border-[#ff6b4a]'
                    ]"
                    @click.stop="toggleEmailDomainDropdown"
                  >
                    <span class="truncate">
                      {{ emailDomain || '도메인 선택' }}
                    </span>
                    <ChevronDown class="w-4 h-4 text-[#1e3a5f]" />
                  </button>

                  <div
                    v-if="isEmailDomainOpen"
                    class="absolute left-0 right-0 mt-2 bg-white border border-[#e9ecef] rounded-lg shadow-md z-30 overflow-y-auto max-h-56"
                  >
                    <button
                      v-for="domain in emailDomains"
                      :key="domain"
                      type="button"
                      class="w-full text-left px-4 py-2 text-sm hover:bg-[#f8f9fa]"
                      @click.stop="selectEmailDomain(domain)"
                    >
                      {{ domain }}
                    </button>
                  </div>
                </div>
              </div>
              <Button
                type="button"
                @click="handleEmailDuplicateCheck"
                variant="outline"
                class="h-12 px-4 border-[#dee2e6] text-[#495057] bg-white hover:bg-[#f8f9fa] rounded-lg whitespace-nowrap"
              >
                {{ isEmailUnique ? '수정' : '중복확인' }}
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
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
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
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
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
              placeholder="이름을 입력하세요"
              maxlength="10"
              v-model="name"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
            />
          </div>

          <div>
            <label
              for="companyName"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >회사명</label
            >
            <Input
              id="companyName"
              type="text"
              placeholder="회사명을 입력하세요"
              maxlength="100"
              v-model="companyName"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f]"
            />
          </div>

          <div>
            <label
              for="companyAddress"
              class="block text-sm font-medium text-[#1e3a5f] mb-2"
              >회사주소</label
            >
            <Input
              id="companyAddress"
              type="text"
              placeholder="도로명주소를 입력하세요."
              v-model="companyFrontAddress"
              readonly
              @click="handleAddressSearch"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] cursor-pointer bg-white"
            />
            <Input
              ref="companyBackAddressRef"
              type="text"
              placeholder="상세주소를 입력하세요."
              maxlength="100"
              v-model="companyBackAddress"
              class="w-full h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] mt-2"
            />
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
                :readonly="isPhoneVerified"
                :class="`flex-1 h-12 px-4 border-[#dee2e6] rounded-lg focus:border-[#ff6b4a] focus:ring-1 focus:ring-[#ff6b4a] text-[#1e3a5f] ${isPhoneVerified ? 'bg-gray-100 text-gray-500 cursor-not-allowed focus:ring-0 focus:border-[#dee2e6] !important' : ''}`"
              />
              <Button
                type="button"
                @click="handleSendVerifyCode"
                :disabled="isPhoneVerified"
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

              <div class="flex items-center justify-between py-2 group">
                <label class="flex items-center gap-3 cursor-pointer flex-1">
                  <div
                    class="relative flex items-center justify-center w-5 h-5"
                  >
                    <input
                      type="checkbox"
                      v-model="agreeMarketing"
                      class="peer appearance-none w-5 h-5 border border-[#dee2e6] rounded checked:bg-[#ff6b4a] checked:border-[#ff6b4a] transition-colors"
                    />
                    <Check
                      class="absolute w-3.5 h-3.5 text-white pointer-events-none opacity-0 peer-checked:opacity-100"
                    />
                  </div>
                  <span
                    class="text-sm text-[#495057] group-hover:text-[#1e3a5f] transition-colors"
                  >
                    <span class="text-[#adb5bd] mr-1">(선택)</span>마케팅 정보
                    수신 동의
                  </span>
                </label>
                <button
                  type="button"
                  @click.stop="openModal('marketing')"
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
