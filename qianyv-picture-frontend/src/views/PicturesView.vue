<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { list as getPictureList } from '@/api/pictureController'
import { listAll as getCategoryList } from '@/api/picCategoryController'
import { useRouter } from 'vue-router'

const router = useRouter()

// 分类列表
const categories = ref<API.PicCategoryVO[]>([])
// 选中的分类ID（null表示"全部"）
const selectedCategoryId = ref<number | null>(null)
// 新增：图片方向类型，0=横屏（默认），1=竖屏
const pictureType = ref<number>(0)
// 用于触发切换动画的短时标记
const animatePictureType = ref<boolean>(false)

// 滑块指示器样式
const sliderStyle = ref({
  left: '0px',
  width: '0px',
})

// 更新滑块位置
const updateSlider = () => {
  nextTick(() => {
    const activeBtn = document.querySelector('.toggle-btn.active') as HTMLElement
    if (activeBtn) {
      const container = document.querySelector('.picture-type-toggle') as HTMLElement
      if (container) {
        const containerRect = container.getBoundingClientRect()
        const btnRect = activeBtn.getBoundingClientRect()
        sliderStyle.value = {
          left: `${btnRect.left - containerRect.left}px`,
          width: `${btnRect.width}px`,
        }
      }
    }
  })
}

// 搜索文本
const searchText = ref('')
// 图片列表
const pictures = ref<API.PictureListVO[]>([])
// 加载状态
const loading = ref(false)
// 分页参数
const current = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 不再需要手动分列，使用 CSS columns 实现真实 Masonry

