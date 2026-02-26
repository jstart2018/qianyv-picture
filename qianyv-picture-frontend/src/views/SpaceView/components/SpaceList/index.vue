<script setup lang="ts">
import { nextTick } from 'vue'
import type { Space } from '../../composables/useSpace'
import { getSpaceRoleText, getSpaceRoleClass } from '../../composables/spaceHelpers'
import { formatFileSize } from '@/utils'

interface Props {
  mySpaces: Space[]
  joinedManageableSpaces: Space[]
  joinedEditableSpaces: Space[]
  joinedViewableSpaces: Space[]
  joinedSpacesCount: number
  currentSpace: Space | null
  mySpacesExpanded: boolean
  joinedSpacesExpanded: boolean
  joinedManageableExpanded: boolean
  joinedEditableExpanded: boolean
  joinedViewableExpanded: boolean
  editingSpaceId: number | null
  editingSpaceName: string
}

interface Emits {
  (e: 'select-space', space: Space): void
  (e: 'create-space'): void
  (e: 'toggle-my-spaces'): void
  (e: 'toggle-joined-spaces'): void
  (e: 'toggle-joined-manageable'): void
  (e: 'toggle-joined-editable'): void
  (e: 'toggle-joined-viewable'): void
  (e: 'start-edit-space-name', space: Space, event: Event): void
  (e: 'save-space-name', spaceId: number): void
  (e: 'update:editing-space-name', value: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const handleSpaceNameKeydown = (event: KeyboardEvent, spaceId: number) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    emit('save-space-name', spaceId)
  } else if (event.key === 'Escape') {
    event.preventDefault()
    emit('save-space-name', spaceId)
  }
}

const startEdit = (space: Space, event: Event) => {
  event.stopPropagation()
  emit('start-edit-space-name', space, event)
  nextTick(() => {
    const input = (event.target as HTMLElement)
      .closest('.space-item-header')
      ?.querySelector('input.space-name-input') as HTMLInputElement
    if (input) {
      input.focus()
      input.select()
    }
  })
}
</script>

