/**
 * 状态与字典常量
 *
 * 与后端枚举一一对应（见 backend V1 迁移脚本字段注释）。
 * 状态映射携带 CSS 类名（与 Admin 样式表约定），供状态标签直接渲染。
 */

// 商品状态 product.status: 0 在售 / 1 已售出 / 2 已下架
export const PRODUCT_STATUS: Record<number, { label: string; className: string }> = {
  0: { label: '在售', className: 'status-on-sale' },
  1: { label: '已售', className: 'status-sold' },
  2: { label: '已下架', className: 'status-off-shelf' },
}

// 用户账号状态 user.status: 0 正常 / 1 禁用
export const USER_STATUS: Record<number, { label: string; className: string }> = {
  0: { label: '正常', className: 'status-active' },
  1: { label: '已禁用', className: 'status-disabled' },
}

// 分类状态 category.status: 0 启用 / 1 禁用
export const CATEGORY_STATUS: Record<number, { label: string; className: string }> = {
  0: { label: '启用', className: 'status-active' },
  1: { label: '禁用', className: 'status-disabled' },
}

// 实名认证状态 verification.status: 0 待审核 / 1 已通过 / 2 已拒绝
export const VERIFICATION_STATUS: Record<number, { label: string; className: string }> = {
  0: { label: '待审核', className: 'status-pending' },
  1: { label: '已通过', className: 'status-approved' },
  2: { label: '已拒绝', className: 'status-rejected' },
}

// 商品成色 product_condition 可选值
export const PRODUCT_CONDITIONS = ['全新', '九成新', '八成新', '七成新及以下'] as const