// 是否还有更多数据
const hasMore = computed(() => {
  return pictures.value.length < total.value
})

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    // res 是 AxiosResponse，需要访问 res.data 获取实际数据
    if (res.data.code === 0 && res.data.data) {
      categories.value = res.data.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取图片列表
const fetchPictures = async (isLoadMore = false) => {
  if (loading.value) return

  loading.value = true
  try {
    const params: API.PictureQueryListDTO = {
      current: current.value,
      pageSize: pageSize.value,
      searchText: searchText.value.trim() || undefined,
      categoryId: selectedCategoryId.value || undefined,
      // 后端要求的 pictureType 参数：0 横屏，1 竖屏
      pictureType: pictureType.value,
    }

    const res = await getPictureList(params)
    // res 是 AxiosResponse，需要访问 res.data 获取实际数据
    if (res.data.code === 0 && res.data.data) {
      const pageData = res.data.data
      if (isLoadMore) {
        pictures.value = [...pictures.value, ...(pageData.records || [])]
      } else {
        pictures.value = pageData.records || []
      }
      // 响应拦截器已自动规范化数字字符串，无需手动转换
      total.value = pageData.total || 0
    }
  } catch (error) {
    console.error('获取图片列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分类切换
const handleCategoryChange = () => {
  current.value = 1
  fetchPictures()
}

// 搜索（回车触发）
const handleSearch = () => {
  current.value = 1
  fetchPictures()
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

// 新增：切换图片方向（带滑块动画）
const handlePictureTypeChange = (type: number) => {
  if (pictureType.value === type) return
  pictureType.value = type
  // 更新滑块位置
  updateSlider()
  // 触发流畅的切换动画
  animatePictureType.value = true
  setTimeout(() => (animatePictureType.value = false), 280)
  // 重置分页并请求
  current.value = 1
  fetchPictures()
}

// 解析标签 JSON 数组
const parseTags = (tagsStr: string | undefined | null): string[] => {
  if (!tagsStr) return []
  try {
    // 尝试解析 JSON 数组
    const parsed = JSON.parse(tagsStr)
    if (Array.isArray(parsed)) {
      return parsed.filter((tag) => typeof tag === 'string' && tag.trim())
    }
    return []
  } catch {
    // 如果解析失败，尝试按逗号分割
    return tagsStr
      .split(',')
      .map((tag) => tag.trim())
      .filter((tag) => tag)
  }
}

// 3D 旋转跟随鼠标效果
const handleMouseMove = (event: MouseEvent) => {
  const card = event.currentTarget as HTMLElement
  const rect = card.getBoundingClientRect()
  // 计算鼠标在卡片内的相对位置（0-1范围）
  const x = (event.clientX - rect.left) / rect.width
  const y = (event.clientY - rect.top) / rect.height
  // 计算旋转角度（反转方向：鼠标在哪里就升上来）- 进一步缩小旋转幅度
  const rotateY = (0.5 - x) * 12
  const rotateX = (y - 0.5) * 10
  // 应用3D变换 - 进一步减小缩放效果
  card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.005, 1.005, 1.005)`
  card.style.transition = 'transform 0.05s ease-out' // 从 0.1s 降到 0.05s，加快响应速度
}

// 鼠标离开时重置
const handleMouseLeave = (event: MouseEvent) => {
  const card = event.currentTarget as HTMLElement
  card.style.transform = 'perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)'
  card.style.transition = 'transform 0.4s ease-out' // 从 0.5s 降到 0.4s，加快恢复速度
}

// 初始化
onMounted(async () => {
  await fetchCategories()
  await fetchPictures()
  // 初始化滑块位置
  updateSlider()
})
</script>

<template>
  <div class="pictures-view">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-container">
        <!-- 新增：图片方向切换 (调整到最左侧，突出显示) -->
        <div class="filter-item picture-type-toggle" style="order: -1">
          <!-- 滑块背景 -->
          <div class="toggle-slider" :style="sliderStyle"></div>
          <button
            :class="['toggle-btn', { active: pictureType === 0 }]"
            @click="handlePictureTypeChange(0)"
            :aria-pressed="pictureType === 0"
            title="横屏风格"
          >
            横屏风格
          </button>
          <button
            :class="['toggle-btn', { active: pictureType === 1 }]"
            @click="handlePictureTypeChange(1)"
            :aria-pressed="pictureType === 1"
            title="竖屏风格"
          >
            竖屏风格
          </button>
        </div>

        <!-- 分类选择 -->
        <div class="filter-item">
          <label class="filter-label">分类：</label>
          <select
            v-model="selectedCategoryId"
            class="category-select"
            @change="handleCategoryChange"
          >
            <option :value="null">全部</option>
            <option v-for="category in categories" :key="category.id" :value="category.id">
              {{ category.categoryName }}
            </option>
          </select>
        </div>

        <!-- 搜索框 -->
        <div class="filter-item search-item">
          <label class="filter-label">搜索：</label>
          <input
            v-model="searchText"
            type="text"
            placeholder="输入关键词搜索..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2" />
              <path
                d="M21 21L16.65 16.65"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 图片列表 -->
    <div class="pictures-container" :class="{ animate: animatePictureType }">
      <!-- 加载状态 -->
      <div v-if="loading && pictures.length === 0" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && pictures.length === 0" class="empty-state">
        <p>暂无图片数据</p>
      </div>
      <!-- 竖屏：瀑布流布局 - 使用 CSS columns 实现真实 Masonry -->
      <div v-else-if="pictureType === 1" class="waterfall-container">
        <div
          v-for="pic in pictures"
          :key="pic.id"
          class="picture-card"
          @click="openPictureDetail(pic.id)"
        >
          <img :src="pic.thumbUrl" :alt="pic.tags || '图片'" loading="lazy" />
          <div class="picture-overlay">
            <span v-if="pic.tags" class="picture-tags">{{ pic.tags }}</span>
            <div class="picture-info">
              <span class="collect-count">
                <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                  <path
                    d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                  />
                </svg>
                {{ pic.collectCount || 0 }}
              </span>
            </div>
          </div>
        </div>
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
                  <button class="goto-btn" @click.stop="openPictureDetail(pic.id)">
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
  </div>
</template>

<style scoped>
.pictures-view {
  width: 100%;
  min-height: calc(100vh - 60px);
  padding: 24px;
  background: transparent;
}

/* ========== 筛选栏 - 放大 ========== */
.filter-bar {
  width: 100%;
  margin-bottom: 28px;
  display: flex;
  justify-content: center;
}

.filter-container {
  max-width: 1400px;
  width: 100%;
  display: flex;
  gap: 28px;
  padding: 24px 32px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 2px dashed rgba(223, 231, 245, 0.5);
  border-radius: 18px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  align-items: center;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.search-item {
  flex: 1;
  min-width: 320px;
}

.filter-label {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  white-space: nowrap;
}

.category-select {
  padding: 11px 18px;
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  font-size: 15px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 170px;
}

.category-select:hover {
  border-color: rgba(138, 180, 248, 0.6);
  background: rgba(255, 255, 255, 1);
}

.category-select:focus {
  outline: none;
  border-color: rgba(138, 180, 248, 0.8);
  box-shadow: 0 0 0 3px rgba(138, 180, 248, 0.1);
}

.search-input {
  flex: 1;
  padding: 11px 18px;
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 11px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  font-size: 15px;
  color: #4a5568;
  transition: all 0.3s ease;
}

.search-input:hover {
  border-color: rgba(138, 180, 248, 0.6);
  background: rgba(255, 255, 255, 1);
}

.search-input:focus {
  outline: none;
  border-color: rgba(138, 180, 248, 0.8);
  box-shadow: 0 0 0 3px rgba(138, 180, 248, 0.1);
}

.search-btn {
  padding: 11px;
  width: 46px;
  height: 46px;
  background: linear-gradient(135deg, #d6e9cf 0%, #3cb3d4 100%);
  border: none;
  border-radius: 11px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.search-btn svg {
  width: 22px;
  height: 22px;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 新增：图片方向切换按钮 - 滑块效果 - 放大 */
.picture-type-toggle {
  position: relative;
  display: flex;
  gap: 0; /* 移除间距，让按钮紧挨在一起 */
  padding: 5px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(138, 180, 248, 0.2);
}

/* 滑块背景 */
.toggle-slider {
  position: absolute;
  top: 5px;
  bottom: 5px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(245, 248, 255, 0.95) 100%);
  border-radius: 12px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 2px 8px rgba(138, 180, 248, 0.15),
    0 4px 16px rgba(138, 180, 248, 0.08);
  z-index: 0;
}

.toggle-btn {
  position: relative;
  z-index: 1;
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  background: transparent; /* 默认透明背景 */
  font-size: 15px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 120px;
  text-align: center;
  font-weight: 500;
}

.toggle-btn.active {
  color: #1a202c;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

.toggle-btn:hover:not(.active) {
  color: #2d3748;
  background: rgba(255, 255, 255, 0.1);
}

/* ========== 图片容器 - 优化动画效果 - 减少边距增大图片 ========== */
.pictures-container {
  width: 100%;
  max-width: 1600px; /* 从 1500px 增加到 1600px，更宽的容器 */
  margin: 0 auto;
  padding: 0 8px; /* 从 20px 减少到 8px，大幅减少左右边距 */
  transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1); /* 使用Material Design的standard easing */
}

.pictures-container.animate {
  transform: scale(0.985);
  opacity: 0.95;
  transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
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

/* ========== 瀑布流布局（竖屏）- 使用 CSS columns 实现真实 Masonry ========== */
.waterfall-container {
  column-count: 4;
  column-gap: 22px; /* 从 20px 增加到 22px */
}

.picture-card {
  position: relative;
  break-inside: avoid; /* 防止图片被分割到两列 */
  margin-bottom: 22px; /* 从 20px 增加到 22px，与列间距一致 */
  border-radius: 13px;
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.9);
  /* 左边和上边的白框 + 右下方向阴影 */
  border-left: 1px solid rgba(255, 255, 255, 0.3);
  border-top: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    6px 6px 12px rgba(0, 0, 0, 0.4),
    10px 10px 30px rgba(0, 0, 0, 0.3);
}

.picture-card img {
  width: 100%;
  height: auto;
  display: block;
}

/* ========== 固定网格布局（横屏）- 每行3张，大小相同 ========== */
.grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  column-gap: 15px; /* 图片之间的水平间距 */
  row-gap: 23px; /* 图片之间的垂直间距 */
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
  width: 89.95%; /* 精确计算：458.15 / 509.33 ≈ 89.95%，以匹配 458.15×320.5 尺寸 */
  border-radius: 13px;
  overflow: hidden;
  cursor: default; /* 改为默认光标 */
  background: rgba(255, 255, 255, 0.9);
  /* 左边和上边的白框 + 右下方向阴影 */
  border-left: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid rgba(255, 255, 255, 0.3);
  box-shadow:
    6px 6px 12px rgba(0, 0, 0, 0.35),
    10px 10px 35px rgba(0, 0, 0, 0.25); /* 3D 变换相关 */
  transform-style: preserve-3d;
  transform: perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1);
  transform-origin: center 50%; /* 旋转中心向下移动到60%位置，更自然 */
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
  object-fit: cover; /* 裁切图片以填充容器 */
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
  max-height: 96%; /* 从 92% 增加到 96% */
  background: rgba(255, 255, 255, 0.5); /* 白色半透明，无模糊 */
  border-radius: 45px; /* 从 18px 增加到 28px，圆角加大 */
  padding: 24px;
  /* 泛光效果：减弱阴影强度 */
  box-shadow:
    0 0 20px rgba(255, 255, 255, 0.5),
    0 0 40px rgba(255, 255, 255, 0.3),
    0 8px 24px rgba(138, 180, 248, 0.2),
    inset 0 1px 2px rgba(255, 255, 255, 0.8);
  border: 2px solid rgba(255, 255, 255, 0.7); /* 初始状态：完全透明隐藏 */
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
  justify-content: space-evenly; /* 添加均匀分布 */
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
  padding: 10px 22px; /* 从 8px 18px 增加到 10px 22px */
  background: rgba(255, 255, 255, 0.85); /* 更白的背景 */
  color: #000000; /* 纯黑色字体 */
  font-size: 16px; /* 从 14px 增加到 16px */
  font-weight: 600;
  border-radius: 30px; /* 从 24px 增加到 30px，更大的椭圆 */
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
  padding: 14px 48px; /* 从 32px 增加到 48px，加长水平长度 */
  background: rgba(240, 240, 240, 0.9); /* 灰白背景 */
  color: #000000; /* 纯黑色字体 */
  border: 2px solid rgba(255, 255, 255, 0.95); /* 白色边框 */
  border-radius: 24px; /* 更大的圆角 */
  font-size: 18px; /* 从 16px 增加到 18px，加大"前往"文字 */
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

.picture-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.7) 100%);
  opacity: 0;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 16px;
  pointer-events: none; /* 禁用交互 */
}

.picture-tags {
  font-size: 13px;
  color: white;
  margin-bottom: 8px;
  font-weight: 500;
}

.picture-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collect-count {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: white;
  font-weight: 600;
}

.collect-count svg {
  width: 16px;
  height: 16px;
}

/* ========== 加载更多 ========== */
.load-more-container {
  display: flex;
  justify-content: center;
  padding: 32px 0;
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
  padding: 32px 0;
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
  .pictures-container {
    padding: 0 6px; /* 从 15px 减少到 6px */
  }

  .waterfall-container {
    column-count: 3;
    column-gap: 18px;
  }

  .picture-card {
    margin-bottom: 18px;
  }
  .grid-container {
    grid-template-columns: repeat(2, 1fr);
    column-gap: 18px; /* 从 24px 减少到 18px，减小中屏间距 */
    row-gap: 16px; /* 从 26px 减少到 16px，减小垂直间距 */
  }

  .grid-container::after {
    grid-column: span 2;
  }

  /* 优化切换动画在中等屏幕上的表现 */
  .pictures-container.animate {
    transform: scale(0.99);
  }
}

@media (max-width: 968px) {
  .pictures-container {
    padding: 0 4px; /* 从 12px 减少到 4px */
  }

  .waterfall-container {
    column-count: 2;
    column-gap: 16px;
  }

  .picture-card {
    margin-bottom: 16px;
  }
  .grid-container {
    grid-template-columns: repeat(2, 1fr);
    column-gap: 16px; /* 从 20px 减少到 16px，减小小屏间距 */
    row-gap: 14px; /* 从 22px 减少到 14px，减小垂直间距 */
  }
}

@media (max-width: 768px) {
  .pictures-view {
    padding: 16px;
  }

  .pictures-container {
    padding: 0 2px; /* 从 10px 减少到 2px，移动端最小边距 */
  }

  .filter-container {
    flex-direction: column;
    align-items: stretch;
    padding: 20px 24px;
  }

  .search-item {
    min-width: auto;
  }

  .waterfall-container {
    column-count: 1;
    column-gap: 12px;
  }

  .picture-card {
    margin-bottom: 12px;
  }

  .grid-container {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .grid-container::after {
    grid-column: span 1;
  }

  /* 小屏幕上简化动画 */
  .pictures-container.animate {
    transform: scale(0.995);
  }

  /* 优化切换按钮在小屏幕上的显示 */
  .picture-type-toggle {
    padding: 4px;
  }

  .toggle-btn {
    min-width: 100px;
    font-size: 14px;
    padding: 8px 18px;
  }
}
</style>
