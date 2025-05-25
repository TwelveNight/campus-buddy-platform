package com.example.campusbuddy.dto;

import lombok.Data;

/**
 * 创建通知DTO
 */
@Data
public class NotificationCreateDTO {

    /**
     * 接收者用户ID
     */
    private Long recipientId;

    /**
     * 发送者用户ID（系统通知可为null）
     */
    private Long senderId;

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
     * 相关ID（如互助ID、小组ID等）
     */
    private Long relatedId;
}
