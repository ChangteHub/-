package com.xust.secondhand.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新商品状态DTO
 */
@Data
public class UpdateProductStatusRequest {

    /** 状态: 0在售 1已售出 2已下架 */
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 2, message = "状态值非法")
    private Integer status;
}
