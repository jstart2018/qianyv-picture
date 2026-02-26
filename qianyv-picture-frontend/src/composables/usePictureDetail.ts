/**
 * 图片详情页组合式函数
 * 统一管理图片详情的获取、收藏、下载等逻辑
 */

import { ref, onMounted, type Ref } from 'vue'
import { message } from 'ant-design-vue'
import { getOneById, checkCollect, collectToggle } from '@/api/pictureController'

export interface UsePictureDetailOptions {
  /** 图片 ID */
  pictureId: Ref<string> | string
  /** 是否自动加载 */
  autoLoad?: boolean
}

export interface UsePictureDetailReturn {
  /** 图片数据 */
  picture: Ref<any>
  /** 加载状态 */
  loading: Ref<boolean>
  /** 错误信息 */
  error: Ref<string>
  /** 下载状态 */
  downloading: Ref<boolean>
  /** 收藏操作状态 */
  collecting: Ref<boolean>
  /** 是否已收藏 */
  isCollected: Ref<boolean>
  /** 获取图片详情 */
  fetchPictureDetail: () => Promise<void>
  /** 检查收藏状态 */
  checkCollectStatus: () => Promise<void>
  /** 处理下载 */
  handleDownload: () => Promise<void>
  /** 处理收藏/取消收藏 */
  handleCollect: () => Promise<void>
}

/**
 * 图片详情组合式函数
 * @param options 配置选项
 * @returns 图片详情相关状态和方法
 * @example
 * const { picture, loading, isCollected, handleDownload, handleCollect } = usePictureDetail({
 *   pictureId: computed(() => route.params.id as string),
 *   autoLoad: true
 * })
 */
export function usePictureDetail(options: UsePictureDetailOptions): UsePictureDetailReturn {
  const picture = ref<any>(null)
  const loading = ref(false)
  const error = ref<string>('')
  const downloading = ref(false)
  const collecting = ref(false)
  const isCollected = ref(false)

  const pictureId =
    typeof options.pictureId === 'string' ? ref(options.pictureId) : options.pictureId

  // 获取图片详情
  const fetchPictureDetail = async () => {
    loading.value = true
    error.value = ''

    try {
      // ID 保持字符串类型传递，避免精度丢失
      const res = await getOneById({ id: pictureId.value } as any)

      if (res.data.code === 0 && res.data.data) {
        picture.value = res.data.data
        // 获取图片后检查收藏状态
        await checkCollectStatus()
      } else {
        error.value = res.data.message || '获取图片详情失败'
      }
    } catch (err) {
      console.error('获取图片详情失败:', err)
      error.value = '网络错误，请稍后重试'
    } finally {
      loading.value = false
    }
  }

  // 检查收藏状态
  const checkCollectStatus = async () => {
    try {
      // 使用正确的API接口检查收藏状态
      // 保持 ID 为字符串，避免精度丢失
      const res = await checkCollect({ id: pictureId.value } as any)

      if (res.data.code === 0 && res.data.data !== undefined) {
        // data 返回的是数字：1表示已收藏，0表示未收藏
        isCollected.value = Number(res.data.data) === 1
      }
    } catch (err) {
      console.error('检查收藏状态失败:', err)
    }
  }

  // 处理下载
  const handleDownload = async () => {
    if (downloading.value) return

    downloading.value = true
    try {
      // 发送下载请求获取图片URL
      const res = await fetch('/api/picture/download', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ pictureId: pictureId.value }),
      })
      const data = await res.json()

      if (data.code === 0 && data.data) {
        // 获取到图片URL后直接下载
        const link = document.createElement('a')
        link.href = data.data
        link.download = picture.value.picName || 'image'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      } else {
        message.error(data.message || '下载失败')
      }
    } catch (err) {
      console.error('下载失败:', err)
      message.error('下载失败，请稍后重试')
    } finally {
      downloading.value = false
    }
  }

  // 处理收藏/取消收藏
  const handleCollect = async () => {
    if (collecting.value) return

    collecting.value = true
    try {
      const res = await collectToggle({ id: pictureId.value } as any)

      if (res.data.code === 0) {
        // 切换收藏状态
        isCollected.value = !isCollected.value
      } else {
        message.error(res.data.message || '操作失败')
      }
    } catch (err) {
      console.error('收藏操作失败:', err)
      message.error('操作失败，请稍后重试')
    } finally {
      collecting.value = false
    }
  }

  // 自动加载
  if (options.autoLoad !== false) {
    onMounted(() => {
      if (pictureId.value) {
        fetchPictureDetail()
      }
    })
  }

  return {
    picture,
    loading,
    error,
    downloading,
    collecting,
    isCollected,
    fetchPictureDetail,
    checkCollectStatus,
    handleDownload,
    handleCollect,
  }
}
