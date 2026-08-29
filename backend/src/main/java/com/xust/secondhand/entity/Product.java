package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 卖家用户ID */
    private Long sellerId;

    /** 分类ID */
    private Long categoryId;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 出售价格 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 封面图URL */
    private String coverImage;

    /** 商品成色 */
    @TableField("product_condition")
    private String productCondition;

    /** 交易地点 */
    private String location;

    /** 状态: 0在售 1已售出 2已下架 */
    private Integer status;

    /** 浏览量 */
    private Integer viewCount;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
