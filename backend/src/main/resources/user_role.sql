-- 创建用户角色表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    `created_at` datetime DEFAULT NULL COMMENT '创建时间',
    `updated_at` datetime DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户角色表';
-- 创建默认管理员角色数据
INSERT INTO `user_role` (
        `user_id`,
        `role_name`,
        `created_at`,
        `updated_at`
    )
VALUES (1, 'ROLE_ADMIN', NOW(), NOW());
-- 为所有现有用户创建普通用户角色
INSERT INTO `user_role` (
        `user_id`,
        `role_name`,
        `created_at`,
        `updated_at`
    )
SELECT `user_id`,
    'ROLE_USER',
    NOW(),
    NOW()
FROM `user`
WHERE `user_id` NOT IN (
        SELECT `user_id`
        FROM `user_role`
    );