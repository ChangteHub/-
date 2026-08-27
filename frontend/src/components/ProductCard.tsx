import { useNavigate } from 'react-router-dom'
import { EyeOutlined } from '@ant-design/icons'
import type { Product } from '../types'
import { DEFAULT_PRODUCT_IMAGE, productImageFallback } from '../utils/format'
import './ProductCard.css'

interface Props {
  product: Product
}

export default function ProductCard({ product }: Props) {
  const navigate = useNavigate()

  return (
    <div
      className="product-card"
      data-od-id={`product-card-${product.id}`}
      onClick={() => navigate(`/product/${product.id}`)}
    >
      <div className="product-card-img">
        <img
          src={(product as any).coverImage || product.images?.[0] || DEFAULT_PRODUCT_IMAGE}
          alt={product.title}
          loading="lazy"
          onError={productImageFallback}
        />
        {product.productCondition && (
          <span className="product-card-condition">{product.productCondition}</span>
        )}
      </div>
      <div className="product-card-info">
        <h3 className="product-card-title">{product.title}</h3>
        <div className="product-card-price">
          <span className="price">
            <span className="price-symbol">¥</span>
            {Number(product.price).toFixed(2)}
          </span>
          {product.originalPrice && (
            <span className="price-original">¥{Number(product.originalPrice).toFixed(2)}</span>
          )}
        </div>
        <div className="product-card-meta">
          <span className="seller-name">{product.sellerName}</span>
          <span className="views">
            <EyeOutlined /> {product.viewCount}
          </span>
        </div>
      </div>
    </div>
  )
}
