<script setup lang="ts">
import { ref, nextTick, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/request'

const route = useRoute()
const router = useRouter()

// 会话ID - 从路由参数获取
const conversationId = ref<number | null>(null)

// 消息列表
interface Message {
  id: number
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
  streaming?: boolean
}

const messages = ref<Message[]>([])

// 输入框内容
const inputText = ref('')

// 加载状态
const loading = ref(false)
const sending = ref(false)

// 聊天容器引用
const chatContainerRef = ref<HTMLElement | null>(null)

// 判断是否有会话
const hasSession = computed(() => conversationId.value !== null)

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainerRef.value) {
      chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
    }
  })
}

// 创建会话
const initSession = async () => {
  loading.value = true
  try {
    const res = await axios.get('/app/create/session')
    if (res.data?.code === 0 && res.data?.data) {
      conversationId.value = res.data.data
      console.log('会话创建成功，conversationId:', conversationId.value)
      // 更新路由地址栏
      router.replace(`/chat/${conversationId.value}`)
    } else {
      console.error('创建会话失败:', res.data?.message)
    }
  } catch (error) {
    console.error('创建会话失败:', error)
  } finally {
    loading.value = false
  }
}

// 解析HTML内容，提取图片
const parseMessageContent = (content: string) => {
  // 使用正则匹配 <img> 标签
  const imgRegex = /<img[^>]+src=["']([^"']+)["'][^>]*>/gi
  const parts: Array<{ type: 'text' | 'image'; content: string }> = []
  let lastIndex = 0
  let match

  while ((match = imgRegex.exec(content)) !== null) {
    // 添加图片标签前的文本
    if (match.index > lastIndex) {
      const textContent = content.substring(lastIndex, match.index)
      if (textContent.trim()) {
        parts.push({ type: 'text', content: textContent })
      }
    }

    // 添加图片 - 确保 match[1] 存在
    if (match[1]) {
      parts.push({ type: 'image', content: match[1] })
    }
    lastIndex = match.index + match[0].length
  }

  // 添加最后剩余的文本
  if (lastIndex < content.length) {
    const textContent = content.substring(lastIndex)
    if (textContent.trim()) {
      parts.push({ type: 'text', content: textContent })
    }
  }

  return parts.length > 0 ? parts : [{ type: 'text', content }]
}

// 发送消息 - 使用 fetch 接收流式数据（POST 方式）
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text) {
    return
  }

  // 如果没有会话ID，先创建会话
  if (!conversationId.value) {
    await initSession()
    if (!conversationId.value) {
      return
    }
  }

  // 添加用户消息
  const userMessage: Message = {
    id: Date.now(),
    role: 'user',
    content: text,
    timestamp: new Date(),
  }
  messages.value.push(userMessage)
  inputText.value = ''
  scrollToBottom()

  // 添加AI消息占位符
  const aiMessage: Message = {
    id: Date.now() + 1,
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    streaming: true,
  }
  messages.value.push(aiMessage)
  scrollToBottom()

  sending.value = true

  try {
    // 使用 fetch 发送 POST 请求接收 SSE 流
    const response = await fetch(
      `/api/app/chat/${conversationId.value}?input=${encodeURIComponent(text)}`,
      {
        method: 'POST',
        headers: {
          Accept: 'text/event-stream',
          'Content-Type': 'application/json',
        },
      },
    )

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body?.getReader()
    const decoder = new TextDecoder()

    if (!reader) {
      throw new Error('无法获取响应流')
    }

    let buffer = ''

    // 读取流式数据
    while (true) {
      const { done, value } = await reader.read()

      if (done) {
        // 流结束，取消 streaming 标记
        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.role === 'assistant') {
          lastMessage.streaming = false
        }
        sending.value = false
        break
      }

      // 解码数据块
      buffer += decoder.decode(value, { stream: true })

      // 处理 SSE 格式的数据
      const lines = buffer.split('\n')
      buffer = lines.pop() || '' // 保留未完成的行

      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()

          // 检查是否是结束标记
          if (data === '[DONE]') {
            const lastMessage = messages.value[messages.value.length - 1]
            if (lastMessage && lastMessage.role === 'assistant') {
              lastMessage.streaming = false
            }
            sending.value = false
            return
          }

          // 解析 JSON 数据
          try {
            const parsed = JSON.parse(data)
            if (parsed.d) {
              // 更新AI消息内容
              const lastMessage = messages.value[messages.value.length - 1]
              if (lastMessage && lastMessage.role === 'assistant') {
                lastMessage.content += parsed.d
                scrollToBottom()
              }
            }
          } catch (e) {
            // 忽略非 JSON 数据
            console.debug('非JSON数据:', data)
          }
        } else if (line.startsWith('event:')) {
          const event = line.substring(6).trim()
          if (event === 'done') {
            const lastMessage = messages.value[messages.value.length - 1]
            if (lastMessage && lastMessage.role === 'assistant') {
              lastMessage.streaming = false
            }
            sending.value = false
            return
          }
        }
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    // 移除失败的AI消息
    if (messages.value[messages.value.length - 1]?.streaming) {
      messages.value.pop()
    }
    sending.value = false
  }
}

