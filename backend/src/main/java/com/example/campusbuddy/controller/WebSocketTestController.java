package com.example.campusbuddy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.example.campusbuddy.websocket.UserWebSocketHandler;
import com.example.campusbuddy.common.R;

import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * 获取WebSocket服务状态
     */
    @GetMapping("/status")
    public R<Map<String, Object>> getWebSocketStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("onlineUsers", UserWebSocketHandler.getOnlineUserCount());
        status.put("timestamp", System.currentTimeMillis());
        
        return R.ok("获取WebSocket状态成功", status);
    }
    
    /**
     * 检查用户是否在线
     */
    @GetMapping("/user/{userId}/online")
    public R<Boolean> isUserOnline(@PathVariable("userId") Long userId) {
        boolean isOnline = UserWebSocketHandler.isUserOnline(userId);
        return R.ok("查询用户在线状态成功", isOnline);
    }
    
    /**
     * 发送测试消息给指定用户
     */
    @PostMapping("/user/{userId}/send")
    public R<String> sendTestMessage(
            @PathVariable("userId") Long userId,
            @RequestParam("type") String type,
            @RequestParam("content") String content) {
        
        if (!UserWebSocketHandler.isUserOnline(userId)) {
            return R.fail("用户不在线，无法发送消息");
        }
        
        switch (type.toUpperCase()) {
            case "NOTIFICATION":
                UserWebSocketHandler.sendNotification(userId, "测试通知", content, "TEST", "");
                break;
            case "MESSAGE":
                UserWebSocketHandler.sendPrivateMessage(userId, 0L, "系统", content, System.currentTimeMillis());
                break;
            default:
                return R.fail("不支持的消息类型: " + type);
        }
        
        return R.ok("消息已发送");
    }
}
