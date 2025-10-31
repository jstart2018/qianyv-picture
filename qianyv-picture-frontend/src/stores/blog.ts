import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { blogList, likeToggle, collectionsToggle, comments } from '@/api/blogController'

export const useBlogStore = defineStore('blog', () => {
  // 博客列表
  const blogs = ref<API.BlogsVO[]>([])
  const total = ref(0)
  const current = ref(1)
  const pageSize = ref(10)
  const loading = ref(false) // 获取博客列表
  const fetchBlogList = async (params?: Partial<API.BlogListDTO>) => {
    loading.value = true
    try {
      const res = await blogList({
        current: current.value,
        pageSize: pageSize.value,
        ...params,
      })

      // 调试日志已移除

      // 修改判断条件：code === 0 表示成功
      if (res.data?.code === 0 && res.data?.data) {
        // 如果是第一页，替换数据；否则追加数据
        if (current.value === 1) {
          blogs.value = res.data.data.records || []
        } else {
          blogs.value = [...blogs.value, ...(res.data.data.records || [])]
        }
        total.value = res.data.data.total || 0
        // 日志已移除
      } else {
        console.warn('后端返回code不为0:', res.data?.code, res.data?.message)
        // 如果code不为0，也尝试使用mock数据
        if (current.value === 1) {
          blogs.value = generateMockBlogs()
          total.value = 25
        }
      }
    } catch (error) {
      console.error('获取博客列表失败:', error)
      // 如果后端接口失败，使用 mock 数据（仅用于开发调试）
      if (current.value === 1) {
        blogs.value = generateMockBlogs()
        total.value = 25
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
  } // 点赞切换
  const toggleLike = async (blogId: number) => {
    try {
      const res = await likeToggle({
        blogLikeOrCollectDTO: { id: blogId },
      })
      // 修改判断条件：code === 0 表示成功
      if (res.data?.code === 0 && res.data?.data) {
        // 更新本地数据
        const blog = blogs.value.find((b) => b.id === blogId)
        if (blog) {
          blog.likeCount = res.data.data.count || 0
        }
        return res.data.data
      }
    } catch (error) {
      console.error('点赞操作失败:', error)
    }
  }

  // 收藏切换
  const toggleCollect = async (blogId: number) => {
    try {
      const res = await collectionsToggle({
        blogLikeOrCollectDTO: { id: blogId },
      })
      // 修改判断条件：code === 0 表示成功
      if (res.data?.code === 0 && res.data?.data) {
        // 更新本地数据
        const blog = blogs.value.find((b) => b.id === blogId)
        if (blog) {
          blog.collectCount = res.data.data.count || 0
        }
        return res.data.data
      }
    } catch (error) {
      console.error('收藏操作失败:', error)
    }
  }
  // 获取评论
  const fetchComments = async (blogId: number, parentId?: number) => {
    try {
      const res = await comments({
        blogCommentDTO: {
          id: blogId,
          parentId,
        },
      })
      // 修改判断条件：code === 0 表示成功
      if (res.data?.code === 0 && res.data?.data) {
        return res.data.data
      }
    } catch (error) {
      console.error('获取评论失败:', error)
    }
  }

  return {
    blogs,
    total,
    current,
    pageSize,
    loading,
    fetchBlogList,
    toggleLike,
    toggleCollect,
    fetchComments,
  }
})
