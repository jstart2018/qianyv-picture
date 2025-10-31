<template>
  <Teleport to="body">
    <Transition name="modal-fade">
      <div v-if="visible" class="modal-overlay" @click="handleClose">
        <div class="modal-container" @click.stop>
          <!-- 关闭按钮 -->
          <button class="close-btn" @click="handleClose">
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

          <!-- 标题输入 -->
          <div class="form-group">
            <label class="form-label">
              <svg
                class="icon-svg"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M12 20h9M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              标题:
            </label>
            <input
              v-model="formData.title"
              type="text"
              class="form-input"
              placeholder="起什么标题呢....."
              maxlength="25"
            />
            <div class="char-count">剩余字数：{{ 25 - formData.title.length }}</div>
          </div>
          <!-- 内容输入 -->
          <div class="form-group">
            <label class="form-label">
              <svg
                class="icon-svg"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M14 2v6h6M16 13H8M16 17H8M10 9H8"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              陈述:
            </label>
            <textarea
              v-model="formData.content"
              class="form-textarea"
              placeholder="诉说什么呢..... (非必填)"
              maxlength="300"
              rows="5"
            ></textarea>
            <div class="char-count">剩余字数：{{ 300 - formData.content.length }}</div>
          </div>
          <!-- 图片上传区域 - 左右布局 -->
          <div class="upload-section-new">
            <!-- 左侧：图片序号列表 -->
            <div class="image-index-list">
              <!-- 添加按钮 - 始终在顶部 -->
              <button class="add-image-btn" @click="addNewImageSlot">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M12 5v14M5 12h14"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                  />
                </svg>
              </button>
              <!-- 图片序号项 -->
              <div
                v-for="(image, index) in uploadedImages"
                :key="index"
                class="index-item"
                :class="{ active: currentImageIndex === index }"
                @click="currentImageIndex = index"
              >
                第 {{ index + 1 }} 张
              </div>
            </div>
            <!-- 右侧：当前图片的上传/预览区域 -->
            <div class="image-upload-area">
              <div
                v-if="
                  uploadedImages.length > 0 &&
                  currentImageIndex < uploadedImages.length &&
                  currentImage
                "
                class="current-image-container"
              >
                <!-- 图片预览 -->
                <div
                  class="image-preview-box"
                  :class="{ 'drag-over': isDragging }"
                  @click="triggerFileInputForIndex(currentImageIndex)"
                  @drop.prevent="handleDrop"
                  @dragover.prevent="handleDragOver"
                  @dragleave.prevent="handleDragLeave"
                >
                  <img
                    v-if="currentImage.thumbUrl"
                    :src="currentImage.thumbUrl"
                    alt="预览图"
                    class="preview-image"
                  />
                  <div v-else class="upload-placeholder">
                    <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <!-- 云朵 - 灰色 -->
                      <path
                        d="M24 19c1.67 0 3.33-1.33 3.33-3.33s-1.33-3.33-3.33-3.33c0-3.33-2.67-6-6-6-2.67 0-4.93 1.73-5.73 4.13C9.47 11.2 8 13 8 15.33c0 2.53 2.13 4.67 4.67 4.67H24z"
                        stroke="#888"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        fill="rgba(200, 200, 200, 0.2)"
                      />
                    </svg>
                    <p>点击上传或拖拽图片到此处</p>
                    <p style="font-size: 12px; color: #999">支持批量上传</p>
                  </div>
                  <!-- 删除按钮 - 始终显示，右上角 -->
                  <button
                    class="remove-current-btn"
                    :disabled="uploadedImages.length === 1"
                    @click.stop="removeImage(currentImageIndex)"
                  >
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path
                        d="M5 12h14"
                        stroke="currentColor"
                        stroke-width="2"
                        stroke-linecap="round"
                      />
                    </svg>
                  </button>
                </div>

                <!-- 支持格式提示 -->
                <div class="format-hint">
                  「支持格式：png | jpg | jpeg 」<br />
                  「图片」大小不可超过【10MB】
                </div>
                <!-- 分类选择（仅当图片已上传时显示） -->
                <div v-if="currentImage.thumbUrl" class="category-section">
                  <label class="section-label">分类:</label>
                  <select v-model="currentImage.categoryId" class="category-select">
                    <option value="" disabled selected hidden>请选择分类</option>
                    <option v-for="category in categories" :key="category.id" :value="category.id">
                      {{ category.categoryName }}
                    </option>
                  </select>
                </div>

                <!-- 标签输入（仅当图片已上传时显示） -->
                <div v-if="currentImage.thumbUrl" class="tags-section">
                  <label class="section-label">标签:</label>
                  <div class="tags-input-wrapper">
                    <div class="tags-list">
                      <span
                        v-for="(tag, tagIndex) in currentImage.tags"
                        :key="tagIndex"
                        class="tag-item"
                      >
                        {{ tag }}
                        <button
                          class="tag-remove-btn"
                          @click="removeTag(currentImageIndex, tagIndex)"
                        >
                          ×
                        </button>
                      </span>
                    </div>
                    <input
                      v-model="currentTagInput"
                      type="text"
                      class="tag-input"
                      placeholder="输入标签后按回车添加（不填默认使用分类作为标签）"
                      @keydown.enter.prevent="addTag(currentImageIndex)"
                    />
                  </div>
                </div>
              </div>
              <!-- 初始上传提示（无图片时显示） -->
              <div
                v-else
                class="initial-upload"
                :class="{ 'drag-over': isDragging }"
                @click="triggerFileInputForIndex(0)"
                @drop.prevent="handleDrop"
                @dragover.prevent="handleDragOver"
                @dragleave.prevent="handleDragLeave"
              >
                <svg viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <!-- 云朵 - 灰色 -->
                  <path
                    d="M24 19c1.67 0 3.33-1.33 3.33-3.33s-1.33-3.33-3.33-3.33c0-3.33-2.67-6-6-6-2.67 0-4.93 1.73-5.73 4.13C9.47 11.2 8 13 8 15.33c0 2.53 2.13 4.67 4.67 4.67H24z"
                    stroke="#888"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    fill="rgba(200, 200, 200, 0.2)"
                  />
                </svg>
                <p>点击上传或拖拽图片到此处</p>
                <div class="upload-hint-text">
                  「支持格式：png | jpg | jpeg 」<br />
                  「支持批量上传」
                </div>
              </div>
            </div>
            <!-- 隐藏的文件输入 -->
            <input
              ref="fileInputRef"
              type="file"
              accept="image/png,image/jpg,image/jpeg"
              multiple
              style="display: none"
              @change="handleFileChange"
            />
          </div>

          <!-- 底部发布按钮 -->
          <div class="modal-footer">
            <button
              class="publish-btn"
              :disabled="!canPublish || isPublishing"
              @click="handlePublish"
            >
              <span v-if="isPublishing" class="loading-spinner"></span>
              <span v-else>发布</span>
              <svg
                v-if="!isPublishing"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { upload } from '@/api/pictureController'
