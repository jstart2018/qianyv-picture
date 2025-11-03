<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useSpaceStore } from '@/stores/space'
import { useUserStore } from '@/stores/user'

const spaceStore = useSpaceStore()
const userStore = useUserStore()

// 展开状态
const mySpacesExpanded = ref(true) // 默认展开
const joinedSpacesExpanded = ref(false) // 默认收起

// 模拟数据：我的空间列表
const mySpaces = ref([
  {
    id: 1,
    spaceName: '设计团队空间',
    spaceLevel: 1,
    maxSize: 1024 * 1024 * 1024, // 1GB
    maxCount: 500,
    totalSize: 256 * 1024 * 1024, // 256MB
    totalCount: 128,
    memberCount: 5,
  },
  {
    id: 2,
    spaceName: '产品素材库',
    spaceLevel: 0,
    maxSize: 100 * 1024 * 1024, // 100MB
    maxCount: 100,
    totalSize: 45 * 1024 * 1024, // 45MB
    totalCount: 32,
    memberCount: 3,
  },
])

// 模拟数据：已加入的空间
const joinedSpaces = ref([
  {
    id: 3,
    spaceName: '营销活动素材',
    spaceLevel: 2,
    maxSize: 5 * 1024 * 1024 * 1024, // 5GB
    maxCount: 2000,
    totalSize: 1.2 * 1024 * 1024 * 1024, // 1.2GB
    totalCount: 456,
    memberCount: 12,
    role: 'editor',
  },
])

// 当前选中的空间
const currentSpace = ref<any>(null)

// 显示成员列表弹窗
const showMembersModal = ref(false)

// 筛选条件
const filterRole = ref<string>('all')
const filterTimeRange = ref<string>('all')
const searchKeyword = ref<string>('')

// 模拟成员数据
const members = ref([
  {
    id: 1,
    username: '张三',
    avatar: '',
    role: 'admin',
    joinTime: '2024-01-15',
  },
  {
    id: 2,
    username: '李四',
    avatar: '',
    role: 'editor',
    joinTime: '2024-02-20',
  },
  {
    id: 3,
    username: '王五',
    avatar: '',
    role: 'viewer',
    joinTime: '2024-03-10',
  },
  {
    id: 4,
    username: '赵六',
    avatar: '',
    role: 'editor',
    joinTime: '2024-03-25',
  },
  {
    id: 5,
    username: '孙七',
    avatar: '',
    role: 'viewer',
    joinTime: '2024-04-05',
  },
])

// 过滤后的成员列表
const filteredMembers = computed(() => {
  return members.value.filter((member) => {
    // 角色筛选
    if (filterRole.value !== 'all' && member.role !== filterRole.value) {
      return false
    }

    // 时间范围筛选
    if (filterTimeRange.value !== 'all') {
      const joinDate = new Date(member.joinTime)
      const now = new Date()
      const diffInMonths =
        (now.getFullYear() - joinDate.getFullYear()) * 12 + (now.getMonth() - joinDate.getMonth())

      if (filterTimeRange.value === '1month' && diffInMonths > 1) {
        return false
      } else if (filterTimeRange.value === '3months' && diffInMonths > 3) {
        return false
      } else if (filterTimeRange.value === '6months' && diffInMonths > 6) {
        return false
      }
    }

    // 搜索关键词筛选
    if (searchKeyword.value && !member.username.includes(searchKeyword.value)) {
      return false
    }

    return true
  })
})

// 切换展开状态
const toggleMySpaces = () => {
  mySpacesExpanded.value = !mySpacesExpanded.value
}

const toggleJoinedSpaces = () => {
  joinedSpacesExpanded.value = !joinedSpacesExpanded.value
}

// 打开成员列表弹窗
const openMembersModal = () => {
  showMembersModal.value = true
}

// 关闭成员列表弹窗
const closeMembersModal = () => {
  showMembersModal.value = false
}

// 踢出成员
const removeMember = (memberId: number, memberName: string) => {
  if (confirm(`确定要将 ${memberName} 移出空间吗？`)) {
    // 从成员列表中移除
    const index = members.value.findIndex((m) => m.id === memberId)
    if (index !== -1) {
      members.value.splice(index, 1)
      // 更新当前空间的成员数量
      if (currentSpace.value) {
        currentSpace.value.memberCount--
      }
      alert(`已成功将 ${memberName} 移出空间`)
    }
  }
}

