package com.xust.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.common.Result;
import com.xust.secondhand.entity.*;
import com.xust.secondhand.mapper.*;
import com.xust.secondhand.utils.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 */
@Slf4j
@Tag(name = "管理员接口", description = "管理员专用接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final VerificationMapper verificationMapper;
    private final AdminLogMapper adminLogMapper;

    // ==================== 仪表盘 ====================

    @Operation(summary = "获取统计数据")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户统计
        stats.put("totalUsers", userMapper.selectCount(null));
        LambdaQueryWrapper<User> activeUserWrapper = new LambdaQueryWrapper<>();
        activeUserWrapper.eq(User::getStatus, 0);
        stats.put("activeUsers", userMapper.selectCount(activeUserWrapper));
        
        // 商品统计
        stats.put("totalProducts", productMapper.selectCount(null));
        LambdaQueryWrapper<Product> onSaleWrapper = new LambdaQueryWrapper<>();
        onSaleWrapper.eq(Product::getStatus, 0);
        stats.put("onSaleProducts", productMapper.selectCount(onSaleWrapper));
        LambdaQueryWrapper<Product> soldWrapper = new LambdaQueryWrapper<>();
        soldWrapper.eq(Product::getStatus, 1);
        stats.put("soldProducts", productMapper.selectCount(soldWrapper));
        
        // 认证统计
        stats.put("totalVerifications", verificationMapper.selectCount(null));
        LambdaQueryWrapper<Verification> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Verification::getStatus, 0);
        stats.put("pendingVerifications", verificationMapper.selectCount(pendingWrapper));
        
        // 分类统计
        stats.put("totalCategories", categoryMapper.selectCount(null));
        
        // 今日新增用户
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.ge(User::getCreatedAt, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        stats.put("todayNewUsers", userMapper.selectCount(todayUserWrapper));
        
        // 今日新增商品
        LambdaQueryWrapper<Product> todayProductWrapper = new LambdaQueryWrapper<>();
        todayProductWrapper.ge(Product::getCreatedAt, LocalDateTime.now().withHour(0).withMinute(0).withSecond(0));
        stats.put("todayNewProducts", productMapper.selectCount(todayProductWrapper));

        return Result.success(stats);
    }

    // ==================== 用户管理 ====================

    @Operation(summary = "用户列表")
    @GetMapping("/users")
    public Result<PageResult<Map<String, Object>>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getStudentId, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        
        Page<User> result = userMapper.selectPage(page, wrapper);
        
        List<Map<String, Object>> list = result.getRecords().stream()
                .map(this::convertToUserAdminVO)
                .toList();
        
        return Result.success(PageResult.of(result.getTotal(), pageNum, pageSize, list));
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return Result.success(convertToUserAdminVO(user));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        // 不能禁用管理员
        if (user.getRole() == 1) {
            throw BusinessException.badRequest("不能操作管理员账号");
        }
        user.setStatus(request.getStatus());
        userMapper.updateById(user);
        logAction("update_user_status", "user", String.valueOf(id),
                request.getStatus() == 1 ? "禁用用户" : "启用用户");
        return Result.success();
    }

    // ==================== 商品管理 ====================

    @Operation(summary = "商品列表")
    @GetMapping("/products")
    public Result<PageResult<Map<String, Object>>> getProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(Product::getCreatedAt);
        
        Page<Product> result = productMapper.selectPage(page, wrapper);
        
        List<Map<String, Object>> list = result.getRecords().stream()
                .map(this::convertToProductAdminVO)
                .toList();
        
        return Result.success(PageResult.of(result.getTotal(), pageNum, pageSize, list));
    }

    @Operation(summary = "修改商品状态")
    @PutMapping("/products/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }
        product.setStatus(request.getStatus());
        productMapper.updateById(product);
        String statusText = request.getStatus() == 0 ? "上架" : request.getStatus() == 1 ? "标记已售" : "下架";
        logAction("update_product_status", "product", String.valueOf(id), statusText + "：" + product.getTitle());
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }
        // 先清理商品图片，避免孤儿数据
        LambdaQueryWrapper<ProductImage> imgWrapper = new LambdaQueryWrapper<>();
        imgWrapper.eq(ProductImage::getProductId, id);
        productImageMapper.delete(imgWrapper);
        productMapper.deleteById(id);
        logAction("delete_product", "product", String.valueOf(id), "删除商品：" + product.getTitle());
        return Result.success();
    }

    // ==================== 实名认证管理 ====================

    @Operation(summary = "认证列表")
    @GetMapping("/verifications")
    public Result<PageResult<Map<String, Object>>> getVerificationList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        Page<Verification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Verification> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            wrapper.eq(Verification::getStatus, status);
        }
        wrapper.orderByDesc(Verification::getCreatedAt);
        
        Page<Verification> result = verificationMapper.selectPage(page, wrapper);
        
        List<Map<String, Object>> list = result.getRecords().stream()
                .map(this::convertToVerificationAdminVO)
                .toList();
        
        return Result.success(PageResult.of(result.getTotal(), pageNum, pageSize, list));
    }

    @Operation(summary = "审核认证")
    @PutMapping("/verifications/{id}/review")
    public Result<Void> reviewVerification(@PathVariable Long id, @Valid @RequestBody ReviewVerificationRequest request) {
        Verification verification = verificationMapper.selectById(id);
        if (verification == null) {
            throw BusinessException.notFound("认证记录不存在");
        }
        if (verification.getStatus() != 0) {
            throw BusinessException.badRequest("该认证已被审核");
        }
        // 拒绝时必须填写原因
        if (request.getStatus() == 2 && (request.getRejectReason() == null || request.getRejectReason().trim().isEmpty())) {
            throw BusinessException.badRequest("拒绝时必须填写拒绝原因");
        }
        
        verification.setStatus(request.getStatus());
        if (request.getStatus() == 2 && request.getRejectReason() != null) {
            verification.setRejectReason(request.getRejectReason());
        }
        verification.setUpdatedAt(LocalDateTime.now());
        verificationMapper.updateById(verification);
        logAction("review_verification", "verification", String.valueOf(id),
                request.getStatus() == 1 ? "审核通过" : "审核拒绝：" + request.getRejectReason());
        
        return Result.success();
    }

    // ==================== 分类管理 ====================

    @Operation(summary = "分类列表")
    @GetMapping("/categories")
    public Result<List<Category>> getCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        return Result.success(categoryMapper.selectList(wrapper));
    }

    @Operation(summary = "添加分类")
    @PostMapping("/categories")
    public Result<Void> addCategory(@Valid @RequestBody CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setSort(request.getSort() != null ? request.getSort() : 0);
        category.setStatus(0);
        categoryMapper.insert(category);
        logAction("add_category", "category", String.valueOf(category.getId()), "添加分类：" + category.getName());
        return Result.success();
    }

    @Operation(summary = "修改分类")
    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }
        if (request.getSort() != null) {
            category.setSort(request.getSort());
        }
        categoryMapper.updateById(category);
        logAction("update_category", "category", String.valueOf(id), "修改分类：" + category.getName());
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        // 检查分类下是否有商品
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, id);
        if (productMapper.selectCount(wrapper) > 0) {
            throw BusinessException.badRequest("该分类下存在商品，无法删除");
        }
        categoryMapper.deleteById(id);
        logAction("delete_category", "category", String.valueOf(id), "删除分类：" + category.getName());
        return Result.success();
    }

    @Operation(summary = "修改分类状态")
    @PutMapping("/categories/{id}/status")
    public Result<Void> updateCategoryStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw BusinessException.notFound("分类不存在");
        }
        category.setStatus(request.getStatus());
        categoryMapper.updateById(category);
        logAction("update_category_status", "category", String.valueOf(id),
                request.getStatus() == 0 ? "启用分类" : "禁用分类");
        return Result.success();
    }

    // ==================== 审计日志 ====================

    /**
     * 记录管理员操作日志（低频写操作，失败不影响主流程）
     */
    private void logAction(String action, String targetType, String targetId, String detail) {
        try {
            Long adminId = UserContext.getUserId();
            if (adminId == null) {
                return;
            }
            AdminLog log = new AdminLog();
            log.setAdminId(adminId);
            User admin = userMapper.selectById(adminId);
            log.setAdminUsername(admin != null ? admin.getUsername() : null);
            log.setAction(action);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setDetail(detail);
            adminLogMapper.insert(log);
        } catch (Exception e) {
            // 审计失败不阻断业务
            log.error("写入管理员操作日志失败", e);
        }
    }

    // ==================== 转换方法 ====================

    private Map<String, Object> convertToUserAdminVO(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", String.valueOf(user.getId()));
        vo.put("username", user.getUsername());
        vo.put("nickname", user.getNickname());
        vo.put("avatar", user.getAvatar());
        vo.put("phone", user.getPhone());
        vo.put("school", user.getSchool());
        vo.put("studentId", user.getStudentId());
        vo.put("bio", user.getBio());
        vo.put("status", user.getStatus());
        vo.put("role", user.getRole());
        vo.put("createdAt", user.getCreatedAt());
        
        // 统计用户商品数
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.eq(Product::getSellerId, user.getId());
        vo.put("productCount", productMapper.selectCount(productWrapper));
        
        return vo;
    }

    private Map<String, Object> convertToProductAdminVO(Product product) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", String.valueOf(product.getId()));
        vo.put("title", product.getTitle());
        vo.put("price", product.getPrice());
        vo.put("originalPrice", product.getOriginalPrice());
        vo.put("coverImage", product.getCoverImage());
        vo.put("productCondition", product.getProductCondition());
        vo.put("location", product.getLocation());
        vo.put("status", product.getStatus());
        vo.put("viewCount", product.getViewCount());
        vo.put("createdAt", product.getCreatedAt());
        
        // 获取卖家信息
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.put("sellerName", seller.getNickname());
            vo.put("sellerUsername", seller.getUsername());
        }
        
        // 获取分类信息
        Category category = categoryMapper.selectById(product.getCategoryId());
        if (category != null) {
            vo.put("categoryName", category.getName());
        }
        
        return vo;
    }

    private Map<String, Object> convertToVerificationAdminVO(Verification verification) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", String.valueOf(verification.getId()));
        vo.put("userId", String.valueOf(verification.getUserId()));
        vo.put("realName", verification.getRealName());
        vo.put("studentId", verification.getStudentId());
        vo.put("college", verification.getCollege());
        vo.put("enrollYear", verification.getEnrollYear());
        vo.put("studentCardUrl", verification.getStudentCardUrl());
        vo.put("status", verification.getStatus());
        vo.put("rejectReason", verification.getRejectReason());
        vo.put("createdAt", verification.getCreatedAt());
        
        // 获取用户信息
        User user = userMapper.selectById(verification.getUserId());
        if (user != null) {
            vo.put("username", user.getUsername());
            vo.put("nickname", user.getNickname());
        }
        
        return vo;
    }

    // ==================== 请求类 ====================

    @Data
    public static class UpdateStatusRequest {
        @NotNull(message = "状态不能为空")
        @Min(value = 0, message = "状态值无效")
        @Max(value = 2, message = "状态值无效")
        private Integer status;
    }

    @Data
    public static class ReviewVerificationRequest {
        @NotNull(message = "审核结果不能为空")
        @Min(value = 1, message = "状态值无效")
        @Max(value = 2, message = "状态值无效")
        private Integer status;
        private String rejectReason;
    }

    @Data
    public static class CategoryRequest {
        @NotBlank(message = "分类名称不能为空")
        private String name;
        private String icon;
        private Integer sort;
    }
}
