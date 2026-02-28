<script setup lang="ts">
import { ref, reactive, onMounted, h, computed } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
  DownloadOutlined,
  DeleteOutlined,
  TeamOutlined,
} from '@ant-design/icons-vue'
import * as spaceApi from '@/api/spaceController'
import * as pictureApi from '@/api/pictureController'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isBoss = computed(() => userStore.isBoss)

// ==================== 状态 ====================
const loading = ref(false)
const dataSource = ref<API.SpaceVO[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50'],
})

// 搜索条件（对应 SpaceQueryDTO）
const searchForm = reactive({
  spaceName: '' as string,
  spaceLevel: undefined as number | undefined,
  userId: '' as string,
})

// ==================== 等级映射 ====================
const spaceLevelMap: Record<number, { text: string; color: string }> = {
  0: { text: '免费版', color: '#a5aaa3' },
  1: { text: '普通版', color: '#705e78' },
  2: { text: '专业版', color: '#007bff' },
  3: { text: '旗舰版', color: '#c5221f' },
}

// 空间状态映射
const spaceStatusMap: Record<number, { text: string; color: string }> = {
  0: { text: '禁用', color: '#ff4d4f' },
  1: { text: '启用', color: '#52c41a' },
}

// ==================== 图片查看弹窗 ====================
const pictureModalVisible = ref(false)
const pictureModalTitle = ref('空间图片')
const pictureLoading = ref(false)
const pictureList = ref<API.PictureListVO[]>([])
const picturePagination = reactive({
  current: 1,
  pageSize: 12,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 张图片`,
  pageSizeOptions: ['12', '24', '48'],
})
const currentViewSpaceId = ref<number>()

// ==================== 成员查看弹窗 ====================
const memberModalVisible = ref(false)
const memberModalTitle = ref('空间成员')
const memberLoading = ref(false)
const memberList = ref<API.SpaceUserVO[]>([])
const memberPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 名成员`,
  pageSizeOptions: ['10', '20', '50'],
})
const currentMemberSpaceId = ref<number>()

// 空间角色映射
const spaceRoleMap: Record<number, { text: string; color: string }> = {
  0: { text: '浏览者', color: '#8c8c8c' },
  1: { text: '编辑者', color: '#1890ff' },
  2: { text: '管理员', color: '#722ed1' },
  3: { text: '创建者', color: '#faad14' },
}

// ==================== 表格列定义 ====================
const columns = ref([
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 160,
    minWidth: 80,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
    key: 'spaceName',
    width: 180,
    minWidth: 100,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '创建者ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 160,
    minWidth: 80,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '等级',
    dataIndex: 'spaceLevel',
    key: 'spaceLevel',
    width: 100,
    minWidth: 80,
    resizable: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 90,
    minWidth: 70,
    resizable: true,
  },
  {
    title: '成员数',
    dataIndex: 'memberCount',
    key: 'memberCount',
    width: 90,
    minWidth: 60,
    resizable: true,
  },
  {
    title: '图片数量',
    key: 'pictureUsage',
    width: 180,
    minWidth: 120,
    resizable: true,
  },
  {
    title: '容量使用',
    key: 'sizeUsage',
    width: 180,
    minWidth: 120,
    resizable: true,
  },
  { title: '操作', key: 'action', width: 300, fixed: 'right' as const },
])

// 列宽拖拽回调
const handleResizeColumn = (w: number, col: any) => {
  col.width = w
}

// ==================== 数据加载 ====================
const fetchData = async () => {
  loading.value = true
  try {
    const params: API.SpaceQueryDTO = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'id',
      sortOrder: 'desc',
    }
    if (searchForm.spaceName) params.spaceName = searchForm.spaceName
    if (searchForm.spaceLevel !== undefined) params.spaceLevel = searchForm.spaceLevel
    if (searchForm.userId) params.userId = searchForm.userId as any

    const res = await spaceApi.adminListSpaceByPage(params)
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      // records 类型为 SpaceVO[][] (可能是类型生成的问题)，安全处理
      const rawRecords = result.data.records || []
      dataSource.value = Array.isArray(rawRecords[0]) ? rawRecords.flat() : rawRecords
      pagination.total = result.data.total || 0
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error('获取空间列表失败:', e)
    dataSource.value = []
  } finally {
    loading.value = false
  }
}

