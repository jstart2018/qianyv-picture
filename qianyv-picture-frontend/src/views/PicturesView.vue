<script setup lang="ts">
import { ref, onMounted, nextTick, provide, watch } from 'vue'
import { listAll as getCategoryList } from '../api/picCategoryController'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const categories = ref<any[]>([])
const selectedCategoryId = ref<number | null>(null)
const searchText = ref('')
const searchTrigger = ref(0) // 搜索触发器，用于强制刷新
const sliderStyle = ref({ left: '0px', width: '0px' })

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

// 监听路由变化，自动更新滑块位置
watch(
  () => route.name,
  () => {
    updateSlider()
  },
)

const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.data.code === 0 && res.data.data) {
      categories.value = res.data.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

const handlePictureTypeChange = (type: 'horizontal' | 'vertical') => {
  const targetRoute = type === 'horizontal' ? 'picturesHorizontal' : 'picturesVertical'
  router.push({
    name: targetRoute,
    query: {
      categoryId: selectedCategoryId.value?.toString() || undefined,
      searchText: searchText.value || undefined,
    },
  })
  nextTick(() => {
    updateSlider()
  })
}

const isHorizontal = () => {
  return route.name === 'picturesHorizontal'
}

const handleSearch = () => {
  // 触发搜索，先trim再强制触发更新
  searchText.value = searchText.value.trim()
  // 增加触发器的值，强制子组件刷新
  searchTrigger.value++
}

const handleClearSearch = () => {
  // 清空搜索框并触发搜索
  searchText.value = ''
  // 增加触发器的值，强制子组件刷新
  searchTrigger.value++
}

// 测试分类变化
const handleCategoryChange = (event: Event) => {
  const select = event.target as HTMLSelectElement
  const selectedIndex = select.selectedIndex
  if (selectedIndex >= 0) {
    selectedCategoryId.value = categories.value[selectedIndex]?.id || null
  }
}

provide('selectedCategoryId', selectedCategoryId)
provide('searchText', searchText)
provide('searchTrigger', searchTrigger)

onMounted(async () => {
  await fetchCategories()
  updateSlider()
})
</script>

<template>
  <div class="pictures-view">
    <div class="filter-bar">
      <div class="filter-container">
        <div class="filter-item picture-type-toggle" style="order: -1">
          <div class="toggle-slider" :style="sliderStyle"></div>
          <button
            :class="['toggle-btn', { active: isHorizontal() }]"
            @click="handlePictureTypeChange('horizontal')"
            :aria-pressed="isHorizontal()"
            title="横屏风格"
          >
            横屏风格
          </button>
          <button
            :class="['toggle-btn', { active: !isHorizontal() }]"
            @click="handlePictureTypeChange('vertical')"
            :aria-pressed="!isHorizontal()"
            title="竖屏风格"
          >
            竖屏风格
          </button>
        </div>
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
        <div class="filter-item search-item">
          <label class="filter-label">搜索：</label>
          <input
            v-model="searchText"
            type="text"
            placeholder="输入关键词搜索..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button v-if="searchText" class="clear-btn" @click="handleClearSearch" title="清空搜索">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M18 6L6 18M6 6L18 18"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>
          <button class="search-btn" @click="handleSearch" title="搜索">
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
    <router-view />
  </div>
</template>

<style scoped>
.pictures-view {
  width: 100%;
  min-height: calc(100vh - 60px);
  padding: 24px;
  background: transparent;
}
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
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  white-space: nowrap;
}
.category-select {
  padding: 13px 22px;
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  font-size: 16px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 190px;
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
  padding: 13px 22px;
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  font-size: 16px;
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

.clear-btn {
  padding: 8px;
  width: 40px;
  height: 40px;
  background: rgba(248, 113, 113, 0.1);
  border: 1px solid rgba(248, 113, 113, 0.2);
  border-radius: 10px;
  color: #ef4444;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.clear-btn svg {
  width: 18px;
  height: 18px;
}

.clear-btn:hover {
  background: rgba(248, 113, 113, 0.15);
  border-color: rgba(248, 113, 113, 0.4);
  transform: scale(1.05);
}

.clear-btn:active {
  transform: scale(0.95);
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
.picture-type-toggle {
  position: relative;
  display: flex;
  gap: 0;
  padding: 5px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(138, 180, 248, 0.2);
}
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
  background: transparent;
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
@media (max-width: 768px) {
  .pictures-view {
    padding: 16px;
  }
  .filter-container {
    flex-direction: column;
    align-items: stretch;
    padding: 20px 24px;
  }
  .search-item {
    min-width: auto;
  }
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
