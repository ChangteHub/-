package com.xust.secondhand.service;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.*;
import com.xust.secondhand.vo.LoginVO;
import com.xust.secondhand.vo.ProductListVO;
import com.xust.secondhand.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    UserVO register(RegisterDTO dto);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO dto);

    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);

    /**
     * 更新用户资料
     */
    UserVO updateProfile(Long userId, UpdateUserDTO dto);

    /**
     * 获取用户发布的商品
     */
    PageResult<ProductListVO> getUserProducts(Long userId, Integer status, int pageNum, int pageSize);

    /**
     * 获取用户收藏的商品
     */
    PageResult<ProductListVO> getUserFavorites(Long userId, int pageNum, int pageSize);
}
