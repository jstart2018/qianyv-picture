/**
 * 成员管理组合式函数
 */

import { ref, computed, watch, type ComputedRef, type Ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { listSpaceUsers, kickOutMember, editUserRole } from '@/api/spaceController'

export interface Member {
  id: number
  userId: number
  username: string
  avatar?: string
  role: number
  joinTime: string
  roleUpdateTime: string
}

export function useMember(
  spaceIdRef: ComputedRef<number | null> | Ref<number | null>,
  showRef: ComputedRef<boolean> | Ref<boolean>,
) {
  const members = ref<Member[]>([])
  const loading = ref(false)
  const searchKeyword = ref('')

  // 分页相关
  const currentPage = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  // 角色筛选
  const filterRole = ref<number | null>(null)

  // 角色映射
  const roleMap: Record<number, { label: string; class: string }> = {
    0: { label: '创建者', class: 'creator' },
    1: { label: '管理员', class: 'admin' },
    2: { label: '编辑者', class: 'editor' },
    3: { label: '查看者', class: 'viewer' },
  }

  // 总页数
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  // 获取成员列表
  const fetchMembers = async (spaceId?: number) => {
    const spaceIdValue = spaceId ?? spaceIdRef.value
    if (!spaceIdValue) return

    loading.value = true
    try {
      const res = await listSpaceUsers({
        spaceId: spaceIdValue,
        current: currentPage.value,
        pageSize: pageSize.value,
        spaceRole: filterRole.value ?? undefined,
        username: searchKeyword.value.trim() || undefined,
      })

      if (res.data && res.data.code === 0 && res.data.data) {
        const records = res.data.data.records || []
        members.value = records.map((item: any) => ({
          id: item.id,
          userId: item.userId,
          username: item.userName || '未知用户',
          avatar: item.userAvatar,
          role: Number(item.spaceRole ?? 3),
          joinTime: item.createTime || '',
          roleUpdateTime: item.updateTime || '',
        }))
        total.value = res.data.data.total || 0
      }
    } catch (e) {
      console.error('获取成员列表失败', e)
    } finally {
      loading.value = false
    }
  }

  // 切换页码
  const changePage = (page: number) => {
    if (page < 1 || page > totalPages.value) return
    currentPage.value = page
    fetchMembers()
  }

  // 搜索（带防抖）
  let searchTimer: ReturnType<typeof setTimeout> | null = null
  const handleSearch = () => {
    if (searchTimer) {
      clearTimeout(searchTimer)
    }
    searchTimer = setTimeout(() => {
      currentPage.value = 1
      fetchMembers()
    }, 300)
  }

  // 监听空间ID变化，自动刷新成员列表
  watch(
    () => spaceIdRef.value,
    (newId, oldId) => {
      // 只有当空间ID真正变化且弹窗已打开时才刷新
      if (newId && newId !== oldId && showRef.value) {
        currentPage.value = 1
        fetchMembers()
      }
    },
  )

  // 监听弹窗显示状态
  watch(showRef, (show) => {
    if (show && spaceIdRef.value) {
      currentPage.value = 1
      fetchMembers()
    }
  })

  /**
   * 打开成员弹窗
   */
  const openMembersModal = () => {
    // 由外部控制
  }

  /**
   * 关闭成员弹窗
   */
  const closeMembersModal = () => {
    searchKeyword.value = ''
    filterRole.value = null
    currentPage.value = 1
  }

  const handleRemoveMember = async (
    member: Member,
    spaceId: number | null,
    currentUserRole: number | undefined,
  ) => {
    if (!spaceId) return

    // 不能移出创建者
    if (member.role === 0) {
      message.warning('不能移出空间创建者')
      return
    }

    // 权限检查：只有创建者和管理员能踢人
    if (currentUserRole !== 0 && currentUserRole !== 1) {
      message.warning('您没有权限移出成员')
      return
    }

    // 管理员只能由创建者踢出
    if (member.role === 1 && currentUserRole !== 0) {
      message.warning('只有创建者可以移出管理员')
      return
    }

    Modal.confirm({
      title: '确认移出成员',
      content: `确定要将 ${member.username} 移出空间吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await kickOutMember({
            spaceId: spaceId,
            userId: member.userId,
          })

          if (res.data && res.data.code === 0) {
            message.success('成员已移出')
            // 重新获取成员列表
            await fetchMembers(spaceId)
          } else {
            message.error(res.data?.message || '移出成员失败')
          }
        } catch (err) {
          console.error('移出成员失败:', err)
          message.error('操作失败，请稍后重试')
        }
      },
    })
  }

  /**
   * 修改成员角色
   */
  const handleEditRole = async (member: Member, spaceId: number | null, newRole: number) => {
    if (!spaceId) return

    // 不能修改创建者角色
    if (member.role === 0) {
      message.warning('不能修改创建者角色')
      return
    }

    // 不能设置为创建者
    if (newRole === 0) {
      message.warning('不能将成员设置为创建者')
      return
    }

    // 角色没有变化
    if (member.role === newRole) {
      return
    }

    try {
      const res = await editUserRole({
        spaceId: spaceId,
        userId: member.userId,
        spaceRole: newRole,
      })

      if (res.data && res.data.code === 0) {
        message.success('角色修改成功')
        // 重新获取成员列表
        await fetchMembers(spaceId)
      } else {
        message.error(res.data?.message || '修改角色失败')
      }
    } catch (err) {
      console.error('修改角色失败:', err)
      message.error('操作失败，请稍后重试')
    }
  }

  /**
   * 获取角色文本
   */
  const getRoleText = (role: number): string => {
    return roleMap[role]?.label || '成员'
  }

  /**
   * 获取角色样式类
   */
  const getRoleClass = (role: number): string => {
    return roleMap[role]?.class || 'viewer'
  }

  /**
   * 设置角色筛选
   */
  const setRoleFilter = (role: number | null) => {
    filterRole.value = role
    currentPage.value = 1
    fetchMembers()
  }

  return {
    members,
    loading,
    searchKeyword,
    filterRole,
    currentPage,
    pageSize,
    total,
    totalPages,
    closeMembersModal,
    handleRemoveMember,
    handleEditRole,
    getRoleText,
    getRoleClass,
    setRoleFilter,
    fetchMembers,
    changePage,
    handleSearch,
  }
}
