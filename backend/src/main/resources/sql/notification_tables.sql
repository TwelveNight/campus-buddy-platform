-- 消息通知表
CREATE TABLE IF NOT EXISTS notification (
    notification_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    recipient_id BIGINT NOT NULL COMMENT '接收者用户ID',
    sender_id BIGINT COMMENT '发送者用户ID（系统通知可为null）',
    type ENUM(
        'SYSTEM_ANNOUNCEMENT',      -- 系统公告
        'SYSTEM_ACTIVITY',          -- 活动通知
        'HELP_NEW_APPLICATION',     -- 新的互助申请
        'HELP_APPLICATION_ACCEPTED', -- 申请被接受
        'HELP_APPLICATION_REJECTED', -- 申请被拒绝
        'HELP_COMPLETED',           -- 互助完成
        'HELP_NEW_REVIEW',          -- 新的评价
        'GROUP_JOIN_APPROVED',      -- 加入小组申请被批准
        'GROUP_JOIN_REJECTED',      -- 加入小组申请被拒绝
        'GROUP_INVITATION',         -- 被邀请加入小组
        'GROUP_ANNOUNCEMENT',       -- 小组公告
        'GROUP_ADMIN_ASSIGNED'      -- 被设为管理员
    ) NOT NULL COMMENT '通知类型',
    title VARCHAR(100) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    related_id BIGINT COMMENT '相关ID（如互助ID、小组ID等）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (recipient_id) REFERENCES user(user_id),
    FOREIGN KEY (sender_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 私信表（可选功能）
CREATE TABLE IF NOT EXISTS private_message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    sender_id BIGINT NOT NULL COMMENT '发送者用户ID',
    recipient_id BIGINT NOT NULL COMMENT '接收者用户ID',
    content TEXT NOT NULL COMMENT '消息内容',
    is_read BOOLEAN DEFAULT FALSE COMMENT '是否已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (sender_id) REFERENCES user(user_id),
    FOREIGN KEY (recipient_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
