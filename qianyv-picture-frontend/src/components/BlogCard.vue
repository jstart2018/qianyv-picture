<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserAvatarText } from '@/utils'

interface Props {
  blog: API.BlogsVO
}

const props = defineProps<Props>()

const emit = defineEmits<{
  like: [blogId: number | undefined]
  collect: [blogId: number | undefined]
}>()

const router = useRouter()

// 评论展开状态（每个卡片独立管理）
const commentsExpanded = ref(false)

const toggleComments = () => {
  commentsExpanded.value = !commentsExpanded.value
}

const handleLike = () => {
  emit('like', props.blog.id)
}

const handleCollect = () => {
  emit('collect', props.blog.id)
}

// 打开图片详情页（在新标签页中打开）
const openPictureDetail = (picture: API.PictureListVO) => {
  if (!picture || !picture.id) return

  // 根据 picScale 判断图片方向
  const isHorizontal = picture.picScale === undefined || picture.picScale >= 1

  const routeUrl = router.resolve({
    name: isHorizontal ? 'horizontalPictureDetail' : 'verticalPictureDetail',
    params: { id: picture.id },
  })
  window.open(routeUrl.href, '_blank')
}
</script>

<template>
  <article class="blog-card">
    <!-- 博客文本内容区域 -->
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
            <button class="action-btn" @click="handleLike">
              <span class="action-icon">👍</span>
              <span class="action-count">{{ blog.likeCount || 0 }}</span>
            </button>
            <button class="action-btn" @click="handleCollect">
              <span class="action-icon">⭐</span>
              <span class="action-count">{{ blog.collectCount || 0 }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 博客标题和内容 -->
      <div class="blog-content">
        <h2 class="blog-title">{{ blog.title || '无标题' }}</h2>
        <p class="blog-text">{{ blog.content || '暂无内容' }}</p>
      </div>
    </div>

    <!-- 博客图片区域 -->
    <div v-if="blog.pictureVOList && blog.pictureVOList.length > 0" class="blog-images-section">
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
                <circle cx="12" cy="12" r="3" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
              <span>view</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 博客底部：评论区 -->
    <div class="blog-footer">
      <button class="comment-toggle" @click="toggleComments">
        <span class="comment-icon">💬</span>
        <span>评论 ({{ blog.commentCount || 0 }})</span>
        <span class="toggle-arrow" :class="{ expanded: commentsExpanded }">▼</span>
      </button>

      <div v-if="commentsExpanded" class="comment-section">
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
</template>

<style scoped>
.blog-card {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 2px dashed rgba(138, 180, 248, 0.3);
  border-radius: 24px;
  padding: 20px;
  margin-bottom: var(--spacing-lg);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;
}

.blog-card:hover {
  border-color: rgba(138, 180, 248, 0.5);
  box-shadow: 0 8px 24px rgba(138, 180, 248, 0.15);
}

.blog-card::before {
  content: '';
  position: absolute;
  top: -8px;
  left: 40px;
  width: 16px;
  height: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 2px dashed rgba(138, 180, 248, 0.3);
  border-radius: 50%;
}

/* 博客头部 */
.blog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--font-size-lg);
  font-weight: var(--font-weight-bold);
  color: white;
  background: linear-gradient(135deg, #52c85c 0%, #cef4f4 100%);
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
  transition: var(--transition-all);
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
  gap: 2px;
}

.author-name {
  font-size: 15px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.author-tag {
  font-size: var(--font-size-sm);
  color: var(--text-tertiary);
}

.blog-actions {
  display: flex;
  gap: var(--spacing-md);
}

.action-group {
  display: flex;
  gap: var(--spacing-sm);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: var(--spacing-sm) var(--spacing-md);
  background: rgba(138, 180, 248, 0.1);
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition-all);
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.action-btn:hover {
  background: rgba(138, 180, 248, 0.2);
  border-color: rgba(138, 180, 248, 0.5);
  transform: translateY(-2px);
}

.action-icon {
  font-size: var(--font-size-base);
}

.action-count {
  font-weight: var(--font-weight-semibold);
}

/* 博客内容 */
.blog-content {
  margin-bottom: var(--spacing-md);
}

.blog-title {
  font-size: 26px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin-bottom: 14px;
  line-height: 1.4;
}

.blog-text {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.7;
  margin-bottom: 0;
}

/* 博客图片区域 */
.blog-images-section {
  margin-top: var(--spacing-md);
}

.blog-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--spacing-md);
}

.image-item {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  aspect-ratio: 1;
  transition: var(--transition-all);
}

.image-item:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.7), transparent);
  padding: var(--spacing-md);
  opacity: 0;
  transition: var(--transition-base);
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.image-tags {
  color: white;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
}

.image-zoom {
  display: flex;
  align-items: center;
  gap: 4px;
  color: white;
  font-size: var(--font-size-sm);
}

.image-zoom svg {
  width: 18px;
  height: 18px;
  stroke: white;
  fill: none;
  stroke-width: 2;
}

/* 评论区 */
.blog-footer {
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-md);
  border-top: 1px solid rgba(138, 180, 248, 0.2);
}

.comment-toggle {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  padding: var(--spacing-sm);
  border-radius: var(--radius-sm);
  transition: var(--transition-base);
}

.comment-toggle:hover {
  background: rgba(138, 180, 248, 0.1);
}

.comment-icon {
  font-size: var(--font-size-base);
}

.toggle-arrow {
  margin-left: auto;
  transition: var(--transition-base);
}

.toggle-arrow.expanded {
  transform: rotate(180deg);
}

.comment-section {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-md);
}

.comment-input {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-md);
}

.comment-input input {
  flex: 1;
  padding: 10px var(--spacing-md);
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: var(--radius-sm);
  background: white;
  font-size: var(--font-size-sm);
  transition: var(--transition-base);
}

.comment-input input:focus {
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

.comment-list {
  padding: var(--spacing-md);
}

.comment-empty {
  text-align: center;
  color: var(--text-tertiary);
  font-size: var(--font-size-sm);
}
</style>
