import request from './request'
import type { Product, PageParams, PageResult } from './types'

export const favoriteApi = {
  getList: (params?: PageParams): Promise<PageResult<Product>> =>
    request.get('/user/favorites', { params }),
  add: (productId: string): Promise<void> =>
    request.post(`/favorite/${productId}`),
  remove: (productId: string): Promise<void> =>
    request.delete(`/favorite/${productId}`),
  check: (productId: string): Promise<boolean> =>
    request.get(`/favorite/check/${productId}`),
}
