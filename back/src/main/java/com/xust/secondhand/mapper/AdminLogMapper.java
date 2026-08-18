package com.xust.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.AdminLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员操作日志Mapper
 */
@Mapper
public interface AdminLogMapper extends BaseMapper<AdminLog> {
}
