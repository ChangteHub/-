package com.xust.secondhand.service;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.request.ProductSaveRequest;
import com.xust.secondhand.dto.response.ProductListResponse;
import com.xust.secondhand.dto.response.ProductResponse;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 发布商品
     */
    ProductResponse createProduct(Long userId, ProductSaveRequest dto);

    /**
     * 获取商品列表
     */
    PageResult<ProductListResponse> getProductList(Long categoryId, String keyword, String sort, int pageNum, int pageSize, Long currentUserId);

    /**
     * 获取商品详情
     */
    ProductResponse getProductDetail(Long productId, Long currentUserId);

    /**
     * 编辑商品
     */
    ProductResponse updateProduct(Long userId, Long productId, ProductSaveRequest dto);

    /**
     * 修改商品状态
     */
    void updateProductStatus(Long userId, Long productId, Integer status);

    /**
     * 删除商品
     */
    void deleteProduct(Long userId, Long productId);
}
