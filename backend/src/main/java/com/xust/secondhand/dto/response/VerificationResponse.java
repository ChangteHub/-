package com.xust.secondhand.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实名认证状态VO
 */
@Data
public class VerificationResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String realName;
    private String studentId;
    private String college;
    private Integer enrollYear;
    private String studentCardUrl;
    /** 状态: none/pending/approved/rejected */
    private String status;
    private String rejectReason;
    private LocalDateTime createdAt;
}
