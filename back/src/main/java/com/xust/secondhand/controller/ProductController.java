package com.xust.secondhand.controller;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.common.Result;
import com.xust.secondhand.dto.ProductDTO;
import com.xust.secondhand.dto.UpdateProductStatusDTO;
import com.xust.secondhand.service.ProductService;
import com.xust.secondhand.utils.UserContext;
import com.xust.secondhand.vo.ProductListVO;
import com.xust.secondhand.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@Tag(name = "商品接口", description = "商品发布、编辑、查询、删除等")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "发布商品")
    @PostMapping
    public Result<ProductVO> createProduct(@Valid @RequestBody ProductDTO dto) {
        Long userId = UserContext.getUserId();
        ProductVO product = productService.createProduct(userId, dto);
        return Result.success(product);
    }

    @Operation(summary = "商品列表")
    @GetMapping("/list")
    public Result<PageResult<ProductListVO>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long currentUserId = null;
        try {
            currentUserId = UserContext.getUserId();
        } catch (Exception e) {
            // 未登录也可以查看商品列表
        }
        PageResult<ProductListVO> result = productService.getProductList(categoryId, keyword, sort, pageNum, pageSize, currentUserId);
        return Result.success(result);
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        Long currentUserId = null;
        try {
            currentUserId = UserContext.getUserId();
        } catch (Exception e) {
            // 未登录也可以查看商品详情
        }
        ProductVO product = productService.getProductDetail(id, currentUserId);
        return Result.success(product);
    }

    @Operation(summary = "编辑商品")
    @PutMapping("/{id}")
    public Result<ProductVO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        Long userId = UserContext.getUserId();
        ProductVO product = productService.updateProduct(userId, id, dto);
        return Result.success(product);
    }

    @Operation(summary = "修改商品状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @Valid @RequestBody UpdateProductStatusDTO dto) {
        Long userId = UserContext.getUserId();
        productService.updateProductStatus(userId, id, dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        productService.deleteProduct(userId, id);
        return Result.success();
    }
}
