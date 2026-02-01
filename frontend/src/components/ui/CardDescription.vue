<script setup>
import { computed, h } from 'vue';
import { cn } from '@/utils';

const CardDescription = (props, { attrs, slots }) => {
  const descriptionClasses = computed(() =>
    cn('text-muted-foreground text-sm', props.class)
  );
  return h(
    'div',
    {
      'data-slot': 'card-description',
      class: descriptionClasses.value,
      ...attrs,
    },
    slots.default && slots.default()
  );
};
CardDescription.props = { class: { type: String, default: '' } };
</script>

<template>
  <component :is="CardDescription" v-bind="$attrs">
    <slot></slot>
  </component>
</template>

<style scoped></style>
