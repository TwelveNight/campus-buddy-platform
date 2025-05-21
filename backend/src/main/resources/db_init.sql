-- 用户表
CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名/学号/邮箱',
    password_hash VARCHAR(100) NOT NULL COMMENT '加密密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像',
    gender ENUM('MALE', 'FEMALE', 'UNKNOWN') DEFAULT 'UNKNOWN' COMMENT '性别',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(20) COMMENT '年级',
    contact_info VARCHAR(255) COMMENT '联系方式',
    skill_tags VARCHAR(255) COMMENT '技能标签(JSON字符串)',
    credit_score INT DEFAULT 100 COMMENT '信用分',
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- 互助信息表
CREATE TABLE IF NOT EXISTS help_info (
    info_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '信息ID',
    publisher_id BIGINT NOT NULL COMMENT '发布用户ID',
    type ENUM(
        'COURSE_TUTORING',
        'SKILL_LEARNING',
        'ITEM_LEND',
        'ITEM_EXCHANGE',
        'TEAM_UP'
    ) NOT NULL COMMENT '互助类型',
    title VARCHAR(100) NOT NULL COMMENT '标题',
    description TEXT COMMENT '描述',
    expected_time VARCHAR(100) COMMENT '期望时间',
    expected_location VARCHAR(100) COMMENT '期望地点',
    contact_method VARCHAR(100) COMMENT '联系方式',
    reward_amount DECIMAL(10, 2) COMMENT '悬赏金额',
    image_urls VARCHAR(500) COMMENT '图片链接(JSON)',
    status ENUM(
        'OPEN',
        'IN_PROGRESS',
        'RESOLVED',
        'EXPIRED',
        'CLOSED'
    ) DEFAULT 'OPEN' COMMENT '状态',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- 互助申请表
CREATE TABLE IF NOT EXISTS help_application (
    application_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    info_id BIGINT NOT NULL COMMENT '互助信息ID',
    applicant_id BIGINT NOT NULL COMMENT '申请用户ID',
    publisher_id BIGINT NOT NULL COMMENT '信息发布者ID',
    message VARCHAR(255) COMMENT '申请附言',
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CANCELED') DEFAULT 'PENDING' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (info_id) REFERENCES help_info(info_id),
    FOREIGN KEY (applicant_id) REFERENCES user(user_id),
    FOREIGN KEY (publisher_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- 学习小组表
CREATE TABLE IF NOT EXISTS study_group (
    group_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '小组ID',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    group_name VARCHAR(100) NOT NULL COMMENT '小组名称',
    description TEXT COMMENT '描述',
    avatar_url VARCHAR(255) COMMENT '小组头像',
    join_type ENUM('PUBLIC', 'APPROVAL_NEEDED') DEFAULT 'PUBLIC' COMMENT '加入方式',
    group_status ENUM('ACTIVE', 'DISBANDED') DEFAULT 'ACTIVE' COMMENT '小组状态',
    member_count INT DEFAULT 1 COMMENT '成员数量',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- 小组成员表
CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    group_id BIGINT NOT NULL COMMENT '小组ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('MEMBER', 'ADMIN', 'CREATOR') DEFAULT 'MEMBER' COMMENT '成员角色',
    status ENUM('ACTIVE', 'PENDING_APPROVAL', 'BANNED') DEFAULT 'ACTIVE' COMMENT '成员状态',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES study_group(group_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- 共享资源表
CREATE TABLE IF NOT EXISTS shared_resource (
    resource_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资源ID',
    publisher_id BIGINT NOT NULL COMMENT '发布用户ID',
    group_id BIGINT COMMENT '所属小组ID',
    resource_name VARCHAR(100) NOT NULL COMMENT '资源名称',
    description TEXT COMMENT '描述',
    file_url VARCHAR(255) COMMENT '文件路径/链接',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (publisher_id) REFERENCES user(user_id),
    FOREIGN KEY (group_id) REFERENCES study_group(group_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- 评价表
CREATE TABLE IF NOT EXISTS review (
    review_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    reviewed_user_id BIGINT NOT NULL COMMENT '被评用户ID',
    reviewer_user_id BIGINT NOT NULL COMMENT '评价用户ID',
    related_info_id BIGINT COMMENT '关联信息ID',
    score INT NOT NULL COMMENT '评分',
    content VARCHAR(255) COMMENT '评价内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reviewed_user_id) REFERENCES user(user_id),
    FOREIGN KEY (reviewer_user_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
-- 可根据需求继续扩展消息通知、标签等表。