<script setup lang="ts">
import { ref, onMounted, computed, nextTick } from 'vue'
import { useSpaceStore } from '@/stores/space'
import { useUserStore } from '@/stores/user'
import { getUserSpaceList, addSpace, editSpace } from '@/api/spaceController'
import { upload, editByBatch, list } from '@/api/pictureController'

const spaceStore = useSpaceStore()
const userStore = useUserStore()

// 展开状态
const mySpacesExpanded = ref(true) // 默认展开
const joinedSpacesExpanded = ref(false) // 默认收起

// 我的空间（从后端获取，role=0 创建者）
const mySpaces = ref<any[]>([])

// 已加入的空间 - 三个子分类
const joinedManageableSpaces = ref<any[]>([]) // role=1 可管理
const joinedEditableSpaces = ref<any[]>([]) // role=2 可编辑
const joinedViewableSpaces = ref<any[]>([]) // role=3 仅浏览

// 子目录展开状态
const joinedManageableExpanded = ref(false)
const joinedEditableExpanded = ref(false)
const joinedViewableExpanded = ref(false)

// 当前选中的空间
const currentSpace = ref<any>(null)

// 显示成员列表弹窗
const showMembersModal = ref(false)

// 空间名称编辑状态
const editingSpaceId = ref<number | null>(null)
const editingSpaceName = ref<string>('')

// 图片上传相关状态
const showUploadModal = ref(false)
const uploadFiles = ref<File[]>([])
// uploadPreviews 保存每个文件的本地预览、后端返回的 id、标签以及上传状态
const uploadPreviews = ref<
  {
    file: File
    url: string
    tags: string
    id?: number
    status?: 'pending' | 'uploading' | 'uploaded' | 'error'
    errorMsg?: string
  }[]
>([])
const isUploading = ref(false)

// 图片列表相关状态
const pictures = ref<any[]>([])
const pictureTotal = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const pictureLoading = ref(false)

// 图片搜索关键词
const pictureSearchText = ref('')

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

const toggleJoinedManageable = () => {
  joinedManageableExpanded.value = !joinedManageableExpanded.value
}

const toggleJoinedEditable = () => {
  joinedEditableExpanded.value = !joinedEditableExpanded.value
}

const toggleJoinedViewable = () => {
  joinedViewableExpanded.value = !joinedViewableExpanded.value
}

// 打开成员列表弹窗
const openMembersModal = () => {
  showMembersModal.value = true
}

// 关闭成员列表弹窗
const closeMembersModal = () => {
  showMembersModal.value = false
}

// 创建新空间
const createNewSpace = async () => {
  try {
    const res = await addSpace()
    if (res.data && res.data.code === 0) {
      alert('空间创建成功!')
      // 重新加载我的空间列表
      await fetchMySpaces()
    } else {
      alert('空间创建失败: ' + (res.data?.message || '未知错误'))
    }
  } catch (e) {
    console.error('createNewSpace error', e)
    alert('空间创建失败，请稍后重试')
  }
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

// 开始编辑空间名称
const startEditSpaceName = (space: any, event: Event) => {
  event.stopPropagation() // 阻止选择空间
  editingSpaceId.value = space.id
  editingSpaceName.value = space.spaceName

  // 等待 DOM 更新后聚焦输入框
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

// 取消编辑空间名称
const cancelEditSpaceName = () => {
  editingSpaceId.value = null
  editingSpaceName.value = ''
}

// 保存空间名称
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
    } else {
      alert('空间名称修改失败: ' + (res.data?.message || '未知错误'))
      cancelEditSpaceName()
    }
  } catch (e) {
    console.error('saveSpaceName error', e)
    alert('空间名称修改失败，请稍后重试')
    cancelEditSpaceName()
  }
}

// 处理输入框键盘事件
const handleSpaceNameKeydown = (event: KeyboardEvent, spaceId: number) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    saveSpaceName(spaceId)
  } else if (event.key === 'Escape') {
    event.preventDefault()
    cancelEditSpaceName()
  }
}

// 成员角色显示文本（之前用于成员列表）
const getRoleText = (role: string) => {
  const roleMap: { [key: string]: string } = {
    admin: '管理员',
    editor: '编辑者',
    viewer: '查看者',
  }
  return roleMap[role] || role
}

// 恢复用于成员列表的类映射（旧代码依赖）
const getRoleClass = (role: string) => {
  return role
}

