import request from './request'
import type { Product, CreateProductData, UpdateProductData, ProductListParams, StatusPageParams, PageResult } from './types'

export const productApi = {
  getList: (params?: ProductListParams): Promise<PageResult<Product>> =>
    request.get('/product/list', { params }),
  getById: (id: string): Promise<Product> =>
    request.get(`/product/${id}`),
  create: (data: CreateProductData): Promise<Product> =>
    request.post('/product', data),
  update: (id: string, data: UpdateProductData): Promise<Product> =>
    request.put(`/product/${id}`, data),
  delete: (id: string): Promise<void> =>
    request.delete(`/product/${id}`),
  updateStatus: (id: string, status: number): Promise<void> =>
    request.put(`/product/${id}/status`, { status }),
  getMy: (params?: StatusPageParams): Promise<PageResult<Product>> =>
    request.get('/user/products', { params }),
}
