<script setup lang="ts">
import { ref } from 'vue'
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

// 当前正在输入的标签
const currentTagInputs = ref<string[]>([])

// 处理文件选择
const onFileSelect = (event: Event) => {
  handleFileSelect(event, props.spaceId)
  // 初始化标签输入数组
  currentTagInputs.value = new Array(uploadPreviews.value.length).fill('')
}

// 添加标签
const addTag = (index: number) => {
  const tag = currentTagInputs.value[index]?.trim()
  if (tag && !uploadPreviews.value[index].tags.includes(tag)) {
    uploadPreviews.value[index].tags.push(tag)
    currentTagInputs.value[index] = ''
  }
}

// 移除标签
const removeTag = (previewIndex: number, tagIndex: number) => {
  uploadPreviews.value[previewIndex].tags.splice(tagIndex, 1)
}

// 批量保存
const handleBatchUpload = async () => {
  const success = await handleBatchEdit(props.spaceId)
  if (success) {
    emit('uploaded')
    emit('close')
    currentTagInputs.value = []
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
  currentTagInputs.value = []
  emit('close')
}
</script>

<template>
  <transition name="modal-fade">
    <div v-if="show" class="modal-overlay" @click="closeUploadModal">
      <div class="upload-modal-container" @click.stop>
        <div class="modal-header">
          <h3>上传图片</h3>
          <button class="modal-close-btn" @click="closeUploadModal">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
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
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                <polyline points="17 8 12 3 7 8"></polyline>
                <line x1="12" y1="3" x2="12" y2="15"></line>
              </svg>
              <span>点击选择图片（支持多选）</span>
            </label>
          </div>

          <!-- 图片预览区域 -->
          <div v-if="uploadPreviews.length > 0" class="upload-previews">
            <div
              v-for="(preview, index) in uploadPreviews"
              :key="index"
              class="upload-preview-item"
              :class="{
                error: preview.status === 'error',
                uploading: preview.status === 'uploading',
              }"
            >
              <div class="preview-image-wrapper">
                <img :src="preview.url" :alt="preview.file.name" />
                <div v-if="preview.status === 'uploading'" class="upload-overlay">
                  <div class="upload-spinner"></div>
                  <span>上传中...</span>
                </div>
                <div v-if="preview.status === 'error'" class="upload-overlay error">
                  <span>{{ preview.errorMsg || '上传失败' }}</span>
                </div>
                <button class="remove-preview-btn" @click="removePreview(index)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                </button>
              </div>
              <div class="preview-info">
                <p class="file-name">{{ preview.file.name }}</p>

                <!-- 标签输入 -->
                <div class="tags-section">
                  <div class="tags-list">
                    <span v-for="(tag, tagIndex) in preview.tags" :key="tagIndex" class="tag-item">
                      {{ tag }}
                      <button class="tag-remove-btn" @click="removeTag(index, tagIndex)">×</button>
                    </span>
                  </div>
                  <input
                    v-model="currentTagInputs[index]"
                    type="text"
                    placeholder="输入标签后按回车添加"
                    class="tag-input"
                    @keydown.enter.prevent="addTag(index)"
                  />
                </div>

                <!-- 简介输入 -->
                <textarea
                  v-model="preview.introduction"
                  placeholder="添加图片简介（可选）"
                  class="introduction-input"
                  rows="2"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- 上传按钮 -->
          <div v-if="uploadPreviews.length > 0" class="upload-actions">
            <button class="cancel-btn" @click="closeUploadModal" :disabled="isUploading">
              取消
            </button>
            <button
              class="upload-btn"
              @click="handleBatchUpload"
              :disabled="isUploading || uploadPreviews.some((p) => p.status === 'uploading')"
            >
              {{
                isUploading
                  ? '保存中...'
                  : `上传 ${uploadPreviews.filter((p) => p.status === 'uploaded').length} 张图片`
              }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </transition>
</template>
