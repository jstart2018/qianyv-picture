<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRouter, RouterLink, RouterView } from 'vue-router'
import { useUserStore } from './stores/user'

const router = useRouter()
const userStore = useUserStore()
const showDropdown = ref(false)
const showNotification = ref(false)
const bubbles = ref<Array<{ id: number; left: number; delay: number; duration: number }>>([])
const showBackToTop = ref(false)

// 滑动指示器的样式
const activeIndicatorStyle = ref({
  left: '0px',
  width: '0px',
})

// 判断是否在团队空间页面
const isSpaceView = computed(() => {
  return router.currentRoute.value.path.startsWith('/space')
})

// 更新滑动指示器位置
const updateIndicator = () => {
  nextTick(() => {
    const activeLink = document.querySelector('.nav-item.active') as HTMLElement
    if (activeLink) {
      const navContainer = document.querySelector('.nav-container') as HTMLElement
      if (navContainer) {
        const containerRect = navContainer.getBoundingClientRect()
        const linkRect = activeLink.getBoundingClientRect()
        activeIndicatorStyle.value = {
          left: `${linkRect.left - containerRect.left}px`,
          width: `${linkRect.width}px`,
        }
      }
    }
  })
}

onMounted(() => {
  // 尝试获取当前登录用户（如果有）
  userStore.fetchUser().catch(() => {})

  // 生成泡泡
  generateBubbles()

  // 初始化指示器位置 - 延迟执行确保路由和 DOM 都准备好了
  nextTick(() => {
    // 再次使用 nextTick 确保 RouterLink 的 active 类已经应用
    nextTick(() => {
      updateIndicator()
    })
  })

  // 监听滚动事件，控制回到顶部按钮显示
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

// 监听路由变化，更新指示器位置
watch(
  () => router.currentRoute.value.path,
  () => {
    // 使用 nextTick 确保 DOM 更新后再计算位置
    nextTick(() => {
      updateIndicator()
    })
  },
)

// 监听路由准备好的状态，确保首次加载时指示器正确显示
router.isReady().then(() => {
  nextTick(() => {
    updateIndicator()
  })
})

// 生成泡泡
function generateBubbles() {
  const bubbleCount = 20
  for (let i = 0; i < bubbleCount; i++) {
    bubbles.value.push({
      id: i,
      left: Math.random() * 100, // 0-100%
      delay: Math.random() * 15, // 0-15秒延迟
      duration: 15 + Math.random() * 10, // 15-25秒动画时长
    })
  }
}

function goLogin() {
  router.push('/login')
}

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
  showNotification.value = false
}

function toggleNotification() {
  showNotification.value = !showNotification.value
  showDropdown.value = false
}

function closeDropdown() {
  showDropdown.value = false
  showNotification.value = false
}

async function doLogout() {
  await userStore.logout()
  showDropdown.value = false
  router.push('/')
}

const avatarText = computed(() => {
  const u = userStore.user
  return u && (u.username || u.nickname) ? (u.username || u.nickname).charAt(0).toUpperCase() : ''
})

const avatarStyle = computed(() => {
  const u = userStore.user
  const seed = (u && (u.username || u.nickname)) || 'guest'
  let h = 0
  for (let i = 0; i < seed.length; i++) {
    h = seed.charCodeAt(i) + ((h << 5) - h)
  }
  const hue = Math.abs(h) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue} 60% 65%), hsl(${(hue + 40) % 360} 60% 55%))`,
  }
})

// 处理滚动事件
function handleScroll() {
  showBackToTop.value = window.scrollY > 300
}

// 回到顶部
function scrollToTop() {
  window.scrollTo({
    top: 0,
    behavior: 'smooth',
  })
}
</script>

<template>
  <div id="app" @click="closeDropdown">
    <!-- 动态背景 -->
    <div class="animated-bg"></div>

    <!-- 泡泡效果 -->
    <div class="bubbles">
      <div
        v-for="bubble in bubbles"
        :key="bubble.id"
        class="bubble"
        :style="{
          left: bubble.left + '%',
          animationDelay: bubble.delay + 's',
          animationDuration: bubble.duration + 's',
        }"
      ></div>
    </div>

    <header class="topbar">
      <!-- Logo和标题区域 -->
      <div class="logo-container">
        <img class="logo" src="/favicon.ico" alt="千语壁纸" />
        <div class="site-name">千语壁纸</div>
      </div>

      <!-- 导航菜单区域 -->
      <nav class="nav-container">
        <!-- 滑动指示器 -->
        <div
          class="nav-indicator"
          :style="{
            left: activeIndicatorStyle.left,
            width: activeIndicatorStyle.width,
          }"
        ></div>

        <RouterLink to="/" class="nav-item" exact-active-class="active">分享社区</RouterLink>
        <RouterLink to="/pictures" class="nav-item" active-class="active"> 图片素材 </RouterLink>
        <RouterLink to="/space" class="nav-item" exact-active-class="active">团队空间</RouterLink>
      </nav>

      <!-- 右侧功能区域 -->
      <div class="right-actions">
        <!-- 通知铃铛 -->
        <div class="notification-bell" @click.stop="toggleNotification">
          <svg
            class="bell-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          <span v-if="true" class="notification-badge"></span>
        </div>

        <!-- 通知下拉框 -->
        <div v-if="showNotification" class="notification-dropdown" @click.stop>
          <div class="notification-header">
            <span>消息通知</span>
          </div>
          <div class="notification-content">
            <div class="empty-notification">暂无新消息</div>
          </div>
        </div>

        <!-- 用户头像或登录按钮 -->
        <template v-if="userStore.user">
          <div
            class="avatar"
            :class="{ 'has-image': userStore.user.avatar }"
            :style="userStore.user.avatar ? {} : avatarStyle"
            @click.stop="toggleDropdown"
          >
            <img
              v-if="userStore.user.avatar"
              :src="userStore.user.avatar"
              alt="头像"
              class="avatar-image"
            />
            <span v-else class="avatar-text">{{ avatarText }}</span>
          </div>

          <div v-if="showDropdown" class="dropdown" @click.stop>
            <div class="dropdown-top">
              <div
                class="dropdown-avatar"
                :class="{ 'has-image': userStore.user.avatar }"
                :style="userStore.user.avatar ? {} : avatarStyle"
              >
                <img
                  v-if="userStore.user.avatar"
                  :src="userStore.user.avatar"
                  alt="头像"
                  class="avatar-image"
                />
                <span v-else>{{ avatarText }}</span>
              </div>
              <div class="dropdown-info">
                <div class="name">
                  {{ userStore.user?.username || userStore.user?.nickname || '用户' }}
                </div>
                <div class="meta">下载: - 收藏: -</div>
              </div>
            </div>
            <ul class="dropdown-list">
              <li
                @click="
                  () => {
                    router.push(`/user/${userStore.user?.id || ''}`)
                    showDropdown = false
                  }
                "
              >
                个人中心
              </li>
              <li
                @click="
                  () => {
                    router.push(`/user/${userStore.user?.id || ''}`)
                    showDropdown = false
                  }
                "
              >
                编辑信息
              </li>
              <li @click="doLogout">退出登录</li>
            </ul>
          </div>
        </template>

        <button v-else class="login-btn" @click="goLogin">登录 / 注册</button>
      </div>
    </header>

    <main class="main">
      <RouterView />
    </main>

    <!-- 回到顶部按钮 - 火箭图标 (团队空间不显示) -->
    <transition name="rocket-fade">
      <button
        v-if="showBackToTop && !isSpaceView"
        class="back-to-top-rocket"
        @click="scrollToTop"
        aria-label="回到顶部"
      >
        <svg class="rocket-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <!-- 火箭主体 -->
          <path
            d="M12 2C12 2 7 6 7 12V18L9 20H15L17 18V12C17 6 12 2 12 2Z"
            fill="currentColor"
            opacity="0.9"
          />
          <!-- 火箭窗口 -->
          <circle cx="12" cy="11" r="2" fill="white" opacity="0.8" />
          <!-- 左侧火焰 -->
          <path d="M9 20L7 22L8 20Z" fill="#FFA500" opacity="0.9" />
          <!-- 右侧火焰 -->
          <path d="M15 20L17 22L16 20Z" fill="#FFA500" opacity="0.9" />
          <!-- 中间火焰 -->
          <path d="M12 20L12 23L12 20Z" fill="#FF6B00" opacity="0.9" />
        </svg>
        <span class="rocket-text">TOP</span>
      </button>
    </transition>
  </div>
</template>

<style scoped>
:root {
  --bg: #f5fbff;
  --primary: #1aa0c1;
  --primary-2: #7bd3d8;
  --text: #0f2b3b;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  position: relative;
  min-height: 100vh; /* 改为最小高度，允许内容撑开 */
  width: 100%;
  display: flex;
  flex-direction: column;
  color: var(--text);
  overflow: visible; /* 改为 visible，不创建滚动容器 */
}

/* 动态渐变背景 */
.animated-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -2;
  background: linear-gradient(135deg, #b0cddd 0%, #97c2a6 100%);
  background-size: 400% 400%;
  animation: gradientShift 60s ease infinite; /* 减慢渐变速率：从 30s 增加到 60s */
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 泡泡容器 */
.bubbles {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
  pointer-events: none;
}

/* 泡泡样式 */
.bubble {
  position: absolute;
  bottom: -100px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  opacity: 0.6;
  animation: rise 15s infinite ease-in;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
}

.bubble:nth-child(2n) {
  width: 20px;
  height: 20px;
  animation-duration: 12s;
}

.bubble:nth-child(3n) {
  width: 30px;
  height: 30px;
  animation-duration: 18s;
}

.bubble:nth-child(4n) {
  width: 50px;
  height: 50px;
  animation-duration: 20s;
}

.bubble:nth-child(5n) {
  width: 25px;
  height: 25px;
  animation-duration: 14s;
}

@keyframes rise {
  0% {
    bottom: -100px;
    transform: translateX(0) scale(1);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    bottom: 110%;
    transform: translateX(calc(100px - 200px * var(--random))) scale(1.2);
    opacity: 0;
  }
}

/* 顶部导航栏 - 透明与动态背景融合 */
.topbar {
  position: relative;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 24px;
  gap: 20px;
  background: transparent;
}

/* Logo容器 - 玻璃态效果 - 放大 */
.logo-container {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.logo-container:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  object-fit: cover;
}

.site-name {
  font-weight: 700;
  font-size: 22px;
  color: var(--primary);
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

/* 导航容器 - 大长方形圆角背景 - 放大 */
.nav-container {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(10px);
  border-radius: 22px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  flex: 0 0 58%;
  justify-content: space-evenly;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
}

/* 滑动指示器 */
.nav-indicator {
  position: absolute;
  top: 8px;
  height: calc(100% - 16px);
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(25px) saturate(150%);
  border-radius: 18px;
  box-shadow:
    0 3px 12px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  transition: all 0.65s cubic-bezier(0.34, 1.4, 0.64, 1);
  pointer-events: none;
  z-index: 0;
}

.nav-item {
  padding: 12px 32px;
  border-radius: 18px;
  color: #06303a;
  text-decoration: none;
  transition:
    color 0.3s ease,
    transform 0.2s ease;
  font-weight: 500;
  font-size: 16px;
  position: relative;
  flex: 1;
  text-align: center;
  z-index: 1;
  background: transparent;
}

.nav-item:hover {
  transform: translateY(-1px);
  color: var(--primary);
}

/* 活跃路由 - 只改变文字颜色和字重 */
.nav-item.active {
  color: var(--primary);
  font-weight: 600;
}

/* 右侧功能区域 - 放大 */
.right-actions {
  display: flex;
  align-items: center;
  gap: 18px;
}

/* 通知铃铛样式 - 放大 */
.notification-bell {
  position: relative;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.notification-bell:hover {
  background: rgba(255, 255, 255, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.bell-icon {
  width: 24px;
  height: 24px;
  color: var(--primary);
  transition: all 0.3s ease;
}

.notification-bell:hover .bell-icon {
  animation: bellRing 0.5s ease;
}

@keyframes bellRing {
  0%,
  100% {
    transform: rotate(0deg);
  }
  10%,
  30%,
  50%,
  70%,
  90% {
    transform: rotate(-10deg);
  }
  20%,
  40%,
  60%,
  80% {
    transform: rotate(10deg);
  }
}

.notification-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #ff6b6b, #ff5252);
  border-radius: 50%;
  border: 2px solid white;
  box-shadow: 0 0 6px rgba(255, 82, 82, 0.6);
  animation: pulse 2s ease infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

/* 通知下拉框 */
.notification-dropdown {
  position: absolute;
  right: 80px;
  top: 66px;
  width: 320px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(15px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  z-index: 1000;
  animation: dropdownFade 0.3s ease;
}

.notification-header {
  padding: 16px;
  border-bottom: 1px solid rgba(230, 230, 250, 0.3);
  font-weight: 600;
  color: var(--primary);
  background: rgba(230, 230, 250, 0.2);
}

.notification-content {
  padding: 16px;
  max-height: 400px;
  overflow: hidden;
}

.empty-notification {
  text-align: center;
  color: #999;
  padding: 40px 20px;
  font-size: 14px;
}

/* 登录按钮样式 - 放大 */
.login-btn {
  padding: 12px 24px;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  font-size: 15px;
  color: var(--primary);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.6);
  border-color: rgba(26, 160, 193, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 160, 193, 0.15);
}

/* 用户头像样式优化 - 放大 */
.avatar {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  font-size: 20px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.5);
  overflow: hidden;
}

.avatar.has-image {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.8);
}

.avatar:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-weight: 700;
  font-size: 25px;
}

/* 用户下拉菜单 */
.dropdown {
  position: absolute;
  right: 24px;
  top: 66px;
  width: 240px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(15px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1000;
  animation: dropdownFade 0.3s ease;
}

@keyframes dropdownFade {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 下拉菜单顶部 */
.dropdown-top {
  display: flex;
  gap: 12px;
  padding: 16px;
  align-items: center;
  border-bottom: 1px solid rgba(230, 230, 250, 0.3);
  background: rgba(230, 230, 250, 0.2);
}

.dropdown-avatar {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.dropdown-avatar.has-image {
  background: transparent;
}

.dropdown-avatar .avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dropdown-info .name {
  font-weight: 700;
  font-size: 15px;
  color: var(--primary);
}

.dropdown-info .meta {
  font-size: 12px;
  color: #6b7c86;
  margin-top: 6px;
}

/* 下拉菜单列表 */
.dropdown-list {
  list-style: none;
  padding: 8px;
  margin: 0;
}

.dropdown-list li {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 10px;
  font-weight: 500;
  color: #333;
}

.dropdown-list li:hover {
  background: rgba(230, 230, 250, 0.3);
  color: var(--primary);
  transform: translateX(3px);
}

.main {
  position: relative;
  z-index: 10;
  flex: 1 1 auto; /* 改为 auto，允许内容撑开 */
  width: 100%;
  padding: 0;
  overflow: visible; /* 完全不创建滚动容器 */
}

/* ========== 回到顶部按钮 - 火箭样式 ========== */
.back-to-top-rocket {
  position: fixed;
  right: 32px;
  bottom: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7bd3d8 0%, #1aa0c1 100%);
  border: 2px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 4px 16px rgba(26, 160, 193, 0.4),
    0 0 0 0 rgba(123, 211, 216, 0.4);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  z-index: 999;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  animation: float 3s ease-in-out infinite;
}

.back-to-top-rocket:hover {
  transform: translateY(-8px) scale(1.1);
  background: linear-gradient(135deg, #8bdee3 0%, #2ab0d1 100%);
  box-shadow:
    0 8px 24px rgba(26, 160, 193, 0.6),
    0 0 0 10px rgba(123, 211, 216, 0.2);
  animation:
    float 1s ease-in-out infinite,
    pulse 1.5s ease infinite;
}

.back-to-top-rocket:active {
  transform: translateY(-12px) scale(1.05);
  animation: launch 0.6s ease-out;
}

.rocket-icon {
  width: 24px;
  height: 24px;
  color: white;
  transition: all 0.3s ease;
}

.back-to-top-rocket:hover .rocket-icon {
  transform: translateY(-3px) scale(1.1);
  filter: drop-shadow(0 3px 6px rgba(255, 255, 255, 0.5));
}

.rocket-text {
  font-size: 9px;
  font-weight: 700;
  color: white;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

/* 火箭漂浮动画 */
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-6px);
  }
}

/* 脉冲动画 */
@keyframes pulse {
  0%,
  100% {
    box-shadow:
      0 8px 24px rgba(26, 160, 193, 0.6),
      0 0 0 0 rgba(123, 211, 216, 0.4);
  }
  50% {
    box-shadow:
      0 8px 24px rgba(26, 160, 193, 0.6),
      0 0 0 15px rgba(123, 211, 216, 0);
  }
}

/* 发射动画 */
@keyframes launch {
  0% {
    transform: translateY(0) scale(1);
  }
  50% {
    transform: translateY(-20px) scale(0.95);
  }
  100% {
    transform: translateY(0) scale(1);
  }
}

/* 按钮淡入淡出动画 */
.rocket-fade-enter-active {
  animation: rocketEnter 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.rocket-fade-leave-active {
  animation: rocketLeave 0.3s ease-out;
}

@keyframes rocketEnter {
  0% {
    opacity: 0;
    transform: translateY(60px) scale(0.5) rotate(-10deg);
  }
  60% {
    opacity: 0.8;
    transform: translateY(-10px) scale(1.1) rotate(5deg);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1) rotate(0deg);
  }
}

@keyframes rocketLeave {
  0% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(40px) scale(0.6);
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .back-to-top-rocket {
    right: 20px;
    bottom: 20px;
    width: 48px;
    height: 48px;
  }

  .rocket-icon {
    width: 20px;
    height: 20px;
  }

  .rocket-text {
    font-size: 8px;
  }
}
</style>
