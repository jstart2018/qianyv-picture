/**
 * 日期格式化工具
 * 提供多种日期格式化方法
 */

/**
 * 日期格式类型
 */
export type DateFormatType = 'full' | 'date' | 'time' | 'datetime'

/**
 * 格式化日期
 * @param dateString 日期字符串或 Date 对象
 * @param format 格式类型（默认 'date'）
 * @returns 格式化后的日期字符串
 * @example
 * formatDate('2024-01-15', 'date') // "2024-01-15"
 * formatDate('2024-01-15 10:30:00', 'full') // "2024-01-15 10:30:00"
 * formatDate('2024-01-15', 'time') // "00:00:00"
 */
export function formatDate(
  dateString: string | Date | null | undefined,
  format: DateFormatType = 'date',
): string {
  if (!dateString) return '未知'

  const date = new Date(dateString)
  if (isNaN(date.getTime())) return '无效日期'

  const options: Record<DateFormatType, Intl.DateTimeFormatOptions> = {
    date: { year: 'numeric', month: '2-digit', day: '2-digit' },
    time: { hour: '2-digit', minute: '2-digit', second: '2-digit' },
    datetime: {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    },
    full: {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    },
  }

  return date.toLocaleString('zh-CN', options[format])
}

/**
 * 格式化为相对时间（如：刚刚、5分钟前、1小时前）
 * @param dateString 日期字符串或 Date 对象
 * @returns 相对时间字符串
 */
export function formatRelativeTime(dateString: string | Date | null | undefined): string {
  if (!dateString) return '未知'

  const date = new Date(dateString)
  if (isNaN(date.getTime())) return '无效日期'

  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  if (months < 12) return `${months}个月前`
  return `${years}年前`
}

/**
 * 格式化为简短日期（如：01-15、2024-01-15）
 * @param dateString 日期字符串或 Date 对象
 * @param includeYear 是否包含年份（默认 false）
 * @returns 简短日期字符串
 */
export function formatShortDate(
  dateString: string | Date | null | undefined,
  includeYear: boolean = false,
): string {
  if (!dateString) return '未知'

  const date = new Date(dateString)
  if (isNaN(date.getTime())) return '无效日期'

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return includeYear ? `${year}-${month}-${day}` : `${month}-${day}`
}
