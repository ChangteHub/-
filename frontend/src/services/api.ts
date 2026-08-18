import axios from 'axios'
import { useStore } from '../store/useStore'
import type { Product, Category, User, ChatSessionVO, ChatMessageVO } from '../types'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data.code === 200) {
      return data.data
    }
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      // 仅当携带token仍返回401（token过期/失效）时清理凭证并重置登录态；
      // 不做整页跳转（避免刷新丢失表单/滚动状态），由路由守卫与页面引导处理
      if (status === 401 && localStorage.getItem('token')) {
        localStorage.removeItem('token')
        // 运行时访问 store 清理内存态（模块已初始化，无循环依赖问题）
        useStore.getState().checkAuth()
      }
      if (status === 403) {
        // 已登录但权限不足：明确提示，不跳转
        return Promise.reject(new Error(data?.message || '没有权限执行此操作'))
      }
      return Promise.reject(new Error(data?.message || '请求失败'))
    }
    return Promise.reject(error)
  }
)

// 请求参数类型定义
export interface RegisterData {
  username: string
  password: string
  nickname: string
  studentId?: string
}

export interface LoginData {
  username: string
  password: string
}

export interface UpdateProfileData {
  nickname?: string
  avatar?: string
  phone?: string
  bio?: string
}

export interface CreateProductData {
  categoryId: number
  title: string
  description?: string
  price: number
  originalPrice?: number
  coverImage?: string
  location?: string
  productCondition: string
  images?: string[]
}

export interface UpdateProductData {
  categoryId?: string
  title?: string
  description?: string
  price?: number
  originalPrice?: number
  coverImage?: string
  location?: string
  productCondition?: string
  images?: string[]
}

export interface ProductListParams {
  categoryId?: string
  keyword?: string
  sort?: string
  pageNum?: number
  pageSize?: number
}

export interface PageParams {
  pageNum?: number
  pageSize?: number
}

export interface StatusPageParams extends PageParams {
  status?: number
}

// 响应类型定义
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  pageNum: number
  pageSize: number
  pages: number
  list: T[]
}

export interface LoginResult {
  token: string
  user: User
}

export const authApi = {
  register: (data: RegisterData): Promise<User> =>
    api.post('/auth/register', data),
  login: (data: LoginData): Promise<LoginResult> =>
    api.post('/auth/login', data),
  getMe: (): Promise<User> => api.get('/auth/me'),
}

export const userApi = {
  updateProfile: (data: UpdateProfileData): Promise<User> =>
    api.put('/user/profile', data),
  getMyProducts: (params?: StatusPageParams): Promise<PageResult<Product>> =>
    api.get('/user/products', { params }),
  getMyFavorites: (params?: PageParams): Promise<PageResult<Product>> =>
    api.get('/user/favorites', { params }),
}

export const productApi = {
  getList: (params?: ProductListParams): Promise<PageResult<Product>> =>
    api.get('/product/list', { params }),
  getById: (id: string): Promise<Product> =>
    api.get(`/product/${id}`),
  create: (data: CreateProductData): Promise<Product> =>
    api.post('/product', data),
  update: (id: string, data: UpdateProductData): Promise<Product> =>
    api.put(`/product/${id}`, data),
  delete: (id: string): Promise<void> =>
    api.delete(`/product/${id}`),
  updateStatus: (id: string, status: number): Promise<void> =>
    api.put(`/product/${id}/status`, { status }),
  getMy: (params?: StatusPageParams): Promise<PageResult<Product>> =>
    api.get('/user/products', { params }),
}

export const categoryApi = {
  getAll: (): Promise<Category[]> => api.get('/category/list'),
}

export const favoriteApi = {
  getList: (params?: PageParams): Promise<PageResult<Product>> =>
    api.get('/user/favorites', { params }),
  add: (productId: string): Promise<void> =>
    api.post(`/favorite/${productId}`),
  remove: (productId: string): Promise<void> =>
    api.delete(`/favorite/${productId}`),
  check: (productId: string): Promise<boolean> =>
    api.get(`/favorite/check/${productId}`),
}

