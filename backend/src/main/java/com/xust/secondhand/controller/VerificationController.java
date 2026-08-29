package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import com.xust.secondhand.dto.request.VerificationRequest;
import com.xust.secondhand.service.VerificationService;
import com.xust.secondhand.security.UserContext;
import com.xust.secondhand.dto.response.VerificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 实名认证控制器
 */
@Tag(name = "实名认证接口", description = "实名认证提交与查询")
@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    @Operation(summary = "提交实名认证")
    @PostMapping
    public Result<Void> submit(@Valid @RequestBody VerificationRequest dto) {
        Long userId = UserContext.getUserId();
        verificationService.submit(userId, dto);
        return Result.success();
    }

    @Operation(summary = "查询认证状态")
    @GetMapping("/status")
    public Result<VerificationResponse> getStatus() {
        Long userId = UserContext.getUserId();
        VerificationResponse vo = verificationService.getStatus(userId);
        return Result.success(vo);
    }
}
