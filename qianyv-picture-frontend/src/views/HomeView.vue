<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { useBlogStore } from '@/stores/blog'
import { useRouter } from 'vue-router'
import BlogUploadModal from '@/components/BlogUploadModal.vue'

const userStore = useUserStore()
const blogStore = useBlogStore()
const router = useRouter()

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
// 【后端接入指南】
// 1. 数据格式要求：
//    - id: number（用户唯一标识）
//    - nickname: string（用户昵称）
//    - avatar: string（用户头像URL，可为空字符串，空时显示昵称首字母）
//    - postCount: number（发布数量）
//    - fansCount: number（粉丝数量）
//    - recentShareCount: number（近期分享数）
// 2. 数据数量：当前UI最多显示7名用户，后端返回数据应限制在7条以内
// 3. 排序规则：建议后端按综合贡献度排序（可综合考虑发布数、粉丝数、分享数等指标）
// 4. 接入方式：
//    - 创建API接口：例如 getRankingList()
//    - 在 onMounted 中调用接口获取数据
//    - 将接口返回数据赋值给 rankingList.value
// 5. 示例代码：
//    onMounted(async () => {
//      const res = await getRankingList({ pageSize: 7 })
//      if (res.code === 0) {
//        rankingList.value = res.data
//      }
//    })
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
    postCount: 152,
    fansCount: 489,
    recentShareCount: 13,
  },
])

// 展开评论的博客ID集合
const expandedComments = ref<Set<number>>(new Set())

// 切换评论展开状态
const toggleComments = (blogId: number | undefined) => {
  if (!blogId) return
  if (expandedComments.value.has(blogId)) {
    expandedComments.value.delete(blogId)
  } else {
    expandedComments.value.add(blogId)
  }
}

// 点赞操作
const handleLike = async (blogId: number | undefined) => {
  if (blogId) {
    await blogStore.toggleLike(blogId)
  }
}

// 收藏操作
const handleCollect = async (blogId: number | undefined) => {
  if (blogId) {
    await blogStore.toggleCollect(blogId)
  }
}

// 切换右侧面板
const toggleRightPanel = () => {
  rightPanelMode.value = rightPanelMode.value === 'ranking' ? 'chat' : 'ranking'
}

// 分享功能
const handleShare = () => {
  showUploadModal.value = true
}

// 博客发布成功回调
const handlePublishSuccess = async () => {
  // 刷新博客列表
  blogStore.current = 1
  await blogStore.fetchBlogList()
}

// 获取用户头像显示文本
const getUserAvatarText = (nickname?: string) => {
  if (!nickname) return '?'
  return nickname.charAt(0).toUpperCase()
}

