package com.xust.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.AdminLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员操作日志Mapper
 */
@Mapper
public interface AdminLogRepository extends BaseMapper<AdminLog> {
}
