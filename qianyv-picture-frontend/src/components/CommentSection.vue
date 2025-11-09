<template>
  <div class="comment-section">
    <!-- 评论展开/收起按钮 -->
    <div class="comment-toggle" @click="toggleComments">
      <svg
        class="comment-icon"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <path
          d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <span class="comment-count">{{ displayedCount }} 条评论</span>
      <svg
        class="expand-icon"
        :class="{ expanded: isExpanded }"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <polyline points="6 9 12 15 18 9" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </div>

    <!-- 评论列表 -->
    <transition name="slide-fade">
      <div v-if="isExpanded" class="comment-list-container">
        <!-- 发表评论 -->
        <div class="comment-editor">
          <textarea
            v-model="newComment"
            class="comment-textarea"
            placeholder="写下你的评论..."
            rows="2"
            maxlength="64"
          ></textarea>
          <div class="editor-footer">
            <span class="char-counter">{{ trimmedComment.length }}/64</span>
            <div class="editor-actions">
              <span v-if="submitError" class="error-text">{{ submitError }}</span>
              <button
                class="submit-btn"
                :disabled="submitting || !trimmedComment"
                @click="submitComment"
              >
                {{ submitting ? '发送中...' : '发表评论' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <span>加载评论中...</span>
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading && comments.length === 0" class="empty-state">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path
              d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <p>还没有评论，快来抢沙发吧~</p>
        </div>

        <!-- 评论列表 -->
        <div v-else class="comment-list">
          <CommentItem
            v-for="comment in comments"
            :key="comment.id"
            :comment="comment"
            :blogId="blogId"
            @refresh="loadComments"
          />
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { listParentCommon, addCommon } from '@/api/commonController'
import CommentItem from './CommentItem.vue'

interface Props {
  blogId?: number | string
  commentCount?: number
}

const props = defineProps<Props>()

const isExpanded = ref(false)
const loading = ref(false)
const comments = ref<API.CommentQueryVO[]>([])
const submitting = ref(false)
const newComment = ref('')
const submitError = ref('')

// 切换评论展开/收起
const toggleComments = async () => {
  isExpanded.value = !isExpanded.value

  // 首次展开时加载评论
  if (isExpanded.value && comments.value.length === 0 && props.blogId) {
    await loadComments()
  }
}

// 加载评论
const loadComments = async () => {
  if (!props.blogId) return

  loading.value = true
  try {
    const response = await listParentCommon({ blogId: props.blogId })

    if (response.data?.code === 200 || response.data?.code === 0) {
      comments.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loading.value = false
  }
}

const displayedCount = computed(() => {
  if (comments.value.length > 0) {
    return comments.value.reduce((total, item) => total + 1 + (item.replies?.length || 0), 0)
  }
  return props.commentCount || 0
})

const trimmedComment = computed(() => newComment.value.trim())

const submitComment = async () => {
  if (!props.blogId || !trimmedComment.value || submitting.value) return
  submitError.value = ''
  submitting.value = true
  try {
    await addCommon({
      blogId: props.blogId,
      content: trimmedComment.value,
    })
    newComment.value = ''
    await loadComments()
  } catch (error: any) {
    console.error('发表评论失败:', error)
    submitError.value = error?.message || '发表评论失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

// 监听 blogId 变化，重置状态
watch(
  () => props.blogId,
  () => {
    isExpanded.value = false
    comments.value = []
  },
)

watch(
  () => newComment.value,
  () => {
    if (submitError.value) submitError.value = ''
  },
)
</script>

<style scoped>
.comment-section {
  width: 100%;
  margin-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding-top: 12px;
}

.comment-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
  user-select: none;
}

.comment-toggle:hover {
  background: rgba(0, 0, 0, 0.04);
}

.comment-icon {
  width: 18px;
  height: 18px;
  color: #666;
  flex-shrink: 0;
}

.comment-count {
  flex: 1;
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.expand-icon {
  width: 16px;
  height: 16px;
  color: #999;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.comment-list-container {
  margin-top: 12px;
  will-change: opacity, transform;
}

.comment-editor {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.comment-textarea {
  width: 100%;
  border: none;
  resize: vertical;
  min-height: 36px;
  font-size: 13px;
  line-height: 1.4;
  color: #333;
  background: transparent;
  outline: none;
  font-family: 'Microsoft YaHei', '微软雅黑', sans-serif;
}

.comment-textarea::placeholder {
  color: #999;
  font-family: 'Microsoft YaHei', '微软雅黑', sans-serif;
}

.editor-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.char-counter {
  font-size: 12px;
  color: #999;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.error-text {
  font-size: 12px;
  color: #ff5c5c;
}

.submit-btn {
  padding: 8px 18px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.submit-btn:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
}

/* 加载状态 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 32px;
  color: #666;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-top-color: #666;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #999;
}

.empty-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 动画 */
.slide-fade-enter-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}

.slide-fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
}

.slide-fade-enter-from {
  transform: translateY(-8px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateY(-4px);
  opacity: 0;
}
</style>
