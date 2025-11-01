<script setup lang="ts">
import { ref, onMounted, inject, watch, computed } from 'vue'
import { list as getPictureList } from '../api/pictureController'
import { useRouter } from 'vue-router'

const router = useRouter()

// 从父组件注入筛选条件 - 不提供默认值，确保使用父组件的 ref
const selectedCategoryId = inject<import('vue').Ref<number | null> | null>(
  'selectedCategoryId',
  null,
)
const searchText = inject<import('vue').Ref<string> | null>('searchText', null)
const searchTrigger = inject<import('vue').Ref<number> | null>('searchTrigger', null)

// 竖屏页面: 不再打印调试信息

// 图片列表
const pictures = ref<any[]>([])
// 加载状态
const loading = ref(false)
// 分页参数
const current = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 是否还有更多数据
const hasMore = computed(() => {
  return pictures.value.length < total.value
})

// 获取图片列表
const fetchPictures = async (isLoadMore = false) => {
  if (loading.value) return

  loading.value = true
  try {
    const params: any = {
      current: current.value,
      pageSize: pageSize.value,
      searchText: searchText?.value?.trim() || undefined,
      categoryId: selectedCategoryId?.value || undefined,
      pictureType: 1, // 竖屏
    }

    const res = await getPictureList(params)

    if (res.data.code === 0 && res.data.data) {
      const pageData = res.data.data
      if (isLoadMore) {
        pictures.value = [...pictures.value, ...(pageData.records || [])]
      } else {
        pictures.value = pageData.records || []
      }
      total.value = pageData.total || 0
    }
  } catch (error) {
    console.error('获取图片列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!hasMore.value || loading.value) return
  current.value++
  fetchPictures(true)
}

// 打开图片详情
const openPictureDetail = (pictureId: number | string | undefined) => {
  if (!pictureId) return
  const routeUrl = router.resolve({
    name: 'pictureDetail',
    params: { id: pictureId },
  })
  window.open(routeUrl.href, '_blank')
}

// 直接监听注入的 ref 对象（最简单的方式）
if (selectedCategoryId && searchText && searchTrigger) {
  // 分别监听每个 ref
  watch(selectedCategoryId, (newVal, oldVal) => {
    current.value = 1
    fetchPictures()
  })

  watch(searchText, (newVal, oldVal) => {
    current.value = 1
    fetchPictures()
  })

  watch(searchTrigger, (newVal, oldVal) => {
    current.value = 1
    fetchPictures()
  })
} else {
  console.error('❌ 竖屏页面 - 注入失败，无法设置监听器')
}

// 初始化
onMounted(() => {
  fetchPictures()
})
</script>

<template>
  <div class="vertical-pictures-view">
    <!-- 加载状态 -->
    <div v-if="loading && pictures.length === 0" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && pictures.length === 0" class="empty-state">
      <p>暂无图片数据</p>
    </div>

    <!-- 竖屏：瀑布流布局 -->
    <div v-else class="waterfall-container">
      <div v-for="pic in pictures" :key="pic.id" class="picture-card">
        <div class="picture-image-wrapper">
          <img :src="pic.thumbUrl" :alt="pic.tags || '图片'" loading="lazy" />
        </div>

        <!-- 底部"前往"按钮 -->
        <div class="picture-footer">
          <button class="picture-goto-btn" @click="openPictureDetail(pic.id)">
            <svg
              class="picture-plane-icon"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
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
            前往
          </button>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && pictures.length > 0" class="load-more-container">
      <button class="load-more-btn" :disabled="loading" @click="loadMore">
        <span v-if="loading" class="loading-text">
          <span class="loading-spinner-small"></span>
          加载中...
        </span>
        <span v-else>加载更多</span>
      </button>
    </div>

    <!-- 没有更多数据 -->
    <div v-else-if="!hasMore && pictures.length > 0" class="no-more-data">
      <span>—— 已经到底了 ——</span>
    </div>
  </div>
</template>

<style scoped>
.vertical-pictures-view {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 8px;
  animation: slideInFromLeft 0.4s ease-out forwards;
}

@keyframes slideInFromLeft {
  0% {
    opacity: 0;
    transform: translateX(-40px) scale(1);
  }
  100% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
}

.loading-state,
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #718096;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(26, 160, 193, 0.2);
  border-top-color: #1aa0c1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ========== 瀑布流布局（竖屏）========== */
.waterfall-container {
  column-count: 4;
  column-gap: 22px;
}

.picture-card {
  position: relative;
  break-inside: avoid;
  margin-bottom: 22px;
  border-radius: 13px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  border-left: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    6px 6px 12px rgba(0, 0, 0, 0.35),
    10px 10px 35px rgba(0, 0, 0, 0.25);
  transition:
    transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    box-shadow 0.3s ease;
  transform: scale(1);
  transform-origin: center center;
}

/* 悬停时整个卡片放大 */
.picture-card:hover {
  transform: scale(1.05);
  box-shadow:
    8px 8px 20px rgba(0, 0, 0, 0.4),
    15px 15px 45px rgba(0, 0, 0, 0.3);
  z-index: 10;
}

/* 图片包装器 */
.picture-image-wrapper {
  position: relative;
  overflow: hidden;
  background: #f0f0f0;
}

.picture-image-wrapper img {
  width: 100%;
  height: auto;
  display: block;
}

/* 底部按钮区域 */
.picture-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20% 10%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: transparent;
  opacity: 0;
  visibility: hidden;
  transition:
    opacity 0.3s ease,
    visibility 0.3s;
}

/* 悬停时显示按钮 */
.picture-card:hover .picture-footer {
  opacity: 1;
  visibility: visible;
}

.picture-goto-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 80px;
  background: rgba(240, 240, 240, 0.95);
  color: #000000;
  border: 2px solid rgba(255, 255, 255, 0.95);
  border-radius: 24px;
  font-size: 17px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(100, 100, 100, 0.4);
  letter-spacing: 0.5px;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

.picture-plane-icon {
  width: 18px;
  height: 18px;
  color: #000000;
  flex-shrink: 0;
  animation: planeFloat 2s ease-in-out infinite;
}

@keyframes planeFloat {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-3px) rotate(-2deg);
  }
}

