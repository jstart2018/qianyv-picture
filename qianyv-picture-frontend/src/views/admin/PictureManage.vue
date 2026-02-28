<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  ExclamationCircleOutlined,
  DeleteOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import * as pictureApi from '@/api/pictureController'
import * as categoryApi from '@/api/picCategoryController'

const router = useRouter()

// ==================== 状态 ====================
const loading = ref(false)
const dataSource = ref<API.PictureListVO[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50'],
})

// 搜索条件（对应 PictureQueryListDTO）
const searchForm = reactive({
  searchText: '' as string,
  categoryId: undefined as number | undefined,
  spaceId: '' as string,
  pictureType: undefined as number | undefined,
  reviewStatus: undefined as number | undefined,
})

// 分类列表
const categoryList = ref<API.PicCategoryVO[]>([])

// ==================== 审核状态映射 ====================
const reviewStatusMap: Record<number, { text: string; color: string }> = {
  0: { text: '待审核', color: '#faad14' },
  1: { text: '已通过', color: '#52c41a' },
  2: { text: '已拒绝', color: '#ff4d4f' },
}

// 图片类型映射
const pictureTypeMap: Record<number, string> = {
  0: '横屏',
  1: '竖屏',
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
    title: '缩略图',
    dataIndex: 'thumbUrl',
    key: 'thumbUrl',
    width: 100,
    minWidth: 80,
  },
  {
    title: '博客ID',
    dataIndex: 'blogId',
    key: 'blogId',
    width: 160,
    minWidth: 80,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '标签',
    dataIndex: 'tags',
    key: 'tags',
    width: 180,
    minWidth: 100,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '比例',
    dataIndex: 'picScale',
    key: 'picScale',
    width: 80,
    minWidth: 60,
    resizable: true,
  },
  {
    title: '收藏数',
    dataIndex: 'collectCount',
    key: 'collectCount',
    width: 80,
    minWidth: 60,
    resizable: true,
  },
  {
    title: '审核状态',
    dataIndex: 'reviewStatus',
    key: 'reviewStatus',
    width: 100,
    minWidth: 80,
    resizable: true,
  },
  {
    title: '审核时间',
    dataIndex: 'reviewTime',
    key: 'reviewTime',
    width: 170,
    minWidth: 100,
    resizable: true,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right' as const,
  },
])

// 列宽拖拽回调
const handleResizeColumn = (w: number, col: any) => {
  col.width = w
}

// ==================== 数据加载 ====================
const fetchData = async () => {
  loading.value = true
  try {
    const params: API.PictureQueryListDTO = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    }
    if (searchForm.searchText) params.searchText = searchForm.searchText
    if (searchForm.categoryId !== undefined) params.categoryId = searchForm.categoryId
    if (searchForm.spaceId) params.spaceId = searchForm.spaceId as any
    if (searchForm.pictureType !== undefined) params.pictureType = searchForm.pictureType
    if (searchForm.reviewStatus !== undefined) params.reviewStatus = searchForm.reviewStatus

    const res = await pictureApi.list(params)
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      dataSource.value = result.data.records || []
      pagination.total = result.data.total || 0
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error('获取图片列表失败:', e)
    dataSource.value = []
  } finally {
    loading.value = false
  }
}

// 加载分类列表
const fetchCategories = async () => {
  try {
    const res = await categoryApi.listAll()
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      categoryList.value = result.data
    }
  } catch (e) {
    console.error('获取分类列表失败:', e)
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
  searchForm.searchText = ''
  searchForm.categoryId = undefined
  searchForm.spaceId = ''
  searchForm.pictureType = undefined
  searchForm.reviewStatus = undefined
  pagination.current = 1
  fetchData()
}

// ==================== 下载 ====================
const handleDownload = async (record: API.PictureListVO) => {
  try {
    const res = await pictureApi.pictureDownload({ pictureId: record.id })
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      window.open(result.data, '_blank')
      message.success('开始下载')
    }
  } catch (e) {
    console.error('下载失败:', e)
  }
}

// ==================== 删除 ====================
const handleDelete = (record: API.PictureListVO) => {
  Modal.confirm({
    title: '确认删除',
    icon: h(ExclamationCircleOutlined),
    content: '确定删除该图片吗？此操作不可恢复！',
    okText: '确认删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        const deleteParams: API.delete2Params = { id: record.id! }
        if ((record as any).spaceId) {
          deleteParams.spaceId = (record as any).spaceId
        }
        const res = await pictureApi.delete2(deleteParams)
        const result = res.data as any
        if (result?.code === 0) {
          message.success('删除成功')
          dataSource.value = dataSource.value.filter((item) => item.id !== record.id)
          pagination.total = Math.max(0, pagination.total - 1)
          if (dataSource.value.length === 0 && pagination.current > 1) {
            pagination.current--
          }
          fetchData()
        }
      } catch (e) {
        console.error('删除失败:', e)
      }
    },
  })
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

const formatScale = (scale: number | undefined) => {
  if (scale === undefined || scale === null) return '-'
  return scale.toFixed(2)
}

// 格式化时间
const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

