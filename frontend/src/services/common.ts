import request from './request'
import type { Category } from './types'

export const commonApi = {
  getCategories: (): Promise<Category[]> => request.get('/category/list'),
  getBanners: (): Promise<any[]> => request.get('/banners'),
  getHelp: (params?: { category?: string; keyword?: string }): Promise<any[]> =>
    request.get('/help', { params }),
  uploadImage: (file: File): Promise<{ url: string }> => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
