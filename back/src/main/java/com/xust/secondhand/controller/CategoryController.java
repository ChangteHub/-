package com.xust.secondhand.controller;

import com.xust.secondhand.common.Result;
import com.xust.secondhand.service.CategoryService;
import com.xust.secondhand.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类控制器
 */
@Tag(name = "分类接口", description = "商品分类查询")
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "全部分类列表")
    @GetMapping("/list")
    public Result<List<CategoryVO>> getAllCategories() {
        List<CategoryVO> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
}
