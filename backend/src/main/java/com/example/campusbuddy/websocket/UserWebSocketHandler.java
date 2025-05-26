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
        // 解析消息内容
        String payload = message.getPayload();
        try {
            // 检查是否是PING消息
            if (payload.contains("\"type\":\"PING\"")) {
                // 返回PONG响应
                session.sendMessage(new TextMessage("{\"type\":\"PONG\",\"timestamp\":" + System.currentTimeMillis() + "}"));
                return;
            }
            
            // 处理其他类型的消息
            session.sendMessage(new TextMessage("echo: " + payload));
        } catch (Exception e) {
            // 如果解析出错，返回原消息
            session.sendMessage(new TextMessage("echo: " + payload));
        }
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

    // 静态方法：向指定用户推送通知
    public static void sendNotification(Long userId, String content) {
        sendNotification(userId, "新通知", content, "GENERAL", "");
    }

    // 静态方法：向指定用户推送通知（兼容旧业务）
    public static void sendNotification(Long userId, String title, String content, String type, String relatedLink) {
        String json = String.format("{\"type\":\"NOTIFICATION\",\"title\":\"%s\",\"content\":\"%s\",\"typeValue\":\"%s\",\"relatedLink\":\"%s\",\"timestamp\":%d}",
                escape(title), escape(content), escape(type), escape(relatedLink), System.currentTimeMillis());
        sendToUser(userId, json);
    }

    // 静态方法：向指定用户推送好友申请通知
    public static void sendFriendRequestNotification(Long userId, Long requesterId, String requesterName, Long requestId) {
        String json = String.format("{\"type\":\"FRIEND_REQUEST\",\"title\":\"好友申请\",\"content\":\"%s向您发送了好友申请\",\"requesterId\":%d,\"requesterName\":\"%s\",\"requestId\":%d,\"timestamp\":%d}",
                escape(requesterName), requesterId, escape(requesterName), requestId, System.currentTimeMillis());
        sendToUser(userId, json);
    }

    // 静态方法：向指定用户推送好友申请状态变更通知
    public static void sendFriendRequestStatusNotification(Long userId, Long responderId, String responderName, String status) {
        String json = String.format("{\"type\":\"FRIEND_REQUEST_STATUS\",\"title\":\"好友申请状态\",\"content\":\"%s%s了您的好友申请\",\"responderId\":%d,\"responderName\":\"%s\",\"status\":\"%s\",\"timestamp\":%d}",
                escape(responderName), "ACCEPTED".equals(status) ? "接受" : "拒绝", responderId, escape(responderName), escape(status), System.currentTimeMillis());
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
