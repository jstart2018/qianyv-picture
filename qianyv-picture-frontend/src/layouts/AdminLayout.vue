<script setup lang="ts">
import { ref, computed, h } from 'vue'
import { useRouter, RouterView } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  UserOutlined,
  FileTextOutlined,
  PictureOutlined,
  AppstoreOutlined,
  DashboardOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ArrowLeftOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

// 当前选中的菜单项
const selectedKeys = computed(() => {
  const path = router.currentRoute.value.path
  if (path === '/admin' || path === '/admin/') return ['dashboard']
  if (path.startsWith('/admin/users')) return ['users']
  if (path.startsWith('/admin/blogs')) return ['blogs']
  if (path.startsWith('/admin/pictures')) return ['pictures']
  if (path.startsWith('/admin/spaces')) return ['spaces']
  return ['dashboard']
})

// 菜单项配置
const menuItems = [
  {
    key: 'dashboard',
    icon: () => h(DashboardOutlined),
    label: '控制台',
    title: '控制台',
  },
  {
    key: 'users',
    icon: () => h(UserOutlined),
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: 'blogs',
    icon: () => h(FileTextOutlined),
    label: '博客管理',
    title: '博客管理',
  },
  {
    key: 'pictures',
    icon: () => h(PictureOutlined),
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: 'spaces',
    icon: () => h(AppstoreOutlined),
    label: '空间管理',
    title: '空间管理',
  },
]

// 菜单点击处理
const handleMenuClick = ({ key }: { key: string }) => {
  if (key === 'dashboard') {
    router.push('/admin')
  } else {
    router.push(`/admin/${key}`)
  }
}

// 返回用户端
const goBack = () => {
  router.push('/')
}

// 退出登录
const doLogout = async () => {
  await userStore.logout()
  router.push('/login')
}
</script>

<template>
  <a-layout class="admin-layout">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      :width="240"
      :collapsed-width="80"
      class="admin-sider"
      theme="light"
    >
      <!-- Logo -->
      <div class="admin-logo" @click="router.push('/admin')">
        <img src="/favicon.ico" alt="千语壁纸" class="admin-logo-img" />
        <transition name="logo-text">
          <span v-if="!collapsed" class="admin-logo-text">千语管理</span>
        </transition>
      </div>

      <!-- 菜单 -->
      <a-menu
        :selected-keys="selectedKeys"
        mode="inline"
        :items="menuItems"
        class="admin-menu"
        @click="handleMenuClick"
      />

      <!-- 底部操作 -->
      <div class="sider-footer">
        <a-tooltip :title="collapsed ? '返回用户端' : ''" placement="right">
          <div class="sider-footer-item" @click="goBack">
            <ArrowLeftOutlined />
            <span v-if="!collapsed">返回用户端</span>
          </div>
        </a-tooltip>
      </div>
    </a-layout-sider>

    <!-- 右侧内容区 -->
    <a-layout class="admin-content-layout">
      <!-- 顶部栏 -->
      <a-layout-header class="admin-header">
        <div class="header-left">
          <component
            :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
            class="trigger-icon"
            @click="collapsed = !collapsed"
          />
        </div>
        <div class="header-right">
          <a-dropdown>
            <div class="header-user">
              <a-avatar :size="32" style="background-color: #1890ff">
                {{ userStore.user?.nickname?.charAt(0) || 'A' }}
              </a-avatar>
              <span class="header-username">{{ userStore.user?.nickname || '管理员' }}</span>
            </div>
            <template #overlay>
              <a-menu>
                <a-menu-item key="back" @click="goBack">
                  <ArrowLeftOutlined />
                  <span style="margin-left: 8px">返回用户端</span>
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="doLogout">
                  <LogoutOutlined />
                  <span style="margin-left: 8px">退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <!-- 内容区 -->
      <a-layout-content class="admin-content">
        <RouterView />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

/* 侧边栏 */
.admin-sider {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.admin-sider :deep(.ant-layout-sider-children) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* Logo */
.admin-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  overflow: hidden;
  transition: all 0.3s;
}

.admin-logo:hover {
  background: #fafafa;
}

.admin-logo-img {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  flex-shrink: 0;
}

.admin-logo-text {
  font-size: 18px;
  font-weight: 700;
  color: #1890ff;
  white-space: nowrap;
  overflow: hidden;
}

.logo-text-enter-active,
.logo-text-leave-active {
  transition:
    opacity 0.2s,
    width 0.3s;
}
.logo-text-enter-from,
.logo-text-leave-to {
  opacity: 0;
  width: 0;
}

/* 菜单 */
.admin-menu {
  flex: 1;
  border-right: none !important;
  padding: 8px 0;
}

.admin-menu :deep(.ant-menu-item) {
  margin: 4px 8px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
}

.admin-menu :deep(.ant-menu-item-selected) {
  background-color: #e6f4ff;
  font-weight: 500;
}

/* 侧边栏底部 */
.sider-footer {
  border-top: 1px solid #f0f0f0;
  padding: 8px;
  flex-shrink: 0;
}

.sider-footer-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
}

.sider-footer-item:hover {
  background: #f5f5f5;
  color: #1890ff;
}

/* 顶部栏 */
.admin-header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  height: 64px;
  line-height: 64px;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
}

.trigger-icon {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.3s;
  padding: 8px;
  border-radius: 6px;
}

.trigger-icon:hover {
  color: #1890ff;
  background: #f5f5f5;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.header-user:hover {
  background: #f5f5f5;
}

.header-username {
  font-size: 14px;
  color: #333;
}

/* 内容区 */
.admin-content {
  margin: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 64px - 48px);
}
</style>
