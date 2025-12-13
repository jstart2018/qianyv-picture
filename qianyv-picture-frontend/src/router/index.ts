import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/pictures',
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
      path: '/space',
      name: 'space',
      component: () => import('../views/SpaceView/index.vue'),
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('../views/AiChatView.vue'),
    },
    {
      path: '/chat/:conversationId',
      name: 'chatWithId',
      component: () => import('../views/AiChatView.vue'),
    },
    {
      path: '/user/:id',
      name: 'userDetail',
      component: () => import('../views/UserDetailView.vue'),
    },
    {
      path: '/pictures/horizontal/:id',
      name: 'horizontalPictureDetail',
      component: () => import('../views/HorizontalPictureDetailView.vue'),
    },
    {
      path: '/pictures/vertical/:id',
      name: 'verticalPictureDetail',
      component: () => import('../views/VerticalPictureDetailView.vue'),
    },
  ],
})

export default router
