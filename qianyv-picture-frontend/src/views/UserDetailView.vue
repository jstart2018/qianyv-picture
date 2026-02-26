<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import * as userApi from '@/api/userController'
import * as blogApi from '@/api/blogController'
import * as pictureApi from '@/api/pictureController'
import UserAvatar from '@/components/UserAvatar.vue'
import { useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const userId = computed(() => route.params.id as string)

// 用户信息
const userInfo = ref<API.UserInfoVO | null>(null)
const loading = ref(true)

// 主选项卡状态（博客、图片）
const activeTab = ref<'blogs' | 'pictures'>('blogs')
const indicatorStyle = ref({ left: '0px', width: '0px' })

// 子选项卡状态
// 博客：我发布的、点赞、收藏
const blogSubTab = ref<'myPosts' | 'liked' | 'collected'>('myPosts')
// 图片：我发布的、收藏
const pictureSubTab = ref<'myPosts' | 'collected'>('myPosts')
const subIndicatorStyle = ref({ left: '0px', width: '0px' })

// 数据列表
const myBlogs = ref<API.BlogSimpleVO[]>([]) // 我发布的博客
const likedBlogs = ref<API.BlogSimpleVO[]>([]) // 点赞的博客
const collectedBlogs = ref<API.BlogSimpleVO[]>([]) // 收藏的博客
const myPictures = ref<API.PictureListVO[]>([]) // 我发布的图片
const collectedPictures = ref<API.PictureListVO[]>([]) // 收藏的图片

// 分页参数（每页固定6条）
const myBlogsPagination = ref({ current: 1, pageSize: 6, total: 0 })
const likedBlogsPagination = ref({ current: 1, pageSize: 6, total: 0 })
const collectedBlogsPagination = ref({ current: 1, pageSize: 6, total: 0 })
const myPicturesPagination = ref({ current: 1, pageSize: 12, total: 0 })
const collectedPicturesPagination = ref({ current: 1, pageSize: 12, total: 0 })

// 加载状态
const loadingMyBlogs = ref(false)
const loadingLikedBlogs = ref(false)
const loadingCollectedBlogs = ref(false)
const loadingMyPictures = ref(false)
const loadingCollectedPictures = ref(false)

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    loading.value = true
    // @ts-ignore - ID作为路径参数会被转为字符串，避免大整数精度丢失
    const res = await userApi.getById({ id: userId.value })
    if (res.data?.code === 0 && res.data?.data) {
      userInfo.value = res.data.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取我发布的图片
const fetchMyPictures = async (page = 1) => {
  try {
    loadingMyPictures.value = true
    const params: API.PicturePageQueryDTO = {
      current: page,
      pageSize: myPicturesPagination.value.pageSize,
      // @ts-ignore - ID作为字符串传递，避免大整数精度丢失
      userId: userId.value,
    }
    const res = await pictureApi.listByPage(params)
    if (res.data?.code === 0 && res.data?.data) {
      myPictures.value = res.data.data.records || []
      myPicturesPagination.value.current = page
      // 直接使用后端返回的total
      myPicturesPagination.value.total = res.data.data.total || 0

      console.log('我发布的图片分页信息:', {
        current: myPicturesPagination.value.current,
        pageSize: myPicturesPagination.value.pageSize,
        total: myPicturesPagination.value.total,
        dataLength: res.data.data.records?.length || 0,
      })
    }
  } catch (error) {
    console.error('获取我发布的图片失败:', error)
    myPictures.value = []
  } finally {
    loadingMyPictures.value = false
  }
}

// 获取收藏的图片
const fetchCollectedPictures = async (page = 1) => {
  try {
    loadingCollectedPictures.value = true
    const params: API.PicturePageQueryDTO = {
      current: page,
      pageSize: collectedPicturesPagination.value.pageSize,
      myCollect: true, // 查询我收藏的
    }
    const res = await pictureApi.listByPage(params)
    if (res.data?.code === 0 && res.data?.data) {
      collectedPictures.value = res.data.data.records || []
      collectedPicturesPagination.value.current = page
      // 直接使用后端返回的total
      collectedPicturesPagination.value.total = res.data.data.total || 0
    }
  } catch (error) {
    console.error('获取收藏图片失败:', error)
    collectedPictures.value = []
  } finally {
    loadingCollectedPictures.value = false
  }
}

// 获取我发布的博客
const fetchMyBlogs = async (page = 1) => {
  try {
    loadingMyBlogs.value = true
    const params: API.BlogPageQueryDTO = {
      current: page,
      pageSize: myBlogsPagination.value.pageSize,
      // @ts-ignore - ID作为字符串传递，避免大整数精度丢失
      userId: userId.value, // 保持字符串类型，不转换为Number
    }
    const res = await blogApi.blogListByPage(params)
    if (res.data?.code === 0 && res.data?.data) {
      myBlogs.value = res.data.data.records || []
      myBlogsPagination.value.current = page
      // 直接使用后端返回的total
      myBlogsPagination.value.total = res.data.data.total || 0

      console.log('我发布的博客分页信息:', {
        current: myBlogsPagination.value.current,
        pageSize: myBlogsPagination.value.pageSize,
        total: myBlogsPagination.value.total,
        dataLength: res.data.data.records?.length || 0,
      })
    }
  } catch (error) {
    console.error('获取我发布的博客失败:', error)
    myBlogs.value = []
  } finally {
    loadingMyBlogs.value = false
  }
}

// 获取点赞的博客
const fetchLikedBlogs = async (page = 1) => {
  try {
    loadingLikedBlogs.value = true
    const params: API.BlogPageQueryDTO = {
      current: page,
      pageSize: likedBlogsPagination.value.pageSize,
      myLike: true, // 查询我点赞的
    }
    const res = await blogApi.blogListByPage(params)
    if (res.data?.code === 0 && res.data?.data) {
      likedBlogs.value = res.data.data.records || []
      likedBlogsPagination.value.current = page
      // 直接使用后端返回的total
      likedBlogsPagination.value.total = res.data.data.total || 0
    }
  } catch (error) {
    console.error('获取点赞博客失败:', error)
    likedBlogs.value = []
  } finally {
    loadingLikedBlogs.value = false
  }
}

// 获取收藏的博客
const fetchCollectedBlogs = async (page = 1) => {
  try {
    loadingCollectedBlogs.value = true
    const params: API.BlogPageQueryDTO = {
      current: page,
      pageSize: collectedBlogsPagination.value.pageSize,
      myCollect: true, // 查询我收藏的
    }
    const res = await blogApi.blogListByPage(params)
    if (res.data?.code === 0 && res.data?.data) {
      collectedBlogs.value = res.data.data.records || []
      collectedBlogsPagination.value.current = page
      // 直接使用后端返回的total
      collectedBlogsPagination.value.total = res.data.data.total || 0
    }
  } catch (error) {
    console.error('获取收藏博客失败:', error)
    collectedBlogs.value = []
  } finally {
    loadingCollectedBlogs.value = false
  }
}

// 切换主选项卡
const switchTab = (tab: typeof activeTab.value) => {
  activeTab.value = tab
  nextTick(() => updateIndicator())
}

// 切换博客子选项卡
const switchBlogSubTab = (tab: typeof blogSubTab.value) => {
  blogSubTab.value = tab
  nextTick(() => updateSubIndicator())
  // 切换子选项卡时重置分页并加载对应的数据
  if (tab === 'myPosts') {
    myBlogsPagination.value.current = 1
    fetchMyBlogs(1)
  } else if (tab === 'liked') {
    likedBlogsPagination.value.current = 1
    fetchLikedBlogs(1)
  } else if (tab === 'collected') {
    collectedBlogsPagination.value.current = 1
    fetchCollectedBlogs(1)
  }
}

// 分页切换处理
const handlePageChange = (page: number) => {
  if (blogSubTab.value === 'myPosts') {
    fetchMyBlogs(page)
  } else if (blogSubTab.value === 'liked') {
    fetchLikedBlogs(page)
  } else if (blogSubTab.value === 'collected') {
    fetchCollectedBlogs(page)
  }
}

// 获取当前激活的分页对象
const currentPagination = computed(() => {
  if (activeTab.value === 'blogs') {
    switch (blogSubTab.value) {
      case 'myPosts':
        return myBlogsPagination.value
      case 'liked':
        return likedBlogsPagination.value
      case 'collected':
        return collectedBlogsPagination.value
    }
  }
  return { current: 1, pageSize: 6, total: 0 }
})

// 切换图片子选项卡
const switchPictureSubTab = (tab: typeof pictureSubTab.value) => {
  pictureSubTab.value = tab
  nextTick(() => updateSubIndicator())
  // 切换子选项卡时重置分页并加载对应的数据
  if (tab === 'myPosts') {
    myPicturesPagination.value.current = 1
    fetchMyPictures(1)
  } else if (tab === 'collected') {
    collectedPicturesPagination.value.current = 1
    fetchCollectedPictures(1)
  }
}

// 图片分页切换处理
const handlePicturePageChange = (page: number) => {
  if (pictureSubTab.value === 'myPosts') {
    fetchMyPictures(page)
  } else if (pictureSubTab.value === 'collected') {
    fetchCollectedPictures(page)
  }
}

// 获取当前激活的图片分页对象
const currentPicturePagination = computed(() => {
  if (activeTab.value === 'pictures') {
    switch (pictureSubTab.value) {
      case 'myPosts':
        return myPicturesPagination.value
      case 'collected':
        return collectedPicturesPagination.value
    }
  }
  return { current: 1, pageSize: 12, total: 0 }
})

// 更新主选项卡指示器位置
const updateIndicator = () => {
  const tabs = document.querySelectorAll('.main-tab-btn')
  const tabNames: (typeof activeTab.value)[] = ['blogs', 'pictures']
  const activeTabIndex = tabNames.indexOf(activeTab.value)
  if (tabs[activeTabIndex]) {
    const activeTabEl = tabs[activeTabIndex] as HTMLElement
    indicatorStyle.value = {
      left: `${activeTabEl.offsetLeft}px`,
      width: `${activeTabEl.offsetWidth}px`,
    }
  }
}

// 更新子选项卡指示器位置
const updateSubIndicator = () => {
  const tabs = document.querySelectorAll('.sub-tab-btn')
  if (tabs.length === 0) return

  let activeTabIndex = -1
  if (activeTab.value === 'blogs') {
    const tabNames: (typeof blogSubTab.value)[] = ['myPosts', 'liked', 'collected']
    activeTabIndex = tabNames.indexOf(blogSubTab.value)
  } else if (activeTab.value === 'pictures') {
    const tabNames: (typeof pictureSubTab.value)[] = ['myPosts', 'collected']
    activeTabIndex = tabNames.indexOf(pictureSubTab.value)
  }

  if (activeTabIndex >= 0 && tabs[activeTabIndex]) {
    const activeTabEl = tabs[activeTabIndex] as HTMLElement
    subIndicatorStyle.value = {
      left: `${activeTabEl.offsetLeft}px`,
      width: `${activeTabEl.offsetWidth}px`,
    }
  }
}

// 根据当前选项卡获取内容
const currentContent = computed(() => {
  if (activeTab.value === 'blogs') {
    switch (blogSubTab.value) {
      case 'myPosts':
        return myBlogs.value
      case 'liked':
        return likedBlogs.value
      case 'collected':
        return collectedBlogs.value
    }
  } else if (activeTab.value === 'pictures') {
    switch (pictureSubTab.value) {
      case 'myPosts':
        return myPictures.value
      case 'collected':
        return collectedPictures.value
    }
  }
  return []
})

// 跳转到博客详情
const goToBlogDetail = (blogId: number | string | undefined) => {
  if (!blogId) return
  // 保持字符串类型，避免大整数精度丢失
  router.push({ name: 'blogDetail', params: { id: String(blogId) } })
}

// 跳转到图片详情
const goToPictureDetail = (pictureId: number | string | undefined, picScale?: number) => {
  if (!pictureId) return
  // 根据图片比例决定跳转到横图还是竖图详情页
  const routeName = picScale && picScale > 1 ? 'horizontalPictureDetail' : 'verticalPictureDetail'
  router.push({ name: routeName, params: { id: String(pictureId) } })
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '未知时间'
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const months = Math.floor(days / 30)

  if (months > 0) return `${months}月前`
  if (days > 0) return `${days}天前`
  const hours = Math.floor(diff / (1000 * 60 * 60))
  if (hours > 0) return `${hours}小时前`
  const minutes = Math.floor(diff / (1000 * 60))
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

onMounted(async () => {
  await fetchUserInfo()
  await Promise.all([
    fetchMyPictures(1),
    fetchMyBlogs(1), // 默认加载“我发布的”博客
  ])
  // 等待 DOM 更新后再更新指示器
  nextTick(() => {
    updateIndicator()
    updateSubIndicator()
  })
})
</script>

<template>
  <div class="user-detail-view">
    <!-- 左侧：用户信息和资料 -->
    <div class="left-section">
      <!-- 用户信息头部 -->
      <div v-if="userInfo" class="user-header-card">
        <div class="user-info-row">
          <UserAvatar :nickname="userInfo.nickname" :avatar="userInfo.avatar" :size="80" />
          <div class="user-details">
            <h1 class="username">{{ userInfo.nickname }}</h1>
            <p class="user-bio">{{ userInfo.introduction || '这个人很神秘，什么也没有留下~' }}</p>
          </div>
        </div>

        <!-- 统计数据 -->
        <div class="user-stats-row">
          <div class="stat-item">
            <span class="stat-label">发贴</span>
            <span class="stat-value">{{ userInfo.publishCount || 0 }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">分享</span>
            <span class="stat-value">{{ myPictures.length }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">获赞</span>
            <span class="stat-value">{{ userInfo.likeCount || 0 }}</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-label">获藏</span>
            <span class="stat-value">{{ userInfo.collectCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 资料信息 -->
      <div class="user-info-detail">
        <div class="info-section-title">资料信息</div>
        <div class="info-row">
          <span class="info-label">昵称：</span>
          <span class="info-value">{{ userInfo?.nickname || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">签名：</span>
          <span class="info-value">{{ userInfo?.tag || '-' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">简介：</span>
          <span class="info-value">{{ userInfo?.introduction || '-' }}</span>
        </div>
      </div>
    </div>

    <!-- 右侧：内容区域 -->
    <div class="right-section">
      <!-- 主选项卡 -->
      <div class="main-tabs-container">
        <div class="main-tabs-wrapper">
          <div class="main-tab-indicator" :style="indicatorStyle"></div>
          <button
            class="main-tab-btn"
            :class="{ active: activeTab === 'blogs' }"
            @click="switchTab('blogs')"
          >
            博客
          </button>
          <button
            class="main-tab-btn"
            :class="{ active: activeTab === 'pictures' }"
            @click="switchTab('pictures')"
          >
            图片
          </button>
        </div>
      </div>

      <!-- 子选项卡（博客：我发布的、点赞、收藏） -->
      <div v-if="activeTab === 'blogs'" class="sub-tabs-container">
        <div class="sub-tabs-wrapper">
          <div class="sub-tab-indicator" :style="subIndicatorStyle"></div>
          <button
            class="sub-tab-btn"
            :class="{ active: blogSubTab === 'myPosts' }"
            @click="switchBlogSubTab('myPosts')"
          >
            我发布的
          </button>
          <button
            class="sub-tab-btn"
            :class="{ active: blogSubTab === 'liked' }"
            @click="switchBlogSubTab('liked')"
          >
            点赞
          </button>
          <button
            class="sub-tab-btn"
            :class="{ active: blogSubTab === 'collected' }"
            @click="switchBlogSubTab('collected')"
          >
            收藏
          </button>
        </div>
      </div>

      <!-- 子选项卡（图片：我发布的、收藏） -->
      <div v-if="activeTab === 'pictures'" class="sub-tabs-container">
        <div class="sub-tabs-wrapper">
          <div class="sub-tab-indicator" :style="subIndicatorStyle"></div>
          <button
            class="sub-tab-btn"
            :class="{ active: pictureSubTab === 'myPosts' }"
            @click="switchPictureSubTab('myPosts')"
          >
            我发布的
          </button>
          <button
            class="sub-tab-btn"
            :class="{ active: pictureSubTab === 'collected' }"
            @click="switchPictureSubTab('collected')"
          >
            收藏
          </button>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 博客内容 -->
        <div v-if="activeTab === 'blogs'" class="blogs-list">
          <div v-if="myBlogs.length > 0 || likedBlogs.length > 0 || collectedBlogs.length > 0">
            <div class="blogs-scroll-container">
              <div
                v-for="blog in currentContent as API.BlogSimpleVO[]"
                :key="blog.id"
                class="simple-blog-card"
                @click="goToBlogDetail(blog.id!)"
              >
                <!-- 博客头部：用户信息 + 统计信息 -->
                <div class="blog-card-header">
                  <div class="blog-card-left">
                    <UserAvatar
                      :nickname="blog.user?.nickname"
                      :avatar="blog.user?.avatar"
                      :size="40"
                    />
                    <div class="blog-card-user-info">
                      <span class="blog-card-username">{{
                        blog.user?.nickname || '匿名用户'
                      }}</span>
                      <span class="blog-card-time"
                        >发布时间：{{ formatTime(blog.updateTime) }}</span
                      >
                    </div>
                  </div>

                  <!-- 点赞和收藏统计 -->
                  <div class="blog-card-stats">
                    <div class="stat-item">
                      <svg
                        class="stat-icon"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <path
                          d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"
                        />
                      </svg>
                      <span>{{ blog.likeCount || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <svg
                        class="stat-icon"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <polygon
                          points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"
                        />
                      </svg>
                      <span>{{ blog.collectCount || 0 }}</span>
                    </div>
                  </div>
                </div>

                <!-- 博客内容 -->
                <h3 class="simple-blog-title">{{ blog.title || '无标题' }}</h3>
                <p class="simple-blog-content">{{ blog.content || '暂无内容' }}</p>
              </div>
            </div>

            <!-- 分页器 -->
            <div v-if="currentPagination.total > 0" class="pagination-container">
              <a-pagination
                v-model:current="currentPagination.current"
                :total="currentPagination.total"
                :page-size="currentPagination.pageSize"
                :show-total="(total: number) => `共 ${total} 条`"
                :show-size-changer="false"
                show-quick-jumper
                @change="handlePageChange"
              />
            </div>
            <!-- 调试信息 -->
            <div v-if="currentPagination" class="debug-info">
              <p>
                当前页: {{ currentPagination.current }} | 每页: {{ currentPagination.pageSize }} |
                总数: {{ currentPagination.total }}
              </p>
            </div>
          </div>
          <div v-else class="empty-content">
            <p>暂无内容</p>
          </div>
        </div>

        <!-- 图片内容 -->
        <div v-if="activeTab === 'pictures'" class="pictures-grid">
          <div
            v-if="
              (pictureSubTab === 'myPosts' && myPictures.length > 0) ||
              (pictureSubTab === 'collected' && collectedPictures.length > 0)
            "
          >
            <div class="pictures-masonry">
              <div
                v-for="pic in pictureSubTab === 'myPosts' ? myPictures : collectedPictures"
                :key="pic.id"
                class="picture-card"
                @click="goToPictureDetail(pic.id, pic.picScale)"
              >
                <img :src="pic.thumbUrl" :alt="pic.tags" />
                <div class="picture-overlay">
                  <span class="picture-tags">{{ pic.tags || '无标签' }}</span>
                </div>
              </div>
            </div>

            <!-- 图片分页器 -->
            <div v-if="currentPicturePagination.total > 0" class="pagination-container">
              <a-pagination
                v-model:current="currentPicturePagination.current"
                :total="currentPicturePagination.total"
                :page-size="currentPicturePagination.pageSize"
                :show-total="(total: number) => `共 ${total} 张`"
                :show-size-changer="false"
                show-quick-jumper
                @change="handlePicturePageChange"
              />
            </div>
            <!-- 调试信息 -->
            <div v-if="currentPicturePagination" class="debug-info">
              <p>
                当前页: {{ currentPicturePagination.current }} | 每页:
                {{ currentPicturePagination.pageSize }} | 总数:
                {{ currentPicturePagination.total }}
              </p>
            </div>
          </div>
          <div v-else class="empty-content">
            <p>暂无图片</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.user-detail-view {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
  min-height: 100vh;
}

/* ===== 左侧区域 ===== */
.left-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 用户信息头部 */
.user-header-card {
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-info-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.user-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username {
  font-size: 22px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
}

.user-bio {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
  line-height: 1.5;
}

.user-stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  align-items: center;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.9);
}

.stat-divider {
  display: none;
}

/* 资料信息 */
.user-info-detail {
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-section-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 8px;
}

.info-row {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  min-width: 60px;
}

.info-value {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  flex: 1;
  word-break: break-word;
}

/* ===== 右侧区域 ===== */
.right-section {
  display: flex;
  flex-direction: column;
  gap: 0; /* 移除选项卡之间的间距 */
}

/* 内容区域与选项卡之间保持间距 */
.content-area {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 主选项卡 */
.main-tabs-container {
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(10px);
  border-radius: 16px 16px 0 0;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-bottom: none;
  padding: 8px;
}

/* 当有子选项卡时，保持圆角 */
.main-tabs-container:has(+ .sub-tabs-container) {
  border-radius: 16px 16px 0 0;
}

/* 当没有子选项卡时，恢复完整圆角 */
.right-section > .main-tabs-container:not(:has(+ .sub-tabs-container)) {
  border-radius: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.main-tabs-wrapper {
  display: flex;
  position: relative;
  gap: 4px;
}

.main-tab-indicator {
  position: absolute;
  height: calc(100% - 8px);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.34, 1.4, 0.64, 1);
  z-index: 0;
  top: 4px;
}

.main-tab-btn {
  flex: 1;
  padding: 12px 16px;
  background: transparent;
  border: none;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.main-tab-btn:hover {
  color: rgba(255, 255, 255, 0.9);
}

.main-tab-btn.active {
  color: rgba(255, 255, 255, 1);
  font-weight: 600;
}

/* 子选项卡 */
.sub-tabs-container {
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 0 0 16px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-top: none;
  padding: 8px;
  margin-top: 0;
}

.sub-tabs-wrapper {
  display: flex;
  position: relative;
  gap: 4px;
}

.sub-tab-indicator {
  position: absolute;
  height: calc(100% - 8px);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 10px;
  transition: all 0.4s cubic-bezier(0.34, 1.4, 0.64, 1);
  z-index: 0;
  top: 4px;
}

.sub-tab-btn {
  flex: 1;
  padding: 10px 12px;
  background: transparent;
  border: none;
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  z-index: 1;
}

.sub-tab-btn:hover {
  color: rgba(255, 255, 255, 0.8);
}

.sub-tab-btn.active {
  color: rgba(255, 255, 255, 0.95);
  font-weight: 600;
}

/* 内容区域 */
.content-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.blogs-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 图片网格 */
.pictures-grid {
  display: flex;
  flex-direction: column;
}

.pictures-masonry {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.picture-card {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  aspect-ratio: 3 / 4;
}

.picture-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.picture-card:hover .picture-overlay {
  opacity: 1;
}

.picture-card img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.picture-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, rgba(0, 0, 0, 0) 100%);
  padding: 16px 12px 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.picture-tags {
  color: rgba(255, 255, 255, 0.95);
  font-size: 13px;
  font-weight: 500;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-content {
  text-align: center;
  padding: 80px 20px;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  color: rgba(255, 255, 255, 0.4);
  font-size: 15px;
}

/* 简单博客卡片 */
.blogs-scroll-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.simple-blog-card {
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 14px 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.simple-blog-card:hover {
  background: rgba(0, 0, 0, 0.4);
  border-color: rgba(138, 180, 248, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* 博客卡片头部 */
.blog-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.blog-card-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.blog-card-user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.blog-card-username {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.blog-card-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.simple-blog-title {
  font-size: 16px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.simple-blog-content {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 博客统计信息 */
.blog-card-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 分页器容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  margin-top: 10px;
}

/* 调试信息 */
.debug-info {
  text-align: center;
  padding: 10px;
  background: rgba(255, 255, 0, 0.1);
  border: 1px solid rgba(255, 255, 0, 0.3);
  border-radius: 8px;
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
}

.debug-info p {
  margin: 0;
}

/* 自定义分页器样式 */
:deep(.ant-pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.ant-pagination-item),
:deep(.ant-pagination-prev),
:deep(.ant-pagination-next) {
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.ant-pagination-item a),
:deep(.ant-pagination-prev .ant-pagination-item-link),
:deep(.ant-pagination-next .ant-pagination-item-link) {
  color: rgba(255, 255, 255, 0.7);
}

:deep(.ant-pagination-item:hover),
:deep(.ant-pagination-prev:hover),
:deep(.ant-pagination-next:hover) {
  background: rgba(0, 0, 0, 0.5);
  border-color: rgba(138, 180, 248, 0.3);
}

:deep(.ant-pagination-item:hover a),
:deep(.ant-pagination-prev:hover .ant-pagination-item-link),
:deep(.ant-pagination-next:hover .ant-pagination-item-link) {
  color: rgba(255, 255, 255, 0.9);
}

:deep(.ant-pagination-item-active) {
  background: rgba(138, 180, 248, 0.2);
  border-color: rgba(138, 180, 248, 0.5);
}

:deep(.ant-pagination-item-active a) {
  color: rgba(138, 180, 248, 1);
  font-weight: 600;
}

:deep(.ant-pagination-disabled) {
  opacity: 0.4;
  cursor: not-allowed;
}

:deep(.ant-pagination-total-text) {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
}

.blog-card-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.blog-card-stats .stat-icon {
  width: 16px;
  height: 16px;
  stroke: rgba(255, 255, 255, 0.5);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .user-detail-view {
    grid-template-columns: 1fr;
  }

  .left-section {
    order: 1;
  }

  .right-section {
    order: 2;
  }
}

@media (max-width: 768px) {
  .user-detail-view {
    padding: 20px 12px;
  }

  .user-header-card {
    padding: 16px;
  }

  .user-stats-row {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .main-tab-btn,
  .sub-tab-btn {
    font-size: 12px;
    padding: 10px 8px;
  }

  .pictures-masonry {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>
