package com.xust.secondhand.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xust.secondhand.dto.WsMessageDTO;
import com.xust.secondhand.entity.ChatSession;
import com.xust.secondhand.entity.User;
import com.xust.secondhand.mapper.ChatSessionMapper;
import com.xust.secondhand.mapper.UserMapper;
import com.xust.secondhand.service.ChatService;
import com.xust.secondhand.utils.JwtUtil;
import com.xust.secondhand.vo.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket聊天处理器
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtil jwtUtil;
    private final ChatService chatService;
    private final ChatSessionMapper sessionMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 在线用户会话映射: userId -> 该用户的所有WebSocket连接（支持多标签页/多端） */
    private static final Map<Long, Set<WebSocketSession>> ONLINE_USERS = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(JwtUtil jwtUtil, ChatService chatService, ChatSessionMapper sessionMapper, UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.chatService = chatService;
        this.sessionMapper = sessionMapper;
        this.userMapper = userMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从URL参数获取token
        String token = getTokenFromSession(session);
        if (token == null || !jwtUtil.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        // 与HTTP侧对齐：校验用户仍存在且未被禁用/删除
        User user = userMapper.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 1)) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        session.getAttributes().put("userId", userId);

        // 添加到在线用户列表（同一用户多标签页/多端连接共存）
        Set<WebSocketSession> userSessions =
                ONLINE_USERS.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());
        userSessions.add(session);
        log.info("用户 {} 已连接WebSocket，当前连接数 {}", userId, userSessions.size());

        // 标记该用户的所有未读消息为已读
        // 这里简化处理，实际应该遍历所有会话
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            removeSession(userId, session);
            log.info("用户 {} 已断开WebSocket", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = (Long) session.getAttributes().get("userId");
        if (senderId == null) {
            return;
        }

        try {
            // 解析消息
            WsMessageDTO dto = objectMapper.readValue(message.getPayload(), WsMessageDTO.class);

            // 保存消息到数据库
            ChatMessageVO savedMessage = chatService.saveMessage(senderId, dto.getSessionId(), dto.getContent(), dto.getType());

            // 获取会话信息，确定接收者
            ChatSession chatSession = sessionMapper.selectById(dto.getSessionId());
            if (chatSession == null) {
                return;
            }

            // 确定接收者ID
            Long receiverId = senderId.equals(chatSession.getBuyerId()) 
                    ? chatSession.getSellerId() 
                    : chatSession.getBuyerId();

            // 构建推送消息
            String pushMessage = objectMapper.writeValueAsString(savedMessage);

            // 推送给接收者（所有在线连接）
            Set<WebSocketSession> receiverSessions = ONLINE_USERS.get(receiverId);
            if (receiverSessions != null) {
                for (WebSocketSession s : receiverSessions) {
                    if (s.isOpen()) {
                        try {
                            s.sendMessage(new TextMessage(pushMessage));
                        } catch (IOException e) {
                            log.warn("推送消息失败，用户 {}", receiverId, e);
                        }
                    }
                }
                log.info("消息已推送给用户 {}", receiverId);
            }

            // 也推送给发送者（确认消息已保存）
            session.sendMessage(new TextMessage(pushMessage));

        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            session.sendMessage(new TextMessage("{\"error\":\"消息发送失败\"}"));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误", exception);
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            removeSession(userId, session);
        }
    }

    /**
     * 从在线用户映射中移除指定连接（仅移除当前session，不影响其他标签页）
     */
    private void removeSession(Long userId, WebSocketSession session) {
        ONLINE_USERS.computeIfPresent(userId, (uid, sessions) -> {
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    /**
     * 从WebSocket session中获取token
     */
    private String getTokenFromSession(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null) {
            Map<String, String> params = UriComponentsBuilder
                    .fromUriString("?" + query)
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();
            return params.get("token");
        }
        return null;
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        Set<WebSocketSession> sessions = ONLINE_USERS.get(userId);
        return sessions != null && sessions.stream().anyMatch(WebSocketSession::isOpen);
    }
}
