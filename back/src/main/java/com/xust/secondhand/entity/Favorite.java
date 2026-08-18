package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 收藏实体
 */
@Data
@TableName("favorite")
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
