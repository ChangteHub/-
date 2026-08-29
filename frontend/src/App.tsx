import { useEffect } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { ConfigProvider } from 'antd-mobile'
import zhCN from 'antd-mobile/es/locales/zh-CN'
import { useStore } from './stores/useStore'
import BottomTab from './components/BottomTab'
import LoginModal from './components/LoginModal'
import './index.css'

/** 需要显示底部 Tab 的路由 */
const tabRoutes = ['/', '/category', '/chat', '/profile']

export default function App() {
  const location = useLocation()
  const { checkAuth } = useStore()
  const showTab = tabRoutes.some(
    (r) => location.pathname === r || (r !== '/' && location.pathname.startsWith(r))
  )

  useEffect(() => {
    checkAuth()
    const darkMode = localStorage.getItem('darkMode') === 'true'
    if (darkMode) {
      document.body.classList.add('dark-mode')
    }

    // 多标签页同步：其他标签页登录/登出导致 token 变化时，重新校验本页身份
    const onStorage = (e: StorageEvent) => {
      if (e.key === 'token') {
        checkAuth()
      }
    }
    window.addEventListener('storage', onStorage)
    return () => window.removeEventListener('storage', onStorage)
  }, [])

  // 路由切换时滚动内容区到顶部（不再强制整树重挂载，保留页面状态）
  useEffect(() => {
    document.querySelector('.app-content')?.scrollTo(0, 0)
  }, [location.pathname])

  return (
    <ConfigProvider locale={zhCN}>
      <div className="app-container">
        <div className="app-content">
          <Outlet />
        </div>
        {showTab && <BottomTab />}
        <LoginModal />
      </div>
    </ConfigProvider>
  )
}
