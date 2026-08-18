import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import {
  BookOutlined,
  MobileOutlined,
  HomeOutlined,
  ShoppingOutlined,
  TrophyOutlined,
  SmileOutlined,
  CustomerServiceOutlined,
  EllipsisOutlined,
} from '@ant-design/icons'
import ProductCard from '../../components/ProductCard'
import { productApi, commonApi } from '../../services/api'
import type { Product, Category } from '../../types'
import './Category.css'

const iconMap: Record<string, React.ReactNode> = {
  BookOutlined: <BookOutlined />,
  MobileOutlined: <MobileOutlined />,
  HomeOutlined: <HomeOutlined />,
  ShoppingOutlined: <ShoppingOutlined />,
  TrophyOutlined: <TrophyOutlined />,
  SmileOutlined: <SmileOutlined />,
  CustomerServiceOutlined: <CustomerServiceOutlined />,
  EllipsisOutlined: <EllipsisOutlined />,
}

export default function CategoryPage() {
  const [searchParams] = useSearchParams()
  const [categories, setCategories] = useState<Category[]>([])
  const [products, setProducts] = useState<Product[]>([])
  const [activeCat, setActiveCat] = useState(searchParams.get('cat') || '')
  const [loading, setLoading] = useState(false)
  const seqRef = useRef(0)

  useEffect(() => {
    loadCategories()
  }, [])

  useEffect(() => {
    if (activeCat) {
      loadProducts()
    }
  }, [activeCat])

  const loadCategories = async () => {
    try {
      const res: any = await commonApi.getCategories()
      if (Array.isArray(res)) {
        setCategories(res)
        if (!activeCat && res.length > 0) {
          setActiveCat(res[0].id)
        }
      }
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }

  const loadProducts = async () => {
    const cur = ++seqRef.current // 序号保护：只接受最新请求的结果
    setLoading(true)
    try {
      const res: any = await productApi.getList({ categoryId: activeCat, pageSize: 100 })
      if (cur !== seqRef.current) return
      if (res?.list) {
        setProducts(res.list)
      }
    } catch (error) {
      if (cur !== seqRef.current) return
      console.error('加载商品失败:', error)
    } finally {
      if (cur === seqRef.current) setLoading(false)
    }
  }

  return (
    <div className="page-wrapper category-page" data-od-id="category-page">
      <header className="category-header">
        <h1>分类</h1>
      </header>
      <div className="category-body">
        {/* 左侧分类列表 */}
        <nav className="category-sidebar" aria-label="商品分类">
          {categories.map((cat) => (
            <div
              key={cat.id}
              className={`sidebar-item${activeCat === cat.id ? ' active' : ''}`}
              role="button"
              tabIndex={0}
              onClick={() => setActiveCat(cat.id)}
              onKeyDown={(e) => e.key === 'Enter' && setActiveCat(cat.id)}
              aria-current={activeCat === cat.id ? 'true' : undefined}
            >
              <span className="sidebar-icon">{iconMap[cat.icon]}</span>
              <span>{cat.name}</span>
            </div>
          ))}
        </nav>
        {/* 右侧商品网格 */}
        <main className="category-grid">
          {loading ? (
            <div className="empty-hint">加载中...</div>
          ) : products.length > 0 ? (
            products.map((p) => <ProductCard key={p.id} product={p} />)
          ) : (
            <div className="empty-hint">该分类暂无商品</div>
          )}
        </main>
      </div>
    </div>
  )
}
