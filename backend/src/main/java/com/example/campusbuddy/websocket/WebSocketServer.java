package com.example.campusbuddy.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * WebSocket服务端点
 * 处理实时通知和私信功能
 */
@Slf4j
@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    // 存放每个客户端对应的WebSocketServer对象
    private static Map<Long, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    // 接收用户ID
    private Long userId;
    
    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        this.session = session;
        this.userId = userId;
        webSocketMap.put(userId, this);
        log.info("用户{}连接WebSocket，当前在线人数为:{}", userId, webSocketMap.size());
        
        try {
            sendMessage(createMessage("CONNECTION", "连接成功", null));
        } catch (IOException e) {
            log.error("发送连接成功消息失败", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (userId != null) {
            webSocketMap.remove(userId);
            log.info("用户{}断开WebSocket连接，当前在线人数为:{}", userId, webSocketMap.size());
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户{}发送消息:{}", userId, message);
        try {
            // 解析客户端消息
            Map<String, Object> messageData = objectMapper.readValue(message, Map.class);
            String type = (String) messageData.get("type");
            
            switch (type) {
                case "PING":
                    // 心跳检测
                    sendMessage(createMessage("PONG", "pong", null));
                    break;
                case "MARK_READ":
                    // 标记消息已读的处理可以在这里添加
                    break;
                default:
                    log.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户{}的WebSocket发生错误", userId, error);
    }

    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 发送自定义消息
     */
    public static void sendInfo(Long userId, String message) {
        log.info("发送消息到用户{}，消息内容:{}", userId, message);
        if (webSocketMap.containsKey(userId)) {
            try {
                webSocketMap.get(userId).sendMessage(message);
            } catch (IOException e) {
                log.error("发送消息到用户{}失败", userId, e);
            }
        } else {
            log.warn("用户{}不在线，无法发送消息", userId);
        }
    }

    /**
     * 发送通知消息
     */
    public static void sendNotification(Long userId, String title, String content, String type, String relatedLink) {
        try {
            String message = createMessage("NOTIFICATION", title, Map.of(
                "content", content,
                "type", type,
                "relatedLink", relatedLink,
                "timestamp", System.currentTimeMillis()
            ));
            sendInfo(userId, message);
        } catch (Exception e) {
            log.error("发送通知到用户{}失败", userId, e);
        }
    }

    /**
     * 发送私信消息
     */
    public static void sendPrivateMessage(Long userId, Long senderId, String senderName, String content, Long messageId) {
        try {
            String message = createMessage("PRIVATE_MESSAGE", "新的私信", Map.of(
                "senderId", senderId,
                "senderName", senderName,
                "content", content,
                "messageId", messageId,
                "timestamp", System.currentTimeMillis()
            ));
            sendInfo(userId, message);
        } catch (Exception e) {
            log.error("发送私信到用户{}失败", userId, e);
        }
    }

    /**
     * 创建消息格式
     */
    private static String createMessage(String type, String title, Map<String, Object> data) {
        try {
            Map<String, Object> message = Map.of(
                "type", type,
                "title", title,
                "data", data != null ? data : Map.of(),
                "timestamp", System.currentTimeMillis()
            );
            return objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("创建消息格式失败", e);
            return "";
        }
    }

    /**
     * 获取在线用户数量
     */
    public static int getOnlineCount() {
        return webSocketMap.size();
    }

    /**
     * 检查用户是否在线
     */
    public static boolean isUserOnline(Long userId) {
        return webSocketMap.containsKey(userId);
    }

    /**
     * 广播消息给所有在线用户
     */
    public static void broadcast(String message) {
        for (WebSocketServer webSocketServer : webSocketMap.values()) {
            try {
                webSocketServer.sendMessage(message);
            } catch (IOException e) {
                log.error("广播消息失败", e);
            }
        }
    }
}
