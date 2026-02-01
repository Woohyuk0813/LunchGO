<script setup>
import { ref, computed, onBeforeUnmount, watch } from "vue";

const props = defineProps({
  isProcessing: {
    type: Boolean,
    default: false,
  },
  initialImageUrl: {
    type: String,
    default: "",
  },
  ocrResult: {
    type: Object,
    default: null,
  },
  days: {
    type: Array,
    default: () => [],
  },
  errorMessage: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["close", "run-ocr", "confirm", "file-change"]);

const previewUrl = ref("");
const localDays = ref([]);
const newMenuInputs = ref([]);
let currentObjectUrl = "";
const displayUrl = computed(() => previewUrl.value || props.initialImageUrl);
const hasFile = computed(() => Boolean(previewUrl.value));
const canConfirm = computed(() =>
  localDays.value.some((day) => (day.menus?.length ?? 0) > 0)
);

const handleFileChange = (event) => {
  const file = event.target.files?.[0];
  if (!file) {
    if (currentObjectUrl) {
      URL.revokeObjectURL(currentObjectUrl);
      currentObjectUrl = "";
    }
    previewUrl.value = "";
    emit("file-change", null);
    return;
  }

  if (currentObjectUrl) {
    URL.revokeObjectURL(currentObjectUrl);
  }

  currentObjectUrl = URL.createObjectURL(file);
  previewUrl.value = currentObjectUrl;
  emit("file-change", file);
};

const normalizeMenuInput = (value) =>
  value
    .split(/\n|,/)
    .map((item) => item.trim())
    .filter(Boolean);

const updateMenuItem = (dayIndex, menuIndex, value) => {
  const menus = localDays.value[dayIndex]?.menus;
  if (!menus) return;
  menus.splice(menuIndex, 1, value);
};

const removeMenuItem = (dayIndex, menuIndex) => {
  const menus = localDays.value[dayIndex]?.menus;
  if (!menus) return;
  menus.splice(menuIndex, 1);
};

const commitMenuInput = (dayIndex) => {
  const value = newMenuInputs.value[dayIndex] || "";
  const items = normalizeMenuInput(value);
  if (!items.length) return;
  const menus = localDays.value[dayIndex]?.menus;
  if (!menus) return;
  menus.push(...items);
  newMenuInputs.value[dayIndex] = "";
};

const handleConfirm = () => {
  const normalized = localDays.value.map((day) => ({
    ...day,
    menus: (day.menus || []).map((menu) => menu.trim()).filter(Boolean),
  }));
  localDays.value = normalized;
  emit("confirm", normalized);
};

watch(
  () => props.days,
  (value) => {
    localDays.value = (value || []).map((day) => ({
      ...day,
      menus: Array.isArray(day.menus) ? [...day.menus] : [],
    }));
    newMenuInputs.value = localDays.value.map(() => "");
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  if (currentObjectUrl) {
    URL.revokeObjectURL(currentObjectUrl);
  }
});
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed inset-0 z-[110] bg-black/50 flex items-center justify-center"
      @click.self="emit('close')"
    >
      <div class="w-full max-w-[520px] bg-white rounded-2xl shadow-xl mx-4 max-h-[85vh] flex flex-col">
        <div class="px-5 py-4 border-b border-[#e9ecef] flex items-center justify-between">
          <h3 class="text-lg font-semibold text-[#1e3a5f]">
            구내식당 주간 메뉴
          </h3>
          <button
            type="button"
            class="text-[#6c757d] hover:text-[#1e3a5f]"
            @click="emit('close')"
          >
            닫기
          </button>
        </div>

        <div class="p-5 space-y-4 overflow-y-auto">
          <p class="text-sm text-[#6c757d]">
            이번주 기준(월~금)으로 날짜가 분류됩니다. 메뉴 사진을 올리면 대체 식당을 추천해줄게요.
          </p>

          <div class="space-y-2">
            <label class="text-sm font-semibold text-[#1e3a5f]">
              메뉴 사진 업로드
            </label>
            <input
              type="file"
              accept="image/*"
              class="w-full text-sm file:mr-4 file:py-2 file:px-3 file:rounded-lg file:border-0 file:bg-[#f8f9fa] file:text-[#495057] hover:file:bg-[#e9ecef]"
              @change="handleFileChange"
            />
          </div>

          <div
            class="border border-dashed border-[#dee2e6] rounded-xl p-4 text-center bg-[#f8f9fa]"
          >
            <template v-if="displayUrl">
              <img
                :src="displayUrl"
                alt="주간 메뉴 미리보기"
                class="max-h-[280px] w-full object-contain rounded-lg bg-white"
              />
            </template>
            <template v-else>
              <p class="text-sm text-[#6c757d]">
                업로드된 사진이 여기에 표시됩니다.
              </p>
            </template>
          </div>

          <div class="space-y-4">
            <div
              v-if="ocrResult || localDays.length"
              class="rounded-xl border border-[#e9ecef] bg-white p-4 space-y-3"
            >
              <p class="text-sm font-semibold text-[#1e3a5f]">
                OCR 인식 결과
              </p>
              <p v-if="ocrResult && !ocrResult.ocrSuccess" class="text-xs text-[#e03131]">
                OCR 인식이 불완전해요. 날짜별 주요 메뉴를 직접 입력해주세요.
              </p>
              <p v-else-if="ocrResult" class="text-xs text-[#6c757d]">
                날짜별 메뉴를 확인하고 수정할 수 있어요.
              </p>
              <div class="space-y-4">
                <div v-for="(day, index) in localDays" :key="day.date">
                  <label class="text-xs font-semibold text-[#1e3a5f]">
                    {{ day.day }} ({{ day.date }})
                  </label>
                  <div class="mt-2 space-y-2">
                    <div
                      v-for="(menu, menuIndex) in day.menus"
                      :key="`${day.date}-${menuIndex}`"
                      class="flex items-center gap-2"
                    >
                      <input
                        type="text"
                        class="flex-1 rounded-lg border border-[#dee2e6] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#ff6b4a]"
                        :value="menu"
                        placeholder="메뉴명을 입력하세요"
                        @input="updateMenuItem(index, menuIndex, $event.target.value)"
                      />
                      <button
                        type="button"
                        class="px-2.5 py-2 rounded-lg border border-[#e9ecef] text-xs text-[#6c757d] hover:bg-[#f1f3f5]"
                        @click="removeMenuItem(index, menuIndex)"
                      >
                        삭제
                      </button>
                    </div>
                    <div class="flex items-center gap-2">
                      <input
                        v-model="newMenuInputs[index]"
                        type="text"
                        class="flex-1 rounded-lg border border-dashed border-[#dee2e6] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#ff6b4a]"
                        placeholder="메뉴 추가 (쉼표 또는 줄바꿈 가능)"
                        @keydown.enter.prevent="commitMenuInput(index)"
                      />
                      <button
                        type="button"
                        class="px-3 py-2 rounded-lg text-xs font-semibold gradient-primary text-white"
                        @click="commitMenuInput(index)"
                      >
                        추가
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              <div
                v-if="ocrResult && ocrResult.detectedMenus?.length"
                class="text-xs text-[#6c757d]"
              >
                인식된 메뉴: {{ ocrResult.detectedMenus.join(', ') }}
              </div>
            </div>

            <p v-if="errorMessage" class="text-xs text-[#e03131]">
              {{ errorMessage }}
            </p>
          </div>
        </div>

        <div class="px-5 py-4 border-t border-[#e9ecef] flex justify-end gap-2 bg-white">
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-colors bg-[#f8f9fa] text-[#495057] hover:bg-[#e9ecef]"
            @click="emit('close')"
          >
            닫기
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-colors gradient-primary text-white disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="!hasFile || isProcessing"
            @click="emit('run-ocr')"
          >
            OCR 인식하기
          </button>
          <button
            v-if="ocrResult"
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium transition-colors bg-[#1e3a5f] text-white disabled:opacity-60 disabled:cursor-not-allowed"
            :disabled="!canConfirm || isProcessing"
            @click="handleConfirm"
          >
            확정 후 추천
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
