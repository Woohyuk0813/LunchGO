<script setup>
const props = defineProps({
  count: {
    type: Number,
    default: 3,
  },
});
</script>

<template>
  <div class="space-y-3">
    <div
      v-for="index in count"
      :key="index"
      class="relative overflow-hidden border border-[#e9ecef] rounded-xl bg-white shadow-card"
    >
      <div class="flex gap-3 p-2 animate-pulse">
        <div class="w-24 h-24 rounded-lg bg-[#e9ecef]" />
        <div class="flex-1 min-w-0 space-y-2">
          <div class="h-4 w-2/3 bg-[#e9ecef] rounded" />
          <div class="h-3 w-5/6 bg-[#f1f3f5] rounded" />
          <div class="h-3 w-1/3 bg-[#e9ecef] rounded" />
          <div class="flex gap-2">
            <div class="h-4 w-16 bg-[#f1f3f5] rounded-full" />
            <div class="h-4 w-20 bg-[#f1f3f5] rounded-full" />
          </div>
          <div class="h-4 w-24 bg-[#e9ecef] rounded" />
        </div>
      </div>
    </div>
  </div>
</template>