// 空间中返回的整数角色（0: CREATER,1:ADMIN,2:EDITOR,3:VIEWER）映射为中文和 class
const spaceRoleMapLabel: { [k: number]: string } = {
  0: 'CREATOR',
  1: 'ADMIN',
  2: 'EDITOR',
  3: 'VIEWER',
}

const spaceRoleMapClass: { [k: number]: string } = {
  0: 'owner',
  1: 'admin',
  2: 'editor',
  3: 'viewer',
}

const getSpaceRoleText = (role: number) => {
  return spaceRoleMapLabel[role] || String(role)
}

const getSpaceRoleClass = (role: number) => {
  return spaceRoleMapClass[role] || 'viewer'
}

// 空间等级字典映射（0: 普通版, 1: 专业版, 2: 旗舰版, 3: 企业版）
const spaceLevelMapLabel: { [k: number]: string } = {
  0: 'Lv0-免费版',
  1: 'Lv1-专业版',
  2: 'Lv2-旗舰版',
  3: 'Lv3-企业版',
}

const getSpaceLevelText = (level: number) => {
  return spaceLevelMapLabel[level] || `Lv${level}`
}

// 获取用户头像文本
const getUserAvatarText = (username: string) => {
  return username ? username.charAt(0).toUpperCase() : '?'
}

// 格式化文件大小
const formatSize = (bytes: number) => {
  if (bytes >= 1024 * 1024 * 1024 * 1024) {
    return (bytes / (1024 * 1024 * 1024 * 1024)).toFixed(2) + ' TB'
  } else if (bytes >= 1024 * 1024 * 1024) {
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
  } else if (bytes >= 1024 * 1024) {
    return (bytes / (1024 * 1024)).toFixed(2) + ' MB'
  } else {
    return (bytes / 1024).toFixed(2) + ' KB'
  }
}

// 选择空间
const selectSpace = (space: any) => {
  currentSpace.value = space
  spaceStore.switchSpace(space.id)

  // 重置分页并加载该空间的图片列表
  currentPage.value = 1
  pictureSearchText.value = ''
  fetchPictures()
}

// 计算使用百分比
const getUsagePercent = (used: number, total: number) => {
  return Math.round((used / total) * 100)
}

// 初始化
onMounted(() => {
  // 先从后端加载空间列表
  fetchMySpaces()
  fetchJoinedManageableSpaces()
  fetchJoinedEditableSpaces()
  fetchJoinedViewableSpaces()
})

// 从后端获取“我的空间”
const fetchMySpaces = async () => {
  try {
    const res = await getUserSpaceList({ spaceRole: [0] })
    if (res.data && res.data.code === 0 && Array.isArray(res.data.data)) {
      mySpaces.value = res.data.data.map((s) => ({
        id: s.id,
        spaceName: s.spaceName,
        spaceLevel: s.spaceLevel,
        maxSize: Number(s.maxSize) || 0,
        maxCount: Number(s.maxCount) || 0,
        totalSize: Number(s.totalSize) || 0,
        totalCount: Number(s.totalCount) || 0,
        memberCount: Number(s.memberCount) || 0,
        role: s.role,
      }))

      if (mySpaces.value.length > 0) {
        selectSpace(mySpaces.value[0])
      }
    } else {
      console.error('获取我的空间数据失败', res.data?.message || '未知错误')
    }
  } catch (e) {
    console.error('fetchMySpaces error', e)
  }
}

// 从后端获取"已加入-可管理"的空间 (role=1)
const fetchJoinedManageableSpaces = async () => {
  try {
    const res = await getUserSpaceList({ spaceRole: [1] })
    if (res.data && res.data.code === 0 && Array.isArray(res.data.data)) {
      joinedManageableSpaces.value = res.data.data.map((s) => ({
        id: s.id,
        spaceName: s.spaceName,
        spaceLevel: s.spaceLevel,
        maxSize: Number(s.maxSize) || 0,
        maxCount: Number(s.maxCount) || 0,
        totalSize: Number(s.totalSize) || 0,
        totalCount: Number(s.totalCount) || 0,
        memberCount: Number(s.memberCount) || 0,
        role: s.role,
      }))
    } else {
      console.error('获取可管理空间数据失败', res.data?.message || '未知错误')
    }
  } catch (e) {
    console.error('fetchJoinedManageableSpaces error', e)
  }
}

