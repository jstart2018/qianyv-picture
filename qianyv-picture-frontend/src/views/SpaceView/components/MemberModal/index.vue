<script setup lang="ts">
import { computed, toRef, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useMember } from '../../composables/useMember'
import type { Space } from '../../composables/useSpace'
import { inviteMember } from '@/api/spaceController'

interface Props {
  show: boolean
  currentSpace: Space | null
  currentUserRole?: number
}

interface Emits {
  (e: 'close'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const spaceIdRef = computed(() => props.currentSpace?.id ?? null)
const showRef = toRef(props, 'show')

const {
  members,
  loading,
  searchKeyword,
  filterRole,
  currentPage,
  totalPages,
  total,
  handleRemoveMember,
  handleEditRole,
  getRoleText,
  getRoleClass,
  setRoleFilter,
  changePage,
  handleSearch,
  fetchMembers,
} = useMember(spaceIdRef, showRef)

const handleClose = () => {
  emit('close')
}

const handleRemove = async (member: any) => {
  await handleRemoveMember(member, spaceIdRef.value, props.currentUserRole)
}

// 是否有权限踢出成员
const canRemoveMember = (memberRole: number) => {
  // 创建者不能被踢
  if (memberRole === 0) return false

  // 只有创建者和管理员能踢人
  if (props.currentUserRole !== 0 && props.currentUserRole !== 1) return false

  // 管理员只能由创建者踢出
  if (memberRole === 1 && props.currentUserRole !== 0) return false

  return true
}

// 是否有权限修改角色（创建者或管理员）
const canEditRole = computed(() => {
  return props.currentUserRole === 0 || props.currentUserRole === 1
})

// 可选择的角色选项（排除创建者）
const roleOptions = [
  { value: 1, label: '管理员' },
  { value: 2, label: '编辑者' },
  { value: 3, label: '查看者' },
]

// 修改成员角色
const editingMemberId = ref<number | null>(null)

const startEditRole = (member: any) => {
  // 不能修改创建者角色
  if (member.role === 0) {
    message.warning('不能修改创建者角色')
    return
  }
  editingMemberId.value = member.id
}

const cancelEditRole = () => {
  editingMemberId.value = null
}

const confirmEditRole = async (member: any, event: Event) => {
  const target = event.target as HTMLSelectElement
  const newRole = Number(target.value)
  await handleEditRole(member, spaceIdRef.value, newRole)
  editingMemberId.value = null
}

// 邀请成员
const showInviteInput = ref(false)
const inviteUserId = ref('')
const inviting = ref(false)

const toggleInviteInput = () => {
  showInviteInput.value = !showInviteInput.value
  if (!showInviteInput.value) {
    inviteUserId.value = ''
  }
}

const handleInvite = async () => {
  const userId = inviteUserId.value.trim()
  if (!userId) {
    message.warning('请输入用户ID')
    return
  }

  const spaceId = spaceIdRef.value
  if (!spaceId) {
    message.error('空间ID不能为空')
    return
  }

  console.log('开始邀请成员:', { spaceId, userId })

  inviting.value = true
  try {
    const res = await inviteMember({
      spaceId: spaceId,
      userId: userId as any,
    })
    console.log('邀请接口返回:', res)
    if (res.data && res.data.code === 0) {
      message.success('邀请成功')
      inviteUserId.value = ''
      showInviteInput.value = false
      // 刷新成员列表
      if (spaceIdRef.value) {
        await fetchMembers(spaceIdRef.value)
      }
    } else {
      message.error(res.data?.message || '邀请失败')
    }
  } catch (err) {
    console.error('邀请成员失败:', err)
    message.error('邀请失败，请重试')
  } finally {
    inviting.value = false
  }
}

// 角色筛选选项
const roleFilters = [
  { value: null, label: '全部' },
  { value: 0, label: '创建者' },
  { value: 1, label: '管理员' },
  { value: 2, label: '编辑者' },
  { value: 3, label: '查看者' },
]

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 获取头像首字母
const getAvatarText = (name: string) => {
  return name ? name.charAt(0).toUpperCase() : '?'
}
</script>

<template>
  <teleport to="body">
    <transition name="fade">
      <div v-if="show" class="member-modal-overlay" @click="handleClose">
        <div class="member-modal" @click.stop>
          <!-- 头部 -->
          <div class="modal-header">
            <h3 class="modal-title">
              <svg
                class="title-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
              空间成员
              <span class="member-count" v-if="total > 0">({{ total }})</span>
            </h3>
            <button class="close-btn" @click="handleClose">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>

          <!-- 搜索和筛选 -->
          <div class="modal-toolbar">
            <div class="search-box">
              <svg
                class="search-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索成员..."
                class="search-input"
                @input="handleSearch"
              />
            </div>

            <!-- 邀请成员 -->
            <div class="invite-section">
              <button v-if="!showInviteInput" class="invite-btn" @click="toggleInviteInput">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                  <circle cx="8.5" cy="7" r="4"></circle>
                  <line x1="20" y1="8" x2="20" y2="14"></line>
                  <line x1="23" y1="11" x2="17" y2="11"></line>
                </svg>
                邀请成员
              </button>
              <div v-else class="invite-input-wrapper">
                <input
                  v-model="inviteUserId"
                  type="text"
                  placeholder="输入用户ID"
                  class="invite-input"
                  @keyup.enter="handleInvite"
                />
                <button class="invite-confirm" :disabled="inviting" @click="handleInvite">
                  {{ inviting ? '邀请中...' : '确认' }}
                </button>
                <button class="invite-cancel" @click="toggleInviteInput">取消</button>
              </div>
            </div>

            <div class="role-filters">
              <button
                v-for="role in roleFilters"
                :key="role.label"
                class="filter-btn"
                :class="{ active: filterRole === role.value }"
                @click="setRoleFilter(role.value)"
              >
                {{ role.label }}
              </button>
            </div>
          </div>

          <!-- 成员列表 -->
          <div class="modal-body">
            <!-- 加载状态 -->
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner"></div>
              <span>加载中...</span>
            </div>

            <!-- 空状态 -->
            <div v-else-if="members.length === 0" class="empty-state">
              <svg
                class="empty-icon"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
              >
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <p>{{ searchKeyword || filterRole !== null ? '未找到匹配的成员' : '暂无成员' }}</p>
            </div>

            <!-- 成员列表 -->
            <div v-else class="member-list">
              <div v-for="member in members" :key="member.id" class="member-item">
                <div class="member-avatar">
                  <img v-if="member.avatar" :src="member.avatar" :alt="member.username" />
                  <span v-else>{{ getAvatarText(member.username) }}</span>
                </div>
                <div class="member-info">
                  <span class="member-name">{{ member.username }}</span>
                  <span class="member-time">
                    <span class="time-label">加入:</span>{{ formatDate(member.joinTime) }}
                    <span class="time-separator">|</span>
                    <span class="time-label">角色变更:</span
                    >{{ formatDateTime(member.roleUpdateTime) }}
                  </span>
                </div>
                <span
                  v-if="editingMemberId !== member.id"
                  class="member-role editable"
                  :class="getRoleClass(member.role)"
                  @click="canEditRole && member.role !== 0 && startEditRole(member)"
                >
                  {{ getRoleText(member.role) }}
                  <svg
                    v-if="canEditRole && member.role !== 0"
                    class="dropdown-icon"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <polyline points="6 9 12 15 18 9"></polyline>
                  </svg>
                </span>
                <!-- 角色选择下拉框 -->
                <div v-else class="role-select-wrapper">
                  <select
                    class="role-select"
                    :value="member.role"
                    @change="(e) => confirmEditRole(member, e)"
                  >
                    <option v-for="role in roleOptions" :key="role.value" :value="role.value">
                      {{ role.label }}
                    </option>
                  </select>
                  <button class="role-cancel" @click="cancelEditRole">取消</button>
                </div>

                <button
                  v-if="canRemoveMember(member.role)"
                  class="remove-btn"
                  @click="handleRemove(member)"
                  title="移出空间"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                    <circle cx="8.5" cy="7" r="4"></circle>
                    <line x1="18" y1="8" x2="23" y2="13"></line>
                    <line x1="23" y1="8" x2="18" y2="13"></line>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="totalPages > 1" class="modal-footer">
            <div class="pagination">
              <button
                class="page-btn"
                :disabled="currentPage === 1"
                @click="changePage(currentPage - 1)"
              >
                上一页
              </button>
              <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
              <button
                class="page-btn"
                :disabled="currentPage === totalPages"
                @click="changePage(currentPage + 1)"
              >
                下一页
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<style scoped>
/* 遮罩层 */
.member-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

/* 弹窗主体 */
.member-modal {
  background: #ffffff;
  border-radius: 16px;
  width: 100%;
  max-width: 580px;
  height: 600px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

/* 头部 */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  width: 24px;
  height: 24px;
  color: #4a90d9;
}

.member-count {
  font-size: 14px;
  font-weight: 400;
  color: #999;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f5f5;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: #666;
}

.close-btn svg {
  width: 18px;
  height: 18px;
}

.close-btn:hover {
  background: #e8e8e8;
  color: #333;
}

/* 工具栏 */
.modal-toolbar {
  padding: 16px 24px;
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  width: 16px;
  height: 16px;
  color: #999;
}

.search-input {
  width: 100%;
  padding: 10px 12px 10px 36px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  font-size: 14px;
  background: #fafafa;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #4a90d9;
  background: #ffffff;
}

/* 邀请成员 */
.invite-section {
  display: flex;
  align-items: center;
}

.invite-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #8ab4f8 0%, #667eea 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.invite-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(138, 180, 248, 0.4);
}

.invite-btn svg {
  width: 16px;
  height: 16px;
}

.invite-input-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.invite-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #8ab4f8;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.invite-input:focus {
  border-color: #667eea;
}

