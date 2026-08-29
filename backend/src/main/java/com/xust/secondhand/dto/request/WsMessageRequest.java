package com.xust.secondhand.dto.request;

import lombok.Data;

/**
 * WebSocket消息DTO
 */
@Data
public class WsMessageRequest {

    /** 会话ID */
    private Long sessionId;

    /** 消息内容 */
    private String content;

    /** 消息类型: 0文字 1图片 */
    private Integer type;
}
