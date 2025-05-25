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
}
