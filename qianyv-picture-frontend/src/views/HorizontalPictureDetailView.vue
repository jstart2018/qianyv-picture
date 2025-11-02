<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOneById, checkCollect, collectToggle } from '../api/pictureController'

const route = useRoute()
const pictureId = ref<string>('')
const picture = ref<any>(null)
const loading = ref(false)
const error = ref<string>('')
const downloading = ref(false)
const collecting = ref(false)
const isCollected = ref(false)

// 解析标签数据
const parseTags = (tagsData: string | null | undefined): string[] => {
  if (!tagsData) return []

  try {
    // 如果是 JSON 数组字符串，解析它
    if (typeof tagsData === 'string' && tagsData.startsWith('[')) {
      return JSON.parse(tagsData)
    }
    // 如果已经是数组，直接返回
    if (Array.isArray(tagsData)) {
      return tagsData
    }
    // 如果是逗号分隔的字符串
    if (typeof tagsData === 'string') {
      return tagsData
        .split(',')
        .map((tag) => tag.trim())
        .filter((tag) => tag)
    }
    return []
  } catch (err) {
    console.error('解析标签失败:', err)
    return []
  }
}

// 获取图片详情
const fetchPictureDetail = async () => {
  loading.value = true
  error.value = ''

  try {
    // ID 保持字符串类型传递，避免精度丢失
    const res = await getOneById({ id: pictureId.value } as any)

    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
      // 获取图片后检查收藏状态
      await checkCollectStatus()
    } else {
      error.value = res.data.message || '获取图片详情失败'
    }
  } catch (err) {
    console.error('获取图片详情失败:', err)
    error.value = '网络错误，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (dateString: string): string => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

// 检查收藏状态
const checkCollectStatus = async () => {
  try {
    // 使用正确的API接口检查收藏状态
    // 保持 ID 为字符串，避免精度丢失
    const res = await checkCollect({ id: pictureId.value } as any)

    if (res.data.code === 0 && res.data.data !== undefined) {
      // data 返回的是数字：1表示已收藏，0表示未收藏
      isCollected.value = res.data.data === 1
    }
  } catch (err) {
    console.error('检查收藏状态失败:', err)
  }
}

// 处理下载
const handleDownload = async () => {
  if (downloading.value) return

  downloading.value = true
  try {
    // 发送下载请求获取图片URL
    const res = await fetch('/api/picture/download', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ pictureId: pictureId.value }),
    })
    const data = await res.json()

    if (data.code === 0 && data.data) {
      // 获取到图片URL后直接下载
      const link = document.createElement('a')
      link.href = data.data
      link.download = picture.value.picName || 'image'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    } else {
      alert(data.message || '下载失败')
    }
  } catch (err) {
    console.error('下载失败:', err)
    alert('下载失败，请稍后重试')
  } finally {
    downloading.value = false
  }
}

// 处理收藏/取消收藏
const handleCollect = async () => {
  if (collecting.value) return

  collecting.value = true
  try {
    // 使用正确的API接口切换收藏状态
    // collect参数：1表示收藏，0表示取消收藏
    // 注意：当前状态是已收藏，则执行取消收藏（传0）；反之执行收藏（传1）
    const collectAction = isCollected.value ? 0 : 1

    // 保持 ID 为字符串，避免精度丢失
    const res = await collectToggle({
      id: pictureId.value,
      collect: collectAction,
    } as any)

    if (res.data.code === 0) {
      // 切换成功后更新本地状态
      isCollected.value = !isCollected.value

      // 更新收藏数量
      if (picture.value) {
        picture.value.collectCount =
          (picture.value.collectCount || 0) + (isCollected.value ? 1 : -1)
      }
    } else {
      alert(res.data.message || '操作失败')
    }
  } catch (err) {
    console.error('收藏操作失败:', err)
    alert('操作失败，请稍后重试')
  } finally {
    collecting.value = false
  }
}

onMounted(() => {
  pictureId.value = route.params.id as string
  fetchPictureDetail()
})
</script>

