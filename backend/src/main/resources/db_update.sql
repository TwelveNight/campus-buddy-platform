-- 给 review 表添加 module_type 字段（如未存在）
ALTER TABLE review ADD COLUMN module_type VARCHAR(32) DEFAULT NULL COMMENT '所属模块类型（如HELP、GROUP等）2025-05-22 15:49:13';

-- 用 help_info.type 批量补齐历史数据
UPDATE review r
LEFT JOIN help_info h ON r.related_info_id =     
WHERE r.module_type IS NULL;