<script setup>
import { computed, h } from 'vue';
import { cn } from '@/utils';

const Card = (props, { attrs, slots }) => {
  const cardClasses = computed(() =>
    cn(
      'bg-card text-card-foreground flex flex-col gap-6 rounded-xl border py-6 shadow-sm',
      props.class
    )
  );
  return h(
    'div',
    {
      'data-slot': 'card',
      class: cardClasses.value,
      ...attrs,
    },
    slots.default && slots.default()
  );
};
Card.props = { class: { type: String, default: '' } };
</script>

<template>
  <component :is="Card" v-bind="$attrs">
    <slot></slot>
  </component>
</template>

<style scoped></style>
