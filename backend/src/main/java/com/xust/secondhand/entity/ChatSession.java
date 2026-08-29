package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天会话实体
 */
@Data
@TableName("chat_session")
public class ChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联商品ID */
    private Long productId;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /** 最后一条消息摘要 */
    private String lastMessage;

    /** 最后消息时间 */
    private LocalDateTime lastMessageTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
