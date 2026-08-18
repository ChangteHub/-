package com.xust.secondhand.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息VO
 */
@Data
public class ChatMessageVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sessionId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private Integer type;
    private Integer isRead;
    private LocalDateTime createdAt;
}