// 从后端获取"已加入-可编辑"的空间 (role=2)
const fetchJoinedEditableSpaces = async () => {
  try {
    const res = await getUserSpaceList({ spaceRole: [2] })
    if (res.data && res.data.code === 0 && Array.isArray(res.data.data)) {
      joinedEditableSpaces.value = res.data.data.map((s) => ({
        id: s.id,
        spaceName: s.spaceName,
        spaceLevel: s.spaceLevel,
        maxSize: Number(s.maxSize) || 0,
        maxCount: Number(s.maxCount) || 0,
        totalSize: Number(s.totalSize) || 0,
        totalCount: Number(s.totalCount) || 0,
        memberCount: Number(s.memberCount) || 0,
        role: s.role,
      }))
    } else {
      console.error('获取可编辑空间数据失败', res.data?.message || '未知错误')
    }
  } catch (e) {
    console.error('fetchJoinedEditableSpaces error', e)
  }
}

// 从后端获取"已加入-仅浏览"的空间 (role=3)
const fetchJoinedViewableSpaces = async () => {
  try {
    const res = await getUserSpaceList({ spaceRole: [3] })
    if (res.data && res.data.code === 0 && Array.isArray(res.data.data)) {
      joinedViewableSpaces.value = res.data.data.map((s) => ({
        id: s.id,
        spaceName: s.spaceName,
        spaceLevel: s.spaceLevel,
        maxSize: Number(s.maxSize) || 0,
        maxCount: Number(s.maxCount) || 0,
        totalSize: Number(s.totalSize) || 0,
        totalCount: Number(s.totalCount) || 0,
        memberCount: Number(s.memberCount) || 0,
        role: s.role,
      }))
    } else {
      console.error('获取仅浏览空间数据失败', res.data?.message || '未知错误')
    }
  } catch (e) {
    console.error('fetchJoinedViewableSpaces error', e)
  }
}

// 计算已加入空间总数
const joinedSpacesCount = computed(() => {
  return (
    joinedManageableSpaces.value.length +
    joinedEditableSpaces.value.length +
    joinedViewableSpaces.value.length
  )
})

// 图片与目录（本地 mock，用于界面原型）
const directories = ref([
  { id: 0, name: '全部' },
  { id: 1, name: '策划稿' },
  { id: 2, name: '素材' },
  { id: 3, name: '临时' },
])
const selectedDirectoryId = ref<number>(0)
const imageFilterTime = ref<string>('all') // all, 1d, 1w, 1m, 3m
const imageSearch = ref<string>('')

const images = ref([
  { id: 1, url: '', title: '海报初稿', tags: ['海报'], createdAt: '2025-10-20', directoryId: 1 },
  { id: 2, url: '', title: '轮播素材', tags: ['banner'], createdAt: '2025-09-12', directoryId: 2 },
  { id: 3, url: '', title: '草稿图', tags: ['草稿'], createdAt: '2025-11-01', directoryId: 3 },
  { id: 4, url: '', title: '素材A', tags: ['素材'], createdAt: '2025-08-01', directoryId: 2 },
])

// 选择目录
const selectDirectory = (id: number) => {
  selectedDirectoryId.value = id
}

// 新建目录（原型使用 prompt）
const createDirectory = () => {
  const name = prompt('请输入目录名称：')
  if (name && name.trim()) {
    const id = Date.now()
    directories.value.push({ id, name: name.trim() })
    selectedDirectoryId.value = id
  }
}

// 图片过滤器
const filteredImages = computed(() => {
  const now = new Date()
  return images.value.filter((img) => {
    // 目录过滤
    if (
      selectedDirectoryId.value &&
      selectedDirectoryId.value !== 0 &&
      img.directoryId !== selectedDirectoryId.value
    ) {
      return false
    }

    // 时间范围过滤
    if (imageFilterTime.value !== 'all') {
      const created = new Date(img.createdAt)
      const diffDays = Math.floor((now.getTime() - created.getTime()) / (1000 * 60 * 60 * 24))
      if (imageFilterTime.value === '1d' && diffDays > 1) return false
      if (imageFilterTime.value === '1w' && diffDays > 7) return false
      if (imageFilterTime.value === '1m' && diffDays > 30) return false
      if (imageFilterTime.value === '3m' && diffDays > 90) return false
    }

    // 搜索关键词（标题或标签）
    if (imageSearch.value) {
      const key = imageSearch.value.toLowerCase()
      const inTitle = img.title.toLowerCase().includes(key)
      const inTags = img.tags.some((t: string) => t.toLowerCase().includes(key))
      if (!inTitle && !inTags) return false
    }

    return true
  })
})

