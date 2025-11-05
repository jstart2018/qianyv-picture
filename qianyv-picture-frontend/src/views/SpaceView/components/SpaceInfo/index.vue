<script setup lang="ts">
import type { Space } from '../../composables/useSpace'
import { getSpaceLevelText, getUsagePercent } from '../../composables/spaceHelpers'
import { formatFileSize } from '@/utils'

interface Props {
  space: Space | null
}

interface Emits {
  (e: 'open-members'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
</script>

<template>
  <div v-if="space" class="space-info-card">
    <div class="info-header">
      <span class="info-icon">📊</span>
      <span class="info-title">空间信息</span>
    </div>
    <div class="info-content">
      <div class="info-item">
        <span class="info-label">空间名称</span>
        <span class="info-value">{{ space.spaceName }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">成员数量</span>
        <div class="member-info">
          <span class="info-value">{{ space.memberCount }} 人</span>
          <button class="view-members-btn" @click="emit('open-members')">👥 查看成员</button>
        </div>
      </div>
      <div class="info-item">
        <span class="info-label">空间等级</span>
        <span class="info-value">{{ getSpaceLevelText(space.spaceLevel) }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">容量使用</span>
        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{
              width: getUsagePercent(space.totalSize, space.maxSize) + '%',
            }"
          ></div>
        </div>
        <span class="info-value-small"
          >{{ formatFileSize(space.totalSize) }} / {{ formatFileSize(space.maxSize) }} ({{
            getUsagePercent(space.totalSize, space.maxSize)
          }}%)</span
        >
      </div>
      <div class="info-item">
        <span class="info-label">图片数量</span>
        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{
              width: getUsagePercent(space.totalCount, space.maxCount) + '%',
            }"
          ></div>
        </div>
        <span class="info-value-small"
          >{{ space.totalCount }} / {{ space.maxCount }} ({{
            getUsagePercent(space.totalCount, space.maxCount)
          }}%)</span
        >
      </div>
    </div>
  </div>
</template>
