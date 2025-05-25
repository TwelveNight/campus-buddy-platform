package com.example.campusbuddy.websocket;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 支持用户定向推送的WebSocket处理器
 */
@Component
public class UserWebSocketHandler extends TextWebSocketHandler {
    // 存储用户ID与WebSocketSession的映射
    private static final Map<Long, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessionMap.put(userId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessionMap.remove(userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 可根据需要处理客户端发来的消息
        session.sendMessage(new TextMessage("echo: " + message.getPayload()));
    }

    // 静态方法：向指定用户推送消息
    public static void sendToUser(Long userId, String message) {
        WebSocketSession session = userSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception ignored) {}
        }
    }

    // 静态方法：广播消息
    public static void broadcast(String message) {
        for (WebSocketSession session : userSessionMap.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (Exception ignored) {}
            }
        }
    }

    // 静态方法：向指定用户推送通知（兼容旧业务）
    public static void sendNotification(Long userId, String title, String content, String type, String relatedLink) {
        String json = String.format("{\"type\":\"NOTIFICATION\",\"title\":\"%s\",\"content\":\"%s\",\"typeValue\":\"%s\",\"relatedLink\":\"%s\",\"timestamp\":%d}",
                escape(title), escape(content), escape(type), escape(relatedLink), System.currentTimeMillis());
        sendToUser(userId, json);
    }

    // 静态方法：向指定用户推送私信（兼容旧业务）
    public static void sendPrivateMessage(Long userId, Long senderId, String senderName, String content, Long messageId) {
        String json = String.format("{\"type\":\"PRIVATE_MESSAGE\",\"title\":\"新的私信\",\"senderId\":%d,\"senderName\":\"%s\",\"content\":\"%s\",\"messageId\":%d,\"timestamp\":%d}",
                senderId, escape(senderName), escape(content), messageId, System.currentTimeMillis());
        sendToUser(userId, json);
    }

    // 简单转义，防止JSON格式错误
    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    // 从URL中获取userId
    private Long getUserId(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            // 例如 /ws/123
            String[] parts = path.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            return null;
        }
    }
}
