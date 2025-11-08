<template>
  <div class="comment-item">
    <!-- 父级评论 -->
    <div class="comment-main">
      <!-- 用户头像 -->
      <div class="comment-avatar">
        <img
          v-if="comment.userInfoVO?.avatar"
          :src="comment.userInfoVO.avatar"
          :alt="comment.userInfoVO.nickname"
        />
        <div v-else class="avatar-placeholder">
          {{ comment.userInfoVO?.nickname?.charAt(0) || '匿' }}
        </div>
      </div>

      <!-- 评论内容 -->
      <div class="comment-body">
        <!-- 用户信息 -->
        <div class="comment-header">
          <span class="comment-author">{{ comment.userInfoVO?.nickname || '匿名用户' }}</span>
          <span v-if="comment.userInfoVO?.tag" class="user-tag">{{ comment.userInfoVO.tag }}</span>
        </div>

        <!-- 评论文本 -->
        <div class="comment-content">{{ comment.content }}</div>

        <!-- 评论操作栏 -->
        <div class="comment-actions">
          <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
          <button class="action-button">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path
                d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <span>{{ comment.likeCount || 0 }}</span>
          </button>
          <button class="action-button" @click="openReplyEditor(comment.userInfoVO)">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path
                d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <span>回复</span>
          </button>
        </div>

        <!-- 回复编辑器 -->
        <transition name="replies-fade">
          <div v-if="showReplyInput" class="reply-editor">
            <textarea
              v-model="replyContent"
              class="reply-textarea"
              rows="2"
              maxlength="64"
              :placeholder="replyPlaceholder"
            ></textarea>
            <div class="reply-footer">
              <span class="char-counter">{{ trimmedReply.length }}/64</span>
              <div class="editor-actions">
                <button class="ghost-btn" @click="cancelReply" :disabled="submittingReply">
                  取消
                </button>
                <button
                  class="primary-btn"
                  :disabled="submittingReply || !trimmedReply"
                  @click="submitReply"
                >
                  {{ submittingReply ? '发送中...' : '发送回复' }}
                </button>
              </div>
            </div>
            <p v-if="replyError" class="error-text">{{ replyError }}</p>
          </div>
        </transition>

        <!-- 查看回复按钮 -->
        <div
          v-if="comment.replies && comment.replies.length > 0"
          class="toggle-replies"
          @click="toggleReplies"
        >
          <svg
            class="toggle-icon"
            :class="{ expanded: showReplies }"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
          >
            <polyline points="6 9 12 15 18 9" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <span>{{ showReplies ? '收起' : '查看' }}{{ comment.replies.length }}条回复</span>
        </div>

        <!-- 回复列表 -->
        <transition name="replies-fade">
          <div v-if="showReplies && comment.replies && comment.replies.length > 0" class="replies">
            <CommentReply
              v-for="reply in comment.replies"
              :key="reply.id"
              :reply="reply"
              :blogId="blogId"
              @reply="handleReplyFromChild"
            />
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { addCommon } from '@/api/commonController'
import CommentReply from './CommentReply.vue'

interface Props {
  comment: API.CommentQueryVO
  blogId?: number
}

const props = defineProps<Props>()
const emit = defineEmits(['refresh'])

const showReplies = ref(false)
const showReplyInput = ref(false)
const replyContent = ref('')
const submittingReply = ref(false)
const replyError = ref('')
const replyTarget = ref<API.UserInfoVO | undefined>(undefined)

// 切换回复显示
const toggleReplies = () => {
  showReplies.value = !showReplies.value
}

const trimmedReply = computed(() => replyContent.value.trim())

const replyPlaceholder = computed(() => {
  const name = replyTarget.value?.nickname || props.comment.userInfoVO?.nickname || 'TA'
  return `回复 @${name} ...`
})

const openReplyEditor = (target?: API.UserInfoVO) => {
  // 如果已经展开，则收起
  if (showReplyInput.value) {
    cancelReply()
    return
  }

  replyTarget.value = target
  replyContent.value = ''
  replyError.value = ''
  showReplyInput.value = true
}

