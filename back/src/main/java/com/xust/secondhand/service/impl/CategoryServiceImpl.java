package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xust.secondhand.entity.Category;
import com.xust.secondhand.mapper.CategoryMapper;
import com.xust.secondhand.service.CategoryService;
import com.xust.secondhand.vo.CategoryVO;
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

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> getAllCategories() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getStatus, 0)
               .orderByAsc(Category::getSort);

        List<Category> categories = categoryMapper.selectList(wrapper);

        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setIcon(category.getIcon());
        vo.setSort(category.getSort());
        return vo;
    }
}
