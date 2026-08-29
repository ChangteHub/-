package com.xust.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper
 */
@Mapper
public interface CategoryRepository extends BaseMapper<Category> {
}