// ==================== 图片上传功能 ====================

// 打开上传弹窗
const openUploadModal = () => {
  showUploadModal.value = true
  uploadFiles.value = []
  uploadPreviews.value = []
}

// 关闭上传弹窗
const closeUploadModal = () => {
  showUploadModal.value = false
  uploadFiles.value = []
  uploadPreviews.value = []
}

// 选择文件并立即上传到后端，后端返回 id 与 thumbUrl 用于预览
const handleFileSelect = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  if (!currentSpace.value) {
    alert('请先选择一个空间')
    target.value = ''
    return
  }

  // 先生成本地预览并标记为 uploading，同时发起上传请求
  for (const file of Array.from(files)) {
    if (!file.type.startsWith('image/')) {
      alert(`文件 ${file.name} 不是图片格式`)
      continue
    }

    // 本地预览（DataURL）用于快速显示
    const reader = new FileReader()
    const previewIndex = uploadPreviews.value.length
    uploadPreviews.value.push({ file, url: '', tags: '', status: 'uploading' })

    // 保存对当前 preview 对象的引用，避免后续通过索引写入时发生未定义
    const setPreview = (updater: (preview: any) => void) => {
      const preview = uploadPreviews.value[previewIndex]
      if (!preview) return
      updater(preview)
    }

    reader.onload = async (e) => {
      // 先显示本地 dataURL
      setPreview((p) => {
        p.url = (e.target && (e.target as FileReader).result) as string
      })

      // 构造表单并上传到 /picture/uploadByFile，携带 spaceId
      try {
        const formData = new FormData()
        formData.append('file', file)

        const uploadRes = await upload({ spaceId: currentSpace.value.id }, formData as any)

        if (uploadRes && uploadRes.data?.code === 0 && uploadRes.data.data) {
          // 使用后端返回的缩略图 URL 替换预览地址，并保存 id
          setPreview((p) => {
            p.id = uploadRes.data!.data!.id
            if (uploadRes.data!.data!.thumbUrl) {
              p.url = uploadRes.data!.data!.thumbUrl!
            }
            p.status = 'uploaded'
          })
        } else {
          setPreview((p) => {
            p.status = 'error'
            p.errorMsg = uploadRes?.data?.message || '上传失败'
          })
          console.error('upload failed', uploadRes?.data)
        }
      } catch (err: any) {
        setPreview((p) => {
          p.status = 'error'
          p.errorMsg = err?.message || '上传异常'
        })
        console.error('handleFileSelect upload error', err)
      }
    }

    reader.readAsDataURL(file)
  }

  // 重置 input
  target.value = ''
}

// 移除预览图片
const removePreview = (index: number) => {
  uploadPreviews.value.splice(index, 1)
}

// 点击“上传 N 张图片”按钮：将已经上传成功并有 id 的图片组成 PictureEditDTO 数组，调用 editByBatch
const handleBatchUpload = async () => {
  if (!currentSpace.value) {
    alert('请先选择一个空间')
    return
  }

  // 只对已经上传成功（uploaded）的图片进行批量编辑；也可包含未填写标签的项
  const toEdit = uploadPreviews.value
    .filter((p) => p.id && p.status === 'uploaded')
    .map((p) => ({
      id: p.id as number,
      tags: p.tags?.trim() || '',
      spaceId: currentSpace.value.id,
    }))

  if (toEdit.length === 0) {
    alert('没有可提交的图片（请确保文件已上传成功）')
    return
  }

  isUploading.value = true

  try {
    const res = await editByBatch(toEdit)
    if (res.data?.code === 0) {
      alert('图片信息提交成功')
      // 关闭弹窗并刷新列表
      closeUploadModal()
      await fetchPictures()
    } else {
      alert('图片信息提交失败: ' + (res.data?.message || '未知错误'))
    }
  } catch (err: any) {
    console.error('handleBatchUpload error', err)
    alert('图片信息提交失败: ' + (err.message || '未知错误'))
  } finally {
    isUploading.value = false
  }
}

// ==================== 图片列表功能 ====================

