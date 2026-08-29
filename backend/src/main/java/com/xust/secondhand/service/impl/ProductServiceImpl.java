package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.request.ProductSaveRequest;
import com.xust.secondhand.entity.Category;
import com.xust.secondhand.entity.Product;
import com.xust.secondhand.entity.ProductImage;
import com.xust.secondhand.entity.User;
import com.xust.secondhand.repository.CategoryRepository;
import com.xust.secondhand.repository.ProductImageRepository;
import com.xust.secondhand.repository.ProductRepository;
import com.xust.secondhand.repository.UserRepository;
import com.xust.secondhand.service.FavoriteService;
import com.xust.secondhand.service.ProductService;
import com.xust.secondhand.dto.response.ProductListResponse;
import com.xust.secondhand.dto.response.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FavoriteService favoriteService;

    @Override
    @Transactional
    public ProductResponse createProduct(Long userId, ProductSaveRequest dto) {
        // 创建商品
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setSellerId(userId);
        product.setStatus(0);
        product.setViewCount(0);

        // 设置封面图为第一张图片
        if (dto.getCoverImage() == null && dto.getImages() != null && !dto.getImages().isEmpty()) {
            product.setCoverImage(dto.getImages().get(0));
        }

        productRepository.insert(product);

        // 保存商品图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(product.getId());
                image.setImageUrl(dto.getImages().get(i));
                image.setSort(i + 1);
                productImageRepository.insert(image);
            }
        }

        // 直接转换返回，不走getProductDetail避免发布即浏览量+1
        return convertToDetailVO(product, userId);
    }

    @Override
    public PageResult<ProductListResponse> getProductList(Long categoryId, String keyword, String sort, 
                                                     int pageNum, int pageSize, Long currentUserId) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查询在售商品
        wrapper.eq(Product::getStatus, 0);

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Product::getTitle, keyword)
                    .or().like(Product::getDescription, keyword));
        }

        // 排序
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt);
        }

        Page<Product> result = productRepository.selectPage(page, wrapper);

        // 批量查询卖家信息，避免 N+1
        Set<Long> sellerIds = result.getRecords().stream()
                .map(Product::getSellerId)
                .collect(Collectors.toSet());
        Map<Long, User> sellerMap = Map.of();
        if (!sellerIds.isEmpty()) {
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(User::getId, sellerIds);
            sellerMap = userRepository.selectList(userWrapper).stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
        }

        Map<Long, User> finalSellerMap = sellerMap;
        List<ProductListResponse> list = result.getRecords().stream()
                .map(p -> convertToListVO(p, finalSellerMap))
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageNum, pageSize, list);
    }

    @Override
    public ProductResponse getProductDetail(Long productId, Long currentUserId) {
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }

        // 非卖家不可查看已下架/已售出商品
        if (product.getStatus() != 0
                && (currentUserId == null || !product.getSellerId().equals(currentUserId))) {
            throw BusinessException.notFound("商品不存在或已下架");
        }

        // 浏览量原子+1（避免读改写丢更新）
        productRepository.incrementViewCount(productId);

        ProductResponse vo = convertToDetailVO(product, currentUserId);
        // 同步返回自增后的浏览量（DB已+1）；viewCount为null（脏数据）时按0处理
        vo.setViewCount((product.getViewCount() == null ? 0 : product.getViewCount()) + 1);
        return vo;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long userId, Long productId, ProductSaveRequest dto) {
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }

        // 验证是否是卖家本人
        if (!product.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("只能编辑自己的商品");
        }

        // 更新商品信息（仅覆盖非null字段，避免清空原值）
        if (dto.getCategoryId() != null) product.setCategoryId(dto.getCategoryId());
        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getOriginalPrice() != null) product.setOriginalPrice(dto.getOriginalPrice());
        if (dto.getCoverImage() != null) product.setCoverImage(dto.getCoverImage());
        if (dto.getLocation() != null) product.setLocation(dto.getLocation());
        if (dto.getProductCondition() != null) product.setProductCondition(dto.getProductCondition());
        productRepository.updateById(product);

        // 更新图片（先删除旧的，再添加新的）
        if (dto.getImages() != null) {
            LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductImage::getProductId, productId);
            productImageRepository.delete(wrapper);

            for (int i = 0; i < dto.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setImageUrl(dto.getImages().get(i));
                image.setSort(i + 1);
                productImageRepository.insert(image);
            }
        }

        // 直接转换返回，不走getProductDetail避免编辑商品浏览量+1
        return convertToDetailVO(product, userId);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long userId, Long productId, Integer status) {
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }

        if (!product.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("只能修改自己的商品状态");
        }

        // 校验状态值范围
        if (status < 0 || status > 2) {
            throw BusinessException.badRequest("无效的状态值");
        }

        product.setStatus(status);
        productRepository.updateById(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }

        if (!product.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("只能删除自己的商品");
        }

        productRepository.deleteById(productId);

        // 删除商品图片
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, productId);
        productImageRepository.delete(wrapper);
    }

    /**
     * 转换为列表VO（使用缓存的卖家信息）
     */
    private ProductListResponse convertToListVO(Product product, Map<Long, User> sellerMap) {
        ProductListResponse vo = new ProductListResponse();
        BeanUtils.copyProperties(product, vo);

        // 从缓存中获取卖家信息
        User seller = sellerMap.get(product.getSellerId());
        if (seller != null) {
            vo.setSellerName(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }

        return vo;
    }

    /**
     * 转换为详情VO
     */
    private ProductResponse convertToDetailVO(Product product, Long currentUserId) {
        ProductResponse vo = new ProductResponse();
        BeanUtils.copyProperties(product, vo);

        // 获取卖家信息
        User seller = userRepository.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerName(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
            vo.setSellerSchool(seller.getSchool());
        }

        // 获取分类信息
        Category category = categoryRepository.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 获取商品图片
        LambdaQueryWrapper<ProductImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductImage::getProductId, product.getId())
               .orderByAsc(ProductImage::getSort);
        List<ProductImage> images = productImageRepository.selectList(wrapper);
        vo.setImages(images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList()));

        // 检查是否已收藏
        if (currentUserId != null) {
            vo.setIsFavorite(favoriteService.isFavorite(currentUserId, product.getId()));
        } else {
            vo.setIsFavorite(false);
        }

        return vo;
    }
}
