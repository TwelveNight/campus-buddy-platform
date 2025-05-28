package com.example.campusbuddy.dto;

import lombok.Data;

/**
 * 发送私信DTO
 */
@Data
public class PrivateMessageDTO {

    /**
     * 接收者用户ID
     */
    private Long recipientId;

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
}