// 分页变化
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.spaceName = ''
  searchForm.spaceLevel = undefined
  searchForm.userId = ''
  pagination.current = 1
  fetchData()
}

// ==================== 格式化工具 ====================
const copyId = (id: string | number) => {
  navigator.clipboard
    .writeText(String(id))
    .then(() => {
      message.success('ID 已复制到剪贴板')
    })
    .catch(() => {
      message.error('复制失败')
    })
}

const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

// 格式化文件大小
const formatSize = (bytes: number | undefined) => {
  if (bytes === undefined || bytes === null || bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(2) + ' ' + units[i]
}

// 计算容量使用百分比
const getUsagePercent = (used: number | undefined, max: number | undefined) => {
  if (!max || max === 0) return 0
  return Math.min(100, Math.round(((used || 0) / max) * 100))
}

// 容量进度条颜色
const getProgressColor = (percent: number) => {
  if (percent >= 90) return '#ff4d4f'
  if (percent >= 70) return '#faad14'
  return '#52c41a'
}

// ==================== 改变空间状态 ====================
const handleChangeStatus = (record: API.SpaceVO) => {
  const newStatus = record.status === 1 ? 0 : 1
  const actionText = newStatus === 0 ? '禁用' : '启用'
  Modal.confirm({
    title: `确认${actionText}空间`,
    icon: h(ExclamationCircleOutlined),
    content: `确定${actionText}空间「${record.spaceName || record.id}」吗？${newStatus === 0 ? '禁用后空间内的内容将无法被访问。' : ''}`,
    okText: '确认',
    okButtonProps: { danger: newStatus === 0 },
    cancelText: '取消',
    async onOk() {
      try {
        const res = await spaceApi.changeSpaceStatus({
          spaceId: record.id!,
          status: newStatus,
        })
        const result = res.data as any
        if (result?.code === 0) {
          message.success(`${actionText}成功`)
          fetchData()
        }
      } catch (e) {
        console.error(`${actionText}失败:`, e)
      }
    },
  })
}

// ==================== 查看空间图片 ====================
const handleViewPictures = async (record: API.SpaceVO) => {
  currentViewSpaceId.value = record.id
  pictureModalTitle.value = `空间「${record.spaceName || record.id}」的图片`
  pictureModalVisible.value = true
  picturePagination.current = 1
  await fetchPictures()
}

const fetchPictures = async () => {
  if (!currentViewSpaceId.value) return
  pictureLoading.value = true
  try {
    const params: API.PictureQueryListDTO = {
      current: picturePagination.current,
      pageSize: picturePagination.pageSize,
      spaceId: currentViewSpaceId.value,
      sortField: 'createTime',
      sortOrder: 'desc',
    }
    const res = await pictureApi.list(params)
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      pictureList.value = result.data.records || []
      picturePagination.total = result.data.total || 0
    } else {
      pictureList.value = []
      picturePagination.total = 0
    }
  } catch (e) {
    console.error('获取空间图片失败:', e)
    pictureList.value = []
  } finally {
    pictureLoading.value = false
  }
}

const handlePicturePaginationChange = (page: number, pageSize: number) => {
  picturePagination.current = page
  picturePagination.pageSize = pageSize
  fetchPictures()
}

// 图片下载（携带空间ID）
const handleDownloadPicture = async (pic: API.PictureListVO) => {
  try {
    const res = await pictureApi.pictureDownload({
      pictureId: pic.id,
      spaceId: currentViewSpaceId.value,
    })
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      window.open(result.data, '_blank')
      message.success('开始下载')
    }
  } catch (e) {
    console.error('下载失败:', e)
  }
}

// 图片删除
const handleDeletePicture = (pic: API.PictureListVO) => {
  Modal.confirm({
    title: '确认删除图片',
    icon: h(ExclamationCircleOutlined),
    content: `确定删除图片「${pic.id}」吗？删除后不可恢复。`,
    okText: '确认删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        const res = await pictureApi.delete2({
          id: pic.id!,
          spaceId: currentViewSpaceId.value,
        })
        const result = res.data as any
        if (result?.code === 0) {
          message.success('删除成功')
          await fetchPictures()
          // 同步刷新空间列表数据（更新图片数量和容量）
          fetchData()
        }
      } catch (e) {
        console.error('删除失败:', e)
      }
    },
  })
}

