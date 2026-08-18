import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { SearchOutline, BellOutline } from 'antd-mobile-icons'
import { Swiper, Grid, DotLoading, Toast } from 'antd-mobile'
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
import { useInView } from '../../hooks/useInView'
import { productApi, commonApi } from '../../services/api'
import type { Product, Category } from '../../types'
import './Home.css'

/** Scroll-reveal wrapper for product cards */
function RevealCard({ product, index }: { product: Product; index: number }) {
  const { ref, inView } = useInView()
  return (
    <div
      ref={ref}
      className={`waterfall-item reveal ${inView ? 'in-view' : ''}`}
      style={{ transitionDelay: `${(index % 4) * 60}ms` }}
    >
      <ProductCard product={product} />
    </div>
  )
}

/** Scroll-reveal wrapper for category icons */
function RevealCategory({ cat, index, icon, onClick }: {
  cat: Category; index: number; icon: React.ReactNode; onClick: () => void
}) {
  const { ref, inView } = useInView()
  return (
    <div
      ref={ref}
      className={`reveal-scale ${inView ? 'in-view' : ''}`}
      style={{ transitionDelay: `${index * 50}ms` }}
    >
      <div
        className="category-item"
        role="button"
        tabIndex={0}
        onClick={onClick}
        onKeyDown={(e) => e.key === 'Enter' && onClick()}
      >
        <span className="category-icon">{icon}</span>
        <span className="category-name">{cat.name}</span>
      </div>
    </div>
  )
}

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

/** 首页商品分页大小（loadProducts / loadMore 边界共用） */
const PAGE_SIZE = 20

/** 本地渐变 Banner 占位（内联 SVG，离线/内网可用，不依赖外部服务） */
const svgBanner = (id: string, from: string, to: string) =>
  `data:image/svg+xml;utf8,${encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="750" height="300"><defs><linearGradient id="${id}" x1="0" y1="0" x2="1" y2="1"><stop offset="0%" stop-color="${from}"/><stop offset="100%" stop-color="${to}"/></linearGradient></defs><rect width="750" height="300" fill="url(#${id})"/></svg>`
  )}`

const defaultBanners = [
  { id: 'b1', image: svgBanner('g1', '#1677ff', '#69b1ff'), title: '开学季二手好物' },
  { id: 'b2', image: svgBanner('g2', '#ff7a00', '#ffc53d'), title: '教材低价出' },
  { id: 'b3', image: svgBanner('g3', '#10b981', '#87e8de'), title: '数码好物捡漏' },
]

export default function HomePage() {
  const navigate = useNavigate()
  const [products, setProducts] = useState<Product[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [banners, setBanners] = useState(defaultBanners)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [total, setTotal] = useState(0)
  const [loadError, setLoadError] = useState(false)

  useEffect(() => {
    loadProducts()
    loadCategories()
    loadBanners()
  }, [])

  const loadProducts = async (pageNum = 1) => {
    setLoading(true)
    try {
      const res: any = await productApi.getList({ pageNum: pageNum, pageSize: PAGE_SIZE })
      if (res?.list) {
        setProducts(prev => pageNum === 1 ? res.list : [...prev, ...res.list])
        setPage(pageNum)
        setTotal(res.total ?? 0)
        setLoadError(false)
      }
    } catch (error) {
      console.error('加载商品失败:', error)
      setLoadError(true)
    } finally {
      setLoading(false)
    }
  }

  const loadCategories = async () => {
    try {
      const res: any = await commonApi.getCategories()
      if (Array.isArray(res)) {
        setCategories(res)
      }
    } catch (error) {
      console.error('加载分类失败:', error)
    }
  }

  const loadBanners = async () => {
    try {
      const res: any = await commonApi.getBanners()
      if (Array.isArray(res) && res.length > 0) {
        setBanners(res.map((b: any) => ({
          id: b.id,
          image: b.imageUrl,
          title: b.title
        })))
      }
    } catch (error) {
      console.error('加载轮播图失败:', error)
    }
  }

  const loadMore = useCallback(() => {
    if (loading || loadError) return
    // 已加载完：用 page*pageSize 与 total 比较（比 products.length 更稳，避免空页死循环）
    if (total > 0 && page * PAGE_SIZE >= total) return
    if (total === 0 && products.length === 0) return // 空库/无结果
    loadProducts(page + 1)
  }, [loading, loadError, page, products.length, total])

  return (
    <div className="page-wrapper home-page" data-od-id="home-page">
      {/* 顶部搜索栏 */}
      <header className="home-header" data-od-id="home-header">
        <div
          className="search-bar"
          role="searchbox"
          tabIndex={0}
          onClick={() => navigate('/search')}
          onKeyDown={(e) => e.key === 'Enter' && navigate('/search')}
        >
          <SearchOutline fontSize={16} />
          <span>搜索你想要的宝贝</span>
        </div>
        <button
          className="header-icon"
          onClick={() => navigate('/chat')}
          aria-label="消息通知"
        >
          <BellOutline fontSize={22} />
        </button>
      </header>

      {/* Banner 轮播 */}
      <section className="home-banner" data-od-id="home-banner" aria-label="活动轮播">
        <Swiper autoplay loop>
          {banners.map((b) => (
            <Swiper.Item key={b.id}>
              <div className="banner-item">
                <img src={b.image} alt={b.title} />
                <span className="banner-title">{b.title}</span>
              </div>
            </Swiper.Item>
          ))}
        </Swiper>
      </section>

      {/* 分类入口 */}
      <section className="home-categories" data-od-id="home-categories" aria-label="商品分类">
        <Grid columns={4} gap={8}>
          {categories.map((cat, i) => (
            <Grid.Item key={cat.id}>
              <RevealCategory
                cat={cat}
                index={i}
                icon={iconMap[cat.icon]}
                onClick={() => navigate(`/category?cat=${cat.id}`)}
              />
            </Grid.Item>
          ))}
        </Grid>
      </section>

      {/* 商品瀑布流 */}
      <section className="home-products" data-od-id="home-products" aria-label="商品列表">
        <div className="waterfall">
          {products.map((p, i) => (
            <RevealCard key={p.id} product={p} index={i} />
          ))}
        </div>
        <div className="load-more" onClick={loadError ? () => loadProducts() : loadMore}>
          {loading ? <DotLoading color="primary" /> : loadError ? '加载失败，点击重试' : total === 0 && products.length === 0 ? '暂无商品' : page * PAGE_SIZE >= total && total > 0 ? '没有更多了' : '上拉加载更多'}
        </div>
      </section>

      {/* 悬浮发布按钮 */}
      <button
        className="fab-publish"
        data-od-id="fab-publish"
        onClick={() => navigate('/publish')}
        aria-label="发布商品"
      >
        +
      </button>
    </div>
  )
}
