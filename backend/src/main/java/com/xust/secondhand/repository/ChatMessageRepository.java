package com.xust.secondhand.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageRepository extends BaseMapper<ChatMessage> {
}
