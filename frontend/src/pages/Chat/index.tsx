import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Badge } from 'antd-mobile'
import { chatApi } from '../../services/api'
import { useStore } from '../../stores/useStore'
import { formatChatTime, avatarFallback, DEFAULT_AVATAR } from '../../utils/format'
import type { ChatSessionVO } from '../../types'
import './Chat.css'

export default function ChatPage() {
  const navigate = useNavigate()
  const { user, showLogin, authChecked } = useStore()
  const [sessions, setSessions] = useState<ChatSessionVO[]>([])
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (!authChecked) return // 等待认证检查完成，避免冷启动误弹登录框
    if (!user) {
      // 未登录引导登录，避免401/403静默失败
      showLogin()
      return
    }
    loadSessions()
    // 会话列表 5s 轮询刷新（新消息/未读角标实时性，静默模式不闪加载态）
    const timer = setInterval(() => loadSessions(true), 5000)
    return () => clearInterval(timer)
  }, [user, authChecked])

  const loadSessions = async (silent = false) => {
    if (!silent) setLoading(true)
    try {
      const res: any = await chatApi.getConversations()
      if (Array.isArray(res)) {
        setSessions(res)
      }
    } catch (error) {
      console.error('加载会话列表失败:', error)
    } finally {
      if (!silent) setLoading(false)
    }
  }

  return (
    <div className="page-wrapper chat-page" data-od-id="chat-list-page">
      <div className="chat-header">
        <h1>消息</h1>
      </div>

      <div className="conversation-list">
        {loading ? (
          <div className="empty-hint">加载中...</div>
        ) : sessions.length > 0 ? (
          sessions.map((conv) => (
            <div
              key={conv.id}
              className="conversation-item"
              onClick={() => navigate(`/chat/${conv.id}`)}
            >
              <div className="conv-avatar-wrap">
                <img
                  src={conv.otherUserAvatar || DEFAULT_AVATAR}
                  alt=""
                  className="conv-avatar"
                  onError={avatarFallback}
                />
                {conv.unreadCount > 0 && (
                  <Badge content={conv.unreadCount} className="conv-badge" />
                )}
              </div>
              <div className="conv-body">
                <div className="conv-top">
                  <span className="conv-name">{conv.otherUserName}</span>
                  <span className="conv-time">{formatChatTime(conv.lastMessageTime)}</span>
                </div>
                <div className="conv-bottom">
                  <span className="conv-msg">{conv.lastMessage}</span>
                </div>
                {conv.productTitle && (
                  <div className="conv-product">[商品] {conv.productTitle}</div>
                )}
              </div>
            </div>
          ))
        ) : (
          <div className="empty-hint">暂无消息</div>
        )}
      </div>
    </div>
  )
}
