package com.example.campusbuddy.config;

import com.example.campusbuddy.websocket.EchoHandler;
import com.example.campusbuddy.websocket.UserWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Echo 测试端点
        registry.addHandler(new EchoHandler(), "/ws/echo").setAllowedOrigins("*");
        // 用户定向推送端点
        registry.addHandler(new UserWebSocketHandler(), "/ws/{userId}").setAllowedOrigins("*");
    }
}
