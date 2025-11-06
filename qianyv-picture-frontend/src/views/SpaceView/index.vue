<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useSpace } from './composables/useSpace'
import { usePicture } from './composables/usePicture'
import { useMember } from './composables/useMember'

import SpaceList from './components/SpaceList/index.vue'
import SpaceInfo from './components/SpaceInfo/index.vue'
import ImageGallery from './components/ImageGallery/index.vue'
import ImageUploader from './components/ImageUploader/index.vue'
import MemberModal from './components/MemberModal/index.vue'

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
const openUploadModal = () => {
  showUploadModal.value = true
}
const closeUploadModal = () => {
  showUploadModal.value = false
}

// 成员管理
const { showMembersModal, openMembersModal, closeMembersModal } = useMember()

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
const handleUploadComplete = () => {
  if (currentSpace.value) {
    fetchPictures(currentSpace.value.id)
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
            <button class="action-btn">设置</button>
            <button class="action-btn primary" @click="openUploadModal">上传图片</button>
          </div>
        </div>

        <div class="detail-content">
          <ImageGallery :space-id="currentSpace?.id" />
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
      @close="closeMembersModal"
    />

    <!-- 上传图片弹窗 -->
    <ImageUploader
      :show="showUploadModal"
      :space-id="currentSpace?.id ?? null"
      @close="closeUploadModal"
      @uploaded="handleUploadComplete"
    />
  </div>
</template>

<style src="./styles.css"></style>
