<script setup>
import { computed, h } from 'vue';
import { cn } from '@/utils';

const CardHeader = (props, { attrs, slots }) => {
  const headerClasses = computed(() =>
    cn(
      '@container/card-header grid auto-rows-min grid-rows-[auto_auto] items-start gap-2 px-6 has-data-[slot=card-action]:grid-cols-[1fr_auto] [.border-b]:pb-6',
      props.class
    )
  );
  return h(
    'div',
    {
      'data-slot': 'card-header',
      class: headerClasses.value,
      ...attrs,
    },
    slots.default && slots.default()
  );
};
CardHeader.props = { class: { type: String, default: '' } };
</script>

<template>
  <component :is="CardHeader" v-bind="$attrs">
    <slot></slot>
  </component>
</template>

<style scoped></style>
