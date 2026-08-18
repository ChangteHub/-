package com.xust.secondhand.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    /** JWT Token */
    private String token;

    /** 用户信息 */
    private UserVO user;

    public LoginVO() {}

    public LoginVO(String token, UserVO user) {
        this.token = token;
        this.user = user;
    }
}
