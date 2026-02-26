<script setup lang="ts">
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
// @ts-ignore - cropperjs v1.6.2 类型在 bundler 模式下解析异常，使用 any 绕过
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

interface Props {
  visible: boolean
  imageFile: File | null
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'confirm', croppedBlob: Blob, croppedFile: File): void
  (e: 'cancel'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const imageRef = ref<HTMLImageElement | null>(null)
const previewUrl = ref('')
const cropping = ref(false)
let cropperInstance: any = null

// 监听弹窗打开，初始化裁剪器
watch(
  () => props.visible,
  async (val) => {
    if (val && props.imageFile) {
      // 先销毁旧实例
      destroyCropper()
      // 生成预览 URL
      previewUrl.value = URL.createObjectURL(props.imageFile)
      // 等待 DOM 更新（img 标签渲染 src）
      await nextTick()
      initCropper()
    } else {
      destroyCropper()
    }
  },
)

const initCropper = () => {
  const img = imageRef.value
  if (!img) return

  // blob URL 需要等图片真正加载完成
  if (img.complete && img.naturalWidth > 0) {
    createCropper(img)
  } else {
    img.onload = () => {
      createCropper(img)
    }
    img.onerror = () => {
      console.error('AvatarCropper: 图片加载失败')
    }
  }
}

const createCropper = (img: HTMLImageElement) => {
  const CropperConstructor = Cropper as any
  cropperInstance = new CropperConstructor(img, {
    aspectRatio: 1, // 固定正方形
    viewMode: 1,
    dragMode: 'move',
    autoCropArea: 0.8,
    cropBoxResizable: true,
    cropBoxMovable: true,
    guides: true,
    center: true,
    highlight: true,
    background: true,
    responsive: true,
    checkOrientation: false,
    minCropBoxWidth: 80,
    minCropBoxHeight: 80,
  })
}

const destroyCropper = () => {
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

// 确认裁剪
const handleConfirm = () => {
  if (!cropperInstance || cropping.value) return
  cropping.value = true

  const canvas = cropperInstance.getCroppedCanvas({
    width: 400,
    height: 400,
    imageSmoothingEnabled: true,
    imageSmoothingQuality: 'high',
  })

  canvas.toBlob(
    (blob: Blob | null) => {
      cropping.value = false
      if (blob) {
        // 生成裁剪后的 File 对象
        const fileName = props.imageFile?.name || 'avatar.png'
        const croppedFile = new File([blob], fileName, {
          type: 'image/png',
          lastModified: Date.now(),
        })
        emit('confirm', blob, croppedFile)
        handleClose()
      }
    },
    'image/png',
    1,
  )
}

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false)
  emit('cancel')
  // 延迟销毁，避免关闭动画中图片闪现
  setTimeout(() => {
    destroyCropper()
  }, 300)
}

// 旋转操作
const rotateLeft = () => cropperInstance?.rotate(-90)
const rotateRight = () => cropperInstance?.rotate(90)
// 翻转操作
const flipHorizontal = () => {
  if (!cropperInstance) return
  const data = cropperInstance.getData()
  cropperInstance.scaleX(data.scaleX === -1 ? 1 : -1)
}
const flipVertical = () => {
  if (!cropperInstance) return
  const data = cropperInstance.getData()
  cropperInstance.scaleY(data.scaleY === -1 ? 1 : -1)
}
// 重置
const resetCrop = () => cropperInstance?.reset()

onBeforeUnmount(() => {
  destroyCropper()
})
</script>

