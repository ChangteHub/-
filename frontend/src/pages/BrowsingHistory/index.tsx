import { useState, useEffect } from 'react'
import { NavBar, Empty, Dialog } from 'antd-mobile'
import { useNavigate } from 'react-router-dom'
import { LeftOutlined, DeleteOutlined } from '@ant-design/icons'
import { historyApi } from '../../services/api'
import { useStore } from '../../stores/useStore'
import ProductCard from '../../components/ProductCard'
import type { Product } from '../../types'
import './BrowsingHistory.css'

export default function BrowsingHistoryPage() {
  const navigate = useNavigate()
  const { user, showLogin, authChecked } = useStore()
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!authChecked) return
    if (!user) {
      showLogin()
      return
    }
    loadHistory()
  }, [user, authChecked])

  const loadHistory = async () => {
    setLoading(true)
    try {
      const res: any = await historyApi.getList({ pageSize: 100 })
      if (res?.list) {
        setProducts(res.list)
      }
    } catch (error) {
      console.error('加载浏览历史失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleClear = async () => {
    const confirmed = await Dialog.confirm({
      content: '确定清空所有浏览记录吗？',
    })
    if (confirmed) {
      try {
        await historyApi.clear()
        setProducts([])
      } catch (error) {
        console.error('清空历史失败:', error)
      }
    }
  }

  return (
    <div className="page-wrapper history-page" data-od-id="history-page">
      <NavBar
        onBack={() => navigate(-1)}
        backArrow={<LeftOutlined />}
        className="page-navbar"
        right={
          products.length > 0 ? (
            <DeleteOutlined
              className="history-clear-btn"
              onClick={handleClear}
              aria-label="清空浏览记录"
            />
          ) : null
        }
      >
        浏览历史
      </NavBar>

      <div className="history-content">
        {loading ? (
          <div style={{ textAlign: 'center', padding: 40 }}>加载中...</div>
        ) : products.length > 0 ? (
          <div className="history-grid">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        ) : (
          <Empty
            description="还没有浏览记录"
            className="history-empty"
          />
        )}
      </div>
    </div>
  )
}
