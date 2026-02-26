<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useSpace } from './composables/useSpace'
import { usePicture } from './composables/usePicture'
import { useMember } from './composables/useMember'

import SpaceList from './components/SpaceList/index.vue'
import SpaceInfo from './components/SpaceInfo/index.vue'
import ImageGallery from './components/ImageGallery/index.vue'
import ImageUploader from './components/ImageUploader/index.vue'
import MemberModal from './components/MemberModal/index.vue'
import SpaceSettingModal from './components/SpaceSettingModal/index.vue'

// 空间管理
const {
  mySpaces,
  joinedManageableSpaces,
  joinedEditableSpaces,
  joinedViewableSpaces,
  joinedSpacesCount,
  currentSpace,
  mySpacesExpanded,
  joinedSpacesExpanded,
  joinedManageableExpanded,
  joinedEditableExpanded,
  joinedViewableExpanded,
  editingSpaceId,
  editingSpaceName,
  loadAllSpaces,
  selectSpace,
  createNewSpace,
  startEditSpaceName,
  saveSpaceName,
  refreshCurrentSpace,
  toggleMySpaces,
  toggleJoinedSpaces,
  toggleJoinedManageable,
  toggleJoinedEditable,
  toggleJoinedViewable,
} = useSpace()

// 图片管理
const { fetchPictures } = usePicture()

// 上传管理
const showUploadModal = ref(false)
const refreshKey = ref(0)
const openUploadModal = () => {
  showUploadModal.value = true
}
const closeUploadModal = () => {
  showUploadModal.value = false
}

// 成员管理
const showMembersModal = ref(false)
const openMembersModal = () => {
  showMembersModal.value = true
}
const closeMembersModal = () => {
  showMembersModal.value = false
}

// 设置管理
const showSettingModal = ref(false)
const openSettingModal = () => {
  showSettingModal.value = true
}
const closeSettingModal = () => {
  showSettingModal.value = false
}

// 初始化
onMounted(() => {
  loadAllSpaces()
})

// 监听空间切换，刷新图片列表
watch(
  () => currentSpace.value,
  (newSpace) => {
    if (newSpace) {
      fetchPictures(newSpace.id)
    }
  },
)

// 处理上传完成
const handleUploadComplete = async () => {
  if (currentSpace.value) {
    // 增加刷新 key，触发 ImageGallery 刷新
    refreshKey.value++
    // 刷新空间信息（更新容量进度条）
    await refreshCurrentSpace()
  }
}

// 处理选择空间
const handleSelectSpace = (space: any) => {
  selectSpace(space)
}
</script>

<template>
  <div class="space-view">
    <!-- 左侧：空间列表 -->
    <aside class="left-panel">
      <SpaceList
        :my-spaces="mySpaces"
        :joined-manageable-spaces="joinedManageableSpaces"
        :joined-editable-spaces="joinedEditableSpaces"
        :joined-viewable-spaces="joinedViewableSpaces"
        :joined-spaces-count="joinedSpacesCount"
        :current-space="currentSpace"
        :my-spaces-expanded="mySpacesExpanded"
        :joined-spaces-expanded="joinedSpacesExpanded"
        :joined-manageable-expanded="joinedManageableExpanded"
        :joined-editable-expanded="joinedEditableExpanded"
        :joined-viewable-expanded="joinedViewableExpanded"
        :editing-space-id="editingSpaceId"
        :editing-space-name="editingSpaceName"
        @select-space="handleSelectSpace"
        @create-space="createNewSpace"
        @toggle-my-spaces="toggleMySpaces"
        @toggle-joined-spaces="toggleJoinedSpaces"
        @toggle-joined-manageable="toggleJoinedManageable"
        @toggle-joined-editable="toggleJoinedEditable"
        @toggle-joined-viewable="toggleJoinedViewable"
        @start-edit-space-name="startEditSpaceName"
        @save-space-name="saveSpaceName"
        @update:editing-space-name="(val) => (editingSpaceName = val)"
      />

      <SpaceInfo :space="currentSpace" @open-members="openMembersModal" />
    </aside>

    <!-- 右侧：空间详情 -->
    <main class="right-panel">
      <div v-if="currentSpace" class="space-detail">
        <div class="detail-header">
          <h2>{{ currentSpace.spaceName }}</h2>
          <div class="header-actions">
            <button class="action-btn" @click="openSettingModal">设置</button>
            <button class="action-btn" @click="openUploadModal">上传图片</button>
          </div>
        </div>

        <div class="detail-content">
          <ImageGallery :space-id="currentSpace?.id" :refresh-key="refreshKey" />
        </div>
      </div>

      <div v-else class="empty-content">
        <p>请从左侧选择一个空间</p>
      </div>
    </main>

    <!-- 成员管理弹窗 -->
    <MemberModal
      :show="showMembersModal"
      :current-space="currentSpace"
      :current-user-role="currentSpace?.role"
      @close="closeMembersModal"
    />

    <!-- 上传图片弹窗 -->
    <ImageUploader
      :show="showUploadModal"
      :space-id="currentSpace?.id ?? null"
      @close="closeUploadModal"
      @uploaded="handleUploadComplete"
    />

    <!-- 设置弹窗 -->
    <SpaceSettingModal
      :show="showSettingModal"
      :space-id="currentSpace?.id ?? null"
      @close="closeSettingModal"
      @updated="refreshCurrentSpace"
    />
  </div>
</template>

<style src="./styles.css"></style>
