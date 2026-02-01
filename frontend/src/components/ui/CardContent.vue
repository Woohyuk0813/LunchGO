<script setup>
import { computed, h } from 'vue';
import { cn } from '@/utils';

const CardContent = (props, { attrs, slots }) => {
  const contentClasses = computed(() => cn('px-6', props.class));
  return h(
    'div',
    {
      'data-slot': 'card-content',
      class: contentClasses.value,
      ...attrs,
    },
    slots.default && slots.default()
  );
};
CardContent.props = { class: { type: String, default: '' } };
</script>

<template>
  <component :is="CardContent" v-bind="$attrs">
    <slot></slot>
  </component>
</template>

<style scoped></style>
