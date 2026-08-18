import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { adminApi } from '../../services/api'
import './Dashboard.css'

interface DashboardStats {
  totalUsers: number
  activeUsers: number
  totalProducts: number
  onSaleProducts: number
  soldProducts: number
  totalVerifications: number
  pendingVerifications: number
  totalCategories: number
  todayNewUsers: number
  todayNewProducts: number
}

export default function Dashboard() {
  const navigate = useNavigate()
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadStats()
  }, [])

  const loadStats = async () => {
    try {
      setLoading(true)
      const data = await adminApi.getDashboard()
      setStats(data)
    } catch (error) {
      console.error('Failed to load dashboard stats:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return <div className="dashboard-loading">加载中...</div>
  }

  if (!stats) {
    return <div className="dashboard-error">加载失败</div>
  }

  const statCards = [
    {
      title: '总用户数',
      value: stats.totalUsers,
      subtitle: `今日新增 ${stats.todayNewUsers}`,
      icon: '👥',
      color: '#1890ff',
      onClick: () => navigate('/admin/users'),
    },
    {
      title: '总商品数',
      value: stats.totalProducts,
      subtitle: `今日新增 ${stats.todayNewProducts}`,
      icon: '📦',
      color: '#52c41a',
      onClick: () => navigate('/admin/products'),
    },
    {
      title: '在售商品',
      value: stats.onSaleProducts,
      subtitle: `已售 ${stats.soldProducts}`,
      icon: '🏷',
      color: '#faad14',
      onClick: () => navigate('/admin/products?status=0'),
    },
    {
      title: '待审核认证',
      value: stats.pendingVerifications,
      subtitle: `总认证 ${stats.totalVerifications}`,
      icon: '⏳',
      color: '#ff4d4f',
      onClick: () => navigate('/admin/verifications?status=0'),
    },
  ]

  return (
    <div className="dashboard">
      <h1 className="dashboard-title">仪表盘</h1>
      <div className="stat-cards">
        {statCards.map((card, index) => (
          <div
            key={index}
            className="stat-card"
            onClick={card.onClick}
            style={{ borderLeftColor: card.color }}
          >
            <div className="stat-icon">{card.icon}</div>
            <div className="stat-info">
              <div className="stat-title">{card.title}</div>
              <div className="stat-value" style={{ color: card.color }}>
                {card.value}
              </div>
              <div className="stat-subtitle">{card.subtitle}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="quick-actions">
        <h2>快捷操作</h2>
        <div className="action-buttons">
          <button
            className="action-btn"
            onClick={() => navigate('/admin/users')}
          >
            👥 用户管理
          </button>
          <button
            className="action-btn"
            onClick={() => navigate('/admin/products')}
          >
            📦 商品管理
          </button>
          <button
            className="action-btn"
            onClick={() => navigate('/admin/verifications')}
          >
            ✅ 认证审核
          </button>
          <button
            className="action-btn"
            onClick={() => navigate('/admin/categories')}
          >
            📂 分类管理
          </button>
        </div>
      </div>
    </div>
  )
}
