import { useState, useRef, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Input, Button, Image, Toast } from 'antd-mobile'
import { LeftOutline } from 'antd-mobile-icons'
import { chatApi } from '../../services/api'
import { useStore } from '../../store/useStore'
import { DEFAULT_AVATAR, avatarFallback } from '../../utils/format'
import type { ChatMessageVO, ChatSessionVO } from '../../types'
import './ChatDetail.css'

export default function ChatDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user, showLogin, authChecked } = useStore()
  const [inputVal, setInputVal] = useState('')
  const [messages, setMessages] = useState<ChatMessageVO[]>([])
  const [sessionInfo, setSessionInfo] = useState<ChatSessionVO | null>(null)
  const [loading, setLoading] = useState(false)
  const listRef = useRef<HTMLDivElement>(null)
  const wsRef = useRef<WebSocket | null>(null)
  const reconnectTimerRef = useRef<number | null>(null)
  const reconnectAttemptRef = useRef(0)

  const loadSessionInfo = async () => {
    try {
      const res: any = await chatApi.getConversation(id!)
      setSessionInfo(res)
    } catch (error) {
      console.error('加载会话信息失败:', error)
    }
  }

  const loadMessages = async () => {
    setLoading(true)
    try {
      const res: any = await chatApi.getMessages(id!, { pageSize: 100 })
      if (res?.list) {
        setMessages(res.list)
      }
      // 进入会话即标记已读
      chatApi.markRead(id!).catch(() => {})
    } catch (error) {
      console.error('加载消息失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const connectWebSocket = useCallback(() => {
    const token = localStorage.getItem('token')
    if (!token) return

    if (wsRef.current && (wsRef.current.readyState === WebSocket.OPEN || wsRef.current.readyState === WebSocket.CONNECTING)) {
      return
    }

    const wsUrl = window.location.protocol === 'https:' 
      ? `wss://${window.location.host}/ws/chat?token=${token}`
      : `ws://${window.location.host}/ws/chat?token=${token}`
    const ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      reconnectAttemptRef.current = 0
    }

    ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        // 归一化 id 比较：后端 VO 字符串化后为 string，此处再兜底一次
        if (String(message.sessionId) === id) {
          setMessages(prev => {
            if (prev.some(m => String(m.id) === String(message.id))) return prev
            return [...prev, message]
          })
        }
      } catch (error) {
        console.error('解析消息失败:', error)
      }
    }

    ws.onerror = () => {}

    ws.onclose = () => {
      // 组件已卸载则不重连
      if (wsRef.current !== ws) return
      // 连续失败 5 次后停止重连，避免 token 失效后无限无效重试
      if (reconnectAttemptRef.current >= 5) {
        Toast.show({ content: '连接失败，请刷新页面重试', position: 'center' })
        return
      }
      // 指数退避重连：1s, 2s, 4s, 8s... 上限30s
      const delay = Math.min(1000 * Math.pow(2, reconnectAttemptRef.current), 30000)
      reconnectAttemptRef.current += 1
      if (reconnectTimerRef.current) window.clearTimeout(reconnectTimerRef.current)
      reconnectTimerRef.current = window.setTimeout(() => {
        connectWebSocket()
      }, delay)
    }

    wsRef.current = ws
  }, [id])

  useEffect(() => {
    if (!authChecked) return // 等待认证检查完成
    if (!user) {
      showLogin()
      return
    }
    if (id) {
      loadMessages()
      loadSessionInfo()
      connectWebSocket()
    }

    return () => {
      if (reconnectTimerRef.current) {
        window.clearTimeout(reconnectTimerRef.current)
        reconnectTimerRef.current = null
      }
      reconnectAttemptRef.current = 0
      if (wsRef.current) {
        wsRef.current.close()
        wsRef.current = null
      }
    }
  }, [id, connectWebSocket, user, authChecked])

  useEffect(() => {
    const el = listRef.current
    if (!el) return
    // 仅当滚动位置接近底部时才自动置底（用户上翻看历史时不打扰）
    const nearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 80
    if (nearBottom) {
      el.scrollTop = el.scrollHeight
    }
  }, [messages])

  const send = () => {
    const text = inputVal.trim()
    if (!text) return

    if (!wsRef.current || wsRef.current.readyState !== WebSocket.OPEN) {
      Toast.show({ content: '连接已断开，正在重连...', position: 'center' })
      connectWebSocket()
      return
    }

    const message = {
      sessionId: parseInt(id!),
      content: text,
      type: 0
    }

    wsRef.current.send(JSON.stringify(message))
    setInputVal('')
  }

  if (!id) {
    return (
      <div className="page-wrapper" style={{ padding: 40, textAlign: 'center' }}>
        <p>会话不存在</p>
        <Button onClick={() => navigate('/chat')}>返回消息列表</Button>
      </div>
    )
  }

  return (
    <div className="page-wrapper chat-detail-page" data-od-id="chat-detail-page">
      {/* 顶部 */}
      <div className="chat-detail-header">
        <button className="back-btn" onClick={() => navigate('/chat')}>
          <LeftOutline />
        </button>
        <span className="chat-title">{sessionInfo?.otherUserName || '聊天'}</span>
        <div style={{ width: 24 }} />
      </div>

      {/* 消息列表 */}
      <div className="message-list" ref={listRef}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: 20 }}>加载中...</div>
        ) : (
          messages.map((msg) => {
            const isMe = msg.senderId === user?.id
            return (
              <div key={msg.id} className={`message-row ${isMe ? 'me' : 'other'}`}>
                {!isMe && (
                  <img
                    src={msg.senderAvatar || DEFAULT_AVATAR}
                    alt=""
                    className="msg-avatar"
                    onError={avatarFallback}
                  />
                )}
                <div className={`message-bubble ${isMe ? 'bubble-me' : 'bubble-other'}`}>
                  {msg.type === 1 ? (
                    <Image
                      src={msg.content}
                      width={160}
                      style={{ borderRadius: 6 }}
                    />
                  ) : (
                    <span>{msg.content}</span>
                  )}
                </div>
                {isMe && (
                  <img
                    src={user?.avatar || DEFAULT_AVATAR}
                    alt=""
                    className="msg-avatar"
                    onError={avatarFallback}
                  />
                )}
              </div>
            )
          })
        )}
      </div>

      {/* 输入栏 */}
      <div className="chat-input-bar">
        <Input
          placeholder="输入消息..."
          value={inputVal}
          onChange={setInputVal}
          className="chat-input"
          onEnterPress={send}
        />
        <Button color="primary" size="small" className="send-btn" onClick={send}>
          发送
        </Button>
      </div>
    </div>
  )
}
