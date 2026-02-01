<script setup>
import { ref, computed, onMounted } from 'vue';
import { RouterLink, useRoute, useRouter } from 'vue-router'; // Import useRoute to get dynamic params
import { ArrowLeft, Plus, Minus, ShoppingCart } from 'lucide-vue-next';
import Button from '@/components/ui/Button.vue';
import Card from '@/components/ui/Card.vue';
import WaitingModal from '@/components/ui/WaitingModal.vue';
import httpRequest from '@/router/httpRequest';
import { useAccountStore } from '@/stores/account';
import { useReservationQueue } from '@/composables/useReservationQueue';

const route = useRoute();
const router = useRouter();
const restaurantId = route.params.id || '1'; // Default to '1' if id is not available
const accountStore = useAccountStore();

const cart = ref([]); // Use ref for the cart array

const menuCategories = ref([]);

const loadMenus = async () => {
  try {
    const res = await httpRequest.get(`/api/restaurants/${restaurantId}/menus`);
    const menus = res?.data ?? [];

    const labelByCode = {
      MAIN: '메인 메뉴',
      SUB: '사이드 메뉴',
      OTHER: '음료/기타',
    };

    const grouped = {};
    menus.forEach((m) => {
      const code = m?.category?.code || m?.category || 'OTHER';
      const label = labelByCode[code] || '음료/기타';
      if (!grouped[label]) grouped[label] = [];
      grouped[label].push({
        id: m.id,
        name: m.name,
        description: m.description,
        price: m.price,
      });
    });

    const orderedLabels = ['메인 메뉴', '사이드 메뉴', '음료/기타'];
    menuCategories.value = orderedLabels
        .filter((label) => Array.isArray(grouped[label]) && grouped[label].length)
        .map((label) => ({ name: label, items: grouped[label] }));
  } catch (e) {
    console.error('메뉴 조회 실패:', e);
    menuCategories.value = [];
  }
};

onMounted(() => {
  loadMenus();
});

// "35,000원" -> 35000
const parsePrice = (price) => Number(String(price).replace(/[^\d]/g, ''));

// flat으로 펴서 지금 UI 구조 유지
const menuItems = computed(() =>
  menuCategories.value.flatMap((cat) =>
    cat.items.map((item) => ({
      id: item.id,
      name: item.name,
      description: item.description,
      category: cat.name,
      price: parsePrice(item.price),
    })),
  ),
);

const categories = computed(() => menuCategories.value.map((c) => c.name));

const addToCart = (item) => {
  const existingItem = cart.value.find((cartItem) => cartItem.id === item.id);
  if (existingItem) {
    existingItem.quantity++;
  } else {
    cart.value.push({ ...item, quantity: 1 });
  }
};

const removeFromCart = (itemId) => {
  const existingItem = cart.value.find((cartItem) => cartItem.id === itemId);
  if (existingItem && existingItem.quantity > 1) {
    existingItem.quantity--;
  } else {
    cart.value = cart.value.filter((cartItem) => cartItem.id !== itemId);
  }
};

const getItemQuantity = (itemId) => {
  return cart.value.find((item) => item.id === itemId)?.quantity || 0;
};

const totalAmount = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0);
});
const totalItems = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.quantity, 0);
});

// 예약 페이지에서 넘어온 인원수 (없으면 1)
const partySize = computed(() => {
  const q = Number(route.query.partySize);
  return Number.isFinite(q) && q > 0 ? q : 1;
});

const isSubmitting = ref(false);
const submitErrorMessage = ref('');

const { 
  isWaiting, 
  modalType, 
  modalMessage, 
  queueErrorMessage, 
  currentWaitingCount,
  formattedWaitTime,
  processQueue, 
  handleQueueModalClose 
} = useReservationQueue();

import { watch } from 'vue';
watch(queueErrorMessage, (newVal) => {
  if (newVal) submitErrorMessage.value = newVal;
});

const resolveUserId = () => {
  const storeMemberId = accountStore.member?.id;
  if (storeMemberId) return storeMemberId;
  const rawMember = localStorage.getItem('member');
  if (!rawMember) return null;
  try {
    return JSON.parse(rawMember)?.id ?? null;
  } catch (e) {
    return null;
  }
};

const selectedSlotDate = computed(() => {
  const idx = Number(route.query.dateIndex);
  if (!Number.isFinite(idx)) return null;
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() + idx);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
});

const selectedSlotTime = computed(() => {
  const t = String(route.query.time || '');
  return t ? (t.length === 5 ? `${t}:00` : t) : null;
});

// 1인당 최소 1개 => 총 선택 개수 >= 인원수
const canProceed = computed(() => totalItems.value >= partySize.value);

const createReservation = async () => {
  if (!selectedSlotDate.value || !selectedSlotTime.value) {
    throw new Error('예약 정보가 없습니다. 다시 예약 과정을 진행해 주세요.');
  }
  const userId = resolveUserId();
  if (!userId) {
    throw new Error('로그인이 필요합니다.');
  }

  const payload = {
    userId,
    restaurantId: Number(restaurantId),
    slotDate: selectedSlotDate.value,
    slotTime: selectedSlotTime.value,
    partySize: Number(partySize.value),
    reservationType: 'PREORDER_PREPAY',
    requestMessage: String(route.query.requestNote || '').trim() || null,
    totalAmount: totalAmount.value,
    menuItems: cart.value.map((i) => ({
      menuId: Number(i.id),
      quantity: Number(i.quantity || 0),
    })),
  };

  const res = await httpRequest.post('/api/reservations', payload);
  return res.data;
};