import { addBlog } from '@/api/blogController'
import { listAll } from '@/api/picCategoryController'

interface Props {
  visible: boolean
}

interface UploadedImage {
  id?: number
  thumbUrl: string
  categoryId?: number
  tags: string[]
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const formData = ref({
  title: '',
  content: '',
})
const uploadedImages = ref<UploadedImage[]>([])
const currentImageIndex = ref(0)
const fileInputRef = ref<HTMLInputElement>()
const isPublishing = ref(false)
const categories = ref<any[]>([])
const currentTagInput = ref('')
const isDragging = ref(false)

// 草稿相关
const DRAFT_KEY = 'blog_upload_draft'
const DRAFT_EXPIRY_DAYS = 7
let saveTimer: ReturnType<typeof setTimeout> | null = null

// 当前选中的图片
const currentImage = computed(() => uploadedImages.value[currentImageIndex.value])

// 简单的消息提示函数
const showMessage = (type: 'success' | 'error' | 'loading', text: string) => {
  if (type === 'loading') {
    // 加载状态提示（当前简单实现为空操作）
  } else {
    alert(text)
  }
}

// 加载分类列表
const loadCategories = async () => {
  try {
    const response = await listAll()
    const result = response.data || response
    if (result && result.data) {
      categories.value = result.data
    }
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 是否可以发布
const canPublish = computed(() => {
  const hasValidImage = uploadedImages.value.some((img) => img.thumbUrl && img.id)
  return formData.value.title.trim() !== '' && hasValidImage
})

// 保存草稿到 localStorage
const saveDraft = () => {
  try {
    const draftData = {
      title: formData.value.title,
      content: formData.value.content,
      uploadedImages: uploadedImages.value,
      currentImageIndex: currentImageIndex.value,
      timestamp: Date.now(),
    }
    localStorage.setItem(DRAFT_KEY, JSON.stringify(draftData))
  } catch (error) {
    console.error('保存草稿失败:', error)
  }
}

// 防抖保存草稿
const debounceSaveDraft = () => {
  if (saveTimer) {
    clearTimeout(saveTimer)
  }
  saveTimer = setTimeout(() => {
    saveDraft()
  }, 1000) // 1秒防抖
}

// 加载草稿
const loadDraft = () => {
  try {
    const draftStr = localStorage.getItem(DRAFT_KEY)
    if (!draftStr) return null

    const draft = JSON.parse(draftStr)

    // 检查草稿是否过期（7天）
    const daysAgo = (Date.now() - draft.timestamp) / (1000 * 60 * 60 * 24)
    if (daysAgo > DRAFT_EXPIRY_DAYS) {
      clearDraft()
      return null
    }

    return draft
  } catch (error) {
    console.error('加载草稿失败:', error)
    return null
  }
}

// 恢复草稿数据
const restoreDraft = (draft: any) => {
  formData.value.title = draft.title || ''
  formData.value.content = draft.content || ''
  uploadedImages.value = draft.uploadedImages || []
  currentImageIndex.value = draft.currentImageIndex || 0
}

// 清除草稿
const clearDraft = () => {
  try {
    localStorage.removeItem(DRAFT_KEY)
  } catch (error) {
    console.error('清除草稿失败:', error)
  }
}

// 检查并提示恢复草稿
const checkAndRestoreDraft = () => {
  const draft = loadDraft()
  if (!draft) return

  // 检查是否有内容值得恢复
  const hasContent =
    draft.title || draft.content || (draft.uploadedImages && draft.uploadedImages.length > 0)
  if (!hasContent) {
    clearDraft()
    return
  }

  // 计算草稿保存时间
  const savedTime = new Date(draft.timestamp)
  const timeStr = savedTime.toLocaleString('zh-CN')

  const shouldRestore = confirm(
    `检测到未发布的草稿（保存于 ${timeStr}），是否恢复？\n\n` +
      `标题：${draft.title || '(空)'}\n` +
      `内容：${draft.content ? draft.content.substring(0, 30) + '...' : '(空)'}\n` +
      `图片：${draft.uploadedImages?.length || 0} 张`,
  )

  if (shouldRestore) {
    restoreDraft(draft)
  } else {
    clearDraft()
  }
}

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false)
}

// 添加新的图片槽位
const addNewImageSlot = () => {
  uploadedImages.value.push({
    thumbUrl: '',
    categoryId: '' as any, // 初始化为空字符串，显示占位文本
    tags: [],
  })
  currentImageIndex.value = uploadedImages.value.length - 1
}

// 触发指定索引的文件选择
const triggerFileInputForIndex = (index: number) => {
  if (index === 0 && uploadedImages.value.length === 0) {
    addNewImageSlot()
  }
  currentImageIndex.value = index
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  await handleMultipleFiles(files)

  // 清空input，允许重复选择同一文件
  target.value = ''
}

// 处理多个文件上传
const handleMultipleFiles = async (files: FileList) => {
  const fileArray = Array.from(files)

  // 如果当前位置为空，从当前位置开始上传
  let startIndex = currentImageIndex.value

  // 如果当前位置已有图片，则从下一个位置开始
  if (uploadedImages.value[startIndex]?.thumbUrl) {
    startIndex++
  }

  // 计算需要新增的槽位数量
  const existingEmptySlots = uploadedImages.value.length - startIndex
  const neededSlots = fileArray.length - existingEmptySlots

  // 如果需要，新增槽位
  for (let i = 0; i < neededSlots; i++) {
    uploadedImages.value.push({
      thumbUrl: '',
      categoryId: '' as any,
      tags: [],
    })
  }

  // 依次上传每个文件
  for (let i = 0; i < fileArray.length; i++) {
    const file = fileArray[i]
    const targetIndex = startIndex + i
    await uploadFile(file, targetIndex)
  }

  // 上传完成后，切换到第一个上传的图片
  currentImageIndex.value = startIndex
}

// 拖拽处理
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  isDragging.value = true
}

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  isDragging.value = false
}

