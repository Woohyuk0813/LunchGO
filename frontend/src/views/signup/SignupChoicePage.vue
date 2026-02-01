<script setup>
import { RouterLink } from 'vue-router';
import { ArrowLeft, User, Building2, ChevronRight } from 'lucide-vue-next';

// navigateTo 함수와 useRouter는 이제 필요 없으므로 제거했습니다.
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa] flex flex-col">
    <header class="bg-white border-b border-[#e9ecef]">
      <div
        class="max-w-[1000px] mx-auto px-4 h-14 flex items-center justify-between"
      >
        <RouterLink
          to="/"
          class="flex items-center gap-2 text-[#495057] hover:text-[#1e3a5f] transition-colors"
        >
          <ArrowLeft class="w-5 h-5" />
          <span class="text-sm font-medium">메인으로</span>
        </RouterLink>

        <div class="flex items-center gap-4 text-sm font-medium">
          <RouterLink to="/login" class="text-[#495057] hover:text-[#1e3a5f]">
            로그인
          </RouterLink>
        </div>
      </div>
    </header>

    <main class="flex-1 flex flex-col justify-center items-center px-4 py-10">
      <div class="text-center mb-10">
        <h1 class="text-3xl font-bold text-[#1e3a5f] mb-3">회원가입</h1>
        <div class="w-16 h-1 bg-[#ff6b4a] mx-auto rounded-full mb-4"></div>
        <p class="text-[#6c757d]">원하시는 회원가입 유형을 선택해주세요.</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 w-full max-w-[800px]">
        <RouterLink
          to="/signup/user"
          class="block group relative bg-white p-8 rounded-2xl shadow-card border border-[#e9ecef] text-left hover:border-[#ff6b4a] hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1"
        >
          <div class="flex justify-between items-start mb-6">
            <div
              class="w-14 h-14 rounded-xl bg-[#fff5f2] flex items-center justify-center text-[#ff6b4a] group-hover:scale-110 transition-transform"
            >
              <User class="w-7 h-7" />
            </div>
            <ChevronRight
              class="w-5 h-5 text-[#adb5bd] group-hover:text-[#ff6b4a] transition-colors"
            />
          </div>

          <h2
            class="text-xl font-bold text-[#1e3a5f] mb-2 group-hover:text-[#ff6b4a] transition-colors"
          >
            사용자 회원가입
          </h2>
          <p class="text-sm text-[#6c757d] leading-relaxed">
            직장인 점심, 회식 장소 예약 등<br />
            런치고의 다양한 서비스를 이용해보세요.
          </p>
        </RouterLink>

        <RouterLink
          to="/signup/owner"
          class="block group relative bg-white p-8 rounded-2xl shadow-card border border-[#e9ecef] text-left hover:border-[#1e3a5f] hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1"
        >
          <div class="flex justify-between items-start mb-6">
            <div
              class="w-14 h-14 rounded-xl bg-[#eef2f6] flex items-center justify-center text-[#1e3a5f] group-hover:scale-110 transition-transform"
            >
              <Building2 class="w-7 h-7" />
            </div>
            <ChevronRight
              class="w-5 h-5 text-[#adb5bd] group-hover:text-[#1e3a5f] transition-colors"
            />
          </div>

          <h2 class="text-xl font-bold text-[#1e3a5f] mb-2">사업자 회원가입</h2>
          <p class="text-sm text-[#6c757d] leading-relaxed">
            매장을 등록하고 예약 고객을 관리하며<br />
            매출을 효율적으로 관리해보세요.
          </p>
        </RouterLink>
      </div>

      <div class="mt-12 text-center">
        <p class="text-sm text-[#6c757d]">
          이미 계정이 있으신가요?
          <RouterLink
            to="/login"
            class="text-[#ff6b4a] font-semibold hover:underline ml-1"
          >
            로그인하기
          </RouterLink>
        </p>
      </div>
    </main>
  </div>
</template>

<style scoped>
.shadow-card {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05),
    0 2px 4px -1px rgba(0, 0, 0, 0.03);
}
</style>
