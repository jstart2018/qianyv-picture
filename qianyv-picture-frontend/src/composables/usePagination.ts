/**
 * 分页逻辑组合式函数
 * 统一管理分页状态和操作
 */

import { ref, computed } from 'vue'

export interface UsePaginationOptions {
  /** 每页数量 */
  pageSize?: number
  /** 初始页码 */
  initialPage?: number
}

export interface UsePaginationReturn {
  /** 当前页码 */
  current: ReturnType<typeof ref<number>>
  /** 每页数量 */
  pageSize: ReturnType<typeof ref<number>>
  /** 总数 */
  total: ReturnType<typeof ref<number>>
  /** 加载状态 */
  loading: ReturnType<typeof ref<boolean>>
  /** 是否还有更多数据 */
  hasMore: ReturnType<typeof computed<boolean>>
  /** 总页数 */
  totalPages: ReturnType<typeof computed<number>>
  /** 下一页 */
  nextPage: () => void
  /** 上一页 */
  prevPage: () => void
  /** 重置分页 */
  reset: () => void
  /** 跳转到指定页 */
  goToPage: (page: number) => void
}

/**
 * 分页组合式函数
 * @param options 配置选项
 * @returns 分页状态和方法
 * @example
 * const { current, pageSize, total, hasMore, nextPage } = usePagination({ pageSize: 20 })
 */
export function usePagination(options: UsePaginationOptions = {}): UsePaginationReturn {
  const current = ref(options.initialPage || 1)
  const pageSize = ref(options.pageSize || 20)
  const total = ref(0)
  const loading = ref(false)

  const hasMore = computed(() => {
    return current.value * pageSize.value < total.value
  })

  const totalPages = computed(() => {
    return Math.ceil(total.value / pageSize.value)
  })

  const nextPage = () => {
    if (hasMore.value) {
      current.value++
    }
  }

  const prevPage = () => {
    if (current.value > 1) {
      current.value--
    }
  }

  const reset = () => {
    current.value = 1
    total.value = 0
  }

  const goToPage = (page: number) => {
    if (page >= 1 && page <= totalPages.value) {
      current.value = page
    }
  }

  return {
    current,
    pageSize,
    total,
    loading,
    hasMore,
    totalPages,
    nextPage,
    prevPage,
    reset,
    goToPage,
  }
}