<template>
  <div class="horizontal-picture-detail-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">加载中...</p>
    </div>

    <!-- 错误状态 -->
    <div v-else-if="error" class="error-container">
      <div class="error-icon">⚠️</div>
      <p class="error-text">{{ error }}</p>
      <button class="retry-btn" @click="fetchPictureDetail">重试</button>
    </div>
    <!-- 图片详情内容 -->
    <div v-else-if="picture" class="detail-content">
      <div class="content-wrapper">
        <!-- 图片展示区域 -->
        <div class="picture-display-section">
          <!-- 图片及装饰元素的父容器 -->
          <div class="picture-wrapper">
            <div class="picture-container">
              <img
                :src="picture.thumbUrl"
                :alt="picture.introduction || '图片'"
                class="picture-image"
              />
              <!-- 灰色平台托盘 -->
              <div class="platform-base"></div>
              <!-- 黑色小梯形 -->
              <div class="black-trapezoid"></div>
            </div>
          </div>
          <!-- 标签区域 -->
          <div class="tags-section" v-if="picture.tags && parseTags(picture.tags).length > 0">
            <div class="tags-header">
              <!-- 线条风格的标签图标 -->
              <svg
                class="tags-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"
                ></path>
                <line x1="7" y1="7" x2="7.01" y2="7"></line>
              </svg>
              <span class="tags-title">相关标签</span>
            </div>
            <div class="tags-container">
              <span v-for="(tag, index) in parseTags(picture.tags)" :key="index" class="tag-item">
                <!-- 青柠切片图标 -->
                <svg
                  class="lime-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <circle cx="12" cy="12" r="10"></circle>
                  <line x1="12" y1="2" x2="12" y2="22"></line>
                  <line x1="2" y1="12" x2="22" y2="12"></line>
                  <line x1="5.6" y1="5.6" x2="18.4" y2="18.4"></line>
                  <line x1="5.6" y1="18.4" x2="18.4" y2="5.6"></line>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
                {{ tag }}
              </span>
            </div>
          </div>
        </div>

        <!-- 信息卡片区域 -->
        <div class="info-card-section">
          <div class="info-card">
            <!-- 图片基本信息卡片 -->
            <div class="basic-info-card">
              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">分类：</span>
                  <span class="info-value">{{ picture.category || '-' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">分辨率：</span>
                  <span class="info-value">{{ picture.picWidth }}x{{ picture.picHeight }}</span>
                </div>
              </div>

              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">格式：</span>
                  <span class="info-value">{{ picture.picFormat || 'PNG' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">大小：</span>
                  <span class="info-value">{{ formatFileSize(picture.picSize) }}</span>
                </div>
              </div>

              <div class="info-row">
                <div class="info-item">
                  <span class="info-label">下载量：</span>
                  <span class="info-value">{{ picture.downloadCount || 0 }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">收藏量：</span>
                  <span class="info-value">{{ picture.favourCount || 0 }}</span>
                </div>
              </div>

              <div class="info-row center">
                <div class="info-item">
                  <span class="info-label">发布时间：</span>
                  <span class="info-value">{{ formatDate(picture.createTime) }}</span>
                </div>
              </div>
            </div>

            <!-- 作者信息区域 -->
            <div class="author-section">
              <div class="author-avatar">
                <img :src="picture.user?.userAvatar || '/default-avatar.png'" alt="作者头像" />
              </div>
              <div class="author-info">
                <div class="author-name">{{ picture.user?.userName || '匿名用户' }}</div>
                <div class="author-stats">
                  <span class="stat-item">粉丝 {{ picture.user?.followCount || 0 }}</span>
                  <span class="stat-divider">|</span>
                  <span class="stat-item">分享 {{ picture.user?.shareCount || 0 }}</span>
                  <span class="stat-divider">|</span>
                  <span class="stat-item">获赞 {{ picture.user?.likeCount || 0 }}</span>
                </div>
                <div class="author-bio">{{ picture.user?.userProfile || '暂无简介~' }}</div>
              </div>
            </div>

            <!-- 分割线 -->
            <div class="divider"></div>
            <!-- 操作按钮区域 -->
            <div class="action-buttons">
              <button class="download-btn" @click="handleDownload" :disabled="downloading">
                <span class="btn-text">{{ downloading ? '下载中...' : '下载' }}</span>
                <svg
                  class="btn-icon-svg"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="7 10 12 15 17 10"></polyline>
                  <line x1="12" y1="15" x2="12" y2="3"></line>
                </svg>
              </button>
              <button
                class="collect-btn"
                :class="{ collected: isCollected }"
                @click="handleCollect"
                :disabled="collecting"
              >
                <!-- 水波纹效果容器 -->
                <span class="ripple-container">
                  <span class="ripple ripple-1"></span>
                  <span class="ripple ripple-2"></span>
                  <span class="ripple ripple-3"></span>
                </span>

                <!-- 星星图标 -->
                <svg
                  class="star-icon"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polygon
                    points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
                  ></polygon>
                </svg>

                <!-- 水泡粒子 -->
                <span class="bubble bubble-1"></span>
                <span class="bubble bubble-2"></span>
                <span class="bubble bubble-3"></span>
                <span class="bubble bubble-4"></span>
                <span class="bubble bubble-5"></span>
                <span class="bubble bubble-6"></span>
                <span class="bubble bubble-7"></span>
                <span class="bubble bubble-8"></span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.horizontal-picture-detail-view {
  width: 100%;
  min-height: calc(100vh - 60px);
  padding: 40px 0;
  /* 使用全局渐变背景，不设置背景色 */
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(26, 160, 193, 0.1);
  border-top-color: #1aa0c1;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: 16px;
  font-size: 16px;
  color: #1aa0c1;
}

/* 错误状态 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 20px;
}

.error-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.error-text {
  font-size: 18px;
  color: #e53e3e;
  margin-bottom: 24px;
}

.retry-btn {
  padding: 12px 32px;
  font-size: 16px;
  font-weight: 500;
  color: #fff;
  background: linear-gradient(135deg, #1aa0c1 0%, #7bd3d8 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(26, 160, 193, 0.3);
}

/* 详情内容 */
.detail-content {
  max-width: 1400px; /* 增加容器宽度以容纳图片和卡片 */
  margin: 0 auto;
  padding: 0 20px;
}

/* 内容包裹器 - 横向布局 */
.content-wrapper {
  display: flex;
  gap: 40px; /* 图片和卡片之间的间距 */
  align-items: flex-start;
}

/* 图片展示区域 */
.picture-display-section {
  flex-shrink: 0; /* 防止图片被压缩 */
}

/* 图片及装饰元素的父容器 */
.picture-wrapper {
  position: relative;
  transform: translate(225px, -38px);
}

/* 图片展示区域 */
.picture-container {
  width: 843.02px;
  height: 558.22px;
  margin: 40px -299px; /* 上40px 右auto 下0 左0 - 向下和向左移动 */
  position: relative;
}

/* 半透明白框 + 纯白边界线 */
.picture-container::before {
  content: '';
  position: absolute;
  top: -8px;
  left: -8px;
  right: -8px;
  bottom: -8px;
  background: rgba(255, 255, 255, 0.5);
  border: 2px solid #ffffff;
  border-radius: 32px 32px 0 0; /* 只有上方圆角，下方直角 */
  z-index: 0;
}

/* 图片层 */
.picture-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 28px 28px 0 0; /* 只有上方圆角，下方直角 */
  box-shadow:
    0 0 0 1px rgba(26, 160, 193, 0.08),
    0 10px 40px rgba(26, 160, 193, 0.12);
  pointer-events: none;
  z-index: 2;
}

.picture-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
  border-radius: 28px 28px 0 0; /* 只有上方圆角，下方直角 */
  position: relative;
  z-index: 1;
  background: #fff;
}

/* 灰色平台托盘 */
.platform-base {
  position: absolute;
  bottom: -11px;
  left: 50%;
  transform: translateX(-50%);
  width: 1100px;
  height: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #aaa8a8 20%, #777777 80%, #000000 100%);
  border-radius: 0 0 6px 6px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 黑色小梯形 */
.black-trapezoid {
  position: absolute;
  top: 558px; /* 距离容器顶部的距离 - 可微调 */
  left: 371px; /* 距离容器左侧的距离 - 可微调 */
  width: 100px; /* 梯形顶部宽度 - 可微调 */
  height: 6px; /* 梯形高度（非常扁平）- 可微调 */
  background: linear-gradient(180deg, #222121 0%, #222121 100%);
  clip-path: polygon(
    0% 0%,
    /* 左上角 - 完全展开 */ 100% 0%,
    /* 右上角 - 完全展开 */ 92% 100%,
    /* 右下角 - 向内收缩8% */ 8% 100% /* 左下角 - 向内收缩8% */
  );
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  z-index: 3;
}

/* ========== 标签区域 ========== */
.tags-section {
  position: absolute;
  left: 100px;
  top: 640px; /* Moved 100px upward */
  width: 12000px;
  background: transparent; 
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  border-radius: 0;
  padding: 20px 20px 24px 20px;
  box-shadow: none;
  border: none;
}

.tags-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.tags-icon {
  width: 28px;
  height: 28px;
  stroke: #000000;
  stroke-linejoin: round;
  stroke-linecap: round;
  flex-shrink: 0;
}

.tags-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.tags-container {
  display: flex;
  flex-wrap: nowrap;
  gap: 10px;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
  scrollbar-color: rgba(128, 128, 128, 0.3) transparent;
  padding-top: 4px;
  padding-bottom: 8px;
}

.tags-container::-webkit-scrollbar {
  height: 6px;
}

.tags-container::-webkit-scrollbar-track {
  background: transparent;
}

/* 滚动条颜色 */
.tags-container::-webkit-scrollbar-thumb {
  background: rgba(128, 128, 128, 0.3);
  border-radius: 3px;
}

/* 滚动条颜色 */
.tags-container::-webkit-scrollbar-thumb:hover {
  background: rgba(128, 128, 128, 0.5);
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 24px;
  font-size: 18px;
  color: #2c3e50;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(228, 227, 227, 0.08);
  white-space: nowrap;
  flex-shrink: 0;
  text-shadow: 0 1px 2px rgba(216, 214, 214, 0.15);
}

.lime-icon {
  width: 18px;
  height: 18px;
  stroke: #84cc16;
  stroke-linejoin: round;
  stroke-linecap: round;
  flex-shrink: 0;
}

.tag-item:hover {
  background: rgba(255, 255, 255, 0.35);
  border-color: rgba(255, 254, 254, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.tag-item:hover .lime-icon {
  stroke: #84cc16;
  transform: rotate(15deg);
}

.tag-item:active {
  transform: translateY(0);
}

/* ========== 信息卡片区域 ========== */
.info-card-section {
  flex: 0 0 auto; /* 不拉伸，固定宽度 */
  width: 478px; /* 增加宽度：从 calc(100% / 3 - 13.33px) ≈ 280px 增加到 380px */
  min-width: 320px; /* 最小宽度防止过窄 */
  margin-left: 650px; /* 向左移动：从 700px 减少到 650px */
  margin-top: 30px; /* 向下移动：新增 30px 上边距 */
}

.info-card {
  width: 100%;
  height: 500px;
  background: rgb(212, 230, 218);
  border-radius: 20px;
  border: none; /* 去掉外边框 */
  box-shadow:
    0 0 30px rgba(218, 253, 226, 0.3),
    /* 向外的黑色阴影 */ 0 0 20px rgba(0, 0, 0, 0.1),
    /* 更大范围的黑色阴影 */ 0 4px 20px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgb(211, 211, 211);
  backdrop-filter: blur(10px); /* 添加背景模糊效果 */
  -webkit-backdrop-filter: blur(10px); /* Safari 兼容 */
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 基本信息卡片（内嵌卡片） */
.basic-info-card {
  background: rgba(255, 255, 255, 0.5); /* 白色半透明背景 */
  border-radius: 16px;
  border: 1px solid rgba(0, 0, 0, 0.2);
  padding: 16px 20px;
  box-shadow:
    inset 0 2px 8px rgba(0, 0, 0, 0.15),
    /* 向内的黑色阴影 - 顶部 */ inset 0 4px 12px rgba(0, 0, 0, 0.1),
    /* 向内的黑色阴影 - 增强 */ 0 1px 0 rgba(255, 255, 255, 0.6); /* 外部高光 */
}

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 12px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row.center {
  justify-content: flex-start; /* 改为左对齐 */
}

.info-item {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
}

.info-row.center .info-item {
  flex: 1; /* 改为占据整行，实现左对齐 */
}

.info-label {
  color: #000000;
  font-weight: 500;
  white-space: nowrap;
  margin-right: 6px;
}

.info-value {
  color: #000000;
  font-weight: 600;
}

/* 作者信息区域 */
.author-section {
  display: flex;
  gap: 16px;
  padding: 16px;
}

.author-avatar {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid rgba(71, 70, 70, 0.7);
  box-shadow: 0 2px 8px rgba(121, 118, 118, 0.2);
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.author-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.author-stats {
  font-size: 15px;
  color: #333333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-item {
  white-space: nowrap;
}

.stat-divider {
  color: #3d3d3d;
}

.author-bio {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 分割线 */
.divider {
  height: 1px;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(26, 160, 193, 0.3) 20%,
    rgba(26, 160, 193, 0.3) 80%,
    transparent 100%
  );
  margin: 8px 0;
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 8px; /* 向上移动10px：使用负值 */
  padding-top: 6px; /* 向上移动：从 16px 减少到 6px */
}

.download-btn {
  flex: 1;
  max-width: 200px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #7aca52 0%, #84cf5f 60%, #afda98 100%); /* 绿色渐变 */
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.download-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(82, 196, 26, 0.4);
}

.download-btn:active:not(:disabled) {
  transform: translateY(0);
}

.download-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 20px;
}

.btn-icon-svg {
  width: 20px;
  height: 20px;
  stroke-width: 2.5;
}

.btn-text {
  font-size: 16px;
}

.collect-btn {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent; /* 透明背景 */
  color: #ff69b4; /* 粉红色 */
  border: 2px solid #000000; /* 黑色边框 */
  border-radius: 50%;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
  overflow: visible;
}

.collect-btn:hover:not(:disabled) {
  transform: scale(1.1) rotate(15deg);
  box-shadow: 0 4px 12px rgba(255, 105, 180, 0.3);
}

.collect-btn.collected {
  background: transparent; /* 收藏后也保持透明 */
  color: #f1b8c8; /* 收藏后深粉色 */
  border-color: #f1b8c8; /* 边框也变色 */
}

.collect-btn.collected .star-icon {
  fill: #f1b8c8; /* 收藏后填充粉红色 */
  animation: starFormation 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 水波纹容器 */
.ripple-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  pointer-events: none;
}

/* 水波纹效果 */
.ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 2px solid #f1b8c8;
  opacity: 0;
  pointer-events: none;
}

.collect-btn.collected .ripple-1 {
  animation: rippleEffect 1.2s ease-out;
}

.collect-btn.collected .ripple-2 {
  animation: rippleEffect 1.2s ease-out 0.2s;
}

.collect-btn.collected .ripple-3 {
  animation: rippleEffect 1.2s ease-out 0.4s;
}

/* 水泡粒子 */
.bubble {
  position: absolute;
  width: 6px;
  height: 6px;
  background: #f1b8c8;
  border-radius: 50%;
  opacity: 0;
  pointer-events: none;
  box-shadow: 0 0 8px rgba(241, 184, 200, 0.6);
}

/* 水泡初始位置（围绕按钮周围） */
.bubble-1 {
  top: 10%;
  left: 50%;
}
.bubble-2 {
  top: 25%;
  left: 85%;
}
.bubble-3 {
  top: 50%;
  left: 90%;
}
.bubble-4 {
  top: 75%;
  left: 85%;
}
.bubble-5 {
  top: 90%;
  left: 50%;
}
.bubble-6 {
  top: 75%;
  left: 15%;
}
.bubble-7 {
  top: 50%;
  left: 10%;
}
.bubble-8 {
  top: 25%;
  left: 15%;
}

/* 收藏时触发水泡聚合动画 */
.collect-btn.collected .bubble-1 {
  animation: bubbleToCenter1 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.collect-btn.collected .bubble-2 {
  animation: bubbleToCenter2 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) 0.05s;
}

.collect-btn.collected .bubble-3 {
  animation: bubbleToCenter3 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) 0.1s;
}

.collect-btn.collected .bubble-4 {
  animation: bubbleToCenter4 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) 0.15s;
}

.collect-btn.collected .bubble-5 {
  animation: bubbleToCenter5 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) 0.2s;
}

.collect-btn.collected .bubble-6 {
  animation: bubbleToCenter6 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) 0.25s;
}

.collect-btn.collected .bubble-7 {
  animation: bubbleToCenter7 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) 0.3s;
}

.collect-btn.collected .bubble-8 {
  animation: bubbleToCenter8 0.8s cubic-bezier(0.34, 1.56, 0.64, 1) 0.35s;
}

.collect-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.star-icon {
  width: 24px;
  height: 24px;
  stroke: #f5b2c6; /* 粉红色描边 */
  stroke-linejoin: round; /* 圆角连接 */
  stroke-linecap: round; /* 圆角端点 */
  transition: all 0.3s ease;
  position: relative;
  z-index: 2;
}

/* 水波纹扩散动画 */
@keyframes rippleEffect {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.8;
  }
  100% {
    transform: translate(-50%, -50%) scale(2.5);
    opacity: 0;
  }
}

/* 星星形成动画 */
@keyframes starFormation {
  0% {
    fill: transparent;
    fill-opacity: 0;
    transform: scale(0) rotate(-180deg);
  }
  60% {
    fill: #f1b8c8;
    fill-opacity: 0.8;
    transform: scale(1.3) rotate(20deg);
  }
  80% {
    transform: scale(0.9) rotate(-10deg);
  }
  100% {
    fill: #f1b8c8;
    fill-opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

/* 水泡聚合到中心的动画 - 每个水泡不同的轨迹 */
@keyframes bubbleToCenter1 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, 150%) scale(0.3);
  }
}

