import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Tabs, Grid } from 'antd-mobile'
import {
  ShopOutlined,
  StarOutlined,
  ClockCircleOutlined,
  SafetyOutlined,
  QuestionCircleOutlined,
  SettingOutlined,
  RightOutlined,
  DashboardOutlined,
} from '@ant-design/icons'
import { useStore } from '../../store/useStore'
import { productApi } from '../../services/api'
import ProductCard from '../../components/ProductCard'
import type { Product } from '../../types'
import './Profile.css'

const featureItems = [
  { icon: <ShopOutlined />, label: '我的发布', key: 'publish', color: '#1677ff', path: '/my-publish' },
  { icon: <StarOutlined />, label: '我的收藏', key: 'collect', color: '#f59e0b', path: '/favorites' },
  { icon: <ClockCircleOutlined />, label: '浏览历史', key: 'history', color: '#10b981', path: '/history' },
  { icon: <SafetyOutlined />, label: '实名认证', key: 'verify', color: '#4f6ef7', path: '/verification' },
  { icon: <QuestionCircleOutlined />, label: '帮助中心', key: 'help', color: '#ec4899', path: '/help' },
  { icon: <SettingOutlined />, label: '设置', key: 'settings', color: '#6b7280', path: '/settings' },
]

export default function ProfilePage() {
  const navigate = useNavigate()
  const { user, showLogin } = useStore()
  const [activeTab, setActiveTab] = useState('on_sale')
  const [myProducts, setMyProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const seqRef = useRef(0)

  useEffect(() => {
    if (user) {
      loadMyProducts()
    }
  }, [user, activeTab])

  const loadMyProducts = async () => {
    const cur = ++seqRef.current // 序号保护：快速切换Tab时旧响应不覆盖新结果
    setLoading(true)
    try {
      const statusMap: Record<string, number> = {
        on_sale: 0,
        sold: 1,
        off_shelf: 2,
      }
      const status = statusMap[activeTab]
      const res: any = await productApi.getMy({ status, pageSize: 10 })
      if (cur !== seqRef.current) return
      if (res?.list) {
        setMyProducts(res.list)
      }
    } catch (error) {
      if (cur !== seqRef.current) return
      console.error('加载商品失败:', error)
    } finally {
      if (cur === seqRef.current) setLoading(false)
    }
  }

  const handleFeature = (item: typeof featureItems[0]) => {
    if (item.key === 'settings' || item.key === 'help') {
      navigate(item.path)
      return
    }
    if (!user) {
      showLogin()
      return
    }
    navigate(item.path)
  }

  return (
    <div className="page-wrapper profile-page" data-od-id="profile-page">
      {/* 用户信息区 */}
      <header className="profile-header" data-od-id="profile-header">
        <div className="profile-user">
          {user ? (
            <>
              <img src={user.avatar} alt={user.nickname} className="profile-avatar" />
              <div className="profile-info">
                <span className="profile-name">{user.nickname}</span>
                <span className="profile-school">{user.school}</span>
              </div>
              <Button size="small" fill="outline" onClick={() => { navigate('/edit-profile') }}>
                编辑资料
              </Button>
            </>
          ) : (
            <>
              <div className="profile-avatar-placeholder" aria-hidden="true" />
              <div className="profile-info">
                <span className="profile-name">未登录</span>
                <span className="profile-school">登录后查看更多</span>
              </div>
              <Button size="small" color="primary" onClick={showLogin}>
                立即登录
              </Button>
            </>
          )}
        </div>
      </header>

      {/* 功能网格 */}
      <section className="profile-features" data-od-id="profile-features" aria-label="功能入口">
        <Grid columns={3} gap={0}>
          {/* 管理员入口 */}
          {user?.role === 1 && (
            <Grid.Item>
              <div
                className="feature-item"
                role="button"
                tabIndex={0}
                onClick={() => navigate('/admin')}
                onKeyDown={(e) => e.key === 'Enter' && navigate('/admin')}
              >
                <span className="feature-icon" style={{ background: '#ff4d4f12', color: '#ff4d4f' }}>
                  <DashboardOutlined />
                </span>
                <span className="feature-label">管理后台</span>
              </div>
            </Grid.Item>
          )}
          {featureItems.map((item) => (
            <Grid.Item key={item.key}>
              <div
                className="feature-item"
                role="button"
                tabIndex={0}
                onClick={() => handleFeature(item)}
                onKeyDown={(e) => e.key === 'Enter' && handleFeature(item)}
              >
                <span className="feature-icon" style={{ background: `${item.color}12`, color: item.color }}>{item.icon}</span>
                <span className="feature-label">{item.label}</span>
              </div>
            </Grid.Item>
          ))}
        </Grid>
      </section>

      {/* 我的发布 Tab */}
      {user && (
        <section className="profile-my-products" data-od-id="profile-my-products">
          <div className="section-title" onClick={() => navigate('/my-publish')} role="button" tabIndex={0}>
            <span>我的发布</span>
            <RightOutlined style={{ fontSize: 12, color: 'var(--text-muted)' }} />
          </div>
          <Tabs activeKey={activeTab} onChange={setActiveTab}>
            <Tabs.Tab title="在售" key="on_sale" />
            <Tabs.Tab title="已售出" key="sold" />
            <Tabs.Tab title="已下架" key="off_shelf" />
          </Tabs>
          <div className="my-products-grid">
            {loading ? (
              <div style={{ textAlign: 'center', padding: 20, gridColumn: '1 / -1' }}>加载中...</div>
            ) : myProducts.length > 0 ? (
              myProducts.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))
            ) : (
              <div style={{ textAlign: 'center', padding: 20, gridColumn: '1 / -1', color: 'var(--text-muted)' }}>暂无商品</div>
            )}
          </div>
        </section>
      )}

      {/* 退出登录（入口在设置页） */}
      {user && (
        <div className="profile-logout">
          <Button block fill="outline" color="danger" onClick={() => navigate('/settings')}>
            账号设置与退出
          </Button>
        </div>
      )}
    </div>
  )
}
