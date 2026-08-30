import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Toast, Dialog } from 'antd-mobile'
import { adminApi, categoryApi } from '../../services/api'
import { formatDate, formatPrice, DEFAULT_PRODUCT_IMAGE } from '../../utils/format'
import type { Category } from '../../types'
import { PRODUCT_STATUS } from '../../constants'
import './ProductManagement.css'

interface ProductItem {
  id: string
  title: string
  price: number
  originalPrice: number
  coverImage: string
  productCondition: string
  location: string
  status: number
  viewCount: number
  sellerName: string
  sellerUsername: string
  categoryName: string
  createdAt: string
}

export default function ProductManagement() {
  const [searchParams] = useSearchParams()
  const [products, setProducts] = useState<ProductItem[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [loading, setLoading] = useState(true)
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize] = useState(10)
  const [keyword, setKeyword] = useState('')
  const [searchedKeyword, setSearchedKeyword] = useState('') // 已提交搜索的关键词（筛选/翻页用它，避免未提交输入污染）
  const [statusFilter, setStatusFilter] = useState<number | undefined>(
    searchParams.get('status') ? Number(searchParams.get('status')) : undefined
  )
  const [categoryFilter, setCategoryFilter] = useState<number | undefined>()
  const [actingId, setActingId] = useState<string | null>(null)
  const seqRef = useRef(0)

  useEffect(() => {
    loadCategories()
  }, [])

  useEffect(() => {
    loadProducts(pageNum, searchedKeyword)
  }, [pageNum, statusFilter, categoryFilter, searchedKeyword])

  const loadCategories = async () => {
    try {
      const data = await categoryApi.getAll()
      setCategories(data)
    } catch (error) {
      console.error('Failed to load categories:', error)
    }
  }

  const loadProducts = async (page: number = pageNum, kw: string = keyword) => {
    const cur = ++seqRef.current // 序号保护：翻页/筛选/搜索竞态时旧响应不覆盖新结果
    try {
      setLoading(true)
      const data = await adminApi.getProducts({
        keyword: kw || undefined,
        status: statusFilter,
        categoryId: categoryFilter,
        pageNum: page,
        pageSize,
      })
      if (cur !== seqRef.current) return
      setProducts(data.list)
      setTotal(data.total)
    } catch (error) {
      if (cur !== seqRef.current) return
      console.error('Failed to load products:', error)
    } finally {
      if (cur === seqRef.current) setLoading(false)
    }
  }

  const handleSearch = () => {
    // 提交当前关键词：翻页/筛选统一使用已提交值
    setSearchedKeyword(keyword)
    setPageNum(1)
    loadProducts(1, keyword)
  }

  const handleUpdateStatus = async (product: ProductItem, newStatus: number) => {
    if (actingId) return // 防连点
    const actionMap: Record<number, string> = {
      0: '上架',
      1: '标记为已售',
      2: '下架',
    }
    const action = actionMap[newStatus] || '修改状态'
    const confirmed = await Dialog.confirm({
      content: `确定要${action}商品 "${product.title}" 吗？`,
    })
    if (!confirmed) return
    setActingId(product.id)
    try {
      await adminApi.updateProductStatus(product.id, newStatus)
      Toast.show({ content: '操作成功', icon: 'success' })
      loadProducts(pageNum)
    } catch (error) {
      console.error('Failed to update product status:', error)
      Toast.show({ content: '操作失败', icon: 'fail' })
    } finally {
      setActingId(null)
    }
  }

  const handleDelete = async (product: ProductItem) => {
    if (actingId) return // 防连点
    const confirmed = await Dialog.confirm({
      content: `确定要删除商品 "${product.title}" 吗？此操作不可恢复。`,
    })
    if (!confirmed) return
    setActingId(product.id)
    try {
      await adminApi.deleteProduct(product.id)
      Toast.show({ content: '删除成功', icon: 'success' })
      loadProducts(pageNum)
    } catch (error) {
      console.error('Failed to delete product:', error)
      Toast.show({ content: '删除失败', icon: 'fail' })
    } finally {
      setActingId(null)
    }
  }

  return (
    <div className="product-management">
      <h1 className="page-title">商品管理</h1>

      <div className="toolbar">
        <div className="search-bar">
          <input
            type="text"
            placeholder="搜索商品标题"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          />
          <button onClick={handleSearch}>搜索</button>
        </div>
        <div className="filter-bar">
          <select
            value={statusFilter ?? ''}
            onChange={(e) => {
              setStatusFilter(e.target.value ? Number(e.target.value) : undefined)
              setPageNum(1)
            }}
          >
            <option value="">全部状态</option>
            <option value="0">在售</option>
            <option value="1">已售</option>
            <option value="2">已下架</option>
          </select>
          <select
            value={categoryFilter ?? ''}
            onChange={(e) => {
              setCategoryFilter(e.target.value ? Number(e.target.value) : undefined)
              setPageNum(1)
            }}
          >
            <option value="">全部分类</option>
            {categories.map((cat) => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="table-container">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>商品信息</th>
              <th>价格</th>
              <th>分类</th>
              <th>卖家</th>
              <th>浏览量</th>
              <th>状态</th>
              <th>发布时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={9} className="loading-cell">
                  加载中...
                </td>
              </tr>
            ) : products.length === 0 ? (
              <tr>
                <td colSpan={9} className="empty-cell">
                  暂无数据
                </td>
              </tr>
            ) : (
              products.map((product) => (
                <tr key={product.id}>
                  <td>{product.id}</td>
                  <td>
                    <div className="product-info">
                      <img
                        src={product.coverImage || DEFAULT_PRODUCT_IMAGE}
                        alt="cover"
                        className="product-cover"
                      />
                      <div className="product-detail">
                        <div className="product-title">{product.title}</div>
                        <div className="product-meta">
                          {product.productCondition && (
                            <span className="condition-tag">
                              {product.productCondition}
                            </span>
                          )}
                          <span className="location">{product.location}</span>
                        </div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <div className="price-cell">
                      <span className="current-price">
                        {formatPrice(product.price)}
                      </span>
                      {product.originalPrice && (
                        <span className="original-price">
                          {formatPrice(product.originalPrice)}
                        </span>
                      )}
                    </div>
                  </td>
                  <td>{product.categoryName || '-'}</td>
                  <td>
                    <div className="seller-info">
                      <span>{product.sellerName}</span>
                      <span className="seller-id">{product.sellerUsername}</span>
                    </div>
                  </td>
                  <td>{product.viewCount}</td>
                  <td>
                    <span
                      className={`status-tag ${PRODUCT_STATUS[product.status]?.className || ''}`}
                    >
                      {PRODUCT_STATUS[product.status]?.label || '未知'}
                    </span>
                  </td>
                  <td>{formatDate(product.createdAt)}</td>
                  <td>
                    <div className="action-buttons">
                      {product.status === 0 && (
                        <button
                          className="action-btn btn-warning"
                          onClick={() => handleUpdateStatus(product, 2)}
                        >
                          下架
                        </button>
                      )}
                      {product.status === 2 && (
                        <button
                          className="action-btn btn-success"
                          onClick={() => handleUpdateStatus(product, 0)}
                        >
                          上架
                        </button>
                      )}
                      <button
                        className="action-btn btn-danger"
                        onClick={() => handleDelete(product)}
                      >
                        删除
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {total > pageSize && (
        <div className="pagination">
          <button
            disabled={pageNum <= 1}
            onClick={() => setPageNum(pageNum - 1)}
          >
            上一页
          </button>
          <span className="page-info">
            第 {pageNum} 页 / 共 {Math.ceil(total / pageSize)} 页
          </span>
          <button
            disabled={pageNum >= Math.ceil(total / pageSize)}
            onClick={() => setPageNum(pageNum + 1)}
          >
            下一页
          </button>
        </div>
      )}
    </div>
  )
}
