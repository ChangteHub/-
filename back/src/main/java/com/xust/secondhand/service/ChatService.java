package com.xust.secondhand.service;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.CreateSessionDTO;
import com.xust.secondhand.vo.ChatMessageVO;
import com.xust.secondhand.vo.ChatSessionVO;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 创建或获取会话
     */
    ChatSessionVO getOrCreateSession(Long userId, CreateSessionDTO dto);

    /**
     * 获取会话列表
     */
    List<ChatSessionVO> getSessions(Long userId);

    /**
     * 获取单个会话详情
     */
    ChatSessionVO getSession(Long userId, Long sessionId);

    /**
     * 获取会话消息列表
     */
    PageResult<ChatMessageVO> getMessages(Long userId, Long sessionId, int pageNum, int pageSize);

    /**
     * 发送消息（持久化）
     */
    ChatMessageVO saveMessage(Long senderId, Long sessionId, String content, Integer type);

    /**
     * 标记会话消息为已读
     */
    void markAsRead(Long userId, Long sessionId);
}