// 打开图片详情页（在新标签页中打开）- 根据宽高比判断跳转到横屏或竖屏详情页
const openPictureDetail = (picture: any) => {
  if (!picture || !picture.id) return

  // 根据 picScale 判断图片方向
  // picScale >= 1 为横屏，< 1 为竖屏
  const isHorizontal = picture.picScale === undefined || picture.picScale >= 1

  // 在新标签页中打开图片详情页
  const routeUrl = router.resolve({
    name: isHorizontal ? 'horizontalPictureDetail' : 'verticalPictureDetail',
    params: { id: picture.id },
  })
  window.open(routeUrl.href, '_blank')
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
      <div class="user-info-card">
        <div class="user-avatar-large" :class="{ 'has-image': userStore.user?.avatar }">
          <img v-if="userStore.user?.avatar" :src="userStore.user.avatar" alt="avatar" />
          <span v-else>{{ getUserAvatarText(userStore.user?.nickname) }}</span>
        </div>
        <h3 class="user-nickname">{{ userStore.user?.nickname || '游客' }}</h3>
        <p class="user-intro">{{ userStore.user?.introduction || '这个人很懒，什么都没写~' }}</p>

        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-value">{{ userStats.postCount }}</div>
            <div class="stat-label">发布数</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.likeCount }}</div>
            <div class="stat-label">点赞数</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-value">{{ userStats.collectCount }}</div>
            <div class="stat-label">收藏数</div>
          </div>
        </div>

        <button class="share-btn" @click="handleShare">
          <svg
            class="plane-icon"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M22 2L11 13"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
            <path
              d="M22 2L15 22L11 13L2 9L22 2Z"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          分享
        </button>
      </div>
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
        <article v-for="blog in blogStore.blogs" :key="blog.id" class="blog-item">
          <!-- 博客文本内容区域：包含用户信息、标题、内容 -->
          <div class="blog-text-section">
            <!-- 博客头部：作者信息 + 点赞收藏 -->
            <div class="blog-header">
              <div class="author-info">
                <div class="author-avatar" :class="{ 'has-image': blog.user?.avatar }">
                  <img v-if="blog.user?.avatar" :src="blog.user.avatar" alt="avatar" />
                  <span v-else>{{ getUserAvatarText(blog.user?.nickname) }}</span>
                </div>
                <div class="author-details">
                  <div class="author-name">{{ blog.user?.nickname || '匿名用户' }}</div>
                  <div class="author-tag">
                    {{ blog.user?.tag || blog.user?.introduction || '暂无签名' }}
                  </div>
                </div>
              </div>

              <div class="blog-actions">
                <div class="action-group">
                  <button class="action-btn" @click="handleLike(blog.id)">
                    <span class="action-icon">👍</span>
                    <span class="action-count">{{ blog.likeCount || 0 }}</span>
                  </button>
                  <button class="action-btn" @click="handleCollect(blog.id)">
                    <span class="action-icon">⭐</span>
                    <span class="action-count">{{ blog.collectCount || 0 }}</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- 博客标题和内容 -->
            <div class="blog-content">
              <h2 class="blog-title">{{ blog.title }}</h2>
              <p class="blog-text">{{ blog.content }}</p>
            </div>
          </div>

          <!-- 博客图片区域：独立的图片瀑布流 -->
          <div
            v-if="blog.pictureVOList && blog.pictureVOList.length > 0"
            class="blog-images-section"
          >
            <div class="blog-images">
              <div
                v-for="pic in blog.pictureVOList"
                :key="pic.id"
                class="image-item"
                @click="openPictureDetail(pic)"
              >
                <img :src="pic.thumbUrl" :alt="pic.tags" />
                <div class="image-overlay">
                  <span class="image-tags">{{ pic.tags }}</span>
                  <div class="image-zoom">
                    <svg viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                      <circle
                        cx="12"
                        cy="12"
                        r="3"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      />
                    </svg>
                    <span>view</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 博客底部：评论区 -->
          <div class="blog-footer">
            <button class="comment-toggle" @click="toggleComments(blog.id)">
              <span class="comment-icon">💬</span>
              <span>评论 ({{ blog.commentCount || 0 }})</span>
              <span
                class="toggle-arrow"
                :class="{ expanded: blog.id && expandedComments.has(blog.id) }"
                >▼</span
              >
            </button>

            <div v-if="blog.id && expandedComments.has(blog.id)" class="comment-section">
              <div class="comment-input">
                <input type="text" placeholder="写下你的评论..." />
                <button class="send-btn">发送</button>
              </div>
              <div class="comment-list">
                <p class="comment-empty">暂无评论，快来抢沙发吧~</p>
              </div>
            </div>
          </div>
        </article>

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

        <!-- 没有更多数据提示 -->
        <div v-else-if="blogStore.blogs.length > 0" class="no-more-data">
          <span>—— 已经到底了 ——</span>
        </div>
      </div>
    </main>

    <!-- 右侧：排行榜 / 聊天框 -->
    <aside class="right-panel">
      <!-- 排行榜模式 -->
      <div v-if="rightPanelMode === 'ranking'" class="ranking-card">
        <!-- 书签样式的标题 -->
        <div class="ranking-bookmark">
          <span class="bookmark-text">近期贡献者</span>
        </div>

        <div class="ranking-list">
          <div v-for="user in rankingList" :key="user.id" class="ranking-item">
            <div class="rank-avatar" :class="{ 'has-image': user.avatar }">
              <img v-if="user.avatar" :src="user.avatar" alt="avatar" />
              <span v-else>{{ getUserAvatarText(user.nickname) }}</span>
            </div>
            <div class="rank-info">
              <div class="rank-name">{{ user.nickname }}</div>
              <div class="rank-stats">
                <span>发布: {{ user.postCount }}</span>
                <span>粉丝: {{ user.fansCount }}</span>
                <span>分享: {{ user.recentShareCount }}</span>
              </div>
            </div>
          </div>
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
  min-height: calc(100vh - 60px); /* 只减去导航栏高度 */
  overflow: visible;
  position: relative;
  padding-bottom: 40px;
}

