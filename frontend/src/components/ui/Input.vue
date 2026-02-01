<script setup lang="ts">
import { computed, ref } from 'vue';
import { cn } from '@/utils';

// 1. 내부 input 태그를 가리킬 변수 생성
const inputRef = ref<HTMLInputElement | null>(null);

const props = defineProps({
  // v-model을 위한 필수 prop 추가
  modelValue: {
    type: [String, Number],
    default: '',
  },
  type: {
    type: String,
    default: 'text',
  },
  class: {
    type: String,
    default: '',
  },
});

// 2. 부모 컴포넌트에서 이 컴포넌트의 focus()를 호출할 수 있도록 노출
defineExpose({
  focus: () => {
    inputRef.value?.focus();
  },
});

// v-model 업데이트를 위한 emit 정의
const emit = defineEmits(['update:modelValue']);

const inputClasses = computed(() => {
  return cn(
    'file:text-foreground placeholder:text-muted-foreground selection:bg-primary selection:text-primary-foreground dark:bg-input/30 border-input h-9 w-full min-w-0 rounded-md border bg-transparent px-3 py-1 text-base shadow-xs transition-[color,box-shadow] outline-none file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium disabled:pointer-events-none disabled:cursor-not-allowed disabled:opacity-50 md:text-sm',
    'focus-visible:border-ring focus-visible:ring-ring/50 focus-visible:ring-[3px]',
    'aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive',
    props.class
  );
});

// 입력 이벤트 핸들러
const handleInput = (event: Event) => {
  const target = event.target as HTMLInputElement;
  emit('update:modelValue', target.value);
};
</script>

<template>
  <input
    :type="type"
    data-slot="input"
    :class="inputClasses"
    :value="modelValue"
    @input="handleInput"
    v-bind="$attrs"
  />
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles it. */
</style>
