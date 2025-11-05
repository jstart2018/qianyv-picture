/**
 * 文件验证工具
 * 用于验证文件大小、类型等
 */

import { FILE_CONSTRAINTS } from '@/constants/file'

/** 文件验证结果 */
export interface FileValidationResult {
  valid: boolean
  error?: string
}

/**
 * 验证文件大小
 * @param file 文件对象
 * @returns 验证结果
 */
export function validateFileSize(file: File): FileValidationResult {
  const maxSize = file.type.startsWith('image/')
    ? FILE_CONSTRAINTS.MAX_IMAGE_SIZE
    : FILE_CONSTRAINTS.MAX_VIDEO_SIZE

  if (file.size > maxSize) {
    const maxSizeMB = maxSize / (1024 * 1024)
    return {
      valid: false,
      error: `文件过大，最大支持 ${maxSizeMB}MB`,
    }
  }

  return { valid: true }
}

/**
 * 验证文件类型
 * @param file 文件对象
 * @param allowedTypes 允许的文件类型数组
 * @returns 验证结果
 */
export function validateFileType(file: File, allowedTypes: string[]): FileValidationResult {
  if (!allowedTypes.includes(file.type)) {
    return {
      valid: false,
      error: `不支持的文件类型：${file.type}`,
    }
  }

  return { valid: true }
}

/**
 * 验证图片文件
 * @param file 文件对象
 * @returns 验证结果
 */
export function validateImageFile(file: File): FileValidationResult {
  // 验证类型
  const typeResult = validateFileType(file, FILE_CONSTRAINTS.ALLOWED_IMAGE_TYPES)
  if (!typeResult.valid) return typeResult

  // 验证大小
  const sizeResult = validateFileSize(file)
  if (!sizeResult.valid) return sizeResult

  return { valid: true }
}

/**
 * 验证视频文件
 * @param file 文件对象
 * @returns 验证结果
 */
export function validateVideoFile(file: File): FileValidationResult {
  // 验证类型
  const typeResult = validateFileType(file, FILE_CONSTRAINTS.ALLOWED_VIDEO_TYPES)
  if (!typeResult.valid) return typeResult

  // 验证大小
  const sizeResult = validateFileSize(file)
  if (!sizeResult.valid) return sizeResult

  return { valid: true }
}

/**
 * 批量验证文件
 * @param files 文件数组
 * @param validator 验证函数
 * @returns 验证结果数组
 */
export function validateFiles(
  files: File[],
  validator: (file: File) => FileValidationResult,
): FileValidationResult[] {
  return files.map(validator)
}

/**
 * 检查是否所有文件都验证通过
 * @param results 验证结果数组
 * @returns 是否全部通过
 */
export function allFilesValid(results: FileValidationResult[]): boolean {
  return results.every((result) => result.valid)
}

/**
 * 获取第一个验证失败的错误信息
 * @param results 验证结果数组
 * @returns 错误信息，如果全部通过则返回 null
 */
export function getFirstError(results: FileValidationResult[]): string | null {
  const failedResult = results.find((result) => !result.valid)
  return failedResult?.error || null
}