export const historyApi = {
  getList: (params?: PageParams): Promise<PageResult<Product>> =>
    api.get('/history', { params }),
  add: (productId: string): Promise<void> =>
    api.post('/history', { productId }),
  clear: (): Promise<void> => api.delete('/history'),
  remove: (productId: string): Promise<void> =>
    api.delete(`/history/${productId}`),
}

export const searchApi = {
  getHistory: (): Promise<string[]> => api.get('/search/history'),
  clearHistory: (): Promise<void> => api.delete('/search/history'),
  removeHistory: (keyword: string): Promise<void> =>
    api.delete(`/search/history/${keyword}`),
  getHot: (): Promise<string[]> => api.get('/search/hot'),
}

export const chatApi = {
  getConversations: (): Promise<ChatSessionVO[]> =>
    api.get('/chat/sessions'),
  getConversation: (id: string): Promise<ChatSessionVO> =>
    api.get(`/chat/session/${id}`),
  createConversation: (productId: string, targetUserId: string): Promise<ChatSessionVO> =>
    api.post('/chat/session', { productId, targetUserId }),
  getMessages: (sessionId: string, params?: PageParams): Promise<PageResult<ChatMessageVO>> =>
    api.get(`/chat/messages/${sessionId}`, { params }),
  markRead: (sessionId: string): Promise<void> =>
    api.put(`/chat/messages/${sessionId}/read`),
}

export const verificationApi = {
  submit: (data: {
    realName: string
    studentId: string
    college: string
    enrollYear?: number
    studentCardUrl: string
  }): Promise<void> => api.post('/verification', data),
  getStatus: (): Promise<any> => api.get('/verification/status'),
}

export const commonApi = {
  getCategories: (): Promise<Category[]> => api.get('/category/list'),
  getBanners: (): Promise<any[]> => api.get('/banners'),
  getHelp: (params?: { category?: string; keyword?: string }): Promise<any[]> =>
    api.get('/help', { params }),
  uploadImage: (file: File): Promise<{ url: string }> => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

// 管理员API
export const adminApi = {
  // 仪表盘
  getDashboard: (): Promise<any> => api.get('/admin/dashboard'),

  // 用户管理
  getUsers: (params?: {
    keyword?: string
    status?: number
    pageNum?: number
    pageSize?: number
  }): Promise<PageResult<any>> => api.get('/admin/users', { params }),
  getUserDetail: (id: string): Promise<any> => api.get(`/admin/users/${id}`),
  updateUserStatus: (id: string, status: number): Promise<void> =>
    api.put(`/admin/users/${id}/status`, { status }),

  // 商品管理
  getProducts: (params?: {
    keyword?: string
    status?: number
    categoryId?: number
    pageNum?: number
    pageSize?: number
  }): Promise<PageResult<any>> => api.get('/admin/products', { params }),
  updateProductStatus: (id: string, status: number): Promise<void> =>
    api.put(`/admin/products/${id}/status`, { status }),
  deleteProduct: (id: string): Promise<void> =>
    api.delete(`/admin/products/${id}`),

  // 认证管理
  getVerifications: (params?: {
    status?: number
    pageNum?: number
    pageSize?: number
  }): Promise<PageResult<any>> => api.get('/admin/verifications', { params }),
  reviewVerification: (
    id: string,
    data: { status: number; rejectReason?: string }
  ): Promise<void> => api.put(`/admin/verifications/${id}/review`, data),

  // 分类管理
  getCategories: (): Promise<Category[]> => api.get('/admin/categories'),
  addCategory: (data: {
    name: string
    icon?: string
    sort?: number
  }): Promise<void> => api.post('/admin/categories', data),
  updateCategory: (
    id: string,
    data: { name?: string; icon?: string; sort?: number }
  ): Promise<void> => api.put(`/admin/categories/${id}`, data),
  deleteCategory: (id: string): Promise<void> =>
    api.delete(`/admin/categories/${id}`),
  updateCategoryStatus: (id: string, status: number): Promise<void> =>
    api.put(`/admin/categories/${id}/status`, { status }),
}

export default api
