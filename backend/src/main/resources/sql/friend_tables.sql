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

-- 将通知类型改为 VARCHAR，避免 ENUM 扩展困难
ALTER TABLE notification MODIFY COLUMN type VARCHAR(50) NOT NULL COMMENT '通知类型';
