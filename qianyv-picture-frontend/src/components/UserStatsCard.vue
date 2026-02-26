<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import UserAvatar from './UserAvatar.vue'

const router = useRouter()

interface User {
  id?: number
  nickname?: string
  avatar?: string
  introduction?: string
}

interface UserStats {
  postCount: number
  likeCount: number
  collectCount: number
  downloadCount?: number
  fansCount?: number
}

interface Props {
  user: User | null
  stats: UserStats
}

const props = defineProps<Props>()

const emit = defineEmits<{
  share: []
}>()

// 是否已登录
const isLoggedIn = computed(() => !!props.user)

const handleShare = () => {
  emit('share')
}

// 点击头像跳转到用户详情页
const handleAvatarClick = () => {
  if (props.user?.id) {
    router.push(`/user/${props.user.id}`)
  }
}

// 格式化数字（大于 10000 显示 x.xw）
const formatNumber = (num: number = 0) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toString()
}
</script>

<template>
  <div class="user-stats-card">
    <div class="avatar-container" :class="{ clickable: isLoggedIn }" @click="handleAvatarClick">
      <UserAvatar
        :nickname="isLoggedIn ? user?.nickname : undefined"
        :avatar="isLoggedIn ? user?.avatar : undefined"
        :is-ghost="!isLoggedIn"
        size="large"
        class="user-avatar-large"
      />
    </div>
    <h3 class="user-nickname">{{ isLoggedIn ? user?.nickname || '游客' : '请先登录' }}</h3>
    <p class="user-intro">
      {{
        isLoggedIn ? user?.introduction || '这个人很懒，什么都没写~' : '登录后可以查看您的个人信息'
      }}
    </p>

    <!-- 用户元数据统计 - 网格布局 -->
    <div class="user-metadata">
      <div class="metadata-grid">
        <div class="metadata-item">
          <div class="metadata-value">
            {{ isLoggedIn ? formatNumber(stats.downloadCount || 0) : '-' }}
          </div>
          <div class="metadata-label">获载量</div>
        </div>
        <div class="metadata-item">
          <div class="metadata-value">{{ isLoggedIn ? formatNumber(stats.likeCount) : '-' }}</div>
          <div class="metadata-label">获赞</div>
        </div>
        <div class="metadata-item">
          <div class="metadata-value">
            {{ isLoggedIn ? formatNumber(stats.collectCount) : '-' }}
          </div>
          <div class="metadata-label">获收藏</div>
        </div>
        <div class="metadata-item">
          <div class="metadata-value">
            {{ isLoggedIn ? formatNumber(stats.fansCount || 0) : '-' }}
          </div>
          <div class="metadata-label">粉丝数</div>
        </div>
        <div class="metadata-item">
          <div class="metadata-value">{{ isLoggedIn ? formatNumber(stats.postCount) : '-' }}</div>
          <div class="metadata-label">发布数</div>
        </div>
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
      {{ isLoggedIn ? '分享' : '登录后分享' }}
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

/* 头像容器 - 添加点击和悬停效果 */
.avatar-container {
  cursor: pointer;
  margin-bottom: 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: inline-block;
}

.avatar-container.clickable:hover {
  transform: translateY(-6px) scale(1.05);
}

.avatar-container.clickable:active {
  transform: translateY(-3px) scale(1.02);
}

.user-avatar-large {
  border: none;
  box-shadow:
    0 2px 8px rgba(230, 230, 230, 0.8),
    0 4px 16px rgba(0, 0, 0, 0.5);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
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
  margin-bottom: 24px;
  line-height: 1.6;
  max-width: 90%;
}

/* 用户元数据统计区域 */
.user-metadata {
  width: 100%;
  margin-bottom: 24px;
}

.metadata-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  width: 100%;
  justify-content: center;
  padding: 0 10px;
}

.metadata-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 88px;
  height: 88px;
  background: transparent;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
  box-shadow:
    0 0 0 1px rgba(138, 180, 248, 0.15),
    0 0 12px rgba(255, 255, 255, 0.4),
    0 0 20px rgba(255, 255, 255, 0.25),
    0 4px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  flex-shrink: 0;
}

.metadata-item:hover {
  transform: translateY(-3px) scale(1.08);
  box-shadow:
    0 0 0 1px rgba(138, 180, 248, 0.3),
    0 0 15px rgba(255, 255, 255, 0.6),
    0 0 28px rgba(255, 255, 255, 0.4),
    0 6px 16px rgba(0, 0, 0, 0.08);
}

.metadata-value {
  font-size: 20px;
  font-weight: 700;
  color: #000000;
  line-height: 1;
}

.metadata-label {
  font-size: 11px;
  color: #000000;
  white-space: nowrap;
  font-weight: 500;
  opacity: 0.75;
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