// 获取图片列表
const fetchPictures = async () => {
  if (!currentSpace.value) return

  pictureLoading.value = true

  try {
    const res = await list({
      spaceId: currentSpace.value.id,
      current: currentPage.value,
      pageSize: pageSize.value,
      searchText: pictureSearchText.value || undefined,
    })

    if (res.data?.code === 0 && res.data.data) {
      pictures.value = res.data.data.records || []
      pictureTotal.value = res.data.data.total || 0
    } else {
      console.error('获取图片列表失败', res.data?.message)
      pictures.value = []
      pictureTotal.value = 0
    }
  } catch (e) {
    console.error('fetchPictures error', e)
    pictures.value = []
    pictureTotal.value = 0
  } finally {
    pictureLoading.value = false
  }
}

// 搜索图片
const handlePictureSearch = () => {
  currentPage.value = 1
  fetchPictures()
}

// 分页改变
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchPictures()
}

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(pictureTotal.value / pageSize.value)
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
            <button class="create-btn" @click.stop="createNewSpace">+ 创建</button>
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
                  <span
                    v-if="editingSpaceId !== space.id"
                    class="space-name"
                    @click="startEditSpaceName(space, $event)"
                    >{{ space.spaceName }}</span
                  >
                  <input
                    v-else
                    class="space-name-input"
                    v-model="editingSpaceName"
                    @keydown="handleSpaceNameKeydown($event, space.id)"
                    @blur="saveSpaceName(space.id)"
                  />
                  <span class="space-badge" :class="getSpaceRoleClass(space.role)">{{
                    getSpaceRoleText(space.role)
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

        <!-- 已加入的空间 -->
        <div class="space-section">
          <div class="section-header" @click="toggleJoinedSpaces">
            <span class="section-icon">👥</span>
            <span class="section-title">已加入</span>
            <span class="space-count">({{ joinedSpacesCount }})</span>
            <span class="toggle-arrow" :class="{ expanded: joinedSpacesExpanded }">▼</span>
          </div>
          <transition name="expand">
            <div v-show="joinedSpacesExpanded" class="joined-subcategories">
              <!-- 子分类1: 可管理 -->
              <div class="subcategory">
                <div class="subcategory-header" @click="toggleJoinedManageable">
                  <span class="subcategory-icon">⚙️</span>
                  <span class="subcategory-title">可管理</span>
                  <span class="space-count">({{ joinedManageableSpaces.length }})</span>
                  <span class="toggle-arrow" :class="{ expanded: joinedManageableExpanded }"
                    >▼</span
                  >
                </div>
                <transition name="expand">
                  <div v-show="joinedManageableExpanded" class="space-items">
                    <div
                      v-for="space in joinedManageableSpaces"
                      :key="space.id"
                      class="space-item"
                      :class="{ active: currentSpace?.id === space.id }"
                      @click="selectSpace(space)"
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
                            >{{ formatSize(space.totalSize) }} /
                            {{ formatSize(space.maxSize) }}</span
                          >
                        </div>
                        <div class="stat-row">
                          <span class="stat-label">图片:</span>
                          <span class="stat-value"
                            >{{ space.totalCount }} / {{ space.maxCount }}</span
                          >
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
                <div class="subcategory-header" @click="toggleJoinedEditable">
                  <span class="subcategory-icon">✏️</span>
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
                      @click="selectSpace(space)"
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
                            >{{ formatSize(space.totalSize) }} /
                            {{ formatSize(space.maxSize) }}</span
                          >
                        </div>
                        <div class="stat-row">
                          <span class="stat-label">图片:</span>
                          <span class="stat-value"
                            >{{ space.totalCount }} / {{ space.maxCount }}</span
                          >
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
                <div class="subcategory-header" @click="toggleJoinedViewable">
                  <span class="subcategory-icon">👁️</span>
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
                      @click="selectSpace(space)"
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
                            >{{ formatSize(space.totalSize) }} /
                            {{ formatSize(space.maxSize) }}</span
                          >
                        </div>
                        <div class="stat-row">
                          <span class="stat-label">图片:</span>
                          <span class="stat-value"
                            >{{ space.totalCount }} / {{ space.maxCount }}</span
                          >
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
            <span class="info-value">{{ getSpaceLevelText(currentSpace.spaceLevel) }}</span>
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
            <button class="action-btn">设置</button>
            <button class="action-btn primary" @click="openUploadModal">上传图片</button>
          </div>
        </div>

        <div class="detail-content">
          <!-- 图片搜索工具栏 -->
          <div class="image-toolbar">
            <div class="filters">
              <div class="filter-item search-box image-search">
                <span class="filter-icon">🔍</span>
                <input
                  v-model="pictureSearchText"
                  type="text"
                  placeholder="搜索图片标签或描述..."
                  class="search-input"
                  @keyup.enter="handlePictureSearch"
                />
                <button class="search-btn" @click="handlePictureSearch">搜索</button>
              </div>
            </div>
          </div>

          <!-- 图片展示区域（瀑布流布局） -->
          <div v-if="pictureLoading" class="loading-state">
            <p>加载中...</p>
          </div>

          <div v-else-if="pictures.length === 0" class="empty-pictures">
            <p>暂无图片，点击右上角"上传图片"按钮开始上传吧！</p>
          </div>

          <div v-else class="pictures-waterfall">
            <div v-for="picture in pictures" :key="picture.id" class="picture-card">
              <img :src="picture.thumbUrl" :alt="picture.introduction || '图片'" />
              <div class="picture-info">
                <p v-if="picture.tags" class="picture-tags">{{ picture.tags }}</p>
                <p v-if="picture.introduction" class="picture-desc">{{ picture.introduction }}</p>
                <div class="picture-stats">
                  <span>❤️ {{ picture.collectCount || 0 }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="pictures.length > 0" class="pagination">
            <button
              class="page-btn"
              :disabled="currentPage === 1"
              @click="handlePageChange(currentPage - 1)"
            >
              上一页
            </button>
            <span class="page-info">
              第 {{ currentPage }} / {{ totalPages }} 页，共 {{ pictureTotal }} 张图片
            </span>
            <button
              class="page-btn"
              :disabled="currentPage >= totalPages"
              @click="handlePageChange(currentPage + 1)"
            >
              下一页
            </button>
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

    <!-- 上传图片弹窗 -->
    <transition name="modal-fade">
      <div v-if="showUploadModal" class="modal-overlay" @click="closeUploadModal">
        <div class="upload-modal-container" @click.stop>
          <div class="modal-header">
            <h3>📤 上传图片</h3>
            <button class="modal-close-btn" @click="closeUploadModal">✕</button>
          </div>

          <div class="upload-modal-body">
            <!-- 文件选择区域 -->
            <div class="file-select-area">
              <input
                type="file"
                id="fileInput"
                accept="image/*"
                multiple
                @change="handleFileSelect"
                style="display: none"
              />
              <label for="fileInput" class="file-select-label">
                <span class="upload-icon">📁</span>
                <span>点击选择图片（支持多选）</span>
              </label>
            </div>

            <!-- 图片预览区域 -->
            <div v-if="uploadPreviews.length > 0" class="upload-previews">
              <div
                v-for="(preview, index) in uploadPreviews"
                :key="index"
                class="upload-preview-item"
              >
                <img :src="preview.url" :alt="preview.file.name" />
                <button class="remove-preview-btn" @click="removePreview(index)">✕</button>
                <div class="preview-info">
                  <p class="file-name">{{ preview.file.name }}</p>
                  <input
                    v-model="preview.tags"
                    type="text"
                    placeholder="添加标签（可选）"
                    class="tag-input"
                  />
                </div>
              </div>
            </div>

            <!-- 上传按钮 -->
            <div class="upload-actions">
              <button class="cancel-btn" @click="closeUploadModal" :disabled="isUploading">
                取消
              </button>
              <button
                class="upload-btn"
                @click="handleBatchUpload"
                :disabled="uploadPreviews.length === 0 || isUploading"
              >
                {{ isUploading ? '上传中...' : `上传 ${uploadPreviews.length} 张图片` }}
              </button>
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

/* 已加入空间的子分类容器 */
.joined-subcategories {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 8px 0;
}

/* 子分类样式 */
.subcategory {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 8px;
  border: 1px solid rgba(138, 180, 248, 0.15);
}

.subcategory-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.subcategory-header:hover {
  background: rgba(138, 180, 248, 0.12);
}

.subcategory-icon {
  font-size: 15px;
}

.subcategory-title {
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
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
  cursor: pointer;
  transition: color 0.2s;
}

.space-name:hover {
  color: #4299e1;
}

.space-name-input {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  border: none;
  border-bottom: 2px solid #000;
  border-radius: 0;
  padding: 2px 4px;
  width: 100%;
  background: transparent;
  outline: none;
}

.space-badge {
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
}

.space-badge.owner {
  background: linear-gradient(135deg, #f78282 0%, #f73434 100%);
  color: white;
}

.space-badge.admin {
  background: linear-gradient(135deg, #8acbdb 0%, #1ebee6 100%);
  color: white;
}

.space-badge.editor {
  background: linear-gradient(135deg, #90dd96 0%, #52c85c 100%);
  color: white;
}

.space-badge.viewer {
  background: rgba(21, 91, 182, 0.3);
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

/* 图片工具栏样式 */
.image-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 12px 0 18px;
  border-bottom: 1px dashed rgba(138, 180, 248, 0.12);
}

.dir-list {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.dir-chip {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(138, 180, 248, 0.25);
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
}

.dir-chip.active {
  background: linear-gradient(135deg, #89cbdc 0%, #1aa0c1 100%);
  color: white;
  box-shadow: 0 6px 20px rgba(26, 160, 193, 0.12);
}

.create-dir-btn {
  padding: 6px 10px;
  background: rgba(138, 180, 248, 0.12);
  border: 1px dashed rgba(138, 180, 248, 0.3);
  border-radius: 10px;
  color: #1aa0c1;
  cursor: pointer;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.image-time-select {
  min-width: 140px;
}

.image-search .search-input {
  min-width: 220px;
  padding-left: 36px;
}

.image-search .filter-icon.line-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 18px;
  background: rgba(59, 130, 246, 0.6);
  border-radius: 1px;
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

/* ========== 图片展示区域 ========== */
.loading-state,
.empty-pictures {
  text-align: center;
  padding: 60px 20px;
  color: #718096;
  font-size: 14px;
}

.pictures-waterfall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

.picture-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.picture-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(138, 180, 248, 0.3);
}

.picture-card img {
  width: 100%;
  height: auto;
  display: block;
  object-fit: cover;
}

.picture-info {
  padding: 12px;
}

.picture-tags {
  font-size: 13px;
  color: #4a5568;
  margin: 0 0 6px 0;
  font-weight: 500;
}

.picture-desc {
  font-size: 12px;
  color: #718096;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.picture-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #a0aec0;
}

/* ========== 分页 ========== */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 30px;
  padding: 20px 0;
}

.page-btn {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #4a5568;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: #8ab4f8;
  color: white;
  border-color: #8ab4f8;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #4a5568;
}

.search-btn {
  padding: 6px 16px;
  background: #8ab4f8;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
  margin-left: 8px;
}

.search-btn:hover {
  background: #6b9de8;
}

/* ========== 上传图片弹窗 ========== */
.upload-modal-container {
  background: white;
  border-radius: 16px;
  width: 90%;
  max-width: 800px;
  max-height: 85vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.upload-modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.file-select-area {
  margin-bottom: 24px;
}

.file-select-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  border: 2px dashed #cbd5e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f7fafc;
}

.file-select-label:hover {
  border-color: #8ab4f8;
  background: #eef6ff;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.file-select-label span:last-child {
  font-size: 14px;
  color: #4a5568;
}

.upload-previews {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.upload-preview-item {
  position: relative;
  background: #f7fafc;
  border-radius: 12px;
  overflow: hidden;
  padding: 8px;
}

.upload-preview-item img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 8px;
}

.remove-preview-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.remove-preview-btn:hover {
  background: rgba(220, 38, 38, 0.9);
}

.preview-info {
  padding: 0 4px;
}

.file-name {
  font-size: 12px;
  color: #4a5568;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-input {
  width: 100%;
  padding: 8px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.3s ease;
}

.tag-input:focus {
  outline: none;
  border-color: #8ab4f8;
  box-shadow: 0 0 0 3px rgba(138, 180, 248, 0.1);
}

.upload-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.cancel-btn,
.upload-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.cancel-btn {
  background: #f7fafc;
  color: #4a5568;
}

.cancel-btn:hover:not(:disabled) {
  background: #edf2f7;
}

.upload-btn {
  background: #8ab4f8;
  color: white;
}

.upload-btn:hover:not(:disabled) {
  background: #6b9de8;
}

.cancel-btn:disabled,
.upload-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
