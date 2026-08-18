package com.xust.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 浏览量原子+1（避免读改写丢更新）
     */
    @Update("UPDATE product SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementViewCount(@Param("id") Long id);
}