const handleDrop = async (event: DragEvent) => {
  event.preventDefault()
  isDragging.value = false

  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return

  // 过滤只保留图片文件
  const imageFiles = Array.from(files).filter(
    (file) => file.type === 'image/png' || file.type === 'image/jpeg' || file.type === 'image/jpg',
  )

  if (imageFiles.length === 0) {
    showMessage('error', '请拖拽图片文件（支持 png、jpg、jpeg 格式）')
    return
  }

  // 创建 FileList 对象（使用 DataTransfer）
  const dataTransfer = new DataTransfer()
  imageFiles.forEach((file) => dataTransfer.items.add(file))

  await handleMultipleFiles(dataTransfer.files)
}

// 上传单个文件
const uploadFile = async (file: File, index: number) => {
  try {
    // 验证文件大小
    const maxSize = file.type.startsWith('image/') ? 30 * 1024 * 1024 : 500 * 1024 * 1024
    if (file.size > maxSize) {
      showMessage(
        'error',
        `文件 ${file.name} 过大，${file.type.startsWith('image/') ? '图片' : '视频'}最大支持 ${file.type.startsWith('image/') ? '30MB' : '500MB'}`,
      )
      return
    }

    // 创建 FormData
    const formData = new FormData()
    formData.append('file', file)

    // 调用上传接口，spaceId 传 undefined
    const response = await upload({ spaceId: undefined }, formData as any, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }) // 检查响应数据
    const result = response.data || response
    if (result && result.data && result.data.id && result.data.thumbUrl) {
      // 更新当前索引的图片信息
      uploadedImages.value[index] = {
        id: result.data.id,
        thumbUrl: result.data.thumbUrl,
        categoryId: uploadedImages.value[index]?.categoryId,
        tags: uploadedImages.value[index]?.tags || [],
      }
    } else {
      showMessage('error', `${file.name} 上传失败`)
    }
  } catch (error: any) {
    console.error('上传失败:', error)
    showMessage('error', `${file.name} 上传失败`)
  }
}

