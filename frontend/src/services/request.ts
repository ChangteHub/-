import axios from 'axios'
import { useStore } from '../stores/useStore'

/**
 * Axios 实例 + 统一拦截器（token 注入、业务码拆包、401/403 处理）。
 * 所有后端通信必须经由本实例（或引用它的 services 模块）发出。
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
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

export default request