.invite-confirm {
  padding: 8px 16px;
  background: #8ab4f8;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.invite-confirm:hover:not(:disabled) {
  background: #667eea;
}

.invite-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.invite-cancel {
  padding: 8px 16px;
  background: #f1f5f9;
  color: #64748b;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.invite-cancel:hover {
  background: #e2e8f0;
}

/* 角色筛选 */
.role-filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 6px 14px;
  border: 1px solid #e8e8e8;
  background: #ffffff;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #4a90d9;
  color: #4a90d9;
}

.filter-btn.active {
  background: #4a90d9;
  border-color: #4a90d9;
  color: #ffffff;
}

/* 内容区 */
.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
  gap: 12px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #f0f0f0;
  border-top-color: #4a90d9;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
  color: #ccc;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 成员列表 */
.member-list {
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  transition: background 0.2s;
}

.member-item:hover {
  background: #f8f9fa;
}

.member-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;
}

.member-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.member-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.member-name {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.member-time {
  font-size: 12px;
  color: #999;
}

.time-label {
  font-weight: 500;
}

.time-separator {
  margin: 0 4px;
}

.member-role {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.member-role.creator {
  background: #fef3f2;
  color: #dc2626;
}

.member-role.admin {
  background: #eff6ff;
  color: #2563eb;
}

.member-role.editor {
  background: #f0fdf4;
  color: #16a34a;
}

.member-role.viewer {
  background: #f3f4f6;
  color: #6b7280;
}

.member-role.editable {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: all 0.2s;
}

.member-role.editable:hover {
  filter: brightness(0.95);
  transform: translateY(-1px);
}

.dropdown-icon {
  width: 12px;
  height: 12px;
  opacity: 0.7;
}

/* 角色选择下拉框 */
.role-select-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
}

.role-select {
  padding: 6px 10px;
  border: 2px solid #4a90d9;
  border-radius: 8px;
  font-size: 13px;
  background: white;
  color: #1a1a1a;
  cursor: pointer;
  outline: none;
  font-weight: 500;
  min-width: 80px;
}

.role-select:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(74, 144, 217, 0.15);
}

.role-cancel {
  padding: 6px 12px;
  background: #f3f4f6;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.role-cancel:hover {
  background: #e5e7eb;
}

/* 踢出成员按钮 */
.remove-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: transparent;
  border: 1px solid #e8e8e8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-left: 8px;
  opacity: 0;
  color: #999;
}

.remove-btn svg {
  width: 16px;
  height: 16px;
}

.member-item:hover .remove-btn {
  opacity: 1;
}

.remove-btn:hover {
  background: #fef2f2;
  border-color: #fecaca;
  color: #ef4444;
}

/* 底部分页 */
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.page-btn {
  padding: 8px 16px;
  border: 1px solid #e8e8e8;
  background: #ffffff;
  border-radius: 8px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #4a90d9;
  color: #4a90d9;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 13px;
  color: #999;
}

/* 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