/* ========== 左侧用户信息区域 - 放大 ========== */
.left-panel {
  position: fixed;
  left: 0;
  top: 60px;
  width: 36%;
  height: calc(100vh - 60px); /* 只减去导航栏高度 */
  overflow: hidden;
  background: transparent;
  z-index: 15;
  display: flex;
  align-items: flex-start;
  padding: 0;
  pointer-events: auto; /* 允许事件交互 */
}

.user-info-card {
  /* 尺寸和位置：左边距调整，卡片宽度增大 */
  width: 72%;
  height: auto; /* 改为自动高度，让内容自然撑开 */
  max-height: 75%; /* 最大高度限制 */
  margin-left: 3%; /* 左边距 3% */
  margin-right: 20%; /* 右边距 20% */
  margin-top: 60px; /* 距离顶部 60px */

  /* 外观样式 */
  background: rgba(255, 255, 255, 0.1); /* 更加透明的白色背景，能清晰看到背后的渐变 */
  backdrop-filter: blur(8px); /* 进一步减少模糊度，让背景渐变更清晰 */
  -webkit-backdrop-filter: blur(8px);
  border: 2px dashed rgba(223, 231, 245, 0.5); /* 稍微加深边框，增强对比 */
  border-radius: 22px; /* 适当圆角 */

  /* 内容布局 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start; /* 改为顶部对齐，避免压缩 */
  padding: 36px 28px; /* 适当的内边距 */

  /* 阴影效果 */
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);

  /* 动画过渡 */
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  pointer-events: auto; /* 卡片本身可以交互 */
}

.user-info-card:hover {
  border-color: rgba(138, 180, 248, 0.6);
  box-shadow: 0 12px 48px rgba(138, 180, 248, 0.15);
  transform: translateY(-4px);
}

.user-avatar-large {
  width: 110px;
  height: 110px; /* 固定高度，确保不会被压扁 */
  min-height: 110px; /* 最小高度保护 */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 44px;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-bottom: 18px; /* 适当的底部间距 */
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  border: 3px solid rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  flex-shrink: 0; /* 防止被压缩 */
}

.user-avatar-large.has-image {
  background: transparent;
  overflow: hidden;
}

.user-avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 确保图片按比例裁剪 */
}

.user-nickname {
  font-size: 20px; /* 减小昵称字体 */
  font-weight: 700;
  color: #2c5282;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.user-intro {
  font-size: 13px; /* 减小简介字体 */
  color: #718096;
  text-align: center;
  margin-bottom: 20px;
  line-height: 1.5;
  max-width: 90%;
}

.user-stats {
  display: flex;
  align-items: center;
  gap: 16px; /* 减小间距 */
  margin-bottom: 20px; /* 减小间距 */
  width: 100%;
  padding: 16px 0; /* 减小内边距 */
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.stat-item {
  flex: 1;
  text-align: center;
  transition: transform 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-value {
  font-size: 22px; /* 减小数值字体 */
  font-weight: 700;
  color: #1aa0c1;
  margin-bottom: 4px; /* 减小间距 */
  text-shadow: 0 2px 4px rgba(26, 160, 193, 0.1);
}

.stat-label {
  font-size: 11px; /* 减小标签字体 */
  color: #a0aec0;
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 32px; /* 减小分隔线高度 */
  background: rgba(0, 0, 0, 0.08);
}

.share-btn {
  width: auto; /* 改为自动宽度 */
  padding: 16px 40px; /* 放大内边距 */
  margin: 0 auto; /* 居中显示 */
  margin-top: 8px;
  background: rgba(138, 180, 248, 0.12); /* 稍微加深背景 */
  color: #000000; /* 黑色文字 */
  border: 2px solid rgba(255, 255, 255, 0.8); /* 白色边框 */
  border-radius: 12px; /* 稍微增大圆角 */
  font-size: 18px; /* 放大字体 */
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s ease;
  box-shadow:
    0 0 10px rgba(255, 255, 255, 0.3),
    0 0 20px rgba(138, 180, 248, 0.2); /* 泛光效果 */
  position: relative;
  overflow: hidden;
}

.share-btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition:
    width 0.6s,
    height 0.6s;
}

.share-btn:hover::before {
  width: 300px;
  height: 300px;
}

.share-btn:hover {
  background: rgba(138, 180, 248, 0.2);
  border-color: rgba(255, 255, 255, 1); /* 悬停时边框更亮 */
  transform: translateY(-2px);
  box-shadow:
    0 0 15px rgba(255, 255, 255, 0.5),
    0 0 30px rgba(138, 180, 248, 0.4),
    0 4px 12px rgba(138, 180, 248, 0.2); /* 增强泛光效果 */
}

.plane-icon {
  width: 18px; /* 纸飞机图标大小 */
  height: 18px;
  transition: transform 0.3s ease;
  position: relative;
  z-index: 1;
}

.share-btn:hover .plane-icon {
  transform: translateX(3px) translateY(-3px); /* 悬停时飞起效果 */
}

/* ========== 中部博客列表 - 调整左边距 ========== */
.center-panel {
  position: absolute; /* 使用绝对定位 */
  left: 28%; /* 左侧留出 28% 给左侧面板（从25%增加） */
  right: 22%; /* 右侧留出 22% 给右侧面板 */
  top: 0;
  min-height: 100vh; /* 使用最小高度，允许内容超出视口 */
  overflow: visible; /* 允许内容溢出，使用浏览器滚动条 */
  padding: 0;
  z-index: 1;
}

/* 博客列表容器 - 添加内边距 */
.blog-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px; /* 在内部添加padding，不影响滚动条位置 */
}

