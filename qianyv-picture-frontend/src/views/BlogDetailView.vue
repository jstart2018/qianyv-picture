<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import * as blogApi from '@/api/blogController'
import UserAvatar from '@/components/UserAvatar.vue'
import CommentSection from '@/components/CommentSection.vue'
import { useBlogActions } from '@/composables'

const route = useRoute()
const blogId = computed(() => route.params.id as string)

// 博客详情
const blog = ref<API.BlogsVO | null>(null)
const loading = ref(true)

// 点赞和收藏的计数（使用响应式引用）
const likeCount = ref(0)
const collectCount = ref(0)

// 使用博客点赞和收藏功能
const {
  isLiked,
  isCollected,
  likeCount: composableLikeCount,
  collectCount: composableCollectCount,
  liking,
  collecting,
  justLiked,
  justCollected,
  handleLike,
  handleCollect,
} = useBlogActions({
  // @ts-ignore - ID会被正确转换
  blogId: blogId.value,
  initialLikeCount: 0,
  initialCollectCount: 0,
  autoCheck: true,
})

// 获取博客详情
const fetchBlogDetail = async () => {
  try {
    loading.value = true
    // @ts-ignore - ID作为路径参数会被转为字符串
    const res = await blogApi.getBlogDetail({ id: blogId.value })
    if (res.data?.code === 0 && res.data?.data) {
      blog.value = res.data.data
      // 更新点赞和收藏计数
      likeCount.value = blog.value.likeCount || 0
      collectCount.value = blog.value.collectCount || 0
    }
  } catch (error) {
    console.error('获取博客详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听 composable 中的计数变化，同步到本地
watch([composableLikeCount, composableCollectCount], ([newLikeCount, newCollectCount]) => {
  likeCount.value = newLikeCount
  collectCount.value = newCollectCount
})

onMounted(async () => {
  await fetchBlogDetail()
})
</script>

<template>
  <div class="blog-detail-view">
    <div v-if="loading" class="loading">加载中...</div>
    <article v-else-if="blog" class="blog-card">
      <!-- 博客文本内容区域 -->
      <div class="blog-text-section">
        <!-- 博客头部：作者信息 + 点赞收藏 -->
        <div class="blog-header">
          <div class="author-info">
            <UserAvatar :nickname="blog.user?.nickname" :avatar="blog.user?.avatar" size="medium" />
            <div class="author-details">
              <div class="author-name">{{ blog.user?.nickname || '匿名用户' }}</div>
              <div class="author-tag">
                {{ blog.user?.tag || blog.user?.introduction || '暂无签名' }}
              </div>
            </div>
          </div>

          <div class="blog-actions">
            <div class="action-group">
              <!-- 点赞按钮 -->
              <button
                class="action-btn like-btn"
                :class="{ liked: isLiked, animate: justLiked, loading: liking }"
                @click="handleLike"
                :disabled="liking"
              >
                <!-- 水波纹效果容器 -->
                <span class="ripple-container">
                  <span class="ripple ripple-1"></span>
                  <span class="ripple ripple-2"></span>
                  <span class="ripple ripple-3"></span>
                </span>

                <!-- 爱心 SVG 图标 -->
                <svg
                  class="action-icon heart-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path
                    d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                <span class="action-count">{{ likeCount }}</span>

                <!-- 点赞动画水泡 -->
                <span class="like-bubble bubble-1"></span>
                <span class="like-bubble bubble-2"></span>
                <span class="like-bubble bubble-3"></span>
                <span class="like-bubble bubble-4"></span>
                <span class="like-bubble bubble-5"></span>
                <span class="like-bubble bubble-6"></span>
                <span class="like-bubble bubble-7"></span>
                <span class="like-bubble bubble-8"></span>
              </button>

              <!-- 收藏按钮 -->
              <button
                class="action-btn collect-btn"
                :class="{ collected: isCollected, animate: justCollected, loading: collecting }"
                @click="handleCollect"
                :disabled="collecting"
              >
                <!-- 水波纹效果容器 -->
                <span class="ripple-container">
                  <span class="ripple ripple-1"></span>
                  <span class="ripple ripple-2"></span>
                  <span class="ripple ripple-3"></span>
                </span>

                <!-- 星星 SVG 图标 -->
                <svg
                  class="action-icon star-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polygon
                    points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
                <span class="action-count">{{ collectCount }}</span>

                <!-- 收藏动画水泡 -->
                <span class="collect-bubble bubble-1"></span>
                <span class="collect-bubble bubble-2"></span>
                <span class="collect-bubble bubble-3"></span>
                <span class="collect-bubble bubble-4"></span>
                <span class="collect-bubble bubble-5"></span>
                <span class="collect-bubble bubble-6"></span>
                <span class="collect-bubble bubble-7"></span>
                <span class="collect-bubble bubble-8"></span>
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
          <div v-for="pic in blog.pictureVOList" :key="pic.id" class="image-item">
            <img :src="pic.thumbUrl" :alt="pic.tags" />
          </div>
        </div>
      </div>

      <!-- 博客底部：评论区 -->
      <CommentSection :blogId="blog.id" :commentCount="blog.commentCount || 0" />
    </article>
    <div v-else class="error">博客不存在</div>
  </div>
</template>

<style scoped>
.blog-detail-view {
  max-width: 900px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 100vh;
}

.loading,
.error {
  text-align: center;
  padding: 100px 20px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.6);
}

/* ========== 博客卡片样式（复用 BlogCard.vue 的样式）========== */
.blog-card {
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border: 2px dashed rgba(138, 180, 248, 0.3);
  border-radius: 24px;
  padding: 20px;
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
  padding: 8px 12px;
  background: transparent;
  border: 2px solid transparent;
  border-radius: 50px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 15px;
  font-weight: 600;
  color: #333;
  position: relative;
  overflow: visible;
}

.action-btn:hover:not(:disabled) {
  background: transparent;
  border-color: rgba(0, 0, 0, 0.15);
  transform: translateY(-1px) scale(1.05);
}

.action-btn:active:not(:disabled) {
  transform: translateY(0) scale(1.01);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  transition:
    transform 0.3s ease,
    opacity 0.3s ease;
  position: relative;
  z-index: 2;
}

/* 爱心图标 - 未点赞 */
.heart-icon {
  fill: none;
  stroke: #ff69b4;
  stroke-width: 2;
  transition:
    fill 0.3s ease,
    stroke 0.3s ease;
}

/* 爱心图标 - 已点赞 */
.like-btn.liked {
  color: #ff69b4;
}

.like-btn.liked .heart-icon {
  fill: #ff69b4;
  stroke: #ff69b4;
}

/* 只在动画时触发爱心跳动 */
.like-btn.liked.animate .heart-icon {
  animation: heartBeat 0.5s ease-out;
  transition: none !important;
}

/* 星星图标 - 未收藏 */
.star-icon {
  fill: none;
  stroke: #ffd54f;
  stroke-width: 2;
  transition:
    fill 0.3s ease,
    stroke 0.3s ease;
}

/* 星星图标 - 已收藏 */
.collect-btn.collected {
  color: #ffd54f;
}

.collect-btn.collected .star-icon {
  fill: #ffd54f;
  stroke: #ffd54f;
}

/* 只在动画时触发星星形成 */
.collect-btn.collected.animate .star-icon {
  animation: starFormation 0.5s ease-out;
  transition: none !important;
}

/* 加载状态 */
.action-btn.loading {
  pointer-events: none;
}

.action-btn.loading .action-icon {
  animation: spin 0.8s linear infinite;
}

.action-count {
  font-weight: 700;
  font-size: 15px;
  min-width: 24px;
  text-align: left;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 水波纹容器 */
.ripple-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border-radius: 24px;
  pointer-events: none;
  overflow: hidden;
}

/* 水波纹效果 */
.ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border-radius: 24px;
  opacity: 0;
  pointer-events: none;
}

/* 点赞水波纹 */
.like-btn.liked .ripple {
  border: 3px solid #ff69b4;
}

/* 只在动画时触发水波纹 */
.like-btn.liked.animate .ripple-1 {
  animation: rippleEffect 0.6s ease-out;
}

.like-btn.liked.animate .ripple-2 {
  animation: rippleEffect 0.6s ease-out 0.08s;
}

.like-btn.liked.animate .ripple-3 {
  animation: rippleEffect 0.6s ease-out 0.15s;
}

/* 收藏水波纹 */
.collect-btn.collected .ripple {
  border: 3px solid #ffd54f;
}

/* 只在动画时触发水波纹 */
.collect-btn.collected.animate .ripple-1 {
  animation: rippleEffect 0.6s ease-out;
}

.collect-btn.collected.animate .ripple-2 {
  animation: rippleEffect 0.6s ease-out 0.08s;
}

.collect-btn.collected.animate .ripple-3 {
  animation: rippleEffect 0.6s ease-out 0.15s;
}

/* 点赞水泡效果 */
.like-bubble {
  position: absolute;
  width: 6px;
  height: 6px;
  background: linear-gradient(135deg, #ff69b4, #ff69b4);
  border-radius: 50%;
  opacity: 0;
  pointer-events: none;
  box-shadow: 0 0 10px rgba(255, 105, 180, 0.6);
}

/* 水泡初始位置 */
.like-bubble.bubble-1 {
  top: 10%;
  left: 50%;
}
.like-bubble.bubble-2 {
  top: 25%;
  left: 85%;
}
.like-bubble.bubble-3 {
  top: 50%;
  left: 90%;
}
.like-bubble.bubble-4 {
  top: 75%;
  left: 85%;
}
.like-bubble.bubble-5 {
  top: 90%;
  left: 50%;
}
.like-bubble.bubble-6 {
  top: 75%;
  left: 15%;
}
.like-bubble.bubble-7 {
  top: 50%;
  left: 10%;
}
.like-bubble.bubble-8 {
  top: 25%;
  left: 15%;
}

/* 点赞时触发水泡动画 */
.like-btn.liked.animate .bubble-1 {
  animation: bubbleToCenter1 0.5s ease-out;
}

.like-btn.liked.animate .bubble-2 {
  animation: bubbleToCenter2 0.5s ease-out 0.03s;
}

.like-btn.liked.animate .bubble-3 {
  animation: bubbleToCenter3 0.5s ease-out 0.05s;
}

.like-btn.liked.animate .bubble-4 {
  animation: bubbleToCenter4 0.5s ease-out 0.08s;
}

.like-btn.liked.animate .bubble-5 {
  animation: bubbleToCenter5 0.5s ease-out 0.1s;
}

.like-btn.liked.animate .bubble-6 {
  animation: bubbleToCenter6 0.5s ease-out 0.13s;
}

.like-btn.liked.animate .bubble-7 {
  animation: bubbleToCenter7 0.5s ease-out 0.15s;
}

.like-btn.liked.animate .bubble-8 {
  animation: bubbleToCenter8 0.5s ease-out 0.18s;
}

/* 收藏水泡效果 */
.collect-bubble {
  position: absolute;
  width: 6px;
  height: 6px;
  background: linear-gradient(135deg, #ffd54f, #ffd54f);
  border-radius: 50%;
  opacity: 0;
  pointer-events: none;
  box-shadow: 0 0 10px rgba(255, 213, 79, 0.6);
}

/* 水泡初始位置 */
.collect-bubble.bubble-1 {
  top: 10%;
  left: 50%;
}
.collect-bubble.bubble-2 {
  top: 25%;
  left: 85%;
}
.collect-bubble.bubble-3 {
  top: 50%;
  left: 90%;
}
.collect-bubble.bubble-4 {
  top: 75%;
  left: 85%;
}
.collect-bubble.bubble-5 {
  top: 90%;
  left: 50%;
}
.collect-bubble.bubble-6 {
  top: 75%;
  left: 15%;
}
.collect-bubble.bubble-7 {
  top: 50%;
  left: 10%;
}
.collect-bubble.bubble-8 {
  top: 25%;
  left: 15%;
}

/* 收藏时触发水泡动画 */
.collect-btn.collected.animate .bubble-1 {
  animation: bubbleToCenter1 0.5s ease-out;
}

.collect-btn.collected.animate .bubble-2 {
  animation: bubbleToCenter2 0.5s ease-out 0.03s;
}

.collect-btn.collected.animate .bubble-3 {
  animation: bubbleToCenter3 0.5s ease-out 0.05s;
}

.collect-btn.collected.animate .bubble-4 {
  animation: bubbleToCenter4 0.5s ease-out 0.08s;
}

.collect-btn.collected.animate .bubble-5 {
  animation: bubbleToCenter5 0.5s ease-out 0.1s;
}

.collect-btn.collected.animate .bubble-6 {
  animation: bubbleToCenter6 0.5s ease-out 0.13s;
}

.collect-btn.collected.animate .bubble-7 {
  animation: bubbleToCenter7 0.5s ease-out 0.15s;
}

.collect-btn.collected.animate .bubble-8 {
  animation: bubbleToCenter8 0.5s ease-out 0.18s;
}

/* ========== 动画定义 ========== */

/* 爱心跳动动画 */
@keyframes heartBeat {
  0% {
    transform: scale(1);
  }
  30% {
    transform: scale(1.15);
  }
  50% {
    transform: scale(1.05);
  }
  70% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

/* 星星形成动画 */
@keyframes starFormation {
  0% {
    fill: transparent;
    fill-opacity: 0;
    transform: scale(0) rotate(-30deg);
  }
  50% {
    fill: #ffd54f;
    fill-opacity: 0.8;
    transform: scale(1.1) rotate(3deg);
  }
  75% {
    transform: scale(0.98) rotate(-1deg);
  }
  100% {
    fill: #ffd54f;
    fill-opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

/* 水波纹扩散动画 */
@keyframes rippleEffect {
  0% {
    transform: translate(-50%, -50%) scale(0.8);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(2.5);
    opacity: 0;
  }
}

/* 水泡聚合到中心的动画 - 每个水泡不同的轨迹 */
@keyframes bubbleToCenter1 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, 150%) scale(0.3);
  }
}

@keyframes bubbleToCenter2 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(100%, 0%) scale(0.3);
  }
}

@keyframes bubbleToCenter3 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(120%, -50%) scale(0.3);
  }
}

@keyframes bubbleToCenter4 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(100%, -100%) scale(0.3);
  }
}

@keyframes bubbleToCenter5 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -150%) scale(0.3);
  }
}

@keyframes bubbleToCenter6 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-100%, -100%) scale(0.3);
  }
}

@keyframes bubbleToCenter7 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-120%, -50%) scale(0.3);
  }
}

@keyframes bubbleToCenter8 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-100%, 0%) scale(0.3);
  }
}

/* 加载旋转动画 */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
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
  margin-top: 16px;
}

.blog-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.image-item {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.image-item:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.5);
}

.image-item img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .blog-images {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .blog-card {
    padding: 16px;
  }

  .blog-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .blog-title {
    font-size: 20px;
  }

  .blog-images {
    grid-template-columns: 1fr;
  }
}
</style>
