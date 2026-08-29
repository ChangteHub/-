package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 搜索控制器
 */
@Tag(name = "搜索接口", description = "搜索历史、热门搜索")
@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Operation(summary = "获取搜索历史")
    @GetMapping("/history")
    public Result<List<String>> getHistory() {
        // 简化实现：返回空列表（可后续改为数据库存储）
        return Result.success(Collections.emptyList());
    }

    @Operation(summary = "清空搜索历史")
    @DeleteMapping("/history")
    public Result<Void> clearHistory() {
        return Result.success();
    }

    @Operation(summary = "删除单条搜索历史")
    @DeleteMapping("/history/{keyword}")
    public Result<Void> removeHistory(@PathVariable String keyword) {
        return Result.success();
    }

    @Operation(summary = "获取热门搜索")
    @GetMapping("/hot")
    public Result<List<String>> getHot() {
        // 返回热门搜索关键词（可后续改为数据库管理）
        return Result.success(Arrays.asList("教材", "iPad", "耳机", "台灯", "考研", "四六级"));
    }
}