// 移除图片
const removeImage = (index: number) => {
  // 至少保留一个上传位置
  if (uploadedImages.value.length <= 1) {
    return
  }

  uploadedImages.value.splice(index, 1)

  // 调整当前索引
  if (currentImageIndex.value >= uploadedImages.value.length) {
    currentImageIndex.value = uploadedImages.value.length - 1
  }
}

// 添加标签
const addTag = (imageIndex: number) => {
  const tag = currentTagInput.value.trim()
  const image = uploadedImages.value[imageIndex]
  if (tag && image && !image.tags.includes(tag)) {
    image.tags.push(tag)
    currentTagInput.value = ''
  }
}

// 移除标签
const removeTag = (imageIndex: number, tagIndex: number) => {
  const image = uploadedImages.value[imageIndex]
  if (image) {
    image.tags.splice(tagIndex, 1)
  }
}

// 发布博客
const handlePublish = async () => {
  if (!canPublish.value || isPublishing.value) return
  try {
    isPublishing.value = true

    // 验证每张图片是否都选择了分类
    const imagesWithoutCategory = uploadedImages.value.filter(
      (img) => img.thumbUrl && img.id && (!img.categoryId || img.categoryId === ''),
    )

    if (imagesWithoutCategory.length > 0) {
      showMessage('error', '请为所有图片选择分类')
      isPublishing.value = false
      return
    }

    // 构造请求数据
    const pictureEditDTOList = uploadedImages.value
      .filter((img) => img.thumbUrl && img.id)
      .map((img) => ({
        id: img.id,
        categoryId: img.categoryId,
        tags: img.tags.length > 0 ? img.tags.join(',') : undefined,
      }))

    const blogData = {
      title: formData.value.title.trim(),
      content: formData.value.content.trim() || undefined,
      pictureEditDTOList,
    }

    const response = await addBlog(blogData) // 检查响应
    const result = response.data || response
    if (result && result.code === 0) {
      alert('发布成功！')
      // 清除草稿
      clearDraft()
      // 重置表单
      formData.value = { title: '', content: '' }
      uploadedImages.value = []
      currentImageIndex.value = 0
      currentTagInput.value = ''
      // 关闭弹窗
      emit('update:visible', false)
      // 通知父组件刷新
      emit('success')
    } else {
      showMessage('error', `发布失败`)
    }
  } catch (error: any) {
    console.error('发布失败:', error)
    showMessage('error', '发布失败，请重试')
  } finally {
    isPublishing.value = false
  }
}

