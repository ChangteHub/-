import request from './request'
import type { Product, PageParams, PageResult } from './types'

export const historyApi = {
  getList: (params?: PageParams): Promise<PageResult<Product>> =>
    request.get('/history', { params }),
  add: (productId: string): Promise<void> =>
    request.post('/history', { productId }),
  clear: (): Promise<void> => request.delete('/history'),
  remove: (productId: string): Promise<void> =>
    request.delete(`/history/${productId}`),
}
