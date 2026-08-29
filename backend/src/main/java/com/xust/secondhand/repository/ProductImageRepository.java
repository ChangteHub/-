package com.xust.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品图片Mapper
 */
@Mapper
public interface ProductImageRepository extends BaseMapper<ProductImage> {
}
