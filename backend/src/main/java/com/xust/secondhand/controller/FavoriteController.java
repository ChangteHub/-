package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import com.xust.secondhand.service.FavoriteService;
import com.xust.secondhand.security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏控制器
 */
@Tag(name = "收藏接口", description = "商品收藏、取消收藏")
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "添加收藏")
    @PostMapping("/{productId}")
    public Result<Void> addFavorite(@PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        favoriteService.addFavorite(userId, productId);
        return Result.success();
    }

    @Operation(summary = "取消收藏")
    @DeleteMapping("/{productId}")
    public Result<Void> removeFavorite(@PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        favoriteService.removeFavorite(userId, productId);
        return Result.success();
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check/{productId}")
    public Result<Boolean> checkFavorite(@PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        boolean isFavorite = favoriteService.isFavorite(userId, productId);
        return Result.success(isFavorite);
    }
}
