package com.example.campusbuddy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 好友申请实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("friend_request")
public class FriendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请ID
     */
    @TableId(value = "request_id", type = IdType.AUTO)
    private Long requestId;

    /**
     * 申请者ID
     */
    private Long requesterId;

    /**
     * 接收者ID
     */
    private Long recipientId;

    /**
     * 申请消息
     */
    private String requestMessage;

    /**
     * 申请状态：PENDING-待处理，ACCEPTED-已接受，REJECTED-已拒绝
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
