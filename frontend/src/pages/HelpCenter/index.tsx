import { useState, useEffect } from 'react'
import { NavBar, SearchBar, Collapse, Tag, Toast } from 'antd-mobile'
import { useNavigate } from 'react-router-dom'
import { LeftOutlined, CustomerServiceOutlined } from '@ant-design/icons'
import { commonApi } from '../../services/api'
import './HelpCenter.css'

interface HelpItem {
  id: string
  question: string
  answer: string
  category: string
}

const categories = ['全部', '发布相关', '交易相关', '账号相关', '其他']

export default function HelpCenterPage() {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')
  const [activeCategory, setActiveCategory] = useState('全部')
  const [helpData, setHelpData] = useState<HelpItem[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    loadHelpData()
  }, [])

  const loadHelpData = async () => {
    setLoading(true)
    try {
      const res: any = await commonApi.getHelp()
      if (Array.isArray(res)) {
        setHelpData(res)
      }
    } catch (error) {
      console.error('加载帮助数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const filtered = helpData.filter((item) => {
    const matchKeyword = !keyword || item.question.includes(keyword) || item.answer.includes(keyword)
    const matchCategory = activeCategory === '全部' || item.category === activeCategory
    return matchKeyword && matchCategory
  })

  return (
    <div className="page-wrapper help-page" data-od-id="help-page">
      <NavBar
        onBack={() => navigate(-1)}
        backArrow={<LeftOutlined />}
        className="page-navbar"
      >
        帮助中心
      </NavBar>

      <div className="help-search">
        <SearchBar
          placeholder="搜索常见问题"
          value={keyword}
          onChange={setKeyword}
          className="help-search-bar"
        />
      </div>

      <div className="help-categories">
        {categories.map((cat) => (
          <Tag
            key={cat}
            round
            color={activeCategory === cat ? 'primary' : 'default'}
            fill={activeCategory === cat ? 'solid' : 'outline'}
            onClick={() => setActiveCategory(cat)}
            className="help-cat-tag"
          >
            {cat}
          </Tag>
        ))}
      </div>

      <div className="help-list">
        {loading ? (
          <div style={{ textAlign: 'center', padding: 40 }}>加载中...</div>
        ) : filtered.length > 0 ? (
          <Collapse accordion className="help-collapse">
            {filtered.map((item) => (
              <Collapse.Panel
                key={item.id}
                title={item.question}
                className="help-panel"
              >
                <p className="help-answer">{item.answer}</p>
              </Collapse.Panel>
            ))}
          </Collapse>
        ) : (
          <div className="help-empty">
            <p>没有找到相关问题</p>
            <p className="help-empty-hint">试试其他关键词或分类</p>
          </div>
        )}
      </div>

      <div className="help-contact">
        <div className="help-contact-inner">
          <CustomerServiceOutlined className="help-contact-icon" />
          <div className="help-contact-text">
            <span className="help-contact-title">没有找到答案？</span>
            <span className="help-contact-desc">联系客服获取帮助</span>
          </div>
          <Tag
            color="primary"
            fill="outline"
            round
            onClick={() => Toast.show({ content: '客服邮箱：swust-trade@example.com', position: 'center' })}
          >
            联系客服
          </Tag>
        </div>
      </div>
    </div>
  )
}
