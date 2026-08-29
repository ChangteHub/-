package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 商品图片实体
 */
@Data
@TableName("product_image")
public class ProductImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 图片URL */
    private String imageUrl;

    /** 排序 */
    private Integer sort;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