// 处理回车发送
const handleKeyDown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

// 监听路由参数变化
watch(
  () => route.params.conversationId,
  (newId) => {
    if (newId) {
      conversationId.value = Number(newId)
    } else {
      conversationId.value = null
    }
  },
  { immediate: true },
)

// 初始化 - 不自动创建会话
onMounted(() => {
  // 从路由获取 conversationId
  if (route.params.conversationId) {
    conversationId.value = Number(route.params.conversationId)
  }
  // 不再自动调用 initSession()
})
</script>

<template>
  <div class="ai-chat-view">
    <!-- 左侧会话列表 -->
    <aside class="chat-sidebar">
      <div class="sidebar-header">
        <h3>会话记录</h3>
      </div>
      <div class="session-list">
        <div class="session-placeholder">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M12 2C6.48 2 2 6.48 2 12C2 17.52 6.48 22 12 22C17.52 22 22 17.52 22 12C22 6.48 17.52 2 12 2ZM12 20C7.59 20 4 16.41 4 12C4 7.59 7.59 4 12 4C16.41 4 20 7.59 20 12C20 16.41 16.41 20 12 20Z"
              fill="currentColor"
            />
            <path
              d="M12 6C11.45 6 11 6.45 11 7V13C11 13.55 11.45 14 12 14C12.55 14 13 13.55 13 13V7C13 6.45 12.55 6 12 6ZM12 16C11.45 16 11 16.45 11 17C11 17.55 11.45 18 12 18C12.55 18 13 17.55 13 17C13 16.45 12.55 16 12 16Z"
              fill="currentColor"
            />
          </svg>
          <p>后端功能开发中</p>
          <span>会话历史功能即将上线</span>
        </div>
      </div>
    </aside>

    <!-- 右侧对话区域 -->
    <main class="chat-main">
      <!-- 顶部标题 -->
      <div class="chat-header">
        <div class="header-info">
          <h2>AI 智能助手</h2>
          <p v-if="conversationId">会话ID: {{ conversationId }}</p>
        </div>
      </div>

      <!-- 消息列表 -->
      <div
        ref="chatContainerRef"
        class="chat-container"
        :class="{ 'is-centered': !hasSession || messages.length === 0 }"
      >
        <div v-if="loading && messages.length === 0" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在创建会话...</p>
        </div>

        <div v-else-if="messages.length === 0" class="empty-state">
          <div class="welcome-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M12 2L2 7L12 12L22 7L12 2Z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M2 17L12 22L22 17"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M2 12L12 17L22 12"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </div>
          <h3>您好，我是 AI 智能助手</h3>
          <p>有什么可以帮助您的吗？</p>
        </div>

        <div v-else class="messages-list">
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <div class="message-avatar">
              <template v-if="msg.role === 'user'">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2" />
                  <path
                    d="M6 21C6 17.686 8.686 15 12 15C15.314 15 18 17.686 18 21"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                  />
                </svg>
              </template>
              <template v-else>
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M12 2L2 7L12 12L22 7L12 2Z"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M2 17L12 22L22 17"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M2 12L12 17L22 12"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </template>
            </div>
            <div class="message-content">
              <!-- 解析并渲染消息内容（支持文本和图片） -->
              <div class="message-text">
                <template v-for="(part, index) in parseMessageContent(msg.content)" :key="index">
                  <span v-if="part.type === 'text'">{{ part.content }}</span>
                  <img
                    v-else-if="part.type === 'image'"
                    :src="part.content"
                    alt="AI生成的图片"
                    class="message-image"
                  />
                </template>
              </div>
              <div v-if="msg.streaming" class="streaming-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="chat-input-area">
        <div class="input-container">
          <textarea
            v-model="inputText"
            placeholder="输入消息内容... (Shift+Enter 换行，Enter 发送)"
            class="message-input"
            rows="1"
            :disabled="sending"
            @keydown="handleKeyDown"
          ></textarea>
          <button class="send-btn" :disabled="sending || !inputText.trim()" @click="sendMessage">
            <svg v-if="!sending" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M22 2L11 13"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M22 2L15 22L11 13L2 9L22 2Z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            <span v-else class="loading-spinner-small"></span>
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
.ai-chat-view {
  display: flex;
  width: 100%;
  height: calc(100vh - 80px);
  background: transparent;
  gap: 16px;
  padding: 16px;
}

