<script setup>
import { computed, h } from 'vue';
import { cn } from '@/utils';

const CardTitle = (props, { attrs, slots }) => {
  const titleClasses = computed(() =>
    cn('leading-none font-semibold', props.class)
  );
  return h(
    'div',
    {
      'data-slot': 'card-title',
      class: titleClasses.value,
      ...attrs,
    },
    slots.default && slots.default()
  );
};
CardTitle.props = { class: { type: String, default: '' } };
</script>

<template>
  <component :is="CardTitle" v-bind="$attrs">
    <slot></slot>
  </component>
</template>

<style scoped></style>
