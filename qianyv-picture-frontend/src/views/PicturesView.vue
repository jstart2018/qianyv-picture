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
            title="横屏壁纸"
          >
            横屏壁纸
          </button>
          <button
            :class="['toggle-btn', { active: pictureType === 1 }]"
            @click="handlePictureTypeChange(1)"
            :aria-pressed="pictureType === 1"
            title="竖屏壁纸"
          >
            竖屏壁纸
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

      <!-- 瀑布流布局 - 使用 CSS columns 实现真实 Masonry -->
      <div v-else class="waterfall-container">
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

/* ========== 筛选栏 ========== */
.filter-bar {
  width: 100%;
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.filter-container {
  max-width: 1200px;
  width: 100%;
  display: flex;
  gap: 24px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 2px dashed rgba(223, 231, 245, 0.5);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  align-items: center;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-item {
  flex: 1;
  min-width: 300px;
}

.filter-label {
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
  white-space: nowrap;
}

.category-select {
  padding: 10px 16px;
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  font-size: 14px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 150px;
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
  padding: 10px 16px;
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  font-size: 14px;
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
  padding: 10px;
  width: 42px;
  height: 42px;
  background: linear-gradient(135deg, #d6e9cf 0%, #3cb3d4 100%);
  border: none;
  border-radius: 10px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.search-btn svg {
  width: 20px;
  height: 20px;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 新增：图片方向切换按钮 - 滑块效果 */
.picture-type-toggle {
  position: relative;
  display: flex;
  gap: 0; /* 移除间距，让按钮紧挨在一起 */
  padding: 4px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 14px;
  border: 1px solid rgba(138, 180, 248, 0.2);
}

/* 滑块背景 */
.toggle-slider {
  position: absolute;
  top: 4px;
  bottom: 4px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(245, 248, 255, 0.95) 100%);
  border-radius: 10px;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 2px 8px rgba(138, 180, 248, 0.15),
    0 4px 16px rgba(138, 180, 248, 0.08);
  z-index: 0;
}

.toggle-btn {
  position: relative;
  z-index: 1;
  padding: 8px 20px;
  border: none;
  border-radius: 10px;
  background: transparent; /* 默认透明背景 */
  font-size: 14px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 110px;
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

/* ========== 图片容器 - 优化动画效果 ========== */
.pictures-container {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
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

/* ========== 瀑布流布局 - 使用 CSS columns 实现真实 Masonry ========== */
.waterfall-container {
  column-count: 4;
  column-gap: 20px; /* 列间距增加到 20px，更宽松 */
}

.picture-card {
  position: relative;
  break-inside: avoid; /* 防止图片被分割到两列 */
  margin-bottom: 20px; /* 行间距增加到 20px，与列间距一致 */
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.picture-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.picture-card img {
  width: 100%;
  height: auto;
  display: block;
}

.picture-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to bottom, transparent 0%, rgba(0, 0, 0, 0.7) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 16px;
}

.picture-card:hover .picture-overlay {
  opacity: 1;
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
  .waterfall-container {
    column-count: 3;
    column-gap: 16px;
  }

  .picture-card {
    margin-bottom: 16px;
  }

  /* 优化切换动画在中等屏幕上的表现 */
  .pictures-container.animate {
    transform: scale(0.99);
  }
}

@media (max-width: 968px) {
  .waterfall-container {
    column-count: 2;
    column-gap: 14px;
  }

  .picture-card {
    margin-bottom: 14px;
  }
}

@media (max-width: 768px) {
  .pictures-view {
    padding: 16px;
  }

  .filter-container {
    flex-direction: column;
    align-items: stretch;
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
  /* 小屏幕上简化动画 */
  .pictures-container.animate {
    transform: scale(0.995);
  }

  /* 优化切换按钮在小屏幕上的显示 */
  .picture-type-toggle {
    padding: 3px;
  }

  .toggle-btn {
    min-width: 90px;
    font-size: 13px;
    padding: 6px 16px;
  }
}
</style>
