package com.xust.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 实名认证提交DTO
 */
@Data
public class VerificationDTO {

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "学号不能为空")
    private String studentId;

    private String college;

    private Integer enrollYear;

    @NotBlank(message = "学生证照片不能为空")
    private String studentCardUrl;
}
