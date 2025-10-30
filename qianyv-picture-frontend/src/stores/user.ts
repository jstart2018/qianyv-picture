import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as userApi from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  const user = ref<API.UserVO | null>(null)
  async function fetchUser() {
    try {
      const res = await userApi.getLoginUser()
      user.value = res.data?.data || null
      return user.value
    } catch (e) {
      user.value = null
      throw e
    }
  }

  async function logout() {
    try {
      await userApi.logout()
    } finally {
      user.value = null
    }
  }

  return { user, fetchUser, logout }
})
