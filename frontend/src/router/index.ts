import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { isAuthenticated } from '../auth/auth';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: () => (isAuthenticated() ? '/dashboard' : '/login'),
  },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { guestOnly: true } },
  { path: '/dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue'), meta: { requiresAuth: true } },
  { path: '/chat', name: 'chat', component: () => import('../views/ChatView.vue'), meta: { requiresAuth: true } },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const authenticated = isAuthenticated();

  if (to.meta.requiresAuth && !authenticated) {
    return '/login';
  }

  if (to.meta.guestOnly && authenticated) {
    return '/dashboard';
  }

  return true;
});
