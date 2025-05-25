package com.example.campusbuddy.dto;

import lombok.Data;

/**
 * 好友申请DTO
 */
@Data
public class FriendRequestDTO {
    /**
     * 接收者ID
     */
    private Long recipientId;
    
    /**
     * 申请消息
     */
    private String requestMessage;
}
