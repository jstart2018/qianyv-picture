/**
 * 用户头像相关工具函数
 */

/**
 * 获取用户头像显示文本（昵称首字母）
 * @param nickname 用户昵称
 * @returns 昵称首字母（大写），如果昵称为空则返回 '?'
 */
export function getUserAvatarText(nickname?: string): string {
  if (!nickname) return '?'
  return nickname.charAt(0).toUpperCase()
}

/**
 * 生成默认头像背景色（根据昵称哈希）
 * @param nickname 用户昵称
 * @returns CSS 渐变背景色
 */
export function getAvatarGradient(nickname?: string): string {
  if (!nickname) {
    return 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  }

  // 简单哈希函数
  let hash = 0
  for (let i = 0; i < nickname.length; i++) {
    hash = nickname.charCodeAt(i) + ((hash << 5) - hash)
  }

  // 预定义的渐变色方案
  const gradients = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    'linear-gradient(135deg, #30cfd0 0%, #330867 100%)',
    'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
    'linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%)',
    'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)',
    'linear-gradient(135deg, #ff6e7f 0%, #bfe9ff 100%)',
  ]

  return gradients[Math.abs(hash) % gradients.length]
}
