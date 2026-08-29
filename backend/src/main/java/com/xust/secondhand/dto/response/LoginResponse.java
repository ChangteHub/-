package com.xust.secondhand.dto.response;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginResponse {

    /** JWT Token */
    private String token;

    /** 用户信息 */
    private UserResponse user;

    public LoginResponse() {}

    public LoginResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
}
