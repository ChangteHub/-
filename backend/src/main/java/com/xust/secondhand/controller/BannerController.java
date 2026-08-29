package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Banner控制器
 */
@Tag(name = "Banner接口", description = "首页轮播图")
@RestController
@RequestMapping("/api/banners")
public class BannerController {

    @Operation(summary = "获取Banner列表")
    @GetMapping
    public Result<List<Map<String, Object>>> getBanners() {
        // 返回静态Banner数据（可后续改为数据库管理）
        // 图片使用可访问的占位图，避免本地资源404
        List<Map<String, Object>> banners = Arrays.asList(
            createBanner(1L, "开学季二手教材特惠", "https://picsum.photos/seed/banner1/750/300", "/product/list?categoryId=1"),
            createBanner(2L, "数码好物低价出", "https://picsum.photos/seed/banner2/750/300", "/product/list?categoryId=2"),
            createBanner(3L, "毕业季清仓大甩卖", "https://picsum.photos/seed/banner3/750/300", "/product/list")
        );
        return Result.success(banners);
    }

    private Map<String, Object> createBanner(Long id, String title, String imageUrl, String link) {
        Map<String, Object> banner = new HashMap<>();
        banner.put("id", id);
        banner.put("title", title);
        banner.put("imageUrl", imageUrl);
        banner.put("link", link);
        return banner;
    }
}
