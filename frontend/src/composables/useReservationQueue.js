import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

const MAX_RETRIES = 10;
const RETRY_INTERVAL_MIN = 2000;
const RETRY_INTERVAL_MAX = 4000;
const ESTIMATED_TIME_PER_PERSON = 3;
const DEFAULT_ESTIMATED_TIME = 5;

export function useReservationQueue() {
  const router = useRouter();
  
  const isWaiting = ref(false);
  const modalType = ref('waiting'); // 'waiting' | 'error'
  const modalMessage = ref('');
  const queueErrorMessage = ref(''); // 컴포넌트의 에러 메시지와 구분하기 위해 이름 변경
  const currentWaitingCount = ref(0);
  const estimatedWaitTime = ref(0);

  // 예상 대기 시간 포맷팅 (초 -> 분/초)
  const formattedWaitTime = computed(() => {
    const totalSeconds = estimatedWaitTime.value;
    if (totalSeconds <= 0) return `약 ${DEFAULT_ESTIMATED_TIME}초`;
    
    const mins = Math.floor(totalSeconds / 60);
    const secs = totalSeconds % 60;
    
    if (mins > 0) {
      return `약 ${mins}분 ${secs > 0 ? secs + '초' : ''}`.trim();
    }
    return `약 ${totalSeconds}초`;
  });
  
  /**
   * 예약 대기열 처리 로직
   * @param {Function} apiCall 예약 생성 API 호출 함수 (Promise 반환)
   * @param {Function} onSuccess 성공 시 실행할 콜백
   * @param {Ref<Boolean>} isSubmitting 외부 로딩 상태 Ref
   */
  const processQueue = async (apiCall, onSuccess, isSubmitting) => {
    queueErrorMessage.value = '';
    modalType.value = 'waiting';
    let retryCount = 0;

    const attempt = async () => {
      try {
        const result = await apiCall();
        if (onSuccess) onSuccess(result);
      } catch (e) {
        const msg = e.response?.data?.message || e?.message || '';
        const waitingCount = e.response?.data?.waitingCount || 0;

        // 1. 중복 예약 등 에러 모달 ("대기 중" 없는 409)
        if (e.response?.status === 409 && !msg.includes('대기 중')) {
          modalType.value = 'error';
          modalMessage.value = (msg || '이미 처리된 예약이거나 잔여석이 부족합니다.').replace('. ', '.\n');
          isWaiting.value = true;
          return;
        }

        // 2. 대기열 진입 ("대기 중" 포함)
        if (e.response?.status === 409 && msg.includes('대기 중')) {
          if (retryCount < MAX_RETRIES) {
            retryCount++;
            currentWaitingCount.value = waitingCount;
            // 대기 시간 시뮬레이션: 인원당 약 3초 + 기본 5초
            estimatedWaitTime.value = waitingCount > 0 
              ? (waitingCount * ESTIMATED_TIME_PER_PERSON) 
              : DEFAULT_ESTIMATED_TIME;
            
            modalType.value = 'waiting';
            modalMessage.value = '예약 요청이 많아 대기 중입니다.\n잠시만 기다려주세요...';
            isWaiting.value = true;
            
            // 재시도 간격 분산 (Random Backoff/Jitter)
            const retryDelay = RETRY_INTERVAL_MIN + Math.floor(Math.random() * (RETRY_INTERVAL_MAX - RETRY_INTERVAL_MIN));
            setTimeout(attempt, retryDelay);
            return;
          } else {
            modalType.value = 'error';
            modalMessage.value = '현재 예약 요청이 많습니다.\n잠시 후 다시 시도해 주세요.';
            isWaiting.value = true;
          }
        } else {
          // 3. 기타 에러
          queueErrorMessage.value = msg || '예약 생성 중 오류가 발생했습니다.';
        }
      } finally {
        // 재시도 중이 아니고, 에러 모달 상태도 아닐 때만 로딩 해제
        if (modalType.value !== 'error' && (!isWaiting.value || retryCount >= MAX_RETRIES)) {
          isWaiting.value = false;
          if (isSubmitting) isSubmitting.value = false;
        }
      }
    };

    await attempt();
  };

  const handleQueueModalClose = () => {
    isWaiting.value = false;
    if (modalType.value === 'error') {
      router.push('/');
    }
  };

  return {
    isWaiting,
    modalType,
    modalMessage,
    queueErrorMessage,
    currentWaitingCount,
    estimatedWaitTime,
    formattedWaitTime,
    processQueue,
    handleQueueModalClose
  };
}
