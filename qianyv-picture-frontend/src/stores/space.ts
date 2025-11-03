import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSpaceStore = defineStore('space', () => {
  // 当前选中的空间ID
  const currentSpaceId = ref<number | null>(null)

  // 当前空间信息
  const currentSpace = ref<API.SpaceVO | null>(null)

  // 我创建的空间列表
  const ownedSpaces = ref<API.SpaceVO[]>([])

  // 我加入的空间列表
  const joinedSpaces = ref<API.SpaceVO[]>([])

  // 空间成员列表
  const spaceMembers = ref<API.SpaceUserVO[]>([])

  // 空间图片列表
  const spacePictures = ref<any[]>([])

  // 当前用户在空间的角色
  const userRole = ref<'admin' | 'editor' | 'viewer' | null>(null)

  // 权限信息
  const permissions = ref({
    canUpload: false,
    canEdit: false,
    canDelete: false,
    canManageMembers: false,
  })

  // 加载状态
  const loading = ref(false)

  // 切换空间
  const switchSpace = async (spaceId: number) => {
    currentSpaceId.value = spaceId
    // TODO: 后续接入API获取空间详情
  }

  return {
    currentSpaceId,
    currentSpace,
    ownedSpaces,
    joinedSpaces,
    spaceMembers,
    spacePictures,
    userRole,
    permissions,
    loading,
    switchSpace,
  }
})
