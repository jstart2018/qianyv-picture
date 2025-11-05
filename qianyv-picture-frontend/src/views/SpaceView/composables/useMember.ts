/**
 * 成员管理组合式函数
 */

import { ref, computed } from 'vue'

export interface Member {
  id: number
  username: string
  avatar: string
  role: string
  joinTime: string
}

export function useMember() {
  const showMembersModal = ref(false)
  const filterRole = ref<string>('all')
  const filterTimeRange = ref<string>('all')
  const searchKeyword = ref<string>('')

  // 模拟成员数据（实际项目中应该从API获取）
  const members = ref<Member[]>([
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

  /**
   * 打开成员弹窗
   */
  const openMembersModal = () => {
    showMembersModal.value = true
  }

  /**
   * 关闭成员弹窗
   */
  const closeMembersModal = () => {
    showMembersModal.value = false
  }

  /**
   * 踢出成员
   */
  const removeMember = (memberId: number, memberName: string, currentSpace: any) => {
    if (confirm(`确定要将 ${memberName} 移出空间吗？`)) {
      const index = members.value.findIndex((m) => m.id === memberId)
      if (index !== -1) {
        members.value.splice(index, 1)
        // 更新当前空间的成员数量
        if (currentSpace) {
          currentSpace.memberCount--
        }
        alert(`已成功将 ${memberName} 移出空间`)
      }
    }
  }

  /**
   * 获取角色文本
   */
  const getRoleText = (role: string) => {
    const roleMap: { [key: string]: string } = {
      admin: '管理员',
      editor: '编辑者',
      viewer: '查看者',
    }
    return roleMap[role] || role
  }

  /**
   * 获取角色样式类
   */
  const getRoleClass = (role: string) => {
    return role
  }

  return {
    showMembersModal,
    members,
    filteredMembers,
    filterRole,
    filterTimeRange,
    searchKeyword,
    openMembersModal,
    closeMembersModal,
    removeMember,
    getRoleText,
    getRoleClass,
  }
}
