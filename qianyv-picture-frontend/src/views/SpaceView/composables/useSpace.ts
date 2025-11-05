/**
 * 空间管理组合式函数
 * 管理空间列表、选择、编辑等核心逻辑
 */

import { ref, computed } from 'vue'
import { useSpaceStore } from '@/stores/space'
import { getUserSpaceList, addSpace, editSpace } from '@/api/spaceController'

export interface Space {
  id: number
  spaceName: string
  spaceLevel: number
  maxSize: number
  maxCount: number
  totalSize: number
  totalCount: number
  memberCount: number
  role: number
}

export function useSpace() {
  const spaceStore = useSpaceStore()

  // ========== 空间列表 ==========
  const mySpaces = ref<Space[]>([])
  const joinedManageableSpaces = ref<Space[]>([])
  const joinedEditableSpaces = ref<Space[]>([])
  const joinedViewableSpaces = ref<Space[]>([])

  // ========== 当前选中的空间 ==========
  const currentSpace = ref<Space | null>(null)

  // ========== 展开/收起状态 ==========
  const mySpacesExpanded = ref(true)
  const joinedSpacesExpanded = ref(false)
  const joinedManageableExpanded = ref(false)
  const joinedEditableExpanded = ref(false)
  const joinedViewableExpanded = ref(false)

  // ========== 空间名称编辑 ==========
  const editingSpaceId = ref<number | null>(null)
  const editingSpaceName = ref<string>('')

  // ========== 计算属性 ==========
  const joinedSpacesCount = computed(() => {
    return (
      joinedManageableSpaces.value.length +
      joinedEditableSpaces.value.length +
      joinedViewableSpaces.value.length
    )
  })

  // ========== 辅助函数 ==========
  /**
   * 转换后端数据为Space对象
   */
  const transformSpaceData = (data: any): Space => ({
    id: data.id,
    spaceName: data.spaceName,
    spaceLevel: data.spaceLevel,
    maxSize: Number(data.maxSize) || 0,
    maxCount: Number(data.maxCount) || 0,
    totalSize: Number(data.totalSize) || 0,
    totalCount: Number(data.totalCount) || 0,
    memberCount: Number(data.memberCount) || 0,
    role: data.role,
  })

  // ========== API 调用函数 ==========
  /**
   * 获取我的空间列表（role=0）
   */
  const fetchMySpaces = async () => {
    try {
      const res = await getUserSpaceList({ spaceRole: [0] })
      if (res.data && res.data.code === 0 && Array.isArray(res.data.data)) {
        mySpaces.value = res.data.data.map(transformSpaceData)

        // 默认选中第一个空间
        if (mySpaces.value.length > 0 && !currentSpace.value) {
          selectSpace(mySpaces.value[0])
        }
      } else {
        console.error('获取我的空间数据失败', res.data?.message || '未知错误')
      }
    } catch (e) {
      console.error('fetchMySpaces error', e)
    }
  }

  /**
   * 获取已加入-可管理的空间（role=1）
   */
  const fetchJoinedManageableSpaces = async () => {
    try {
      const res = await getUserSpaceList({ spaceRole: [1] })
      if (res.data && res.data.code === 0 && Array.isArray(res.data.data)) {
        joinedManageableSpaces.value = res.data.data.map(transformSpaceData)
      } else {
        console.error('获取可管理空间数据失败', res.data?.message || '未知错误')
      }
    } catch (e) {
      console.error('fetchJoinedManageableSpaces error', e)
    }
  }

  /**
   * 获取已加入-可编辑的空间（role=2）
   */
  const fetchJoinedEditableSpaces = async () => {
    try {
      const res = await getUserSpaceList({ spaceRole: [2] })
      if (res.data && res.data.code === 0 && Array.isArray(res.data.data)) {
        joinedEditableSpaces.value = res.data.data.map(transformSpaceData)
      } else {
        console.error('获取可编辑空间数据失败', res.data?.message || '未知错误')
      }
    } catch (e) {
      console.error('fetchJoinedEditableSpaces error', e)
    }
  }

  /**
   * 获取已加入-仅浏览的空间（role=3）
   */
  const fetchJoinedViewableSpaces = async () => {
    try {
      const res = await getUserSpaceList({ spaceRole: [3] })
      if (res.data && res.data.code === 0 && Array.isArray(res.data.data)) {
        joinedViewableSpaces.value = res.data.data.map(transformSpaceData)
      } else {
        console.error('获取仅浏览空间数据失败', res.data?.message || '未知错误')
      }
    } catch (e) {
      console.error('fetchJoinedViewableSpaces error', e)
    }
  }

  /**
   * 加载所有空间列表
   */
  const loadAllSpaces = async () => {
    await Promise.all([
      fetchMySpaces(),
      fetchJoinedManageableSpaces(),
      fetchJoinedEditableSpaces(),
      fetchJoinedViewableSpaces(),
    ])
  }

  /**
   * 选择空间
   */
  const selectSpace = (space: Space) => {
    currentSpace.value = space
    spaceStore.switchSpace(space.id)
  }

  /**
   * 创建新空间
   */
  const createNewSpace = async () => {
    try {
      const res = await addSpace()
      if (res.data && res.data.code === 0) {
        alert('空间创建成功!')
        await fetchMySpaces()
        return true
      } else {
        alert('空间创建失败: ' + (res.data?.message || '未知错误'))
        return false
      }
    } catch (e) {
      console.error('createNewSpace error', e)
      alert('空间创建失败，请稍后重试')
      return false
    }
  }

  /**
   * 开始编辑空间名称
   */
  const startEditSpaceName = (space: Space) => {
    editingSpaceId.value = space.id
    editingSpaceName.value = space.spaceName
  }

  /**
   * 取消编辑空间名称
   */
  const cancelEditSpaceName = () => {
    editingSpaceId.value = null
    editingSpaceName.value = ''
  }

  /**
   * 保存空间名称
   */
  const saveSpaceName = async (spaceId: number) => {
    // 如果名称为空或未修改，则取消编辑
    if (!editingSpaceName.value.trim()) {
      cancelEditSpaceName()
      return
    }

    // 查找原空间数据
    const space = mySpaces.value.find((s) => s.id === spaceId)
    if (space && editingSpaceName.value.trim() === space.spaceName) {
      // 名称未修改，直接取消编辑
      cancelEditSpaceName()
      return
    }

    try {
      const res = await editSpace({
        spaceId: spaceId,
        name: editingSpaceName.value.trim(),
      })

      if (res.data && res.data.code === 0) {
        // 更新本地数据
        if (space) {
          space.spaceName = editingSpaceName.value.trim()
        }

        // 如果是当前选中的空间，也要更新
        if (currentSpace.value && currentSpace.value.id === spaceId) {
          currentSpace.value.spaceName = editingSpaceName.value.trim()
        }

        // 重置编辑状态
        cancelEditSpaceName()

        alert('空间名称修改成功')
        return true
      } else {
        alert('空间名称修改失败: ' + (res.data?.message || '未知错误'))
        cancelEditSpaceName()
        return false
      }
    } catch (e) {
      console.error('saveSpaceName error', e)
      alert('保存失败，请稍后重试')
      cancelEditSpaceName()
      return false
    }
  }

  // ========== 展开/收起控制 ==========
  const toggleMySpaces = () => {
    mySpacesExpanded.value = !mySpacesExpanded.value
  }

  const toggleJoinedSpaces = () => {
    joinedSpacesExpanded.value = !joinedSpacesExpanded.value
  }

  const toggleJoinedManageable = () => {
    joinedManageableExpanded.value = !joinedManageableExpanded.value
  }

  const toggleJoinedEditable = () => {
    joinedEditableExpanded.value = !joinedEditableExpanded.value
  }

  const toggleJoinedViewable = () => {
    joinedViewableExpanded.value = !joinedViewableExpanded.value
  }

  return {
    // 空间列表
    mySpaces,
    joinedManageableSpaces,
    joinedEditableSpaces,
    joinedViewableSpaces,
    joinedSpacesCount,

    // 当前空间
    currentSpace,

    // 展开状态
    mySpacesExpanded,
    joinedSpacesExpanded,
    joinedManageableExpanded,
    joinedEditableExpanded,
    joinedViewableExpanded,

    // 编辑状态
    editingSpaceId,
    editingSpaceName,

    // 方法
    loadAllSpaces,
    selectSpace,
    createNewSpace,
    startEditSpaceName,
    cancelEditSpaceName,
    saveSpaceName,
    toggleMySpaces,
    toggleJoinedSpaces,
    toggleJoinedManageable,
    toggleJoinedEditable,
    toggleJoinedViewable,
  }
}
