/**
 * 文件相关常量定义
 */

/** 文件大小限制 */
export const FILE_CONSTRAINTS = {
  /** 最大图片大小（30MB） */
  MAX_IMAGE_SIZE: 30 * 1024 * 1024,

  /** 最大视频大小（500MB） */
  MAX_VIDEO_SIZE: 500 * 1024 * 1024,

  /** 允许的图片类型 */
  ALLOWED_IMAGE_TYPES: ['image/png', 'image/jpeg', 'image/jpg', 'image/webp', 'image/gif'],

  /** 允许的视频类型 */
  ALLOWED_VIDEO_TYPES: ['video/mp4', 'video/webm', 'video/quicktime'],
} as const

/** 草稿相关常量 */
export const DRAFT_CONFIG = {
  /** 草稿存储键 */
  STORAGE_KEY: 'blog_upload_draft',

  /** 草稿过期天数 */
  EXPIRY_DAYS: 7,

  /** 草稿最大数量 */
  MAX_DRAFTS: 10,
} as const

/** 文件上传相关常量 */
export const UPLOAD_CONFIG = {
  /** 单次最大上传文件数 */
  MAX_FILES_PER_UPLOAD: 20,

  /** 并发上传数量 */
  CONCURRENT_UPLOADS: 3,

  /** 上传超时时间（毫秒） */
  TIMEOUT: 60000,
} as const

/** 图片分类 */
export const IMAGE_CATEGORIES = {
  HORIZONTAL: 'horizontal', // 横屏
  VERTICAL: 'vertical', // 竖屏
} as const

/** 图片比例阈值 */
export const IMAGE_SCALE = {
  /** 横屏图片最小比例（宽/高） */
  HORIZONTAL_MIN: 1.0,

  /** 竖屏图片最大比例（宽/高） */
  VERTICAL_MAX: 1.0,
} as const
