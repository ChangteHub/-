import { useState, useEffect } from 'react'
import { NavBar, Empty } from 'antd-mobile'
import { useNavigate } from 'react-router-dom'
import { LeftOutlined } from '@ant-design/icons'
import { useStore } from '../../stores/useStore'
import { favoriteApi } from '../../services/api'
import ProductCard from '../../components/ProductCard'
import type { Product } from '../../types'
import './Favorites.css'

export default function FavoritesPage() {
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
    loadFavorites()
  }, [user, authChecked])

  const loadFavorites = async () => {
    setLoading(true)
    try {
      const res: any = await favoriteApi.getList({ pageSize: 100 })
      if (res?.list) {
        setProducts(res.list)
      }
    } catch (error) {
      console.error('加载收藏失败:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-wrapper favorites-page" data-od-id="favorites-page">
      <NavBar
        onBack={() => navigate(-1)}
        backArrow={<LeftOutlined />}
        className="page-navbar"
      >
        我的收藏
      </NavBar>

      <div className="favorites-content">
        {loading ? (
          <div style={{ textAlign: 'center', padding: 40 }}>加载中...</div>
        ) : products.length > 0 ? (
          <div className="favorites-grid">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        ) : (
          <Empty
            description="还没有收藏商品"
            className="favorites-empty"
          />
        )}
      </div>
    </div>
  )
}