// 打开博客详情
const handleOpenBlog = (blogId: number) => {
  const route = router.resolve({ name: 'blogDetail', params: { id: blogId } })
  window.open(route.href, '_blank')
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchData()
  fetchCategories()
})
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <div class="page-header-left">
        <h3>图片管理</h3>
        <p>管理系统中的所有图片资源，查看与下载</p>
      </div>
      <div class="page-header-right"></div>
    </div>

    <a-card>
      <!-- 搜索区域 -->
      <div class="search-bar">
        <a-row :gutter="[16, 12]">
          <a-col :xs="24" :sm="12" :md="6">
            <a-input
              v-model:value="searchForm.searchText"
              placeholder="搜索图片标签"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <a-select
              v-model:value="searchForm.categoryId"
              placeholder="分类筛选"
              style="width: 100%"
              allow-clear
            >
              <a-select-option v-for="cat in categoryList" :key="cat.id" :value="cat.id">
                {{ cat.categoryName }}
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <a-select
              v-model:value="searchForm.pictureType"
              placeholder="横屏/竖屏"
              style="width: 100%"
              allow-clear
            >
              <a-select-option :value="0">横屏</a-select-option>
              <a-select-option :value="1">竖屏</a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <a-select
              v-model:value="searchForm.reviewStatus"
              placeholder="审核状态"
              style="width: 100%"
              allow-clear
            >
              <a-select-option :value="0">待审核</a-select-option>
              <a-select-option :value="1">已通过</a-select-option>
              <a-select-option :value="2">已拒绝</a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <a-input
              v-model:value="searchForm.spaceId"
              placeholder="空间ID"
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
          <!-- 缩略图列 -->
          <template v-else-if="column.key === 'thumbUrl'">
            <div class="thumb-cell">
              <a-image
                v-if="record.thumbUrl"
                :src="record.thumbUrl"
                :width="58"
                :height="58"
                style="object-fit: cover; border-radius: 4px"
              />
              <div v-else class="thumb-empty">暂无</div>
            </div>
          </template>

          <!-- 博客ID列 -->
          <template v-else-if="column.key === 'blogId'">
            <template v-if="record.blogId">
              <a-tooltip :title="'博客ID: ' + record.blogId + '（点击查看详情）'">
                <span class="blog-id-cell" @click="handleOpenBlog(record.blogId)">
                  {{ record.blogId }}
                </span>
              </a-tooltip>
            </template>
            <span v-else style="color: #ccc">-</span>
          </template>

          <!-- 标签列 -->
          <template v-else-if="column.key === 'tags'">
            <template v-if="parseTags(record.tags).length > 0">
              <a-tag
                v-for="tag in parseTags(record.tags).slice(0, 3)"
                :key="tag"
                color="blue"
                style="margin-bottom: 2px"
              >
                {{ tag }}
              </a-tag>
              <a-tooltip
                v-if="parseTags(record.tags).length > 3"
                :title="parseTags(record.tags).join('、')"
              >
                <a-tag color="default">+{{ parseTags(record.tags).length - 3 }}</a-tag>
              </a-tooltip>
            </template>
            <span v-else style="color: #ccc">-</span>
          </template>

          <!-- 比例列 -->
          <template v-else-if="column.key === 'picScale'">
            {{ formatScale(record.picScale) }}
          </template>

          <!-- 审核状态列 -->
          <template v-else-if="column.key === 'reviewStatus'">
            <a-tooltip v-if="record.reviewMessage" :title="'审核意见: ' + record.reviewMessage">
              <a-tag :color="reviewStatusMap[record.reviewStatus]?.color || '#999'">
                {{ reviewStatusMap[record.reviewStatus]?.text || '未知' }}
              </a-tag>
            </a-tooltip>
            <a-tag v-else :color="reviewStatusMap[record.reviewStatus]?.color || '#999'">
              {{ reviewStatusMap[record.reviewStatus]?.text || '未知' }}
            </a-tag>
          </template>

          <!-- 审核时间列 -->
          <template v-else-if="column.key === 'reviewTime'">
            {{ formatTime(record.reviewTime) }}
          </template>
          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button
                type="link"
                size="small"
                class="download-link"
                @click="handleDownload(record)"
              >
                <template #icon><DownloadOutlined /></template>
                下载
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                <template #icon><DeleteOutlined /></template>
                删除
              </a-button>
            </a-space>
          </template>
        </template>

        <!-- 空状态 -->
        <template #emptyText>
          <a-empty description="暂无图片数据" />
        </template>
      </a-table>
    </a-card>
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

/* 缩略图样式 */
.thumb-cell {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  transition: border-color 0.2s;
}

.thumb-cell:hover {
  border-color: #1890ff;
}

.thumb-empty {
  font-size: 11px;
  color: #ccc;
}

/* 下载按钮样式 */
.download-link {
  color: #52c41a !important;
}

.download-link:hover {
  color: #389e0d !important;
}

/* 标签对齐 */
:deep(.ant-tag) {
  margin-right: 4px;
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

/* 博客ID列样式 */
.blog-id-cell {
  cursor: pointer;
  color: #1890ff;
  font-size: 12px;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 100%;
  transition: color 0.2s;
}

.blog-id-cell:hover {
  color: #096dd9;
  text-decoration: underline;
}
</style>
```
