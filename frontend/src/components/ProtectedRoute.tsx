import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useStore } from '../store/useStore'

interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRole?: number
}

export default function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const navigate = useNavigate()
  const { user, authChecked, showLogin, checkAuth } = useStore()

  useEffect(() => {
    // 首次进入时若认证检查尚未执行（如 /admin 为独立顶层路由，不经过 App 的 checkAuth），主动触发
    if (!authChecked) {
      checkAuth()
      return
    }

    if (!user) {
      showLogin()
      navigate('/')
      return
    }
    if (requiredRole !== undefined && user.role !== requiredRole) {
      navigate('/')
      return
    }
  }, [user, authChecked, requiredRole, navigate, showLogin, checkAuth])

  // 认证状态未就绪：渲染空（避免闪烁/误跳转）
  if (!authChecked) {
    return null
  }
  if (!user) {
    return null
  }
  if (requiredRole !== undefined && user.role !== requiredRole) {
    return null
  }

  return <>{children}</>
}
