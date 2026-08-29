package com.xust.secondhand.controller;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.common.Result;
import com.xust.secondhand.dto.request.CreateSessionRequest;
import com.xust.secondhand.service.ChatService;
import com.xust.secondhand.security.UserContext;
import com.xust.secondhand.dto.response.ChatMessageResponse;
import com.xust.secondhand.dto.response.ChatSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器
 */
@Tag(name = "聊天接口", description = "聊天会话、消息管理")
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "创建或获取会话")
    @PostMapping("/session")
    public Result<ChatSessionResponse> getOrCreateSession(@Valid @RequestBody CreateSessionRequest dto) {
        Long userId = UserContext.getUserId();
        ChatSessionResponse session = chatService.getOrCreateSession(userId, dto);
        return Result.success(session);
    }

    @Operation(summary = "会话列表")
    @GetMapping("/sessions")
    public Result<List<ChatSessionResponse>> getSessions() {
        Long userId = UserContext.getUserId();
        List<ChatSessionResponse> sessions = chatService.getSessions(userId);
        return Result.success(sessions);
    }

    @Operation(summary = "获取单个会话详情")
    @GetMapping("/session/{id}")
    public Result<ChatSessionResponse> getSession(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        ChatSessionResponse session = chatService.getSession(userId, id);
        return Result.success(session);
    }

    @Operation(summary = "获取会话消息列表")
    @GetMapping("/messages/{sessionId}")
    public Result<PageResult<ChatMessageResponse>> getMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<ChatMessageResponse> result = chatService.getMessages(userId, sessionId, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/messages/{sessionId}/read")
    public Result<Void> markRead(@PathVariable Long sessionId) {
        Long userId = UserContext.getUserId();
        chatService.markAsRead(userId, sessionId);
        return Result.success();
    }
}
