package com.xust.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实名认证实体
 */
@Data
@TableName("verification")
public class Verification {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String realName;
    private String studentId;
    private String college;
    private Integer enrollYear;
    private String studentCardUrl;
    /** 状态: 0待审核 1已通过 2已拒绝 */
    private Integer status;
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
