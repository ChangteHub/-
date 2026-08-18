package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import com.xust.secondhand.dto.LoginDTO;
import com.xust.secondhand.dto.RegisterDTO;
import com.xust.secondhand.service.UserService;
import com.xust.secondhand.utils.UserContext;
import com.xust.secondhand.vo.LoginVO;
import com.xust.secondhand.vo.UserVO;
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
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        UserVO user = userService.register(dto);
        return Result.success(user);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO loginVO = userService.login(dto);
        return Result.success(loginVO);
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        Long userId = UserContext.getUserId();
        UserVO user = userService.getCurrentUser(userId);
        return Result.success(user);
    }
}
