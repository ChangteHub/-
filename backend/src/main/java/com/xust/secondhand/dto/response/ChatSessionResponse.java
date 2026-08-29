package com.xust.secondhand.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天会话VO
 */
@Data
public class ChatSessionResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long productId;
    private String productTitle;
    private String productCoverImage;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long buyerId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sellerId;

    /** 对方用户信息 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;

    private String lastMessage;
    private LocalDateTime lastMessageTime;

    /** 未读消息数 */
    private Integer unreadCount;
}
