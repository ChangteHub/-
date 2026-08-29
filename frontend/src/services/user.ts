import request from './request'
import type { Product, User, UpdateProfileData, PageParams, StatusPageParams, PageResult } from './types'

export const userApi = {
  updateProfile: (data: UpdateProfileData): Promise<User> =>
    request.put('/user/profile', data),
  getMyProducts: (params?: StatusPageParams): Promise<PageResult<Product>> =>
    request.get('/user/products', { params }),
  getMyFavorites: (params?: PageParams): Promise<PageResult<Product>> =>
    request.get('/user/favorites', { params }),
}
