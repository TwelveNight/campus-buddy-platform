-- 为私信表添加消息类型和图片URL字段
-- 支持表情符号和图片消息功能

-- 添加消息类型字段
ALTER TABLE private_message 
ADD COLUMN message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型: TEXT, IMAGE, EMOJI' 
AFTER content;

-- 添加图片URL字段  
ALTER TABLE private_message 
ADD COLUMN image_url VARCHAR(500) NULL COMMENT '图片URL（当messageType为IMAGE时使用）' 
AFTER message_type;

-- 添加索引优化查询性能
CREATE INDEX idx_message_type ON private_message(message_type);
CREATE INDEX idx_created_at ON private_message(created_at);

-- 更新现有数据的消息类型为TEXT（如果有的话）
UPDATE private_message SET message_type = 'TEXT' WHERE message_type IS NULL;
