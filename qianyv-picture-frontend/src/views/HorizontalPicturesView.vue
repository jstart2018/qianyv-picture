<script setup lang="ts">
import { ref, onMounted, inject, watch } from 'vue'
import { list as getPictureList } from '../api/pictureController'
import { useRouter } from 'vue-router'
import { parseTags } from '@/utils/parse'
import { usePagination } from '@/composables'

const router = useRouter()

// 从父组件注入筛选条件 - 指定类型为 Ref 以获得类型信息
const selectedCategoryId = inject<import('vue').Ref<number | null> | null>(
  'selectedCategoryId',
  null,
)
const searchText = inject<import('vue').Ref<string> | null>('searchText', null)
const searchTrigger = inject<import('vue').Ref<number> | null>('searchTrigger', null)

// 图片列表
const pictures = ref<any[]>([])

// 使用分页组合式函数
const { current, pageSize, total, loading, hasMore } = usePagination({ pageSize: 20 })

// 3D 旋转跟随鼠标效果
const handleMouseMove = (event: MouseEvent) => {
  const card = event.currentTarget as HTMLElement
  const rect = card.getBoundingClientRect()
  const x = (event.clientX - rect.left) / rect.width
  const y = (event.clientY - rect.top) / rect.height
  const rotateY = (0.5 - x) * 12
  const rotateX = (y - 0.5) * 10
  card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.005, 1.005, 1.005)`
  card.style.transition = 'transform 0.05s ease-out'
}

// 鼠标离开时重置
const handleMouseLeave = (event: MouseEvent) => {
  const card = event.currentTarget as HTMLElement
  card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)'
  card.style.transition = 'transform 0.4s ease-out'
}

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
      pictureType: 0, // 横屏
    }

    const res = await getPictureList(params)

    if (res.data?.code === 0 && res.data?.data) {
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

// 打开图片详情 - 根据宽高比判断跳转到横屏或竖屏详情页
const openPictureDetail = (picture: any) => {
  if (!picture || !picture.id) return

  // 根据 picScale 判断图片方向
  // picScale >= 1 为横屏，< 1 为竖屏
  const isHorizontal = picture.picScale === undefined || picture.picScale >= 1

  const routeUrl = router.resolve({
    name: isHorizontal ? 'horizontalPictureDetail' : 'verticalPictureDetail',
    params: { id: picture.id },
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
}

// 初始化
onMounted(() => {
  fetchPictures()
})
</script>

<template>
  <div class="horizontal-pictures-view">
    <!-- 加载状态 -->
    <div v-if="loading && pictures.length === 0" class="loading-state">
      <div class="loading-spinner"></div>
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && pictures.length === 0" class="empty-state">
      <p>暂无图片数据</p>
    </div>

    <!-- 横屏：固定三列网格布局 -->
    <div v-else class="grid-container">
      <div
        v-for="pic in pictures"
        :key="pic.id"
        class="grid-card"
        @mousemove="handleMouseMove($event)"
        @mouseleave="handleMouseLeave($event)"
      >
        <div class="grid-image-wrapper">
          <img :src="pic.thumbUrl" :alt="pic.tags || '图片'" loading="lazy" />

          <!-- 横屏独有：悬停时显示的标签白框 -->
          <div class="tags-panel" v-if="parseTags(pic.tags).length > 0">
            <div class="tags-content">
              <!-- 标签列表 -->
              <div class="tags-list">
                <span
                  v-for="(tag, index) in parseTags(pic.tags)"
                  :key="index"
                  class="tag-item"
                  :style="{ animationDelay: `${index * 0.05}s` }"
                >
                  {{ tag }}
                </span>
              </div>
              <!-- 底部操作栏 -->
              <div class="tags-footer">
                <button class="goto-btn" @click.stop="openPictureDetail(pic)">
                  <svg
                    class="plane-icon"
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
.horizontal-pictures-view {
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

/* ========== 固定网格布局（横屏）- 每行3张，大小相同 ========== */
.grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  column-gap: 15px;
  row-gap: 23px;
  justify-items: center;
  /* 为3D效果提供透视 */
  perspective: 1200px;
}

/* 当最后一行只有1-2张图片时，使用居中布局 */
.grid-container::after {
  content: '';
  grid-column: span 3;
}

.grid-card {
  position: relative;
  width: 89.95%;
  border-radius: 13px;
  overflow: hidden;
  cursor: default;
  background: rgba(255, 255, 255, 0.9);
  /* 左边和上边的白框 + 右下方向阴影 */
  border-left: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    6px 6px 12px rgba(0, 0, 0, 0.35),
    10px 10px 35px rgba(0, 0, 0, 0.25);
  /* 3D 变换相关 */
  transform-style: preserve-3d;
  transform: perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1);
  transform-origin: center 50%;
  transition:
    transform 0.5s ease-out,
    box-shadow 0.3s ease;
  will-change: transform;
}

/* 悬停时增强阴影效果 */
.grid-card:hover {
  box-shadow:
    8px 8px 20px rgba(0, 0, 0, 0.4),
    15px 15px 45px rgba(0, 0, 0, 0.3);
}

/* 图片包装器 - 固定宽高比（精确匹配 458.15×320.5 尺寸） */
.grid-image-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 69.94%; /* 320.5 / 458.15 = 0.6994313，精确匹配指定尺寸比例 */
  overflow: hidden;
  background: #f0f0f0;
  /* 保持3D效果 */
  transform-style: preserve-3d;
  transform: translateZ(0);
}

.grid-image-wrapper img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  /* 图片也参与3D变换 */
  transform: translateZ(20px);
  transition: transform 0.3s ease;
}

/* ========== 横屏独有：悬停时显示的标签白框（在图片内部，等比缩小） ========== */
.tags-panel {
  position: absolute;
  top: 50%;
  left: 50%;
  /* 初始状态：从更下方隐藏 */
  transform: translate(-50%, -20%) scale(0.88) translateY(80px);
  width: 96%;
  max-height: 96%;
  background: rgba(255, 255, 255, 0.5); /* 白色半透明，无模糊 */
  border-radius: 45px;
  padding: 24px;
  /* 泛光效果：减弱阴影强度 */
  box-shadow:
    0 0 20px rgba(255, 255, 255, 0.5),
    0 0 40px rgba(255, 255, 255, 0.3),
    0 8px 24px rgba(138, 180, 248, 0.2),
    inset 0 1px 2px rgba(255, 255, 255, 0.8);
  border: 2px solid rgba(255, 255, 255, 0.7);
  /* 初始状态：完全透明隐藏 */
  opacity: 0;
  visibility: hidden;
  /* 优化过渡效果：先快后慢，无过冲 */
  transition:
    opacity 0.4s ease-out,
    transform 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    visibility 0s linear 0.4s;
  z-index: 10;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  /* 3D效果 - 白框浮在图片上方 */
  transform-style: preserve-3d;
}

/* 悬停时白框显示 - 从下往上滑动并淡入 */
.grid-card:hover .tags-panel {
  opacity: 1;
  visibility: visible;
  /* 滑动到中心位置 */
  transform: translate(-50%, -50%) scale(0.95) translateZ(30px);
  /* 先快后慢的入场效果，无过冲 */
  transition:
    opacity 0.3s ease-out,
    transform 0.45s cubic-bezier(0.25, 0.46, 0.45, 0.94),
    visibility 0s linear 0s;
}

.tags-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.tags-list {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-start;
  align-content: flex-start;
  justify-content: space-evenly;
  overflow-y: auto;
  padding: 4px;
  /* 自定义滚动条 */
  scrollbar-width: thin;
  scrollbar-color: rgba(138, 180, 248, 0.3) transparent;
}

.tags-list::-webkit-scrollbar {
  width: 4px;
}

.tags-list::-webkit-scrollbar-track {
  background: transparent;
}

.tags-list::-webkit-scrollbar-thumb {
  background: rgba(138, 180, 248, 0.3);
  border-radius: 2px;
}

.tag-item {
  display: inline-block;
  padding: 10px 22px;
  background: rgba(255, 255, 255, 0.85); /* 更白的背景 */
  color: #000000; /* 纯黑色字体 */
  font-size: 16px;
  font-weight: 600;
  border-radius: 30px;
  border: 1.5px solid rgba(49, 48, 48, 0.5); /* 浅灰色边框 */
  white-space: nowrap;
  /* 只过渡变换和阴影，不过渡背景 */
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15); /* 更柔和的阴影 */
  /* 标签淡入动画 */
  animation: tagPopIn 0.4s ease forwards;
  opacity: 0;
  transform: scale(0.8);
}

/* 标签弹出动画 */
@keyframes tagPopIn {
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.tag-item:hover {
  /* 保持背景颜色不变，只添加缩放和阴影效果 */
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.25);
}

/* 底部操作栏 */
.tags-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 12px;
  margin-top: auto;
}

.goto-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 48px;
  background: rgba(240, 240, 240, 0.9); /* 灰白背景 */
  color: #000000; /* 纯黑色字体 */
  border: 2px solid rgba(255, 255, 255, 0.95); /* 白色边框 */
  border-radius: 24px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(100, 100, 100, 0.35); /* 灰色阴影 */
  letter-spacing: 0.5px;
}

.plane-icon {
  width: 20px;
  height: 20px;
  color: #000000; /* 黑色纸飞机 */
  flex-shrink: 0;
  animation: planeFloat 2s ease-in-out infinite;
  transform: translateY(2px); /* 纸飞机向下移动2px */
}

/* 纸飞机浮动动画 */
@keyframes planeFloat {
  0%,
  100% {
    transform: translateY(3px) rotate(-5deg); /* 基础位置 */
  }
  50% {
    transform: translateY(0px) rotate(-5deg); /* 向上浮动2px */
  }
}

.goto-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(100, 100, 100, 0.45); /* 悬停时加深阴影 */
  background: rgba(245, 245, 245, 0.95); /* 悬停时稍微变白 */
}

.goto-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(100, 100, 100, 0.3);
}

/* 加载更多 */
.load-more-container {
  display: flex;
  justify-content: center;
  padding: 32px 0 16px;
}

.load-more-btn {
  padding: 12px 36px;
  background: linear-gradient(135deg, #d6e9cf 0%, #3cb3d4 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(60, 179, 212, 0.3);
}

.load-more-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(60, 179, 212, 0.4);
}

.load-more-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.loading-spinner-small {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  display: inline-block;
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
  .horizontal-pictures-view {
    padding: 0 6px;
  }

  .grid-container {
    grid-template-columns: repeat(2, 1fr);
    column-gap: 18px;
    row-gap: 16px;
  }

  .grid-container::after {
    grid-column: span 2;
  }
}

@media (max-width: 968px) {
  .horizontal-pictures-view {
    padding: 0 4px;
  }

  .grid-container {
    grid-template-columns: repeat(2, 1fr);
    column-gap: 16px;
    row-gap: 14px;
  }
}

@media (max-width: 768px) {
  .horizontal-pictures-view {
    padding: 0 2px;
  }

  .grid-container {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .grid-container::after {
    grid-column: span 1;
  }
}
</style>
