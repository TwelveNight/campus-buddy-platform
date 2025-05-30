package com.example.campusbuddy.websocket;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 支持用户定向推送的WebSocket处理器
 */
@Component
public class UserWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserWebSocketHandler.class);
    
    // 存储用户ID与WebSocketSession的映射
    private static final Map<Long, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            // 如果已存在会话，先关闭旧会话
            WebSocketSession existingSession = userSessionMap.get(userId);
            if (existingSession != null && existingSession.isOpen()) {
                try {
                    existingSession.close(CloseStatus.NORMAL.withReason("用户在其他地方连接"));
                } catch (IOException e) {
                    logger.warn("关闭用户{}的旧会话时出错: {}", userId, e.getMessage());
                }
            }
            
            // 存储新会话
            userSessionMap.put(userId, session);
            logger.info("用户 {} 成功建立WebSocket连接", userId);
            
            // 发送连接成功消息
            try {
                session.sendMessage(new TextMessage("{\"type\":\"CONNECTION\",\"status\":\"CONNECTED\",\"timestamp\":" + System.currentTimeMillis() + "}"));
            } catch (IOException e) {
                logger.error("无法发送连接成功消息给用户 {}: {}", userId, e.getMessage());
            }
        } else {
            logger.warn("无法从会话中提取用户ID，关闭连接");
            session.close(CloseStatus.PROTOCOL_ERROR.withReason("无效的用户ID"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            // 仅当存储的会话与当前关闭的会话相同时才移除
            WebSocketSession storedSession = userSessionMap.get(userId);
            if (storedSession != null && storedSession.getId().equals(session.getId())) {
                userSessionMap.remove(userId);
                logger.info("用户 {} 的WebSocket连接已关闭: {}", userId, status);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 解析消息内容
        String payload = message.getPayload();
        Long userId = getUserId(session);
        
        try {
            // 处理纯文本PING消息
            if (payload.equals("PING")) {
                // 返回纯文本PONG响应
                session.sendMessage(new TextMessage("PONG"));
                logger.debug("向用户 {} 发送纯文本PONG响应", userId);
                return;
            }
            
            // 检查是否是JSON格式的PING消息
            if (payload.contains("\"type\":\"PING\"")) {
                // 返回JSON格式的PONG响应
                session.sendMessage(new TextMessage("{\"type\":\"PONG\",\"timestamp\":" + System.currentTimeMillis() + "}"));
                logger.debug("向用户 {} 发送JSON PONG响应", userId);
                return;
            }
            
            // 处理其他类型的消息
            if (payload.startsWith("{")) {
                // 看起来是JSON格式，直接回显
                session.sendMessage(new TextMessage(payload));
                logger.debug("回显JSON消息给用户 {}", userId);
            } else {
                // 非JSON消息，添加前缀
                session.sendMessage(new TextMessage("echo: " + payload));
                logger.debug("回显纯文本消息给用户 {}: {}", userId, payload);
            }
        } catch (Exception e) {
            logger.error("处理用户 {} 的消息时出错: {}", userId, e.getMessage());
            // 如果解析出错，返回原消息
            try {
                session.sendMessage(new TextMessage("error: " + e.getMessage()));
            } catch (IOException ioException) {
                logger.error("无法发送错误响应给用户 {}: {}", userId, ioException.getMessage());
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserId(session);
        logger.error("用户 {} 的WebSocket传输出错: {}", userId, exception.getMessage());
        
        // 移除会话
        if (userId != null) {
            WebSocketSession storedSession = userSessionMap.get(userId);
            if (storedSession != null && storedSession.getId().equals(session.getId())) {
                userSessionMap.remove(userId);
                logger.info("由于传输错误，移除了用户 {} 的会话", userId);
            }
        }
        
        // 尝试关闭会话
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR.withReason("传输错误: " + exception.getMessage()));
            } catch (IOException e) {
                logger.warn("关闭错误会话时出现异常: {}", e.getMessage());
            }
        }
    }

    // 静态方法：向指定用户推送消息
    public static void sendToUser(Long userId, String message) {
        WebSocketSession session = userSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                logger.debug("消息已发送给用户 {}: {}", userId, message);
            } catch (IOException e) {
                logger.error("向用户 {} 发送消息失败: {}", userId, e.getMessage());
                // 如果发送失败，可能是连接已断开但未检测到，移除该会话
                userSessionMap.remove(userId);
            }
        } else {
            logger.debug("无法发送消息给用户 {}: 会话不存在或已关闭", userId);
        }
    }

    // 静态方法：广播消息
    public static void broadcast(String message) {
        for (Map.Entry<Long, WebSocketSession> entry : userSessionMap.entrySet()) {
            WebSocketSession session = entry.getValue();
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                    logger.debug("广播消息已发送给用户 {}", entry.getKey());
                } catch (IOException e) {
                    logger.error("向用户 {} 广播消息失败: {}", entry.getKey(), e.getMessage());
                    // 移除无效会话
                    userSessionMap.remove(entry.getKey());
                }
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

    // 静态方法：向指定用户推送私信
    public static void sendPrivateMessage(Long userId, Long senderId, String senderName, String content, Long messageId) {
        String json = String.format("{\"type\":\"PRIVATE_MESSAGE\",\"title\":\"新的私信\",\"senderId\":%d,\"senderName\":\"%s\",\"content\":\"%s\",\"messageId\":%d,\"timestamp\":%d}",
                senderId, escape(senderName), escape(content), messageId, System.currentTimeMillis());
        sendToUser(userId, json);
        logger.info("私信已发送给用户 {}, 发送者: {}", userId, senderId);
    }

    // 简单转义，防止JSON格式错误
    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // 从URL中获取userId
    private Long getUserId(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            // 例如 /ws/123
            String[] parts = path.split("/");
            return Long.parseLong(parts[parts.length - 1]);
        } catch (Exception e) {
            logger.error("无法从会话URL提取用户ID: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取当前在线用户数
     * @return 在线用户数
     */
    public static int getOnlineUserCount() {
        return userSessionMap.size();
    }
    
    /**
     * 检查用户是否在线
     * @param userId 用户ID
     * @return 是否在线
     */
    public static boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessionMap.get(userId);
        return session != null && session.isOpen();
    }
}