<template>
  <div class="space-list-card">
    <!-- 我的空间 -->
    <div class="space-section">
      <div class="section-header" @click="emit('toggle-my-spaces')">
        <svg
          class="section-icon-svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
        </svg>
        <span class="section-title">我的空间</span>
        <span class="space-count">({{ mySpaces.length }})</span>
        <button class="create-btn" @click.stop="emit('create-space')">+ 创建</button>
        <span class="toggle-arrow" :class="{ expanded: mySpacesExpanded }">▼</span>
      </div>
      <transition name="expand">
        <div v-show="mySpacesExpanded" class="space-items">
          <div
            v-for="space in mySpaces"
            :key="space.id"
            class="space-item"
            :class="{ active: currentSpace?.id === space.id }"
            @click="emit('select-space', space)"
          >
            <div class="space-item-header">
              <span
                v-if="editingSpaceId !== space.id"
                class="space-name"
                @click="startEdit(space, $event)"
                >{{ space.spaceName }}</span
              >
              <input
                v-else
                class="space-name-input"
                :value="editingSpaceName"
                @input="
                  emit('update:editing-space-name', ($event.target as HTMLInputElement).value)
                "
                @keydown="handleSpaceNameKeydown($event, space.id)"
                @blur="emit('save-space-name', space.id)"
              />
              <span class="space-badge" :class="getSpaceRoleClass(space.role)">{{
                getSpaceRoleText(space.role)
              }}</span>
            </div>
            <div class="space-stats">
              <div class="stat-row">
                <span class="stat-label">容量:</span>
                <span class="stat-value"
                  >{{ formatFileSize(space.totalSize) }} / {{ formatFileSize(space.maxSize) }}</span
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
      <div class="section-header" @click="emit('toggle-joined-spaces')">
        <svg
          class="section-icon-svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
          <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
        <span class="section-title">已加入</span>
        <span class="space-count">({{ joinedSpacesCount }})</span>
        <span class="toggle-arrow" :class="{ expanded: joinedSpacesExpanded }">▼</span>
      </div>
      <transition name="expand">
        <div v-show="joinedSpacesExpanded" class="joined-subcategories">
          <!-- 子分类1: 可管理 -->
          <div class="subcategory">
            <div class="subcategory-header" @click="emit('toggle-joined-manageable')">
              <svg
                class="subcategory-icon-svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <circle cx="12" cy="12" r="3" />
                <path
                  d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"
                />
              </svg>
              <span class="subcategory-title">可管理</span>
              <span class="space-count">({{ joinedManageableSpaces.length }})</span>
              <span class="toggle-arrow" :class="{ expanded: joinedManageableExpanded }">▼</span>
            </div>
            <transition name="expand">
              <div v-show="joinedManageableExpanded" class="space-items">
                <div
                  v-for="space in joinedManageableSpaces"
                  :key="space.id"
                  class="space-item"
                  :class="{ active: currentSpace?.id === space.id }"
                  @click="emit('select-space', space)"
                >
                  <div class="space-item-header">
                    <span class="space-name">{{ space.spaceName }}</span>
                    <span class="space-badge" :class="getSpaceRoleClass(space.role)">{{
                      getSpaceRoleText(space.role)
                    }}</span>
                  </div>
                  <div class="space-stats">
                    <div class="stat-row">
                      <span class="stat-label">容量:</span>
                      <span class="stat-value"
                        >{{ formatFileSize(space.totalSize) }} /
                        {{ formatFileSize(space.maxSize) }}</span
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

          <!-- 子分类2: 可编辑 -->
          <div class="subcategory">
            <div class="subcategory-header" @click="emit('toggle-joined-editable')">
              <svg
                class="subcategory-icon-svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
              <span class="subcategory-title">可编辑</span>
              <span class="space-count">({{ joinedEditableSpaces.length }})</span>
              <span class="toggle-arrow" :class="{ expanded: joinedEditableExpanded }">▼</span>
            </div>
            <transition name="expand">
              <div v-show="joinedEditableExpanded" class="space-items">
                <div
                  v-for="space in joinedEditableSpaces"
                  :key="space.id"
                  class="space-item"
                  :class="{ active: currentSpace?.id === space.id }"
                  @click="emit('select-space', space)"
                >
                  <div class="space-item-header">
                    <span class="space-name">{{ space.spaceName }}</span>
                    <span class="space-badge" :class="getSpaceRoleClass(space.role)">{{
                      getSpaceRoleText(space.role)
                    }}</span>
                  </div>
                  <div class="space-stats">
                    <div class="stat-row">
                      <span class="stat-label">容量:</span>
                      <span class="stat-value"
                        >{{ formatFileSize(space.totalSize) }} /
                        {{ formatFileSize(space.maxSize) }}</span
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

          <!-- 子分类3: 仅浏览 -->
          <div class="subcategory">
            <div class="subcategory-header" @click="emit('toggle-joined-viewable')">
              <svg
                class="subcategory-icon-svg"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <span class="subcategory-title">仅浏览</span>
              <span class="space-count">({{ joinedViewableSpaces.length }})</span>
              <span class="toggle-arrow" :class="{ expanded: joinedViewableExpanded }">▼</span>
            </div>
            <transition name="expand">
              <div v-show="joinedViewableExpanded" class="space-items">
                <div
                  v-for="space in joinedViewableSpaces"
                  :key="space.id"
                  class="space-item"
                  :class="{ active: currentSpace?.id === space.id }"
                  @click="emit('select-space', space)"
                >
                  <div class="space-item-header">
                    <span class="space-name">{{ space.spaceName }}</span>
                    <span class="space-badge" :class="getSpaceRoleClass(space.role)">{{
                      getSpaceRoleText(space.role)
                    }}</span>
                  </div>
                  <div class="space-stats">
                    <div class="stat-row">
                      <span class="stat-label">容量:</span>
                      <span class="stat-value"
                        >{{ formatFileSize(space.totalSize) }} /
                        {{ formatFileSize(space.maxSize) }}</span
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
      </transition>
    </div>
  </div>
</template>
