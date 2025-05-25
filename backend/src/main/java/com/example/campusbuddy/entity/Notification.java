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
 * 消息通知实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 通知ID
     */
    @TableId(value = "notification_id", type = IdType.AUTO)
    private Long notificationId;

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
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 相关ID（如互助ID、小组ID等）
     */
    private Long relatedId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        SYSTEM_ANNOUNCEMENT,      // 系统公告
        SYSTEM_ACTIVITY,          // 活动通知
        HELP_NEW_APPLICATION,     // 新的互助申请
        HELP_APPLICATION_ACCEPTED, // 申请被接受
        HELP_APPLICATION_REJECTED, // 申请被拒绝
        HELP_COMPLETED,           // 互助完成
        HELP_NEW_REVIEW,          // 新的评价
        GROUP_JOIN_APPROVED,      // 加入小组申请被批准
        GROUP_JOIN_REJECTED,      // 加入小组申请被拒绝
        GROUP_INVITATION,         // 被邀请加入小组
        GROUP_ANNOUNCEMENT,       // 小组公告
        GROUP_ADMIN_ASSIGNED      // 被设为管理员
    }
}