/* ========== 左侧会话列表 ========== */
.chat-sidebar {
  width: 280px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-md);
}

.session-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xl);
  text-align: center;
  color: var(--text-tertiary);
}

.session-placeholder svg {
  width: 48px;
  height: 48px;
  margin-bottom: var(--spacing-md);
  opacity: 0.5;
}

.session-placeholder p {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.session-placeholder span {
  font-size: 12px;
  opacity: 0.7;
}

/* ========== 右侧对话区域 ========== */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

.chat-header {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
}

.header-info h2 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--color-primary);
}

.header-info p {
  margin: 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

/* ========== 消息容器 ========== */
.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-xl);
}

/* 居中布局（无会话或无消息时） */
.chat-container.is-centered {
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-tertiary);
}

.welcome-icon svg {
  width: 80px;
  height: 80px;
  color: var(--color-primary);
  opacity: 0.6;
  margin-bottom: var(--spacing-lg);
}

.empty-state h3 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 var(--spacing-sm) 0;
  color: var(--text-primary);
}

.empty-state p {
  font-size: 16px;
  margin: 0;
}

/* ========== 消息列表 ========== */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.message-item {
  display: flex;
  gap: var(--spacing-md);
  animation: fadeIn 0.3s ease;
}

.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.8);
  border: 2px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-message .message-avatar {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  border-color: var(--color-primary);
}

.ai-message .message-avatar {
  background: linear-gradient(135deg, var(--color-secondary) 0%, #a78bfa 100%);
  border-color: var(--color-secondary);
}

.message-avatar svg {
  width: 24px;
  height: 24px;
  color: white;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.message-text {
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md);
  font-size: 15px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.user-message .message-text {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  border-top-right-radius: 4px;
}

.ai-message .message-text {
  background: rgba(255, 255, 255, 0.9);
  color: var(--text-primary);
  border-top-left-radius: 4px;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

/* 消息中的图片样式 */
.message-image {
  max-width: 100%;
  max-height: 400px;
  border-radius: var(--radius-md);
  margin-top: var(--spacing-sm);
  display: block;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.message-image:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* ========== 流式输入指示器 ========== */
.streaming-indicator {
  display: flex;
  gap: 4px;
  padding: var(--spacing-sm);
}

.streaming-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: bounce 1.4s infinite ease-in-out both;
}

.streaming-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.streaming-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

/* ========== 输入区域 ========== */
.chat-input-area {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
}

.input-container {
  display: flex;
  gap: var(--spacing-md);
  align-items: flex-end;
}

.message-input {
  flex: 1;
  min-height: 48px;
  max-height: 120px;
  padding: var(--spacing-md) var(--spacing-lg);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  color: var(--text-primary);
  resize: vertical;
  transition: var(--transition-all);
}

.message-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(26, 160, 193, 0.1);
}

.message-input::placeholder {
  color: var(--text-tertiary);
}

.message-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-btn {
  width: 48px;
  height: 48px;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--transition-all);
  flex-shrink: 0;
}

.send-btn svg {
  width: 24px;
  height: 24px;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 160, 193, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ========== 响应式 ========== */
@media (max-width: 768px) {
  .ai-chat-view {
    flex-direction: column;
    height: auto;
    min-height: calc(100vh - 80px);
  }

  .chat-sidebar {
    width: 100%;
    height: 200px;
  }

  .message-content {
    max-width: 85%;
  }
}
</style>