/* 加载状态也需要padding */
.loading-state,
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #718096;
  margin: 24px; /* 使用margin替代padding */
}

/* 删除中部滚动条样式 - 不再需要 */

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(26, 160, 193, 0.2);
  border-top-color: #1aa0c1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.blog-item {
  background: rgba(255, 255, 255, 0.2); /* 增加透明度，从0.95降低到0.6 */
  backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 20px; /* 从28px减少到20px，让整体更紧凑 */
  border: 2px dashed rgba(138, 180, 248, 0.3);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
}

.blog-item:hover {
  border-color: rgba(138, 180, 248, 0.5);
  box-shadow: 0 8px 24px rgba(138, 180, 248, 0.15);
}

.blog-item::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 40px;
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.6); /* 与博客项背景透明度一致 */
  border: 2px dashed rgba(138, 180, 248, 0.3);
  border-radius: 50%;
}

.blog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px; /* 从20px减少到12px，减少与标题的间距 */
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px; /* 从14px减少到10px，减少头像与文字间距 */
}

.author-avatar {
  width: 48px; /* 从60px减少到48px，缩小头像尺寸 */
  height: 48px; /* 从60px减少到48px */
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px; /* 从26px减少到20px，相应减小字体 */
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #52c85c 0%, #cef4f4 100%);
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
  transition: all 0.3s ease;
}

.author-avatar.has-image {
  background: transparent;
  overflow: hidden;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 2px; /* 从4px减少到2px，让用户名和标签更紧凑 */
}

.author-name {
  font-size: 15px; /* 从16px减少到15px，稍微缩小字体 */
  font-weight: 600;
  color: #2d3748;
}

.author-tag {
  font-size: 12px; /* 从13px减少到12px，稍微缩小字体 */
  color: #a0aec0;
}

.blog-actions {
  display: flex;
  gap: 12px;
}

.action-group {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(138, 180, 248, 0.1);
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #4a5568;
}

.action-btn:hover {
  background: rgba(138, 180, 248, 0.2);
  border-color: rgba(138, 180, 248, 0.5);
  transform: translateY(-2px);
}

.action-icon {
  font-size: 16px;
}

.action-count {
  font-weight: 600;
}

/* ========== 博客内容区域 ========== */
.blog-content {
  margin-bottom: 16px; /* 从20px减少到16px */
}

.blog-title {
  font-size: 20px; /* 从26px减少到22px，进一步缩小标题 */
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 12px; /* 从16px减少到12px，减少标题与内容间距 */
  line-height: 1.4;
}

.blog-text {
  font-size: 15px; /* 从16px减少到15px，稍微缩小字体 */
  color: #4a5568;
  line-height: 1.7; /* 从1.8减少到1.7，让行高更紧凑 */
  margin-bottom: 0; /* 去除底部间距，因为图片区域是独立的 */
}

/* ========== 博客图片区域（独立区域） ========== */
.blog-images-section {
  margin-top: 16px; /* 图片区域与文本区域的间距 */
}

.blog-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 固定显示3列 */
  gap: 12px; /* 减小间距使整体更紧凑 */
}

.image-item {
  position: relative;
  border-radius: 10px; /* 减小圆角 */
  overflow: hidden;
  aspect-ratio: 3/4;
  cursor: pointer;
  transition: transform 0.3s ease;
}

.image-item:hover {
  transform: scale(1.05);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.image-tags {
  position: absolute;
  bottom: 12px;
  left: 12px;
  font-size: 12px;
  color: white;
}

.image-zoom {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
}

.image-zoom svg {
  width: 15px;
  height: 15px;
  stroke: rgba(255, 255, 255, 0.6);
  stroke-width: 2;
  fill: none;
}

.image-zoom span {
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.6);
}

