package com.xust.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏Mapper
 */
@Mapper
public interface FavoriteRepository extends BaseMapper<Favorite> {
}
