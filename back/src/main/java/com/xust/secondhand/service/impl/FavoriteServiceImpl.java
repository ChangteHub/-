package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.entity.Favorite;
import com.xust.secondhand.entity.Product;
import com.xust.secondhand.mapper.FavoriteMapper;
import com.xust.secondhand.mapper.ProductMapper;
import com.xust.secondhand.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 收藏服务实现类
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Override
    public void addFavorite(Long userId, Long productId) {
        // 校验商品存在
        if (productMapper.selectById(productId) == null) {
            throw BusinessException.notFound("商品不存在");
        }

        // 检查是否已收藏
        if (isFavorite(userId, productId)) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        try {
            favoriteMapper.insert(favorite);
        } catch (DuplicateKeyException e) {
            // 并发重复收藏，忽略
        }
    }

    @Override
    public void removeFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getProductId, productId);
        favoriteMapper.delete(wrapper);
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
               .eq(Favorite::getProductId, productId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }
}
