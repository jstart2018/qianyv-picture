<template>
  <Teleport to="body">
    <Transition name="avatar-preview">
      <div v-if="visible" class="avatar-preview-overlay" @click="close">
        <div class="avatar-preview-container" @click.stop>
          <button class="close-btn" @click="close">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M18 6L6 18M6 6l12 12"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>
          <div class="avatar-preview-content">
            <!-- 只显示头像图片，不显示用户名和简介 -->
            <div class="avatar-image-wrapper">
              <img :src="avatarUrl" :alt="nickname || '用户头像'" />
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { watch } from 'vue'

interface Props {
  visible: boolean
  avatarUrl: string // 必需参数，因为只在有头像时才显示预览
  nickname?: string
  introduction?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const close = () => {
  emit('update:visible', false)
}

// 监听 visible 变化，控制 body 滚动
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
  },
)

// ESC 键关闭
const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.visible) {
    close()
  }
}

// 添加和移除键盘事件监听
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      document.addEventListener('keydown', handleKeyDown)
    } else {
      document.removeEventListener('keydown', handleKeyDown)
    }
  },
  { immediate: true },
)
</script>

<style scoped>
.avatar-preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.avatar-preview-container {
  position: relative;
  max-width: 600px;
  max-height: 80vh;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  cursor: default;
  animation: scaleIn 0.3s ease-out;
}

@keyframes scaleIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.1);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
}

.close-btn svg {
  width: 20px;
  height: 20px;
  color: #333;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.2);
  transform: rotate(90deg);
}

.avatar-preview-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-image-wrapper {
  width: 400px;
  height: 400px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  border: 4px solid rgba(255, 255, 255, 0.9);
}

.avatar-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 过渡动画 */
.avatar-preview-enter-active,
.avatar-preview-leave-active {
  transition: opacity 0.3s ease;
}

.avatar-preview-enter-from,
.avatar-preview-leave-to {
  opacity: 0;
}

.avatar-preview-enter-active .avatar-preview-container,
.avatar-preview-leave-active .avatar-preview-container {
  transition: transform 0.3s ease;
}

.avatar-preview-enter-from .avatar-preview-container,
.avatar-preview-leave-to .avatar-preview-container {
  transform: scale(0.9);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .avatar-preview-container {
    max-width: 90%;
    padding: 30px 20px;
  }

  .avatar-image-wrapper {
    width: 250px;
    height: 250px;
  }
}
</style>
