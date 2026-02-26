import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { blogList } from '@/api/blogController'

export const useBlogStore = defineStore('blog', () => {
  // 博客列表
  const blogs = ref<API.BlogsVO[]>([])
  const loading = ref(false)
  const hasMore = ref(true) // 是否还有更多数据
  const lastId = ref<number | undefined>(undefined) // 记录最后一条数据的id
  const pageSize = 5 // 博客列表每页数据量

  // 筛选条件
  const searchText = ref('')
  const isRecommend = ref<boolean | undefined>(undefined)
  const upToDate = ref<boolean | undefined>(undefined)

  // 获取博客列表（游标分页）
  const fetchBlogList = async (reset = false) => {
    if (loading.value) return

    loading.value = true
    try {
      const params: API.BlogListDTO = {}

      // 首次查询不传id，后续传入上次返回的最后一条数据的id
      if (!reset && lastId.value !== undefined) {
        params.id = lastId.value
      }

      // 添加筛选条件（只有选中时才添加）
      if (searchText.value.trim()) {
        params.searchText = searchText.value.trim()
      }
      if (isRecommend.value !== undefined) {
        params.isRecommend = isRecommend.value ? 1 : 0
      }
      if (upToDate.value !== undefined) {
        params.upToDate = upToDate.value
      }

      const res = await blogList(params)

      // 修改判断条件：code === 0 表示成功
      if (res.data?.code === 0 && res.data?.data) {
        const newBlogs = res.data.data || []

        // 如果是重置（首次查询或筛选条件变化），替换数据；否则追加数据
        if (reset) {
          blogs.value = newBlogs
        } else {
          blogs.value = [...blogs.value, ...newBlogs]
        }

        // 更新lastId为本次返回数据的最后一条id
        if (newBlogs.length > 0) {
          const lastBlog = newBlogs[newBlogs.length - 1]
          lastId.value = lastBlog?.id
        } else if (reset) {
          // 如果reset且没有数据，重置lastId
          lastId.value = undefined
        }

        // 根据返回的数据量判断是否还有更多数据
        // 如果返回数据少于预期分页大小，说明已到最后一页
        hasMore.value = newBlogs.length === pageSize
      } else {
        console.warn('后端返回code不为0:', res.data?.code, res.data?.message)
        // 如果code不为0，也尝试使用mock数据
        if (reset) {
          blogs.value = generateMockBlogs()
          hasMore.value = true
        }
      }
    } catch (error) {
      console.error('获取博客列表失败:', error)
      // 如果后端接口失败，使用 mock 数据（仅用于开发调试）
      if (reset) {
        blogs.value = generateMockBlogs()
        hasMore.value = true
      }
    } finally {
      loading.value = false
    }
  }

  // 生成 Mock 博客数据（仅用于开发调试）
  const generateMockBlogs = (): API.BlogsVO[] => {
    const mockBlogs: API.BlogsVO[] = []
    for (let i = 1; i <= 10; i++) {
      mockBlogs.push({
        id: i,
        userId: i,
        title: `精美壁纸分享 #${i} - 探索视觉的无限可能`,
        content: `这是一组精心挑选的${['自然风光', '城市夜景', '抽象艺术', '动漫二次元', '极简设计'][i % 5]}壁纸合集。每一张图片都经过精心筛选，希望能为你的设备带来全新的视觉体验。这些壁纸不仅色彩丰富，构图精美，更能为你的日常生活增添一丝艺术气息。`,
        viewCount: Math.floor(Math.random() * 5000) + 100,
        likeCount: Math.floor(Math.random() * 500) + 10,
        commentCount: Math.floor(Math.random() * 50) + 2,
        collectCount: Math.floor(Math.random() * 300) + 5,
        isRecommend: i % 3 === 0 ? 1 : 0,
        user: {
          id: i,
          nickname: ['壁纸达人', '设计师小王', '美图收藏家', '创意无限', '色彩大师'][i % 5],
          avatar: '',
          gender: i % 2,
          tag: ['资深设计师', '摄影爱好者', '美学追求者', '视觉艺术家', '创意工作者'][i % 5],
          introduction: '分享美好，传递视觉享受',
        },
        pictureVOList: generateMockPictures(i),
      })
    }
    return mockBlogs
  }

  // 生成 Mock 图片数据
  const generateMockPictures = (blogId: number): API.PictureListVO[] => {
    const count = Math.floor(Math.random() * 6) + 2 // 2-7张图片
    const pictures: API.PictureListVO[] = []
    const categories = ['nature', 'city', 'abstract', 'anime', 'minimal']
    const category = categories[blogId % 5]

    for (let i = 0; i < count; i++) {
      pictures.push({
        id: blogId * 100 + i,
        thumbUrl: `https://picsum.photos/400/600?random=${blogId * 100 + i}`,
        tags: `${category},wallpaper,hd`,
        collectCount: Math.floor(Math.random() * 100) + 5,
      })
    }
    return pictures
  }

  // 注意：点赞和收藏功能已迁移到 useBlogActions composable
  // 这里保留空的函数以保持接口兼容性
  const toggleLike = async (blogId: number) => {
    // 已弃用，请使用 useBlogActions
  }

  const toggleCollect = async (blogId: number) => {
    // 已弃用，请使用 useBlogActions
  }

  const fetchComments = async (blogId: number, parentId?: number) => {
    // 暂未实现
    return []
  }

  // 重置筛选条件
  const resetFilters = () => {
    searchText.value = ''
    isRecommend.value = undefined
    upToDate.value = undefined
    lastId.value = undefined
  }

  return {
    blogs,
    loading,
    hasMore,
    searchText,
    isRecommend,
    upToDate,
    fetchBlogList,
    resetFilters,
    toggleLike,
    toggleCollect,
    fetchComments,
  }
})
