-- 好友申请表
CREATE TABLE IF NOT EXISTS friend_request (
    request_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    requester_id BIGINT NOT NULL COMMENT '申请者ID',
    recipient_id BIGINT NOT NULL COMMENT '接收者ID',
    request_message VARCHAR(255) COMMENT '申请消息',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING' COMMENT '申请状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (requester_id) REFERENCES user(user_id),
    FOREIGN KEY (recipient_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 好友关系表
CREATE TABLE IF NOT EXISTS friend (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (friend_id) REFERENCES user(user_id),
    UNIQUE KEY uk_user_friend (user_id, friend_id) COMMENT '用户-好友唯一索引'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 添加好友相关的通知类型
ALTER TABLE notification MODIFY COLUMN type ENUM(
    'SYSTEM_ANNOUNCEMENT',      -- 系统公告
    'SYSTEM_ACTIVITY',          -- 活动通知
    'HELP_NEW_APPLICATION',     -- 新的互助申请
    'HELP_APPLICATION_ACCEPTED', -- 申请被接受
    'HELP_APPLICATION_REJECTED', -- 申请被拒绝
    'HELP_COMPLETED',           -- 互助完成
    'HELP_NEW_REVIEW',          -- 新的评价
    'GROUP_JOIN_APPLICATION',   -- 用户申请加入小组
    'GROUP_JOIN_APPROVED',      -- 加入小组申请被批准
    'GROUP_JOIN_REJECTED',      -- 加入小组申请被拒绝
    'GROUP_INVITATION',         -- 被邀请加入小组
    'GROUP_ANNOUNCEMENT',       -- 小组公告
    'GROUP_ADMIN_ASSIGNED',     -- 被设为管理员
    'FRIEND_REQUEST',           -- 好友申请
    'FRIEND_REQUEST_ACCEPTED',  -- 好友申请被接受
    'FRIEND_REQUEST_REJECTED'   -- 好友申请被拒绝
) NOT NULL COMMENT '通知类型';
