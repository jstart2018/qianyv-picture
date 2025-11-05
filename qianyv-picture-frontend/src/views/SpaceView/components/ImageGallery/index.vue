<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { usePicture } from '../../composables/usePicture'

interface Props {
  spaceId: number | null
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

const handleSearchClick = () => {
  handleSearch(props.spaceId)
}

const handlePage = (page: number) => {
  handlePageChange(page, props.spaceId)
}
</script>

<template>
  <div class="image-gallery">
    <!-- 搜索工具栏 -->
    <div class="image-toolbar">
      <div class="filters">
        <div class="filter-item search-box image-search">
          <span class="filter-icon">🔍</span>
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
      <p>加载中...</p>
    </div>

    <div v-else-if="pictures.length === 0" class="empty-pictures">
      <p>暂无图片，点击右上角"上传图片"按钮开始上传吧！</p>
    </div>

    <div v-else class="pictures-waterfall">
      <div v-for="picture in pictures" :key="picture.id" class="picture-card">
        <img :src="picture.thumbUrl || picture.url" :alt="picture.introduction || '图片'" />
        <div class="picture-info">
          <p v-if="picture.tags" class="picture-tags">{{ picture.tags }}</p>
          <p v-if="picture.introduction" class="picture-desc">{{ picture.introduction }}</p>
          <div class="picture-stats">
            <span>❤️ {{ picture.collectCount || 0 }}</span>
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
