package com.xust.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
