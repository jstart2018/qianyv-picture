<script setup lang="ts">
import { useMember } from '../../composables/useMember'
import { getUserAvatarText } from '../../composables/spaceHelpers'
import type { Space } from '../../composables/useSpace'

interface Props {
  show: boolean
  currentSpace: Space | null
}

interface Emits {
  (e: 'close'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const {
  filteredMembers,
  filterRole,
  filterTimeRange,
  searchKeyword,
  removeMember,
  getRoleText,
  getRoleClass,
} = useMember()

const handleRemoveMember = (memberId: number, memberName: string) => {
  removeMember(memberId, memberName, props.currentSpace)
}
</script>

<template>
  <transition name="modal-fade">
    <div v-if="show" class="modal-overlay" @click="emit('close')">
      <div class="modal-container" @click.stop>
        <div class="modal-header">
          <h3>👥 空间成员</h3>
          <button class="modal-close-btn" @click="emit('close')">✕</button>
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
                @click="handleRemoveMember(member.id, member.username)"
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
</template>
