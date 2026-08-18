package com.xust.secondhand.dto;

import lombok.Data;

/**
 * WebSocket消息DTO
 */
@Data
public class WsMessageDTO {

    /** 会话ID */
    private Long sessionId;

    /** 消息内容 */
    private String content;

    /** 消息类型: 0文字 1图片 */
    private Integer type;
}