// 监听弹窗显示/隐藏，控制页面滚动
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      document.body.style.overflow = 'hidden'
      // 加载分类列表
      loadCategories()
      // 检查并恢复草稿
      checkAndRestoreDraft()
    } else {
      document.body.style.overflow = ''
      // 关闭时保存草稿（如果有内容）
      const hasContent =
        formData.value.title || formData.value.content || uploadedImages.value.length > 0
      if (hasContent) {
        saveDraft()
      }
    }
  },
)

// 监听表单数据变化，自动保存草稿
watch(
  () => [formData.value.title, formData.value.content, uploadedImages.value.length],
  () => {
    if (props.visible) {
      debounceSaveDraft()
    }
  },
  { deep: true },
)

// 组件挂载时加载分类
onMounted(() => {
  if (props.visible) {
    loadCategories()
    checkAndRestoreDraft()
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 20px;
}

.modal-container {
  position: relative;
  width: 100%;
  max-width: 900px;
  max-height: 90vh;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.3),
    0 0 30px rgba(255, 255, 255, 0.4),
    inset 0 0 50px rgba(255, 255, 255, 0.5);
  padding: 28px;
  overflow-y: auto;
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
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
  width: 18px;
  height: 18px;
  color: #333;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.2);
  transform: rotate(90deg);
}

/* 表单组 */
.form-group {
  margin-bottom: 16px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.form-label .icon-svg {
  width: 20px;
  height: 20px;
  color: #000000;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border-radius: 12px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  transition: all 0.3s ease;
  resize: none;
  box-shadow:
    0 0 10px rgba(255, 255, 255, 0.3),
    inset 0 0 20px rgba(255, 255, 255, 0.6);
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.95);
  box-shadow:
    0 0 20px rgba(255, 255, 255, 0.6),
    0 0 30px rgba(255, 255, 255, 0.4),
    inset 0 0 20px rgba(255, 255, 255, 0.7);
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #000000;
  margin-top: 4px;
}

/* 上传区域 - 新布局 */
.upload-section-new {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  min-height: 400px;
}

/* 左侧：图片序号列表 */
.image-index-list {
  flex-shrink: 0;
  width: 100px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
}

/* 添加按钮 - 始终在顶部，圆形设计 */
.add-image-btn {
  width: 48px;
  height: 48px;
  padding: 0;
  background: rgba(128, 128, 128, 0.5);
  border: 3px solid rgba(255, 255, 255, 1);
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  order: -1; /* 确保始终在最前面 */
  flex-shrink: 0;
  box-shadow:
    0 0 20px rgba(113, 113, 114, 0.6),
    0 0 30px rgba(0, 0, 0, 0.4);
}

.add-image-btn svg {
  width: 24px;
  height: 24px;
  color: #ffffff;
}

.add-image-btn:hover {
  background: rgba(128, 128, 128, 0.7);
  border-color: rgba(255, 255, 255, 1);
  box-shadow:
    0 0 25px rgba(154, 203, 234, 0.7),
    0 0 35px rgba(174, 231, 241, 0.5);
  transform: scale(1.05);
}

/* 序号项 */
.index-item {
  padding: 12px;
  width: 90px;
  background: rgba(255, 255, 255, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 13px;
  color: #000000;
  font-weight: 500;
  box-shadow:
    0 0 10px rgba(255, 255, 255, 0.3),
    inset 0 0 15px rgba(255, 255, 255, 0.5);
}

.index-item:hover {
  background: rgba(255, 255, 255, 0.7);
  border-color: rgba(255, 255, 255, 0.9);
  box-shadow:
    0 0 15px rgba(255, 255, 255, 0.5),
    inset 0 0 20px rgba(255, 255, 255, 0.6);
}

.index-item.active {
  background: rgba(255, 255, 255, 1);
  border-color: #141414;
  color: #000000;
  font-weight: 600;
  box-shadow:
    0 0 10px rgba(0, 0, 0, 0.5),
    inset 0 0 20px rgba(255, 255, 255, 0.8);
}

/* 右侧：图片上传/预览区域 */
.image-upload-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.current-image-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.image-preview-box {
  position: relative;
  width: 100%;
  height: 300px;
  border-radius: 16px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.5);
  border: 2px dashed rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 0 15px rgba(255, 255, 255, 0.3),
    inset 0 0 30px rgba(255, 255, 255, 0.4);
}

.image-preview-box:hover {
  border-color: rgba(255, 255, 255, 0.9);
  box-shadow:
    0 0 25px rgba(255, 255, 255, 0.5),
    inset 0 0 35px rgba(255, 255, 255, 0.5);
}

.image-preview-box.drag-over {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  box-shadow:
    0 0 30px rgba(16, 185, 129, 0.5),
    inset 0 0 40px rgba(16, 185, 129, 0.3);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #666;
}

.upload-placeholder svg {
  width: 48px;
  height: 48px;
  color: #1aa0c1;
}

.upload-placeholder p {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.remove-current-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  z-index: 10;
}

.remove-current-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  background: rgba(200, 200, 200, 0.5);
}

