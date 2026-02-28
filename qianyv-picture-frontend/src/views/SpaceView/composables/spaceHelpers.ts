/**
 * 空间相关辅助函数
 */

import { formatFileSize } from '@/utils'

// 空间角色映射
export const SPACE_ROLE_MAP = {
  LABEL: {
    0: 'CREATOR',
    1: 'ADMIN',
    2: 'EDITOR',
    3: 'VIEWER',
  },
  CLASS: {
    0: 'owner',
    1: 'admin',
    2: 'editor',
    3: 'viewer',
  },
} as const

// 空间等级映射
export const SPACE_LEVEL_MAP = {
  0: 'Lv0-免费版',
  1: 'Lv1-普通版',
  2: 'Lv2-专业版',
  3: 'Lv3-旗舰版',
} as const

/**
 * 获取空间角色文本
 */
export function getSpaceRoleText(role: number): string {
  return SPACE_ROLE_MAP.LABEL[role as keyof typeof SPACE_ROLE_MAP.LABEL] || String(role)
}

/**
 * 获取空间角色样式类名
 */
export function getSpaceRoleClass(role: number): string {
  return SPACE_ROLE_MAP.CLASS[role as keyof typeof SPACE_ROLE_MAP.CLASS] || 'viewer'
}

/**
 * 获取空间等级文本
 */
export function getSpaceLevelText(level: number): string {
  return SPACE_LEVEL_MAP[level as keyof typeof SPACE_LEVEL_MAP] || `Lv${level}`
}

/**
 * 计算容量使用百分比
 */
export function getUsagePercent(used: number, total: number): number {
  if (total === 0) return 0
  return Math.round((used / total) * 100)
}

/**
 * 格式化空间容量显示
 */
export function formatSpaceSize(used: number, total: number): string {
  return `${formatFileSize(used)} / ${formatFileSize(total)}`
}

/**
 * 获取用户头像文本（首字母）
 */
export function getUserAvatarText(username: string): string {
  return username ? username.charAt(0).toUpperCase() : '?'
}
