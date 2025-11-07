<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  visible: boolean
  images: string[]
  currentIndex?: number
}

const props = withDefaults(defineProps<Props>(), {
  currentIndex: 0,
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'update:currentIndex': [value: number]
}>()

const localIndex = ref(props.currentIndex)

watch(
  () => props.currentIndex,
  (val) => {
    localIndex.value = val
  },
)

const close = () => {
  emit('update:visible', false)
}

const prev = () => {
  if (localIndex.value > 0) {
    localIndex.value--
    emit('update:currentIndex', localIndex.value)
  }
}

const next = () => {
  if (localIndex.value < props.images.length - 1) {
    localIndex.value++
    emit('update:currentIndex', localIndex.value)
  }
}

const selectImage = (index: number) => {
  localIndex.value = index
  emit('update:currentIndex', index)
}

const handleKeydown = (e: KeyboardEvent) => {
  if (!props.visible) return

  switch (e.key) {
    case 'Escape':
      close()
      break
    case 'ArrowLeft':
      prev()
      break
    case 'ArrowRight':
      next()
      break
  }
}

// 添加键盘事件监听
if (typeof window !== 'undefined') {
  window.addEventListener('keydown', handleKeydown)
}
</script>

<template>
  <Teleport to="body">
    <Transition name="preview-fade">
      <div v-if="visible" class="image-preview-mask" @click="close">
        <div class="preview-container" @click.stop>
          <!-- 关闭按钮 -->
          <button class="close-btn" @click="close">
            <span>✕</span>
          </button>

          <!-- 图片计数 -->
          <div class="image-counter">{{ localIndex + 1 }} / {{ images.length }}</div>

          <!-- 主图片 -->
          <div class="image-wrapper">
            <img :src="images[localIndex]" :alt="`Image ${localIndex + 1}`" class="preview-image" />
          </div>

          <!-- 左右切换按钮 -->
          <button v-if="localIndex > 0" class="nav-btn prev-btn" @click="prev">
            <span>‹</span>
          </button>
          <button v-if="localIndex < images.length - 1" class="nav-btn next-btn" @click="next">
            <span>›</span>
          </button>
          <!-- 缩略图列表 -->
          <div v-if="images.length > 1" class="thumbnail-list">
            <div
              v-for="(img, index) in images"
              :key="index"
              class="thumbnail-item"
              :class="{ active: index === localIndex }"
              @click="selectImage(index)"
            >
              <img :src="img" :alt="`Thumbnail ${index + 1}`" />
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.image-preview-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.preview-container {
  position: relative;
  width: 90vw;
  height: 90vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 28px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

.image-counter {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  color: white;
  font-size: 18px;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.5);
  padding: 8px 20px;
  border-radius: 20px;
  z-index: 10;
}

.image-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-bottom: 80px;
}

.preview-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 40px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.nav-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-50%) scale(1.1);
}

.prev-btn {
  left: 20px;
}

.next-btn {
  right: 20px;
}

.thumbnail-list {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  max-width: 80vw;
  overflow-x: auto;
  padding: 12px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.thumbnail-item {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid transparent;
  transition: all 0.3s ease;
}

.thumbnail-item:hover {
  transform: scale(1.1);
}

.thumbnail-item.active {
  border-color: #667eea;
  box-shadow: 0 0 16px rgba(102, 126, 234, 0.6);
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 过渡动画 */
.preview-fade-enter-active,
.preview-fade-leave-active {
  transition: opacity 0.3s ease;
}

.preview-fade-enter-from,
.preview-fade-leave-to {
  opacity: 0;
}
</style>
