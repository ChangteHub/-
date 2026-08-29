import request from './request'
import type { Category } from './types'

export const categoryApi = {
  getAll: (): Promise<Category[]> => request.get('/category/list'),
}
