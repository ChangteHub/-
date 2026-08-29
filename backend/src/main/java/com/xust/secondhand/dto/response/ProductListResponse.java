package com.xust.secondhand.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表VO（简化版）
 */
@Data
public class ProductListResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private String location;
    /** 商品成色（列表卡片角标展示用） */
    private String productCondition;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createdAt;

    /** 卖家信息 */
    private String sellerName;
    private String sellerAvatar;
}
