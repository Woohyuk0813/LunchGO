<script setup>
import { ref, computed, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ArrowLeft, Bot, Send } from "lucide-vue-next";
import httpRequest from "@/router/httpRequest.js";
import { useAccountStore } from "@/stores/account";

const router = useRouter();
const accountStore = useAccountStore();
const isLoggedIn = computed(() =>
  Boolean(accountStore.accessToken || localStorage.getItem("accessToken"))
);
const member = computed(() => {
  if (accountStore.member) {
    return accountStore.member;
  }
  const raw = localStorage.getItem("member");
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch (error) {
    return null;
  }
});
const role = computed(() => member.value?.role || "");
const getChatbotUserId = () => {
  const stored = localStorage.getItem("chatbotUserId");
  if (stored) {
    return stored;
  }
  let generated = "";
  if (window.crypto && typeof window.crypto.randomUUID === "function") {
    generated = window.crypto.randomUUID();
  } else {
    generated = `chatbot-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  }
  localStorage.setItem("chatbotUserId", generated);
  return generated;
};
const chatbotUserId = getChatbotUserId();

const inputMessage = ref("");
const isSending = ref(false);
const messages = ref([]);
const messagesEndRef = ref(null);

const appendMessage = (message) => {
  messages.value.push(message);
  nextTick(() => {
    messagesEndRef.value?.scrollIntoView({ behavior: "smooth", block: "end" });
    requestAnimationFrame(() => {
      window.scrollTo({
        top: document.documentElement.scrollHeight,
        behavior: "smooth",
      });
    });
  });
};

const appendBubbles = (data, fallbackText = "응답이 없습니다.") => {
  if (!data || !data.bubbles || !data.bubbles.length) {
    if (fallbackText) {
      appendMessage({ role: "bot", type: "text", text: fallbackText });
    }
    return;
  }
  data.bubbles.forEach((bubble) => {
    if (bubble.type === "text") {
      appendMessage({
        role: "bot",
        type: "text",
        text: bubble?.data?.description || "",
      });
    } else if (bubble.type === "template") {
      appendMessage({
        role: "bot",
        type: "template",
        template: bubble?.data || {},
      });
    }
  });
};

const addTypingIndicator = () => {
  const hasTyping = messages.value.some((message) => message.type === "typing");
  if (!hasTyping) {
    appendMessage({ role: "bot", type: "typing" });
  }
};

const removeTypingIndicator = () => {
  const index = messages.value.findIndex(
    (message) => message.type === "typing"
  );
  if (index >= 0) {
    messages.value.splice(index, 1);
  }
};

onMounted(async () => {
  if (!isLoggedIn.value) {
    router.replace({ name: "login", query: { next: "/chatbot" } });
    return;
  }
  if (role.value !== "ROLE_USER") {
    router.replace({ name: "home" });
    return;
  }
  try {
    addTypingIndicator();
    const response = await httpRequest.post("/api/chatbot/open", {
      message: "open",
      userId: chatbotUserId,
    });
    removeTypingIndicator();
    appendBubbles(response?.data, "무엇을 도와드릴까요?");
  } catch (error) {
    removeTypingIndicator();
    appendMessage({ role: "bot", type: "text", text: "무엇을 도와드릴까요?" });
    console.error("챗봇 오픈 실패:", error);
  }
});

const sendMessage = async (requestMessage = null, displayText = null) => {
  const normalizedRequest =
    typeof requestMessage === "string" ? requestMessage : inputMessage.value;
  const normalizedDisplay =
    typeof displayText === "string" ? displayText : inputMessage.value;
  const outgoing = (normalizedRequest || "").trim();
  const shownText = (normalizedDisplay || normalizedRequest || "").trim();
  if (!outgoing || isSending.value) {
    return;
  }
  appendMessage({ role: "user", type: "text", text: shownText || outgoing });
  inputMessage.value = "";
  isSending.value = true;
  addTypingIndicator();

  try {
    const response = await httpRequest.post("/api/chatbot/message", {
      message: outgoing,
      userId: chatbotUserId,
    });
    removeTypingIndicator();
    appendBubbles(response?.data);
  } catch (error) {
    removeTypingIndicator();
    appendMessage({ role: "bot", type: "text", text: "[error] 서버가 요청에 응답하지 않습니다." });
    console.error("챗봇 전송 실패:", error);
  } finally {
    isSending.value = false;
  }
};

const handleTemplateButton = async (action) => {
  const postback = action?.data?.postbackFull || action?.data?.postback || "";
  const displayText = action?.data?.displayText || "";
  if (!postback) {
    return;
  }
  const resolvedText = displayText || postback || "버튼 선택";
  inputMessage.value = resolvedText;
  sendMessage(postback, resolvedText);
};
</script>

<template>
  <div class="min-h-screen bg-[#f6f7f9] pb-24">
    <header class="sticky top-0 z-10 border-b border-[#e9ecef] bg-white px-5 py-4">
      <div class="flex items-center justify-between text-[#1e3a5f]">
        <button
          type="button"
          class="flex items-center gap-2 text-sm font-semibold"
          @click="router.back()"
          aria-label="뒤로 가기"
        >
          <ArrowLeft class="h-4 w-4" />
        </button>
        <div class="flex items-center gap-2">
          <Bot class="h-5 w-5" />
          <h1 class="text-base font-semibold">LunchGo 고객센터 챗봇</h1>
        </div>
        <span class="w-12" aria-hidden="true"></span>
      </div>
    </header>

    <section class="mx-auto flex max-w-[720px] flex-col gap-4 px-5 py-6">
      <div
        v-for="(message, index) in messages"
        :key="`${message.role}-${index}`"
        class="flex"
        :class="message.role === 'user' ? 'justify-end' : 'justify-start'"
      >
        <div
          class="max-w-[80%] rounded-2xl px-4 py-3 text-sm shadow-sm"
          :class="
            message.role === 'user'
              ? 'bg-[#1e3a5f] text-white'
              : 'bg-white text-[#2b2f33]'
          "
        >
          <template v-if="message.type === 'typing'">
            <span class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </template>
          <template v-else-if="message.type === 'template'">
            <p class="text-sm leading-relaxed">
              {{ message.template?.cover?.data?.description || "안내 메시지" }}
            </p>
            <div
              v-if="message.template?.contentTable?.length"
              class="mt-3 flex flex-col gap-2"
            >
              <button
                v-for="(row, rowIndex) in message.template.contentTable"
                :key="`row-${rowIndex}`"
                type="button"
                class="w-full rounded-md border border-[#d0d4d9] bg-[#f7f8fa] px-3 py-2 text-xs font-semibold text-[#4b4f56] transition hover:bg-[#eef1f3]"
                @click="
                  handleTemplateButton(
                    row?.[0]?.data?.data?.action || row?.[0]?.data?.action || row?.[0]?.action
                  )
                "
              >
                {{ row?.[0]?.data?.title || "선택" }}
              </button>
            </div>
          </template>
          <template v-else>
            {{ message.text }}
          </template>
        </div>
      </div>
      <div ref="messagesEndRef"></div>
    </section>

    <div class="fixed bottom-0 left-0 right-0 border-t border-[#e9ecef] bg-white">
      <div class="mx-auto flex max-w-[720px] items-center gap-3 px-5 py-4">
        <input
          v-model="inputMessage"
          type="text"
          class="h-11 flex-1 rounded-full border border-[#d0d4d9] px-4 text-sm text-[#2b2f33] outline-none transition focus:border-[#1e3a5f]"
          placeholder="메시지를 입력하세요"
          @keyup.enter="sendMessage"
        />
        <button
          type="button"
          class="flex h-11 w-11 items-center justify-center rounded-full bg-[#1e3a5f] text-white transition hover:bg-[#17314f] disabled:opacity-60"
          :disabled="isSending"
          @click="sendMessage"
        >
          <Send class="h-4 w-4" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.typing-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 12px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 9999px;
  background-color: #8b9097;
  animation: typingPulse 1.2s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typingPulse {
  0%,
  100% {
    opacity: 0.3;
    transform: translateY(0);
  }
  50% {
    opacity: 1;
    transform: translateY(-2px);
  }
}
</style>
