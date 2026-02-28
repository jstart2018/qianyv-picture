import { createRouter, createWebHistory } from 'vue-router'
import UserLayout from '@/layouts/UserLayout.vue'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ========== 用户端路由（使用 UserLayout 布局） ==========
    {
      path: '/',
      component: UserLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('../views/HomeView.vue'),
        },
        {
          path: 'login',
          name: 'login',
          component: () => import('../views/LoginView.vue'),
        },
        {
          path: 'pictures',
          name: 'pictures',
          component: () => import('../views/PicturesView.vue'),
          redirect: '/pictures/horizontal',
          children: [
            {
              path: 'horizontal',
              name: 'picturesHorizontal',
              component: () => import('../views/HorizontalPicturesView.vue'),
            },
            {
              path: 'vertical',
              name: 'picturesVertical',
              component: () => import('../views/VerticalPicturesView.vue'),
            },
          ],
        },
        {
          path: 'space',
          name: 'space',
          component: () => import('../views/SpaceView/index.vue'),
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('../views/AiChatView.vue'),
        },
        {
          path: 'chat/:conversationId',
          name: 'chatWithId',
          component: () => import('../views/AiChatView.vue'),
        },
        {
          path: 'user/:id',
          name: 'userDetail',
          component: () => import('../views/UserDetailView.vue'),
        },
        {
          path: 'user/edit/:id',
          name: 'userEdit',
          component: () => import('../views/UserEditView.vue'),
        },
        {
          path: 'blog/:id',
          name: 'blogDetail',
          component: () => import('../views/BlogDetailView.vue'),
        },
        {
          path: 'pictures/horizontal/:id',
          name: 'horizontalPictureDetail',
          component: () => import('../views/HorizontalPictureDetailView.vue'),
        },
        {
          path: 'pictures/vertical/:id',
          name: 'verticalPictureDetail',
          component: () => import('../views/VerticalPictureDetailView.vue'),
        },
      ],
    },

    // ========== 管理端路由（使用 AdminLayout 布局） ==========
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        {
          path: '',
          name: 'adminDashboard',
          component: () => import('../views/admin/AdminDashboard.vue'),
        },
        {
          path: 'users',
          name: 'adminUsers',
          component: () => import('../views/admin/UserManage.vue'),
        },
        {
          path: 'blogs',
          name: 'adminBlogs',
          component: () => import('../views/admin/BlogManage.vue'),
        },
        {
          path: 'pictures',
          name: 'adminPictures',
          component: () => import('../views/admin/PictureManage.vue'),
        },
        {
          path: 'spaces',
          name: 'adminSpaces',
          component: () => import('../views/admin/SpaceManage.vue'),
        },
      ],
    },
  ],
})

// ========== 路由守卫：管理端权限校验 ==========
router.beforeEach(async (to, _from, next) => {
  // 只拦截需要管理员权限的路由
  if (!to.matched.some((r) => r.meta.requiresAdmin)) {
    return next()
  }

  const userStore = useUserStore()

  // 如果还没有用户信息，先尝试获取
  if (!userStore.user) {
    try {
      await userStore.fetchUser()
    } catch {
      // 获取失败，跳转登录
      return next({ name: 'login', query: { redirect: to.fullPath } })
    }
  }

  // 未登录 → 去登录页
  if (!userStore.user) {
    return next({ name: 'login', query: { redirect: to.fullPath } })
  }

  // 非管理员（role === 2 普通用户）→ 跳回首页
  if (!userStore.isAdmin) {
    return next({ name: 'home' })
  }

  next()
})

export default router