// 获取角色显示文本
const getRoleText = (role: string) => {
  const roleMap: { [key: string]: string } = {
    admin: '管理员',
    editor: '编辑者',
    viewer: '查看者',
  }
  return roleMap[role] || role
}

// 获取角色类名
const getRoleClass = (role: string) => {
  return role
}

// 获取用户头像文本
const getUserAvatarText = (username: string) => {
  return username ? username.charAt(0).toUpperCase() : '?'
}

// 格式化文件大小
const formatSize = (bytes: number) => {
  if (bytes >= 1024 * 1024 * 1024) {
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  } else if (bytes >= 1024 * 1024) {
    return (bytes / (1024 * 1024)).toFixed(0) + ' MB'
  } else {
    return (bytes / 1024).toFixed(0) + ' KB'
  }
}

// 选择空间
const selectSpace = (space: any) => {
  currentSpace.value = space
  spaceStore.switchSpace(space.id)
}

// 计算使用百分比
const getUsagePercent = (used: number, total: number) => {
  return Math.round((used / total) * 100)
}

// 初始化
onMounted(() => {
  // 默认选中第一个空间
  if (mySpaces.value.length > 0) {
    selectSpace(mySpaces.value[0])
  }
})
</script>

<template>
  <div class="space-view">
    <!-- 左侧：空间列表卡片 -->
    <aside class="left-panel">
      <!-- 空间列表卡片 -->
      <div class="space-list-card">
        <!-- 我的空间 -->
        <div class="space-section">
          <div class="section-header" @click="toggleMySpaces">
            <span class="section-icon">📁</span>
            <span class="section-title">我的空间</span>
            <span class="space-count">({{ mySpaces.length }})</span>
            <button class="create-btn" @click.stop="() => {}">+ 创建</button>
            <span class="toggle-arrow" :class="{ expanded: mySpacesExpanded }">▼</span>
          </div>
          <transition name="expand">
            <div v-show="mySpacesExpanded" class="space-items">
              <div
                v-for="space in mySpaces"
                :key="space.id"
                class="space-item"
                :class="{ active: currentSpace?.id === space.id }"
                @click="selectSpace(space)"
              >
                <div class="space-item-header">
                  <span class="space-name">{{ space.spaceName }}</span>
                  <span class="space-badge owner">创建者</span>
                </div>
                <div class="space-stats">
                  <div class="stat-row">
                    <span class="stat-label">容量:</span>
                    <span class="stat-value"
                      >{{ formatSize(space.totalSize) }} / {{ formatSize(space.maxSize) }}</span
                    >
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">图片:</span>
                    <span class="stat-value">{{ space.totalCount }} / {{ space.maxCount }}</span>
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">成员:</span>
                    <span class="stat-value">{{ space.memberCount }} 人</span>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>

        <!-- 已加入的空间 -->
        <div class="space-section">
          <div class="section-header" @click="toggleJoinedSpaces">
            <span class="section-icon">👥</span>
            <span class="section-title">已加入</span>
            <span class="space-count">({{ joinedSpaces.length }})</span>
            <span class="toggle-arrow" :class="{ expanded: joinedSpacesExpanded }">▼</span>
          </div>
          <transition name="expand">
            <div v-show="joinedSpacesExpanded" class="space-items">
              <div
                v-for="space in joinedSpaces"
                :key="space.id"
                class="space-item"
                :class="{ active: currentSpace?.id === space.id }"
                @click="selectSpace(space)"
              >
                <div class="space-item-header">
                  <span class="space-name">{{ space.spaceName }}</span>
                  <span class="space-badge" :class="space.role">{{
                    space.role === 'editor' ? '编辑者' : '查看者'
                  }}</span>
                </div>
                <div class="space-stats">
                  <div class="stat-row">
                    <span class="stat-label">容量:</span>
                    <span class="stat-value"
                      >{{ formatSize(space.totalSize) }} / {{ formatSize(space.maxSize) }}</span
                    >
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">图片:</span>
                    <span class="stat-value">{{ space.totalCount }} / {{ space.maxCount }}</span>
                  </div>
                  <div class="stat-row">
                    <span class="stat-label">成员:</span>
                    <span class="stat-value">{{ space.memberCount }} 人</span>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </div>
      </div>

      <!-- 当前空间信息卡片 -->
      <div v-if="currentSpace" class="space-info-card">
        <div class="info-header">
          <span class="info-icon">📊</span>
          <span class="info-title">空间信息</span>
        </div>
        <div class="info-content">
          <div class="info-item">
            <span class="info-label">空间名称</span>
            <span class="info-value">{{ currentSpace.spaceName }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">成员数量</span>
            <div class="member-info">
              <span class="info-value">{{ currentSpace.memberCount }} 人</span>
              <button class="view-members-btn" @click="openMembersModal">👥 查看成员</button>
            </div>
          </div>
          <div class="info-item">
            <span class="info-label">空间等级</span>
            <span class="info-value">{{
              currentSpace.spaceLevel === 0 ? '免费版' : `Lv${currentSpace.spaceLevel}`
            }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">容量使用</span>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{
                  width: getUsagePercent(currentSpace.totalSize, currentSpace.maxSize) + '%',
                }"
              ></div>
            </div>
            <span class="info-value-small"
              >{{ formatSize(currentSpace.totalSize) }} / {{ formatSize(currentSpace.maxSize) }} ({{
                getUsagePercent(currentSpace.totalSize, currentSpace.maxSize)
              }}%)</span
            >
          </div>
          <div class="info-item">
            <span class="info-label">图片数量</span>
            <div class="progress-bar">
              <div
                class="progress-fill"
                :style="{
                  width: getUsagePercent(currentSpace.totalCount, currentSpace.maxCount) + '%',
                }"
              ></div>
            </div>
            <span class="info-value-small"
              >{{ currentSpace.totalCount }} / {{ currentSpace.maxCount }} ({{
                getUsagePercent(currentSpace.totalCount, currentSpace.maxCount)
              }}%)</span
            >
          </div>
        </div>
      </div>
    </aside>

    <!-- 右侧：空间详情（带滚动条） -->
    <main class="right-panel">
      <div v-if="currentSpace" class="space-detail">
        <div class="detail-header">
          <h2>{{ currentSpace.spaceName }}</h2>
          <div class="header-actions">
            <button class="action-btn">⚙️ 设置</button>
            <button class="action-btn primary">📤 上传图片</button>
          </div>
        </div>

        <div class="detail-content">
          <div class="placeholder-box">
            <h3>空间内容区域</h3>
            <p>图片网格和成员管理功能正在开发中...</p>
            <div style="height: 800px; margin-top: 20px">
              <p style="opacity: 0.5">滚动测试内容区域</p>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p>请从左侧选择一个空间</p>
      </div>
    </main>

    <!-- 成员列表弹窗 -->
    <transition name="modal-fade">
      <div v-if="showMembersModal" class="modal-overlay" @click="closeMembersModal">
        <div class="modal-container" @click.stop>
          <div class="modal-header">
            <h3>👥 空间成员</h3>
            <button class="modal-close-btn" @click="closeMembersModal">✕</button>
          </div>

          <!-- 筛选条件区域 -->
          <div class="modal-filters">
            <!-- 搜索框 -->
            <div class="filter-item search-box">
              <span class="filter-icon">🔍</span>
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索成员姓名..."
                class="search-input"
              />
            </div>

            <!-- 角色筛选 -->
            <div class="filter-item">
              <label class="filter-label">角色：</label>
              <select v-model="filterRole" class="filter-select">
                <option value="all">全部角色</option>
                <option value="admin">管理员</option>
                <option value="editor">编辑者</option>
                <option value="viewer">查看者</option>
              </select>
            </div>

            <!-- 时间范围筛选 -->
            <div class="filter-item">
              <label class="filter-label">加入时间：</label>
              <select v-model="filterTimeRange" class="filter-select">
                <option value="all">全部时间</option>
                <option value="1month">最近1个月</option>
                <option value="3months">最近3个月</option>
                <option value="6months">最近6个月</option>
              </select>
            </div>
          </div>

          <div class="modal-body">
            <div v-if="filteredMembers.length === 0" class="empty-members">
              <p>😔 没有找到符合条件的成员</p>
            </div>
            <div v-else class="members-list">
              <div v-for="member in filteredMembers" :key="member.id" class="member-item">
                <div class="member-avatar" :class="{ 'has-image': member.avatar }">
                  <img v-if="member.avatar" :src="member.avatar" alt="avatar" />
                  <span v-else>{{ getUserAvatarText(member.username) }}</span>
                </div>
                <div class="member-info-detail">
                  <div class="member-name">{{ member.username }}</div>
                  <div class="member-join-time">加入时间: {{ member.joinTime }}</div>
                </div>
                <div class="member-role-badge" :class="getRoleClass(member.role)">
                  {{ getRoleText(member.role) }}
                </div>
                <button
                  class="remove-member-btn"
                  @click="removeMember(member.id, member.username)"
                  title="踢出成员"
                >
                  🚫
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.space-view {
  width: 100%;
  min-height: calc(100vh - 60px);
  display: flex;
  gap: 0;
  position: relative;
}

