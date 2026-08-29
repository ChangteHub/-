import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { Input, TextArea, Picker, Button, Toast, Image } from 'antd-mobile'
import { CloseOutline, LeftOutline } from 'antd-mobile-icons'
import { productApi, commonApi } from '../../services/api'
import { useStore } from '../../stores/useStore'
import type { Category } from '../../types'
import './Publish.css'

const conditionOptions = ['全新', '九成新', '八成新', '七成新及以下'].map((v) => ({
  label: v,
  value: v,
}))

export default function PublishPage() {
  const navigate = useNavigate()
  const { requireLogin } = useStore()
  const [categories, setCategories] = useState<Category[]>([])
  const [title, setTitle] = useState('')
  const [category, setCategory] = useState<string>('')
  const [catVisible, setCatVisible] = useState(false)
  const [price, setPrice] = useState('')
  const [originalPrice, setOriginalPrice] = useState('')
  const [description, setDescription] = useState('')
  const [location, setLocation] = useState('西南科技大学')
  const [productCondition, setProductCondition] = useState<string>('')
  const [condVisible, setCondVisible] = useState(false)
  const [images, setImages] = useState<string[]>([])
  const [submitting, setSubmitting] = useState(false)
  const [uploading, setUploading] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    loadCategories()
  }, [])

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

  const categoryColumns = categories.map((c) => ({ label: c.name, value: c.id }))

  const addImage = () => {
    if (images.length >= 9) {
      Toast.show({ content: '最多上传9张图片', position: 'center' })
      return
    }
    fileInputRef.current?.click()
  }

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    setUploading(true)
    try {
      const res = await commonApi.uploadImage(file)
      if (res?.url) {
        setImages([...images, res.url])
      }
    } catch (error) {
      Toast.show({ content: '图片上传失败', position: 'center' })
    } finally {
      setUploading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  const removeImage = (index: number) => {
    setImages(prev => prev.filter((_, i) => i !== index)) // 函数式更新
  }

  const handleSubmit = async () => {
    if (!requireLogin()) return
    if (!title.trim()) {
      Toast.show({ content: '请输入商品标题', position: 'center' })
      return
    }
    if (!category) {
      Toast.show({ content: '请选择分类', position: 'center' })
      return
    }
    if (!price) {
      Toast.show({ content: '请输入出售价格', position: 'center' })
      return
    }
    const priceNum = parseFloat(price)
    if (isNaN(priceNum) || priceNum <= 0) {
      Toast.show({ content: '价格必须大于0', position: 'center' })
      return
    }
    if (originalPrice && (isNaN(parseFloat(originalPrice)) || parseFloat(originalPrice) <= 0)) {
      Toast.show({ content: '原价必须大于0', position: 'center' })
      return
    }
    if (!productCondition) {
      Toast.show({ content: '请选择成色', position: 'center' })
      return
    }

    setSubmitting(true)
    try {
      await productApi.create({
        title: title.trim(),
        categoryId: Number(category),
        price: priceNum,
        originalPrice: originalPrice ? parseFloat(originalPrice) : undefined,
        description,
        location,
        productCondition,
        images,
        coverImage: images[0] || '',
      })
      Toast.show({ content: '发布成功', position: 'center' })
      setTimeout(() => navigate('/'), 800)
    } catch (error) {
      Toast.show({ content: '发布失败', position: 'center' })
    } finally {
      setSubmitting(false)
    }
  }

  const selectedCatLabel = categoryColumns.find((c) => c.value === category)?.label || '请选择分类'

  return (
    <div className="page-wrapper publish-page" data-od-id="publish-page">
      <input
        type="file"
        ref={fileInputRef}
        accept="image/*"
        style={{ display: 'none' }}
        onChange={handleImageUpload}
      />
      <header className="publish-header">
        <button className="back-btn" onClick={() => navigate(-1)} aria-label="返回">
          <LeftOutline />
        </button>
        <h1>发布商品</h1>
        <div style={{ width: 32 }} />
      </header>

      <div className="publish-form">
        {/* 图片上传 */}
        <div className="form-section">
          <div className="image-grid">
            {images.map((img, i) => (
              <div key={i} className="image-thumb">
                <Image src={img} width={72} height={72} fit="cover" style={{ borderRadius: 10 }} />
                <button className="remove-img" onClick={() => removeImage(i)} aria-label={`删除图片${i + 1}`}>
                  <CloseOutline />
                </button>
              </div>
            ))}
            {images.length < 9 && (
              <div
                className="image-add"
                role="button"
                tabIndex={0}
                onClick={addImage}
                onKeyDown={(e) => e.key === 'Enter' && addImage()}
                style={{ opacity: uploading ? 0.5 : 1, pointerEvents: uploading ? 'none' : 'auto' }}
              >
                <span>+</span>
                <span className="add-text">{images.length}/9</span>
              </div>
            )}
          </div>
        </div>

        {/* 标题 */}
        <div className="form-row">
          <label htmlFor="publish-title">商品标题</label>
          <Input id="publish-title" placeholder="请输入商品标题" value={title} onChange={setTitle} />
        </div>

        {/* 分类 */}
        <div className="form-row" onClick={() => setCatVisible(true)}>
          <label>分类</label>
          <div className={`form-select${category ? '' : ' placeholder'}`}>
            {selectedCatLabel}
          </div>
        </div>
        <Picker
          columns={[categoryColumns]}
          visible={catVisible}
          onClose={() => setCatVisible(false)}
          onConfirm={(val) => setCategory(val[0] as string)}
        />

        {/* 成色 */}
        <div className="form-row" onClick={() => setCondVisible(true)}>
          <label>商品成色</label>
          <div className={`form-select${productCondition ? '' : ' placeholder'}`}>
            {productCondition || '请选择成色'}
          </div>
        </div>
        <Picker
          columns={[conditionOptions]}
          visible={condVisible}
          onClose={() => setCondVisible(false)}
          onConfirm={(val) => setProductCondition(val[0] as string)}
        />

        {/* 价格 */}
        <div className="form-row">
          <label htmlFor="publish-price">出售价格</label>
          <Input
            id="publish-price"
            placeholder="¥ 请输入价格"
            value={price}
            onChange={setPrice}
            type="number"
          />
        </div>

        {/* 原价 */}
        <div className="form-row">
          <label htmlFor="publish-original-price">原价（选填）</label>
          <Input
            id="publish-original-price"
            placeholder="¥ 请输入原价"
            value={originalPrice}
            onChange={setOriginalPrice}
            type="number"
          />
        </div>

        {/* 描述 */}
        <div className="form-row">
          <label htmlFor="publish-desc">商品描述</label>
          <TextArea
            id="publish-desc"
            placeholder="请描述商品的详细信息"
            value={description}
            onChange={setDescription}
            rows={4}
            maxLength={500}
            showCount
          />
        </div>

        {/* 交易地点 */}
        <div className="form-row">
          <label htmlFor="publish-location">交易地点</label>
          <Input id="publish-location" placeholder="请输入交易地点" value={location} onChange={setLocation} />
        </div>
      </div>

      {/* 底部发布按钮 */}
      <div className="publish-footer">
        <Button block color="primary" size="large" onClick={handleSubmit} loading={submitting}>
          发布
        </Button>
      </div>
    </div>
  )
}
