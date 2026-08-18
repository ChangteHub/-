package com.xust.secondhand.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品列表VO（简化版）
 */
@Data
public class ProductListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private String location;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createdAt;

    /** 卖家信息 */
    private String sellerName;
    private String sellerAvatar;
}
