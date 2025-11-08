<template>
  <div class="comment-reply">
    <!-- 回复评论 -->
    <div class="reply-main">
      <!-- 用户头像 -->
      <div class="reply-avatar">
        <img
          v-if="reply.userInfoVO?.avatar"
          :src="reply.userInfoVO.avatar"
          :alt="reply.userInfoVO.nickname"
        />
        <div v-else class="avatar-placeholder">
          {{ reply.userInfoVO?.nickname?.charAt(0) || '匿' }}
        </div>
      </div>

      <!-- 回复内容 -->
      <div class="reply-body">
        <!-- 用户信息 -->
        <div class="reply-header">
          <span class="reply-author">{{ reply.userInfoVO?.nickname || '匿名用户' }}</span>
          <span v-if="reply.userInfoVO?.tag" class="user-tag">{{ reply.userInfoVO.tag }}</span>
        </div>

        <!-- 回复文本 -->
        <div class="reply-content">
          <template v-if="reply.replyToUser?.nickname">
            <span class="reply-to">回复 @{{ reply.replyToUser.nickname }}：</span>
          </template>
          {{ reply.content }}
        </div>

        <!-- 回复操作栏 -->
        <div class="reply-actions">
          <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
          <button class="action-button">
            <svg class="action-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path
                d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <span>{{ reply.likeCount || 0 }}</span>
          </button>
          <button class="action-button" @click="emit('reply', reply)">
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
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  reply: API.CommentQueryVO
  blogId?: number
}

const props = defineProps<Props>()
const emit = defineEmits(['reply'])

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
.comment-reply {
  width: 100%;
  margin-bottom: 12px;
}

.comment-reply:last-child {
  margin-bottom: 0;
}

.reply-main {
  display: flex;
  gap: 10px;
}

/* 头像 */
.reply-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
}

.reply-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 13px;
}

/* 回复主体 */
.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.reply-author {
  font-weight: 600;
  font-size: 13px;
  color: #333;
}

.user-tag {
  padding: 2px 6px;
  border-radius: 3px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 10px;
  font-weight: 500;
}

.reply-content {
  font-size: 13px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 6px;
  word-break: break-word;
}

.reply-to {
  color: #777;
  margin-right: 4px;
}

/* 操作栏 */
.reply-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.action-button {
  display: flex;
  align-items: center;
  gap: 3px;
  padding: 3px 6px;
  background: transparent;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 11px;
  color: #666;
  transition: all 0.2s ease;
}

.action-button:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #333;
}

.action-icon {
  width: 12px;
  height: 12px;
  stroke-width: 2;
}

/* 不再渲染更深层级 */
</style>
