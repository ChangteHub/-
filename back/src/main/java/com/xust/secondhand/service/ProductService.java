package com.xust.secondhand.service;

import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.ProductDTO;
import com.xust.secondhand.vo.ProductListVO;
import com.xust.secondhand.vo.ProductVO;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 发布商品
     */
    ProductVO createProduct(Long userId, ProductDTO dto);

    /**
     * 获取商品列表
     */
    PageResult<ProductListVO> getProductList(Long categoryId, String keyword, String sort, int pageNum, int pageSize, Long currentUserId);

    /**
     * 获取商品详情
     */
    ProductVO getProductDetail(Long productId, Long currentUserId);

    /**
     * 编辑商品
     */
    ProductVO updateProduct(Long userId, Long productId, ProductDTO dto);

    /**
     * 修改商品状态
     */
    void updateProductStatus(Long userId, Long productId, Integer status);

    /**
     * 删除商品
     */
    void deleteProduct(Long userId, Long productId);
}