.remove-current-btn:not(:disabled):hover {
  background: rgba(255, 85, 87, 0.9);
  transform: scale(1.1);
}

.remove-current-btn svg {
  width: 16px;
  height: 16px;
  color: #ff4757;
}

.remove-current-btn:not(:disabled):hover svg {
  color: white;
}

.remove-current-btn:disabled svg {
  color: #999;
}

.format-hint {
  font-size: 11px;
  color: #000;
  text-align: center;
  line-height: 1.5;
}

/* 分类选择 */
.category-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.category-select {
  width: 25%;
  padding: 10px 12px;
  border-radius: 10px;
  border: 2px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333;
}

.category-select:invalid {
  color: #999;
}

.category-select option {
  color: #333;
}

.category-select:focus {
  outline: none;
}

.category-hint {
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

/* 标签输入 */
.tags-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tags-input-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 10px;
  border: 2px solid rgba(200, 200, 200, 0.5);
  background: rgba(240, 240, 240, 0.8);
  min-height: 36px;
  box-shadow:
    0 0 10px rgba(200, 200, 200, 0.2),
    inset 0 0 15px rgba(240, 240, 240, 0.5);
  transition: all 0.3s ease;
}

.tags-input-wrapper:focus-within {
  outline: none;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(16, 185, 129, 0.15);
  border: 1px solid rgba(16, 185, 129, 0.4);
  border-radius: 16px;
  font-size: 12px;
  color: #10b981;
  font-weight: 500;
}

.tag-remove-btn {
  border: none;
  background: none;
  color: #10b981;
  font-size: 16px;
  line-height: 1;
  cursor: pointer;
  padding: 0;
  margin: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.tag-remove-btn:hover {
  color: #ff4757;
  transform: scale(1.2);
}

.tag-input {
  flex: 1;
  min-width: 120px;
  border: none;
  background: none;
  outline: none;
  font-size: 13px;
  padding: 4px;
}

.tag-input::placeholder {
  color: #999;
}

/* 初始上传提示 */
.initial-upload {
  width: 100%;
  height: 400px;
  border: 2px dashed rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.5);
  box-shadow:
    0 0 15px rgba(255, 255, 255, 0.3),
    inset 0 0 30px rgba(255, 255, 255, 0.4);
}

.initial-upload:hover {
  border-color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.6);
  box-shadow:
    0 0 25px rgba(255, 255, 255, 0.5),
    inset 0 0 35px rgba(255, 255, 255, 0.5);
}

.initial-upload.drag-over {
  border-color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  box-shadow:
    0 0 30px rgba(16, 185, 129, 0.5),
    inset 0 0 40px rgba(16, 185, 129, 0.3);
}

.initial-upload svg {
  width: 64px;
  height: 64px;
  color: #1aa0c1;
}

.initial-upload p {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.upload-hint-text {
  font-size: 12px;
  color: #999;
}

/* 底部按钮 */
.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.publish-btn {
  padding: 14px 40px;
  border-radius: 16px;
  border: none;
  background: linear-gradient(135deg, #047a52 0%, #6eecc4 100%);
  color: white;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.publish-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.4);
}

.publish-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.publish-btn svg {
  width: 18px;
  height: 18px;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 过渡动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* 隐藏滚动条但保留滚动功能 */
.modal-container::-webkit-scrollbar {
  display: none; /* 隐藏滚动条 */
}

.modal-container {
  -ms-overflow-style: none; /* IE和Edge */
  scrollbar-width: none; /* Firefox */
}
</style>
