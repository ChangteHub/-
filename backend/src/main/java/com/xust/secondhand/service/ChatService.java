package com.xust.secondhand.service;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.request.CreateSessionRequest;
import com.xust.secondhand.dto.response.ChatMessageResponse;
import com.xust.secondhand.dto.response.ChatSessionResponse;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 创建或获取会话
     */
    ChatSessionResponse getOrCreateSession(Long userId, CreateSessionRequest dto);

    /**
     * 获取会话列表
     */
    List<ChatSessionResponse> getSessions(Long userId);

    /**
     * 获取单个会话详情
     */
    ChatSessionResponse getSession(Long userId, Long sessionId);

    /**
     * 获取会话消息列表
     */
    PageResult<ChatMessageResponse> getMessages(Long userId, Long sessionId, int pageNum, int pageSize);

    /**
     * 发送消息（持久化）
     */
    ChatMessageResponse saveMessage(Long senderId, Long sessionId, String content, Integer type);

    /**
     * 标记会话消息为已读
     */
    void markAsRead(Long userId, Long sessionId);
}
