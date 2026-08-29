package com.xust.secondhand.websocket;

import com.xust.secondhand.entity.User;
import com.xust.secondhand.repository.UserRepository;
import com.xust.secondhand.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * WebSocket 握手认证拦截器
 * 在握手阶段校验 ?token= 的合法性与用户状态，拒绝匿名/无效连接，
 * 避免连接建立后才在 handler 里关闭造成的资源浪费
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUriString(request.getURI().toString())
                .build()
                .getQueryParams()
                .getFirst("token");

        if (token == null || !jwtUtil.validateToken(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        // 与HTTP侧对齐：用户需存在且未被禁用/删除
        User user = userRepository.selectById(userId);
        if (user == null || (user.getStatus() != null && user.getStatus() == 1)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        // 握手即注入身份，handler 中无需再解析 token
        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // 无需处理
    }
}
