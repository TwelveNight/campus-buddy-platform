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
 * 私信实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("private_message")
public class PrivateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    /**
     * 发送者用户ID
     */
    private Long senderId;

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

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
