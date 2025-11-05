/**
 * 标签解析工具
 * 用于解析各种格式的标签数据
 */

/**
 * 解析标签数据
 * 支持多种输入格式：数组、JSON字符串、逗号分隔字符串
 * @param tagsData 标签数据（可能是字符串、数组或 JSON）
 * @returns 标签数组
 * @example
 * parseTags(['标签1', '标签2']) // ['标签1', '标签2']
 * parseTags('["标签1", "标签2"]') // ['标签1', '标签2']
 * parseTags('标签1,标签2') // ['标签1', '标签2']
 * parseTags(null) // []
 */
export function parseTags(tagsData: string | string[] | null | undefined): string[] {
  if (!tagsData) return []

  try {
    // 如果已经是数组，直接返回
    if (Array.isArray(tagsData)) {
      return tagsData.filter((tag) => typeof tag === 'string' && tag.trim())
    }

    // 如果是 JSON 数组字符串，解析它
    if (typeof tagsData === 'string' && tagsData.startsWith('[')) {
      const parsed = JSON.parse(tagsData)
      if (Array.isArray(parsed)) {
        return parsed.filter((tag) => typeof tag === 'string' && tag.trim())
      }
    }

    // 如果是逗号分隔的字符串
    if (typeof tagsData === 'string') {
      return tagsData
        .split(',')
        .map((tag) => tag.trim())
        .filter((tag) => tag)
    }

    return []
  } catch (err) {
    console.error('解析标签失败:', err)
    return []
  }
}

/**
 * 将标签数组转换为逗号分隔的字符串
 * @param tags 标签数组
 * @returns 逗号分隔的字符串
 */
export function tagsToString(tags: string[]): string {
  if (!Array.isArray(tags) || tags.length === 0) return ''
  return tags.join(',')
}

/**
 * 将标签数组转换为 JSON 字符串
 * @param tags 标签数组
 * @returns JSON 字符串
 */
export function tagsToJSON(tags: string[]): string {
  if (!Array.isArray(tags) || tags.length === 0) return '[]'
  return JSON.stringify(tags)
}

/**
 * 验证标签是否有效
 * @param tag 标签字符串
 * @param maxLength 最大长度（默认50）
 * @returns 是否有效
 */
export function isValidTag(tag: string, maxLength: number = 50): boolean {
  if (!tag || typeof tag !== 'string') return false
  const trimmed = tag.trim()
  return trimmed.length > 0 && trimmed.length <= maxLength
}
