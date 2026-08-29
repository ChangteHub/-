package com.xust.secondhand.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 分类VO
 */
@Data
public class CategoryResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
}
