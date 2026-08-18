import { useLocation, useNavigate } from 'react-router-dom'
import {
  HomeOutlined,
  AppstoreOutlined,
  PlusCircleOutlined,
  MessageOutlined,
  UserOutlined,
} from '@ant-design/icons'
import './BottomTab.css'

const tabs = [
  { path: '/', icon: <HomeOutlined />, label: '首页' },
  { path: '/category', icon: <AppstoreOutlined />, label: '分类' },
  { path: '/publish', icon: <PlusCircleOutlined />, label: '发布', primary: true },
  { path: '/chat', icon: <MessageOutlined />, label: '消息' },
  { path: '/profile', icon: <UserOutlined />, label: '我的' },
]

export default function BottomTab() {
  const location = useLocation()
  const navigate = useNavigate()

  return (
    <nav className="bottom-tab" data-od-id="bottom-tab" role="navigation" aria-label="主导航">
      {tabs.map((tab) => {
        const active = tab.path === '/' ? location.pathname === '/' : location.pathname.startsWith(tab.path)
        return (
          <button
            key={tab.path}
            className={`tab-item${active ? ' active' : ''}${tab.primary ? ' tab-primary' : ''}`}
            onClick={() => navigate(tab.path)}
            aria-label={tab.label}
            aria-current={active ? 'page' : undefined}
          >
            <span className="tab-icon">{tab.icon}</span>
            <span className="tab-label">{tab.label}</span>
          </button>
        )
      })}
    </nav>
  )
}
