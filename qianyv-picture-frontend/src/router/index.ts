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
    },
    {
      path: '/space',
      name: 'space',
      component: () => import('../views/SpaceView.vue'),
    },
    {
      path: '/user/:id',
      name: 'userDetail',
      component: () => import('../views/UserDetailView.vue'),
    },
    {
      path: '/picture/:id',
      name: 'pictureDetail',
      component: () => import('../views/PictureDetailView.vue'),
    },
  ],
})

export default router
