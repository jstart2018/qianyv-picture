<script setup lang="ts">
import { getUserAvatarText } from '@/utils'

interface User {
  nickname?: string
  avatar?: string
  introduction?: string
}

interface UserStats {
  postCount: number
  likeCount: number
  collectCount: number
}

interface Props {
  user: User | null
  stats: UserStats
}

defineProps<Props>()

const emit = defineEmits<{
  share: []
}>()

const handleShare = () => {
  emit('share')
}
</script>

<template>
  <div class="user-stats-card">
    <div class="user-avatar-large" :class="{ 'has-image': user?.avatar }">
      <img v-if="user?.avatar" :src="user.avatar" alt="avatar" />
      <span v-else>{{ getUserAvatarText(user?.nickname) }}</span>
    </div>
    <h3 class="user-nickname">{{ user?.nickname || '游客' }}</h3>
    <p class="user-intro">{{ user?.introduction || '这个人很懒，什么都没写~' }}</p>

    <div class="user-stats">
      <div class="stat-item">
        <div class="stat-value">{{ stats.postCount }}</div>
        <div class="stat-label">发布数</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.likeCount }}</div>
        <div class="stat-label">点赞数</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.collectCount }}</div>
        <div class="stat-label">收藏数</div>
      </div>
    </div>

    <button class="share-btn" @click="handleShare">
      <svg class="plane-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
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
</template>

<style scoped>
.user-stats-card {
  width: 95%;
  height: auto;
  max-height: 80%;
  margin-left: 8%;
  margin-right: 7%;
  margin-top: 60px;

  /* 半透明背景效果，匹配原始样式 */
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 2px dashed rgba(223, 231, 245, 0.5);
  border-radius: 22px;

  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding: 40px 32px;

  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  pointer-events: auto;
}

.user-stats-card:hover {
  border-color: rgba(138, 180, 248, 0.6);
  box-shadow: 0 12px 48px rgba(138, 180, 248, 0.15);
  transform: translateY(-4px);
}

.user-avatar-large {
  width: 140px;
  height: 140px;
  min-height: 140px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin-bottom: 20px;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  border: 4px solid rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.user-avatar-large.has-image {
  background: transparent;
  overflow: hidden;
}

.user-avatar-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar-large:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.5);
}

.user-nickname {
  font-size: 24px;
  font-weight: 700;
  color: #2c5282;
  margin-bottom: 10px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  text-align: center;
}

.user-intro {
  font-size: 15px;
  color: #4a5568;
  text-align: center;
  margin-bottom: 20px;
  line-height: 1.6;
  max-width: 90%;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  align-items: center;
  width: 100%;
  padding: 20px 0;
  background: rgba(138, 180, 248, 0.08);
  border-radius: 14px;
  gap: 16px;
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #667eea;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  white-space: nowrap;
}

.stat-divider {
  width: 1px;
  height: 44px;
  background: rgba(138, 180, 248, 0.2);
}

.share-btn {
  width: 100%;
  padding: 14px 32px;
  background: rgba(138, 180, 248, 0.1);
  color: #2d3748;
  border: 2px solid rgba(255, 255, 255, 0.7);
  border-radius: 18px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(138, 180, 248, 0.15);
}

.share-btn::before {
  content: '';
  position: absolute;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.4) 0%, transparent 70%);
  transition:
    width 0.5s ease,
    height 0.5s ease;
  transform: translate(-50%, -50%);
  left: 50%;
  top: 50%;
  z-index: 0;
}

.share-btn:hover::before {
  width: 300px;
  height: 300px;
}

.share-btn:hover {
  background: rgba(138, 180, 248, 0.2);
  border-color: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
  box-shadow:
    0 0 15px rgba(255, 255, 255, 0.5),
    0 0 30px rgba(138, 180, 248, 0.4),
    0 4px 12px rgba(138, 180, 248, 0.2);
}

.plane-icon {
  width: 20px;
  height: 20px;
  transition: transform 0.3s ease;
  position: relative;
  z-index: 1;
}

.share-btn:hover .plane-icon {
  transform: translateX(3px) translateY(-3px);
}
</style>
