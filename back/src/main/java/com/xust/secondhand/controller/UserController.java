package com.xust.secondhand.controller;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.common.Result;
import com.xust.secondhand.dto.UpdateUserDTO;
import com.xust.secondhand.service.UserService;
import com.xust.secondhand.utils.UserContext;
import com.xust.secondhand.vo.ProductListVO;
import com.xust.secondhand.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户接口", description = "用户资料管理、我的发布、我的收藏")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "更新个人资料")
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateUserDTO dto) {
        Long userId = UserContext.getUserId();
        UserVO user = userService.updateProfile(userId, dto);
        return Result.success(user);
    }

    @Operation(summary = "我发布的商品")
    @GetMapping("/products")
    public Result<PageResult<ProductListVO>> getUserProducts(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<ProductListVO> result = userService.getUserProducts(userId, status, pageNum, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "我的收藏")
    @GetMapping("/favorites")
    public Result<PageResult<ProductListVO>> getUserFavorites(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<ProductListVO> result = userService.getUserFavorites(userId, pageNum, pageSize);
        return Result.success(result);
    }
}
