package com.xust.secondhand.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品详情VO
 */
@Data
public class ProductResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private String sellerSchool;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private String productCondition;
    private String location;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 商品图片列表 */
    private List<String> images;

    /** 是否已收藏 */
    private Boolean isFavorite;
}
