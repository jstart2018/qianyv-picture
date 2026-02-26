<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch, nextTick } from 'vue'
import { useUserStore } from '@/stores/user'
import { useBlogStore } from '@/stores/blog'
import * as userApi from '@/api/userController'
import BlogUploadModal from '@/components/BlogUploadModal.vue'
import BlogCard from '@/components/BlogCard.vue'
import RankingUserCard from '@/components/RankingUserCard.vue'
import UserStatsCard from '@/components/UserStatsCard.vue'

const userStore = useUserStore()
const blogStore = useBlogStore()

// 博客上传弹窗
const showUploadModal = ref(false)

// 右侧切换状态：排行榜 or 聊天框
const rightPanelMode = ref<'ranking' | 'chat'>('ranking')

// 筛选条件
const activeFilter = ref<'recommend' | 'latest' | null>(null)
const searchKeyword = ref('')

// 用户统计信息 - 从后端数据计算
const userStats = computed(() => {
  const user = userStore.user
  if (!user) {
    return {
      postCount: 0,
      likeCount: 0,
      collectCount: 0,
      downloadCount: 0,
      fansCount: 0,
    }
  }

  // 将字符串转换为数字
  const toNumber = (value: number | string | undefined): number => {
    if (value === undefined || value === null) return 0
    if (typeof value === 'string') return parseInt(value, 10) || 0
    return value
  }

  return {
    postCount: toNumber(user.publishCount),
    likeCount: toNumber(user.likeCount),
    collectCount: toNumber(user.collectCount),
    downloadCount: toNumber(user.downloadCount),
    fansCount: toNumber(user.fanCount),
  }
})

// ========== 排行榜数据 ==========
const rankingList = ref<
  Array<{
    id: number
    nickname: string
    avatar?: string
    postCount: number
    fansCount: number
    recentShareCount: number
  }>
>([])

const rankingLoading = ref(false)

// 获取热门用户排行榜
const fetchRankingList = async () => {
  rankingLoading.value = true
  try {
    const res = await userApi.getHotUser()
    if (res.data?.code === 0 && res.data?.data) {
      // 将字符串转换为数字的工具函数
      const toNumber = (value: number | string | undefined | null): number => {
        if (value === undefined || value === null) return 0
        if (typeof value === 'string') return parseInt(value, 10) || 0
        return value
      }

      // 转换为 RankingUserCard 需要的格式
      rankingList.value = res.data.data.map((user, index) => ({
        id: user.id || index + 1, // 如果 id 为 null，使用索引+1作为临时 id
        nickname: user.nickname || '未知用户',
        avatar: user.avatar,
        postCount: toNumber(user.publishCount),
        fansCount: toNumber(user.fanCount),
        recentShareCount: toNumber(user.publishCount), // 使用发布数作为最近分享数
      }))
    }
  } catch (error) {
    console.error('获取排行榜数据失败:', error)
    rankingList.value = []
  } finally {
    rankingLoading.value = false
  }
}

// 事件处理（点赞和收藏已在 BlogCard 组件内部处理）

const toggleRightPanel = () => {
  rightPanelMode.value = rightPanelMode.value === 'ranking' ? 'chat' : 'ranking'
}

const handleShare = () => {
  showUploadModal.value = true
}

// 博客发布成功回调
const handlePublishSuccess = async () => {
  await refreshBlogList()
}

// 筛选条件变化时重新加载
const handleFilterChange = async (filter: 'recommend' | 'latest' | null) => {
  if (activeFilter.value === filter) {
    // 如果点击已选中的，则取消选择
    activeFilter.value = null
  } else {
    activeFilter.value = filter
  }
  await refreshBlogList()
}

// 搜索功能
const handleSearch = async () => {
  await refreshBlogList()
}

// 重新加载博客列表
const refreshBlogList = async () => {
  // 更新 store 中的筛选条件
  blogStore.searchText = searchKeyword.value
  blogStore.isRecommend = activeFilter.value === 'recommend' ? true : undefined
  blogStore.upToDate = activeFilter.value === 'latest' ? true : undefined

  // 重置加载
  await blogStore.fetchBlogList(true)
}

// 滚动加载更多
let lastScrollTime = 0 // 上次滚动触发时间
const throttleDelay = 500 // 节流延迟（毫秒）
let observer: IntersectionObserver | null = null // 底部观察器

