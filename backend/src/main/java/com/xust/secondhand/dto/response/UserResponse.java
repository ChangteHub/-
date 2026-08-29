package com.xust.secondhand.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
public class UserResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String school;
    private String studentId;
    private String bio;
    private Integer role;
    private LocalDateTime createdAt;
}
