package com.xust.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xust.secondhand.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 物理删除指定用户名的【已逻辑删除】记录
     * 用于释放 uk_username 唯一索引：注销用户（deleted=1）后允许同名重新注册
     * 仅删除 deleted=1 的记录，不会误删正常用户
     */
    @Delete("DELETE FROM user WHERE username = #{username} AND deleted = 1")
    int deletePhysicalDeletedByUsername(@Param("username") String username);
}
