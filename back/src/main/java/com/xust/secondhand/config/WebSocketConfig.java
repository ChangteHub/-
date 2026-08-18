package com.xust.secondhand.config;

import com.xust.secondhand.websocket.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;
import java.util.List;

/**
 * WebSocket配置
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins(origins.toArray(new String[0]))
                .addInterceptors();
    }
}
