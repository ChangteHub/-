import { useState, useEffect, useRef, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Swiper, Button, Tag, Toast } from 'antd-mobile'
import { HeartOutline, HeartFill, LeftOutline } from 'antd-mobile-icons'
import { productApi, favoriteApi, chatApi } from '../../services/api'
import { useStore } from '../../stores/useStore'
import { DEFAULT_AVATAR, avatarFallback } from '../../utils/format'
import type { ProductVO } from '../../types'
import './ProductDetail.css'

export default function ProductDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { requireLogin, user, addBrowsingHistory } = useStore()
  const [product, setProduct] = useState<ProductVO | null>(null)
  const [isFavorite, setIsFavorite] = useState(false)
  const [loading, setLoading] = useState(false)
  const heartRef = useRef<HTMLSpanElement>(null)
  const seqRef = useRef(0)

  useEffect(() => {
    if (id) {
      loadProduct()
      addBrowsingHistory(id)
    }
  }, [id])

  // 登录状态变化后刷新收藏状态（拆开依赖，避免user变化重复加载商品）
  useEffect(() => {
    if (id && user) {
      checkFavorite()
    }
  }, [id, user])

  const loadProduct = async () => {
    const cur = ++seqRef.current // 序号保护：详情页内切换商品时旧响应不覆盖新结果
    setLoading(true)
    try {
      const res: any = await productApi.getById(id!)
      if (cur !== seqRef.current) return
      setProduct(res)
    } catch (error) {
      if (cur !== seqRef.current) return
      console.error('加载商品失败:', error)
    } finally {
      if (cur === seqRef.current) setLoading(false)
    }
  }

  const checkFavorite = async () => {
    if (!user) return // 未登录不请求，避免401/403静默失败
    try {
      const res: any = await favoriteApi.check(id!)
      setIsFavorite(res === true)
    } catch {
      // 未登录时忽略
    }
  }

  const handleContact = async () => {
    if (!requireLogin()) return
    if (!product) return

    if (product.sellerId === user?.id) {
      Toast.show({ content: '这是你的商品，不能联系自己', position: 'center' })
      return
    }

    try {
      const res: any = await chatApi.createConversation(product.id, product.sellerId)
      if (res?.id) {
        navigate(`/chat/${res.id}`)
      }
    } catch {
      Toast.show({ content: '创建会话失败', position: 'center' })
    }
  }

  const handleCollect = useCallback(async () => {
    if (!requireLogin()) return
    if (!product) return

    try {
      if (isFavorite) {
        await favoriteApi.remove(product.id)
        setIsFavorite(false)
        Toast.show({ content: '已取消收藏', position: 'center' })
      } else {
        await favoriteApi.add(product.id)
        setIsFavorite(true)
        Toast.show({ content: '已收藏', position: 'center' })
      }

      // 心跳动画
      if (heartRef.current) {
        heartRef.current.classList.remove('heartbeat')
        void heartRef.current.offsetWidth
        heartRef.current.classList.add('heartbeat')
      }
    } catch {
      Toast.show({ content: '操作失败', position: 'center' })
    }
  }, [isFavorite, product])

  if (loading) {
    return (
      <div className="page-wrapper" style={{ padding: 40, textAlign: 'center' }}>
        加载中...
      </div>
    )
  }

  if (!product) {
    return (
      <div className="page-wrapper" style={{ padding: 40, textAlign: 'center' }}>
        <p style={{ marginBottom: 16, color: 'var(--text-muted)' }}>商品不存在</p>
        <Button color="primary" onClick={() => navigate('/')}>返回首页</Button>
      </div>
    )
  }

  return (
    <div className="page-wrapper detail-page" data-od-id="product-detail">
      {/* 图片轮播 */}
      <div className="detail-images">
        <Swiper autoplay loop>
          {product.images?.map((img: string, i: number) => (
            <Swiper.Item key={i}>
              <img src={img} alt={`${product.title} - 图片${i + 1}`} className="detail-img" />
            </Swiper.Item>
          ))}
        </Swiper>
        <button
          className="detail-back"
          onClick={() => navigate(-1)}
          aria-label="返回"
        >
          <LeftOutline />
        </button>
      </div>

      <div className="detail-body">
        {/* 价格区 */}
        <div className="detail-price-section">
          <span className="detail-price">
            <span className="price-symbol">¥</span>
            {product.price}
          </span>
          {product.originalPrice && (
            <span className="detail-original-price">原价 ¥{product.originalPrice}</span>
          )}
        </div>

        {/* 标题 */}
        <h1 className="detail-title">{product.title}</h1>

        {/* 标签 */}
        <div className="detail-tags">
          <Tag color="primary" fill="outline">
            {product.productCondition}
          </Tag>
          <Tag color="default" fill="outline">
            {product.categoryName || '其他'}
          </Tag>
        </div>

        {/* 描述 */}
        <div className="detail-desc">
          <h3>商品描述</h3>
          <p>{product.description}</p>
        </div>

        {/* 交易地点 */}
        <div className="detail-location">
          <span className="label">交易地点</span>
          <span>{product.location}</span>
        </div>

        {/* 卖家信息 */}
        <div className="seller-card" data-od-id="seller-card">
          <img
            src={product.sellerAvatar || DEFAULT_AVATAR}
            alt={product.sellerName || '卖家'}
            className="seller-avatar"
            onError={avatarFallback}
          />
          <div className="seller-info">
            <span className="seller-nickname">{product.sellerName || '匿名用户'}</span>
            <span className="seller-school">{product.sellerSchool || '西南科技大学'}</span>
          </div>
        </div>
      </div>

      {/* 底部操作栏 */}
      <div className="detail-footer" data-od-id="detail-footer">
        <button className="collect-btn" onClick={handleCollect} aria-label={isFavorite ? '取消收藏' : '收藏'}>
          <span ref={heartRef} className="collect-heart">
            {isFavorite ? (
              <HeartFill style={{ color: 'var(--danger)' }} />
            ) : (
              <HeartOutline />
            )}
          </span>
          <span>{isFavorite ? '已收藏' : '收藏'}</span>
        </button>
        <Button
          block
          color="primary"
          size="large"
          className="contact-btn"
          onClick={handleContact}
        >
          联系卖家
        </Button>
      </div>
    </div>
  )
}
