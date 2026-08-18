import { useState, useEffect, useCallback, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { Input, Tag } from 'antd-mobile'
import { SearchOutline, CloseOutline } from 'antd-mobile-icons'
import ProductCard from '../../components/ProductCard'
import { useInView } from '../../hooks/useInView'
import { productApi, searchApi } from '../../services/api'
import type { Product } from '../../types'
import './Search.css'

type SortType = 'default' | 'price_asc' | 'price_desc' | 'newest'

export default function SearchPage() {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [searched, setSearched] = useState(false)
  const [history, setHistory] = useState<string[]>([])
  const [hotSearches, setHotSearches] = useState<string[]>([])
  const [results, setResults] = useState<Product[]>([])
  const [sort, setSort] = useState<SortType>('default')
  const [loading, setLoading] = useState(false)
  const seqRef = useRef(0)

  useEffect(() => {
    loadHistory()
    loadHotSearches()
  }, [])

  const HISTORY_KEY = 'search_history' // 搜索历史存 localStorage（后端接口为空实现，本地存储更合理）

  const loadHistory = () => {
    try {
      const raw = localStorage.getItem(HISTORY_KEY)
      setHistory(raw ? JSON.parse(raw) : [])
    } catch {
      setHistory([])
    }
  }

  const loadHotSearches = async () => {
    try {
      const res: any = await searchApi.getHot()
      if (Array.isArray(res)) {
        setHotSearches(res)
      }
    } catch (error) {
      console.error('加载热门搜索失败:', error)
    }
  }

  const doSearch = useCallback(async (kw?: string, sortOverride?: SortType) => {
    const q = kw || keyword
    const s = sortOverride ?? sort
    if (!q.trim()) return
    const cur = ++seqRef.current // 序号保护：连续搜索时旧响应不覆盖新结果
    setKeyword(q)
    setSearched(true)
    setLoading(true)

    try {
      let sortParam = 'newest'
      if (s === 'price_asc') sortParam = 'price_asc'
      if (s === 'price_desc') sortParam = 'price_desc'

      const res: any = await productApi.getList({ keyword: q, sort: sortParam, pageSize: 50 })
      if (cur !== seqRef.current) return
      if (res?.list) {
        setResults(res.list)
      }
      // 搜索成功后写入本地历史（去重、最多10条）
      setHistory(prev => {
        const next = [q, ...prev.filter((h) => h !== q)].slice(0, 10)
        localStorage.setItem(HISTORY_KEY, JSON.stringify(next))
        return next
      })
    } catch (error) {
      if (cur !== seqRef.current) return
      console.error('搜索失败:', error)
    } finally {
      if (cur === seqRef.current) setLoading(false)
    }
  }, [keyword, sort])

  const clearHistory = () => {
    localStorage.removeItem(HISTORY_KEY)
    setHistory([])
  }

  const removeHistory = (kw: string) => {
    setHistory(prev => {
      const next = prev.filter((h) => h !== kw) // 函数式更新，避免快速连删丢更新
      localStorage.setItem(HISTORY_KEY, JSON.stringify(next))
      return next
    })
  }

  const sortOptions: { key: SortType; label: string }[] = [
    { key: 'default', label: '综合' },
    { key: 'price_asc', label: '价格↑' },
    { key: 'price_desc', label: '价格↓' },
    { key: 'newest', label: '最新' },
  ]

  return (
    <div className="page-wrapper search-page" data-od-id="search-page">
      {/* 搜索栏 */}
      <div className="search-header">
        <div className="search-input-wrap">
          <SearchOutline fontSize={16} />
          <Input
            placeholder="搜索你想要的宝贝"
            value={keyword}
            onChange={setKeyword}
            onEnterPress={() => doSearch()}
            autoFocus
          />
        </div>
        <button className="search-cancel" onClick={() => navigate(-1)}>
          取消
        </button>
      </div>

      {!searched ? (
        <div className="search-body">
          {/* 搜索历史 */}
          {history.length > 0 && (
            <div className="search-section">
              <div className="section-header">
                <h3>搜索历史</h3>
                <button onClick={clearHistory}>清空</button>
              </div>
              <div className="tag-list">
                {history.map((h) => (
                  <span key={h} className="history-tag">
                    <span onClick={() => doSearch(h)}>{h}</span>
                    <CloseOutline
                      fontSize={10}
                      onClick={(e) => {
                        e.stopPropagation()
                        removeHistory(h)
                      }}
                    />
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* 热门搜索 */}
          {hotSearches.length > 0 && (
            <div className="search-section">
              <div className="section-header">
                <h3>热门搜索</h3>
              </div>
              <div className="tag-list">
                {hotSearches.map((h) => (
                  <Tag
                    key={h}
                    round
                    color="default"
                    fill="outline"
                    onClick={() => doSearch(h)}
                    style={{ cursor: 'pointer' }}
                  >
                    {h}
                  </Tag>
                ))}
              </div>
            </div>
          )}
        </div>
      ) : (
        <div className="search-results">
          {/* 排序 */}
          <div className="sort-bar">
            {sortOptions.map((s) => (
              <button
                key={s.key}
                className={`sort-btn ${sort === s.key ? 'active' : ''}`}
                onClick={() => {
                  setSort(s.key)
                  doSearch(keyword, s.key)
                }}
              >
                {s.label}
              </button>
            ))}
          </div>

          {/* 结果列表 */}
          <div className="result-grid">
            {loading ? (
              <div className="empty-hint">搜索中...</div>
            ) : results.length > 0 ? (
              results.map((p, i) => (
                <RevealResult key={p.id} product={p} index={i} />
              ))
            ) : (
              <div className="empty-hint">没有找到相关商品</div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

function RevealResult({ product, index }: { product: Product; index: number }) {
  const { ref, inView } = useInView()
  return (
    <div
      ref={ref}
      className={`reveal ${inView ? 'in-view' : ''}`}
      style={{ transitionDelay: `${(index % 4) * 60}ms` }}
    >
      <ProductCard product={product} />
    </div>
  )
}
