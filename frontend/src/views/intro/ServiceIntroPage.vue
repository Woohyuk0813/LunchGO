<script setup>
import { ArrowLeft } from 'lucide-vue-next'; // Import Lucide icons for Vue
import Button from '@/components/ui/Button.vue'; // Import our custom Button component
import { RouterLink } from 'vue-router'; // Import Vue RouterLink
</script>

<template>
  <div class="min-h-screen bg-white">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div
        class="max-w-[500px] mx-auto px-4 h-14 flex items-center justify-between"
      >
        <RouterLink to="/" class="p-2 -ml-2">
          <ArrowLeft class="w-5 h-5 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="text-base font-semibold text-[#1e3a5f]">서비스 소개</h1>
        <div class="w-9" />
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-8">
      <!-- Hero Section -->
      <section class="px-6 py-8 text-center">
        <h2 class="text-2xl font-bold text-[#1e3a5f] mb-2">
          직장인 회식, 이젠 쉽고 빠르게
        </h2>
        <p class="text-sm text-[#6c757d]">걱정 없는 회식 예약 플랫폼</p>
      </section>

      <!-- Service Description -->
      <section class="px-4 pb-6">
        <h3 class="text-base font-semibold text-[#1e3a5f] mb-3 px-2">
          어떤 서비스를 제공하나요?
        </h3>
        <div class="border-2 border-[#e9ecef] rounded-2xl p-4 bg-white">
          <p class="text-sm text-[#495057] leading-relaxed mb-4">
            런치고 서비스는 직장인 정상 회식을 위한 전문 예약 서비스입니다.
            인원수, 예산, 분위기, 팀원의 선호 음식과 재료에 맞는 최적의 식당을
            추천하며, 간편하게 예약할 수 있는 도구를 드립니다.
          </p>
          <div class="rounded-xl overflow-hidden">
            <img
              src="/office-workers-enjoying-company-dinner-together-at.jpg"
              alt="직장인 회식 이미지"
              width="350"
              height="250"
              class="w-full h-auto object-cover"
            />
          </div>
        </div>
      </section>

      <!-- Main Features -->
      <section class="px-4 py-6">
        <h3 class="text-base font-semibold text-[#1e3a5f] mb-4 px-2">
          주요 기능
        </h3>
        <div class="space-y-3">
          <div class="border-2 border-[#e9ecef] rounded-2xl p-4 bg-white">
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-2">
              간편한 예약 시스템
            </h4>
            <p class="text-xs text-[#6c757d] leading-relaxed">
              날짜, 인원, 예산과 입력하여 맞춤 식당을 추천받고 즉시 예약할 수
              있습니다.
            </p>
          </div>

          <div class="border-2 border-[#e9ecef] rounded-2xl p-4 bg-white">
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-2">
              노쇼 방지 시스템
            </h4>
            <p class="text-xs text-[#6c757d] leading-relaxed">
              예약 확정 결제와 리마인드로 노쇼를 방지하고, 식당과 예약자 모두를
              보호합니다.
            </p>
          </div>

          <div class="border-2 border-[#e9ecef] rounded-2xl p-4 bg-white">
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-2">
              선 주문 시스템
            </h4>
            <p class="text-xs text-[#6c757d] leading-relaxed">
              소중한 점심시간의 20분을 아껴드립니다. 선 주문 및 결제 알림을 통해
              회사 식사 시간을 줄기만 수 있도록 고객님의 여유를 보장해드립니다.
            </p>
          </div>

          <div class="border-2 border-[#e9ecef] rounded-2xl p-4 bg-white">
            <h4 class="text-sm font-semibold text-[#1e3a5f] mb-2">
              신뢰할 수 있는 리뷰와 평점
            </h4>
            <p class="text-xs text-[#6c757d] leading-relaxed">
              실제 회식을 다녀온 회사 내 팀들이 솔직한 리뷰를 확인하고 안심하고
              선택하세요.
            </p>
          </div>
        </div>
      </section>

      <!-- How to Use -->
      <section class="px-4 py-6 bg-[#f8f9fa]">
        <h3 class="text-base font-semibold text-[#1e3a5f] mb-4 px-2">
          이용 방법
        </h3>
        <div
          class="border-2 border-[#e9ecef] rounded-2xl p-5 bg-white space-y-4"
        >
          <div class="flex gap-3">
            <div
              class="flex-shrink-0 w-8 h-8 rounded-full bg-[#f8f9fa] flex items-center justify-center"
            >
              <span class="text-[#1e3a5f] font-bold text-sm">1</span>
            </div>
            <div>
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-1">
                식당 검색 및 선택
              </h4>
              <p class="text-xs text-[#6c757d] leading-relaxed">
                위치, 인원, 예산, 팀원의 선호도에 맞는 추천 식당을
              </p>
            </div>
          </div>

          <div class="flex gap-3">
            <div
              class="flex-shrink-0 w-8 h-8 rounded-full bg-[#f8f9fa] flex items-center justify-center"
            >
              <span class="text-[#1e3a5f] font-bold text-sm">2</span>
            </div>
            <div>
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-1">
                예약 정보 입력
              </h4>
              <p class="text-xs text-[#6c757d] leading-relaxed">
                날짜, 시간, 인원수와 특별 요청사항을 입력합니다.
              </p>
            </div>
          </div>

          <div class="flex gap-3">
            <div
              class="flex-shrink-0 w-8 h-8 rounded-full bg-[#f8f9fa] flex items-center justify-center"
            >
              <span class="text-[#1e3a5f] font-bold text-sm">3</span>
            </div>
            <div>
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-1">
                예약 확정
              </h4>
              <p class="text-xs text-[#6c757d] leading-relaxed">
                식당에서 예약을 확인하면 알림을 받고, 예약이 확정됩니다.
              </p>
            </div>
          </div>

          <div class="flex gap-3">
            <div
              class="flex-shrink-0 w-8 h-8 rounded-full bg-[#f8f9fa] flex items-center justify-center"
            >
              <span class="text-[#1e3a5f] font-bold text-sm">4</span>
            </div>
            <div>
              <h4 class="text-sm font-semibold text-[#1e3a5f] mb-1">
                회식 즐기기
              </h4>
              <p class="text-xs text-[#6c757d] leading-relaxed">
                예약 시간에 맞춰 방문하고, 빠르고 즐거운 회식을 즐기세요.
              </p>
            </div>
          </div>
        </div>
      </section>

      <!-- CTA Section -->
      <section class="px-4 py-8">
        <div
          class="border-2 border-[#e9ecef] rounded-2xl p-6 bg-white text-center"
        >
          <h3 class="text-lg font-bold text-[#1e3a5f] mb-3">
            지금 바로 시작하세요
          </h3>
          <p class="text-sm text-[#6c757d] mb-5 leading-relaxed">
            간편한 회식 예약으로 더 이상 식당 찾기에 시간 낭비하지 마세요.
          </p>
          <RouterLink to="/">
            <Button
              class="w-full h-12 gradient-primary text-white font-semibold rounded-xl shadow-button-hover hover:shadow-button-pressed transition-all"
            >
              실시간 내 주변 회식 맞춤 식당 추천 받기
            </Button>
          </RouterLink>
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
