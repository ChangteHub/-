package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员操作日志实体
 */
@Data
@TableName("admin_log")
public class AdminLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作管理员ID */
    private Long adminId;

    /** 管理员用户名 */
    private String adminUsername;

    /** 操作类型 */
    private String action;

    /** 目标类型: user/product/verification/category */
    private String targetType;

    /** 目标ID */
    private String targetId;

    /** 操作详情 */
    private String detail;

    /** 操作时间 */
    private LocalDateTime createdAt;
}
