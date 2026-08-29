package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 浏览历史实体
 */
@Data
@TableName("browsing_history")
public class BrowsingHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 浏览时间 */
    private LocalDateTime viewedAt;
}
