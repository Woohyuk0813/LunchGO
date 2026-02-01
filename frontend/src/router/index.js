import { createRouter, createWebHistory } from 'vue-router';
import axios from 'axios';
import HomeView from '../views/HomeView.vue';
import httpRequest from './httpRequest';

import { useRestaurantStore } from '@/stores/restaurant';
import { useAccountStore } from '@/stores/account';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    }
    if (to.hash) {
      return { el: to.hash, behavior: "smooth" };
    }
    return { left: 0, top: 0 };
  },
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/business/dashboard',
      name: 'business-dashboard',
      component: () =>
        import('../views/business/dashboard/BusinessDashboardPage.vue'),
    },
    {
      path: '/business/reservations',
      name: 'business-reservations',
      component: () =>
        import('../views/business/reservations/BusinessReservationsPage.vue'),
    },
    {
      path: '/business/ai-insights',
      name: 'business-ai-insights',
      component: () =>
        import('../views/business/insights/BusinessAiInsightsPage.vue'),
    },
    {
      path: '/business/restaurant-info/resolve',
      name: 'resolve-restaurant-info',
      meta: { requiredAuth: true, roles: ['ROLE_OWNER'] },
      beforeEnter: async (to, from, next) => {
        const accountStore = useAccountStore();
        const member = accountStore.member || JSON.parse(localStorage.getItem('member'));

        if (!member?.id) {
          window.alert("로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.");
          return next({ name: 'login' });
        }
        
        try {
          const response = await httpRequest.get('/api/business/owner/restaurant');
          const restaurantId = response.data?.restaurantId;

          if (restaurantId) {
            // 현재 사업자가 등록한 식당이 존재 -> 조회 페이지 이동
            next({ name: 'business-restaurant-info', params: { id: restaurantId } });
          } else {
            // 현재 사업자가 등록한 식당이 없음 -> 등록 페이지 이동
            next({ name: 'business-restaurant-info-add' });
          }
        } catch (error) {
          if (error.response && error.response.status === 404) {
            next({ name: 'business-restaurant-info-add' });
          } else {
            console.error('식당 정보 확인 중 오류:', error);
            window.alert('식당 정보를 확인하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            if (from.path !== '/' && from.name) {
              next(false);
            } else {
              next({ name: 'business-dashboard' });
            }
          }
        }
      }
    },
    {
      path: '/business/restaurant-info/add',
      name: 'business-restaurant-info-add',
      meta: { requiredAuth: true, roles: ['ROLE_OWNER'] },
      component: () =>
        import(
          '../views/business/restaurant-info/edit/RestaurantInfoEditPage.vue'
        ),
    },
    {
      path: '/business/restaurant-info/edit/:id',
      name: 'business-restaurant-info-edit',
      meta: { requiredAuth: true, roles: ['ROLE_OWNER'] },
      component: () =>
        import(
          '../views/business/restaurant-info/edit/RestaurantInfoEditPage.vue'
        ),
    },
    {
      path: '/business/restaurant-info/menu/add',
      name: 'business-restaurant-menu-add',
      meta: { requiredAuth: true, roles: ['ROLE_OWNER'] },
      component: () =>
        import('../views/business/restaurant-info/menu/MenuEditPage.vue'),
    },
    {
      path: '/business/restaurant-info/menu/edit/:id',
      name: 'business-restaurant-menu-edit',
      meta: { requiredAuth: true, roles: ['ROLE_OWNER'] },
      component: () =>
        import('../views/business/restaurant-info/menu/MenuEditPage.vue'),
      props: true,
    },
    {
      path: '/business/restaurant-info/:id',
      name: 'business-restaurant-info',
      meta: { requiredAuth: true, roles: ['ROLE_OWNER'] },
      component: () =>
        import('../views/business/restaurant-info/RestaurantInfoPage.vue'),
      props: true,
    },
    {
      path: '/business/restaurant-info/:id/menus',
      name: 'business-restaurant-menus',
      meta: { requiredAuth: true, roles: ['ROLE_OWNER'] },
      component: () =>
        import('../views/business/restaurant-info/menu/MenusInfoPage.vue'),
      props: true,
    },
    {
      path: '/business/staff',
      name: 'business-staff',
      component: () => import('../views/business/staff/BusinessStaffPage.vue'),
      meta: { requiredAuth: true, roles: ['ROLE_OWNER']},
    },
    {
      path: '/business/reviews',
      name: 'business-reviews',
      component: () =>
        import('../views/business/reviews/BusinessReviewsPage.vue'),
    },
    {
      path: '/business/promotion',
      name: 'business-promotion',
      component: () =>
        import('../views/business/promotion/BusinessPromotionPage.vue'),
      meta: { requiredAuth: true, roles: ['ROLE_OWNER']},
    },
    {
      path: '/admin/dashboard',
      name: 'admin-dashboard',
      component: () =>
        import('../views/admin/dashboard/AdminDashboardPage.vue'),
    },
    {
      path: '/admin/reservations',
      name: 'admin-reservations',
      component: () =>
        import('../views/admin/reservations/AdminReservationsPage.vue'),
    },
    {
      path: '/admin/franchises',
      name: 'admin-franchises',
      component: () =>
        import('../views/admin/franchises/AdminFranchisesPage.vue'),
    },
    {
      path: '/admin/reviews',
      name: 'admin-reviews',
      component: () => import('../views/admin/reviews/AdminReviewsPage.vue'),
    },
    {
      path: '/admin/members',
      name: 'admin-members',
      component: () => import('../views/admin/members/AdminMembersPage.vue'),
    },
    {
      path: '/admin/finance',
      name: 'admin-finance',
      component: () => import('../views/admin/finance/AdminFinancePage.vue'),
    },
    {
      path: '/admin/owners',
      name: 'admin-owners',
      component: () => import('../views/admin/owners/AdminOwnerApprovalPage.vue'),
      meta: { requiredAuth: true, roles: ['ROLE_ADMIN']},
    },
    {
      path: '/intro',
      name: 'intro',
      component: () => import('../views/intro/ServiceIntroPage.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/login/LoginPage.vue'),
    },
    {
      path: '/admin/login',
      name: 'admin-login',
      component: () => import('../views/login/AdminLoginPage.vue'),
    },
    {
      path: '/my-reservations',
      name: 'my-reservations',
      component: () =>
        import('../views/my-reservations/MyReservationsPage.vue'),
    },
    {
      path: '/mypage/reviews',
      name: 'mypage-reviews',
      component: () => import('../views/mypage/reviews/ReviewsPage.vue'),
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/mypage/UserMyPage.vue'),
      meta: { requiredAuth: true, roles: ['ROLE_USER']},
    },
    {
      path: '/chatbot',
      name: 'chatbot',
      component: () => import('../views/chatbot/ChatBotPage.vue'),
      meta: { requiredAuth: true, roles: ['ROLE_USER'] },
    },
    {
      path: '/business/mypage',
      name: 'business-mypage',
      component: () => import('../views/mypage/OwnerMyPage.vue'),
      meta: { requiredAuth: true , roles: ['ROLE_OWNER']},
    },
    {
      path: '/partner',
      name: 'partner',
      component: () => import('../views/partner/PartnerPage.vue'),
    },
    {
      path: '/restaurant/:id/booking',
      name: 'restaurant-booking',
      component: () =>
        import('../views/restaurant/id/booking/RestaurantBookingPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/confirmation',
      name: 'restaurant-confirmation',
      component: () =>
        import(
          '../views/restaurant/id/confirmation/RestaurantConfirmationPage.vue'
        ),
      props: true,
    },
    {
      path: '/restaurant/:id/menu',
      name: 'restaurant-menu',
      component: () =>
        import('../views/restaurant/id/menu/MenuSelectionPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/menus',
      name: 'restaurant-menus',
      component: () =>
        import('../views/restaurant/id/menus/RestaurantMenusPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id',
      name: 'restaurant-detail',
      component: () =>
        import('../views/restaurant/id/RestaurantDetailPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/payment',
      name: 'restaurant-payment',
      component: () =>
        import('../views/restaurant/id/payment/RestaurantPaymentPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/summary',
      name: 'restaurant-summary',
      component: () =>
        import('../views/restaurant/id/summary/RestaurantSummaryPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/reviews/write',
      name: 'write-review',
      component: () =>
        import('../views/restaurant/id/reviews/WriteReviewPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/reviews/:reviewId/edit',
      name: 'edit-review',
      component: () =>
        import('../views/restaurant/id/reviews/WriteReviewPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/reviews/:reviewId',
      name: 'review-detail',
      component: () =>
        import('../views/restaurant/id/reviews/reviewId/ReviewDetailPage.vue'),
      props: true,
    },
    {
      path: '/restaurant/:id/reviews',
      name: 'restaurant-reviews',
      component: () =>
        import('../views/restaurant/id/reviews/RestaurantReviewsPage.vue'),
      props: true,
    },
    {
      path: '/signup',
      name: 'signup',
      component: () => import('../views/signup/SignupChoicePage.vue'),
    },
    {
      path: '/signup/user',
      name: 'signup-user',
      component: () => import('../views/signup/UserSignupPage.vue'),
    },
    {
      path: '/signup/owner',
      name: 'signup-owner',
      component: () => import('../views/signup/OwnerSignupPage.vue'),
    },
    //방문 선택 승인 페이지
    {
      path: '/business/notifications',
      name: 'business-notifications',
      component: () => import('../views/business/notifications/BusinessNotificationsPage.vue'),
    },
    //예약 상세 보기
    {
      path: '/business/reservations/:id',
      name: 'reservation-detail',
      component: () => import('@/views/business/reservations/ReservationDetailPage.vue'),
      props: true,
      alias: '/reservations/:id',
    },
    //예약 취소 페이지
    {
      path: "/my-reservations/:id/cancel",
      name: "reservation-cancel",
      component: () => import("@/views/my-reservations/ReservationCancelView.vue"),
    },
    {
      path: '/staff/list',
      name : 'staff-list',
      component: () => import('@/views/staff/StaffListPage.vue'),
      meta: {requiredAuth: true, roles: ['ROLE_STAFF']}
    },
    // Wildcard route for 404 - make sure this is the last route
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      redirect: '/', // Redirect to home for any unmatched routes
    },
  ],
});

router.beforeEach(async (to, from, next) => {
  const accountStore = useAccountStore();
  const requiresAuth = to.matched.some((r) => r.meta?.requiredAuth);
  const requiredRoles = to.matched.flatMap((r) => r.meta?.roles || []);

  if (!requiresAuth) return next(); //비회원인 경우

  const accessToken = localStorage.getItem('accessToken');
  const memberRaw = localStorage.getItem('member');
  let member = null;

  if (memberRaw) {
    try {
      member = JSON.parse(memberRaw);
    } catch (error) {
      member = null;
    }
  }

  const isLoggedIn = Boolean(accessToken);
  const currentRole = member?.role;

  if (!isLoggedIn) {
    window.alert("로그인이 필요합니다.");
    return next({
      name: 'login',
      query: { next: to.fullPath },
    });
  }

  if (requiredRoles.length > 0) {
    const hasRole = requiredRoles.includes(currentRole);
    if (!hasRole) {
      window.alert("권한이 없습니다");
      return next({ name: 'home' });
    }
  }

  try {
    const res = await axios.get('/api/auth/check', {
      headers: { Authorization: `Bearer ${accessToken}` },
    });

    if (res.status === 200 && res.data === true) {
      const storedMember = localStorage.getItem('member');
      if (storedMember) {
        accountStore.setLoggedIn(true, JSON.parse(storedMember));
        accountStore.setAccessToken(accessToken);
      }

      return next();
    }

    window.alert('로그인 세션이 만료되었습니다.');
    accountStore.clearAccount();
    return next('/login');

  } catch (error) {
    console.warn(error);
    accountStore.clearAccount();
    localStorage.removeItem('accessToken');
    localStorage.removeItem('member');
    window.alert('로그인 세션이 만료되었습니다.');
    return next('/login');
  }
});

router.beforeEach((to, from) => {
  
  const store = useRestaurantStore();

  const workflowRoutes = [
    'business-restaurant-info-add',
    'business-restaurant-info-edit',
    'business-restaurant-menu-add',
    'business-restaurant-menu-edit',
  ];

  const isFromWorkflow = workflowRoutes.includes(from.name);
  const isToWorkflow = workflowRoutes.includes(to.name);

  
  if (isFromWorkflow && !isToWorkflow) {
    store.clearRestaurant();
    return;
  }

  
  if (to.name === 'business-restaurant-info-add') {
    
    if (!store.restaurantInfo || store.restaurantInfo.restaurantId) {
      store.initializeNewRestaurant();
    }
  }
});

export default router;

