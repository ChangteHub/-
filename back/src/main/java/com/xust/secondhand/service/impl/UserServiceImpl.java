package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.RegisterDTO;
import com.xust.secondhand.dto.LoginDTO;
import com.xust.secondhand.dto.UpdateUserDTO;
import com.xust.secondhand.entity.Favorite;
import com.xust.secondhand.entity.Product;
import com.xust.secondhand.entity.ProductImage;
import com.xust.secondhand.entity.User;
import com.xust.secondhand.mapper.FavoriteMapper;
import com.xust.secondhand.mapper.ProductImageMapper;
import com.xust.secondhand.mapper.ProductMapper;
import com.xust.secondhand.mapper.UserMapper;
import com.xust.secondhand.service.UserService;
import com.xust.secondhand.utils.JwtUtil;
import com.xust.secondhand.vo.LoginVO;
import com.xust.secondhand.vo.ProductListVO;
import com.xust.secondhand.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final FavoriteMapper favoriteMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserVO register(RegisterDTO dto) {
        // 保留系统用户名（忽略大小写与首尾空格），防止普通用户抢注后 DataInitializer 创建管理员冲突
        if ("admin".equalsIgnoreCase(dto.getUsername().trim())) {
            throw BusinessException.badRequest("该用户名不可注册");
        }
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw BusinessException.badRequest("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setStudentId(dto.getStudentId());
        user.setSchool("西南科技大学");
        user.setStatus(0);
        user.setRole(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突可能来自：并发注册同名用户，或逻辑删除记录仍占用用户名。
            // 清理已逻辑删除的同名记录后重试一次（不会误删正常用户）
            userMapper.deletePhysicalDeletedByUsername(dto.getUsername());
            try {
                userMapper.insert(user);
            } catch (DuplicateKeyException ex) {
                throw BusinessException.badRequest("用户名已存在");
            }
        }

        return convertToUserVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 1) {
            throw BusinessException.forbidden("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginVO(token, convertToUserVO(user));
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    public UserVO updateProfile(Long userId, UpdateUserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }

        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }

        userMapper.updateById(user);
        return convertToUserVO(user);
    }

    @Override
    public PageResult<ProductListVO> getUserProducts(Long userId, Integer status, int pageNum, int pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, userId);

        // 状态筛选
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreatedAt);

        Page<Product> result = productMapper.selectPage(page, wrapper);

        List<ProductListVO> list = result.getRecords().stream()
                .map(this::convertToProductListVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageNum, pageSize, list);
    }

    @Override
    public PageResult<ProductListVO> getUserFavorites(Long userId, int pageNum, int pageSize) {
        // 查询用户的收藏记录
        Page<Favorite> favPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(Favorite::getUserId, userId)
                  .orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> favResult = favoriteMapper.selectPage(favPage, favWrapper);

        // 获取收藏的商品ID列表
        List<Long> productIds = favResult.getRecords().stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());

        if (productIds.isEmpty()) {
            return PageResult.of(0, pageNum, pageSize, java.util.Collections.emptyList());
        }

        // 查询商品信息
        LambdaQueryWrapper<Product> prodWrapper = new LambdaQueryWrapper<>();
        prodWrapper.in(Product::getId, productIds);
        List<Product> products = productMapper.selectList(prodWrapper);

        // 按收藏顺序排序（因为IN查询不保序）
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<ProductListVO> list = productIds.stream()
                .map(productMap::get)
                .filter(p -> p != null)
                .map(this::convertToProductListVO)
                .collect(Collectors.toList());

        return PageResult.of(favResult.getTotal(), pageNum, pageSize, list);
    }

    /**
     * 转换为UserVO
     */
    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setSchool(user.getSchool());
        vo.setStudentId(user.getStudentId());
        vo.setBio(user.getBio());
        vo.setRole(user.getRole());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    /**
     * 转换为ProductListVO
     */
    private ProductListVO convertToProductListVO(Product product) {
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

        // 获取卖家信息
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerName(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }

        return vo;
    }
}
