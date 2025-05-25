package com.example.campusbuddy.vo;

import com.example.campusbuddy.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知视图对象
 */
@Data
public class NotificationVO {
    
    /**
     * 通知ID
     */
    private Long notificationId;
    
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
     * 通知类型
     */
    private String type;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 相关ID（如互助ID、小组ID等）
     */
    private Long relatedId;
    
    /**
     * 相关链接（前端路由地址）
     */
    private String relatedLink;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
