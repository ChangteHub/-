package com.xust.secondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.common.Result;
import com.xust.secondhand.entity.BrowsingHistory;
import com.xust.secondhand.entity.Product;
import com.xust.secondhand.entity.User;
import com.xust.secondhand.mapper.BrowsingHistoryMapper;
import com.xust.secondhand.mapper.ProductMapper;
import com.xust.secondhand.mapper.UserMapper;
import com.xust.secondhand.utils.UserContext;
import com.xust.secondhand.vo.ProductListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 浏览历史控制器
 */
@Tag(name = "浏览历史接口", description = "浏览历史管理")
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final BrowsingHistoryMapper historyMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    @Operation(summary = "获取浏览历史")
    @GetMapping
    public Result<PageResult<ProductListVO>> getHistory(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = UserContext.getUserId();

        Page<BrowsingHistory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BrowsingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowsingHistory::getUserId, userId)
               .orderByDesc(BrowsingHistory::getViewedAt);

        Page<BrowsingHistory> historyPage = historyMapper.selectPage(page, wrapper);

        List<Long> productIds = historyPage.getRecords().stream()
                .map(BrowsingHistory::getProductId)
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return Result.success(PageResult.of(0, pageNum, pageSize, List.of()));
        }

        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        productWrapper.in(Product::getId, productIds);
        List<Product> products = productMapper.selectList(productWrapper);

        // 按浏览时间倒序重排（IN查询不保序）
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<ProductListVO> list = historyPage.getRecords().stream()
                .map(h -> productMap.get(h.getProductId()))
                .filter(p -> p != null)
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(PageResult.of(historyPage.getTotal(), pageNum, pageSize, list));
    }

    @Operation(summary = "添加浏览记录")
    @PostMapping
    public Result<Void> addHistory(@RequestBody Map<String, Long> body) {
        Long userId = UserContext.getUserId();
        Long productId = body.get("productId");

        // 校验商品ID
        if (productId == null) {
            throw BusinessException.badRequest("商品ID不能为空");
        }
        if (productMapper.selectById(productId) == null) {
            throw BusinessException.notFound("商品不存在");
        }

        // 检查是否已存在
        LambdaQueryWrapper<BrowsingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowsingHistory::getUserId, userId)
               .eq(BrowsingHistory::getProductId, productId);
        BrowsingHistory existing = historyMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setViewedAt(java.time.LocalDateTime.now());
            historyMapper.updateById(existing);
        } else {
            BrowsingHistory history = new BrowsingHistory();
            history.setUserId(userId);
            history.setProductId(productId);
            history.setViewedAt(java.time.LocalDateTime.now());
            try {
                historyMapper.insert(history);
            } catch (DuplicateKeyException e) {
                // 并发浏览同一商品，已存在则只更新时间
                existing = historyMapper.selectOne(wrapper);
                if (existing != null) {
                    existing.setViewedAt(java.time.LocalDateTime.now());
                    historyMapper.updateById(existing);
                }
            }
        }

        return Result.success();
    }

    @Operation(summary = "清空浏览历史")
    @DeleteMapping
    public Result<Void> clearHistory() {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<BrowsingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowsingHistory::getUserId, userId);
        historyMapper.delete(wrapper);
        return Result.success();
    }

    @Operation(summary = "删除单条浏览记录")
    @DeleteMapping("/{productId}")
    public Result<Void> removeHistory(@PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<BrowsingHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowsingHistory::getUserId, userId)
               .eq(BrowsingHistory::getProductId, productId);
        historyMapper.delete(wrapper);
        return Result.success();
    }

    private ProductListVO convertToVO(Product product) {
        ProductListVO vo = new ProductListVO();
        vo.setId(product.getId());
        vo.setTitle(product.getTitle());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setCoverImage(product.getCoverImage());
        vo.setLocation(product.getLocation());
        vo.setStatus(product.getStatus());
        vo.setViewCount(product.getViewCount());
        vo.setCreatedAt(product.getCreatedAt());

        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerName(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }

        return vo;
    }
}
