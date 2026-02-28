<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  ExclamationCircleOutlined,
  EyeOutlined,
  DeleteOutlined,
  AuditOutlined,
  StarOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons-vue'
import * as blogApi from '@/api/blogController'

const router = useRouter()

// ==================== 状态 ====================
const loading = ref(false)
const dataSource = ref<API.BlogAdminQueryVO[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50'],
})

// 搜索条件（对应 BlogListDTO）
const searchForm = reactive({
  searchText: '' as string,
  userId: '' as string,
  reviewStatus: undefined as number | undefined,
  isRecommend: undefined as number | undefined,
  sort: undefined as number | undefined,
  createTime: '' as string,
  updateTime: '' as string,
})

// 博客详情弹窗
const detailModalVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref<API.BlogsVO | null>(null)

// 审核弹窗
const auditModalVisible = ref(false)
const auditLoading = ref(false)
const auditForm = reactive({
  id: undefined as number | undefined,
  blogTitle: '' as string,
  reviewStatus: undefined as number | undefined,
  reviewMessage: '' as string,
})

// ==================== 审核状态映射 ====================
const reviewStatusMap: Record<number, { text: string; color: string }> = {
  0: { text: '待审核', color: '#faad14' },
  1: { text: '已通过', color: '#52c41a' },
  2: { text: '已拒绝', color: '#ff4d4f' },
}

// 推荐状态映射
const recommendMap: Record<number, { text: string; color: string }> = {
  0: { text: '普通', color: '#d9d9d9' },
  1: { text: '推荐', color: '#1890ff' },
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
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    width: 200,
    minWidth: 120,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '作者ID',
    dataIndex: 'userId',
    key: 'userId',
    width: 160,
    minWidth: 80,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '浏览',
    dataIndex: 'viewCount',
    key: 'viewCount',
    width: 80,
    minWidth: 60,
    resizable: true,
  },
  {
    title: '点赞',
    dataIndex: 'likeCount',
    key: 'likeCount',
    width: 80,
    minWidth: 60,
    resizable: true,
  },
  {
    title: '评论',
    dataIndex: 'commentCount',
    key: 'commentCount',
    width: 80,
    minWidth: 60,
    resizable: true,
  },
  {
    title: '收藏',
    dataIndex: 'collectCount',
    key: 'collectCount',
    width: 80,
    minWidth: 60,
    resizable: true,
  },
  {
    title: '推荐',
    dataIndex: 'isRecommend',
    key: 'isRecommend',
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
    title: '审核人',
    dataIndex: 'reviewerName',
    key: 'reviewerName',
    width: 100,
    minWidth: 80,
    resizable: true,
    ellipsis: true,
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
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 170,
    minWidth: 100,
    resizable: true,
  },
  {
    title: '操作',
    key: 'action',
    width: 260,
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
    const params: API.BlogListDTO = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    }
    if (searchForm.searchText) params.searchText = searchForm.searchText
    if (searchForm.userId) params.userId = searchForm.userId as any
    if (searchForm.reviewStatus !== undefined) params.reviewStatus = searchForm.reviewStatus
    if (searchForm.isRecommend !== undefined) params.isRecommend = searchForm.isRecommend
    if (searchForm.sort !== undefined) params.sort = searchForm.sort
    if (searchForm.createTime) params.updateTime = searchForm.createTime
    if (searchForm.updateTime) params.updateTime = searchForm.updateTime

    const res = await blogApi.blogListByPageForAdmin(params)
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      dataSource.value = result.data.records || []
      pagination.total = result.data.total || 0
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error('获取博客列表失败:', e)
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
  searchForm.searchText = ''
  searchForm.userId = ''
  searchForm.reviewStatus = undefined
  searchForm.isRecommend = undefined
  searchForm.sort = undefined
  searchForm.createTime = ''
  searchForm.updateTime = ''
  pagination.current = 1
  fetchData()
}

// ==================== 标题点击跳转详情页 ====================
const handleOpenInNewTab = (record: API.BlogAdminQueryVO) => {
  const route = router.resolve({ name: 'blogDetail', params: { id: record.id } })
  window.open(route.href, '_blank')
}

