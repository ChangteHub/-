package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import com.xust.secondhand.dto.request.LoginRequest;
import com.xust.secondhand.dto.request.RegisterRequest;
import com.xust.secondhand.service.UserService;
import com.xust.secondhand.security.UserContext;
import com.xust.secondhand.dto.response.LoginResponse;
import com.xust.secondhand.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证接口", description = "用户注册、登录、获取当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest dto) {
        UserResponse user = userService.register(dto);
        return Result.success(user);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest dto) {
        LoginResponse loginVO = userService.login(dto);
        return Result.success(loginVO);
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser() {
        Long userId = UserContext.getUserId();
        UserResponse user = userService.getCurrentUser(userId);
        return Result.success(user);
    }
}
