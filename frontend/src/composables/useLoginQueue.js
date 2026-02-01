import { ref, computed, onBeforeUnmount } from "vue";
import httpRequest from "@/router/httpRequest";

const POLL_INTERVAL_MS = 2000;
const ESTIMATED_LOGIN_SECONDS = 0.8;
const DEFAULT_ESTIMATED_TIME = 5;

export const useLoginQueue = () => {
  const isWaiting = ref(false);
  const modalType = ref("waiting");
  const modalMessage = ref("로그인 요청이 많아 대기 중입니다.\n잠시만 기다려주세요...");
  const waitingCount = ref(0);
  const estimatedWaitTime = ref(0);
  const queueToken = ref(null);
  let pollTimer = null;
  let pendingResolve = null;
  let pendingReject = null;

  const formattedWaitTime = computed(() => {
    const totalSeconds = estimatedWaitTime.value;
    if (totalSeconds <= 0) return `약 ${DEFAULT_ESTIMATED_TIME}초`;

    const mins = Math.floor(totalSeconds / 60);
    const secs = totalSeconds % 60;

    if (mins > 0) {
      return `약 ${mins}분 ${secs > 0 ? `${secs}초` : ""}`.trim();
    }
    return `약 ${totalSeconds}초`;
  });

  const updateWaitState = (status) => {
    const count = Number(status?.waitingCount ?? 0);
    const capacity = Number(status?.capacity ?? 0);
    waitingCount.value = Number.isFinite(count) ? count : 0;

    if (waitingCount.value <= 0) {
      estimatedWaitTime.value = DEFAULT_ESTIMATED_TIME;
      return;
    }

    const effectiveCapacity =
      Number.isFinite(capacity) && capacity > 0 ? capacity : 1;
    const estimatedSeconds =
      (waitingCount.value / effectiveCapacity) * ESTIMATED_LOGIN_SECONDS;
    estimatedWaitTime.value = Math.max(
      DEFAULT_ESTIMATED_TIME,
      Math.round(estimatedSeconds)
    );
  };

  const clearPolling = () => {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
  };

  const resetQueueState = () => {
    clearPolling();
    queueToken.value = null;
    waitingCount.value = 0;
    estimatedWaitTime.value = 0;
    modalType.value = "waiting";
    modalMessage.value =
      "로그인 요청이 많아 대기 중입니다.\n잠시만 기다려주세요...";
    isWaiting.value = false;
    pendingResolve = null;
    pendingReject = null;
  };

  const leaveQueue = async () => {
    if (!queueToken.value) return;
    try {
      await httpRequest.delete("/api/login/queue", {
        params: { token: queueToken.value },
        skipAuth: true,
      });
    } catch (error) {
      // ignore
    }
  };

  const joinQueue = async () => {
    const response = await httpRequest.post(
      "/api/login/queue",
      {},
      { skipAuth: true }
    );
    return response.data;
  };

  const fetchStatus = async () => {
    if (!queueToken.value) return null;
    const response = await httpRequest.get(
      "/api/login/queue",
      { token: queueToken.value },
      { skipAuth: true }
    );
    return response.data;
  };

  const handleQueueError = (message) => {
    modalType.value = "error";
    modalMessage.value = message;
    isWaiting.value = true;
  };

  const waitForTurn = async () => {
    if (pendingResolve) {
      return new Promise((resolve, reject) => {
        const resolveWrapper = pendingResolve;
        const rejectWrapper = pendingReject;
        pendingResolve = (token) => {
          resolveWrapper(token);
          resolve(token);
        };
        pendingReject = (error) => {
          rejectWrapper(error);
          reject(error);
        };
      });
    }

    try {
      const joinStatus = await joinQueue();
      queueToken.value = joinStatus?.queueToken || null;
      updateWaitState(joinStatus);

      if (joinStatus?.allowed) {
        return queueToken.value;
      }

      if (!queueToken.value) {
        handleQueueError("대기열 정보를 불러오지 못했습니다.\n다시 시도해주세요.");
        throw new Error("queue-token-missing");
      }

      isWaiting.value = true;
      modalType.value = "waiting";

      return await new Promise((resolve, reject) => {
        pendingResolve = resolve;
        pendingReject = reject;
        pollTimer = setInterval(async () => {
          try {
            const status = await fetchStatus();
            if (!status) {
              handleQueueError(
                "대기열 상태를 확인할 수 없습니다.\n다시 시도해주세요."
              );
              clearPolling();
              reject(new Error("queue-status-missing"));
              pendingResolve = null;
              pendingReject = null;
              return;
            }

            updateWaitState(status);

            if (status.allowed) {
              clearPolling();
              isWaiting.value = false;
              pendingResolve = null;
              pendingReject = null;
              resolve(queueToken.value);
              return;
            }

            if (status.expired) {
              clearPolling();
              handleQueueError(
                "대기열 시간이 만료되었습니다.\n다시 시도해주세요."
              );
              reject(new Error("queue-expired"));
              pendingResolve = null;
              pendingReject = null;
              return;
            }
          } catch (error) {
            clearPolling();
            handleQueueError(
              "대기열 상태를 확인할 수 없습니다.\n다시 시도해주세요."
            );
            reject(error);
            pendingResolve = null;
            pendingReject = null;
            return;
          }
        }, POLL_INTERVAL_MS);
      });
    } catch (error) {
      clearPolling();
      throw error;
    }
  };

  const handleQueueModalClose = () => {
    leaveQueue();
    resetQueueState();
  };

  onBeforeUnmount(() => {
    leaveQueue();
    resetQueueState();
  });

  return {
    isWaiting,
    modalType,
    modalMessage,
    waitingCount,
    formattedWaitTime,
    waitForTurn,
    handleQueueModalClose,
    resetQueueState,
  };
};
