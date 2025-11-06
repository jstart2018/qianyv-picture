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

// 事件处理
const handleLike = async (blogId: number | undefined) => {
  if (!blogId) return
  console.log('点赞博客:', blogId)
  // 此处应调用点赞接口
}

const handleCollect = async (blogId: number | undefined) => {
  if (!blogId) return
  console.log('收藏博客:', blogId)
  // 此处应调用收藏接口
}

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
        <BlogCard
          v-for="blog in blogStore.blogs"
          :key="blog.id"
          :blog="blog"
          @like="handleLike"
          @collect="handleCollect"
        />

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
  min-height: calc(100vh - 60px);
  overflow: visible;
  position: relative;
  padding-bottom: 40px;
}

/* ========== 左侧用户信息区域 ========== */
.left-panel {
  position: fixed;
  left: 0;
  top: 60px;
  width: 36%;
  height: calc(100vh - 60px);
  overflow: hidden;
  background: transparent;
  z-index: 15;
  display: flex;
  align-items: flex-start;
  padding: 0;
  pointer-events: auto;
}

/* ========== 中部博客列表区域 ========== */
.center-panel {
  flex: 1;
  margin: 0 auto;
  max-width: 950px;
  width: 58%;
  padding: var(--spacing-xl) var(--spacing-md);
  position: relative;
  left: 36%;
  z-index: 10;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl) 0;
  color: var(--text-tertiary);
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

.blog-list {
  display: flex;
  flex-direction: column;
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
  top: 60px;
  width: 25%;
  height: calc(100vh - 60px);
  overflow-y: auto;
  background: transparent;
  z-index: 15;
  padding: var(--spacing-xl) var(--spacing-lg);
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
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: var(--blur-lg);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  padding: var(--spacing-xl);
  border: 2px solid rgba(138, 180, 248, 0.3);
  margin-top: 60px;
}

/* 排行榜 */
.ranking-bookmark {
  background: var(--gradient-primary);
  color: white;
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md) var(--radius-md) 0 0;
  margin: calc(var(--spacing-xl) * -1) calc(var(--spacing-xl) * -1) var(--spacing-lg);
  text-align: center;
  font-weight: var(--font-weight-bold);
  font-size: var(--font-size-lg);
  box-shadow: var(--shadow-sm);
  position: relative;
}

.bookmark-text {
  position: relative;
  z-index: 1;
}

.ranking-list {
  display: flex;
  flex-direction: column;
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
  overflow-y: auto;
  margin-bottom: var(--spacing-md);
  padding: var(--spacing-md);
  background: rgba(247, 250, 252, 0.6);
  border-radius: var(--radius-md);
  min-height: 300px;
  max-height: 400px;
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
    width: 50%;
  }

  .right-panel {
    width: 20%;
  }
}

@media (max-width: 1200px) {
  .home-view {
    flex-direction: column;
    height: auto;
  }

  .left-panel,
  .right-panel {
    position: relative;
    width: 100%;
    height: auto;
    min-height: 300px;
  }

  .center-panel {
    position: relative;
    left: 0;
    width: 100%;
    max-width: 100%;
    order: -1;
  }
}
</style>