/* ========== 左侧空间列表 ========== */
.left-panel {
  position: fixed;
  left: 0;
  top: 96.5px; /* 从导航栏下方开始，增加一点偏移 */
  width: 28%;
  height: calc(100vh - 96.5px); /* 高度为视口减去导航栏高度 */
  overflow-y: auto;
  overflow-x: hidden;
  background: transparent;
  z-index: 10; /* 低于导航栏的 z-index: 100 */
  padding: 24px 12px;
}

/* 左侧滚动条样式 */
.left-panel::-webkit-scrollbar {
  width: 6px;
}

.left-panel::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.left-panel::-webkit-scrollbar-thumb {
  background: rgba(138, 180, 248, 0.3);
  border-radius: 3px;
  transition: background 0.3s ease;
}

.left-panel::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 180, 248, 0.5);
}

.space-list-card {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 2px dashed rgba(223, 231, 245, 0.5);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.space-list-card:hover {
  border-color: rgba(138, 180, 248, 0.6);
  box-shadow: 0 12px 48px rgba(138, 180, 248, 0.15);
}

.space-section {
  margin-bottom: 16px;
}

.space-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 0;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.section-header:hover {
  background: rgba(138, 180, 248, 0.1);
}

.section-icon {
  font-size: 18px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c5282;
}

.space-count {
  font-size: 13px;
  color: #718096;
  margin-left: -4px;
}

.toggle-arrow {
  margin-left: auto;
  font-size: 12px;
  color: #718096;
  transition: transform 0.3s ease;
  display: inline-block;
}

.toggle-arrow.expanded {
  transform: rotate(180deg);
}

.create-btn {
  padding: 4px 12px;
  background: rgba(138, 180, 248, 0.15);
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 8px;
  color: #1aa0c1;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-right: 8px;
}

.create-btn:hover {
  background: rgba(138, 180, 248, 0.25);
  border-color: rgba(138, 180, 248, 0.5);
  transform: translateY(-1px);
}

.space-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 0;
  overflow: hidden;
}

