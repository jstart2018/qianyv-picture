import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import * as userApi from '@/api/userController'

export const useUserStore = defineStore('user', () => {
  const user = ref<API.UserInfoVO | null>(null)

  /** 是否为管理员（Boss=0 或 管理员=1） */
  const isAdmin = computed(() => {
    return user.value?.role !== undefined && user.value.role <= 1
  })

  /** 是否为 Boss（role=0） */
  const isBoss = computed(() => {
    return user.value?.role === 0
  })

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

  return { user, isAdmin, isBoss, fetchUser, logout }
})
