package com.example.campusbuddy.config;

import com.example.campusbuddy.websocket.EchoHandler;
import com.example.campusbuddy.websocket.UserWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Echo 测试端点
        registry.addHandler(new EchoHandler(), "/ws/echo")
                .setAllowedOrigins("*") // 在生产环境中应限制来源
                .withSockJS(); // 添加SockJS支持，增强浏览器兼容性

        // 用户定向推送端点
        registry.addHandler(new UserWebSocketHandler(), "/ws/{userId}")
                .setAllowedOrigins("*") // 在生产环境中应限制来源
                .withSockJS(); // 添加SockJS支持，增强浏览器兼容性
    }

    /**
     * 配置WebSocket会话参数
     * 增加缓冲区大小和超时时间，提高连接稳定性
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        // 设置消息缓冲区大小
        container.setMaxTextMessageBufferSize(16384); // 增加到16KB
        container.setMaxBinaryMessageBufferSize(16384);
        // 设置会话超时时间（毫秒）
        container.setMaxSessionIdleTimeout(1800000L); // 增加到30分钟
        // 设置异步发送超时
        container.setAsyncSendTimeout(10000L); // 增加到10秒
        return container;
    }
}
