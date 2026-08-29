package com.xust.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xust.secondhand.common.BusinessException;
import com.xust.secondhand.common.PageResult;
import com.xust.secondhand.dto.request.RegisterRequest;
import com.xust.secondhand.dto.request.LoginRequest;
import com.xust.secondhand.dto.request.UpdateUserRequest;
import com.xust.secondhand.entity.Favorite;
import com.xust.secondhand.entity.Product;
import com.xust.secondhand.entity.ProductImage;
import com.xust.secondhand.entity.User;
import com.xust.secondhand.repository.FavoriteRepository;
import com.xust.secondhand.repository.ProductImageRepository;
import com.xust.secondhand.repository.ProductRepository;
import com.xust.secondhand.repository.UserRepository;
import com.xust.secondhand.service.UserService;
import com.xust.secondhand.security.JwtUtil;
import com.xust.secondhand.dto.response.LoginResponse;
import com.xust.secondhand.dto.response.ProductListResponse;
import com.xust.secondhand.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 登录防暴力破解：连续失败 N 次后锁定一段时间（内存态，重启即清空） */
    private static final int MAX_LOGIN_FAILURES = 5;
    private static final long LOGIN_LOCK_MILLIS = 15 * 60 * 1000L;
    private static final Map<String, LoginFailure> LOGIN_FAILURES = new ConcurrentHashMap<>();

    private record LoginFailure(int count, long lockUntil) {}

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FavoriteRepository favoriteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserResponse register(RegisterRequest dto) {
        // 保留系统用户名（忽略大小写与首尾空格），防止普通用户抢注后 DataInitializer 创建管理员冲突
        if ("admin".equalsIgnoreCase(dto.getUsername().trim())) {
            throw BusinessException.badRequest("该用户名不可注册");
        }
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userRepository.selectCount(wrapper) > 0) {
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
            userRepository.insert(user);
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突可能来自：并发注册同名用户，或逻辑删除记录仍占用用户名。
            // 清理已逻辑删除的同名记录后重试一次（不会误删正常用户）
            userRepository.deletePhysicalDeletedByUsername(dto.getUsername());
            try {
                userRepository.insert(user);
            } catch (DuplicateKeyException ex) {
                throw BusinessException.badRequest("用户名已存在");
            }
        }

        return convertToUserVO(user);
    }

    @Override
    public LoginResponse login(LoginRequest dto) {
        String username = dto.getUsername();

        // 防暴力破解：失败次数过多的账号临时锁定
        LoginFailure failure = LOGIN_FAILURES.get(username);
        long now = System.currentTimeMillis();
        if (failure != null && failure.lockUntil() > now) {
            long remainMin = (failure.lockUntil() - now) / 60000 + 1;
            throw BusinessException.forbidden("密码错误次数过多，请约 " + remainMin + " 分钟后重试");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userRepository.selectOne(wrapper);

        if (user == null) {
            recordLoginFailure(username);
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            recordLoginFailure(username);
            throw BusinessException.badRequest("用户名或密码错误");
        }

        // 登录成功，清除失败计数
        LOGIN_FAILURES.remove(username);

        // 检查用户状态
        if (user.getStatus() == 1) {
            throw BusinessException.forbidden("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return new LoginResponse(token, convertToUserVO(user));
    }

    /**
     * 记录一次登录失败；连续失败达上限则锁定
     */
    private void recordLoginFailure(String username) {
        LOGIN_FAILURES.compute(username, (k, prev) -> {
            int count = (prev == null || prev.lockUntil() > 0 && prev.lockUntil() < System.currentTimeMillis())
                    ? 1 : prev.count() + 1;
            if (count >= MAX_LOGIN_FAILURES) {
                return new LoginFailure(count, System.currentTimeMillis() + LOGIN_LOCK_MILLIS);
            }
            return new LoginFailure(count, 0);
        });
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("用户不存在");
        }
        return convertToUserVO(user);
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateUserRequest dto) {
        User user = userRepository.selectById(userId);
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

        userRepository.updateById(user);
        return convertToUserVO(user);
    }

    @Override
    public PageResult<ProductListResponse> getUserProducts(Long userId, Integer status, int pageNum, int pageSize) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, userId);

        // 状态筛选
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreatedAt);

        Page<Product> result = productRepository.selectPage(page, wrapper);

        // 本列表所有商品卖家均为当前用户，只查一次即可
        User seller = userRepository.selectById(userId);

        List<ProductListResponse> list = result.getRecords().stream()
                .map(p -> convertToProductListVO(p, seller))
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageNum, pageSize, list);
    }

    @Override
    public PageResult<ProductListResponse> getUserFavorites(Long userId, int pageNum, int pageSize) {
        // 查询用户的收藏记录
        Page<Favorite> favPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Favorite> favWrapper = new LambdaQueryWrapper<>();
        favWrapper.eq(Favorite::getUserId, userId)
                  .orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> favResult = favoriteRepository.selectPage(favPage, favWrapper);

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
        List<Product> products = productRepository.selectList(prodWrapper);

        // 按收藏顺序排序（因为IN查询不保序）
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 批量查卖家信息，避免每个商品单独查询（N+1）
        List<Long> sellerIds = products.stream()
                .map(Product::getSellerId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> sellerMap = sellerIds.isEmpty()
                ? Map.of()
                : userRepository.selectBatchIds(sellerIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        List<ProductListResponse> list = productIds.stream()
                .map(productMap::get)
                .filter(p -> p != null)
                .map(p -> convertToProductListVO(p, sellerMap.get(p.getSellerId())))
                .collect(Collectors.toList());

        return PageResult.of(favResult.getTotal(), pageNum, pageSize, list);
    }

    /**
     * 转换为UserVO
     */
    private UserResponse convertToUserVO(User user) {
        UserResponse vo = new UserResponse();
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
     * 转换为ProductListVO（卖家信息由调用方批量传入，避免 N+1）
     */
    private ProductListResponse convertToProductListVO(Product product, User seller) {
        ProductListResponse vo = new ProductListResponse();
        vo.setId(product.getId());
        vo.setTitle(product.getTitle());
        vo.setPrice(product.getPrice());
        vo.setOriginalPrice(product.getOriginalPrice());
        vo.setCoverImage(product.getCoverImage());
        vo.setLocation(product.getLocation());
        vo.setProductCondition(product.getProductCondition());
        vo.setStatus(product.getStatus());
        vo.setViewCount(product.getViewCount());
        vo.setCreatedAt(product.getCreatedAt());

        if (seller != null) {
            vo.setSellerName(seller.getNickname());
            vo.setSellerAvatar(seller.getAvatar());
        }

        return vo;
    }
}
