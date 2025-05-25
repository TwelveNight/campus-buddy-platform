package com.example.campusbuddy.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 好友申请VO
 */
@Data
public class FriendRequestVO {
    /**
     * 申请ID
     */
    private Long requestId;
    
    /**
     * 申请者ID
     */
    private Long requesterId;
    
    /**
     * 申请者信息
     */
    private UserVO requester;
    
    /**
     * 接收者ID
     */
    private Long recipientId;
    
    /**
     * 接收者信息
     */
    private UserVO recipient;
    
    /**
     * 申请消息
     */
    private String requestMessage;
    
    /**
     * 申请状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
