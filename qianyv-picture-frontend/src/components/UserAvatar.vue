<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  // 用户昵称
  nickname?: string
  // 用户头像URL
  avatar?: string
  // 头像尺寸，支持预设或自定义数字（单位px）
  size?: 'small' | 'medium' | 'large' | 'xlarge' | number
  // 头像形状：circle-圆形，rounded-圆角矩形
  shape?: 'circle' | 'rounded'
  // 是否为透明幽灵模式（用于未登录时显示）
  isGhost?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  nickname: '',
  avatar: '',
  size: 'medium',
  shape: 'circle',
  isGhost: false,
})

// 计算头像文本（昵称首字母）
const avatarText = computed(() => {
  if (!props.nickname) return '?'
  return props.nickname.charAt(0).toUpperCase()
})

// 计算头像尺寸
const avatarSize = computed(() => {
  if (typeof props.size === 'number') {
    return `${props.size}px`
  }

  const sizeMap = {
    small: '32px',
    medium: '48px',
    large: '80px',
    xlarge: '140px',
  }

  return sizeMap[props.size] || sizeMap.medium
})

// 计算字体大小
const fontSize = computed(() => {
  if (typeof props.size === 'number') {
    return `${props.size * 0.4}px`
  }

  const fontSizeMap = {
    small: '14px',
    medium: '20px',
    large: '32px',
    xlarge: '56px',
  }

  return fontSizeMap[props.size] || fontSizeMap.medium
})

// 计算圆角
const borderRadius = computed(() => {
  if (props.shape === 'rounded') {
    return '14px' // 圆角矩形
  }
  return '50%' // 圆形
})
</script>

<template>
  <div
    class="user-avatar"
    :class="{ 'has-image': avatar, 'is-ghost': isGhost }"
    :style="{
      width: avatarSize,
      height: avatarSize,
      fontSize: fontSize,
      borderRadius: borderRadius,
    }"
  >
    <img v-if="avatar" :src="avatar" :alt="nickname" />
    <span v-else>{{ avatarText }}</span>
  </div>
</template>

<style scoped>
.user-avatar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #7cd3e2 0%, #e8a484 100%); /* 浅青色渐变 */
  box-shadow: 0 2px 8px rgba(103, 197, 196, 0.3);
  transition: all 0.3s ease;
  flex-shrink: 0;
  overflow: hidden;
  user-select: none;
}

.user-avatar.has-image {
  background: transparent;
}

/* 幽灵模式 - 透明背景，只显示问号 */
.user-avatar.is-ghost {
  background: transparent !important;
  border: 2px dashed rgba(26, 160, 193, 0.3);
  color: rgba(26, 160, 193, 0.5);
  box-shadow: none;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
