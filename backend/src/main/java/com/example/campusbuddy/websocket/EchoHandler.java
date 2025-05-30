package com.example.campusbuddy.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(EchoHandler.class);
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("接收到Echo消息: {}", payload);
        
        // 处理特殊消息类型
        if (payload.equals("PING")) {
            // 处理心跳消息
            session.sendMessage(new TextMessage("PONG"));
            logger.info("已回应心跳消息");
            return;
        }
        
        // 尝试解析JSON消息
        try {
            // 检查是否为JSON格式
            if (payload.trim().startsWith("{")) {
                // 回显消息，添加echo:前缀
                session.sendMessage(new TextMessage(payload));
                logger.info("已回显JSON消息");
            } else {
                // 非JSON消息，简单回显
                session.sendMessage(new TextMessage("echo: " + payload));
                logger.info("已回显纯文本消息");
            }
        } catch (Exception e) {
            // 如果JSON解析失败，以纯文本方式回显
            session.sendMessage(new TextMessage("echo: " + payload));
            logger.info("回显消息失败: {}", e.getMessage());
        }
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Echo WebSocket连接已建立: {}", session.getId());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        logger.info("Echo WebSocket连接已关闭: {}, 状态: {}", session.getId(), status);
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Echo WebSocket传输错误: {}", exception.getMessage());
    }
}
