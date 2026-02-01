<script setup>
import { ref, computed } from "vue";
import { RouterLink } from "vue-router";
import { marked } from "marked";

import PRIVACY_POLICY_TEXT from "@/content/privacyPolicy.md?raw";
import LOCATION_POLICY_TEXT from "@/content/locationServicePolicy.md?raw";
import SERVICE_TERMS_TEXT from "@/content/serviceTerms.md?raw";

import PolicyModal from "@/components/ui/PolicyModal.vue";

const activeModalKey = ref(null);

const privacyHtml = computed(() =>
  marked.parse(PRIVACY_POLICY_TEXT, { breaks: true })
);
const locationPolicyHtml = computed(() =>
  marked.parse(LOCATION_POLICY_TEXT, { breaks: true })
);
const serviceTermsHtml = computed(() =>
  marked.parse(SERVICE_TERMS_TEXT, { breaks: true })
);

const activeModal = computed(() => {
  if (!activeModalKey.value) return null;

  const map = {
    serviceTerms: { title: "서비스 이용약관", html: serviceTermsHtml.value },
    privacy: { title: "개인정보 처리방침", html: privacyHtml.value },
    locationPolicy: {
      title: "위치정보 이용약관",
      html: locationPolicyHtml.value,
    },
  };

  return map[activeModalKey.value] || null;
});

const openPrivacy = () => {
  activeModalKey.value = "privacy";
};

const openLocationPolicy = () => {
  activeModalKey.value = "locationPolicy";
};

const openServiceTerms = () => {
  activeModalKey.value = "serviceTerms";
};

const closeModal = () => {
  activeModalKey.value = null;
};
</script>

<template>
  <footer class="bg-white border-t border-[#e9ecef] px-4 py-6 mt-8">
    <div class="text-xs text-[#6c757d] space-y-1 leading-relaxed">
      <p class="font-semibold text-[#1e3a5f]">(주) 런치고</p>
      <p>주소 : 경기 성남시 분당구 판교동 621 1501호</p>

      <p>
        <RouterLink to="/intro" class="hover:underline">서비스 소개</RouterLink>
        |
        <button type="button" class="hover:underline" @click="openServiceTerms">
          서비스 이용약관
        </button>
        |
        <button
          type="button"
          class="hover:underline"
          @click="openLocationPolicy"
        >
          위치정보 이용약관
        </button>
        |
        <RouterLink to="/partner" class="text-[#FF6B4A] hover:underline">
          파트너 문의
        </RouterLink>
      </p>

      <p>
        <button type="button" class="hover:underline" @click="openPrivacy">
          개인정보 처리방침
        </button>
      </p>
    </div>
  </footer>

  <PolicyModal
    v-if="activeModal"
    :title="activeModal.title"
    :content-html="activeModal.html"
    @close="closeModal"
  />
</template>

<style scoped></style>
