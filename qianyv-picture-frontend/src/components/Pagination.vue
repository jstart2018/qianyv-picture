<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  current: number
  totalPages: number
  total: number
  pageSize: number
  loading?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'change', page: number): void
}>()

// 计算要显示的页码
const displayedPages = computed(() => {
  const pages: (number | string)[] = []
  const total = props.totalPages || 1
  const current = props.current || 1

  if (total <= 1) {
    // 只有一页时，仍然显示页码1
    pages.push(1)
  } else if (total <= 7) {
    // 页数较少时，显示所有页码
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    // 页数较多时，显示省略号
    if (current <= 3) {
      // 当前页在前几页
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    } else if (current >= total - 2) {
      // 当前页在后几页
      pages.push(1)
      pages.push('...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      // 当前页在中间
      pages.push(1)
      pages.push('...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...')
      pages.push(total)
    }
  }

  return pages
})

const handlePageClick = (page: number | string) => {
  if (typeof page === 'number' && page !== props.current && !props.loading) {
    emit('change', page)
  }
}

const handlePrev = () => {
  if (props.current > 1 && !props.loading) {
    emit('change', props.current - 1)
  }
}

const handleNext = () => {
  if (props.current < props.totalPages && !props.loading) {
    emit('change', props.current + 1)
  }
}
</script>

<script lang="ts">
export default {
  name: 'Pagination',
}
</script>

<template>
  <div class="pagination-container">
    <div class="pagination-info">
      共 <span class="highlight">{{ total }}</span> 条， 每页
      <span class="highlight">{{ pageSize }}</span> 条， 共
      <span class="highlight">{{ totalPages }}</span> 页
    </div>

    <div class="pagination-wrapper">
      <!-- 上一页 -->
      <button
        class="pagination-btn prev-btn"
        :class="{ disabled: current <= 1 || loading }"
        :disabled="current <= 1 || loading"
        @click="handlePrev"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M15 18L9 12L15 6"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>

      <!-- 页码 -->
      <div class="page-numbers">
        <button
          v-for="(page, index) in displayedPages"
          :key="index"
          class="page-number"
          :class="{
            active: page === current,
            ellipsis: page === '...',
            disabled: loading,
          }"
          :disabled="page === '...' || loading"
          @click="handlePageClick(page)"
        >
          {{ page }}
        </button>
      </div>

      <!-- 下一页 -->
      <button
        class="pagination-btn next-btn"
        :class="{ disabled: current >= totalPages || loading }"
        :disabled="current >= totalPages || loading"
        @click="handleNext"
      >
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path
            d="M9 18L15 12L9 6"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 24px 0;
  margin-top: 20px;
}

.pagination-info {
  font-size: 14px;
  color: #718096;
  font-weight: 500;
}

.pagination-info .highlight {
  color: #4a5568;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 8px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow:
    0 4px 16px rgba(138, 180, 248, 0.1),
    0 0 0 1px rgba(138, 180, 248, 0.05);
}

.pagination-btn {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #4a5568;
}

.pagination-btn svg {
  width: 20px;
  height: 20px;
}

.pagination-btn:hover:not(:disabled) {
  background: rgba(138, 180, 248, 0.15);
  color: #1a202c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.2);
}

.pagination-btn:active:not(:disabled) {
  transform: translateY(0);
}

.pagination-btn.disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 6px;
}

.page-number {
  min-width: 40px;
  height: 40px;
  border-radius: 12px;
  border: none;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #4a5568;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
}

.page-number:hover:not(:disabled):not(.active):not(.ellipsis) {
  background: rgba(138, 180, 248, 0.15);
  color: #1a202c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.15);
}

.page-number.active {
  background: linear-gradient(135deg, rgba(138, 180, 248, 0.3) 0%, rgba(138, 180, 248, 0.2) 100%);
  color: #1a202c;
  font-weight: 600;
  box-shadow:
    0 2px 8px rgba(138, 180, 248, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.page-number.ellipsis {
  cursor: default;
  background: transparent;
  color: #a0aec0;
}

.page-number:disabled:not(.ellipsis) {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 响应式 */
@media (max-width: 640px) {
  .pagination-container {
    padding: 16px 0;
  }

  .pagination-wrapper {
    padding: 6px;
    gap: 4px;
  }

  .pagination-btn,
  .page-number {
    width: 36px;
    height: 36px;
    min-width: 36px;
  }

  .page-numbers {
    gap: 4px;
  }
}
</style>
