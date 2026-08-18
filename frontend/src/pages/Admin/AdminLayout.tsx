import { useState } from 'react'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import { useStore } from '../../store/useStore'
import './AdminLayout.css'

const menuItems = [
  { path: '/admin', label: '仪表盘', icon: '📊' },
  { path: '/admin/users', label: '用户管理', icon: '👥' },
  { path: '/admin/products', label: '商品管理', icon: '📦' },
  { path: '/admin/verifications', label: '认证审核', icon: '✅' },
  { path: '/admin/categories', label: '分类管理', icon: '📂' },
]

export default function AdminLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useStore()
  const [collapsed, setCollapsed] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const getActiveMenu = () => {
    const path = location.pathname
    if (path === '/admin') return '/admin'
    return menuItems.find((item) => path.startsWith(item.path) && item.path !== '/admin')?.path || '/admin'
  }

  return (
    <div className="admin-layout">
      <aside className={`admin-sidebar ${collapsed ? 'collapsed' : ''}`}>
        <div className="admin-logo">
          <span className="logo-icon">🛠</span>
          {!collapsed && <span className="logo-text">管理后台</span>}
        </div>
        <nav className="admin-menu">
          {menuItems.map((item) => (
            <div
              key={item.path}
              className={`menu-item ${getActiveMenu() === item.path ? 'active' : ''}`}
              onClick={() => navigate(item.path)}
            >
              <span className="menu-icon">{item.icon}</span>
              {!collapsed && <span className="menu-label">{item.label}</span>}
            </div>
          ))}
        </nav>
        <div className="menu-item back-home" onClick={() => navigate('/')}>
          <span className="menu-icon">🏠</span>
          {!collapsed && <span className="menu-label">返回前台</span>}
        </div>
      </aside>
      <main className="admin-main">
        <header className="admin-header">
          <div className="header-left">
            <button className="collapse-btn" onClick={() => setCollapsed(!collapsed)}>
              {collapsed ? '☰' : '✕'}
            </button>
            <h2>校园二手交易平台管理后台</h2>
          </div>
          <div className="header-right">
            <span className="admin-user">
              {user?.avatar ? (
                <img src={user.avatar} alt="avatar" className="admin-avatar" />
              ) : (
                <span className="admin-avatar-placeholder">
                  {user?.nickname?.[0] || 'A'}
                </span>
              )}
              <span className="admin-name">{user?.nickname || '管理员'}</span>
            </span>
            <button className="logout-btn" onClick={handleLogout}>
              退出登录
            </button>
          </div>
        </header>
        <div className="admin-content">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
