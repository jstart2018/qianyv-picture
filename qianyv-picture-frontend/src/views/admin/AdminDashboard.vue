<script setup lang="ts">
import {
  UserOutlined,
  FileTextOutlined,
  PictureOutlined,
  AppstoreOutlined,
} from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'
import { count as getUserCount } from '@/api/userController'
import { count1 as getSpaceCount } from '@/api/spaceController'
import { count2 as getPictureCount } from '@/api/pictureController'
import { countBlog } from '@/api/blogController'

const router = useRouter()

const userCount = ref<number | string>('-')
const spaceCount = ref<number | string>('-')
const pictureCount = ref<number | string>('-')
const blogCount = ref<number | string>('-')

const loadStats = async () => {
  try {
    const [userRes, spaceRes, pictureRes, blogRes] = await Promise.all([
      getUserCount(),
      getSpaceCount(),
      getPictureCount(),
      countBlog(),
    ])
    if (userRes.data?.code === 0) userCount.value = userRes.data.data ?? '-'
    if (spaceRes.data?.code === 0) spaceCount.value = spaceRes.data.data ?? '-'
    if (pictureRes.data?.code === 0) pictureCount.value = pictureRes.data.data ?? '-'
    if (blogRes.data?.code === 0) blogCount.value = blogRes.data.data ?? '-'
  } catch (err) {
    console.error('加载统计数据失败:', err)
  }
}

onMounted(() => {
  loadStats()
})

const modules = [
  {
    title: '用户管理',
    desc: '管理用户账号、角色权限和用户状态',
    icon: UserOutlined,
    color: '#1890ff',
    bg: '#e6f4ff',
    route: '/admin/users',
  },
  {
    title: '博客管理',
    desc: '管理博客内容、审核和评论',
    icon: FileTextOutlined,
    color: '#52c41a',
    bg: '#f6ffed',
    route: '/admin/blogs',
  },
  {
    title: '图片管理',
    desc: '管理图片资源、分类和审核',
    icon: PictureOutlined,
    color: '#fa8c16',
    bg: '#fff7e6',
    route: '/admin/pictures',
  },
  {
    title: '空间管理',
    desc: '管理团队空间、成员和容量',
    icon: AppstoreOutlined,
    color: '#722ed1',
    bg: '#f9f0ff',
    route: '/admin/spaces',
  },
]
</script>

<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h2>管理控制台</h2>
      <p class="dashboard-desc">欢迎使用千语壁纸管理后台</p>
    </div>

    <a-row :gutter="[24, 24]" class="module-cards">
      <a-col :xs="24" :sm="12" :lg="6" v-for="item in modules" :key="item.title">
        <a-card hoverable class="module-card" @click="router.push(item.route)">
          <div class="module-icon" :style="{ background: item.bg }">
            <component :is="item.icon" :style="{ color: item.color, fontSize: '28px' }" />
          </div>
          <h3 class="module-title">{{ item.title }}</h3>
          <p class="module-desc">{{ item.desc }}</p>
        </a-card>
      </a-col>
    </a-row>
    <a-card class="quick-stats" title="系统概览">
      <a-row :gutter="24">
        <a-col :span="6">
          <a-statistic title="用户总数" :value="userCount" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="博客总数" :value="blogCount" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="图片总数" :value="pictureCount" />
        </a-col>
        <a-col :span="6">
          <a-statistic title="空间总数" :value="spaceCount" />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<style scoped>
.admin-dashboard {
  /* 无自定义背景，完全使用 Ant Design 的白色 */
}

.dashboard-header {
  margin-bottom: 24px;
}

.dashboard-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #1a1a1a;
}

.dashboard-desc {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.module-cards {
  margin-bottom: 24px;
}

.module-card {
  text-align: center;
  border-radius: 12px;
  transition: all 0.3s;
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.module-icon {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.module-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #333;
}

.module-desc {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.quick-stats {
  border-radius: 12px;
}
</style>
