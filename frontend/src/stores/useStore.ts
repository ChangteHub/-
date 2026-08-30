import { create } from 'zustand'
import type { User } from '../types'
import { authApi, favoriteApi, historyApi } from '../services/api'

interface AppState {
  user: User | null
  loginVisible: boolean
  /** 是否已完成登录态检查（checkAuth 执行完毕；用于路由守卫区分"加载中"与"未登录"） */
  authChecked: boolean
  favorites: string[]
  browsingHistory: string[]
  login: (username: string, password: string) => Promise<void>
  logout: () => void
  showLogin: () => void
  hideLogin: () => void
  requireLogin: () => boolean
  toggleFavorite: (productId: string) => void
  addBrowsingHistory: (productId: string) => void
  clearBrowsingHistory: () => void
  removeBrowsingHistory: (productId: string) => void
  loadFavorites: () => void
  loadBrowsingHistory: () => void
  checkAuth: () => Promise<void>
}

export const useStore = create<AppState>((set, get) => ({
  user: null,
  loginVisible: false,
  authChecked: false,
  favorites: [],
  browsingHistory: [],

  login: async (username: string, password: string) => {
    try {
      const res: any = await authApi.login({ username, password })
      if (res?.token && res?.user) {
        localStorage.setItem('token', res.token)
        set({
          user: res.user,
          loginVisible: false,
          authChecked: true,
        })
        get().loadFavorites()
        get().loadBrowsingHistory()
      }
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  },

  logout: () => {
    localStorage.removeItem('token')
    set({ user: null, favorites: [], browsingHistory: [], loginVisible: false })
  },

  showLogin: () => set({ loginVisible: true }),

  hideLogin: () => set({ loginVisible: false }),

  requireLogin: () => {
    const { user, showLogin } = get()
    if (!user) {
      showLogin()
      return false
    }
    return true
  },

  toggleFavorite: async (productId: string) => {
    const { favorites, requireLogin } = get()
    if (!requireLogin()) return
    try {
      if (favorites.includes(productId)) {
        await favoriteApi.remove(productId)
        set({ favorites: favorites.filter((id) => id !== productId) })
      } else {
        await favoriteApi.add(productId)
        set({ favorites: [...favorites, productId] })
      }
    } catch (error) {
      console.error('收藏操作失败:', error)
    }
  },

  addBrowsingHistory: async (productId: string) => {
    // 未登录不发请求
    if (!get().user) return
    const { browsingHistory } = get()
    const filtered = browsingHistory.filter((id) => id !== productId)
    set({ browsingHistory: [productId, ...filtered].slice(0, 100) })
    try {
      await historyApi.add(productId)
    } catch (error) {
      console.error('添加浏览记录失败:', error)
    }
  },

  clearBrowsingHistory: async () => {
    try {
      await historyApi.clear()
      set({ browsingHistory: [] })
    } catch (error) {
      console.error('清空浏览记录失败:', error)
    }
  },

  removeBrowsingHistory: async (productId: string) => {
    const { browsingHistory } = get()
    try {
      await historyApi.remove(productId)
      set({ browsingHistory: browsingHistory.filter((id) => id !== productId) })
    } catch (error) {
      console.error('删除浏览记录失败:', error)
    }
  },

  loadFavorites: async () => {
    try {
      const res: any = await favoriteApi.getList({ pageSize: 100 })
      if (res?.list) {
        set({ favorites: res.list.map((p: any) => p.id) })
      }
    } catch (error) {
      console.error('加载收藏列表失败:', error)
    }
  },

  loadBrowsingHistory: async () => {
    try {
      const res: any = await historyApi.getList({ pageSize: 100 })
      if (res?.list) {
        set({ browsingHistory: res.list.map((p: any) => p.id) })
      }
    } catch (error) {
      console.error('加载浏览记录失败:', error)
    }
  },

  checkAuth: async () => {
    const token = localStorage.getItem('token')
    if (!token) {
      // 无 token：检查完成，明确未登录并清空内存态（避免残留旧用户数据）
      set({ authChecked: true, user: null, favorites: [], browsingHistory: [] })
      return
    }

    try {
      const res: any = await authApi.getMe()
      if (res) {
        set({ user: res, authChecked: true })
        get().loadFavorites()
        get().loadBrowsingHistory()
      } else {
        // getMe 返回空（用户被删/异常）：与其他分支一致清空内存态
        set({ authChecked: true, user: null, favorites: [], browsingHistory: [] })
      }
    } catch {
      // token 失效：清理凭证与内存态，完成检查
      localStorage.removeItem('token')
      set({ authChecked: true, user: null, favorites: [], browsingHistory: [] })
    }
  },
}))
