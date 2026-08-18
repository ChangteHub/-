package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.CreateSessionDTO;
import com.xust.secondhand.entity.*;
import com.xust.secondhand.mapper.*;
import com.xust.secondhand.service.ChatService;
import com.xust.secondhand.vo.ChatMessageVO;
import com.xust.secondhand.vo.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天服务实现类
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ChatSessionVO getOrCreateSession(Long userId, CreateSessionDTO dto) {
        Long productId = dto.getProductId();
        Long targetUserId = dto.getTargetUserId();

        // 确定买家和卖家
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }

        // 校验目标用户
        if (targetUserId == null) {
            throw BusinessException.badRequest("对方用户ID不能为空");
        }
        if (targetUserId.equals(userId)) {
            throw BusinessException.badRequest("不能与自己创建会话");
        }
        if (userMapper.selectById(targetUserId) == null) {
            throw BusinessException.notFound("对方用户不存在");
        }

        Long buyerId, sellerId;
        if (userId.equals(product.getSellerId())) {
            // 当前用户是卖家，对方是买家
            sellerId = userId;
            buyerId = targetUserId;
        } else {
            // 当前用户是买家，只能与商品卖家建立会话
            if (!targetUserId.equals(product.getSellerId())) {
                throw BusinessException.badRequest("只能与商品卖家发起会话");
            }
            buyerId = userId;
            sellerId = product.getSellerId();
        }

        // 查找是否已存在会话
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getProductId, productId)
               .eq(ChatSession::getBuyerId, buyerId)
               .eq(ChatSession::getSellerId, sellerId);
        ChatSession session = sessionMapper.selectOne(wrapper);

        if (session == null) {
            // 创建新会话
            session = new ChatSession();
            session.setProductId(productId);
            session.setBuyerId(buyerId);
            session.setSellerId(sellerId);
            try {
                sessionMapper.insert(session);
            } catch (DuplicateKeyException e) {
                // 并发创建同一会话时唯一索引冲突，重新查询
                session = sessionMapper.selectOne(wrapper);
                if (session == null) {
                    throw e;
                }
            }
        }

        return convertToSessionVO(session, userId);
    }

    @Override
    public ChatSessionVO getSession(Long userId, Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BusinessException.notFound("会话不存在");
        }
        if (!session.getBuyerId().equals(userId) && !session.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("无权访问此会话");
        }
        return convertToSessionVO(session, userId);
    }

    @Override
    public List<ChatSessionVO> getSessions(Long userId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getBuyerId, userId)
               .or()
               .eq(ChatSession::getSellerId, userId)
               .orderByDesc(ChatSession::getLastMessageTime);

        List<ChatSession> sessions = sessionMapper.selectList(wrapper);

        return sessions.stream()
                .map(s -> convertToSessionVO(s, userId))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ChatMessageVO> getMessages(Long userId, Long sessionId, int pageNum, int pageSize) {
        // 验证用户是否是会话参与者
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BusinessException.notFound("会话不存在");
        }

        if (!session.getBuyerId().equals(userId) && !session.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("无权访问此会话");
        }

        // 查询消息（按时间正序）
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByAsc(ChatMessage::getCreatedAt);

        Page<ChatMessage> result = messageMapper.selectPage(page, wrapper);

        List<ChatMessageVO> list = result.getRecords().stream()
                .map(this::convertToMessageVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageNum, pageSize, list);
    }

    @Override
    @Transactional
    public ChatMessageVO saveMessage(Long senderId, Long sessionId, String content, Integer type) {
        // 验证用户是否是会话参与者
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BusinessException.notFound("会话不存在");
        }
        if (!session.getBuyerId().equals(senderId) && !session.getSellerId().equals(senderId)) {
            throw BusinessException.forbidden("无权发送消息");
        }

        // 校验消息内容
        if (content == null || content.trim().isEmpty()) {
            throw BusinessException.badRequest("消息内容不能为空");
        }

        // 保存消息
        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setType(type != null ? type : 0);
        message.setIsRead(0);
        messageMapper.insert(message);

        // 更新会话最后消息
        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChatSession::getId, sessionId)
                     .set(ChatSession::getLastMessage, content.length() > 50 ? content.substring(0, 50) + "..." : content)
                     .set(ChatSession::getLastMessageTime, LocalDateTime.now());
        sessionMapper.update(null, updateWrapper);

        return convertToMessageVO(message);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long sessionId) {
        // 校验用户是否是会话参与者
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw BusinessException.notFound("会话不存在");
        }
        if (!session.getBuyerId().equals(userId) && !session.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("无权操作此会话");
        }

        LambdaUpdateWrapper<ChatMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .ne(ChatMessage::getSenderId, userId)
               .eq(ChatMessage::getIsRead, 0)
               .set(ChatMessage::getIsRead, 1);
        messageMapper.update(null, wrapper);
    }

    /**
     * 转换为会话VO
     */
    private ChatSessionVO convertToSessionVO(ChatSession session, Long currentUserId) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setId(session.getId());
        vo.setProductId(session.getProductId());
        vo.setBuyerId(session.getBuyerId());
        vo.setSellerId(session.getSellerId());
        vo.setLastMessage(session.getLastMessage());
        vo.setLastMessageTime(session.getLastMessageTime());

        // 获取商品信息
        Product product = productMapper.selectById(session.getProductId());
        if (product != null) {
            vo.setProductTitle(product.getTitle());
            vo.setProductCoverImage(product.getCoverImage());
        }

        // 确定对方用户
        Long otherUserId;
        if (currentUserId.equals(session.getBuyerId())) {
            otherUserId = session.getSellerId();
        } else {
            otherUserId = session.getBuyerId();
        }
        vo.setOtherUserId(otherUserId);

        // 获取对方用户信息
        User otherUser = userMapper.selectById(otherUserId);
        if (otherUser != null) {
            vo.setOtherUserName(otherUser.getNickname());
            vo.setOtherUserAvatar(otherUser.getAvatar());
        }

        // 计算未读消息数
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, session.getId())
               .ne(ChatMessage::getSenderId, currentUserId)
               .eq(ChatMessage::getIsRead, 0);
        Long unreadCount = messageMapper.selectCount(wrapper);
        vo.setUnreadCount(unreadCount.intValue());

        return vo;
    }

    /**
     * 转换为消息VO
     */
    private ChatMessageVO convertToMessageVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(message.getId());
        vo.setSessionId(message.getSessionId());
        vo.setSenderId(message.getSenderId());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setIsRead(message.getIsRead());
        vo.setCreatedAt(message.getCreatedAt());

        // 获取发送者信息
        User sender = userMapper.selectById(message.getSenderId());
        if (sender != null) {
            vo.setSenderName(sender.getNickname());
            vo.setSenderAvatar(sender.getAvatar());
        }

        return vo;
    }
}
