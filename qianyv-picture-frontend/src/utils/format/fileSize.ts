/**
 * 文件大小格式化工具
 * 将字节数转换为可读的文件大小格式
 */

/**
 * 格式化文件大小
 * @param bytes 文件字节数
 * @returns 格式化后的字符串（如 "2.5 MB"）
 * @example
 * formatFileSize(1024) // "1.00 KB"
 * formatFileSize(1048576) // "1.00 MB"
 * formatFileSize(0) // "0 B"
 */
export function formatFileSize(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B'

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  const size = bytes / Math.pow(k, i)

  return `${size.toFixed(2)} ${units[i]}`
}

/**
 * 格式化文件大小（整数版本）
 * @param bytes 文件字节数
 * @returns 格式化后的字符串（如 "3 MB"）
 */
export function formatFileSizeInt(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B'

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  const size = bytes / Math.pow(k, i)

  return `${Math.round(size)} ${units[i]}`
}
