<script setup lang="ts">
import { useUpload } from '../../composables/useUpload'

interface Props {
  show: boolean
  spaceId: number | null
}

interface Emits {
  (e: 'close'): void
  (e: 'uploaded'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const {
  uploadPreviews,
  isUploading,
  handleFileSelect,
  removePreview,
  handleBatchEdit,
  clearPreviews,
} = useUpload()

// 处理文件选择
const onFileSelect = (event: Event) => {
  handleFileSelect(event, props.spaceId)
}

// 批量保存
const handleBatchUpload = async () => {
  const success = await handleBatchEdit(props.spaceId)
  if (success) {
    emit('uploaded')
    emit('close')
  }
}

// 关闭弹窗
const closeUploadModal = () => {
  if (isUploading.value) {
    if (!confirm('正在上传中，确定要关闭吗？')) {
      return
    }
  }
  clearPreviews()
  emit('close')
}
</script>

<template>
  <transition name="modal-fade">
    <div v-if="show" class="modal-overlay" @click="closeUploadModal">
      <div class="upload-modal-container" @click.stop>
        <div class="modal-header">
          <h3>📤 上传图片</h3>
          <button class="modal-close-btn" @click="closeUploadModal">✕</button>
        </div>

        <div class="upload-modal-body">
          <!-- 文件选择区域 -->
          <div class="file-select-area">
            <input
              type="file"
              id="fileInput"
              accept="image/*"
              multiple
              @change="onFileSelect"
              style="display: none"
            />
            <label for="fileInput" class="file-select-label">
              <span class="upload-icon">📁</span>
              <span>点击选择图片（支持多选）</span>
            </label>
          </div>

          <!-- 图片预览区域 -->
          <div v-if="uploadPreviews.length > 0" class="upload-previews">
            <div
              v-for="(preview, index) in uploadPreviews"
              :key="index"
              class="upload-preview-item"
            >
              <img :src="preview.url" :alt="preview.file.name" />
              <button class="remove-preview-btn" @click="removePreview(index)">✕</button>
              <div class="preview-info">
                <p class="file-name">{{ preview.file.name }}</p>
                <input
                  v-model="preview.tags"
                  type="text"
                  placeholder="添加标签（可选）"
                  class="tag-input"
                />
              </div>
            </div>
          </div>

          <!-- 上传按钮 -->
          <div class="upload-actions">
            <button class="cancel-btn" @click="closeUploadModal" :disabled="isUploading">
              取消
            </button>
            <button
              class="upload-btn"
              @click="handleBatchUpload"
              :disabled="uploadPreviews.length === 0 || isUploading"
            >
              {{ isUploading ? '上传中...' : `上传 ${uploadPreviews.length} 张图片` }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>
