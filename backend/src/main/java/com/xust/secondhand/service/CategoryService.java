package com.xust.secondhand.service;

import com.xust.secondhand.dto.response.CategoryResponse;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类
     */
    List<CategoryResponse> getAllCategories();
}
