import request from './request'

export const searchApi = {
  getHistory: (): Promise<string[]> => request.get('/search/history'),
  clearHistory: (): Promise<void> => request.delete('/search/history'),
  removeHistory: (keyword: string): Promise<void> =>
    request.delete(`/search/history/${keyword}`),
  getHot: (): Promise<string[]> => request.get('/search/hot'),
}
