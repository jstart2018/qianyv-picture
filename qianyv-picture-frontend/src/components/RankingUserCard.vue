<script setup lang="ts">
import UserAvatar from './UserAvatar.vue'

interface User {
  id: number
  nickname: string
  avatar?: string
  postCount: number
  fansCount: number
  recentShareCount: number
}

interface Props {
  user: User
  rank: number
}

defineProps<Props>()
</script>

<template>
  <div class="ranking-item">
    <UserAvatar :nickname="user.nickname" :avatar="user.avatar" size="medium" />
    <div class="rank-info">
      <div class="rank-name">{{ user.nickname }}</div>
      <div class="rank-stats">
        <span>发布: {{ user.postCount }}</span>
        <span>粉丝: {{ user.fansCount }}</span>
        <span>分享: {{ user.recentShareCount }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 6px;
  background: transparent;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item:hover {
  background: rgba(138, 180, 248, 0.08);
  transform: translateX(2px);
}

.rank-info {
  flex: 1;
  min-width: 0;
}

.rank-name {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-stats {
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: #a0aec0;
}

.rank-stats span {
  white-space: nowrap;
}
</style>
