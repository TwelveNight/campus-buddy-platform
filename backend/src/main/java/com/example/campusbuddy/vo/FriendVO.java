package com.example.campusbuddy.vo;

import lombok.Data;

/**
 * 好友信息VO
 */
@Data
public class FriendVO {
    /**
     * 关系ID
     */
    private Long id;
    
    /**
     * 好友用户ID
     */
    private Long friendId;
    
    /**
     * 好友用户信息
     */
    private UserVO friend;
}
