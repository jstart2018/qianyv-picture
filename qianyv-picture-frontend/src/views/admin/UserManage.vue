<script setup lang="ts">
import { ref, reactive, onMounted, computed, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons-vue'
import * as userApi from '@/api/userController'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ==================== 状态 ====================
const loading = ref(false)
const dataSource = ref<API.User[]>([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50'],
})

// 搜索条件
const searchForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  role: undefined as number | undefined,
  status: undefined as number | undefined,
  createTime: '' as string,
  lastLoginTime: '' as string,
  isdelete: false as boolean | undefined,
})

// 创建用户弹窗
const createModalVisible = ref(false)
const createLoading = ref(false)
const createForm = reactive({
  account: '',
  password: '',
})

// ==================== 角色 & 状态映射 ====================
const roleMap: Record<number, { text: string; color: string }> = {
  0: { text: 'Boss', color: '#f50' },
  1: { text: '管理员', color: '#2db7f5' },
  2: { text: '普通用户', color: '#87d068' },
}

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '禁用', color: '#ff4d4f' },
  1: { text: '启用', color: '#52c41a' },
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
    title: '头像',
    dataIndex: 'avatar',
    key: 'avatar',
    width: 70,
    minWidth: 50,
  },
  {
    title: '昵称',
    dataIndex: 'nickname',
    key: 'nickname',
    width: 120,
    minWidth: 80,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '邮箱',
    dataIndex: 'email',
    key: 'email',
    width: 180,
    minWidth: 100,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '手机',
    dataIndex: 'phone',
    key: 'phone',
    width: 130,
    minWidth: 80,
    resizable: true,
    ellipsis: true,
  },
  {
    title: '角色',
    dataIndex: 'role',
    key: 'role',
    width: 90,
    minWidth: 70,
    resizable: true,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80,
    minWidth: 60,
    resizable: true,
  },
  {
    title: '注册时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 170,
    minWidth: 100,
    resizable: true,
  },
  {
    title: '最后登录',
    dataIndex: 'lastLoginTime',
    key: 'lastLoginTime',
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
    const params: API.UserQueryDTO = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    }
    if (searchForm.nickname) params.nickname = searchForm.nickname
    if (searchForm.email) params.email = searchForm.email
    if (searchForm.phone) params.phone = searchForm.phone
    if (searchForm.role !== undefined) params.role = searchForm.role
    if (searchForm.status !== undefined) params.status = searchForm.status
    if (searchForm.createTime) {
      params.createTime = searchForm.createTime
    }
    if (searchForm.lastLoginTime) {
      params.lastLoginTime = searchForm.lastLoginTime
    }
    if (searchForm.isdelete !== undefined) {
      params.isdelete = searchForm.isdelete
    }

    const res = await userApi.page(params)
    // 后端统一返回 { code, data, message }，axios 解析后 res.data 即为该对象
    const result = res.data as any
    if (result?.code === 0 && result?.data) {
      dataSource.value = result.data.records || []
      pagination.total = result.data.total || 0
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (e) {
    console.error('获取用户列表失败:', e)
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
  searchForm.nickname = ''
  searchForm.email = ''
  searchForm.phone = ''
  searchForm.role = undefined
  searchForm.status = undefined
  searchForm.createTime = ''
  searchForm.lastLoginTime = ''
  searchForm.isdelete = false
  pagination.current = 1
  fetchData()
}

// ==================== 角色操作（Boss 专属） ====================
const handleChangeRole = (record: API.User) => {
  if (record.id === userStore.user?.id) {
    message.warning('不能修改自己的角色')
    return
  }
  const newRoleText = record.role === 1 ? '普通用户' : '管理员'
  Modal.confirm({
    title: '确认修改角色',
    icon: h(ExclamationCircleOutlined),
    content: `确定将用户「${record.nickname || record.id}」的角色修改为「${newRoleText}」吗？`,
    okText: '确认',
    cancelText: '取消',
    async onOk() {
      try {
        const res = await userApi.changeRole({ userId: record.id! })
        const result = res.data as any
        if (result?.code === 0) {
          message.success('角色修改成功')
          fetchData()
        }
      } catch (e) {
        console.error('修改角色失败:', e)
      }
    },
  })
}

// ==================== 状态操作 ====================
const handleChangeStatus = (record: API.User) => {
  const newStatus = record.status === 1 ? 0 : 1
  const actionText = newStatus === 0 ? '禁用' : '启用'
  Modal.confirm({
    title: `确认${actionText}用户`,
    icon: h(ExclamationCircleOutlined),
    content: `确定${actionText}用户「${record.nickname || record.id}」吗？`,
    okText: '确认',
    okButtonProps: { danger: newStatus === 0 },
    cancelText: '取消',
    async onOk() {
      try {
        const res = await userApi.changeStatus({ userId: record.id!, status: newStatus })
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

// ==================== 删除操作 ====================
const handleDelete = (record: API.User) => {
  if (record.role === 0) {
    message.warning('不能删除 Boss 账号')
    return
  }
  Modal.confirm({
    title: '确认删除',
    icon: h(ExclamationCircleOutlined),
    content: `确定删除用户「${record.nickname || record.id}」吗？此操作不可恢复！`,
    okText: '确认删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    async onOk() {
      try {
        const res = await userApi.delete1({ id: record.id! })
        const result = (res as any).data
        if (result?.code === 0) {
          message.success('删除成功')
          if (dataSource.value.length === 1 && pagination.current > 1) {
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

// ==================== 创建用户（Boss 专属） ====================
const handleCreate = () => {
  createForm.account = ''
  createForm.password = ''
  createModalVisible.value = true
}

const handleCreateSubmit = async () => {
  if (!createForm.account.trim()) {
    message.warning('请输入账号（邮箱或手机号）')
    return
  }
  if (!createForm.password.trim() || createForm.password.length < 6) {
    message.warning('密码至少 6 位')
    return
  }

  createLoading.value = true
  try {
    const res = await userApi.createUser({
      account: createForm.account.trim(),
      password: createForm.password.trim(),
    })
    const result = res.data as any
    if (result?.code === 0) {
      message.success('用户创建成功')
      createModalVisible.value = false
      fetchData()
    }
  } catch (e) {
    console.error('创建用户失败:', e)
  } finally {
    createLoading.value = false
  }
}

// ==================== 格式化工具 ====================
const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

// 复制 ID 到剪贴板
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
        <h3>用户管理</h3>
        <p>管理系统中的所有用户账号</p>
      </div>
      <div class="page-header-right">
        <a-button v-if="userStore.isBoss" type="primary" @click="handleCreate">
          <template #icon><PlusOutlined /></template>
          创建用户
        </a-button>
      </div>
    </div>

    <a-card>
      <!-- 搜索区域 -->
      <div class="search-bar">
        <a-row :gutter="[16, 12]">
          <a-col :xs="24" :sm="12" :md="6">
            <a-input
              v-model:value="searchForm.nickname"
              placeholder="搜索昵称"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-input
              v-model:value="searchForm.email"
              placeholder="搜索邮箱"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-input
              v-model:value="searchForm.phone"
              placeholder="搜索手机号"
              allow-clear
              @pressEnter="handleSearch"
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-space>
              <a-select
                v-model:value="searchForm.role"
                placeholder="角色"
                style="width: 110px"
                allow-clear
              >
                <a-select-option :value="0">Boss</a-select-option>
                <a-select-option :value="1">管理员</a-select-option>
                <a-select-option :value="2">普通用户</a-select-option>
              </a-select>
              <a-select
                v-model:value="searchForm.status"
                placeholder="状态"
                style="width: 100px"
                allow-clear
              >
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">禁用</a-select-option>
              </a-select>
            </a-space>
          </a-col>
        </a-row>
        <a-row :gutter="[16, 12]" style="margin-top: 12px">
          <a-col :xs="24" :sm="12" :md="6">
            <a-date-picker
              v-model:value="searchForm.createTime"
              value-format="YYYY-MM-DD"
              placeholder="注册时间"
              style="width: 100%"
              allow-clear
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-date-picker
              v-model:value="searchForm.lastLoginTime"
              value-format="YYYY-MM-DD"
              placeholder="最后登录时间"
              style="width: 100%"
              allow-clear
            />
          </a-col>
          <a-col :xs="24" :sm="12" :md="6">
            <a-select
              v-model:value="searchForm.isdelete"
              placeholder="账号状态"
              style="width: 100%"
              allow-clear
            >
              <a-select-option :value="false">正常账号</a-select-option>
              <a-select-option :value="true">已注销</a-select-option>
            </a-select>
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
          <!-- ID 列：悬浮显示完整 ID，点击可复制 -->
          <template v-if="column.key === 'id'">
            <a-tooltip :title="record.id" placement="topLeft">
              <span class="id-cell" @click="copyId(record.id)">
                {{ record.id }}
              </span>
            </a-tooltip>
          </template>
          <!-- 头像列 -->
          <template v-else-if="column.key === 'avatar'">
            <a-avatar :size="36" :src="record.avatar" style="background-color: #1890ff">
              {{ record.nickname?.charAt(0) || '?' }}
            </a-avatar>
          </template>
          <!-- 角色列 -->
          <template v-else-if="column.key === 'role'">
            <a-tag :color="roleMap[record.role]?.color || '#999'">
              {{ roleMap[record.role]?.text || '未知' }}
            </a-tag>
          </template>

          <!-- 状态列 -->
          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'error'"
              :text="statusMap[record.status]?.text || '未知'"
            />
          </template>

          <!-- 时间列 -->
          <template v-else-if="column.key === 'createTime'">
            {{ formatTime(record.createTime) }}
          </template>
          <template v-else-if="column.key === 'lastLoginTime'">
            {{ formatTime(record.lastLoginTime) }}
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space>
              <!-- 修改角色 - Boss 专属，且不能改 Boss -->
              <a-button
                v-if="userStore.isBoss && record.role !== 0"
                type="link"
                size="small"
                @click="handleChangeRole(record)"
              >
                {{ record.role === 1 ? '降为用户' : '升为管理' }}
              </a-button>

              <!-- 禁用/启用 -->
              <a-button
                v-if="record.role !== 0"
                type="link"
                size="small"
                :class="{ 'danger-link': record.status === 1 }"
                @click="handleChangeStatus(record)"
              >
                {{ record.status === 1 ? '禁用' : '启用' }}
              </a-button>

              <!-- 删除 -->
              <a-button
                v-if="record.role !== 0"
                type="link"
                size="small"
                danger
                @click="handleDelete(record)"
              >
                删除
              </a-button>
            </a-space>
          </template>
        </template>

        <!-- 空状态 -->
        <template #emptyText>
          <a-empty description="暂无用户数据" />
        </template>
      </a-table>
    </a-card>

    <!-- 创建用户弹窗 -->
    <a-modal
      v-model:open="createModalVisible"
      title="创建用户"
      :confirm-loading="createLoading"
      @ok="handleCreateSubmit"
      :width="420"
    >
      <a-form layout="vertical" style="margin-top: 16px">
        <a-form-item label="账号（邮箱或手机号）" required>
          <a-input
            v-model:value="createForm.account"
            placeholder="请输入邮箱或手机号"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="初始密码" required>
          <a-input-password
            v-model:value="createForm.password"
            placeholder="至少 6 位"
            allow-clear
          />
        </a-form-item>
      </a-form>
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

/* 操作按钮样式 */
.danger-link {
  color: #ff7875 !important;
}

.danger-link:hover {
  color: #ff4d4f !important;
}

/* 表格内头像微调 */
:deep(.ant-avatar) {
  vertical-align: middle;
}

/* 标签对齐 */
:deep(.ant-tag) {
  margin-right: 0;
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
</style>
