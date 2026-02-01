<script setup>
import { computed, h } from 'vue';
import { cn } from '@/utils';

const CardAction = (props, { attrs, slots }) => {
  const actionClasses = computed(() =>
    cn(
      'col-start-2 row-span-2 row-start-1 self-start justify-self-end',
      props.class
    )
  );
  return h(
    'div',
    {
      'data-slot': 'card-action',
      class: actionClasses.value,
      ...attrs,
    },
    slots.default && slots.default()
  );
};
CardAction.props = { class: { type: String, default: '' } };
</script>

<template>
  <component :is="CardAction" v-bind="$attrs">
    <slot></slot>
  </component>
</template>

<style scoped></style>
