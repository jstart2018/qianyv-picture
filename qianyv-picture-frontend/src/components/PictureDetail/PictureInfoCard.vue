<script setup lang="ts">
import { formatFileSize, formatDate } from '@/utils/format'

interface Picture {
  category?: string
  picWidth?: number
  picHeight?: number
  picFormat?: string
  picSize?: number
  downloadCount?: number
  favourCount?: number
  createTime?: string
  user?: {
    userAvatar?: string
    userName?: string
    followCount?: number
    shareCount?: number
    likeCount?: number
    userProfile?: string
  }
}

interface Props {
  picture: Picture
  downloading?: boolean
  isCollected?: boolean
  collecting?: boolean
}

interface Emits {
  (e: 'download'): void
  (e: 'collect'): void
}

defineProps<Props>()
const emit = defineEmits<Emits>()
</script>

<template>
  <div class="info-card">
    <!-- 图片基本信息卡片 -->
    <div class="basic-info-card">
      <div class="info-row">
        <div class="info-item">
          <span class="info-label">分类：</span>
          <span class="info-value">{{ picture.category || '-' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">分辨率：</span>
          <span class="info-value">{{ picture.picWidth }}x{{ picture.picHeight }}</span>
        </div>
      </div>

      <div class="info-row">
        <div class="info-item">
          <span class="info-label">格式：</span>
          <span class="info-value">{{ picture.picFormat || 'PNG' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">大小：</span>
          <span class="info-value">{{ formatFileSize(picture.picSize) }}</span>
        </div>
      </div>

      <div class="info-row">
        <div class="info-item">
          <span class="info-label">下载量：</span>
          <span class="info-value">{{ picture.downloadCount || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">收藏量：</span>
          <span class="info-value">{{ picture.favourCount || 0 }}</span>
        </div>
      </div>

      <div class="info-row center">
        <div class="info-item">
          <span class="info-label">发布时间：</span>
          <span class="info-value">{{ formatDate(picture.createTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 作者信息区域 -->
    <div class="author-section">
      <div class="author-avatar">
        <img :src="picture.user?.userAvatar || '/default-avatar.png'" alt="作者头像" />
      </div>
      <div class="author-info">
        <div class="author-name">{{ picture.user?.userName || '匿名用户' }}</div>
        <div class="author-stats">
          <span class="stat-item">粉丝 {{ picture.user?.followCount || 0 }}</span>
          <span class="stat-divider">|</span>
          <span class="stat-item">分享 {{ picture.user?.shareCount || 0 }}</span>
          <span class="stat-divider">|</span>
          <span class="stat-item">获赞 {{ picture.user?.likeCount || 0 }}</span>
        </div>
        <div class="author-bio">{{ picture.user?.userProfile || '暂无简介~' }}</div>
      </div>
    </div>

    <!-- 分割线 -->
    <div class="divider"></div>

    <!-- 操作按钮区域 -->
    <div class="action-buttons">
      <button class="download-btn" @click="emit('download')" :disabled="downloading">
        <span class="btn-text">{{ downloading ? '下载中...' : '下载' }}</span>
        <svg
          class="btn-icon-svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
          <polyline points="7 10 12 15 17 10"></polyline>
          <line x1="12" y1="15" x2="12" y2="3"></line>
        </svg>
      </button>

      <button
        class="collect-btn"
        :class="{ collected: isCollected }"
        @click="emit('collect')"
        :disabled="collecting"
      >
        <!-- 水波纹效果容器 -->
        <span class="ripple-container">
          <span class="ripple ripple-1"></span>
          <span class="ripple ripple-2"></span>
          <span class="ripple ripple-3"></span>
        </span>

        <!-- 星星图标 -->
        <svg
          class="star-icon"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <polygon
            points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
          ></polygon>
        </svg>

        <span class="btn-text">{{ isCollected ? '已收藏' : '收藏' }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.info-card {
  width: 100%;
  height: 500px;
  background: rgb(212, 230, 218);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  overflow-y: auto;
}

/* 基本信息卡片 */
.basic-info-card {
  margin-bottom: 24px;
}

.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.info-row.center {
  grid-template-columns: 1fr;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.6);
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 600;
}

/* 作者信息区域 */
.author-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
}

.author-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-info {
  flex: 1;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.9);
  margin-bottom: 6px;
}

.author-stats {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 8px;
}

.stat-item {
  font-weight: 500;
}

.stat-divider {
  color: rgba(0, 0, 0, 0.3);
}

.author-bio {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.65);
  line-height: 1.5;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0.1), rgba(0, 0, 0, 0.05), rgba(0, 0, 0, 0.1));
  margin: 20px 0;
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  gap: 12px;
}

.download-btn,
.collect-btn {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  border: none;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.download-btn {
  background: linear-gradient(135deg, rgba(138, 180, 248, 0.9), rgba(107, 157, 232, 0.9));
  color: white;
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.3);
}

.download-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(138, 180, 248, 0.4);
}

.download-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.collect-btn {
  background: white;
  color: rgba(138, 180, 248, 0.9);
  border: 2px solid rgba(138, 180, 248, 0.3);
}

.collect-btn.collected {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.9), rgba(255, 193, 7, 0.9));
  color: white;
  border-color: rgba(255, 215, 0, 0.5);
}

.collect-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  border-color: rgba(138, 180, 248, 0.6);
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.2);
}

.collect-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-icon-svg {
  width: 18px;
  height: 18px;
}

.star-icon {
  width: 18px;
  height: 18px;
}

.collect-btn.collected .star-icon {
  fill: white;
}

/* 水波纹效果 */
.ripple-container {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 215, 0, 0.3);
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
}

.collect-btn.collected .ripple {
  animation: ripple-animation 1.5s ease-out infinite;
}

.collect-btn.collected .ripple-2 {
  animation-delay: 0.5s;
}

.collect-btn.collected .ripple-3 {
  animation-delay: 1s;
}

@keyframes ripple-animation {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }
  100% {
    transform: translate(-50%, -50%) scale(8);
    opacity: 0;
  }
}
</style>
