package com.xust.secondhand.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建会话DTO
 */
@Data
public class CreateSessionRequest {

    /** 商品ID */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 对方用户ID */
    @NotNull(message = "对方用户ID不能为空")
    private Long targetUserId;
}
