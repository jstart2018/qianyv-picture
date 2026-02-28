<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import { useUserStore } from './stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 全局：尝试获取当前登录用户（如果有）
  userStore.fetchUser().catch(() => {})
})
</script>

<template>
  <RouterView />
</template>

<style>
/* App 层只保留最基础的全局重置，具体布局样式由各 Layout 组件负责 */
</style>
