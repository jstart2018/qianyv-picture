<script setup lang="ts">
import { watch, onMounted, ref, computed, nextTick } from 'vue'
import { usePicture } from '../../composables/usePicture'
import { pictureDownload } from '@/api/pictureController'
import { message } from 'ant-design-vue'

interface Props {
  spaceId: number | null
  refreshKey?: number
}

const props = defineProps<Props>()

const {
  pictures,
  pictureTotal,
  currentPage,
  totalPages,
  pictureLoading,
  pictureSearchText,
  fetchPictures,
  handleSearch,
  handlePageChange,
} = usePicture()

// 瀑布流列数
const columnCount = ref(4)
// 瀑布流列数据
const columnPictures = computed(() => {
  const columns: (typeof pictures.value)[] = Array.from({ length: columnCount.value }, () => [])
  pictures.value.forEach((picture, index) => {
    const columnIndex = index % columnCount.value
    if (columns[columnIndex]) {
      columns[columnIndex].push(picture)
    }
  })
  return columns
})

// 监听窗口大小变化调整列数
const updateColumnCount = () => {
  const width = window.innerWidth
  if (width < 640) {
    columnCount.value = 2
  } else if (width < 1024) {
    columnCount.value = 3
  } else {
    columnCount.value = 4
  }
}

onMounted(() => {
  updateColumnCount()
  window.addEventListener('resize', updateColumnCount)
})

// 监听空间切换
watch(
  () => props.spaceId,
  (newSpaceId) => {
    if (newSpaceId) {
      fetchPictures(newSpaceId)
    }
  },
  { immediate: true },
)

// 监听刷新信号
watch(
  () => props.refreshKey,
  () => {
    if (props.spaceId) {
      fetchPictures(props.spaceId)
    }
  },
)

const handleSearchClick = () => {
  handleSearch(props.spaceId)
}

const handlePage = (page: number) => {
  handlePageChange(page, props.spaceId)
}

// 格式化标签显示
const formatTags = (tags: string) => {
  if (!tags) return ''
  return tags
    .split(',')
    .map((tag) => `#${tag.trim()}`)
    .join(' ')
}

// 下载状态
const downloadingId = ref<number | null>(null)

// 下载图片
const handleDownload = async (picture: any) => {
  if (!props.spaceId) {
    message.error('空间ID不能为空')
    return
  }

  downloadingId.value = picture.id
  try {
    const res = await pictureDownload({
      pictureId: picture.id,
      spaceId: props.spaceId,
    })

    if (res.data && res.data.code === 0 && res.data.data) {
      // 获取下载URL，创建临时链接下载
      const downloadUrl = res.data.data
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = picture.picName || `picture_${picture.id}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      message.success('开始下载')
    } else {
      message.error(res.data?.message || '下载失败')
    }
  } catch (err) {
    console.error('下载失败:', err)
    message.error('下载失败，请重试')
  } finally {
    downloadingId.value = null
  }
}
</script>

<template>
  <div class="image-gallery">
    <!-- 搜索工具栏 -->
    <div class="image-toolbar">
      <div class="filters">
        <div class="filter-item search-box image-search">
          <input
            v-model="pictureSearchText"
            type="text"
            placeholder="搜索图片标签或描述..."
            class="search-input"
            @keyup.enter="handleSearchClick"
          />
          <button class="search-btn" @click="handleSearchClick">搜索</button>
        </div>
      </div>
    </div>

    <!-- 图片展示区域 -->
    <div v-if="pictureLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else-if="pictures.length === 0" class="empty-pictures">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
        <circle cx="8.5" cy="8.5" r="1.5"></circle>
        <polyline points="21 15 16 10 5 21"></polyline>
      </svg>
      <p>暂无图片，点击右上角"上传图片"按钮开始上传吧！</p>
    </div>

    <div v-else class="pictures-masonry">
      <div
        v-for="(column, columnIndex) in columnPictures"
        :key="columnIndex"
        class="masonry-column"
      >
        <div v-for="picture in column" :key="picture.id" class="picture-card">
          <div class="picture-image-wrapper">
            <img
              :src="picture.thumbUrl || picture.url"
              :alt="picture.introduction || '图片'"
              loading="lazy"
            />
          </div>
          <div class="picture-info">
            <!-- 简介区域 -->
            <p v-if="picture.introduction" class="picture-introduction">
              {{ picture.introduction }}
            </p>
            <!-- 标签和下载按钮区域 -->
            <div class="picture-footer">
              <p class="picture-tags">
                {{ picture.tags ? formatTags(picture.tags) : '' }}
              </p>
              <!-- 下载按钮 -->
              <button
                class="download-btn"
                :class="{ downloading: downloadingId === picture.id }"
                :disabled="downloadingId === picture.id"
                @click.stop="handleDownload(picture)"
                title="下载图片"
              >
                <svg
                  v-if="downloadingId !== picture.id"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                  <polyline points="7 10 12 15 17 10"></polyline>
                  <line x1="12" y1="15" x2="12" y2="3"></line>
                </svg>
                <span v-else class="loading-dot"></span>
                <span class="download-text">下载</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="pictures.length > 0" class="pagination">
      <button class="page-btn" :disabled="currentPage === 1" @click="handlePage(currentPage - 1)">
        上一页
      </button>
      <span class="page-info">
        第 {{ currentPage }} / {{ totalPages }} 页，共 {{ pictureTotal }} 张图片
      </span>
      <button
        class="page-btn"
        :disabled="currentPage >= totalPages"
        @click="handlePage(currentPage + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<style scoped>
.image-gallery {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.image-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 10px;
}

.filter-item {
  display: flex;
  align-items: center;
}

.search-box {
  position: relative;
}

.search-input {
  width: 200px;
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  outline: none;
}

.search-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #4a90d9;
}

.search-btn:hover {
  color: #333;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.loading-spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-top-color: #4a90d9;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
}

.empty-pictures {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #999;
}

.empty-pictures svg {
  width: 64px;
  height: 64px;
  fill: #ccc;
}

.pictures-masonry {
  display: flex;
  gap: 10px;
}

.masonry-column {
  flex: 1;
}

.picture-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.picture-card:hover {
  transform: translateY(-4px);
}

.picture-image-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
}

.picture-image-wrapper img {
  width: 100%;
  height: auto;
  display: block;
  transition: transform 0.3s ease;
}

.picture-card:hover .picture-image-wrapper img {
  transform: scale(1.02);
}

.picture-info {
  padding: 12px;
}

.picture-introduction {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
}

.picture-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.picture-tags {
  font-size: 12px;
  color: #999;
  flex: 1;
  margin: 0;
  min-height: 18px;
}

/* 下载按钮 */
.download-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: white;
  color: #333;
  border: 1px solid #333;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
  opacity: 0;
  visibility: hidden;
}

.picture-card:hover .download-btn {
  opacity: 1;
  visibility: visible;
}

.download-btn:hover {
  background: #f5f5f5;
}

.download-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.download-btn svg {
  width: 14px;
  height: 14px;
}

.download-text {
  font-size: 12px;
}

.loading-dot {
  width: 12px;
  height: 12px;
  border: 2px solid rgba(0, 0, 0, 0.2);
  border-top-color: #333;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
}

.page-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #4a90d9;
  margin: 0 10px;
}

.page-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #999;
}
</style>
