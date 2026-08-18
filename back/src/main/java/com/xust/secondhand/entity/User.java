package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名/学号 */
    private String username;

    /** 密码(BCrypt加密) */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 手机号 */
    private String phone;

    /** 学校 */
    private String school;

    /** 学号 */
    private String studentId;

    /** 个人简介 */
    private String bio;

    /** 状态: 0正常 1禁用 */
    private Integer status;

    /** 角色: 0普通用户 1管理员 */
    private Integer role;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