// ==================== 打开博客详情（弹窗） ====================
const handleOpen = async (record: API.BlogAdminQueryVO) => {
  detailData.value = null
  detailModalVisible.value = true
  detailLoading.value = true
  try {
    const res = await blogApi.getBlogDetail({ id: record.id! })
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      detailData.value = result.data
    } else {
      message.error('获取博客详情失败')
    }
  } catch (e) {
    console.error('获取博客详情失败:', e)
    message.error('获取博客详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ==================== 审核操作 ====================
const handleAudit = (record: API.BlogAdminQueryVO) => {
  auditForm.id = record.id
  auditForm.blogTitle = record.title || String(record.id)
  auditForm.reviewStatus = undefined
  auditForm.reviewMessage = ''
  auditModalVisible.value = true
}

const handleAuditSubmit = async () => {
  if (auditForm.reviewStatus === undefined) {
    message.warning('请选择审核结果')
    return
  }

  auditLoading.value = true
  try {
    const res = await blogApi.auditBlog({
      id: auditForm.id,
      reviewStatus: auditForm.reviewStatus,
      reviewMessage: auditForm.reviewMessage.trim() || undefined,
    })
    const result = res.data as any
    if (result?.code === 0) {
      message.success('审核操作成功')
      auditModalVisible.value = false
      fetchData()
    }
  } catch (e) {
    console.error('审核操作失败:', e)
  } finally {
    auditLoading.value = false
  }
}

// ==================== 删除操作 ====================
const handleDelete = (record: API.BlogAdminQueryVO) => {
  Modal.confirm({
    title: '确认删除',
    icon: h(ExclamationCircleOutlined),
    content: `确定删除博客「${record.title || record.id}」吗？此操作不可恢复！`,
    okText: '确认删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        const res = await blogApi.delBlog({ id: record.id! })
        const result = res.data as any
        if (result?.code === 0) {
          message.success('删除成功')
          // 立即从本地数据中移除，确保界面即时更新
          dataSource.value = dataSource.value.filter((item) => item.id !== record.id)
          pagination.total = Math.max(0, pagination.total - 1)
          // 当前页已空且不是第一页，回退到上一页
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

// ==================== 精选操作 ====================
const handleFeatured = async (record: API.BlogAdminQueryVO) => {
  const isFeatured = record.isRecommend === 1
  const actionText = isFeatured ? '取消精选' : '设为精选'

  try {
    const res = await blogApi.featuredBlog({ id: record.id!, featured: !isFeatured })
    const result = res.data as any
    if (result?.code === 0) {
      message.success(`${actionText}成功`)
      fetchData()
    }
  } catch (e) {
    console.error(`${actionText}失败:`, e)
  }
}

// ==================== 格式化工具 ====================

// ==================== 一键快速审核 ====================
const quickAuditLoading = ref(false)
const handleQuickAudit = () => {
  // 筛选出当前页待审核的博客
  const pendingBlogs = dataSource.value.filter((b) => b.reviewStatus === 0)
  if (pendingBlogs.length === 0) {
    message.info('当前页没有待审核的博客')
    return
  }
  Modal.confirm({
    title: '一键快速审核',
    icon: h(ExclamationCircleOutlined),
    content: `将对当前页 ${pendingBlogs.length} 篇待审核博客全部通过审核，审核意见为「快速审核」，确认继续？`,
    okText: '确认审核',
    cancelText: '取消',
    async onOk() {
      quickAuditLoading.value = true
      let successCount = 0
      let failCount = 0
      try {
        for (const blog of pendingBlogs) {
          try {
            const res = await blogApi.auditBlog({
              id: blog.id,
              reviewStatus: 1,
              reviewMessage: '快速审核',
            })
            const result = res.data as any
            if (result?.code === 0) {
              successCount++
            } else {
              failCount++
            }
          } catch {
            failCount++
          }
        }
        if (failCount === 0) {
          message.success(`快速审核完成，共通过 ${successCount} 篇`)
        } else {
          message.warning(`审核完成：${successCount} 篇通过，${failCount} 篇失败`)
        }
        fetchData()
      } finally {
        quickAuditLoading.value = false
      }
    },
  })
}

// ==================== 格式化工具 ====================
const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

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

// ==================== 初始化 ====================
onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <div class="page-header-left">
        <h3>博客管理</h3>
        <p>管理系统中的所有博客内容，审核与删除</p>
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
              placeholder="搜索标题 / 内容"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="4">
            <a-input
              v-model:value="searchForm.userId"
              placeholder="作者ID"
              allow-clear
              @pressEnter="handleSearch"
            />
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
            <a-select
              v-model:value="searchForm.isRecommend"
              placeholder="推荐状态"
              style="width: 100%"
              allow-clear
            >
              <a-select-option :value="0">普通</a-select-option>
              <a-select-option :value="1">推荐</a-select-option>
            </a-select>
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-space>
              <a-date-picker
                v-model:value="searchForm.createTime"
                value-format="YYYY-MM-DD"
                placeholder="创建时间"
                allow-clear
              />
              <a-date-picker
                v-model:value="searchForm.updateTime"
                value-format="YYYY-MM-DD"
                placeholder="更新时间"
                allow-clear
              />
            </a-space>
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
            <a-button
              type="primary"
              :loading="quickAuditLoading"
              @click="handleQuickAudit"
              style="background: #722ed1; border-color: #722ed1"
            >
              <template #icon><ThunderboltOutlined /></template>
              一键审核
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
        :scroll="{ x: 1400 }"
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
          <!-- 标题列：可点击跳转详情页 -->
          <template v-else-if="column.key === 'title'">
            <a class="title-link" @click="handleOpenInNewTab(record)">
              {{ record.title || '-' }}
            </a>
          </template>

          <!-- 作者 ID 列 -->
          <template v-else-if="column.key === 'userId'">
            <a-tooltip :title="record.userId" placement="topLeft">
              <span class="id-cell" @click="copyId(record.userId)">
                {{ record.userId }}
              </span>
            </a-tooltip>
          </template>

          <!-- 推荐列 -->
          <template v-else-if="column.key === 'isRecommend'">
            <a-tag :color="recommendMap[record.isRecommend]?.color || '#d9d9d9'">
              {{ recommendMap[record.isRecommend]?.text || '普通' }}
            </a-tag>
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

          <!-- 审核人列 -->
          <template v-else-if="column.key === 'reviewerName'">
            <template v-if="record.reviewerName">
              <a-tooltip :title="'审核人ID: ' + record.reviewerId + '（点击复制）'">
                <span class="reviewer-cell" @click="copyId(record.reviewerId)">
                  {{ record.reviewerName }}
                </span>
              </a-tooltip>
            </template>
            <span v-else style="color: #ccc">-</span>
          </template>

          <!-- 审核时间列 -->
          <template v-else-if="column.key === 'reviewTime'">
            {{ formatTime(record.reviewTime) }}
          </template>

          <!-- 时间列 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleOpen(record)">
                <template #icon><EyeOutlined /></template> 查看
              </a-button>
              <a-button type="link" size="small" @click="handleAudit(record)">
                <template #icon><AuditOutlined /></template>
                审核
              </a-button>
              <a-button
                type="link"
                size="small"
                :class="record.isRecommend === 1 ? 'featured-active' : 'featured-link'"
                @click="handleFeatured(record)"
              >
                <template #icon><StarOutlined /></template>
                {{ record.isRecommend === 1 ? '取消精选' : '精选' }}
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
          <a-empty description="暂无博客数据" />
        </template>
      </a-table>
    </a-card>
    <!-- 审核弹窗 -->
    <a-modal
      v-model:open="auditModalVisible"
      title="博客审核"
      :confirm-loading="auditLoading"
      @ok="handleAuditSubmit"
      :width="520"
      :maskClosable="false"
    >
      <div class="audit-card">
        <div class="audit-blog-info">
          <span class="audit-label">博客标题：</span>
          <span class="audit-value">{{ auditForm.blogTitle }}</span>
        </div>
        <a-divider style="margin: 12px 0" />
        <a-form layout="vertical">
          <a-form-item label="审核结果" required>
            <a-radio-group v-model:value="auditForm.reviewStatus">
              <a-radio-button :value="1" class="audit-pass">
                <svg
                  viewBox="0 0 24 24"
                  width="14"
                  height="14"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  style="vertical-align: -2px; margin-right: 4px"
                >
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                通过
              </a-radio-button>
              <a-radio-button :value="2" class="audit-reject">
                <svg
                  viewBox="0 0 24 24"
                  width="14"
                  height="14"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  style="vertical-align: -2px; margin-right: 4px"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
                拒绝
              </a-radio-button>
            </a-radio-group>
          </a-form-item>
          <a-form-item label="审核意见">
            <a-textarea
              v-model:value="auditForm.reviewMessage"
              placeholder="请填写审核意见（选填）"
              :rows="4"
              :maxlength="500"
              show-count
              allow-clear
            />
          </a-form-item>
        </a-form>
      </div>
    </a-modal>

    <!-- 博客详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      :title="detailData?.title || '博客详情'"
      :footer="null"
      :width="720"
      :maskClosable="true"
      :destroyOnClose="true"
    >
      <a-spin :spinning="detailLoading">
        <template v-if="detailData">
          <div class="detail-section">
            <div class="detail-meta">
              <span>作者ID：{{ detailData.userId }}</span>
              <a-divider type="vertical" />
              <span>浏览 {{ detailData.viewCount ?? 0 }}</span>
              <a-divider type="vertical" />
              <span>点赞 {{ detailData.likeCount ?? 0 }}</span>
              <a-divider type="vertical" />
              <span>评论 {{ detailData.commentCount ?? 0 }}</span>
              <a-divider type="vertical" />
              <span>收藏 {{ detailData.collectCount ?? 0 }}</span>
              <a-divider type="vertical" />
              <a-tag :color="recommendMap[detailData.isRecommend ?? 0]?.color">
                {{ recommendMap[detailData.isRecommend ?? 0]?.text }}
              </a-tag>
            </div>
            <a-divider style="margin: 12px 0" />
            <div class="detail-content">{{ detailData.content || '暂无内容' }}</div>
          </div>

          <!-- 图片区域 -->
          <template v-if="detailData.pictureVOList && detailData.pictureVOList.length > 0">
            <a-divider style="margin: 16px 0 12px">
              <span style="color: #999; font-size: 13px"
                >图片（{{ detailData.pictureVOList.length }}）</span
              >
            </a-divider>
            <a-image-preview-group>
              <div class="detail-pictures">
                <div
                  v-for="pic in detailData.pictureVOList"
                  :key="pic.id"
                  class="detail-picture-item"
                >
                  <a-image
                    :src="pic.thumbUrl"
                    :width="120"
                    :height="120"
                    fit="cover"
                    style="border-radius: 6px; object-fit: cover"
                    :fallback="'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgZmlsbD0iI2Y1ZjVmNSIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBmaWxsPSIjY2NjIiBmb250LXNpemU9IjE0Ij7ml6Dlm74=PC90ZXh0Pjwvc3ZnPg=='"
                  />
                </div>
              </div>
            </a-image-preview-group>
          </template>
          <template v-else>
            <div class="detail-no-pictures">暂无图片</div>
          </template>
        </template>
        <template v-else-if="!detailLoading">
          <a-empty description="获取详情失败" />
        </template>
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

/* 审核人列样式 */
.reviewer-cell {
  cursor: pointer;
  color: #1a1a1a;
  font-size: 13px;
  transition: color 0.2s;
}

.reviewer-cell:hover {
  color: #1890ff;
  text-decoration: underline;
}

/* 标题链接样式 */
.title-link {
  color: #1a1a1a;
  cursor: pointer;
  transition: color 0.2s;
  font-weight: 500;
}

.title-link:hover {
  color: #1890ff;
}

/* 精选按钮样式 */
.featured-link {
  color: #faad14 !important;
}

.featured-link:hover {
  color: #d48806 !important;
}

.featured-active {
  color: #faad14 !important;
}

.featured-active:hover {
  color: #ad6800 !important;
}

/* 标签对齐 */
:deep(.ant-tag) {
  margin-right: 0;
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

/* ========== 审核弹窗样式 ========== */
.audit-card {
  padding: 8px 0;
}

.audit-blog-info {
  padding: 12px 16px;
  background: #f6f8fa;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.audit-label {
  color: #999;
  font-size: 13px;
}

.audit-value {
  color: #1a1a1a;
  font-weight: 500;
  font-size: 14px;
}

/* 审核通过按钮 */
.audit-pass:hover,
:deep(.ant-radio-button-wrapper-checked).audit-pass {
  color: #52c41a !important;
  border-color: #52c41a !important;
}

/* 审核拒绝按钮 */
.audit-reject:hover,
:deep(.ant-radio-button-wrapper-checked).audit-reject {
  color: #ff4d4f !important;
  border-color: #ff4d4f !important;
}

/* ========== 博客详情弹窗样式 ========== */
.detail-section {
  padding: 4px 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 0;
  color: #666;
  font-size: 13px;
}

.detail-content {
  font-size: 14px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
  padding: 12px 16px;
  background: #f9f9f9;
  border-radius: 8px;
}

.detail-pictures {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-picture-item {
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #f0f0f0;
  transition: border-color 0.2s;
}

.detail-picture-item:hover {
  border-color: #1890ff;
}

.detail-picture-item :deep(.ant-image) {
  display: block;
}

.detail-picture-item :deep(.ant-image img) {
  object-fit: cover;
}

.detail-no-pictures {
  text-align: center;
  color: #ccc;
  font-size: 13px;
  padding: 24px 0;
}
</style>
