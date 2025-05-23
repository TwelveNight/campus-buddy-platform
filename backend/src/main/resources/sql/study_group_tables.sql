-- 学习小组表
CREATE TABLE IF NOT EXISTS study_group (
    group_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '小组ID',
    name VARCHAR(100) NOT NULL COMMENT '小组名称',
    description TEXT COMMENT '小组描述',
    avatar_url VARCHAR(255) COMMENT '小组头像URL',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    join_type ENUM('PUBLIC', 'APPROVAL') DEFAULT 'PUBLIC' COMMENT '加入方式',
    category VARCHAR(50) COMMENT '小组分类',
    tags VARCHAR(255) COMMENT '小组标签(JSON数组字符串)',
    member_count INT DEFAULT 0 COMMENT '成员数量',
    post_count INT DEFAULT 0 COMMENT '帖子数量',
    status ENUM('ACTIVE', 'INACTIVE', 'DISBANDED') DEFAULT 'ACTIVE' COMMENT '小组状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (creator_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 小组成员表
CREATE TABLE IF NOT EXISTS group_member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    group_id BIGINT NOT NULL COMMENT '小组ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role ENUM('MEMBER', 'ADMIN', 'CREATOR') DEFAULT 'MEMBER' COMMENT '角色',
    status ENUM('ACTIVE', 'PENDING_APPROVAL', 'BANNED') DEFAULT 'ACTIVE' COMMENT '状态',
    joined_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    UNIQUE KEY uk_group_user (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES study_group(group_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 小组帖子表
CREATE TABLE IF NOT EXISTS group_post (
    post_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    group_id BIGINT NOT NULL COMMENT '小组ID',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    title VARCHAR(100) NOT NULL COMMENT '帖子标题',
    content TEXT COMMENT '帖子内容',
    content_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '内容类型',
    like_count INT DEFAULT 0 COMMENT '点赞数量',
    comment_count INT DEFAULT 0 COMMENT '评论数量',
    status ENUM('PUBLISHED', 'DELETED') DEFAULT 'PUBLISHED' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (group_id) REFERENCES study_group(group_id),
    FOREIGN KEY (author_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 小组文件表
CREATE TABLE IF NOT EXISTS group_file (
    file_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    group_id BIGINT NOT NULL COMMENT '小组ID',
    uploader_id BIGINT NOT NULL COMMENT '上传者ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_type VARCHAR(20) COMMENT '文件类型',
    file_size BIGINT COMMENT '文件大小(字节)',
    file_url VARCHAR(255) COMMENT '文件存储路径',
    description VARCHAR(255) COMMENT '文件描述',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    status ENUM('AVAILABLE', 'DELETED') DEFAULT 'AVAILABLE' COMMENT '状态',
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (group_id) REFERENCES study_group(group_id),
    FOREIGN KEY (uploader_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 帖子点赞表
CREATE TABLE IF NOT EXISTS post_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_post_user (post_id, user_id),
    FOREIGN KEY (post_id) REFERENCES group_post(post_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 帖子评论表
CREATE TABLE IF NOT EXISTS post_comment (
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT COMMENT '父评论ID，用于回复',
    status ENUM('PUBLISHED', 'DELETED') DEFAULT 'PUBLISHED' COMMENT '状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (post_id) REFERENCES group_post(post_id),
    FOREIGN KEY (user_id) REFERENCES user(user_id),
    FOREIGN KEY (parent_id) REFERENCES post_comment(comment_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
