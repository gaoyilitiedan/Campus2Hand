import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { authState } from '@/store/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '商品市场', icon: '📦' }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/ProductDetail.vue'),
    meta: { title: '商品详情', hidden: true }
  },
  {
    path: '/publish',
    name: 'PublishProduct',
    component: () => import('@/views/PublishProduct.vue'),
    meta: { title: '发布商品', icon: '📝' }
  },
  {
    path: '/my-products',
    name: 'MyProducts',
    component: () => import('@/views/MyProducts.vue'),
    meta: { title: '我的商品', icon: '📋' }
  },
  {
    path: '/my-orders',
    name: 'MyOrders',
    component: () => import('@/views/MyOrders.vue'),
    meta: { title: '我的订单', icon: '🛒' }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/OrderDetail.vue'),
    meta: { title: '订单详情', hidden: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('@/views/Messages.vue'),
    meta: { title: '聊天消息', icon: '💬' }
  },
  {
    path: '/my-reviews',
    name: 'MyReviews',
    component: () => import('@/views/MyReviews.vue'),
    meta: { title: '评价管理', icon: '⭐' }
  },
  {
    path: '/my-disputes',
    name: 'MyDisputes',
    component: () => import('@/views/MyDisputes.vue'),
    meta: { title: '投诉工单', icon: '⚠️' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', icon: '👤' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  if (to.path !== '/login' && !authState.isLoggedIn) {
    next('/login')
  } else if (to.path === '/login' && authState.isLoggedIn) {
    next('/')
  } else {
    next()
  }
})

export default router