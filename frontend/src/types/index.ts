/** 商品信息 */
export interface Product {
  id: string
  title: string
  price: number
  originalPrice?: number
  description: string
  images: string[]
  category: string
  productCondition: string
  location: string
  sellerId: string
  sellerName: string
  sellerAvatar: string
  sellerSchool: string
  viewCount: number
  createdAt: string
  status: number  // 0在售 1已售出 2已下架
}

/** 商品详情VO */
export interface ProductVO {
  id: string
  title: string
  price: number
  originalPrice?: number
  description: string
  images: string[]
  category: string
  categoryName?: string
  productCondition: string
  location: string
  sellerId: string
  sellerName: string
  sellerAvatar: string
  sellerSchool?: string
  viewCount: number
  createdAt: string
  status: number  // 0在售 1已售出 2已下架
  isFavorite?: boolean
}

/** 商品列表VO */
export interface ProductListVO {
  id: string
  title: string
  price: number
  originalPrice?: number
  coverImage: string
  location: string
  status: number
  viewCount: number
  createdAt: string
  sellerName: string
  sellerAvatar: string
}

/** 分类 */
export interface Category {
  id: string
  name: string
  icon: string
  sort?: number
  status?: number
  createdAt?: string
}

/** 聊天会话 */
export interface Conversation {
  id: string
  otherUserId: string
  otherUserName: string
  otherUserAvatar: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
  productId?: string
  productTitle?: string
  productImage?: string
}

/** 聊天会话VO */
export interface ChatSessionVO {
  id: string
  productId?: string
  productTitle?: string
  productCoverImage?: string
  buyerId: string
  sellerId: string
  otherUserId: string
  otherUserName: string
  otherUserAvatar: string
  lastMessage?: string
  lastMessageTime?: string
  unreadCount: number
}

/** 聊天消息 */
export interface Message {
  id: string
  conversationId: string
  senderId: string
  content: string
  type: 'text' | 'image'
  createdAt: string
}

/** 聊天消息VO */
export interface ChatMessageVO {
  id: string
  sessionId: string
  senderId: string
  senderName?: string
  senderAvatar?: string
  content: string
  type: number
  isRead: number
  createdAt: string
}

/** 用户 */
export interface User {
  id: string
  nickname: string
  avatar: string
  school: string
  phone: string
  username?: string
  studentId?: string
  bio?: string
  role?: number  // 0普通用户 1管理员
  createdAt?: string
}

/** 搜索历史 */
export interface SearchHistory {
  keyword: string
  time: string
}

/** 浏览记录 */
export interface BrowsingRecord {
  productId: string
  viewedAt: string
}

/** 帮助中心条目 */
export interface HelpItem {
  id: string
  question: string
  answer: string
  category: string
}
