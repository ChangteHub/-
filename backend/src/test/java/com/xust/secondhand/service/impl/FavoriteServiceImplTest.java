package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.entity.Favorite;
import com.xust.secondhand.entity.Product;
import com.xust.secondhand.repository.FavoriteRepository;
import com.xust.secondhand.repository.ProductRepository;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 收藏服务单元测试（Mockito，不依赖数据库）
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ProductRepository productRepository;

    private FavoriteServiceImpl favoriteService;

    @BeforeAll
    static void initTableInfo() {
        // LambdaQueryWrapper 解析 Favorite::getXxx 需要 MyBatis-Plus 的实体元数据缓存
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Favorite.class);
    }

    @BeforeEach
    void setUp() {
        favoriteService = new FavoriteServiceImpl(favoriteRepository, productRepository);
    }

    @Test
    @DisplayName("收藏不存在的商品抛业务异常")
    void addFavoriteProductNotFound() {
        when(productRepository.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> favoriteService.addFavorite(1L, 999L));
        verify(favoriteRepository, never()).insert(any(Favorite.class));
    }

    @Test
    @DisplayName("未收藏过则插入收藏记录")
    void addFavoriteSuccess() {
        when(productRepository.selectById(1L)).thenReturn(new Product());
        when(favoriteRepository.selectCount(any())).thenReturn(0L);

        favoriteService.addFavorite(1L, 1L);

        verify(favoriteRepository).insert(any(Favorite.class));
    }

    @Test
    @DisplayName("已收藏过则跳过插入（幂等）")
    void addFavoriteAlreadyExists() {
        when(productRepository.selectById(1L)).thenReturn(new Product());
        when(favoriteRepository.selectCount(any())).thenReturn(1L);

        favoriteService.addFavorite(1L, 1L);

        verify(favoriteRepository, never()).insert(any(Favorite.class));
    }

    @Test
    @DisplayName("isFavorite 依据记录数返回布尔值")
    void isFavorite() {
        when(favoriteRepository.selectCount(any())).thenReturn(1L);
        assertTrue(favoriteService.isFavorite(1L, 1L));

        when(favoriteRepository.selectCount(any())).thenReturn(0L);
        assertFalse(favoriteService.isFavorite(1L, 2L));
    }
}