/* 展开/收起动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  opacity: 1;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
  padding: 0;
}

.space-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(138, 180, 248, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.space-item:hover {
  background: rgba(255, 255, 255, 0.4);
  border-color: rgba(138, 180, 248, 0.4);
  transform: translateX(4px);
}

.space-item.active {
  background: rgba(138, 180, 248, 0.2);
  border-color: rgba(138, 180, 248, 0.6);
  box-shadow: 0 2px 8px rgba(138, 180, 248, 0.2);
}

.space-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.space-name {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
}

.space-badge {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
}

.space-badge.owner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.space-badge.editor {
  background: linear-gradient(135deg, #1aa0c1 0%, #52c85c 100%);
  color: white;
}

.space-badge.viewer {
  background: rgba(160, 174, 192, 0.3);
  color: #4a5568;
}

.space-stats {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.stat-label {
  color: #718096;
}

.stat-value {
  color: #4a5568;
  font-weight: 500;
}

/* ========== 空间信息卡片 ========== */
.space-info-card {
  margin-top: 16px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 2px dashed rgba(223, 231, 245, 0.5);
  border-radius: 20px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  animation: slideIn 0.4s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.space-info-card:hover {
  border-color: rgba(138, 180, 248, 0.6);
  box-shadow: 0 12px 48px rgba(138, 180, 248, 0.15);
}

.info-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.info-icon {
  font-size: 18px;
}

.info-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c5282;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-label {
  font-size: 12px;
  color: #718096;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #2d3748;
  font-weight: 600;
}

.member-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.view-members-btn {
  padding: 6px 12px;
  background: rgba(138, 180, 248, 0.15);
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 8px;
  color: #1aa0c1;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.view-members-btn:hover {
  background: rgba(138, 180, 248, 0.25);
  border-color: rgba(138, 180, 248, 0.5);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(138, 180, 248, 0.2);
}

.info-value-small {
  font-size: 11px;
  color: #718096;
  margin-top: 4px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
  transition: width 0.5s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

/* ========== 右侧空间详情（带滚动条） ========== */
.right-panel {
  margin-left: 28%;
  width: 72%;
  min-height: calc(100vh - 60px);
  padding: 24px;
  padding-top: 25px; /* 减少顶部内边距，让内容区域向上移动 */
  overflow-y: auto;
  overflow-x: hidden;
}

/* 右侧滚动条样式 */
.right-panel::-webkit-scrollbar {
  width: 8px;
}

.right-panel::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.right-panel::-webkit-scrollbar-thumb {
  background: rgba(138, 180, 248, 0.3);
  border-radius: 4px;
  transition: background 0.3s ease;
}

.right-panel::-webkit-scrollbar-thumb:hover {
  background: rgba(138, 180, 248, 0.5);
}

.space-detail {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 2px dashed rgba(223, 231, 245, 0.5);
  border-radius: 20px;
  padding: 24px;
  min-height: 600px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid rgba(0, 0, 0, 0.08);
}

.detail-header h2 {
  font-size: 28px;
  font-weight: 700;
  color: #1aa0c1;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(138, 180, 248, 0.3);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.7);
  border-color: rgba(138, 180, 248, 0.5);
  transform: translateY(-2px);
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}

.action-btn.primary:hover {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.detail-content {
  padding: 20px 0;
}

.placeholder-box {
  text-align: center;
  padding: 40px 60px;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.placeholder-box h3 {
  font-size: 24px;
  color: #1aa0c1;
  margin-bottom: 12px;
  font-weight: 700;
}

.placeholder-box p {
  font-size: 16px;
  color: #718096;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  font-size: 18px;
  color: #a0aec0;
}

/* ========== 成员列表弹窗 ========== */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.1); /* 白色半透明背景 */
  backdrop-filter: blur(3px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background: rgba(255, 255, 255, 1);
  backdrop-filter: blur(25px);
  border-radius: 24px;
  border: 2px solid rgba(255, 255, 255, 0.4); /* 白色边框 */
  box-shadow:
    0 25px 70px rgba(255, 255, 255, 0.35),
    /* 白色蓝色阴影 */ 0 0 0 1px rgba(255, 255, 255, 0.5) inset; /* 内部浅青色描边 */
  width: 90%;
  max-width: 800px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: modalSlideIn 0.3s ease;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 28px;
  background: rgb(178, 214, 241, 0.5); /* 卡片顶部背景色 */
}

.modal-header h3 {
  font-size: 22px;
  font-weight: 700;
  color: #000000; /* 黑色文字 */
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.modal-close-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgb(255, 255, 255); /* 稍微调整红色按钮透明度 */
  border: 1px solid rgba(100, 97, 97, 0.3);
  font-size: 20px;
  color: #f02222; /* 红色 × */
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.modal-close-btn:hover {
  background: rgba(0, 0, 0, 0.25);
  border-color: rgba(0, 0, 0, 0.5);
  transform: rotate(90deg);
}

/* 筛选条件区域 */
.modal-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 20px 28px;
  background: rgba(255, 255, 255, 0.658);
  border-bottom: 2px solid rgba(0, 0, 0, 0.3);
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item.search-box {
  flex: 1;
  min-width: 200px;
  position: relative;
}

.filter-icon {
  font-size: 16px;
  opacity: 0.7;
  color: #000000; /* 蓝色图标 */
}

.search-input {
  flex: 1;
  padding: 10px 12px 10px 36px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(0, 0, 0, 0.4); /* 蓝色边框 */
  border-radius: 12px;
  font-size: 14px;
  color: #000000; /* 深蓝色文字 */
  outline: none;
  transition: all 0.3s ease;
}

.search-input:focus {
  background: rgba(253, 252, 252, 0.5);
  border-color: rgba(0, 0, 0, 0.8); /* 聚焦时更深的蓝色 */
}

.search-input::placeholder {
  color: rgba(0, 0, 0, 0.45); /* 蓝色占位符 */
}

.filter-item.search-box .filter-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

.filter-label {
  font-size: 13px;
  font-weight: 600;
  color: #000000; /* 深蓝色标签 */
  white-space: nowrap;
}

.filter-select {
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(0, 0, 0, 0.4); /* 黑色边框 */
  border-radius: 10px;
  font-size: 13px;
  color: #000000; /* 深蓝色文字 */
  outline: none;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
}

.filter-select:hover {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(0, 0, 0, 0.6); /* hover时更深蓝色 */
}

.filter-select:focus {
  background: rgba(255, 255, 255, 1);
  border-color: rgba(0, 0, 0, 0.7);
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
  background: linear-gradient(
    135deg,
    rgba(59, 130, 246, 0.05) 0%,
    rgba(37, 99, 235, 0.03) 100%
  ); /* 淡蓝色渐变背景 */
}

.modal-body::-webkit-scrollbar {
  width: 8px;
}

.modal-body::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.12); /* 蓝色滚动条轨道 */
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.5); /* 蓝色滚动条滑块 */
  border-radius: 4px;
  transition: background 0.3s ease;
}

.modal-body::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.7); /* hover时更深的蓝色 */
}

