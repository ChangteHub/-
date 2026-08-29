package com.xust.secondhand.service;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 添加收藏
     */
    void addFavorite(Long userId, Long productId);

    /**
     * 取消收藏
     */
    void removeFavorite(Long userId, Long productId);

    /**
     * 检查是否已收藏
     */
    boolean isFavorite(Long userId, Long productId);
}
