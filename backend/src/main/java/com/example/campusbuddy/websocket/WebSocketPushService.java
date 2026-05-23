package com.example.campusbuddy.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketPushService {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPushService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(Long userId, String title, String content, String type, String relatedLink) {
        sendNotification(userId, null, title, content, type, relatedLink);
    }

    public void sendNotification(Long userId, Long notificationId, String title, String content, String type, String relatedLink) {
        if (userId == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NOTIFICATION");
        payload.put("notificationId", notificationId);
        payload.put("title", title);
        payload.put("content", content);
        payload.put("typeValue", type);
        payload.put("relatedLink", relatedLink);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", payload);
    }

    public void sendPrivateMessage(
            Long userId,
            Long messageId,
            Long senderId,
            String senderName,
            String senderAvatar,
            String content,
            String messageType,
            String imageUrl
    ) {
        if (userId == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "PRIVATE_MESSAGE");
        payload.put("messageId", messageId);
        payload.put("senderId", senderId);
        payload.put("senderName", senderName);
        payload.put("senderAvatar", senderAvatar);
        payload.put("content", content);
        payload.put("messageType", messageType);
        payload.put("imageUrl", imageUrl);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/messages", payload);
    }

    public void sendMessageReadReceipt(Long userId, Long readerId, Iterable<Long> messageIds) {
        if (userId == null || messageIds == null) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MESSAGE_READ");
        payload.put("readerId", readerId);
        payload.put("messageIds", messageIds);
        payload.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/messages", payload);
    }
}
