package com.example.campusbuddy.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信视图对象
 */
@Data
public class PrivateMessageVO {
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者名称
     */
    private String senderName;
    
    /**
     * 发送者头像
     */
    private String senderAvatar;
    
    /**
     * 接收者ID
     */
    private Long recipientId;
    
    /**
     * 接收者名称
     */
    private String recipientName;
    
    /**
     * 接收者头像
     */
    private String recipientAvatar;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型: TEXT, IMAGE, EMOJI
     */
    private String messageType;
    
    /**
     * 图片URL（当messageType为IMAGE时使用）
     */
    private String imageUrl;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
