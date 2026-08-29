package com.xust.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.BrowsingHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 浏览历史Mapper
 */
@Mapper
public interface BrowsingHistoryRepository extends BaseMapper<BrowsingHistory> {
}
