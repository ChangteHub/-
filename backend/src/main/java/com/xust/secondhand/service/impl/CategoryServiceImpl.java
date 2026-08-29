package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xust.secondhand.entity.Category;
import com.xust.secondhand.repository.CategoryRepository;
import com.xust.secondhand.service.CategoryService;
import com.xust.secondhand.dto.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 0)
               .orderByAsc(Category::getSort);

        List<Category> categories = categoryRepository.selectList(wrapper);

        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private CategoryResponse convertToVO(Category category) {
        CategoryResponse vo = new CategoryResponse();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setIcon(category.getIcon());
        vo.setSort(category.getSort());
        return vo;
    }
}
