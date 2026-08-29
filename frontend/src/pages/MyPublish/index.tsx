import { useState, useEffect, useRef } from 'react'
import { NavBar, Tabs, Empty } from 'antd-mobile'
import { useNavigate } from 'react-router-dom'
import { LeftOutlined } from '@ant-design/icons'
import { productApi } from '../../services/api'
import { useStore } from '../../stores/useStore'
import type { Product } from '../../types'
import ProductCard from '../../components/ProductCard'
import './MyPublish.css'

const statusMap: Record<string, { title: string; filter: string }> = {
  on_sale: { title: '在售', filter: 'on_sale' },
  sold: { title: '已售出', filter: 'sold' },
  off_shelf: { title: '已下架', filter: 'off_shelf' },
}

export default function MyPublishPage() {
  const navigate = useNavigate()
  const { user, showLogin, authChecked } = useStore()
  const [activeTab, setActiveTab] = useState('on_sale')
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const seqRef = useRef(0)

  useEffect(() => {
    if (!authChecked) return
    if (!user) {
      showLogin()
      return
    }
    loadProducts()
  }, [activeTab, user, authChecked])

  const loadProducts = async () => {
    const cur = ++seqRef.current // 序号保护：快速切换Tab时旧响应不覆盖新结果
    setLoading(true)
    try {
      const statusValue = activeTab === 'on_sale' ? 0 : activeTab === 'sold' ? 1 : 2
      const res: any = await productApi.getMy({ status: statusValue, pageSize: 100 })
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
    <div className="page-wrapper my-publish-page" data-od-id="my-publish-page">
      <NavBar
        onBack={() => navigate(-1)}
        backArrow={<LeftOutlined />}
        className="page-navbar"
      >
        我的发布
      </NavBar>

      <Tabs activeKey={activeTab} onChange={setActiveTab} className="my-publish-tabs">
        <Tabs.Tab title="在售" key="on_sale" />
        <Tabs.Tab title="已售出" key="sold" />
        <Tabs.Tab title="已下架" key="off_shelf" />
      </Tabs>

      <div className="my-publish-content">
        {loading ? (
          <div style={{ textAlign: 'center', padding: 40 }}>加载中...</div>
        ) : products.length > 0 ? (
          <div className="my-publish-grid">
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        ) : (
          <Empty
            description={`暂无${statusMap[activeTab]?.title || ''}商品`}
            className="my-publish-empty"
          />
        )}
      </div>
    </div>
  )
}
