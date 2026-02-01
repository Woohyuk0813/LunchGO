<script setup>
import { computed, h } from 'vue';
import { cn } from '@/utils';

const CardFooter = (props, { attrs, slots }) => {
  const footerClasses = computed(() =>
    cn('flex items-center px-6 [.border-t]:pt-6', props.class)
  );
  return h(
    'div',
    {
      'data-slot': 'card-footer',
      class: footerClasses.value,
      ...attrs,
    },
    slots.default && slots.default()
  );
};
CardFooter.props = { class: { type: String, default: '' } };
</script>

<template>
  <component :is="CardFooter" v-bind="$attrs">
    <slot></slot>
  </component>
</template>

<style scoped></style>
