<script setup>
import Card from "@/components/ui/Card.vue";
import { RouterLink } from "vue-router";
import FavoriteStarButton from "@/components/ui/FavoriteStarButton.vue";

defineProps({
  recommendations: {
    type: Array,
    required: true,
  },
});
</script>

<template>
  <section class="space-y-4">
    <div v-for="day in recommendations" :key="day.day" class="space-y-3">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-sm font-semibold text-[#1e3a5f]">
            {{ day.day }} · {{ day.date }}
          </p>
          <p class="text-xs text-[#6c757d]">제외 메뉴: {{ day.avoidMenu }}</p>
        </div>
        <span
          class="text-xs font-semibold px-2 py-1 rounded-full bg-[#f8f9fa] text-[#495057]"
        >
          팀 선호 반영
        </span>
      </div>

      <div class="space-y-3">
        <RouterLink
          v-for="restaurant in day.restaurants"
          :key="restaurant.id"
          :to="`/restaurant/${restaurant.id}`"
        >
          <Card
            class="relative overflow-hidden border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-lg transition-shadow cursor-pointer"
          >
            <FavoriteStarButton :restaurant-id="restaurant.id" />
            <div class="flex gap-3 p-2">
              <div
                class="relative w-24 h-24 flex-shrink-0 rounded-lg overflow-hidden"
              >
                <img
                  :src="restaurant.image || '/placeholder.svg'"
                  :alt="restaurant.name"
                  class="w-full h-full object-cover"
                />
              </div>

              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <h4 class="font-semibold text-[#1e3a5f] text-sm">
                    {{ restaurant.name }}
                  </h4>
                </div>
                <p class="text-xs text-[#6c757d] mb-1 truncate">
                  {{ restaurant.address }}
                </p>
                <div class="flex items-center gap-1 mb-1.5">
                  <span class="text-sm font-medium text-[#1e3a5f]">{{
                    restaurant.rating
                  }}</span>
                  <span class="text-xs text-[#6c757d]"
                    >(리뷰수 : {{ restaurant.reviews }})</span
                  >
                </div>
                <div class="flex flex-wrap gap-1 mb-2">
                  <span
                    v-for="(tag, index) in restaurant.topTags"
                    :key="index"
                    class="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full bg-gradient-to-r from-[#ff6b4a] to-[#ffc4b8] text-white"
                  >
                    {{ tag.name }} {{ tag.count }}
                  </span>
                </div>
                <p class="text-sm font-semibold text-[#1e3a5f]">
                  {{ restaurant.price }}
                </p>
              </div>
            </div>
          </Card>
        </RouterLink>
      </div>
    </div>
  </section>
</template>
