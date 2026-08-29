package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 发送者ID */
    private Long senderId;

    /** 消息内容 */
    private String content;

    /** 消息类型: 0文字 1图片 */
    private Integer type;

    /** 是否已读: 0未读 1已读 */
    private Integer isRead;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