.empty-members {
  text-align: center;
  padding: 60px 20px;
  color: #000000;
}

.empty-members p {
  font-size: 16px;
  margin: 0;
  color: #000000; /* 蓝色文字 */
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.85); /* 更不透明的白色背景 */
  backdrop-filter: blur(10px);
  border: 2px solid rgba(0, 0, 0, 0.35); /* 蓝色边框 */
  border-radius: 16px;
  transition: all 0.3s ease;
}

.member-item:hover {
  background: rgba(255, 255, 255, 0.95);
  border-color: rgba(0, 0, 0, 0.6); /* hover时更深的蓝色 */
  transform: translateX(6px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.3); /* 蓝色阴影 */
}

.member-avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: white;
  background: linear-gradient(135deg, #89daab 0%, #59cce9 100%); /* 蓝色渐变 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.5); /* 蓝色阴影 */
  flex-shrink: 0;
  overflow: hidden;
}

.member-avatar.has-image {
  background: transparent;
  border-color: rgba(0, 0, 0, 0.4); /* 蓝色边框 */
}

.member-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.member-info-detail {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 16px;
  font-weight: 600;
  color: #000000; /* 深蓝色文字 */
  margin-bottom: 5px;
}

.member-join-time {
  font-size: 13px;
  color: #000000;
}

.member-role-badge {
  padding: 6px 14px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.member-role-badge.admin {
  background: linear-gradient(135deg, #98e1e4 0%, #3574e9 100%); /* 靛蓝色渐变 */
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.member-role-badge.editor {
  background: linear-gradient(135deg, #5ce289 0%, #0f8f35 100%); /* 蓝色渐变 */
  color: white;
  box-shadow: 0 2px 8px rgba(138, 238, 180, 0.5);
}

.member-role-badge.viewer {
  background: rgba(59, 130, 246, 0.15); /* 淡蓝色背景 */
  color: #000000; /*  */
  border: 1px solid rgba(0, 0, 0, 0.4); /* 蓝色边框 */
}

/* 踢出成员按钮 */
.remove-member-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
  margin-left: 8px;
}

.remove-member-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.6);
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}

.remove-member-btn:active {
  transform: scale(0.95);
}

/* 弹窗淡入淡出动画 */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active .modal-container,
.modal-fade-leave-active .modal-container {
  transition: transform 0.3s ease;
}

.modal-fade-enter-from .modal-container,
.modal-fade-leave-to .modal-container {
  transform: translateY(-30px) scale(0.95);
}
</style>
