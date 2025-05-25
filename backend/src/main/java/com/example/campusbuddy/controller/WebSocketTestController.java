package com.example.campusbuddy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * 用于测试WebSocket配置的控制器
 */
@RestController
@RequestMapping("/api/websocket")
public class WebSocketTestController {

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/info")
    public String getWebSocketInfo() {
        // 检查ServerEndpointExporter是否存在于Spring上下文中
        boolean hasServerEndpointExporter = applicationContext.containsBean("serverEndpointExporter");
        
        StringBuilder info = new StringBuilder();
        info.append("WebSocket配置状态：\n");
        info.append("ServerEndpointExporter存在: ").append(hasServerEndpointExporter).append("\n");
        
        // 获取WebSocket相关的所有Bean
        String[] webSocketBeans = applicationContext.getBeanNamesForType(jakarta.websocket.server.ServerEndpointConfig.Configurator.class);
        info.append("WebSocket相关Bean数量: ").append(webSocketBeans.length).append("\n");
        
        for (String beanName : webSocketBeans) {
            info.append("Bean名称: ").append(beanName).append("\n");
        }
        
        return info.toString();
    }
}