const handleReplyFromChild = (targetComment: API.CommentQueryVO) => {
  replyTarget.value = targetComment.userInfoVO
  replyContent.value = ''
  replyError.value = ''
  showReplyInput.value = true
}

const cancelReply = () => {
  showReplyInput.value = false
  replyContent.value = ''
  replyTarget.value = undefined
  replyError.value = ''
}

const submitReply = async () => {
  if (!props.blogId || !props.comment.id || !trimmedReply.value || submittingReply.value) return
  submittingReply.value = true
  replyError.value = ''
  try {
    await addCommon({
      blogId: props.blogId,
      parentId: props.comment.id,
      content: trimmedReply.value,
      replyToUserId: replyTarget.value?.id || props.comment.userInfoVO?.id,
    })
    cancelReply()
    emit('refresh')
  } catch (error: any) {
    console.error('回复失败:', error)
    replyError.value = error?.message || '回复失败，请稍后再试'
  } finally {
    submittingReply.value = false
  }
}

watch(
  () => props.comment.id,
  () => {
    cancelReply()
    showReplies.value = false
  },
)

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 小于1分钟
  if (diff < 60 * 1000) {
    return '刚刚'
  }
  // 小于1小时
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  }
  // 小于1天
  if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  }
  // 小于7天
  if (diff < 7 * 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (24 * 60 * 60 * 1000))}天前`
  }

  // 格式化为日期
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.comment-item {
  width: 100%;
}

.comment-main {
  display: flex;
  gap: 12px;
}

/* 头像 */
.comment-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

/* 评论主体 */
.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.user-tag {
  padding: 2px 8px;
  border-radius: 4px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 11px;
  font-weight: 500;
}

.comment-content {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 8px;
  word-break: break-word;
}

/* 操作栏 */
.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.reply-editor {
  margin-top: 12px;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 6px;
  will-change: opacity, transform;
}

.reply-textarea {
  width: 100%;
  min-height: 36px;
  border: none;
  outline: none;
  resize: vertical;
  font-size: 13px;
  line-height: 1.4;
  color: #333;
  background: transparent;
  font-family: 'Microsoft YaHei', '微软雅黑', sans-serif;
}

.reply-textarea::placeholder {
  color: #999;
  font-family: 'Microsoft YaHei', '微软雅黑', sans-serif;
}

.reply-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}

.char-counter {
  font-size: 11px;
  color: #999;
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ghost-btn,
.primary-btn {
  border: none;
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ghost-btn {
  background: rgba(0, 0, 0, 0.05);
  color: #666;
}

.ghost-btn:hover {
  background: rgba(0, 0, 0, 0.08);
}

.primary-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.35);
}

.primary-btn:hover {
  transform: translateY(-1px);
}

.primary-btn:disabled,
.ghost-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.error-text {
  font-size: 11px;
  color: #ff6b6b;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  color: #666;
  transition: all 0.2s ease;
}

.action-button:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #333;
}

.action-icon {
  width: 14px;
  height: 14px;
  stroke-width: 2;
}

/* 查看回复 */
.toggle-replies {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: rgba(102, 126, 234, 0.08);
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #667eea;
  font-weight: 500;
  transition: all 0.2s ease;
  user-select: none;
}

.toggle-replies:hover {
  background: rgba(102, 126, 234, 0.15);
}

.toggle-icon {
  width: 14px;
  height: 14px;
  stroke-width: 2;
  transition: transform 0.3s ease;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

/* 回复列表 */
.replies {
  margin-top: 12px;
  padding-left: 0;
  border-left: 2px solid rgba(0, 0, 0, 0.06);
  padding-left: 16px;
  will-change: opacity, transform;
}

/* 动画 */
.replies-fade-enter-active {
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
  overflow: hidden;
}

.replies-fade-leave-active {
  transition:
    opacity 0.2s ease,
    transform 0.2s ease;
  overflow: hidden;
}

.replies-fade-enter-from {
  transform: translateY(0);
  opacity: 0;
}

.replies-fade-leave-to {
  transform: translateY(0);
  opacity: 0;
}
</style>