// 审核状态映射
const reviewStatusMap: Record<number, { text: string; color: string }> = {
  0: { text: '待审核', color: '#faad14' },
  1: { text: '已通过', color: '#52c41a' },
  2: { text: '已拒绝', color: '#ff4d4f' },
}

// 解析标签
const parseTags = (tags: string | undefined): string[] => {
  if (!tags) return []
  try {
    const parsed = JSON.parse(tags)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return tags
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean)
  }
}

// ==================== 查看空间成员 ====================
const handleViewMembers = async (record: API.SpaceVO) => {
  currentMemberSpaceId.value = record.id
  memberModalTitle.value = `空间「${record.spaceName || record.id}」的成员`
  memberModalVisible.value = true
  memberPagination.current = 1
  await fetchMembers()
}

const fetchMembers = async () => {
  if (!currentMemberSpaceId.value) return
  memberLoading.value = true
  try {
    const params: API.SpaceUserQueryDTO = {
      current: memberPagination.current,
      pageSize: memberPagination.pageSize,
      spaceId: currentMemberSpaceId.value,
    }
    const res = await spaceApi.listSpaceUsers(params)
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      memberList.value = result.data.records || []
      memberPagination.total = result.data.total || 0
    } else {
      memberList.value = []
      memberPagination.total = 0
    }
  } catch (e) {
    console.error('获取空间成员失败:', e)
    memberList.value = []
  } finally {
    memberLoading.value = false
  }
}

const handleMemberPaginationChange = (page: number, pageSize: number) => {
  memberPagination.current = page
  memberPagination.pageSize = pageSize
  fetchMembers()
}

// ==================== 空间升级 ====================
const handleUpgrade = (record: API.SpaceVO) => {
  const currentLevel = record.spaceLevel ?? 0
  if (currentLevel >= 3) {
    message.info('该空间已是最高等级')
    return
  }
  const targetLevel = currentLevel + 1
  Modal.confirm({
    title: '空间升级',
    icon: h(ExclamationCircleOutlined),
    content: `确定将空间「${record.spaceName || record.id}」从「${spaceLevelMap[currentLevel]?.text}」升级为「${spaceLevelMap[targetLevel]?.text}」吗？`,
    okText: '确认升级',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await spaceApi.upgradeSpace({
          spaceId: record.id!,
          level: targetLevel,
        })
        const result = res.data as any
        if (result?.code === 0) {
          message.success('升级成功')
          fetchData()
        }
      } catch (e) {
        console.error('升级失败:', e)
      }
    },
  })
}