.picture-goto-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 16px rgba(100, 100, 100, 0.4);
  background: rgba(245, 245, 245, 0.95);
}

.picture-goto-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(100, 100, 100, 0.25);
}

/* 加载更多 */
.load-more-container {
  display: flex;
  justify-content: center;
  padding: 32px 0 16px;
}

.load-more-btn {
  padding: 14px 48px;
  background: rgba(138, 180, 248, 0.1);
  border: 2px solid rgba(138, 180, 248, 0.3);
  border-radius: 16px;
  color: #4a5568;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.load-more-btn:hover:not(:disabled) {
  background: rgba(138, 180, 248, 0.2);
  border-color: rgba(138, 180, 248, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.2);
}

.load-more-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(138, 180, 248, 0.3);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.no-more-data {
  text-align: center;
  padding: 32px 0 16px;
  color: #a0aec0;
  font-size: 14px;
}

.no-more-data span {
  position: relative;
  padding: 0 20px;
}

.no-more-data span::before,
.no-more-data span::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 60px;
  height: 1px;
  background: linear-gradient(to right, transparent, rgba(160, 174, 192, 0.3));
}

.no-more-data span::before {
  right: 100%;
  margin-right: 10px;
}

.no-more-data span::after {
  left: 100%;
  margin-left: 10px;
  background: linear-gradient(to left, transparent, rgba(160, 174, 192, 0.3));
}

/* ========== 响应式设计 ========== */
@media (max-width: 1200px) {
  .waterfall-container {
    column-count: 3;
    column-gap: 18px;
  }

  .picture-card {
    margin-bottom: 18px;
  }
}

@media (max-width: 968px) {
  .waterfall-container {
    column-count: 2;
    column-gap: 16px;
  }

  .picture-card {
    margin-bottom: 16px;
  }
}

@media (max-width: 768px) {
  .vertical-pictures-view {
    padding: 0 2px;
  }

  .waterfall-container {
    column-count: 1;
    column-gap: 12px;
  }

  .picture-card {
    margin-bottom: 12px;
  }
}
</style>
