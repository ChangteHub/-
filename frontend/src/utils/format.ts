import type { SyntheticEvent } from 'react'

/**
 * 格式化日期
 */
export const formatDate = (dateStr: string | undefined | null): string => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

/**
 * 格式化价格
 */
export const formatPrice = (price: number): string => {
  return `¥${price.toFixed(2)}`
}

/**
 * 默认占位图（内联 SVG，避免依赖外部服务）
 */
export const DEFAULT_AVATAR = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCA0MCA0MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIGZpbGw9IiNlMGUwZTAiLz48Y2lyY2xlIGN4PSIyMCIgY3k9IjE2IiByPSI2IiBmaWxsPSIjYjBiMGIwIi8+PHBhdGggZD0iTTMwIDMyQzMwIDI2LjQ3NyAyNS41MjMgMjIgMjAgMjJDMTQuNDc3IDIyIDEwIDI2LjQ3NyAxMCAzMiIgZmlsbD0iI2IwYjBiMCIvPjwvc3ZnPg=='

export const DEFAULT_PRODUCT_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHZpZXdCb3g9IjAgMCA4MCA4MCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iODAiIGhlaWdodD0iODAiIGZpbGw9IiNmNWY1ZjUiLz48cGF0aCBkPSJNMzAgMzVMMzUgMzBMNDAgMzVMNDUgMjVMNTUgNDVIMjVMMzAgMzVaIiBmaWxsPSIjZDBkMGQwIi8+PGNpcmNsZSBjeD0iMzIiIGN5PSIyOCIgcj0iNCIgZmlsbD0iI2QwZDBkMCIvPjwvc3ZnPg=='

/**
 * 聊天/会话列表时间：今天显示 HH:mm，昨天显示"昨天 HH:mm"，
 * 一周内显示"周X HH:mm"，更早显示 YYYY/M/D
 */
export const formatChatTime = (dateStr: string | undefined | null): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  if (isNaN(date.getTime())) return ''

  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  const hm = `${pad(date.getHours())}:${pad(date.getMinutes())}`
  const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime()
  const dayDiff = Math.round((startOfDay(now) - startOfDay(date)) / 86400000)

  if (dayDiff === 0) return hm
  if (dayDiff === 1) return `昨天 ${hm}`
  if (dayDiff > 1 && dayDiff < 7) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return `${weekdays[date.getDay()]} ${hm}`
  }
  return `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`
}

/**
 * 头像/图片加载失败时兜底为默认占位图（React onError 直接绑定）
 * 用 data-* 记录已兜底过，避免默认图本身加载失败时死循环
 */
export const makeImgFallback =
  (fallback: string) => (e: SyntheticEvent<HTMLImageElement>) => {
    const img = e.currentTarget
    if (img.dataset.fellBack) return
    img.dataset.fellBack = '1'
    img.src = fallback
  }

export const avatarFallback = makeImgFallback(DEFAULT_AVATAR)
export const productImageFallback = makeImgFallback(DEFAULT_PRODUCT_IMAGE)