<template>
  <teleport to="body">
    <transition name="cropper-fade">
      <div v-if="visible" class="cropper-overlay" @click.self="handleClose">
        <div class="avatar-cropper-dialog">
          <!-- 头部 -->
          <div class="cropper-header">
            <h3 class="cropper-title">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                class="title-icon"
              >
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                <line x1="9" y1="3" x2="9" y2="21" />
                <line x1="15" y1="3" x2="15" y2="21" />
                <line x1="3" y1="9" x2="21" y2="9" />
                <line x1="3" y1="15" x2="21" y2="15" />
              </svg>
              裁剪头像
            </h3>
            <button class="close-btn" @click="handleClose">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>
          <!-- 裁剪区域 -->
          <div class="cropper-body">
            <div class="cropper-wrapper">
              <img ref="imageRef" :src="previewUrl" alt="裁剪预览" class="cropper-image" />
            </div>
          </div>

          <!-- 工具栏 -->
          <div class="cropper-toolbar">
            <button class="tool-btn" @click="rotateLeft" title="左旋转 90°">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="1 4 1 10 7 10" />
                <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
              </svg>
            </button>
            <button class="tool-btn" @click="rotateRight" title="右旋转 90°">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="23 4 23 10 17 10" />
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
              </svg>
            </button>
            <span class="tool-divider"></span>
            <button class="tool-btn" @click="flipHorizontal" title="水平翻转">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
                <polyline points="8 21 3 21 3 16" />
                <line x1="20" y1="4" x2="3" y2="21" />
              </svg>
            </button>
            <button class="tool-btn" @click="flipVertical" title="垂直翻转">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="17 1 21 5 17 9" />
                <path d="M3 5h18" />
                <polyline points="7 23 3 19 7 15" />
                <path d="M21 19H3" />
              </svg>
            </button>
            <span class="tool-divider"></span>
            <button class="tool-btn" @click="resetCrop" title="重置">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <polyline points="23 4 23 10 17 10" />
                <polyline points="1 20 1 14 7 14" />
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" />
              </svg>
            </button>
          </div>

          <!-- 底部按钮 -->
          <div class="cropper-footer">
            <button class="cancel-btn" @click="handleClose">取消</button>
            <button class="confirm-btn" :disabled="cropping" @click="handleConfirm">
              {{ cropping ? '处理中...' : '确认裁剪' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<style>
/* 使用非 scoped 样式：cropperjs 动态生成的 DOM 不带 scoped 属性，
   用 .cropper-overlay 父选择器限定作用域，防止全局污染 */
.cropper-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.cropper-overlay .avatar-cropper-dialog {
  background: #fff;
  border-radius: 20px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2);
  animation: cropperSlideIn 0.3s ease;
}

@keyframes cropperSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 头部 */
.cropper-overlay .cropper-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.cropper-overlay .cropper-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
}

.cropper-overlay .title-icon {
  width: 22px;
  height: 22px;
  color: #4a5568;
}

.cropper-overlay .close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #718096;
}

.cropper-overlay .close-btn svg {
  width: 18px;
  height: 18px;
}

.cropper-overlay .close-btn:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #e53e3e;
  transform: rotate(90deg);
}

/* 裁剪区域 */
.cropper-overlay .cropper-body {
  padding: 16px 24px;
  background: #f7fafc;
  overflow: hidden;
}

.cropper-overlay .cropper-wrapper {
  width: 100%;
  height: 400px;
}

/* cropperjs 要求: img 必须是 block，由 cropper 接管渲染 */
.cropper-overlay .cropper-image {
  display: block;
  max-width: 100%;
}

/* 工具栏 */
.cropper-overlay .cropper-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: #fafafa;
}

.cropper-overlay .tool-btn {
  width: 38px;
  height: 38px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: #fff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #4a5568;
}

.cropper-overlay .tool-btn svg {
  width: 18px;
  height: 18px;
}

.cropper-overlay .tool-btn:hover {
  background: rgba(138, 180, 248, 0.12);
  border-color: rgba(138, 180, 248, 0.4);
  color: #2c5282;
  transform: translateY(-1px);
}

.cropper-overlay .tool-btn:active {
  transform: translateY(0);
}

.cropper-overlay .tool-divider {
  width: 1px;
  height: 24px;
  background: rgba(0, 0, 0, 0.1);
  margin: 0 4px;
}

/* 底部按钮 */
.cropper-overlay .cropper-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.cropper-overlay .cancel-btn,
.cropper-overlay .confirm-btn {
  padding: 10px 24px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.cropper-overlay .cancel-btn {
  background: #f7fafc;
  color: #4a5568;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.cropper-overlay .cancel-btn:hover {
  background: #edf2f7;
}

.cropper-overlay .confirm-btn {
  background: linear-gradient(135deg, #8ab4f8 0%, #5a9cf8 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(90, 156, 248, 0.3);
}

.cropper-overlay .confirm-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(90, 156, 248, 0.4);
}

.cropper-overlay .confirm-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 过渡动画 */
.cropper-fade-enter-active,
.cropper-fade-leave-active {
  transition: opacity 0.25s ease;
}
.cropper-fade-enter-from,
.cropper-fade-leave-to {
  opacity: 0;
}

/* 覆盖 cropperjs 默认样式 */
.cropper-overlay .cropper-view-box {
  border-radius: 0;
  outline: 2px solid rgba(138, 180, 248, 0.8);
  outline-color: rgba(138, 180, 248, 0.8);
}

.cropper-overlay .cropper-face {
  background-color: transparent;
}

.cropper-overlay .cropper-dashed {
  border-color: rgba(255, 255, 255, 0.4);
}

.cropper-overlay .cropper-point {
  background-color: #5a9cf8;
  width: 8px !important;
  height: 8px !important;
  border-radius: 50%;
}

.cropper-overlay .cropper-line {
  background-color: rgba(138, 180, 248, 0.6);
}

/* cropperjs 内部遮罩层 - 注意不要与我们的 .avatar-cropper-dialog 冲突 */
.cropper-overlay .cropper-modal {
  background-color: rgba(0, 0, 0, 0.55);
}
</style>
