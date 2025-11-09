import { ref, computed, watch, nextTick } from 'vue'
import { likesToggle, collectToggle1, checkBlogLike, checkCollect1 } from '@/api/blogController'

interface UseBlogActionsOptions {
  blogId: number | string | undefined
  initialLikeCount?: number
  initialCollectCount?: number
  autoCheck?: boolean
}

export function useBlogActions(options: UseBlogActionsOptions) {
  const { blogId, initialLikeCount = 0, initialCollectCount = 0, autoCheck = true } = options

  // 状态
  const isLiked = ref(false)
  const isCollected = ref(false)
  const likeCount = ref(initialLikeCount)
  const collectCount = ref(initialCollectCount)
  const liking = ref(false)
  const collecting = ref(false)
  const checking = ref(false)

  // 用于触发动画的标志
  const justLiked = ref(false)
  const justCollected = ref(false)

  // 检查是否已点赞
  const checkLikeStatus = async () => {
    if (!blogId) return
    try {
      const response = await checkBlogLike({ blogId })
      const code = response.data?.code
      const data = response.data?.data
      if ((code === 200 || code === 0) && data !== undefined) {
        isLiked.value = data === true
      }
    } catch (error) {
      console.error('检查点赞状态失败:', error)
    }
  }

  // 检查是否已收藏
  const checkCollectStatus = async () => {
    if (!blogId) return
    try {
      const response = await checkCollect1({ blogId })
      const code = response.data?.code
      const data = response.data?.data
      if ((code === 200 || code === 0) && data !== undefined) {
        isCollected.value = data === true
      }
    } catch (error) {
      console.error('检查收藏状态失败:', error)
    }
  }

  // 初始化检查状态
  const initCheck = async () => {
    if (!blogId || !autoCheck) return
    checking.value = true
    try {
      await Promise.all([checkLikeStatus(), checkCollectStatus()])
    } finally {
      checking.value = false
    }
  }

  // 点赞/取消点赞
  const handleLike = async () => {
    if (!blogId || liking.value) return

    const wasLiked = isLiked.value
    liking.value = true

    try {
      // 调用点赞切换接口
      const response = await likesToggle({ blogId })

      const toggleCode = response.data?.code
      if (toggleCode === 200 || toggleCode === 0) {
        // 调用检查接口获取最新状态
        const checkResponse = await checkBlogLike({ blogId })

        const checkCode = checkResponse.data?.code
        const checkData = checkResponse.data?.data
        if ((checkCode === 200 || checkCode === 0) && checkData !== undefined) {
          const newLikedState = checkData === true
          isLiked.value = newLikedState

          // 更新计数
          if (newLikedState && !wasLiked) {
            likeCount.value = likeCount.value + 1
          } else if (!newLikedState && wasLiked) {
            likeCount.value = Math.max(0, likeCount.value - 1)
          }

          // 如果是点赞操作（不是取消），触发动画
          if (newLikedState && !wasLiked) {
            justLiked.value = false
            await nextTick()
            justLiked.value = true
            // 0.6秒后重置动画标志
            setTimeout(() => {
              justLiked.value = false
            }, 600)
          }
        }
      } else {
        console.error('操作失败:', response.data)
      }
    } catch (error: any) {
      console.error('点赞操作失败:', error)
    } finally {
      liking.value = false
    }
  }

  // 收藏/取消收藏
  const handleCollect = async () => {
    if (!blogId || collecting.value) return

    const wasCollected = isCollected.value
    collecting.value = true

    try {
      // 调用收藏切换接口
      const response = await collectToggle1({ blogId })

      const toggleCode = response.data?.code
      if (toggleCode === 200 || toggleCode === 0) {
        // 调用检查接口获取最新状态
        const checkResponse = await checkCollect1({ blogId })

        const checkCode = checkResponse.data?.code
        const checkData = checkResponse.data?.data
        if ((checkCode === 200 || checkCode === 0) && checkData !== undefined) {
          const newCollectedState = checkData === true
          isCollected.value = newCollectedState

          // 更新计数
          if (newCollectedState && !wasCollected) {
            collectCount.value = collectCount.value + 1
          } else if (!newCollectedState && wasCollected) {
            collectCount.value = Math.max(0, collectCount.value - 1)
          }

          // 如果是收藏操作（不是取消），触发动画
          if (newCollectedState && !wasCollected) {
            justCollected.value = false
            await nextTick()
            justCollected.value = true
            // 0.6秒后重置动画标志
            setTimeout(() => {
              justCollected.value = false
            }, 600)
          }
        }
      } else {
        console.error('操作失败:', response.data)
      }
    } catch (error: any) {
      console.error('收藏操作失败:', error)
    } finally {
      collecting.value = false
    }
  }

  // 监听 blogId 变化，重新检查状态
  watch(
    () => blogId,
    (newId) => {
      if (newId && autoCheck) {
        initCheck()
      }
    },
    { immediate: true },
  )

  return {
    // 状态
    isLiked: computed(() => isLiked.value),
    isCollected: computed(() => isCollected.value),
    likeCount: computed(() => likeCount.value),
    collectCount: computed(() => collectCount.value),
    liking: computed(() => liking.value),
    collecting: computed(() => collecting.value),
    checking: computed(() => checking.value),

    // 动画标志
    justLiked: computed(() => justLiked.value),
    justCollected: computed(() => justCollected.value),

    // 方法
    handleLike,
    handleCollect,
    checkLikeStatus,
    checkCollectStatus,
    initCheck,
  }
}
