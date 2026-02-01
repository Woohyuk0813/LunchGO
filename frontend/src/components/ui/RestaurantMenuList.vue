<template>
  <template v-if="menuCategories.length">
    <section
      v-for="(category, idx) in menuCategories"
      :key="category.id || idx"
      class="mb-6"
    >
      <div class="flex items-center justify-between mb-3 px-1">
        <h3 class="text-base font-semibold text-[#1e3a5f]">
          {{ category.name }}
        </h3>
        <p class="text-xs text-[#adb5bd]">총 {{ category.items.length }}개</p>
      </div>

      <div class="space-y-3">
        <Card
          v-for="(item, itemIdx) in category.items"
          :key="item.id || itemIdx"
          class="p-3 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md transition-shadow"
        >
          <div class="flex items-center gap-3">
            <img
              :src="item.image || fallbackImage"
              :alt="item.name"
              class="w-20 h-20 rounded-lg object-cover flex-shrink-0"
            />
            <div class="flex-1 min-w-0">
              <div class="flex items-start justify-between gap-4">
                <p class="menu-title text-[#1e3a5f]">
                  {{ item.name }}
                </p>
                <p class="font-semibold text-[#ff6b4a] whitespace-nowrap">
                  {{ item.price }}
                </p>
              </div>
              <p
                v-if="item.description"
                class="text-xs text-[#6c757d] leading-relaxed mt-1"
              >
                {{ item.description }}
              </p>
            </div>
          </div>
        </Card>
      </div>
    </section>
  </template>

  <div
    v-else
    class="py-20 text-center text-sm text-[#6c757d] bg-white rounded-2xl"
  >
    메뉴 정보가 곧 업데이트될 예정입니다.
  </div>
</template>

<script setup>
import Card from '@/components/ui/Card.vue';

defineProps({
  menuCategories: {
    type: Array,
    required: true,
  },
});

const fallbackImage = '/placeholder.svg';
</script>

<style scoped>
.menu-title {
  font-weight: 600;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
