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