// ==================== 删除空间 ====================
const handleDeleteSpace = (record: API.SpaceVO) => {
  Modal.confirm({
    title: '确认删除空间',
    icon: h(ExclamationCircleOutlined),
    content: h('div', [
      h(
        'p',
        { style: { marginBottom: '8px' } },
        `确定删除空间「${record.spaceName || record.id}」吗？`,
      ),
      h(
        'p',
        { style: { color: '#ff4d4f', fontWeight: '600', marginBottom: '4px' } },
        '此操作不可撤销！',
      ),
      h('ul', { style: { paddingLeft: '20px', color: '#666', fontSize: '13px' } }, [
        h('li', '将永久删除该空间的所有图片'),
        h('li', '将移除所有成员的访问权限'),
        h('li', '将清除所有空间数据'),
      ]),
    ]),
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await spaceApi.deleteSpace({ spaceId: record.id! })
        const result = res.data as any
        if (result?.code === 0) {
          message.success('空间已删除')
          fetchData()
        } else {
          message.error(result?.message || '删除失败')
        }
      } catch (e) {
        console.error('删除空间失败:', e)
        message.error('删除失败，请重试')
      }
    },
  })
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <div class="page-header-left">
        <h3>空间管理</h3>
        <p>管理系统中的所有团队空间，查看图片与容量使用</p>
      </div>
      <div class="page-header-right"></div>
    </div>

    <a-card>
      <!-- 搜索区域 -->
      <div class="search-bar">
        <a-row :gutter="[16, 12]">
          <a-col :xs="24" :sm="12" :md="6">
            <a-input
              v-model:value="searchForm.spaceName"
              placeholder="搜索空间名称"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <a-select
              v-model:value="searchForm.spaceLevel"
              placeholder="等级筛选"
              style="width: 100%"
              allow-clear
            >
              <a-select-option :value="0">免费版</a-select-option>
              <a-select-option :value="1">普通版</a-select-option>
              <a-select-option :value="2">专业版</a-select-option>
              <a-select-option :value="3">旗舰版</a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <a-input
              v-model:value="searchForm.userId"
              placeholder="创建者ID"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
        </a-row>
        <div class="search-actions">
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </div>
      </div>

      <!-- 表格 -->
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1300 }"
        row-key="id"
        size="middle"
        @change="handleTableChange"
        @resizeColumn="handleResizeColumn"
      >
        <template #bodyCell="{ column, record }">
          <!-- ID 列 -->
          <template v-if="column.key === 'id'">
            <a-tooltip :title="record.id" placement="topLeft">
              <span class="id-cell" @click="copyId(record.id)">
                {{ record.id }}
              </span>
            </a-tooltip>
          </template>

          <!-- 空间名称列 -->
          <template v-else-if="column.key === 'spaceName'">
            <span class="space-name">{{ record.spaceName || '-' }}</span>
          </template>

          <!-- 创建者ID列 -->
          <template v-else-if="column.key === 'userId'">
            <a-tooltip :title="'创建者ID: ' + record.userId + '（点击复制）'">
              <span class="id-cell" @click="copyId(record.userId)">
                {{ record.userId }}
              </span>
            </a-tooltip>
          </template>

          <!-- 等级列 -->
          <template v-else-if="column.key === 'spaceLevel'">
            <a-tag :color="spaceLevelMap[record.spaceLevel]?.color || '#999'">
              {{ spaceLevelMap[record.spaceLevel]?.text || '未知' }}
            </a-tag>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="spaceStatusMap[record.status]?.text || '未知'"
            />
          </template>
          <!-- 成员数列 -->
          <template v-else-if="column.key === 'memberCount'">
            <a-tooltip title="点击查看成员列表">
              <span class="member-count-link" @click="handleViewMembers(record)">
                <TeamOutlined style="margin-right: 4px" />{{ record.memberCount ?? 0 }}
              </span>
            </a-tooltip>
          </template>

          <!-- 图片数量（使用量/最大量）-->
          <template v-else-if="column.key === 'pictureUsage'">
            <div class="usage-cell">
              <div class="usage-text">
                {{ record.totalCount ?? 0 }} / {{ record.maxCount ?? 0 }}
              </div>
              <a-progress
                :percent="getUsagePercent(record.totalCount, record.maxCount)"
                :stroke-color="
                  getProgressColor(getUsagePercent(record.totalCount, record.maxCount))
                "
                :show-info="false"
                size="small"
              />
            </div>
          </template>

          <!-- 容量使用（已用/最大）-->
          <template v-else-if="column.key === 'sizeUsage'">
            <div class="usage-cell">
              <div class="usage-text">
                {{ formatSize(record.totalSize) }} / {{ formatSize(record.maxSize) }}
              </div>
              <a-progress
                :percent="getUsagePercent(record.totalSize, record.maxSize)"
                :stroke-color="getProgressColor(getUsagePercent(record.totalSize, record.maxSize))"
                :show-info="false"
                size="small"
              />
            </div>
          </template>
          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewPictures(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button
                type="link"
                size="small"
                :class="{ 'danger-link': record.status === 1 }"
                @click="handleChangeStatus(record)"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>
              <a-button
                v-if="isBoss && (record.spaceLevel ?? 0) < 3"
                type="link"
                size="small"
                class="upgrade-link"
                @click="handleUpgrade(record)"
              >
                升级
              </a-button>
              <a-button
                v-if="isBoss"
                type="link"
                size="small"
                danger
                @click="handleDeleteSpace(record)"
              >
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-space>
          </template>
        </template>

        <!-- 空状态 -->
        <template #emptyText>
          <a-empty description="暂无空间数据" />
        </template>
      </a-table>
    </a-card>
    <!-- 空间图片查看弹窗 -->
    <a-modal
      v-model:open="pictureModalVisible"
      :title="pictureModalTitle"
      :footer="null"
      :width="1100"
      :bodyStyle="{ padding: '16px 24px', maxHeight: '80vh', overflowY: 'auto' }"
      centered
    >
      <a-spin :spinning="pictureLoading">
        <!-- 图片网格 -->
        <a-image-preview-group v-if="pictureList.length > 0">
          <div class="picture-grid">
            <div v-for="pic in pictureList" :key="pic.id" class="picture-card">
              <div class="picture-thumb">
                <a-image
                  v-if="pic.thumbUrl"
                  :src="pic.thumbUrl"
                  width="100%"
                  height="100%"
                  style="object-fit: cover"
                />
                <div v-else class="thumb-placeholder">暂无图片</div>
              </div>
              <div class="picture-info">
                <div class="picture-meta">
                  <a-tooltip v-if="pic.id" :title="'ID: ' + pic.id">
                    <span class="pic-id" @click="copyId(pic.id)">{{ pic.id }}</span>
                  </a-tooltip>
                  <a-tag
                    v-if="pic.reviewStatus !== undefined"
                    :color="reviewStatusMap[pic.reviewStatus]?.color || '#999'"
                    size="small"
                  >
                    {{ reviewStatusMap[pic.reviewStatus]?.text || '未知' }}
                  </a-tag>
                </div>
                <div v-if="parseTags(pic.tags).length > 0" class="picture-tags">
                  <a-tag
                    v-for="tag in parseTags(pic.tags).slice(0, 2)"
                    :key="tag"
                    color="blue"
                    size="small"
                  >
                    {{ tag }}
                  </a-tag>
                  <a-tag v-if="parseTags(pic.tags).length > 2" color="default" size="small">
                    +{{ parseTags(pic.tags).length - 2 }}
                  </a-tag>
                </div>
                <div class="picture-actions">
                  <a-button
                    type="link"
                    size="small"
                    class="download-link"
                    @click="handleDownloadPicture(pic)"
                  >
                    <template #icon><DownloadOutlined /></template>
                    下载
                  </a-button>
                  <a-button type="link" size="small" danger @click="handleDeletePicture(pic)">
                    <template #icon><DeleteOutlined /></template>
                    删除
                  </a-button>
                </div>
              </div>
            </div>
          </div>
        </a-image-preview-group>
        <a-empty v-else description="该空间暂无图片" />

        <!-- 分页 -->
        <div v-if="picturePagination.total > 0" class="picture-pagination">
          <a-pagination
            :current="picturePagination.current"
            :page-size="picturePagination.pageSize"
            :total="picturePagination.total"
            :show-size-changer="picturePagination.showSizeChanger"
            :show-total="picturePagination.showTotal"
            :page-size-options="picturePagination.pageSizeOptions"
            size="small"
            @change="handlePicturePaginationChange"
          />
        </div>
      </a-spin>
    </a-modal>

    <!-- 空间成员查看弹窗 -->
    <a-modal
      v-model:open="memberModalVisible"
      :title="memberModalTitle"
      :footer="null"
      :width="720"
      :destroyOnClose="true"
      centered
    >
      <a-spin :spinning="memberLoading">
        <a-table
          v-if="memberList.length > 0"
          :data-source="memberList"
          :pagination="false"
          row-key="id"
          size="small"
        >
          <a-table-column title="用户" key="user" width="200">
            <template #default="{ record }">
              <div class="member-user-cell">
                <a-avatar :src="record.userAvatar" :size="32" style="flex-shrink: 0">
                  {{ record.userName?.[0] || '?' }}
                </a-avatar>
                <div class="member-user-info">
                  <span class="member-user-name">{{ record.userName || '-' }}</span>
                  <a-tooltip :title="'用户ID: ' + record.userId + '（点击复制）'">
                    <span class="member-user-id" @click="copyId(record.userId)">
                      {{ record.userId }}
                    </span>
                  </a-tooltip>
                </div>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="角色" key="spaceRole" width="100" align="center">
            <template #default="{ record }">
              <a-tag :color="spaceRoleMap[record.spaceRole]?.color || '#999'">
                {{ spaceRoleMap[record.spaceRole]?.text || '未知' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="加入时间" key="createTime" width="170">
            <template #default="{ record }">
              {{ formatTime(record.createTime) }}
            </template>
          </a-table-column>
        </a-table>
        <a-empty v-else description="该空间暂无成员" />

        <!-- 分页 -->
        <div v-if="memberPagination.total > 0" class="member-pagination">
          <a-pagination
            :current="memberPagination.current"
            :page-size="memberPagination.pageSize"
            :total="memberPagination.total"
            :show-size-changer="memberPagination.showSizeChanger"
            :show-total="memberPagination.showTotal"
            :page-size-options="memberPagination.pageSizeOptions"
            size="small"
            @change="handleMemberPaginationChange"
          />
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<style scoped>
.admin-page {
  min-height: 100%;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.page-header-left h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 4px;
  color: #1a1a1a;
}

.page-header-left p {
  color: #999;
  font-size: 14px;
  margin: 0;
}

.search-bar {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fafafa;
  border-radius: 8px;
}

.search-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

/* ID 列样式 */
.id-cell {
  cursor: pointer;
  color: #666;
  font-size: 12px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 100%;
  transition: color 0.2s;
}

.id-cell:hover {
  color: #1890ff;
  text-decoration: underline;
}

/* 空间名称 */
.space-name {
  font-weight: 500;
  color: #1a1a1a;
}

/* 容量使用样式 */
.usage-cell {
  min-width: 100px;
}

.usage-text {
  font-size: 12px;
  color: #666;
  margin-bottom: 2px;
  white-space: nowrap;
}

/* 操作按钮样式 */
.danger-link {
  color: #ff7875 !important;
}

.danger-link:hover {
  color: #ff4d4f !important;
}

/* 升级按钮 */
.upgrade-link {
  color: #722ed1 !important;
}

.upgrade-link:hover {
  color: #531dab !important;
}

/* 下载按钮 */
.download-link {
  color: #52c41a !important;
}

.download-link:hover {
  color: #389e0d !important;
}

/* ==================== 图片网格样式 ==================== */
.picture-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 960px) {
  .picture-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 680px) {
  .picture-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.picture-card {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  transition:
    box-shadow 0.2s,
    border-color 0.2s;
}

.picture-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #d9d9d9;
}

.picture-thumb {
  position: relative;
  width: 100%;
  height: 160px;
  overflow: hidden;
  background: #fafafa;
}

.picture-thumb :deep(.ant-image) {
  width: 100%;
  height: 100%;
}

.picture-thumb :deep(.ant-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.picture-thumb:hover :deep(.ant-image img) {
  transform: scale(1.05);
}

.thumb-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 13px;
}

.picture-info {
  padding: 8px 10px;
}

.picture-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.pic-id {
  cursor: pointer;
  color: #999;
  font-size: 11px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100px;
  display: inline-block;
  transition: color 0.2s;
}

.pic-id:hover {
  color: #1890ff;
  text-decoration: underline;
}

.picture-tags {
  margin-bottom: 4px;
}

.picture-tags :deep(.ant-tag) {
  margin-right: 4px;
  margin-bottom: 2px;
  font-size: 11px;
}

.picture-actions {
  display: flex;
  gap: 4px;
  margin-top: 2px;
}

.picture-actions :deep(.ant-btn) {
  padding: 0 4px;
  font-size: 12px;
  height: 22px;
}

.picture-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}

/* 列拖拽手柄样式 */
:deep(.ant-table-resize-handle) {
  position: absolute;
  top: 0;
  right: -5px;
  bottom: 0;
  width: 10px;
  cursor: col-resize;
  z-index: 1;
}

:deep(.ant-table-resize-handle:hover) {
  background: rgba(24, 144, 255, 0.15);
}

/* 标签对齐 */
:deep(.ant-tag) {
  margin-right: 4px;
}

/* ==================== 成员数点击样式 ==================== */
.member-count-link {
  cursor: pointer;
  color: #1890ff;
  font-weight: 500;
  transition: color 0.2s;
}

.member-count-link:hover {
  color: #096dd9;
  text-decoration: underline;
}

/* ==================== 成员弹窗样式 ==================== */
.member-user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.member-user-name {
  font-weight: 500;
  color: #1a1a1a;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-user-id {
  cursor: pointer;
  color: #999;
  font-size: 11px;
  font-family: 'SFMono-Regular', Consolas, monospace;
  transition: color 0.2s;
}

.member-user-id:hover {
  color: #1890ff;
  text-decoration: underline;
}

.member-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
