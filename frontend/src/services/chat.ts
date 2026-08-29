import request from './request'
import type { ChatSessionVO, ChatMessageVO, PageParams, PageResult } from './types'

export const chatApi = {
  getConversations: (): Promise<ChatSessionVO[]> =>
    request.get('/chat/sessions'),
  getConversation: (id: string): Promise<ChatSessionVO> =>
    request.get(`/chat/session/${id}`),
  createConversation: (productId: string, targetUserId: string): Promise<ChatSessionVO> =>
    request.post('/chat/session', { productId, targetUserId }),
  getMessages: (sessionId: string, params?: PageParams): Promise<PageResult<ChatMessageVO>> =>
    request.get(`/chat/messages/${sessionId}`, { params }),
  markRead: (sessionId: string): Promise<void> =>
    request.put(`/chat/messages/${sessionId}/read`),
}