.blog-footer {
  padding-top: 16px;
}

.comment-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(138, 180, 248, 0.08);
  border: 1px solid rgba(138, 180, 248, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  color: #4a5568;
  font-weight: 500;
}

.comment-toggle:hover {
  background: rgba(138, 180, 248, 0.15);
}

.comment-icon {
  font-size: 16px;
}

.toggle-arrow {
  margin-left: auto;
  transition: transform 0.3s ease;
}

.toggle-arrow.expanded {
  transform: rotate(180deg);
}

.comment-section {
  margin-top: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(138, 180, 248, 0.1);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.01);
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.comment-input input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  font-size: 14px;
  transition: all 0.3s ease;
}

.comment-input input:focus {
  outline: none;
  border-color: rgba(138, 180, 248, 0.6);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 3px rgba(138, 180, 248, 0.1);
}

.send-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s ease;
}

.send-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.comment-empty {
  text-align: center;
  color: #a0aec0;
  font-size: 14px;
  padding: 20px;
}

/* ========== 加载更多按钮 ========== */
.load-more-container {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

.load-more-btn {
  padding: 14px 48px;
  background: rgba(138, 180, 248, 0.1);
  border: 2px solid rgba(138, 180, 248, 0.3);
  border-radius: 16px;
  color: #4a5568;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.load-more-btn:hover:not(:disabled) {
  background: rgba(138, 180, 248, 0.2);
  border-color: rgba(138, 180, 248, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.2);
}

.load-more-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(138, 180, 248, 0.3);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.no-more-data {
  text-align: center;
  padding: 32px 0;
  color: #a0aec0;
  font-size: 14px;
}

.no-more-data span {
  position: relative;
  padding: 0 20px;
}

.no-more-data span::before,
.no-more-data span::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 60px;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(160, 174, 192, 0.3));
}

.no-more-data span::before {
  right: 100%;
  margin-right: 10px;
}

.no-more-data span::after {
  left: 100%;
  margin-left: 10px;
  background: linear-gradient(to left, transparent, rgba(160, 174, 192, 0.3));
}

/* ========== 右侧排行榜/聊天框区域 ========== */
.right-panel {
  position: fixed;
  right: 0;
  top: 60px;
  width: 25%;
  height: calc(100vh - 60px); /* 只减去导航栏高度 */
  overflow: hidden;
  background: transparent;
  z-index: 15;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 0;
  pointer-events: auto; /* 允许事件交互 */
}

.ranking-card,
.chat-card {
  width: 85%; /* 增加宽度，从85%改为92% */
  margin: 0 auto; /* 水平居中 */
  height: 93%; /* 垂直高度从99%缩小到95%，减少4% */
  max-height: 93%; /* 最大  高度限制 */
  overflow: hidden; /* 禁止滚动 */
  display: flex;
  flex-direction: column;

  /* 模仿左侧用户卡片的样式 */
  background: rgba(255, 255, 255, 0.1); /* 更加透明的白色背景 */
  backdrop-filter: blur(8px); /* 与用户卡片一致的模糊度 */
  -webkit-backdrop-filter: blur(8px);
  border: 2px dashed rgba(223, 231, 245, 0.5); /* 虚线边框，与用户卡片一致 */
  border-radius: 20px; /* 与用户卡片一致的圆角 */
  padding: 53px 5px 10px;
  box-shadow: none; /* 取消外部阴影 */
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1); /* 与用户卡片一致的过渡 */
  pointer-events: auto; /* 卡片可以交互 */
  position: relative; /* 为书签定位做准备 */
}

/* 移除排行榜卡片的滚动条样式（不再需要） */
.ranking-card::-webkit-scrollbar,
.chat-card::-webkit-scrollbar {
  display: none;
}

.ranking-card:hover,
.chat-card:hover {
  border-color: rgba(138, 180, 248, 0.6); /* 与用户卡片一致的悬停边框色 */
  box-shadow: none; /* 悬停时也不显示阴影 */
  transform: translateY(-4px); /* 与用户卡片一致的悬停效果 */
}

