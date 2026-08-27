package com.xust.secondhand.utils;

import com.xust.secondhand.entity.User;
import com.xust.secondhand.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 仅从请求头获取Token（URL 查询参数传 token 会泄漏到访问日志/Referer，已移除；
            // WebSocket 的 ?token= 由专用 HandshakeInterceptor 在握手阶段单独校验）
            String token = getTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                // 解析用户信息（token仅作身份载体）
                Long userId = jwtUtil.getUserIdFromToken(token);

                // 校验用户仍存在且未被禁用/删除（防止封禁后旧Token继续有效）。
                // 直接写 401 JSON 并终止请求，保留"账号已禁用"语义，不与普通未登录混淆
                User user = userMapper.selectById(userId);
                if (user == null || (user.getStatus() != null && user.getStatus() == 1)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"账号已禁用或不存在\",\"data\":null}");
                    return;
                }

                // 角色一律以数据库当前值为准（防止角色降级后旧Token仍持有管理员权限）
                Integer role = user.getRole() != null ? user.getRole() : 0;

                // 设置用户上下文
                UserContext.setUserId(userId);
                UserContext.setRole(role);

                // 创建认证对象，添加角色权限
                List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
                if (role == 1) {
                    authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置到Security上下文
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.warn("JWT认证失败: " + e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 清除用户上下文
            UserContext.clear();
        }
    }

    /**
     * 从请求头获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
