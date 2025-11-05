/**
 * 防抖组合式函数
 * 用于延迟执行频繁触发的函数
 */

import { ref, onUnmounted } from 'vue'

/**
 * 防抖函数
 * @param fn 要防抖的函数
 * @param delay 延迟时间（毫秒）
 * @returns 防抖后的函数和取消函数
 * @example
 * const { debouncedFn, cancel } = useDebounce(() => console.log('search'), 300)
 * debouncedFn() // 300ms 后执行
 */
export function useDebounce<T extends (...args: any[]) => any>(fn: T, delay: number = 300) {
  let timer: ReturnType<typeof setTimeout> | null = null

  const debouncedFn = (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)

    timer = setTimeout(() => {
      fn(...args)
    }, delay)
  }

  const cancel = () => {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  // 组件卸载时自动取消
  onUnmounted(() => {
    cancel()
  })

  return {
    debouncedFn,
    cancel,
  }
}

/**
 * 防抖值
 * @param value 响应式值
 * @param delay 延迟时间（毫秒）
 * @returns 防抖后的响应式值
 * @example
 * const searchText = ref('')
 * const debouncedSearchText = useDebouncedRef(searchText, 300)
 */
export function useDebouncedRef<T>(value: { value: T }, delay: number = 300) {
  const debouncedValue = ref<T>(value.value)
  let timer: ReturnType<typeof setTimeout> | null = null

  const update = (newValue: T) => {
    if (timer) clearTimeout(timer)

    timer = setTimeout(() => {
      debouncedValue.value = newValue
    }, delay)
  }

  // 监听原值变化
  const stopWatch = () => {
    if (timer) clearTimeout(timer)
  }

  onUnmounted(() => {
    stopWatch()
  })

  return {
    debouncedValue,
    update,
  }
}

/**
 * 节流函数
 * @param fn 要节流的函数
 * @param delay 间隔时间（毫秒）
 * @returns 节流后的函数
 * @example
 * const { throttledFn } = useThrottle(() => console.log('scroll'), 100)
 * throttledFn() // 每 100ms 最多执行一次
 */
export function useThrottle<T extends (...args: any[]) => any>(fn: T, delay: number = 300) {
  let lastTime = 0

  const throttledFn = (...args: Parameters<T>) => {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn(...args)
    }
  }

  return {
    throttledFn,
  }
}