const handleScroll = () => {
  const now = Date.now()
  // 节流：避免频繁触发
  if (now - lastScrollTime < throttleDelay) return
  lastScrollTime = now

  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const scrollHeight = document.documentElement.scrollHeight
  const clientHeight = window.innerHeight

  // 当滚动到页面底部附近时加载更多
  if (scrollTop + clientHeight >= scrollHeight - 300 && blogStore.hasMore && !blogStore.loading) {
    blogStore.fetchBlogList(false)
  }
}

// 设置底部观察器（备用方案）
const setupIntersectionObserver = () => {
  // 清理旧的观察器
  if (observer) {
    observer.disconnect()
  }

  // 创建观察器
  observer = new IntersectionObserver(
    (entries) => {
      if (entries.length === 0) return
      const entry = entries[0]
      if (entry && entry.isIntersecting && blogStore.hasMore && !blogStore.loading) {
        blogStore.fetchBlogList(false)
      }
    },
    {
      rootMargin: '300px', // 提前300px触发
    },
  )

  // 观察最后一个博客元素
  const blogList = document.querySelector('.blog-list')
  if (blogList && blogList.lastElementChild) {
    observer.observe(blogList.lastElementChild)
  }
}

// 初始化
onMounted(async () => {
  // 不等待用户信息，先让页面能浏览
  // 使用 Promise.allSettled 并行加载排行榜和博客列表
  Promise.allSettled([fetchRankingList(), blogStore.fetchBlogList(true)])

  // 同时尝试获取用户信息（不阻塞页面）
  userStore.fetchUser().catch(() => {
    // 静默处理错误，用户可能未登录
  })

  // 添加滚动监听
  window.addEventListener('scroll', handleScroll)

  // 延迟设置 IntersectionObserver 和手动触发一次滚动检测
  setTimeout(() => {
    handleScroll()
    setupIntersectionObserver()
  }, 1000)
})

onUnmounted(() => {
  // 移除滚动监听
  window.removeEventListener('scroll', handleScroll)

  // 清理 IntersectionObserver
  if (observer) {
    observer.disconnect()
    observer = null
  }
})

// 监听博客列表变化，重新设置观察器
watch(
  () => blogStore.blogs.length,
  () => {
    nextTick(() => {
      setupIntersectionObserver()
    })
  },
)
</script>

<template>
  <div class="home-view">
    <!-- 左侧：用户信息卡片 -->
    <aside class="left-panel">
      <UserStatsCard :user="userStore.user" :stats="userStats" @share="handleShare" />
    </aside>

    <!-- 中部：博客列表 -->
    <main class="center-panel">
      <!-- 筛选条件区域 -->
      <div class="filter-container">
        <div class="filter-buttons">
          <button
            :class="['filter-btn', { active: activeFilter === 'recommend' }]"
            @click="handleFilterChange('recommend')"
          >
            精选
          </button>
          <button
            :class="['filter-btn', { active: activeFilter === 'latest' }]"
            @click="handleFilterChange('latest')"
          >
            最新
          </button>
        </div>

        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索博客..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
      </div>

      <div v-if="blogStore.loading && blogStore.blogs.length === 0" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="blogStore.blogs.length === 0" class="empty-state">
        <p>暂无更多博客内容</p>
      </div>

      <div v-else class="blog-list">
        <BlogCard v-for="blog in blogStore.blogs" :key="blog.id" :blog="blog" />

        <!-- 加载更多提示 -->
        <div v-if="blogStore.loading" class="loading-more">
          <div class="loading-spinner-small"></div>
          <span>加载中...</span>
        </div>

        <div v-else-if="!blogStore.hasMore" class="no-more">
          <span>没有更多了 ~</span>
        </div>
      </div>
    </main>

    <!-- 右侧：排行榜/聊天框 -->
    <aside class="right-panel">
      <!-- 切换按钮 -->
      <button class="panel-toggle-btn" @click="toggleRightPanel">
        {{ rightPanelMode === 'ranking' ? '💬' : '🏆' }}
      </button>

      <!-- 排行榜模式 -->
      <div v-if="rightPanelMode === 'ranking'" class="ranking-card">
        <!-- 书签样式的标题 -->
        <div class="ranking-bookmark">
          <span class="bookmark-text">近期贡献者</span>
        </div>

        <!-- 加载状态 -->
        <div v-if="rankingLoading" class="ranking-loading">
          <div class="loading-spinner-small"></div>
          <p>加载中...</p>
        </div>

        <!-- 排行榜列表 -->
        <div v-else-if="rankingList.length > 0" class="ranking-list">
          <RankingUserCard
            v-for="(user, index) in rankingList"
            :key="user.id"
            :user="user"
            :rank="index + 1"
          />
        </div>

        <!-- 空状态 -->
        <div v-else class="ranking-empty">
          <p>暂无排行榜数据</p>
        </div>
      </div>

      <!-- 聊天框模式 -->
      <div v-else class="chat-card">
        <div class="chat-header">
          <h3>💬 在线聊天</h3>
          <button class="close-chat" @click="toggleRightPanel">×</button>
        </div>

        <div class="chat-messages">
          <div class="chat-message system">
            <p>欢迎来到千语壁纸社区聊天室！</p>
          </div>
        </div>

        <div class="chat-input">
          <input type="text" placeholder="输入消息..." />
          <button class="send-btn">发送</button>
        </div>
      </div>
    </aside>

    <!-- 博客上传弹窗 -->
    <BlogUploadModal v-model:visible="showUploadModal" @success="handlePublishSuccess" />
  </div>
