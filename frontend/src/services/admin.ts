import request from './request'
import type { Category, PageResult } from './types'

// 管理员API
export const adminApi = {
  // 仪表盘
  getDashboard: (): Promise<any> => request.get('/admin/dashboard'),

  // 用户管理
  getUsers: (params?: {
    keyword?: string
    status?: number
    pageNum?: number
    pageSize?: number
  }): Promise<PageResult<any>> => request.get('/admin/users', { params }),
  getUserDetail: (id: string): Promise<any> => request.get(`/admin/users/${id}`),
  updateUserStatus: (id: string, status: number): Promise<void> =>
    request.put(`/admin/users/${id}/status`, { status }),

  // 商品管理
  getProducts: (params?: {
    keyword?: string
    status?: number
    categoryId?: number
    pageNum?: number
    pageSize?: number
  }): Promise<PageResult<any>> => request.get('/admin/products', { params }),
  updateProductStatus: (id: string, status: number): Promise<void> =>
    request.put(`/admin/products/${id}/status`, { status }),
  deleteProduct: (id: string): Promise<void> =>
    request.delete(`/admin/products/${id}`),

  // 认证管理
  getVerifications: (params?: {
    status?: number
    pageNum?: number
    pageSize?: number
  }): Promise<PageResult<any>> => request.get('/admin/verifications', { params }),
  reviewVerification: (
    id: string,
    data: { status: number; rejectReason?: string }
  ): Promise<void> => request.put(`/admin/verifications/${id}/review`, data),

  // 分类管理
  getCategories: (): Promise<Category[]> => request.get('/admin/categories'),
  addCategory: (data: {
    name: string
    icon?: string
    sort?: number
  }): Promise<void> => request.post('/admin/categories', data),
  updateCategory: (
    id: string,
    data: { name?: string; icon?: string; sort?: number }
  ): Promise<void> => request.put(`/admin/categories/${id}`, data),
  deleteCategory: (id: string): Promise<void> =>
    request.delete(`/admin/categories/${id}`),
  updateCategoryStatus: (id: string, status: number): Promise<void> =>
    request.put(`/admin/categories/${id}/status`, { status }),
}
