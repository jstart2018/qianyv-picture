/**
 * 图片管理组合式函数
 */

import { ref, computed } from 'vue'
import { list } from '@/api/pictureController'

export function usePicture() {
  const pictures = ref<any[]>([])
  const pictureTotal = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(20)
  const pictureLoading = ref(false)
  const pictureSearchText = ref('')

  const totalPages = computed(() => {
    return Math.ceil(pictureTotal.value / pageSize.value)
  })

  const hasMore = computed(() => {
    return currentPage.value < totalPages.value
  })

  /**
   * 获取图片列表
   */
  const fetchPictures = async (spaceId: number | null) => {
    if (!spaceId) {
      pictures.value = []
      return
    }

    try {
      pictureLoading.value = true
      const res = await list({
        current: currentPage.value,
        pageSize: pageSize.value,
        spaceId: spaceId,
        searchText: pictureSearchText.value,
      })

      if (res.data && res.data.code === 0) {
        const data = res.data.data
        pictures.value = data.records || []
        pictureTotal.value = data.total || 0
      } else {
        console.error('获取图片列表失败', res.data?.message)
        pictures.value = []
        pictureTotal.value = 0
      }
    } catch (e) {
      console.error('fetchPictures error', e)
      pictures.value = []
      pictureTotal.value = 0
    } finally {
      pictureLoading.value = false
    }
  }

  /**
   * 搜索图片
   */
  const handleSearch = (spaceId: number | null) => {
    currentPage.value = 1
    fetchPictures(spaceId)
  }

  /**
   * 翻页
   */
  const handlePageChange = (page: number, spaceId: number | null) => {
    currentPage.value = page
    fetchPictures(spaceId)
  }

  /**
   * 重置列表
   */
  const resetList = () => {
    pictures.value = []
    pictureTotal.value = 0
    currentPage.value = 1
    pictureSearchText.value = ''
  }

  return {
    pictures,
    pictureTotal,
    currentPage,
    pageSize,
    pictureLoading,
    pictureSearchText,
    totalPages,
    hasMore,
    fetchPictures,
    handleSearch,
    handlePageChange,
    resetList,
  }
}