</template>

<style scoped>
.home-view {
  width: 100%;
  min-height: 100vh; /* 最小高度，允许内容扩展 */
  position: relative;
  overflow: visible; /* 允许内容溢出使用全局滚动条 */
  padding-bottom: 40px; /* 底部留白 */
}

/* ========== 左侧用户信息区域 ========== */
.left-panel {
  position: fixed;
  left: 0;
  top: 76px; /* 导航栏下方（padding 14px * 2 + 内容约 48px） */
  width: 28%;
  height: calc(100vh - 76px);
  overflow: hidden; /* 完全禁止滚动，避免多层滚动嵌套 */
  background: transparent;
  z-index: 15;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 20px 0;
  pointer-events: auto;
}

/* ========== 中部博客列表区域 ========== */
.center-panel {
  margin-left: 28%; /* 使用 margin 代替定位，让它占据文档流 */
  margin-right: 25%;
  min-height: 100vh; /* 最小高度，允许内容扩展 */
  overflow: visible; /* 使用全局滚动条 */
  padding: 0;
  z-index: 1;
  /* 不使用 position，让它在文档流中撑开父容器高度 */
}

/* 筛选条件区域 */
.filter-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-md);
  padding: 12px var(--spacing-lg);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  margin: var(--spacing-md) var(--spacing-lg);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-buttons {
  display: flex;
  gap: var(--spacing-md);
}

.filter-btn {
  padding: 8px 20px;
  border-radius: 10px;
  border: 2px solid rgba(138, 180, 248, 0.3);
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  color: #2c5282;
  transition: all 0.3s ease;
}

