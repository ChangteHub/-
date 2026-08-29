package com.xust.secondhand.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布/编辑商品DTO
 */
@Data
public class ProductSaveRequest {

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "标题长度不能超过100个字符")
    private String title;

    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 封面图URL */
    private String coverImage;

    /** 交易地点 */
    private String location;

    /** 商品成色 */
    private String productCondition;

    /** 商品图片URL列表 */
    private List<String> images;
}
