package com.xust.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天会话Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