.filter-btn:hover {
  background: rgba(255, 255, 255, 0.5);
  border-color: rgba(138, 180, 248, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(138, 180, 248, 0.2);
}

.filter-btn.active {
  background: linear-gradient(135deg, #8ab4f8 0%, #5a9cf8 100%);
  border-color: #5a9cf8;
  color: white;
  box-shadow: 0 4px 12px rgba(90, 156, 248, 0.4);
}

.search-box {
  display: flex;
  gap: var(--spacing-sm);
  flex: 1;
  max-width: 400px;
}

.search-input {
  flex: 1;
  padding: 8px 14px;
  border-radius: 10px;
  border: 2px solid rgba(138, 180, 248, 0.3);
  background: rgba(255, 255, 255, 0.5);
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: #8ab4f8;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 3px rgba(138, 180, 248, 0.1);
}

.search-btn {
  padding: 8px 18px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #8ab4f8 0%, #5a9cf8 100%);
  color: white;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(90, 156, 248, 0.4);
}

.blog-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  padding: var(--spacing-lg);
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: var(--text-tertiary);
  margin: var(--spacing-lg);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(138, 180, 248, 0.2);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 加载更多 */
.loading-more,
.no-more {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-xl) 0;
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
}

.no-more {
  color: #999;
  font-style: italic;
}

.loading-spinner-small {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

/* ========== 右侧排行榜区域 ========== */
.right-panel {
  position: fixed;
  right: 0;
  top: 76px; /* 导航栏下方 */
  width: 25%;
  height: calc(100vh - 76px);
  overflow: hidden; /* 完全禁止滚动，避免多层滚动嵌套 */
  background: transparent;
  z-index: 15;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 0;
  pointer-events: auto;
}

.panel-toggle-btn {
  position: absolute;
  top: var(--spacing-lg);
  right: var(--spacing-lg);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--gradient-primary);
  border: none;
  font-size: var(--font-size-xl);
  cursor: pointer;
  box-shadow: var(--shadow-md);
  transition: var(--transition-all);
  z-index: 10;
}

.panel-toggle-btn:hover {
  transform: scale(1.1) rotate(10deg);
  box-shadow: var(--shadow-lg);
}

.ranking-card,
.chat-card {
  width: 85%;
  margin: 0 auto;
  height: 93%;
  max-height: 93%;
  overflow: hidden;
  display: flex;
  flex-direction: column;

  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 2px dashed rgba(223, 231, 245, 0.5);
  border-radius: 20px;
  padding: 53px 5px 10px;
  box-shadow: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  pointer-events: auto;
  position: relative;
}

.ranking-card:hover,
.chat-card:hover {
  border-color: rgba(138, 180, 248, 0.6);
  box-shadow: none;
  transform: translateY(-4px);
}

/* 排行榜 */
.ranking-bookmark {
  position: absolute;
  top: 20px;
  left: 0;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #2c5282;
  padding: 3px 20px 3px 16px;
  border-radius: 0 12px 12px 0;
  font-size: 14px;
  font-weight: 600;
  border: 1px rgba(255, 255, 255, 0.8);
  box-shadow:
    0 0 15px rgba(255, 255, 255, 0.6),
    0 0 30px rgba(255, 255, 255, 0.4),
    inset 0 0 20px rgba(255, 255, 255, 0.2);
  z-index: 10;
  letter-spacing: 0.5px;
}

.bookmark-text {
  display: block;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 0;
  flex: 1;
  overflow-y: auto; /* 允许垂直滚动 */
  overflow-x: hidden; /* 禁止水平滚动 */
  padding-top: 12px;
  /* 隐藏滚动条 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.ranking-list::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}

/* 排行榜加载状态 */
.ranking-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--text-tertiary);
  gap: 12px;
}

.ranking-loading .loading-spinner-small {
  width: 24px;
  height: 24px;
  border: 2px solid rgba(138, 180, 248, 0.2);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 排行榜空状态 */
.ranking-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--text-tertiary);
  font-size: 14px;
}

/* 聊天框 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 2px solid rgba(138, 180, 248, 0.2);
}

.chat-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: var(--font-size-lg);
}

.close-chat {
  background: transparent;
  border: none;
  font-size: var(--font-size-3xl);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: var(--transition-base);
  line-height: 1;
}

.close-chat:hover {
  color: var(--color-error);
  transform: rotate(90deg);
}

.chat-messages {
  flex: 1;
  overflow-y: auto; /* 允许垂直滚动 */
  overflow-x: hidden; /* 禁止水平滚动 */
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-md);
  background: rgba(247, 250, 252, 0.6);
  border-radius: var(--radius-md);
  min-height: 300px;
  max-height: 400px;
  /* 隐藏滚动条 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.chat-messages::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}

.chat-message {
  padding: var(--spacing-md);
  border-radius: var(--radius-sm);
  margin-bottom: var(--spacing-sm);
}

.chat-message.system {
  background: rgba(138, 180, 248, 0.15);
  color: var(--text-secondary);
  text-align: center;
  font-size: var(--font-size-sm);
}

.chat-input {
  display: flex;
  gap: var(--spacing-md);
  flex-shrink: 0;
}

.chat-input input {
  flex: 1;
  padding: 10px var(--spacing-md);
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: var(--radius-sm);
  background: white;
  font-size: var(--font-size-sm);
  transition: var(--transition-base);
}

.chat-input input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.send-btn {
  padding: 10px var(--spacing-lg);
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  transition: var(--transition-base);
}

.send-btn:hover {
  background: #1890a0;
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .left-panel {
    width: 30%;
  }

  .center-panel {
    left: 30%;
    right: 22%;
  }

  .right-panel {
    width: 22%;
  }
}

@media (max-width: 1200px) {
  .home-view {
    min-height: 100vh;
    overflow: visible;
  }

  .left-panel,
  .right-panel {
    position: relative;
    width: 100%;
    height: auto;
    min-height: 300px;
    top: 0;
    padding: 20px;
    overflow: visible; /* 移动端使用全局滚动 */
  }

  .center-panel {
    margin-left: 0;
    margin-right: 0;
    width: 100%;
    height: auto;
    overflow: visible;
    order: -1;
  }
}
</style>
