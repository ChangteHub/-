import type { Product, Category, User, ChatSessionVO, ChatMessageVO } from '../types'

// ===== 请求参数类型 =====
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

// ===== 响应类型 =====
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

export type { Product, Category, User, ChatSessionVO, ChatMessageVO }
