package com.xust.secondhand.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户资料DTO
 */
@Data
public class UpdateUserRequest {

    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    private String avatar;

    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;

    @Size(max = 255, message = "个人简介长度不能超过255个字符")
    private String bio;
}