@keyframes bubbleToCenter2 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(100%, 0%) scale(0.3);
  }
}

@keyframes bubbleToCenter3 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(120%, -50%) scale(0.3);
  }
}

@keyframes bubbleToCenter4 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(100%, -100%) scale(0.3);
  }
}

@keyframes bubbleToCenter5 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -150%) scale(0.3);
  }
}

@keyframes bubbleToCenter6 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-100%, -100%) scale(0.3);
  }
}

@keyframes bubbleToCenter7 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-120%, -50%) scale(0.3);
  }
}

@keyframes bubbleToCenter8 {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0);
  }
  20% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  100% {
    opacity: 0;
    transform: translate(-100%, 0%) scale(0.3);
  }
}

/* 响应式调整 */
@media (max-width: 1280px) {
  .picture-container {
    width: 840px;
    height: 560px;
    margin: 40px auto 0 0;
  }
}

@media (max-width: 1080px) {
  .picture-container {
    width: 720px;
    height: 480px;
    margin: 40px auto 0 0;
  }
}

@media (max-width: 980px) {
  .picture-container {
    width: 660px;
    height: 440px;
    margin: 40px auto 0 0;
  }
}

@media (max-width: 800px) {
  .picture-container {
    width: 540px;
    height: 360px;
    margin: 40px auto 0 0;
  }
}

@media (max-width: 640px) {
  .picture-container {
    width: 420px;
    height: 280px;
    margin: 40px auto 0 0;
  }
}

@media (max-width: 520px) {
  .picture-container {
    width: 100%;
    height: 260px;
    margin: 40px 20px 0 0; /* 小屏幕右侧留20px边距 */
  }

  .detail-content {
    padding: 0 16px;
  }
}
</style>