/* 书签样式 - 左侧贴边矩形，右侧圆角 */
.ranking-bookmark {
  position: absolute;
  top: 20px; /* 往上移动，从20px改为10px */
  left: 0; /* 左侧完全贴边 */
  background: rgba(255, 255, 255, 0.3); /* 白色透明背景 */
  backdrop-filter: blur(8px); /* 添加毛玻璃效果 */
  -webkit-backdrop-filter: blur(8px);
  color: #2c5282; /* 深色文字，确保在白色背景上清晰可见 */
  padding: 3px 20px 3px 16px; /* 垂直方向缩小5px (从10px改为7.5px) */
  border-radius: 0 12px 12px 0; /* 只有右侧有圆角 */
  font-size: 14px;
  font-weight: 600;
  /* 白色泛光边框效果 */
  border: 1px rgba(255, 255, 255, 0.8);
  box-shadow:
    0 0 15px rgba(255, 255, 255, 0.6),
    0 0 30px rgba(255, 255, 255, 0.4),
    inset 0 0 20px rgba(255, 255, 255, 0.2); /* 内部也有泛光 */
  z-index: 10;
  letter-spacing: 0.5px;
  /* 移除 transition，让书签跟随卡片一起移动 */
}

.bookmark-text {
  display: block;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); /* 轻微的文字阴影，增强可读性 */
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 0; /* 移除间距，使用分割线 */
  flex: 1; /* 占据剩余空间 */
  overflow: hidden; /* 隐藏溢出内容 */
  padding-top: 12px; /* 顶部留出一点间距，避免和书签重叠 */
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px; /* 恢复到更大的间距 */
  padding: 8px 6px; /* 恢复到更大的垂直padding */
  background: transparent; /* 取消背景色 */
  border-bottom: 1px solid rgba(0, 0, 0, 0.06); /* 添加不明显的黑色分割线 */
  transition: all 0.3s ease;
}

.ranking-item:last-child {
  border-bottom: none; /* 最后一个元素不需要分割线 */
}

.ranking-item:hover {
  background: rgba(138, 180, 248, 0.08); /* 悬停时淡淡的背景 */
  transform: translateX(2px); /* 减小移动距离 */
}

.rank-avatar {
  width: 48px; /* 恢复到更大的头像尺寸 */
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px; /* 恢复更大的字体 */
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.3);
  flex-shrink: 0; /* 防止被压缩 */
}

.rank-avatar.has-image {
  background: transparent;
  overflow: hidden;
}

.rank-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-name {
  font-size: 14px; /* 恢复到更大的用户名字体 */
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 4px; /* 恢复更大的行间距 */
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-stats {
  display: flex;
  gap: 12px; /* 恢复到更大的间距 */
  font-size: 11px; /* 恢复更大的字体 */
  color: #718096;
  flex-wrap: nowrap; /* 防止换行 */
}

.rank-stats span {
  white-space: nowrap; /* 防止换行 */
  flex-shrink: 0; /* 防止被压缩 */
}

/* 聊天框样式 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  flex-shrink: 0; /* 头部不缩小 */
}

.chat-header h3 {
  font-size: 18px;
  font-weight: 700;
  color: #2c5282;
}

.close-chat {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(245, 87, 108, 0.1);
  border: none;
  font-size: 24px;
  color: #f5576c;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-chat:hover {
  background: rgba(245, 87, 108, 0.2);
  transform: rotate(90deg);
}

.chat-messages {
  flex: 1; /* 占据剩余空间 */
  overflow: hidden; /* 禁止滚动 */
  margin-bottom: 16px;
  padding: 16px;
  background: rgba(247, 250, 252, 0.6);
  border-radius: 12px;
}

.chat-message {
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 8px;
}

.chat-message.system {
  background: rgba(138, 180, 248, 0.15);
  color: #4a5568;
  text-align: center;
  font-size: 13px;
}

.chat-input {
  display: flex;
  gap: 12px;
  flex-shrink: 0; /* 输入框不缩小 */
}

.chat-input input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 8px;
  background: white;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1400px) {
  .left-panel {
    flex-basis: 30%;
  }

  .right-panel {
    flex-basis: 22%;
  }

  .user-info-card {
    width: 70%;
    margin-left: 8%;
    margin-right: 22%;
  }
}

@media (max-width: 1200px) {
  .home-view {
    flex-direction: column;
    height: auto;
  }

  .left-panel,
  .right-panel {
    flex-basis: auto;
    height: auto;
    min-height: 300px;
  }

  .left-panel {
    padding: 24px 0;
  }

  .user-info-card {
    width: 80%;
    height: auto;
    margin: 0 auto;
  }

  .center-panel {
    order: -1;
    height: auto;
    min-height: 500px;
  }
}
</style>
