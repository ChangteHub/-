package com.xust.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.BrowsingHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 浏览历史Mapper
 */
@Mapper
public interface BrowsingHistoryMapper extends BaseMapper<BrowsingHistory> {
}
