import { ref } from 'vue'
import { defineStore } from 'pinia'
import * as userApi from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  const user = ref<API.UserVO | null>(null)

  async function fetchUser() {
    try {
      const res = await userApi.getLoginUser()
      // 调试日志已移除

      // 检查返回code是否为0（成功）
      if (res.data?.code === 0 && res.data?.data) {
        user.value = res.data.data
        return user.value
      } else {
        console.warn('获取用户信息失败:', res.data?.message)
        user.value = null
        return null
      }
    } catch (e) {
      console.error('获取用户信息异常:', e)
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
