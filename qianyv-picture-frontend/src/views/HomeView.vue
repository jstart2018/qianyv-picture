<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useBlogStore } from '@/stores/blog'
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

// 静态数据：用户统计信息
const userStats = ref({
  postCount: 128,
  likeCount: 3456,
  collectCount: 892,
})

// ========== 静态数据：排行榜 ==========
// 【重要说明】以下为临时静态数据，用于界面展示和调试
// 【后端接入指南】见原注释
const rankingList = ref([
  {
    id: 1,
    nickname: '壁纸达人',
    avatar: '',
    postCount: 356,
    fansCount: 1234,
    recentShareCount: 28,
  },
  {
    id: 2,
    nickname: '设计师小王',
    avatar: '',
    postCount: 298,
    fansCount: 987,
    recentShareCount: 24,
  },
  {
    id: 3,
    nickname: '美图收藏家',
    avatar: '',
    postCount: 267,
    fansCount: 856,
    recentShareCount: 21,
  },
  {
    id: 4,
    nickname: '创意无限',
    avatar: '',
    postCount: 234,
    fansCount: 723,
    recentShareCount: 19,
  },
  {
    id: 5,
    nickname: '色彩大师',
    avatar: '',
    postCount: 198,
    fansCount: 645,
    recentShareCount: 17,
  },
  {
    id: 6,
    nickname: '摄影爱好者',
    avatar: '',
    postCount: 176,
    fansCount: 567,
    recentShareCount: 15,
  },
  {
    id: 7,
    nickname: '艺术追梦人',
    avatar: '',
    postCount: 156,
    fansCount: 489,
    recentShareCount: 13,
  },
])

// 事件处理（点赞和收藏已在 BlogCard 组件内部处理）

const toggleRightPanel = () => {
  rightPanelMode.value = rightPanelMode.value === 'ranking' ? 'chat' : 'ranking'
}

const handleShare = () => {
  showUploadModal.value = true
}

// 博客发布成功回调
const handlePublishSuccess = async () => {
  blogStore.current = 1
  await blogStore.fetchBlogList()
}

// 加载更多
const loadMore = async () => {
  if (blogStore.loading) return
  blogStore.current++
  await blogStore.fetchBlogList()
}

// 是否有更多数据
const hasMore = computed(() => {
  return blogStore.blogs.length < blogStore.total
})

// 初始化
onMounted(async () => {
  await blogStore.fetchBlogList()
})
</script>

<template>
  <div class="home-view">
    <!-- 左侧：用户信息卡片 -->
    <aside class="left-panel">
      <UserStatsCard :user="userStore.user" :stats="userStats" @share="handleShare" />
    </aside>

    <!-- 中部：博客列表 -->
    <main class="center-panel">
      <div v-if="blogStore.loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="blogStore.blogs.length === 0" class="empty-state">
        <p>暂无博客内容</p>
      </div>

      <div v-else class="blog-list">
        <BlogCard v-for="blog in blogStore.blogs" :key="blog.id" :blog="blog" />

        <!-- 加载更多按钮 -->
        <div v-if="hasMore" class="load-more-container">
          <button class="load-more-btn" :disabled="blogStore.loading" @click="loadMore">
            <span v-if="blogStore.loading" class="loading-text">
              <span class="loading-spinner-small"></span>
              加载中...
            </span>
            <span v-else>加载更多</span>
          </button>
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

        <div class="ranking-list">
          <RankingUserCard
            v-for="(user, index) in rankingList"
            :key="user.id"
            :user="user"
            :rank="index + 1"
          />
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
.load-more-container {
  display: flex;
  justify-content: center;
  padding: var(--spacing-lg) 0;
}

.load-more-btn {
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--gradient-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  cursor: pointer;
  transition: var(--transition-all);
  min-width: 160px;
}

.load-more-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
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