const handleProceed = async () => {
  if (!canProceed.value || isSubmitting.value) return;
  submitErrorMessage.value = '';
  isSubmitting.value = true;

  await processQueue(
    createReservation,
    (created) => {
      const reservationId = created?.reservationId;
      if (!reservationId) throw new Error('예약 생성 실패');

      router.push({
        path: `/restaurant/${restaurantId}/payment`,
        query: {
          type: 'full',
          totalAmount: totalAmount.value,
          partySize: route.query.partySize,
          requestNote: route.query.requestNote,
          dateIndex: route.query.dateIndex,
          time: route.query.time,
          reservationId: String(reservationId),
        },
      });
    },
    isSubmitting
  );
};
</script>

<template>
  <div class="min-h-screen bg-[#f8f9fa]">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white border-b border-[#e9ecef]">
      <div class="max-w-[500px] mx-auto px-4 h-14 flex items-center">
        <RouterLink :to="`/restaurant/${restaurantId}/booking`" class="mr-3">
          <ArrowLeft class="w-6 h-6 text-[#1e3a5f]" />
        </RouterLink>
        <h1 class="font-semibold text-[#1e3a5f] text-base">메뉴 선택</h1>
      </div>
    </header>

    <main class="max-w-[500px] mx-auto pb-32">
      <div class="bg-white px-4 py-3 border-b border-[#e9ecef]">
        <p class="text-sm text-[#495057] leading-relaxed">
          회식에 필요한 메뉴를 선택해주세요. 1인당 최소 1개 이상 선택해야 합니다.
        </p>
      </div>

      <div v-for="category in categories" :key="category" class="px-4 py-5">
        <h3 class="text-base font-semibold text-[#1e3a5f] mb-4">{{ category }}</h3>
        <div class="space-y-3">
          <Card
            v-for="item in menuItems.filter((mItem) => mItem.category === category)"
            :key="item.id"
            class="p-4 border-[#e9ecef] rounded-xl bg-white shadow-card hover:shadow-md transition-shadow"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="flex-1">
                <h4 class="font-semibold text-[#1e3a5f] mb-1">{{ item.name }}</h4>
                <p v-if="item.description" class="text-xs text-[#6c757d] mb-2 leading-relaxed">
                  {{ item.description }}
                </p>
                <p class="text-base font-semibold text-[#1e3a5f]">{{ item.price.toLocaleString() }}원</p>
              </div>

              <div class="flex items-center gap-2">
                <template v-if="getItemQuantity(item.id) > 0">
                  <button
                    @click="removeFromCart(item.id)"
                    class="w-8 h-8 rounded-full border-2 border-[#ff6b4a] bg-white text-[#ff6b4a] flex items-center justify-center hover:bg-[#fff5f3] transition-colors"
                  >
                    <Minus class="w-4 h-4" />
                  </button>
                  <span class="w-8 text-center font-semibold text-[#1e3a5f]">{{ getItemQuantity(item.id) }}</span>
                  <button
                    @click="addToCart(item)"
                    class="w-8 h-8 rounded-full gradient-primary text-white flex items-center justify-center shadow-button-hover hover:shadow-button-pressed transition-shadow"
                  >
                    <Plus class="w-4 h-4" />
                  </button>
                </template>
                <template v-else>
                  <button
                    @click="addToCart(item)"
                    class="w-8 h-8 rounded-full border-2 border-[#dee2e6] bg-white text-[#6c757d] flex items-center justify-center hover:border-[#ff6b4a] hover:text-[#ff6b4a] transition-colors"
                  >
                    <Plus class="w-4 h-4" />
                  </button>
                </template>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </main>

    <!-- Fixed Bottom Cart Summary -->
    <div v-if="cart.length > 0" class="fixed bottom-0 left-0 right-0 bg-white border-t border-[#e9ecef] z-50 shadow-lg">
      <div class="max-w-[500px] mx-auto px-4 py-3">
        <div class="flex items-center justify-between mb-3">
          <div class="flex items-center gap-2">
            <ShoppingCart class="w-5 h-5 text-[#ff6b4a]" />
            <span class="font-semibold text-[#1e3a5f]">총 {{ totalItems }}개 선택</span>
          </div>
          <p class="text-lg font-bold text-[#1e3a5f]">{{ totalAmount.toLocaleString() }}원</p>
        </div>
        <Button
          v-if="canProceed"
          :disabled="isSubmitting"
          @click="handleProceed"
          class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl shadow-button-hover hover:shadow-button-pressed"
        >
          {{ totalAmount.toLocaleString() }}원 결제하기
        </Button>

        <Button
          v-else
          disabled
          class="w-full h-12 gradient-primary text-white font-semibold text-base rounded-xl opacity-50 cursor-not-allowed"
        >
          1인당 최소 1개 선택 ({{ totalItems }}/{{ partySize }})
        </Button>
      </div>
    </div>

    <WaitingModal 
      :isOpen="isWaiting" 
      :type="modalType"
      :message="modalMessage || undefined"
      :waitingCount="currentWaitingCount"
      :estimatedTime="formattedWaitTime"
      @close="() => {
        isSubmitting = false;
        handleQueueModalClose();
      }"
    />
  </div>
</template>

<style scoped>
/* No specific styles needed here as Tailwind handles most of it. */
</style>
