<script setup>
import { RouterLink } from 'vue-router'; // Import Vue RouterLink
import { ArrowLeft, User } from 'lucide-vue-next'; // Import Lucide icons for Vue
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header
      class="bg-white border-b border-[#e9ecef] px-4 py-3 flex items-center gap-3"
    >
      <RouterLink to="/" class="text-[#1e3a5f]">
        <ArrowLeft class="w-5 h-5" />
      </RouterLink>
      <span class="text-lg font-semibold text-[#1e3a5f]">입점 문의</span>
    </header>

    <!-- Green Banner -->
    <div
      class="bg-gradient-to-r from-[#10b981] to-[#059669] px-6 py-4 text-center"
    >
      <p class="text-white text-base font-semibold">
        💰 🎉 지금 가입하시면 이달의 회식 맛집으로 노출됩니다.
      </p>
    </div>

    <div class="px-4 py-6 space-y-6">
      <!-- Main Title -->
      <div class="text-center space-y-3">
        <h1 class="text-2xl font-bold text-[#1e3a5f]">
          직장인 정심 회식 예약
          <br />
          이제 효율적으로 관리하세요
        </h1>
        <p class="text-sm text-[#6c757d] leading-relaxed">
          예약 확정부터 선결제, 매뉴 주문 주방 전송, 고정 매출 확보까지.
          <br />
          런치고로 매장 운영을 자동화하고 안정적인 수익을 만드세요.
        </p>
      </div>

      <!-- CTA Buttons -->
      <div class="flex gap-3">
        <RouterLink
          to="/partner/signup"
          class="flex-1 bg-[#007bff] text-white py-4 rounded-xl font-semibold text-base hover:bg-[#0056b3] transition-colors text-center"
        >
          무료로 시작하기
        </RouterLink>
        <button
          class="flex-1 bg-[#007bff] text-white py-4 rounded-xl font-semibold text-base hover:bg-[#0056b3] transition-colors"
        >
          상담 신청
        </button>
      </div>

      <!-- Metrics Cards -->
      <div class="grid grid-cols-3 gap-3">
        <div
          class="bg-white border-2 border-[#1e3a5f] rounded-xl p-4 text-center"
        >
          <p class="text-xs text-[#6c757d] mb-1">평균 매출 증가</p>
          <p class="text-xl font-bold gradient-primary-text">+23%</p>
        </div>
        <div
          class="bg-white border-2 border-[#1e3a5f] rounded-xl p-4 text-center"
        >
          <p class="text-xs text-[#6c757d] mb-1">노쇼율 감소</p>
          <p class="text-xl font-bold gradient-primary-text">-50%</p>
        </div>
        <div
          class="bg-white border-2 border-[#1e3a5f] rounded-xl p-4 text-center"
        >
          <p class="text-xs text-[#6c757d] mb-1">가맹점 만족도</p>
          <p class="text-xl font-bold gradient-primary-text">4.8/5.0</p>
        </div>
      </div>

      <!-- Benefits Section -->
      <div class="space-y-4">
        <!-- 메뉴 주방 자동 전송 -->
        <div class="bg-white border-2 border-[#1e3a5f] rounded-xl p-5">
          <h3 class="font-bold text-[#1e3a5f] mb-2">메뉴 주방 자동 전송</h3>
          <p class="text-sm text-[#6c757d] leading-relaxed mb-3">
            예약과 고객이 메뉴를 선택하고, 선주문한 동식에 매뉴 정보가 주방
            태블릿으로 자동 전송됩니다.
            <br />
            시간 준비로 회전율을 30% 높이세요.
          </p>
          <ul class="text-sm text-[#6c757d] space-y-1">
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>실시간 메뉴 데이터 전송</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>식재료 사전 준비 가능</span>
            </li>
          </ul>
        </div>

        <!-- 도착 예정 시각 알림 -->
        <div class="bg-white border-2 border-[#1e3a5f] rounded-xl p-5">
          <h3 class="font-bold text-[#1e3a5f] mb-2">도착 예정 시각 알림</h3>
          <p class="text-sm text-[#6c757d] leading-relaxed mb-3">
            고객 도착 10분 전 자동 알림으로 테이블 준비와 서빙 타이밍을 완벽하게
            맞추세요.
          </p>
          <ul class="text-sm text-[#6c757d] space-y-1">
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>10분 전 자동 알림</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>테이블 세팅 최적화</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>대기 시간 제로화</span>
            </li>
          </ul>
        </div>

        <!-- 고정 매출 확보 -->
        <div class="bg-white border-2 border-[#1e3a5f] rounded-xl p-5">
          <h3 class="font-bold text-[#1e3a5f] mb-2">고정 매출 확보</h3>
          <p class="text-sm text-[#6c757d] leading-relaxed mb-3">
            회식 회식은 일반 방문보다 1인당 객단가가 약 20% 높고, 정기적인
            예약으로 안정적인 수익을 보장합니다.
          </p>
          <ul class="text-sm text-[#6c757d] space-y-1">
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>평균 6~10인 단체 예약</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>일반 대비 1.2배 높은 객단가</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>월 정기 예약 가능</span>
            </li>
          </ul>
        </div>

        <!-- 노쇼 방어 시스템 -->
        <div class="bg-white border-2 border-[#1e3a5f] rounded-xl p-5">
          <h3 class="font-bold text-[#1e3a5f] mb-2">노쇼 방어 시스템</h3>
          <p class="text-sm text-[#6c757d] leading-relaxed mb-3">
            예약 전날, 당일 오전 2차 확인 알림과 예약금 시스템으로 노쇼를 원천
            차단합니다.
          </p>
          <ul class="text-sm text-[#6c757d] space-y-1">
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>2단계 예약 확인 시스템</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>선택적 예약금 설정</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>노쇼율 평균 8% -> 2%</span>
            </li>
          </ul>
        </div>

        <!-- 데이터 기반 운영 -->
        <div class="bg-white border-2 border-[#1e3a5f] rounded-xl p-5">
          <h3 class="font-bold text-[#1e3a5f] mb-2">데이터 기반 운영</h3>
          <p class="text-sm text-[#6c757d] leading-relaxed mb-3">
            예약 패턴, 재방문율, 1인당 객단가 등 실시간 데이터로 매장 운영을
            최적화하세요.
          </p>
          <ul class="text-sm text-[#6c757d] space-y-1">
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>실시간 대시보드 제공</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>고객 행동 패턴 분석</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>맞춤형 마케팅 제안</span>
            </li>
          </ul>
        </div>

        <!-- 자동 정산 시스템 -->
        <div class="bg-white border-2 border-[#1e3a5f] rounded-xl p-5">
          <h3 class="font-bold text-[#1e3a5f] mb-2">자동 정산 시스템</h3>
          <p class="text-sm text-[#6c757d] leading-relaxed mb-3">
            월별 매출과 정산 현황을 한눈에 확인하고, 자동 정산으로 재무 관리
            부담을 줄이세요
          </p>
          <ul class="text-sm text-[#6c757d] space-y-1">
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>주 1회 자동 정산</span>
            </li>
            <li class="flex items-start gap-2">
              <span class="text-[#FF6B4A]">•</span>
              <span>세금계산서 자동 발행</span>
            </li>
          </ul>
        </div>
      </div>

      <!-- Limited Benefits Section -->
      <div
        class="bg-gradient-to-br from-[#FF6B4A] to-[#FFC4B8] rounded-xl p-5 text-white space-y-4"
      >
        <div
          class="flex items-center gap-2 bg-white/20 rounded-full px-4 py-2 w-fit"
        >
          <span class="text-sm font-semibold">✨ 한정 혜택</span>
        </div>

        <div class="space-y-3">
          <h3 class="text-lg font-bold">
            지금 가입하시면 이달의 회식 맛집 노출
          </h3>
          <p class="text-sm leading-relaxed">
            메인 페이지 상단 배너의 '이달의 추천' 섹션에 우선 노출됩니다.
            <br />딱 100% 이상의 예약 증가 효과를 경험하세요
          </p>
          <ul class="text-sm space-y-2">
            <li class="flex items-start gap-2">
              <span>•</span>
              <span>메인 페이지 배너 노출 (30일)</span>
            </li>
            <li class="flex items-start gap-2">
              <span>•</span>
              <span>이달의 추천 맛집 맛집 제공</span>
            </li>
            <li class="flex items-start gap-2">
              <span>•</span>
              <span>검색 결과 상위 노출 보장</span>
            </li>
            <li class="flex items-start gap-2">
              <span>•</span>
              <span>카카오톡 알림톡 무료 500건</span>
            </li>
          </ul>
        </div>

        <RouterLink
          to="/partner/signup"
          class="block w-full bg-white text-[#FF6B4A] py-4 rounded-xl font-bold text-base hover:bg-gray-50 transition-colors text-center"
        >
          지금 바로 시작하기
        </RouterLink>
      </div>

      <!-- Testimonials Section -->
      <div class="space-y-4">
        <h2 class="text-xl font-bold text-[#1e3a5f] text-center">
          이미 많은 점주님들이 경험하고 있습니다.
        </h2>

        <!-- Testimonial 1 -->
        <div
          class="bg-white border-2 border-[#dee2e6] rounded-xl p-5 space-y-3"
        >
          <div class="flex gap-1 text-[#007bff]">
            <span>★</span>
            <span>★</span>
            <span>★</span>
            <span>★</span>
            <span>★</span>
          </div>
          <p class="text-sm text-[#1e3a5f] leading-relaxed">
            "주방에 메뉴가 미리 전송되니까 준비 시간이 확실히 줄었어요. 점심
            피크타임에 회전율이 30% 올랐습니다."
          </p>
          <div class="flex items-center gap-3">
            <div
              class="w-10 h-10 rounded-full bg-[#007bff] flex items-center justify-center"
            >
              <User class="w-6 h-6 text-white" />
            </div>
            <div>
              <p class="text-sm font-semibold text-[#1e3a5f]">김성훈 점주</p>
              <p class="text-xs text-[#6c757d]">서울 강남구 한식당</p>
            </div>
          </div>
        </div>

        <!-- Testimonial 2 -->
        <div
          class="bg-white border-2 border-[#dee2e6] rounded-xl p-5 space-y-3"
        >
          <div class="flex gap-1 text-[#007bff]">
            <span>★</span>
            <span>★</span>
            <span>★</span>
            <span>★</span>
            <span>★</span>
          </div>
          <p class="text-sm text-[#1e3a5f] leading-relaxed">
            "노쇼가 진짜 문제였는데 확인 알림 시스템 및 강력한 예약금
            시스템덕분에 노쇼가 거의 없어졌어요. 매출 손실이 크게 줄었습니다."
          </p>
          <div class="flex items-center gap-3">
            <div
              class="w-10 h-10 rounded-full bg-[#007bff] flex items-center justify-center"
            >
              <User class="w-6 h-6 text-white" />
            </div>
            <div>
              <p class="text-sm font-semibold text-[#1e3a5f]">이지은 대표</p>
              <p class="text-xs text-[#6c757d]">서울 마포구 이탈리안</p>
            </div>
          </div>
        </div>

        <!-- Testimonial 3 -->
        <div
          class="bg-white border-2 border-[#dee2e6] rounded-xl p-5 space-y-3"
        >
          <div class="flex gap-1 text-[#007bff]">
            <span>★</span>
            <span>★</span>
            <span>★</span>
            <span>★</span>
            <span>★</span>
          </div>
          <p class="text-sm text-[#1e3a5f] leading-relaxed">
            "A회사의 회식 예약이 정기적으로 들어와서 매출 예측이 가능해졌어요.
            재료 준비도 효율적으로 할 수 있게 됐습니다."
          </p>
          <div class="flex items-center gap-3">
            <div
              class="w-10 h-10 rounded-full bg-[#007bff] flex items-center justify-center"
            >
              <User class="w-6 h-6 text-white" />
            </div>
            <div>
              <p class="text-sm font-semibold text-[#1e3a5f]">박민수 셰프</p>
              <p class="text-xs text-[#6c757d]">경기도 성남시 일식당</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
.gradient-primary-text {
  background: linear-gradient(135deg, #ff6b4a 0%, #ffaa8d 50%, #ffc4b8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
}
</style>
