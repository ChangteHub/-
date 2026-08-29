package com.xust.secondhand.service;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.request.LoginRequest;
import com.xust.secondhand.dto.request.RegisterRequest;
import com.xust.secondhand.dto.request.UpdateUserRequest;
import com.xust.secondhand.dto.response.UserResponse;
import com.xust.secondhand.dto.response.LoginResponse;
import com.xust.secondhand.dto.response.ProductListResponse;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserResponse register(RegisterRequest dto);

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest dto);

    /**
     * 获取当前用户信息
     */
    UserResponse getCurrentUser(Long userId);

    /**
     * 更新用户资料
     */
    UserResponse updateProfile(Long userId, UpdateUserRequest dto);

    /**
     * 获取用户发布的商品
     */
    PageResult<ProductListResponse> getUserProducts(Long userId, Integer status, int pageNum, int pageSize);

    /**
     * 获取用户收藏的商品
     */
    PageResult<ProductListResponse> getUserFavorites(Long userId, int pageNum, int pageSize);
}
